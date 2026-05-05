package kim.biryeong.perfume.perfume;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 향수 목록 검색과 향수 상세 조회 API를 제공한다. */
@RestController
@RequestMapping("/api/perfumes")
@RequiredArgsConstructor
@Validated
public class PerfumeController {

  private final PerfumeService perfumeService;

  /**
   * 향수 목록을 필터링, 정렬, 페이징하여 조회한다.
   *
   * @param keyword 향수명 또는 브랜드명에 부분 일치로 적용되는 검색어
   * @param gender 성별 필터. W, M, U 중 하나만 허용된다.
   * @param accord 어코드 이름 필터. 지정하면 해당 어코드를 가진 향수만 조회한다.
   * @param sort 정렬 기준. rating_desc 또는 rating_asc
   * @param page 0부터 시작하는 페이지 번호
   * @param size 한 페이지 항목 수. 1부터 100까지 허용된다.
   * @return 목록 카드와 페이징 메타데이터를 포함한 향수 목록 응답
   */
  @GetMapping
  public PerfumeListResponse getPerfumes(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false)
          @Pattern(regexp = "^[WMU]$", message = "gender는 W, M, U 중 하나여야 합니다.")
          String gender,
      @RequestParam(required = false) String accord,
      @RequestParam(defaultValue = "rating_desc")
          @Pattern(
              regexp = "^(rating_asc|rating_desc)$",
              message = "sort는 rating_asc, rating_desc 중 하나여야 합니다.")
          String sort,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size) {
    return new PerfumeListResponse(
        perfumeService.getPerfumes(keyword, gender, accord, sort, page, size));
  }

  /**
   * 향수 상세 정보를 조회한다.
   *
   * @param id 조회할 향수 ID. 1 이상이어야 한다.
   * @return 향수 기본 정보, 노트, 어코드, 리뷰 통계를 포함한 상세 응답
   */
  @GetMapping("/{id}")
  public PerfumeDetailResponse getPerfumeDetail(@PathVariable @Min(1) Long id) {
    return perfumeService.getPerfumeDetail(id);
  }
}
