package kim.biryeong.perfume.layering.model;

public enum LayeringDecision {
  RECOMMENDED,
  TRY_IF_YOU_LIKE_THIS_MOOD,
  NOT_RECOMMENDED;

  public static LayeringDecision fromScore(int score) {
    if (score >= 75) {
      return RECOMMENDED;
    }
    if (score >= 60) {
      return TRY_IF_YOU_LIKE_THIS_MOOD;
    }
    return NOT_RECOMMENDED;
  }
}
