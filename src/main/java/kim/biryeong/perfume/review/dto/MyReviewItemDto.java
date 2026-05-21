package kim.biryeong.perfume.review.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 내가 작성한 리뷰 목록 응답에서 개별 항목을 표현한다. */
@Getter
@AllArgsConstructor
public class MyReviewItemDto {

  /** 리뷰 ID */
  private Long reviewId;

  /** 향수 ID */
  private Long perfumeId;

  /** 향수 이미지 URL */
  private String perfumeImageUrl;

  /** 향수 이름 */
  private String perfumeName;

  /** 브랜드명 */
  private String brand;

  /** 향수 만족도 점수. 1~5 범위의 정수다. */
  private int satisfaction;

  /** 지속력 점수. 1~3 범위이며 작성자가 선택하지 않으면 null일 수 있다. */
  private Integer longevity;

  /** 리뷰 작성자가 선택한 계절 목록. */
  private List<String> seasons;

  /** 리뷰 작성자가 선택한 향 느낌 목록. 최대 5개다. */
  private List<String> scents;

  /** 리뷰 작성일. 날짜 단위로만 노출한다. */
  private LocalDate createdAt;

  /** 리뷰 본문. 작성하지 않으면 null일 수 있다. */
  private String comment;
}
