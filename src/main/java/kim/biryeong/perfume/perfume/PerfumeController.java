package kim.biryeong.perfume.perfume;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import kim.biryeong.perfume.review.ReviewListResponse;
import kim.biryeong.perfume.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfumes")
@RequiredArgsConstructor
@Validated
public class PerfumeController {

    private final PerfumeService perfumeService;
    private final ReviewService reviewService;

    @GetMapping
    public PerfumeListResponse getPerfumes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Pattern(regexp = "^(W|M|U)$", message = "gender는 W, M, U 중 하나여야 합니다.") String gender,
            @RequestParam(required = false) String accord,
            @RequestParam(defaultValue = "rating_desc") @Pattern(regexp = "^(rating_asc|rating_desc)$", message = "sort는 rating_asc, rating_desc 중 하나여야 합니다.") String sort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size) {
        return new PerfumeListResponse(perfumeService.getPerfumes(keyword, gender, accord, sort, page, size));
    }

    @GetMapping("/{id}")
    public PerfumeDetailResponse getPerfumeDetail(@PathVariable @Min(1) Long id) {
        return perfumeService.getPerfumeDetail(id);
    }

    @GetMapping("/{id}/review-summary")
    public StatsDto getReviewSummary(@PathVariable @Min(1) Long id) {
        return reviewService.getReviewSummary(id);
    }

    @GetMapping("/{id}/reviews")
    public ReviewListResponse getReviews(
            @PathVariable @Min(1) Long id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size) {
        return reviewService.getReviews(id, page, size);
    }
}
