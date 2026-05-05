package kim.biryeong.perfume.perfume.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 향수 상세 조회 응답. 기본 정보, 노트, 어코드, 리뷰 기반 통계를 함께 제공한다. */
@Getter
@AllArgsConstructor
public class PerfumeDetailResponse {
  /** 향수 ID */
  private Long id;

  /** 향수 이미지 URL */
  private String imageUrl;

  /** 브랜드명 */
  private String brand;

  /** 향수명 */
  private String name;

  /** 성별 (W: 여성, M: 남성, U: 유니섹스) */
  private String gender;

  /** 향수 설명 */
  private String description;

  /** 평균 만족도 (1.0 ~ 5.0, 리뷰 없으면 0.0) */
  private double rating;

  /** 리뷰 수 */
  private long reviewCount;

  /** 탑/미들/베이스로 그룹화된 노트 목록 */
  private NoteGroupDto notes;

  /** 향수의 주요 어코드 목록과 각 어코드의 비율 */
  private List<AccordDto> accords;

  /** 만족도 통계. 키는 1~5점, 값은 전체 리뷰 대비 비율이다. */
  private Map<Integer, Integer> satisfaction;

  /** 지속력 통계. 키는 1~3점, 값은 지속력 응답 리뷰 대비 비율이다. */
  private Map<Integer, Integer> longevity;

  /** 계절 통계. 키는 봄/여름/가을/겨울, 값은 계절 응답 리뷰 대비 비율이다. */
  private Map<String, Integer> seasons;
}
