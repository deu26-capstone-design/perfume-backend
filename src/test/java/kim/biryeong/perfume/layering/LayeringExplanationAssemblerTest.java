package kim.biryeong.perfume.layering;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kim.biryeong.perfume.layering.model.AccordNarrative;
import kim.biryeong.perfume.layering.model.AccordWeight;
import kim.biryeong.perfume.layering.model.LayeringCandidate;
import kim.biryeong.perfume.layering.model.LayeringColor;
import kim.biryeong.perfume.layering.model.LayeringEvidence;
import kim.biryeong.perfume.layering.model.LayeringEvidenceCode;
import kim.biryeong.perfume.layering.model.LayeringPerfumeProfile;
import kim.biryeong.perfume.layering.model.LayeringScore;
import kim.biryeong.perfume.layering.model.NoteProfile;
import kim.biryeong.perfume.layering.model.RoleVector;
import kim.biryeong.perfume.layering.model.RuleExplanation;
import org.junit.jupiter.api.Test;

class LayeringExplanationAssemblerTest {

  @Test
  void bestForFallsBackToBothAccordsSeasonsAndOccasions() {
    LayeringExplanationAssembler assembler =
        new LayeringExplanationAssembler(
            new StubNarrativeRepository(), new StubRuleExplanationRepository());
    LayeringScore score = new LayeringScore(80, 40, 30, 10, 0, null, List.of());
    LayeringColor color = new LayeringColor("Clear Mint", "#ccddee", "Source", "Target", "맑은 조합");

    List<String> bestFor = assembler.assemble(candidate(1L, 2L), score, color).bestFor();

    assertThat(bestFor).containsExactly("봄", "여름", "오피스");
  }

  @Test
  void usesCanonicalSeedForExplanationVariants() {
    LayeringExplanationAssembler assembler =
        new LayeringExplanationAssembler(
            new StubNarrativeRepository(), new SeedEchoRuleExplanationRepository());
    LayeringScore score =
        new LayeringScore(
            80,
            40,
            30,
            10,
            0,
            null,
            List.of(
                LayeringEvidence.accordPair(
                    LayeringEvidenceCode.HIGH_MATRIX_COMPATIBILITY, "Source", "Target")));
    LayeringColor color = new LayeringColor("Clear Mint", "#ccddee", "Source", "Target", "맑은 조합");

    var forward = assembler.assemble(candidate(1L, 2L), score, color);
    var reversed = assembler.assemble(candidate(2L, 1L), score, color);

    assertThat(reversed.title()).isEqualTo(forward.title());
    assertThat(reversed.summary()).isEqualTo(forward.summary());
    assertThat(reversed.reasons()).isEqualTo(forward.reasons());
  }

  @Test
  void usesCanonicalSeedForReversedAccordPairEvidence() {
    LayeringExplanationAssembler assembler =
        new LayeringExplanationAssembler(
            new StubNarrativeRepository(), new SeedEchoRuleExplanationRepository());
    LayeringScore score =
        new LayeringScore(
            80,
            40,
            30,
            10,
            0,
            null,
            List.of(
                LayeringEvidence.accordPair(
                    LayeringEvidenceCode.HIGH_MATRIX_COMPATIBILITY, "Source", "Target")));
    LayeringScore reversedScore =
        new LayeringScore(
            80,
            40,
            30,
            10,
            0,
            null,
            List.of(
                LayeringEvidence.accordPair(
                    LayeringEvidenceCode.HIGH_MATRIX_COMPATIBILITY, "Target", "Source")));
    LayeringColor color = new LayeringColor("Clear Mint", "#ccddee", "Source", "Target", "맑은 조합");

    List<String> reasons = assembler.assemble(candidate(1L, 2L), score, color).reasons();
    List<String> reversedReasons =
        assembler.assemble(candidate(1L, 2L), reversedScore, color).reasons();

    assertThat(reversedReasons).isEqualTo(reasons);
  }

