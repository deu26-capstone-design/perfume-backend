package kim.biryeong.perfume.review;

public enum Season {
    봄, 여름, 가을, 겨울;

    public static Season from(String value) {
        for (Season s : values()) {
            if (s.name().equals(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 계절 값입니다: " + value);
    }
}
