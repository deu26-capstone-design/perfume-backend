package kim.biryeong.perfume.accord.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 향 계열 상세 조회 응답. 향 계열 기본 정보를 포함한다. */
@Getter
@AllArgsConstructor
public class AccordDetailResponse {
  /** 향 계열 ID */
  private Long id;

  /** 향 계열 이름. 예: citrus, woody, floral 등 */
  private String name;

  /** 향 계열 설명 */
  private String description;

  /** 향 계열 대표 이미지 URL */
  private String imageUrl;
}
