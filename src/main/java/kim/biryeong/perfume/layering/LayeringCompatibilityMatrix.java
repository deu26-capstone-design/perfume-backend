package kim.biryeong.perfume.layering;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class LayeringCompatibilityMatrix {

  private static final String PATH = "data/layering_accord_compatibility.csv";

  private final Map<String, Map<String, Double>> scores = new HashMap<>();

  @PostConstruct
  void load() {
    for (String[] row : LayeringCsvReader.read(PATH, 3)) {
      String source = AccordNameNormalizer.normalize(row[0]);
      String target = AccordNameNormalizer.normalize(row[1]);
      double score = parseScore(row[2], source, target);
      Map<String, Double> rowScores = scores.computeIfAbsent(source, ignored -> new HashMap<>());
      if (rowScores.put(target, score) != null) {
        throw new IllegalStateException(
            "Duplicate layering compatibility pair: " + source + "/" + target);
      }
    }
    validateCompleteness();
  }

  public double score(String sourceAccord, String targetAccord) {
    String source = AccordNameNormalizer.normalize(sourceAccord);
    String target = AccordNameNormalizer.normalize(targetAccord);
    Double score = scores.getOrDefault(source, Map.of()).get(target);
    if (score == null) {
      throw new IllegalArgumentException("Unknown layering accord pair: " + source + "/" + target);
    }
    return score;
  }

  public double symmetricScore(String firstAccord, String secondAccord) {
    return (score(firstAccord, secondAccord) + score(secondAccord, firstAccord)) / 2.0;
  }

  public Set<String> accords() {
    return Set.copyOf(scores.keySet());
  }

  private void validateCompleteness() {
    Set<String> expected = new HashSet<>(AccordNameNormalizer.SUPPORTED_ACCORDS);
    if (!scores.keySet().equals(expected)) {
      throw new IllegalStateException(
          "Layering compatibility source accords must match supported accords");
    }
    for (String source : AccordNameNormalizer.SUPPORTED_ACCORDS) {
      Set<String> targets = scores.getOrDefault(source, Map.of()).keySet();
      if (!targets.equals(expected)) {
        throw new IllegalStateException(
            source + " compatibility targets must match supported accords");
      }
    }
  }

  private static double parseScore(String raw, String source, String target) {
    try {
      double score = Double.parseDouble(raw.trim());
      if (score < 0.0 || score > 1.0) {
        throw new IllegalStateException(
            "Layering score must be 0.0 to 1.0: " + source + "/" + target);
      }
      return score;
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Invalid layering score: " + source + "/" + target, e);
    }
  }
}
