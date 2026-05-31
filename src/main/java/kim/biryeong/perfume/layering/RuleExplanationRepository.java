package kim.biryeong.perfume.layering;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kim.biryeong.perfume.layering.model.LayeringEvidenceCode;
import kim.biryeong.perfume.layering.model.RuleExplanation;
import org.springframework.stereotype.Component;

@Component
public class RuleExplanationRepository {

  private static final String PATH = "data/layering_rule_explanations.csv";

  private final Map<LayeringEvidenceCode, List<RuleExplanation>> explanations =
      new EnumMap<>(LayeringEvidenceCode.class);

  @PostConstruct
  void load() {
    loadRows(LayeringCsvReader.read(PATH, 4));
  }

  void loadRows(List<String[]> rows) {
    explanations.clear();
    Map<LayeringEvidenceCode, Set<String>> variantKeys = new EnumMap<>(LayeringEvidenceCode.class);
    for (String[] row : rows) {
      LayeringEvidenceCode code = LayeringEvidenceCode.valueOf(row[0].trim());
      String severity = row[1].trim();
      if (!severity.equals("reason") && !severity.equals("warning")) {
        throw new IllegalStateException("Invalid layering rule explanation severity: " + severity);
      }
      String variantKey = row[2].trim();
      if (variantKey.isBlank()) {
        throw new IllegalStateException("Blank layering rule explanation variant key: " + code);
      }
      if (!variantKeys.computeIfAbsent(code, ignored -> new HashSet<>()).add(variantKey)) {
        throw new IllegalStateException(
            "Duplicate layering rule explanation variant: " + code + "/" + variantKey);
      }
      String template = row[3].trim();
      if (template.isBlank()) {
        throw new IllegalStateException("Blank layering rule explanation template: " + code);
      }
      explanations
          .computeIfAbsent(code, ignored -> new ArrayList<>())
          .add(new RuleExplanation(code, severity, variantKey, template));
    }
    if (!explanations.keySet().equals(EnumSet.allOf(LayeringEvidenceCode.class))) {
      throw new IllegalStateException(
          "Layering rule explanations must contain every evidence code");
    }
    explanations.forEach(
        (code, variants) -> {
          if (variants.size() < 2) {
            throw new IllegalStateException(
                "Layering rule explanations must contain at least two variants: " + code);
          }
        });
  }

  public RuleExplanation findByCode(LayeringEvidenceCode code, String seed) {
    List<RuleExplanation> variants = explanations.get(code);
    if (variants == null || variants.isEmpty()) {
      throw new IllegalArgumentException("Unknown layering rule explanation: " + code);
    }
    return variants.get(Math.floorMod((seed == null ? "" : seed).hashCode(), variants.size()));
  }

  public List<RuleExplanation> findAllByCode(LayeringEvidenceCode code) {
    List<RuleExplanation> variants = explanations.get(code);
    if (variants == null) {
      throw new IllegalArgumentException("Unknown layering rule explanation: " + code);
    }
    return List.copyOf(variants);
  }

  public int size() {
    return explanations.size();
  }
}
