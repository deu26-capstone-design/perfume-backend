package kim.biryeong.perfume.auth.profileimage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app.r2")
public class R2ProfileImageProperties {

  private String accountId = "";

  private String accessKeyId = "";

  private String secretAccessKey = "";

  private String bucket = "";

  private String publicBaseUrl = "";

  private String keyPrefix = "profile-images";

  private DataSize maxSize = DataSize.ofMegabytes(5);

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getAccessKeyId() {
    return accessKeyId;
  }

  public void setAccessKeyId(String accessKeyId) {
    this.accessKeyId = accessKeyId;
  }

  public String getSecretAccessKey() {
    return secretAccessKey;
  }

  public void setSecretAccessKey(String secretAccessKey) {
    this.secretAccessKey = secretAccessKey;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getPublicBaseUrl() {
    return publicBaseUrl;
  }

  public void setPublicBaseUrl(String publicBaseUrl) {
    this.publicBaseUrl = publicBaseUrl;
  }

  public String getKeyPrefix() {
    return keyPrefix;
  }

  public void setKeyPrefix(String keyPrefix) {
    this.keyPrefix = keyPrefix;
  }

  public DataSize getMaxSize() {
    return maxSize;
  }

  public void setMaxSize(DataSize maxSize) {
    this.maxSize = maxSize;
  }
}
