package kim.biryeong.perfume.auth.profileimage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

class R2ProfileImageStorageTest {

  private S3Client s3Client;
  private R2ProfileImageStorage storage;

  @BeforeEach
  void setUp() {
    s3Client = mock(S3Client.class);
    storage = new R2ProfileImageStorage(properties(), s3Client);
  }

  @Test
  void uploadStoresProfileImageWithExpectedR2Metadata() {
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenReturn(PutObjectResponse.builder().build());

    StoredProfileImage storedImage = storage.upload(7, jpegProfileImage());

    ArgumentCaptor<PutObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

    PutObjectRequest request = requestCaptor.getValue();
    assertThat(request.bucket()).isEqualTo("test-bucket");
    assertThat(request.key()).startsWith("profile-images/7/").endsWith(".jpg");
    assertThat(request.contentType()).isEqualTo("image/jpeg");
    assertThat(request.cacheControl()).isEqualTo("public, max-age=31536000, immutable");
    assertThat(storedImage.key()).isEqualTo(request.key());
    assertThat(storedImage.publicUrl()).isEqualTo("https://cdn.example.com/" + request.key());
  }

  @Test
  void uploadMapsR2FailureToServiceUnavailable() {
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenThrow(S3Exception.builder().message("R2 unavailable").statusCode(500).build());

    assertThatThrownBy(() -> storage.upload(7, jpegProfileImage()))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode())
                    .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
  }

  @Test
  void deleteIfManagedDeletesObjectForConfiguredPublicUrl() {
    storage.deleteIfManaged("https://cdn.example.com/profile-images/7/current.jpg");

    ArgumentCaptor<DeleteObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(s3Client).deleteObject(requestCaptor.capture());
    assertThat(requestCaptor.getValue().bucket()).isEqualTo("test-bucket");
    assertThat(requestCaptor.getValue().key()).isEqualTo("profile-images/7/current.jpg");
  }

  @Test
  void deleteIfManagedIgnoresUnmanagedPublicUrl() {
    storage.deleteIfManaged("https://other.example.com/profile-images/7/current.jpg");

    verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
  }

  private R2ProfileImageProperties properties() {
    R2ProfileImageProperties properties = new R2ProfileImageProperties();
    properties.setAccountId("test-account");
    properties.setAccessKeyId("test-access-key");
    properties.setSecretAccessKey("test-secret-key");
    properties.setBucket("test-bucket");
    properties.setPublicBaseUrl("https://cdn.example.com/");
    properties.setKeyPrefix("/profile-images/");
    properties.setMaxSize(DataSize.ofMegabytes(5));
    return properties;
  }

  private MockMultipartFile jpegProfileImage() {
    return new MockMultipartFile(
        "image",
        "profile.jpg",
        "image/jpeg",
        new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});
  }
}
