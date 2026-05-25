package kim.biryeong.perfume.layering;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import kim.biryeong.perfume.layering.model.AccordPair;
import kim.biryeong.perfume.layering.model.LayeringColor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LayeringColorPalette {

  private static final String PATH = "data/layering_accord_colors.csv";
  private static final Pattern HEX_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

  private final LayeringCompatibilityMatrix matrix;
  private final Map<AccordPair, LayeringColor> colors = new HashMap<>();

  @PostConstruct
  void load() {
    for (String[] row : LayeringCsvReader.read(PATH, 5)) {
      String source = AccordNameNormalizer.normalize(row[0]);
      String target = AccordNameNormalizer.normalize(row[1]);
      String colorName = required(row[2], source, target, "color_name");
      String hex = required(row[3], source, target, "hex");
      String description = required(row[4], source, target, "description");
      if (!HEX_PATTERN.matcher(hex).matches()) {
        throw new IllegalStateException(
            "Invalid hex for layering color pair: " + source + "/" + target);
      }
      AccordPair pair = new AccordPair(source, target);
      if (colors.put(pair, new LayeringColor(colorName, hex, source, target, description))
          != null) {
        throw new IllegalStateException("Duplicate layering color pair: " + source + "/" + target);
      }
    }
    validateCompleteness();
  }

  public LayeringColor findByPair(AccordPair pair) {
    LayeringColor color = colors.get(pair);
    if (color == null) {
      throw new IllegalArgumentException(
          "Unknown layering color pair: " + pair.sourceAccord() + "/" + pair.targetAccord());
    }
    return color;
  }

  public int size() {
    return colors.size();
  }

  private void validateCompleteness() {
    Set<AccordPair> expected = new HashSet<>();
    for (String source : matrix.accords()) {
      for (String target : matrix.accords()) {
        expected.add(new AccordPair(source, target));
      }
    }
    if (!colors.keySet().equals(expected)) {
      throw new IllegalStateException("Layering colors must contain every compatibility pair");
    }
  }

  private static String required(String value, String source, String target, String column) {
    String text = value.trim();
    if (text.isBlank()) {
      throw new IllegalStateException(
          "Blank " + column + " for layering color pair: " + source + "/" + target);
    }
    return text;
  }
}
