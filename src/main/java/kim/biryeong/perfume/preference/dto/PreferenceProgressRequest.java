package kim.biryeong.perfume.preference.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Getter;

/** 향 선호도 테스트 진행 상태 저장 요청. */
@Getter
public class PreferenceProgressRequest {

  /** 현재까지 답변한 전체 문항 맵. 키는 문항 번호(1~12), 값은 선택지(A/B/C/D). */
  @NotNull private Map<Integer, String> answers;
}
