package kim.biryeong.perfume.auth.profileimage;

import java.net.URI;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class R2ProfileImageStorage implements ProfileImageStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger(R2ProfileImageStorage.class);
  private static final String CACHE_CONTROL = "public, max-age=31536000, immutable";

  private final R2ProfileImageProperties properties;
  private final ProfileImageFileValidator validator;
  private final S3Client providedS3Client;
  private volatile S3Client lazyS3Client;

  public R2ProfileImageStorage(R2ProfileImageProperties properties) {
    this(properties, null);
  }

  public R2ProfileImageStorage(R2ProfileImageProperties properties, S3Client s3Client) {
    this.properties = properties;
    this.validator = new ProfileImageFileValidator(properties.getMaxSize());
    this.providedS3Client = s3Client;
  }

  @Override
  public StoredProfileImage upload(Integer userId, MultipartFile image) {
    ValidatedProfileImage validatedImage = validator.validate(image);
    validateConfiguration();
    String key = objectKey(userId, validatedImage.extension());

    PutObjectRequest request =
        PutObjectRequest.builder()
            .bucket(properties.getBucket())
            .key(key)
            .contentType(validatedImage.contentType())
            .cacheControl(CACHE_CONTROL)
            .build();
    try {
      s3Client().putObject(request, RequestBody.fromBytes(validatedImage.bytes()));
    } catch (S3Exception | SdkClientException exception) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "profile image upload failed", exception);
    }
    return new StoredProfileImage(key, publicUrl(key));
  }

  @Override
  public void deleteIfManaged(String publicUrl) {
    String key = managedKey(publicUrl);
    if (key == null) {
      return;
    }
    try {
      s3Client()
          .deleteObject(
              DeleteObjectRequest.builder().bucket(properties.getBucket()).key(key).build());
    } catch (S3Exception | SdkClientException exception) {
      LOGGER.warn("Failed to delete old profile image object {}", key, exception);
    }
  }

  private S3Client s3Client() {
    if (providedS3Client != null) {
      return providedS3Client;
    }
    S3Client initialized = lazyS3Client;
    if (initialized == null) {
      synchronized (this) {
        initialized = lazyS3Client;
        if (initialized == null) {
          initialized = createS3Client();
          lazyS3Client = initialized;
        }
      }
    }
    return initialized;
  }

  private S3Client createS3Client() {
    return S3Client.builder()
        .region(Region.of("auto"))
        .endpointOverride(
            URI.create("https://" + properties.getAccountId().trim() + ".r2.cloudflarestorage.com"))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    properties.getAccessKeyId(), properties.getSecretAccessKey())))
        .serviceConfiguration(
            S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .chunkedEncodingEnabled(false)
                .build())
        .httpClientBuilder(UrlConnectionHttpClient.builder())
        .build();
  }

  private void validateConfiguration() {
    if (!StringUtils.hasText(properties.getAccountId())
        || !StringUtils.hasText(properties.getAccessKeyId())
        || !StringUtils.hasText(properties.getSecretAccessKey())
        || !StringUtils.hasText(properties.getBucket())
        || !StringUtils.hasText(properties.getPublicBaseUrl())) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "profile image storage is not configured");
    }
  }

  private String objectKey(Integer userId, String extension) {
    return normalizedKeyPrefix()
        + "/"
        + userId
        + "/"
        + UUID.randomUUID().toString().toLowerCase(Locale.ROOT)
        + "."
        + extension;
  }

  private String publicUrl(String key) {
    return normalizedPublicBaseUrl() + "/" + key;
  }

  private String managedKey(String publicUrl) {
    if (!StringUtils.hasText(publicUrl)) {
      return null;
    }
    String managedPrefix = normalizedPublicBaseUrl() + "/" + normalizedKeyPrefix() + "/";
    if (!publicUrl.startsWith(managedPrefix)) {
      return null;
    }
    return publicUrl.substring(normalizedPublicBaseUrl().length() + 1);
  }

  private String normalizedPublicBaseUrl() {
    return trimTrailingSlash(properties.getPublicBaseUrl().trim());
  }

  private String normalizedKeyPrefix() {
    String keyPrefix = properties.getKeyPrefix();
    if (!StringUtils.hasText(keyPrefix)) {
      return "profile-images";
    }
    return trimSlashes(keyPrefix.trim());
  }

  private String trimTrailingSlash(String value) {
    String trimmed = value;
    while (trimmed.endsWith("/")) {
      trimmed = trimmed.substring(0, trimmed.length() - 1);
    }
    return trimmed;
  }

  private String trimSlashes(String value) {
    String trimmed = value;
    while (trimmed.startsWith("/")) {
      trimmed = trimmed.substring(1);
    }
    while (trimmed.endsWith("/")) {
      trimmed = trimmed.substring(0, trimmed.length() - 1);
    }
    return trimmed;
  }
}
