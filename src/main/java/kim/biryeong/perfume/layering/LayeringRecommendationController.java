package kim.biryeong.perfume.layering;

import jakarta.validation.Valid;
import kim.biryeong.perfume.layering.dto.LayeringRecommendationRequest;
import kim.biryeong.perfume.layering.dto.LayeringRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 향수 2개 기반의 deterministic 레이어링 추천 API를 제공한다. */
@RestController
@RequestMapping("/api/layering/recommendations")
@RequiredArgsConstructor
@Validated
public class LayeringRecommendationController {

  private final LayeringRecommendationService layeringRecommendationService;

  /**
   * 향수 2개의 어코드와 노트 데이터를 내부 규칙으로 평가해 레이어링 추천 결과를 반환한다.
   *
   * @param request 추천에 사용할 서로 다른 향수 ID 2개
   * @return 추천 여부, 점수, 설명, 주의점, 무드 컬러를 포함한 평가 결과
   */
  @PostMapping
  public LayeringRecommendationResponse recommend(
      @Valid @RequestBody LayeringRecommendationRequest request) {
    return layeringRecommendationService.recommend(request);
  }
}
