package kim.biryeong.perfume.layering;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kim.biryeong.perfume.layering.model.AccordNarrative;
import org.springframework.stereotype.Component;

@Component
public class AccordNarrativeRepository {

  private static final String PATH = "data/layering_accord_narratives.csv";

  private final Map<String, AccordNarrative> narratives = new HashMap<>();

  @PostConstruct
  void load() {
    for (String[] row : LayeringCsvReader.read(PATH, 17)) {
      String accord = AccordNameNormalizer.normalize(row[0]);
      AccordNarrative narrative =
          new AccordNarrative(
              accord,
              required(row[1], accord, "display_name_ko"),
              required(row[2], accord, "display_name_en"),
              required(row[3], accord, "primary_role"),
              required(row[4], accord, "pyramid_tendency"),
              required(row[5], accord, "volatility"),
              required(row[6], accord, "weight"),
              required(row[7], accord, "texture"),
              required(row[8], accord, "impression"),
              required(row[9], accord, "emotion"),
              split(row[10]),
              split(row[11]),
              split(row[12]),
              required(row[13], accord, "positive_phrase"),
              required(row[14], accord, "risk_phrase"),
              required(row[15], accord, "title_adjective"),
              required(row[16], accord, "summary_phrase"));
      if (narratives.put(accord, narrative) != null) {
        throw new IllegalStateException("Duplicate layering narrative accord: " + accord);
      }
    }
    if (!narratives.keySet().equals(Set.copyOf(AccordNameNormalizer.SUPPORTED_ACCORDS))) {
      throw new IllegalStateException("Layering narratives must contain every supported accord");
    }
  }

  public AccordNarrative findByAccord(String accordName) {
    String accord = AccordNameNormalizer.normalize(accordName);
    AccordNarrative narrative = narratives.get(accord);
    if (narrative == null) {
      throw new IllegalArgumentException("Unknown layering accord narrative: " + accord);
    }
    return narrative;
  }

  public int size() {
    return narratives.size();
  }

  private static List<String> split(String value) {
    return List.of(value.trim().split(";")).stream().filter(text -> !text.isBlank()).toList();
  }

  private static String required(String value, String accord, String column) {
    String text = value.trim();
    if (text.isBlank()) {
      throw new IllegalStateException("Blank " + column + " for layering narrative: " + accord);
    }
    return text;
  }
}
