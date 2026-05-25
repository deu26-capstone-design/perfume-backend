package kim.biryeong.perfume.layering;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AccordNameNormalizer {

  public static final List<String> SUPPORTED_ACCORDS =
      List.of(
          "Floral",
          "Woody",
          "Fresh",
          "Spicy",
          "Sweet",
          "Gourmand",
          "Musky",
          "Green",
          "Citrus",
          "Fruity",
          "Aromatic",
          "Earthy/Smoky");

  private static final Map<String, String> NAMES_BY_KEY = namesByKey();

  private AccordNameNormalizer() {}

  public static String normalize(String accordName) {
    if (accordName == null) {
      throw new IllegalArgumentException("Accord name must not be null");
    }
    String key = key(accordName);
    String normalized = NAMES_BY_KEY.get(key);
    if (normalized == null) {
      throw new IllegalArgumentException("Unknown accord name: " + accordName);
    }
    return normalized;
  }

  public static boolean isSupported(String accordName) {
    return NAMES_BY_KEY.containsKey(key(accordName));
  }

  private static Map<String, String> namesByKey() {
    Map<String, String> names = new LinkedHashMap<>();
    for (String accord : SUPPORTED_ACCORDS) {
      names.put(key(accord), accord);
    }
    names.put(key("Earthy"), "Earthy/Smoky");
    return names;
  }

  private static String key(String value) {
    return value.trim().toLowerCase(Locale.ROOT);
  }
}
