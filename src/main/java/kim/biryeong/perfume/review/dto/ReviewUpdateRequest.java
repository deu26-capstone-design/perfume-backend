package kim.biryeong.perfume.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;

/** 리뷰 수정 요청 본문. 작성자 식별자는 JWT subject의 현재 인증 사용자 ID를 사용한다. */
@Getter
public class ReviewUpdateRequest {

  /** 향수 만족도 점수. 1~5 범위의 필수 값이다. */
  @NotNull
  @Min(1)
  @Max(5)
  private Integer satisfaction;

  /** 지속력 점수. 선택 값이며 전달하는 경우 1~3 범위여야 한다. */
  @Min(1)
  @Max(3)
  private Integer longevity;

  /** 리뷰에 어울리는 계절 목록. 봄/여름/가을/겨울 값을 최대 4개까지 전달할 수 있다. */
  @Size(max = 4)
  private List<String> seasons;

  /** 리뷰에 어울리는 향 느낌 목록. 서비스가 정의한 향 이름을 최대 5개까지 전달할 수 있다. */
  @Size(max = 5)
  private List<String> scents;

  /** 리뷰 본문. 선택 값이며 최대 1000자까지 허용된다. */
  @Size(max = 1000)
  private String comment;

  /** 면책 조항 동의 여부. 리뷰 수정 시 true여야 한다. */
  @NotNull private Boolean disclaimerAgreed;
}
