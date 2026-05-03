package kim.biryeong.perfume.perfume;

import kim.biryeong.perfume.review.ReviewListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfumes")
@RequiredArgsConstructor
public class PerfumeController {

    private final PerfumeService perfumeService;

    @GetMapping
    public PerfumeListResponse getPerfumes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String accord,
            @RequestParam(defaultValue = "rating_desc") String sort,
            @RequestParam(defaultValue = "0") int page) {
        return new PerfumeListResponse(perfumeService.getPerfumes(keyword, gender, accord, sort, page));
    }

    @GetMapping("/{id}")
    public PerfumeDetailResponse getPerfumeDetail(@PathVariable Long id) {
        return perfumeService.getPerfumeDetail(id);
    }

    @GetMapping("/{id}/review-summary")
    public StatsDto getReviewSummary(@PathVariable Long id) {
        return perfumeService.getReviewSummary(id);
    }

    @GetMapping("/{id}/reviews")
    public ReviewListResponse getReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page) {
        return perfumeService.getReviews(id, page);
    }
}
