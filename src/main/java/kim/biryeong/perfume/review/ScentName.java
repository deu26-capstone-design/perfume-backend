package kim.biryeong.perfume.review;

public enum ScentName {
  FLORAL("꽃 향"),
  WOODY("나무 향"),
  FRESH("청량한 향"),
  SPICY("스파이시한 향"),
  SWEET("달콤한 향"),
  DESSERT("디저트 향"),
  COZY("포근한 향"),
  GREEN("풀 향"),
  CITRUS("상큼한 향"),
  FRUITY("과일 향"),
  HERBAL("허브 향"),
  EARTHY("흙내음");

  private final String value;

  ScentName(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
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
