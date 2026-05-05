package kim.biryeong.perfume.perfume;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 향수 상세 응답에 포함되는 리뷰 통계 묶음이다. */
@Getter
@AllArgsConstructor
public class StatsDto {
  /** 만족도 분포. 키는 1~5점, 값은 전체 리뷰 대비 비율이다. */
  private Map<Integer, Integer> satisfaction;

  /** 지속력 분포. 키는 1~3점, 값은 지속력 응답 리뷰 대비 비율이다. */
  private Map<Integer, Integer> longevity;

  /** 계절 분포. 키는 봄/여름/가을/겨울, 값은 계절 응답 리뷰 대비 비율이다. */
  private Map<String, Integer> seasons;
}
