package kim.biryeong.perfume.layering;

import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import kim.biryeong.perfume.layering.model.LayeringEvidenceCode;
import kim.biryeong.perfume.layering.model.RuleExplanation;
import org.springframework.stereotype.Component;

@Component
public class RuleExplanationRepository {

  private static final String PATH = "data/layering_rule_explanations.csv";

  private final Map<LayeringEvidenceCode, RuleExplanation> explanations =
      new EnumMap<>(LayeringEvidenceCode.class);

  @PostConstruct
  void load() {
    for (String[] row : LayeringCsvReader.read(PATH, 3)) {
      LayeringEvidenceCode code = LayeringEvidenceCode.valueOf(row[0].trim());
      String severity = row[1].trim();
      if (!severity.equals("reason") && !severity.equals("warning")) {
        throw new IllegalStateException("Invalid layering rule explanation severity: " + severity);
      }
      String template = row[2].trim();
      if (template.isBlank()) {
        throw new IllegalStateException("Blank layering rule explanation template: " + code);
      }
      if (explanations.put(code, new RuleExplanation(code, severity, template)) != null) {
        throw new IllegalStateException("Duplicate layering rule explanation: " + code);
      }
    }
    if (!explanations.keySet().equals(EnumSet.allOf(LayeringEvidenceCode.class))) {
      throw new IllegalStateException(
          "Layering rule explanations must contain every evidence code");
    }
  }

  public RuleExplanation findByCode(LayeringEvidenceCode code) {
    RuleExplanation explanation = explanations.get(code);
    if (explanation == null) {
      throw new IllegalArgumentException("Unknown layering rule explanation: " + code);
    }
    return explanation;
  }

  public int size() {
    return explanations.size();
  }
}
