package kim.biryeong.perfume.review;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfumes")
@RequiredArgsConstructor
@Validated
public class ReviewController {

  private final ReviewService reviewService;

  @GetMapping("/{id}/reviews")
  public ReviewListResponse getReviews(
      @PathVariable @Min(1) Long id,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size) {
    return reviewService.getReviews(id, page, size);
  }

  /**
   * Temporary service-prep contract: userId is accepted as a request parameter only until
   * authentication is integrated and the authenticated subject can be passed into the service. This
   * is not production authorization.
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
