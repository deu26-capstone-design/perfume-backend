package kim.biryeong.perfume.preference.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import kim.biryeong.perfume.review.domain.ScentName;
import kim.biryeong.perfume.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자별 향 선호도 데이터를 저장한다.
 *
 * <p>테스트 원점수(testXxx)와 리뷰 누적 점수(reviewXxx)를 분리 저장하여 리뷰 삭제 시 정확한 역산이 가능하다. 최종 점수는
 * PreferenceScoreCalculator.calculateFinalScores()로 계산한다.
 */
@Entity
@Table(name = "scent_preferences")
@Getter
@Setter
@NoArgsConstructor
public class ScentPreference {

  @Id private Integer userId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  // 테스트 원점수 (테스트 미완료 시 null)
  @Column private Double testFloral;

  @Column private Double testWoody;

  @Column private Double testFresh;

  @Column private Double testSpicy;

  @Column private Double testSweet;

  @Column private Double testDessert;

  @Column private Double testCozy;

  @Column private Double testGreen;

  @Column private Double testCitrus;

  @Column private Double testFruity;

  @Column private Double testHerbal;

  @Column private Double testEarthy;

  // 리뷰 누적 점수 (기본값 0.0)
  @Column(nullable = false)
  private Double reviewFloral = 0.0;

  @Column(nullable = false)
  private Double reviewWoody = 0.0;

  @Column(nullable = false)
  private Double reviewFresh = 0.0;

  @Column(nullable = false)
  private Double reviewSpicy = 0.0;

  @Column(nullable = false)
  private Double reviewSweet = 0.0;

  @Column(nullable = false)
  private Double reviewDessert = 0.0;

  @Column(nullable = false)
  private Double reviewCozy = 0.0;

  @Column(nullable = false)
  private Double reviewGreen = 0.0;

  @Column(nullable = false)
  private Double reviewCitrus = 0.0;

  @Column(nullable = false)
  private Double reviewFruity = 0.0;

  @Column(nullable = false)
  private Double reviewHerbal = 0.0;

  @Column(nullable = false)
  private Double reviewEarthy = 0.0;

  /** 테스트 완료 시각. null이면 테스트 미완료 상태이다. */
  @Column private LocalDateTime testCompletedAt;

  /** 테스트 진행 중 임시 저장 답변. JSON 형식 {"1":"A","2":"C",...}. 완료 시 null로 초기화된다. */
  @Column(columnDefinition = "JSON")
  private String inProgressAnswers;

  public ScentPreference(User user) {
    this.user = user;
  }

  /**
   * ScentName에 해당하는 테스트 점수를 반환한다.
   *
   * @param scent 향 계열
   * @return 테스트 점수. 테스트 미완료이거나 해당 계열 점수가 없으면 null
   */
  public Double getTestScore(ScentName scent) {
    return switch (scent) {
      case FLORAL -> testFloral;
      case WOODY -> testWoody;
      case FRESH -> testFresh;
      case SPICY -> testSpicy;
      case SWEET -> testSweet;
      case DESSERT -> testDessert;
      case COZY -> testCozy;
      case GREEN -> testGreen;
      case CITRUS -> testCitrus;
      case FRUITY -> testFruity;
      case HERBAL -> testHerbal;
      case EARTHY -> testEarthy;
    };
  }

  /**
   * ScentName에 해당하는 테스트 점수를 설정한다.
   *
   * @param scent 향 계열
   * @param value 설정할 점수
   */
  public void setTestScore(ScentName scent, Double value) {
    switch (scent) {
      case FLORAL -> testFloral = value;
      case WOODY -> testWoody = value;
      case FRESH -> testFresh = value;
      case SPICY -> testSpicy = value;
      case SWEET -> testSweet = value;
      case DESSERT -> testDessert = value;
      case COZY -> testCozy = value;
      case GREEN -> testGreen = value;
      case CITRUS -> testCitrus = value;
      case FRUITY -> testFruity = value;
      case HERBAL -> testHerbal = value;
      case EARTHY -> testEarthy = value;
    }
  }

  /**
   * ScentName에 해당하는 리뷰 누적 점수를 반환한다.
   *
   * @param scent 향 계열
   * @return 리뷰 누적 점수
   */
  public Double getReviewScore(ScentName scent) {
    return switch (scent) {
      case FLORAL -> reviewFloral;
      case WOODY -> reviewWoody;
      case FRESH -> reviewFresh;
      case SPICY -> reviewSpicy;
      case SWEET -> reviewSweet;
      case DESSERT -> reviewDessert;
      case COZY -> reviewCozy;
      case GREEN -> reviewGreen;
      case CITRUS -> reviewCitrus;
      case FRUITY -> reviewFruity;
      case HERBAL -> reviewHerbal;
      case EARTHY -> reviewEarthy;
    };
  }

  /**
   * ScentName에 해당하는 리뷰 누적 점수에 delta를 더한다. 결과가 0.0 미만이면 0.0으로 고정한다.
   *
   * @param scent 향 계열
   * @param delta 더할 값. 음수 가능
   */
  public void addReviewScore(ScentName scent, double delta) {
    double current = getReviewScore(scent);
    setReviewScore(scent, Math.max(0.0, current + delta));
  }

  private void setReviewScore(ScentName scent, Double value) {
    switch (scent) {
      case FLORAL -> reviewFloral = value;
      case WOODY -> reviewWoody = value;
      case FRESH -> reviewFresh = value;
      case SPICY -> reviewSpicy = value;
      case SWEET -> reviewSweet = value;
      case DESSERT -> reviewDessert = value;
      case COZY -> reviewCozy = value;
      case GREEN -> reviewGreen = value;
      case CITRUS -> reviewCitrus = value;
      case FRUITY -> reviewFruity = value;
      case HERBAL -> reviewHerbal = value;
      case EARTHY -> reviewEarthy = value;
    }
  }
}
