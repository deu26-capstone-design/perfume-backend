package kim.biryeong.perfume.layering;

import static org.assertj.core.api.Assertions.assertThat;

import kim.biryeong.perfume.layering.model.AccordPair;
import kim.biryeong.perfume.layering.model.LayeringColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LayeringColorPaletteTest {

  private LayeringColorPalette palette;

  @BeforeEach
  void setUp() {
    LayeringCompatibilityMatrix matrix = new LayeringCompatibilityMatrix();
    matrix.load();
    palette = new LayeringColorPalette(matrix);
    palette.load();
  }

  @Test
  void loadsColorForEveryAccordPair() {
    assertThat(palette.size()).isEqualTo(144);
  }

  @Test
  void resolvesDominantPairColor() {
    LayeringColor color = palette.findByPair(new AccordPair("Citrus", "Floral"));

    assertThat(color.name()).isEqualTo("Zest Bloom");
    assertThat(color.hex()).matches("^#[0-9A-Fa-f]{6}$");
    assertThat(color.sourceAccord()).isEqualTo("Citrus");
    assertThat(color.targetAccord()).isEqualTo("Floral");
  }
}
