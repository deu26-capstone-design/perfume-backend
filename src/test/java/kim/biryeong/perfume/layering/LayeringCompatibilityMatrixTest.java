package kim.biryeong.perfume.layering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LayeringCompatibilityMatrixTest {

  private LayeringCompatibilityMatrix matrix;

  @BeforeEach
  void setUp() {
    matrix = new LayeringCompatibilityMatrix();
    matrix.load();
  }

  @Test
  void loadsTwelveByTwelveAccordPairs() {
    assertThat(matrix.accords()).hasSize(12);
    for (String source : AccordNameNormalizer.SUPPORTED_ACCORDS) {
      for (String target : AccordNameNormalizer.SUPPORTED_ACCORDS) {
        assertThat(matrix.score(source, target)).isBetween(0.0, 1.0);
      }
    }
  }

  @Test
  void normalizesEarthyToEarthySmoky() {
    assertThat(matrix.score("Earthy", "Woody")).isEqualTo(matrix.score("Earthy/Smoky", "Woody"));
  }

  @Test
  void rejectsUnknownAccordLookup() {
    assertThrows(IllegalArgumentException.class, () -> matrix.score("Amber", "Woody"));
  }
}
