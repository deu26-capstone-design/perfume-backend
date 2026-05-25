package kim.biryeong.perfume.layering.model;

public record RuleExplanation(LayeringEvidenceCode evidenceCode, String severity, String template) {

  public boolean warning() {
    return "warning".equals(severity);
  }
}
