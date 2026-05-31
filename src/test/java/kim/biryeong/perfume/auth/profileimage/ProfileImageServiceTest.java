package kim.biryeong.perfume.auth.profileimage;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kim.biryeong.perfume.auth.AuthService;
import kim.biryeong.perfume.user.domain.User;
import kim.biryeong.perfume.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

class ProfileImageServiceTest {

  private static final String NEW_PROFILE_IMAGE_URL =
      "https://cdn.example.com/profile-images/1/new.jpg";

  private ProfileImageStorage profileImageStorage;
  private UserRepository userRepository;
  private ProfileImageService profileImageService;

  @BeforeEach
  void setUp() {
    profileImageStorage = mock(ProfileImageStorage.class);
    userRepository = mock(UserRepository.class);
    AuthService authService = new AuthService(userRepository, mock(PasswordEncoder.class));
    profileImageService = new ProfileImageService(profileImageStorage, authService);
  }

  @Test
  void updateProfileImageChecksUserBeforeUploading() {
    when(userRepository.findById(1)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> profileImageService.updateProfileImage(1, jpegProfileImage()))
        .isInstanceOf(RuntimeException.class);

    verify(profileImageStorage, never()).upload(any(), any());
  }

  @Test
  void updateProfileImageDeletesPreviousManagedImageAfterReplacement() {
    MockMultipartFile image = jpegProfileImage();
    User user = user("https://cdn.example.com/profile-images/1/old.jpg");
    when(userRepository.findById(1)).thenReturn(Optional.of(user));
    when(profileImageStorage.upload(1, image))
        .thenReturn(new StoredProfileImage("profile-images/1/new.jpg", NEW_PROFILE_IMAGE_URL));
    when(userRepository.findByIdForUpdate(1)).thenReturn(Optional.of(user));

    profileImageService.updateProfileImage(1, image);

    verify(profileImageStorage).deleteIfManaged("https://cdn.example.com/profile-images/1/old.jpg");
  }

  @Test
  void updateProfileImageDeletesNewUploadWhenDatabaseReplacementFails() {
    MockMultipartFile image = jpegProfileImage();
    User user = user(null);
    when(userRepository.findById(1)).thenReturn(Optional.of(user));
    when(profileImageStorage.upload(1, image))
        .thenReturn(new StoredProfileImage("profile-images/1/new.jpg", NEW_PROFILE_IMAGE_URL));
    when(userRepository.findByIdForUpdate(1)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> profileImageService.updateProfileImage(1, image))
        .isInstanceOf(RuntimeException.class);

    verify(profileImageStorage).deleteIfManaged(NEW_PROFILE_IMAGE_URL);
  }

  private User user(String profileImageUrl) {
    User user = new User();
    user.setUserId(1);
    user.setProfileImageUrl(profileImageUrl);
    return user;
  }

  private MockMultipartFile jpegProfileImage() {
    return new MockMultipartFile(
        "image",
        "profile.jpg",
        "image/jpeg",
        new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});
  }
}
