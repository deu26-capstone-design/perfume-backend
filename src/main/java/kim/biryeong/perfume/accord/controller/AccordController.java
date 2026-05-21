package kim.biryeong.perfume.accord.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import kim.biryeong.perfume.accord.dto.AccordDetailResponse;
import kim.biryeong.perfume.accord.dto.AccordNotePageResponse;
import kim.biryeong.perfume.accord.service.AccordService;
import kim.biryeong.perfume.auth.AuthenticatedUserIds;
import kim.biryeong.perfume.perfume.dto.PerfumeListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 향 계열 목록 및 상세 API를 제공한다. */
@RestController
@RequestMapping("/api/accords")
@RequiredArgsConstructor
@Validated
public class AccordController {

  private final AccordService accordService;

  /**
   * 등록된 향수 어코드 이름을 중복 없이 조회한다.
   *
   * @return 어코드 필터에 사용할 수 있는 어코드 이름 목록
   */
  @GetMapping
  public List<String> getAccords() {
    return accordService.getAccordNames();
  }

  /**
   * 향 계열 전체 목록을 기본 정보와 함께 조회한다.
   *
   * @return 향 계열 id, 이름, 설명, 이미지 목록
   */
  @GetMapping("/detail")
  public List<AccordDetailResponse> getAccordDetails() {
    return accordService.getAccordDetails();
  }

  /**
   * 향 계열에 속한 노트 목록을 페이징하여 조회한다.
   *
   * @param id 향 계열 id
   * @param page 페이지 번호 (0부터 시작)
   * @param size 페이지당 항목 수 (1~100)
   * @return 노트 이름, 이미지 목록
   */
  @GetMapping("/detail/{id}/notes")
  public AccordNotePageResponse getAccordNotes(
      @PathVariable @Min(1) Long id,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size) {
    return accordService.getAccordNotes(id, page, size);
  }

  /**
   * 향 계열에 속한 향수 목록을 페이징하여 조회한다. 해당 계열 비율 내림차순, 동점 시 향수명 오름차순으로 정렬한다.
   *
   * @param id 향 계열 id
   * @param page 페이지 번호 (0부터 시작)
   * @param size 페이지당 항목 수 (1~100)
   * @return 향수 목록
   */
  @GetMapping("/detail/{id}/perfumes")
  public PerfumeListResponse getAccordPerfumes(
      @PathVariable @Min(1) Long id,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size,
      Authentication authentication) {
    return accordService.getAccordPerfumes(
        id, page, size, AuthenticatedUserIds.currentUserIdOrNull(authentication));
  }
}
