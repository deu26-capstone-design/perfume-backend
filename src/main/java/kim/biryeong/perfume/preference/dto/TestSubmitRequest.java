package kim.biryeong.perfume.preference.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Getter;

/** 향 선호도 테스트 제출 요청. 12문항의 답변(A/B/C/D)을 문항 번호(1~12)를 키로 전달한다. */
@Getter
public class TestSubmitRequest {

  /** 문항번호(1~12)를 키, 선택지(A/B/C/D)를 값으로 하는 답변 맵. 12문항 모두 포함해야 한다. */
  @NotNull
  @Size(min = 12, max = 12, message = "12문항 모두 응답해야 합니다.")
  private Map<Integer, String> answers;
}
