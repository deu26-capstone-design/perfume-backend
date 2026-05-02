package kim.biryeong.perfume.controller;

import kim.biryeong.perfume.dto.PerfumeDetailResponse;
import kim.biryeong.perfume.dto.PerfumeListResponse;
import kim.biryeong.perfume.dto.ReviewListResponse;
import kim.biryeong.perfume.service.PerfumeService;
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

    @GetMapping("/{id}/reviews")
    public ReviewListResponse getReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page) {
        return perfumeService.getReviews(id, page);
    }
}
