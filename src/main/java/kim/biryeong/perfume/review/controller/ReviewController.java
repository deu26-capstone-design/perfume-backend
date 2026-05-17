package kim.biryeong.perfume.review.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kim.biryeong.perfume.auth.AuthenticatedUserIds;
import kim.biryeong.perfume.review.dto.ReviewCreateResponse;
import kim.biryeong.perfume.review.dto.ReviewListResponse;
import kim.biryeong.perfume.review.dto.ReviewRequest;
import kim.biryeong.perfume.review.dto.ReviewUpdateRequest;
import kim.biryeong.perfume.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 향수 리뷰 목록 조회, 리뷰 작성, 수정, 삭제 API를 제공한다. */
@RestController
@RequestMapping("/api/perfumes")
@RequiredArgsConstructor
@Validated
public class ReviewController {

  private final ReviewService reviewService;

  /**
   * 특정 향수의 리뷰 목록을 최신순으로 조회한다.
   *
   * @param id 리뷰를 조회할 향수 ID. 1 이상이어야 한다.
   * @param page 0부터 시작하는 페이지 번호
   * @param size 한 페이지 항목 수. 1부터 100까지 허용된다.
   * @return 리뷰 카드 목록과 페이징 메타데이터를 포함한 응답
   */
  @GetMapping("/{id}/reviews")
  public ReviewListResponse getReviews(
      @PathVariable @Min(1) Long id,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size) {
    return reviewService.getReviews(id, page, size);
  }

  /**
   * 특정 향수에 리뷰를 작성한다.
   *
   * <p>리뷰 작성자는 JWT subject에 담긴 현재 인증 사용자 ID로 결정한다.
   *
   * @param id 리뷰를 작성할 향수 ID. 1 이상이어야 한다.
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @param request 만족도, 지속력, 계절, 향 느낌, 코멘트, 면책 동의 여부를 담은 요청 본문
   */
  @PostMapping("/{id}/reviews")
  @ResponseStatus(HttpStatus.CREATED)
  public ReviewCreateResponse createReview(
      @PathVariable @Min(1) Long id,
      Authentication authentication,
      @RequestBody @Valid ReviewRequest request) {
    return reviewService.createReview(
        id, AuthenticatedUserIds.currentUserId(authentication), request);
  }

  /**
   * 리뷰를 수정한다. 본인이 작성한 리뷰만 수정할 수 있다.
   *
   * @param id 수정할 리뷰 ID. 1 이상이어야 한다.
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @param request 만족도, 지속력, 계절, 향 느낌, 코멘트, 면책 동의 여부를 담은 수정 요청 본문
   */
  @PatchMapping("/reviews/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateReview(
      @PathVariable @Min(1) Long id,
      Authentication authentication,
      @RequestBody @Valid ReviewUpdateRequest request) {
    reviewService.updateReview(id, AuthenticatedUserIds.currentUserId(authentication), request);
  }

  /**
   * 리뷰를 삭제한다. 본인이 작성한 리뷰만 삭제할 수 있다.
   *
   * @param id 삭제할 리뷰 ID. 1 이상이어야 한다.
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   */
  @DeleteMapping("/reviews/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteReview(@PathVariable @Min(1) Long id, Authentication authentication) {
    reviewService.deleteReview(id, AuthenticatedUserIds.currentUserId(authentication));
  }
}
