package kim.biryeong.perfume.review.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 리뷰 작성 성공 시 응답. 업데이트된 향수 통계를 포함한다. */
@Getter
@AllArgsConstructor
public class ReviewCreateResponse {
  /** 평균 만족도 (1.0 ~ 5.0) */
  private double rating;

  /** 총 리뷰 수 */
  private long totalReviewCount;

  /** 만족도 분포. 키는 1~5점, 값은 전체 리뷰 대비 비율이다. */
  private Map<Integer, Integer> satisfaction;

  /** 지속력 분포. 키는 1~3점, 값은 지속력 응답 리뷰 대비 비율이다. */
  private Map<Integer, Integer> longevity;

  /** 계절 분포. 키는 봄/여름/가을/겨울, 값은 계절 응답 리뷰 대비 비율이다. */
  private Map<String, Integer> seasons;
}
