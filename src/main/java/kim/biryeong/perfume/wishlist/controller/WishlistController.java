package kim.biryeong.perfume.wishlist.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import kim.biryeong.perfume.audit.AuditEventType;
import kim.biryeong.perfume.audit.AuditLogRequestAttributes;
import kim.biryeong.perfume.auth.AuthenticatedUserIds;
import kim.biryeong.perfume.wishlist.dto.WishlistListResponse;
import kim.biryeong.perfume.wishlist.dto.WishlistResponse;
import kim.biryeong.perfume.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

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
  public void addWishlist(
      @PathVariable @Min(1) Long perfumeId,
      HttpServletRequest servletRequest,
      Authentication authentication) {
    Integer userId = AuthenticatedUserIds.currentUserId(authentication);
    AuditLogRequestAttributes.mark(servletRequest, AuditEventType.WISHLIST_ADD, userId);
    wishlistService.addWishlist(perfumeId, userId);
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
  public void removeWishlist(
      @PathVariable @Min(1) Long perfumeId,
      HttpServletRequest servletRequest,
      Authentication authentication) {
    Integer userId = AuthenticatedUserIds.currentUserId(authentication);
    AuditLogRequestAttributes.mark(servletRequest, AuditEventType.WISHLIST_REMOVE, userId);
    wishlistService.removeWishlist(perfumeId, userId);
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

  /**
   * 사용자의 위시리스트 향수 목록을 페이징하여 조회한다.
   *
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @param page 0부터 시작하는 페이지 번호
   * @param size 한 페이지 항목 수. 1부터 100까지 허용된다.
   * @return 위시리스트 향수 카드 목록과 페이징 메타데이터
   */
  @GetMapping("/page")
  public WishlistListResponse getWishlistPage(
      Authentication authentication,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size) {
    return wishlistService.getWishlistPage(
        AuthenticatedUserIds.currentUserId(authentication), page, size);
  }
}
