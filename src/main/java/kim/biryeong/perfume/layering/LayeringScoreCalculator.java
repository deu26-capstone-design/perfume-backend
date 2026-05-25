package kim.biryeong.perfume.layering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kim.biryeong.perfume.layering.model.AccordPair;
import kim.biryeong.perfume.layering.model.AccordWeight;
import kim.biryeong.perfume.layering.model.LayeringCandidate;
import kim.biryeong.perfume.layering.model.LayeringEvidence;
import kim.biryeong.perfume.layering.model.LayeringEvidenceCode;
import kim.biryeong.perfume.layering.model.LayeringPerfumeProfile;
import kim.biryeong.perfume.layering.model.LayeringScore;
import kim.biryeong.perfume.layering.model.RoleVector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LayeringScoreCalculator {

  private final LayeringCompatibilityMatrix matrix;

  public LayeringScore score(LayeringCandidate candidate) {
    List<LayeringPerfumeProfile> orderedProfiles = orderedProfiles(candidate);
    LayeringPerfumeProfile first = orderedProfiles.get(0);
    LayeringPerfumeProfile second = orderedProfiles.get(1);
    double compatibility = compatibility(first, second);
    int structureOffset = structureOffset(first, second);
    int balanceOffset = balanceOffset(first, second, compatibility);
    List<LayeringEvidence> evidences = new ArrayList<>();
    int penalty = penalty(first.roleVector(), second.roleVector(), evidences);
    AccordPair dominantPair = dominantPair(first, second);
    addPositiveEvidences(first, second, compatibility, evidences);

    double rawScore = 50 + (compatibility - 0.50) * 40 + structureOffset + balanceOffset - penalty;
    int finalScore = clamp((int) Math.round(rawScore), 0, 100);
    return new LayeringScore(
        finalScore,
        clamp((int) Math.round(compatibility * 100), 0, 100),
        clamp(50 + structureOffset * 5, 0, 100),
        clamp(50 + balanceOffset * 5, 0, 100),
        penalty,
        dominantPair,
        List.copyOf(evidences));
  }

  private double compatibility(LayeringPerfumeProfile first, LayeringPerfumeProfile second) {
    double score = 0.0;
    for (AccordWeight firstAccord : first.accords()) {
      for (AccordWeight secondAccord : second.accords()) {
        score +=
            firstAccord.normalizedWeight()
                * secondAccord.normalizedWeight()
                * matrix.symmetricScore(firstAccord.name(), secondAccord.name());
      }
    }
    return score;
  }

  private AccordPair dominantPair(LayeringPerfumeProfile first, LayeringPerfumeProfile second) {
    AccordPair pair = null;
    double bestContribution = -1.0;
    for (AccordWeight firstAccord : first.accords()) {
      for (AccordWeight secondAccord : second.accords()) {
        double contribution =
            firstAccord.normalizedWeight()
                * secondAccord.normalizedWeight()
                * matrix.symmetricScore(firstAccord.name(), secondAccord.name());
        if (contribution > bestContribution) {
          bestContribution = contribution;
          pair = new AccordPair(firstAccord.name(), secondAccord.name());
        }
      }
    }
    if (pair == null) {
      throw new IllegalArgumentException("향수 어코드 데이터가 부족합니다.");
    }
    return pair;
  }

  private int structureOffset(LayeringPerfumeProfile first, LayeringPerfumeProfile second) {
    RoleVector firstRole = first.roleVector();
    RoleVector secondRole = second.roleVector();
    int offset = 0;
    double maxBase = Math.max(firstRole.baseAnchor(), secondRole.baseAnchor());
    double maxTop = Math.max(firstRole.topLift(), secondRole.topLift());
    if (maxBase >= 0.35) {
      offset += 6;
    }
    if (maxTop >= 0.35) {
      offset += 5;
    }
    if (maxBase >= 0.35
        && maxTop >= 0.35
        && !baseOwner(first, second).id().equals(topOwner(first, second).id())) {
      offset += 4;
    }
    if (firstRole.topLift() >= 0.45
        && secondRole.topLift() >= 0.45
        && average(firstRole.baseAnchor(), secondRole.baseAnchor()) < 0.25) {
      offset -= 6;
    }
    if (firstRole.baseAnchor() >= 0.45
        && secondRole.baseAnchor() >= 0.45
        && average(firstRole.topLift(), secondRole.topLift()) < 0.25) {
      offset -= 5;
    }
    return offset;
  }

  private int balanceOffset(
      LayeringPerfumeProfile first, LayeringPerfumeProfile second, double compatibility) {
    Set<String> unique = new HashSet<>();
    first.accords().stream().limit(3).map(AccordWeight::name).forEach(unique::add);
    second.accords().stream().limit(3).map(AccordWeight::name).forEach(unique::add);
    int totalTopAccords =
        Math.min(3, first.accords().size()) + Math.min(3, second.accords().size());
    double diversity = totalTopAccords == 0 ? 0.0 : unique.size() / (double) totalTopAccords;
    boolean shared = first.accords().stream().anyMatch(a -> containsAccord(second, a.name()));
    boolean sameDominant = first.dominantAccord().name().equals(second.dominantAccord().name());
    int offset = 0;
    if (diversity >= 0.50 && diversity <= 0.85) {
      offset += 5;
    }
    if (shared && !sameDominant) {
      offset += 3;
    }
    if (sameDominant && diversity < 0.50) {
      offset -= 4;
    }
    if (compatibility < 0.45) {
      offset -= 6;
    }
    return offset;
  }

  private int penalty(RoleVector first, RoleVector second, List<LayeringEvidence> evidences) {
    int penalty = 0;
    double sweetLoad = average(first.sweetness(), second.sweetness());
    if (sweetLoad >= 0.62) {
      penalty += 6;
      evidences.add(LayeringEvidence.simple(LayeringEvidenceCode.SWEET_OVERLOAD));
    }
    double darkLoad = average(first.darkness(), second.darkness());
    if (darkLoad >= 0.62) {
      penalty += 6;
      evidences.add(LayeringEvidence.simple(LayeringEvidenceCode.DARK_OVERLOAD));
    }
    double freshLoad = average(first.freshness(), second.freshness());
    double baseLoad = average(first.baseAnchor(), second.baseAnchor());
    if (freshLoad >= 0.60 && baseLoad < 0.25) {
      penalty += 5;
      evidences.add(LayeringEvidence.simple(LayeringEvidenceCode.FRESH_WITHOUT_BASE));
    }
    return penalty;
  }

  private void addPositiveEvidences(
      LayeringPerfumeProfile first,
      LayeringPerfumeProfile second,
      double compatibility,
      List<LayeringEvidence> evidences) {
    AccordWeight firstDominant = first.dominantAccord();
    AccordWeight secondDominant = second.dominantAccord();
    if (compatibility >= 0.70) {
      evidences.add(
          LayeringEvidence.accordPair(
              LayeringEvidenceCode.HIGH_MATRIX_COMPATIBILITY,
              firstDominant.name(),
              secondDominant.name()));
    } else if (compatibility < 0.50) {
      evidences.add(
          LayeringEvidence.accordPair(
              LayeringEvidenceCode.LOW_MATRIX_COMPATIBILITY,
              firstDominant.name(),
              secondDominant.name()));
    }
    LayeringPerfumeProfile baseOwner = baseOwner(first, second);
    if (baseOwner.roleVector().baseAnchor() >= 0.35) {
      evidences.add(
          LayeringEvidence.perfumeAccord(
              LayeringEvidenceCode.BASE_ANCHOR_PRESENT,
              baseOwner.name(),
              baseOwner.dominantAccord().name()));
    }
    LayeringPerfumeProfile topOwner = topOwner(first, second);
    if (topOwner.roleVector().topLift() >= 0.35) {
      evidences.add(
          LayeringEvidence.perfumeAccord(
              LayeringEvidenceCode.TOP_LIFT_PRESENT,
              topOwner.name(),
              topOwner.dominantAccord().name()));
    }
  }

  private static LayeringPerfumeProfile baseOwner(
      LayeringPerfumeProfile first, LayeringPerfumeProfile second) {
    int comparison =
        Double.compare(first.roleVector().baseAnchor(), second.roleVector().baseAnchor());
    if (comparison > 0) {
      return first;
    }
    if (comparison < 0) {
      return second;
    }
    return first.id() <= second.id() ? first : second;
  }

  private static LayeringPerfumeProfile topOwner(
      LayeringPerfumeProfile first, LayeringPerfumeProfile second) {
    int comparison = Double.compare(first.roleVector().topLift(), second.roleVector().topLift());
    if (comparison > 0) {
      return first;
    }
    if (comparison < 0) {
      return second;
    }
    return first.id() <= second.id() ? first : second;
  }

  private static boolean containsAccord(LayeringPerfumeProfile profile, String accordName) {
    return profile.accords().stream().anyMatch(accord -> accord.name().equals(accordName));
  }

  private static List<LayeringPerfumeProfile> orderedProfiles(LayeringCandidate candidate) {
    return candidate.first().id() <= candidate.second().id()
        ? List.of(candidate.first(), candidate.second())
        : List.of(candidate.second(), candidate.first());
  }

  private static double average(double first, double second) {
    return (first + second) / 2.0;
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
