package kim.biryeong.perfume.layering;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import kim.biryeong.perfume.layering.dto.LayeringColorResponse;
import kim.biryeong.perfume.layering.dto.LayeringRecommendationDto;
import kim.biryeong.perfume.layering.dto.ScoreBreakdownResponse;
import kim.biryeong.perfume.layering.model.AccordNarrative;
import kim.biryeong.perfume.layering.model.LayeringColor;
import kim.biryeong.perfume.layering.model.LayeringDecision;
import kim.biryeong.perfume.layering.model.LayeringEvidence;
import kim.biryeong.perfume.layering.model.LayeringScore;
import kim.biryeong.perfume.layering.model.RuleExplanation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LayeringExplanationAssembler {

  private final AccordNarrativeRepository narrativeRepository;
  private final RuleExplanationRepository ruleExplanationRepository;

  public LayeringRecommendationDto assemble(LayeringScore score, LayeringColor color) {
    AccordNarrative source = narrativeRepository.findByAccord(color.sourceAccord());
    AccordNarrative target = narrativeRepository.findByAccord(color.targetAccord());
    List<LayeringEvidence> evidences = new ArrayList<>(score.evidences());
    evidences.add(LayeringEvidence.color(color.sourceAccord(), color.targetAccord(), color.name()));

    List<String> reasons = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    for (LayeringEvidence evidence : evidences) {
      RuleExplanation explanation = ruleExplanationRepository.findByCode(evidence.code());
      String message = render(explanation.template(), evidence);
      if (explanation.warning()) {
        if (warnings.size() < 2) {
          warnings.add(message);
        }
      } else if (reasons.size() < 3) {
        reasons.add(message);
      }
    }

    LayeringDecision decision = LayeringDecision.fromScore(score.finalScore());
    return new LayeringRecommendationDto(
        "PAIR",
        decision == LayeringDecision.RECOMMENDED,
        decision.name(),
        score.finalScore(),
        title(source, target),
        summary(source, target),
        new LayeringColorResponse(
            color.name(),
            color.hex(),
            color.sourceAccord(),
            color.targetAccord(),
            color.description()),
        bestFor(source, target),
        reasons,
        warnings,
        new ScoreBreakdownResponse(
            score.matrixScore(),
            score.structureScore(),
            score.balanceScore(),
            score.penaltyScore()));
  }

  private static String render(String template, LayeringEvidence evidence) {
    return template
        .replace("{accordA}", text(evidence.accordA()))
        .replace("{accordB}", text(evidence.accordB()))
        .replace("{perfumeName}", text(evidence.perfumeName()))
        .replace("{accord}", text(evidence.accord()))
        .replace("{colorName}", text(evidence.colorName()));
  }

  private static String title(AccordNarrative source, AccordNarrative target) {
    return source.titleAdjective()
        + " "
        + source.displayNameKo()
        + "와 "
        + target.titleAdjective()
        + " "
        + target.displayNameKo();
  }

  private static String summary(AccordNarrative source, AccordNarrative target) {
    return source.summaryPhrase() + " " + target.positivePhrase();
  }

  private static List<String> bestFor(AccordNarrative source, AccordNarrative target) {
    Set<String> result = new LinkedHashSet<>();
    addTranslatedIntersection(result, source.seasonTags(), target.seasonTags());
    addTranslatedIntersection(result, source.occasionTags(), target.occasionTags());
    addTranslated(result, source.seasonTags());
    addTranslated(result, target.seasonTags());
    addTranslated(result, source.occasionTags());
    addTranslated(result, target.occasionTags());
    return result.stream().limit(3).toList();
  }

  private static void addTranslatedIntersection(
      Set<String> result, List<String> first, List<String> second) {
    for (String value : first) {
      if (second.contains(value)) {
        result.add(translate(value));
      }
    }
  }

  private static void addTranslated(Set<String> result, List<String> values) {
    for (String value : values) {
      result.add(translate(value));
    }
  }

  private static String translate(String value) {
    return switch (value) {
      case "spring" -> "봄";
      case "summer" -> "여름";
      case "autumn" -> "가을";
      case "winter" -> "겨울";
      case "all-season" -> "사계절";
      case "office" -> "오피스";
      case "daily" -> "데일리";
      case "casual" -> "캐주얼";
      case "date" -> "데이트";
      case "night" -> "저녁";
      case "special" -> "특별한 날";
      case "contemplative" -> "사색적인 자리";
      default -> value;
    };
  }

  private static String text(String value) {
    return value == null ? "" : value;
  }
}
