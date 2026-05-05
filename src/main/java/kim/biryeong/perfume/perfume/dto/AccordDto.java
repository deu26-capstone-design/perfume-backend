package kim.biryeong.perfume.perfume.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 향수 상세 응답에서 특정 어코드의 이름과 구성 비율을 표현한다. */
@Getter
@AllArgsConstructor
public class AccordDto {
  /** 어코드 이름. 예: citrus, woody, floral 등 데이터셋에 등록된 값 */
  private String accordName;

  /** 해당 향수에서 어코드가 차지하는 비율. 정수 퍼센트 값으로 내려간다. */
  private int ratio;
}
