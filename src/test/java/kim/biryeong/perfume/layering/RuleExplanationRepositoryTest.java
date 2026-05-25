package kim.biryeong.perfume.layering;

import static org.assertj.core.api.Assertions.assertThat;

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
      RuleExplanation explanation = repository.findByCode(code);

      assertThat(explanation.template()).isNotBlank();
      assertThat(explanation.severity()).isIn("reason", "warning");
    }
  }
}
