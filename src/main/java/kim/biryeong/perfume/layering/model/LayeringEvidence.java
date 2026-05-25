package kim.biryeong.perfume.layering.model;

public record LayeringEvidence(
    LayeringEvidenceCode code,
    String accordA,
    String accordB,
    String perfumeName,
    String accord,
    String colorName) {

  public static LayeringEvidence accordPair(
      LayeringEvidenceCode code, String accordA, String accordB) {
    return new LayeringEvidence(code, accordA, accordB, null, null, null);
  }

  public static LayeringEvidence perfumeAccord(
      LayeringEvidenceCode code, String perfumeName, String accord) {
    return new LayeringEvidence(code, null, null, perfumeName, accord, null);
  }

  public static LayeringEvidence color(String accordA, String accordB, String colorName) {
    return new LayeringEvidence(
        LayeringEvidenceCode.COLOR_PAIR_SELECTED, accordA, accordB, null, null, colorName);
  }

  public static LayeringEvidence simple(LayeringEvidenceCode code) {
    return new LayeringEvidence(code, null, null, null, null, null);
  }
}
