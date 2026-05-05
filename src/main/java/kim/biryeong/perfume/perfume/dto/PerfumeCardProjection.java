package kim.biryeong.perfume.perfume.dto;

/** 향수 목록 카드 응답을 만들기 위한 읽기 전용 projection 계약이다. */
public interface PerfumeCardProjection {
  /** 향수 ID */
  Long getId();

  /** 향수 이미지 URL */
  String getImageUrl();

  /** 브랜드명 */
  String getBrand();

  /** 향수명 */
  String getName();

  /** 성별 코드. W는 여성, M은 남성, U는 유니섹스를 의미한다. */
  String getGender();

  /** 평균 만족도. 리뷰가 없으면 0.0으로 계산된다. */
  Double getRating();

  /** 해당 향수에 작성된 리뷰 수 */
  Long getReviewCount();
}
