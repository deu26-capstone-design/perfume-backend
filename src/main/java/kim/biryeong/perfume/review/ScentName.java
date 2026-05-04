package kim.biryeong.perfume.review;

public enum ScentName {
    꽃향("꽃 향"),
    나무향("나무 향"),
    청량한향("청량한 향"),
    스파이시한향("스파이시한 향"),
    달콤한향("달콤한 향"),
    디저트향("디저트 향"),
    포근한향("포근한 향"),
    풀향("풀 향"),
    상큼한향("상큼한 향"),
    과일향("과일 향"),
    허브향("허브 향"),
    흙내음("흙내음");

    private final String value;

    ScentName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ScentName from(String value) {
        for (ScentName s : values()) {
            if (s.value.equals(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 향 값입니다: " + value);
    }
}
