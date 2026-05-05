package kim.biryeong.perfume.review.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 리뷰 목록 응답에서 개별 리뷰 카드에 표시할 공개 필드를 표현한다. */
@Getter
@AllArgsConstructor
public class ReviewItemDto {
  /** 리뷰 작성자의 닉네임 */
  private String nickname;

  /** 리뷰 작성자의 프로필 이미지 URL. 등록된 이미지가 없으면 null일 수 있다. */
  private String profileImageUrl;

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

  /** 리뷰 작성일. 날짜 단위로만 노출한다. */
  private LocalDate createdAt;
}
