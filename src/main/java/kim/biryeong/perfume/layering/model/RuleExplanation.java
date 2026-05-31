package kim.biryeong.perfume.layering.model;

public record RuleExplanation(
    LayeringEvidenceCode evidenceCode, String severity, String variantKey, String template) {

  public boolean warning() {
    return "warning".equals(severity);
  }
}