  @Test
  void differentPerfumePairsCanUseDifferentExplanationVariants() {
    LayeringExplanationAssembler assembler =
        new LayeringExplanationAssembler(
            new StubNarrativeRepository(), new SeedEchoRuleExplanationRepository());
    LayeringScore score = new LayeringScore(80, 40, 30, 10, 0, null, List.of());
    LayeringColor color = new LayeringColor("Clear Mint", "#ccddee", "Source", "Target", "맑은 조합");

    List<String> firstReasons = assembler.assemble(candidate(1L, 2L), score, color).reasons();
    List<String> secondReasons = assembler.assemble(candidate(1L, 3L), score, color).reasons();

    assertThat(secondReasons).isNotEqualTo(firstReasons);
  }

  @Test
  void limitsReasonsAndWarnings() {
    LayeringExplanationAssembler assembler =
        new LayeringExplanationAssembler(
            new StubNarrativeRepository(), new StubRuleExplanationRepository());
    LayeringScore score =
        new LayeringScore(
            80,
            40,
            30,
            10,
            0,
            null,
            List.of(
                LayeringEvidence.simple(LayeringEvidenceCode.SWEET_OVERLOAD),
                LayeringEvidence.simple(LayeringEvidenceCode.DARK_OVERLOAD),
                LayeringEvidence.simple(LayeringEvidenceCode.FRESH_WITHOUT_BASE),
                LayeringEvidence.accordPair(
                    LayeringEvidenceCode.HIGH_MATRIX_COMPATIBILITY, "Source", "Target"),
                LayeringEvidence.perfumeAccord(
                    LayeringEvidenceCode.BASE_ANCHOR_PRESENT, "Perfume", "Source"),
                LayeringEvidence.perfumeAccord(
                    LayeringEvidenceCode.TOP_LIFT_PRESENT, "Perfume", "Target")));
    LayeringColor color = new LayeringColor("Clear Mint", "#ccddee", "Source", "Target", "맑은 조합");

    var recommendation = assembler.assemble(candidate(1L, 2L), score, color);

    assertThat(recommendation.reasons()).hasSize(3);
    assertThat(recommendation.warnings()).hasSize(2);
  }

  private static class StubNarrativeRepository extends AccordNarrativeRepository {

    @Override
    public AccordNarrative findByAccord(String accordName) {
      return switch (accordName) {
        case "Source" -> narrative("Source", "소스", List.of("spring"), List.of("office"));
        case "Target" -> narrative("Target", "타깃", List.of("summer"), List.of("date"));
        default -> throw new IllegalArgumentException("Unknown test accord: " + accordName);
      };
    }
  }

  private static class StubRuleExplanationRepository extends RuleExplanationRepository {

    @Override
    public RuleExplanation findByCode(LayeringEvidenceCode code, String seed) {
      if (code == LayeringEvidenceCode.SWEET_OVERLOAD
          || code == LayeringEvidenceCode.DARK_OVERLOAD
          || code == LayeringEvidenceCode.FRESH_WITHOUT_BASE) {
        return new RuleExplanation(code, "warning", "test", "warning");
      }
      return new RuleExplanation(code, "reason", "test", "{colorName}");
    }
  }

  private static class SeedEchoRuleExplanationRepository extends RuleExplanationRepository {

    @Override
    public RuleExplanation findByCode(LayeringEvidenceCode code, String seed) {
      return new RuleExplanation(code, "reason", "seed", seed);
    }
  }

  private static AccordNarrative narrative(
      String accordName, String displayNameKo, List<String> seasons, List<String> occasions) {
    return new AccordNarrative(
        accordName,
        displayNameKo,
        accordName,
        "top",
        "top",
        "medium",
        "medium",
        "smooth",
        "clear",
        "calm",
        seasons,
        occasions,
        List.of("note"),
        "잘 어울립니다.",
        "강해질 수 있습니다.",
        "밝은",
        "조화롭습니다.");
  }

  private static LayeringCandidate candidate(long firstId, long secondId) {
    return new LayeringCandidate(profile(firstId), profile(secondId));
  }

  private static LayeringPerfumeProfile profile(long id) {
    return new LayeringPerfumeProfile(
        id,
        "Brand",
        "Perfume " + id,
        List.of(new AccordWeight("Source", 100, 1.0)),
        new NoteProfile(List.of(), List.of(), List.of()),
        new RoleVector(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
  }
}
