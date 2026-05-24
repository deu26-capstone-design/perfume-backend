package kim.biryeong.perfume.wishlist.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import kim.biryeong.perfume.perfume.domain.Gender;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.repository.PerfumeRepository;
import kim.biryeong.perfume.user.domain.User;
import kim.biryeong.perfume.user.repository.UserRepository;
import kim.biryeong.perfume.wishlist.domain.Wishlist;
import kim.biryeong.perfume.wishlist.dto.WishlistResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
class WishlistRepositoryTest {

  @Autowired private WishlistRepository wishlistRepository;

  @Autowired private PerfumeRepository perfumeRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void findByUserIdSortsByCreatedAtThenPerfumeIdDesc() {
    User user = userRepository.save(user("wishlist-order@example.com"));
    Perfume oldPerfume = perfumeRepository.save(perfume(1L, "Old"));
    Perfume sameTimeLowerId = perfumeRepository.save(perfume(2L, "Same Lower"));
    Perfume sameTimeHigherId = perfumeRepository.save(perfume(3L, "Same Higher"));

    wishlistRepository.save(new Wishlist(oldPerfume, user));
    wishlistRepository.save(new Wishlist(sameTimeLowerId, user));
    wishlistRepository.save(new Wishlist(sameTimeHigherId, user));
    wishlistRepository.flush();

    LocalDateTime oldTime = LocalDateTime.of(2026, 5, 1, 10, 0);
    LocalDateTime sameTime = LocalDateTime.of(2026, 5, 2, 10, 0);
    updateCreatedAt(user.getUserId(), oldPerfume.getId(), oldTime);
    updateCreatedAt(user.getUserId(), sameTimeLowerId.getId(), sameTime);
    updateCreatedAt(user.getUserId(), sameTimeHigherId.getId(), sameTime);

    assertThat(wishlistRepository.findByUserId(user.getUserId()))
        .extracting(WishlistResponse::getPerfumeId)
        .containsExactly(3L, 2L, 1L);
  }

  private void updateCreatedAt(Integer userId, Long perfumeId, LocalDateTime createdAt) {
    jdbcTemplate.update(
        "UPDATE wishlist SET created_at = ? WHERE user_id = ? AND perfume_id = ?",
        createdAt,
        userId,
        perfumeId);
  }

  private static User user(String email) {
    User user = new User();
    user.setEmail(email);
    user.setName("Wishlist User");
    user.setNickname("wishlist");
    user.setGender("U");
    user.setPhoneNumber("01012345678");
    return user;
  }

  private static Perfume perfume(Long id, String name) {
    return new Perfume(
        id, name, "Test Brand", Gender.U, "https://example.com/perfume.jpg", "description");
  }
}
