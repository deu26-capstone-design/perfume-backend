package kim.biryeong.perfume.review.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 로그인 사용자가 특정 향수에 작성한 리뷰 단건 응답을 표현한다. */
@Getter
@AllArgsConstructor
public class ReviewDetailResponse {
  /** 리뷰 ID. 수정 또는 삭제 대상 식별자로 사용할 수 있다. */
  private Long id;

  /** 향수 만족도 점수. 1~5 범위의 정수다. */
  private int satisfaction;

  /** 지속력 점수. 1~3 범위이며 작성자가 선택하지 않으면 null일 수 있다. */
  private Integer longevity;

  /** 리뷰 작성자가 선택한 계절 목록. 값은 봄/여름/가을/겨울 중 하나다. */
  private List<String> seasons;

  /** 리뷰 작성자가 선택한 향 느낌 목록. 예: 꽃 향, 나무 향, 청량한 향 */
  private List<String> scents;

  /** 리뷰 본문. 작성하지 않으면 null일 수 있다. */
  private String comment;

  /** 면책 조항 동의 여부. 기존 리뷰는 true여야 한다. */
  private Boolean disclaimerAgreed;

  /** 리뷰 작성일. 날짜 단위로만 노출한다. */
  private LocalDate createdAt;
}
