package kim.biryeong.perfume.user.repository;

import java.util.Optional;
import kim.biryeong.perfume.domain.OAuthProvider;
import kim.biryeong.perfume.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);

  Optional<User> findByOauthProviderAndOauthProviderId(
      OAuthProvider oauthProvider, String oauthProviderId);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  boolean existsByNicknameAndUserIdNot(String nickname, Integer userId);
}
