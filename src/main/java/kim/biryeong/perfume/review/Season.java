package kim.biryeong.perfume.review;

public enum Season {
  SPRING("봄"),
  SUMMER("여름"),
  AUTUMN("가을"),
  WINTER("겨울");

  private final String value;

  Season(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static Season from(String value) {
    for (Season season : values()) {
      if (season.value.equals(value) || season.name().equals(value)) {
        return season;
      }
    }
    throw new IllegalArgumentException("유효하지 않은 계절 값입니다: " + value);
  }
}
