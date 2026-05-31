package kim.biryeong.perfume.auth.profileimage;

import org.springframework.web.multipart.MultipartFile;

/** Stores validated profile images and removes old managed objects when profiles are replaced. */
public interface ProfileImageStorage {

  StoredProfileImage upload(Integer userId, MultipartFile image);

  void deleteIfManaged(String publicUrl);
}
