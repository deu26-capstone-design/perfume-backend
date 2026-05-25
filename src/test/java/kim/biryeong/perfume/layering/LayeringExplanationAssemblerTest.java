package kim.biryeong.perfume.layering;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kim.biryeong.perfume.layering.model.AccordNarrative;
import kim.biryeong.perfume.layering.model.LayeringColor;
import kim.biryeong.perfume.layering.model.LayeringEvidenceCode;
import kim.biryeong.perfume.layering.model.LayeringScore;
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

    List<String> bestFor = assembler.assemble(score, color).bestFor();

    assertThat(bestFor).containsExactly("봄", "여름", "오피스");
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
    public RuleExplanation findByCode(LayeringEvidenceCode code) {
      return new RuleExplanation(code, "reason", "{colorName}");
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
}
