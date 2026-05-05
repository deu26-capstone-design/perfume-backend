package kim.biryeong.perfume.perfume;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 향수 상세 응답에서 노트를 탑/미들/베이스 단계별로 묶어 표현한다. */
@Getter
@AllArgsConstructor
public class NoteGroupDto {
  /** 처음 인지되는 탑 노트 이름 목록 */
  private List<String> top;

  /** 중간 단계에서 주로 인지되는 미들 노트 이름 목록 */
  private List<String> mid;

  /** 잔향과 지속감을 구성하는 베이스 노트 이름 목록 */
  private List<String> base;
}
