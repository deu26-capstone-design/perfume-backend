package kim.biryeong.perfume.accord.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 향 계열 노트 응답. 노트 이름과 이미지 URL을 포함한다. */
@Getter
@AllArgsConstructor
public class AccordNoteDto {
  /** 노트 이름. 예: bergamot, sandalwood 등 */
  private String name;

  /** 노트 대표 이미지 URL */
  private String imageUrl;
}
