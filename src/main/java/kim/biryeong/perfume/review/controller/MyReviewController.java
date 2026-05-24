package kim.biryeong.perfume.review.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kim.biryeong.perfume.auth.AuthenticatedUserIds;
import kim.biryeong.perfume.review.dto.MyReviewListResponse;
import kim.biryeong.perfume.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 사용자 본인의 리뷰 목록 조회 API를 제공한다. */
@RestController
@RequestMapping("/api/auth/me")
@RequiredArgsConstructor
@Validated
public class MyReviewController {

  private final ReviewService reviewService;

  /**
   * 현재 로그인한 사용자가 작성한 리뷰 목록을 최신순으로 반환한다.
   *
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @param page 0부터 시작하는 페이지 번호
   * @param size 한 페이지 항목 수. 1부터 100까지 허용된다.
   * @return 내가 작성한 리뷰 목록과 페이징 메타데이터
   */
  @GetMapping("/reviews")
  public MyReviewListResponse getMyReviews(
      Authentication authentication,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size) {
    return reviewService.getMyReviews(
        AuthenticatedUserIds.currentUserId(authentication), page, size);
  }
}
