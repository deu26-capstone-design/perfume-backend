package kim.biryeong.perfume.auth.profileimage;

public final class ValidatedProfileImage {

  private final byte[] bytes;
  private final String contentType;
  private final String extension;

  ValidatedProfileImage(byte[] bytes, String contentType, String extension) {
    this.bytes = bytes.clone();
    this.contentType = contentType;
    this.extension = extension;
  }

  public byte[] bytes() {
    return bytes.clone();
  }

  public String contentType() {
    return contentType;
  }

  public String extension() {
    return extension;
  }
}
