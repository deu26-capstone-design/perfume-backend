package kim.biryeong.perfume.auth.profileimage;

import kim.biryeong.perfume.auth.AuthService;
import kim.biryeong.perfume.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileImageService {

  private final ProfileImageStorage profileImageStorage;
  private final AuthService authService;

  public ProfileImageService(ProfileImageStorage profileImageStorage, AuthService authService) {
    this.profileImageStorage = profileImageStorage;
    this.authService = authService;
  }

  public User updateProfileImage(Integer userId, MultipartFile image) {
    authService.getCurrentUser(userId);
    StoredProfileImage storedImage = profileImageStorage.upload(userId, image);
    AuthService.ProfileImageReplacement replacement;
    try {
      replacement = authService.replaceProfileImage(userId, storedImage.publicUrl());
    } catch (RuntimeException exception) {
      profileImageStorage.deleteIfManaged(storedImage.publicUrl());
      throw exception;
    }
    profileImageStorage.deleteIfManaged(replacement.previousProfileImageUrl());
    return replacement.user();
  }
}
