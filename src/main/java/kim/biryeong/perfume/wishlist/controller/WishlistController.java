package kim.biryeong.perfume.wishlist.controller;

import jakarta.validation.constraints.Min;
import java.util.List;
import kim.biryeong.perfume.auth.AuthenticatedUserIds;
import kim.biryeong.perfume.wishlist.dto.WishlistResponse;
import kim.biryeong.perfume.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 사용자의 위시리스트 추가, 삭제, 조회 API를 제공한다. */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Validated
public class WishlistController {

  private final WishlistService wishlistService;

  /**
   * 향수를 위시리스트에 추가한다.
   *
   * <p>위시리스트 소유자는 JWT subject에 담긴 현재 인증 사용자 ID로 결정한다.
   *
   * @param perfumeId 추가할 향수 ID. 1 이상이어야 한다.
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   */
  @PostMapping("/{perfumeId}")
  @ResponseStatus(HttpStatus.CREATED)
  public void addWishlist(@PathVariable @Min(1) Long perfumeId, Authentication authentication) {
    wishlistService.addWishlist(perfumeId, AuthenticatedUserIds.currentUserId(authentication));
  }

  /**
   * 향수를 위시리스트에서 제거한다.
   *
   * <p>위시리스트 소유자는 JWT subject에 담긴 현재 인증 사용자 ID로 결정한다.
   *
   * @param perfumeId 제거할 향수 ID. 1 이상이어야 한다.
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   */
  @DeleteMapping("/{perfumeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeWishlist(@PathVariable @Min(1) Long perfumeId, Authentication authentication) {
    wishlistService.removeWishlist(perfumeId, AuthenticatedUserIds.currentUserId(authentication));
  }

  /**
   * 사용자의 위시리스트 향수 목록을 조회한다.
   *
   * <p>위시리스트 소유자는 JWT subject에 담긴 현재 인증 사용자 ID로 결정한다.
   *
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @return 위시리스트에 등록된 향수 카드 목록
   */
  @GetMapping
  public List<WishlistResponse> getWishlist(Authentication authentication) {
    return wishlistService.getWishlist(AuthenticatedUserIds.currentUserId(authentication));
  }
}
