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

  @PostMapping("/{id}/reviews")
  @ResponseStatus(HttpStatus.CREATED)
  public void createReview(
      @PathVariable @Min(1) Long id, @RequestBody @Valid ReviewRequest request) {
    reviewService.createReview(id, request);
  }
}
