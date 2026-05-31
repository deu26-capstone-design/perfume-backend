package kim.biryeong.perfume.user.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kim.biryeong.perfume.domain.OAuthProvider;
import kim.biryeong.perfume.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);

  Optional<User> findByOauthProviderAndOauthProviderId(
      OAuthProvider oauthProvider, String oauthProviderId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT u FROM User u WHERE u.userId = :userId")
  Optional<User> findByIdForUpdate(@Param("userId") Integer userId);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  boolean existsByNicknameAndUserIdNot(String nickname, Integer userId);
}
