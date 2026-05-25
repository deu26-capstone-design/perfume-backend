package kim.biryeong.perfume.preference.dto;

import java.util.Map;
import lombok.Getter;

/** 향 선호도 테스트 단일 문항 응답 DTO. */
@Getter
public class TestQuestionResponse {

  /** 문항 번호. 1~12 범위의 정수다. */
  private final int questionNumber;

  /** 문항 본문 텍스트. */
  private final String question;

  /** 선택지 A/B/C/D → 문항 텍스트 (삽입 순서 유지). */
  private final Map<String, String> options;

  private TestQuestionResponse(int questionNumber, String question, Map<String, String> options) {
    this.questionNumber = questionNumber;
    this.question = question;
    this.options = options;
  }

  /** 문항 번호, 본문, 선택지 맵으로 응답 객체를 생성한다. */
  public static TestQuestionResponse of(
      int questionNumber, String question, Map<String, String> options) {
    return new TestQuestionResponse(questionNumber, question, options);
  }
}
