package kim.biryeong.perfume.review.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kim.biryeong.perfume.review.dto.ReviewListResponse;
import kim.biryeong.perfume.review.dto.ReviewRequest;
import kim.biryeong.perfume.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 향수 리뷰 목록 조회와 리뷰 작성 API를 제공한다. */
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
   * <p>현재는 인증 통합 전 준비 단계로 {@code userId}를 query parameter로 받는다. 이 값은 운영 수준의 authorization이 아니며, 인증
   * 주체가 연결되면 제거될 임시 계약이다.
   *
   * @param id 리뷰를 작성할 향수 ID. 1 이상이어야 한다.
   * @param userId 리뷰 작성자 ID. 인증 통합 전까지 사용하는 임시 입력값이다.
   * @param request 만족도, 지속력, 계절, 향 느낌, 코멘트, 면책 동의 여부를 담은 요청 본문
   */
  @PostMapping("/{id}/reviews")
  @ResponseStatus(HttpStatus.CREATED)
  public void createReview(
      @PathVariable @Min(1) Long id,
      @RequestParam @Min(1) Integer userId,
      @RequestBody @Valid ReviewRequest request) {
    reviewService.createReview(id, userId, request);
  }
}
