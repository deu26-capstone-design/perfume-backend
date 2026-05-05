package kim.biryeong.perfume.wishlist.controller;

import jakarta.validation.constraints.Min;
import java.util.List;
import kim.biryeong.perfume.wishlist.dto.WishlistResponse;
import kim.biryeong.perfume.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
   * <p>현재는 인증 통합 전 준비 단계로 {@code userId}를 query parameter로 받는다. 이 값은 운영 수준의 authorization이 아니며, 인증
   * 주체가 연결되면 제거될 임시 계약이다.
   *
   * @param perfumeId 추가할 향수 ID. 1 이상이어야 한다.
   * @param userId 위시리스트 소유자 ID. 인증 통합 전까지 사용하는 임시 입력값이다.
   */
  @PostMapping("/{perfumeId}")
  @ResponseStatus(HttpStatus.CREATED)
  public void addWishlist(
      @PathVariable @Min(1) Long perfumeId, @RequestParam @Min(1) Integer userId) {
    wishlistService.addWishlist(perfumeId, userId);
  }

  /**
   * 향수를 위시리스트에서 제거한다.
   *
   * <p>현재는 인증 통합 전 준비 단계로 {@code userId}를 query parameter로 받는다. 이 값은 운영 수준의 authorization이 아니며, 인증
   * 주체가 연결되면 제거될 임시 계약이다.
   *
   * @param perfumeId 제거할 향수 ID. 1 이상이어야 한다.
   * @param userId 위시리스트 소유자 ID. 인증 통합 전까지 사용하는 임시 입력값이다.
   */
  @DeleteMapping("/{perfumeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeWishlist(
      @PathVariable @Min(1) Long perfumeId, @RequestParam @Min(1) Integer userId) {
    wishlistService.removeWishlist(perfumeId, userId);
  }

  /**
   * 사용자의 위시리스트 향수 목록을 조회한다.
   *
   * <p>현재는 인증 통합 전 준비 단계로 {@code userId}를 query parameter로 받는다. 이 값은 운영 수준의 authorization이 아니며, 인증
   * 주체가 연결되면 제거될 임시 계약이다.
   *
   * @param userId 조회할 위시리스트 소유자 ID. 인증 통합 전까지 사용하는 임시 입력값이다.
   * @return 위시리스트에 등록된 향수 카드 목록
   */
  @GetMapping
  public List<WishlistResponse> getWishlist(@RequestParam @Min(1) Integer userId) {
    return wishlistService.getWishlist(userId);
  }
}
