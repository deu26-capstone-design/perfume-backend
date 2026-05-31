package kim.biryeong.perfume.layering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import kim.biryeong.perfume.layering.model.LayeringEvidenceCode;
import kim.biryeong.perfume.layering.model.RuleExplanation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuleExplanationRepositoryTest {

  private RuleExplanationRepository repository;

  @BeforeEach
  void setUp() {
    repository = new RuleExplanationRepository();
    repository.load();
  }

  @Test
  void loadsExplanationForEveryEvidenceCode() {
    assertThat(repository.size()).isEqualTo(LayeringEvidenceCode.values().length);

    for (LayeringEvidenceCode code : LayeringEvidenceCode.values()) {
      RuleExplanation explanation = repository.findByCode(code, "seed");

      assertThat(explanation.template()).isNotBlank();
      assertThat(explanation.severity()).isIn("reason", "warning");
    }
  }

  @Test
  void loadsAtLeastTwoVariantsForEveryEvidenceCode() {
    for (LayeringEvidenceCode code : LayeringEvidenceCode.values()) {
      List<RuleExplanation> variants = repository.findAllByCode(code);

      assertThat(variants).hasSizeGreaterThanOrEqualTo(2);
      assertThat(variants)
          .allSatisfy(
              explanation -> {
                assertThat(explanation.evidenceCode()).isEqualTo(code);
                assertThat(explanation.severity()).isIn("reason", "warning");
                assertThat(explanation.variantKey()).isNotBlank();
                assertThat(explanation.template()).isNotBlank();
              });
    }
  }

  @Test
  void selectsSameVariantForSameSeed() {
    RuleExplanation first =
        repository.findByCode(LayeringEvidenceCode.HIGH_MATRIX_COMPATIBILITY, "same-seed");
    RuleExplanation second =
        repository.findByCode(LayeringEvidenceCode.HIGH_MATRIX_COMPATIBILITY, "same-seed");

    assertThat(second).isEqualTo(first);
  }

  @Test
  void rejectsInvalidSeverity() {
    RuleExplanationRepository invalidRepository = new RuleExplanationRepository();

    assertThatThrownBy(
            () ->
                invalidRepository.loadRows(
                    List.<String[]>of(
                        row("HIGH_MATRIX_COMPATIBILITY", "info", "flow", "유효하지 않은 severity"))))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Invalid layering rule explanation severity");
  }

  @Test
  void rejectsBlankTemplate() {
    RuleExplanationRepository invalidRepository = new RuleExplanationRepository();

    assertThatThrownBy(
            () ->
                invalidRepository.loadRows(
                    List.<String[]>of(row("HIGH_MATRIX_COMPATIBILITY", "reason", "flow", " "))))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Blank layering rule explanation template");
  }

  @Test
  void rejectsDuplicateVariantKey() {
    RuleExplanationRepository invalidRepository = new RuleExplanationRepository();

    assertThatThrownBy(
            () ->
                invalidRepository.loadRows(
                    List.<String[]>of(
                        row("HIGH_MATRIX_COMPATIBILITY", "reason", "flow", "첫 번째"),
                        row("HIGH_MATRIX_COMPATIBILITY", "reason", "flow", "두 번째"))))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Duplicate layering rule explanation variant");
  }

  private static String[] row(
      String evidenceCode, String severity, String variantKey, String template) {
    return new String[] {evidenceCode, severity, variantKey, template};
  }
}
