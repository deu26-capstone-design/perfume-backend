package kim.biryeong.perfume.wishlist.service;

import java.util.List;
import java.util.Set;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.repository.PerfumeRepository;
import kim.biryeong.perfume.user.domain.User;
import kim.biryeong.perfume.user.repository.UserRepository;
import kim.biryeong.perfume.wishlist.domain.Wishlist;
import kim.biryeong.perfume.wishlist.domain.WishlistId;
import kim.biryeong.perfume.wishlist.dto.WishlistListResponse;
import kim.biryeong.perfume.wishlist.dto.WishlistResponse;
import kim.biryeong.perfume.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class WishlistService {

  private final WishlistRepository wishlistRepository;
  private final PerfumeRepository perfumeRepository;
  private final UserRepository userRepository;

  @Transactional
  public void addWishlist(Long perfumeId, Integer userId) {
    Perfume perfume = getPerfume(perfumeId);
    User user = getUser(userId);

    WishlistId id = new WishlistId(perfumeId, userId);
    if (wishlistRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 위시리스트에 추가된 향수입니다.");
    }
    wishlistRepository.save(new Wishlist(perfume, user));
  }

  @Transactional
  public void removeWishlist(Long perfumeId, Integer userId) {
    getPerfume(perfumeId);
    getUser(userId);

    WishlistId id = new WishlistId(perfumeId, userId);
    if (!wishlistRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "위시리스트에 없는 향수입니다.");
    }
    wishlistRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public List<WishlistResponse> getWishlist(Integer userId) {
    getUser(userId);
    return wishlistRepository.findByUserId(userId);
  }

  @Transactional(readOnly = true)
  public Set<Long> findWishlistedPerfumeIds(Integer userId, List<Long> perfumeIds) {
    if (userId == null || perfumeIds.isEmpty()) {
      return Set.of();
    }
    return Set.copyOf(wishlistRepository.findWishlistedPerfumeIds(userId, perfumeIds));
  }

  @Transactional(readOnly = true)
  public WishlistListResponse getWishlistPage(Integer userId, int page, int size) {
    getUser(userId);
    return new WishlistListResponse(
        wishlistRepository.findPageByUserId(userId, PageRequest.of(page, size)));
  }

  private Perfume getPerfume(Long perfumeId) {
    return perfumeRepository
        .findById(perfumeId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 향수 ID입니다."));
  }

  private User getUser(Integer userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저 ID입니다."));
  }
}
