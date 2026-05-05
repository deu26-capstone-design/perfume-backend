package kim.biryeong.perfume.perfume;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 향수 상세 조회 기본 정보, 노트(탑/미들/베이스), 어코드 목록 및 비율, 리뷰 통계를 포함 */
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

  /** 탑/미들/베이스 노트 */
  private NoteGroupDto notes;

  /** 어코드 목록 (이름 + 비율) */
  private List<AccordDto> accords;

  /** 만족도 통계 (1~5, 비율) */
  private Map<Integer, Integer> satisfaction;

  /** 지속력 통계 (1~3, 비율) */
  private Map<Integer, Integer> longevity;

  /** 계절 통계 (봄/여름/가을/겨울, 비율) */
  private Map<String, Integer> seasons;
}
