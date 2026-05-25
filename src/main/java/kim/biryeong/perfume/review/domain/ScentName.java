package kim.biryeong.perfume.review.domain;

public enum ScentName {
  FLORAL("꽃 향", "Floral"),
  WOODY("나무 향", "Woody"),
  FRESH("청량한 향", "Fresh"),
  SPICY("스파이시한 향", "Spicy"),
  SWEET("달콤한 향", "Sweet"),
  DESSERT("디저트 향", "Gourmand"),
  COZY("포근한 향", "Musky"),
  GREEN("풀 향", "Green"),
  CITRUS("상큼한 향", "Citrus"),
  FRUITY("과일 향", "Fruity"),
  HERBAL("허브 향", "Aromatic"),
  EARTHY("흙내음", "Earthy/Smoky");

  private final String value;
  private final String englishName;

  ScentName(String value, String englishName) {
    this.value = value;
    this.englishName = englishName;
  }

  public String getValue() {
    return value;
  }

  public String getEnglishName() {
    return englishName;
  }

  public static ScentName from(String value) {
    for (ScentName scentName : values()) {
      if (scentName.value.equals(value) || scentName.name().equals(value)) {
        return scentName;
      }
    }
    throw new IllegalArgumentException("유효하지 않은 향 값입니다: " + value);
  }
}
