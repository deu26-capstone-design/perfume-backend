package kim.biryeong.perfume.preference.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kim.biryeong.perfume.preference.domain.ScentPreference;
import kim.biryeong.perfume.review.domain.ScentName;
import org.junit.jupiter.api.Test;

class PreferenceScoreCalculatorTest {

  /** 12문항 모두 A를 선택하면 FLORAL, FRUITY, COZY 계열에 점수가 집중되어야 한다. */
  @Test
  void calculateTestScoresAllA() {
    Map<Integer, String> answers = new HashMap<>();
    for (int i = 1; i <= 12; i++) {
      answers.put(i, "A");
    }

    Map<ScentName, Double> scores = PreferenceScoreCalculator.calculateTestScores(answers);

    assertThat(scores).containsKey(ScentName.FLORAL);
    assertThat(scores.get(ScentName.FLORAL)).isGreaterThan(scores.get(ScentName.WOODY));
    double total = scores.values().stream().mapToDouble(Double::doubleValue).sum();
    assertThat(total).isCloseTo(100.0, within(0.5));
  }

  /** 12문항 모두 D를 선택하면 SWEET, DESSERT 계열에 점수가 집중되어야 한다. */
  @Test
  void calculateTestScoresAllD() {
    Map<Integer, String> answers = new HashMap<>();
    for (int i = 1; i <= 12; i++) {
      answers.put(i, "D");
    }

    Map<ScentName, Double> scores = PreferenceScoreCalculator.calculateTestScores(answers);

    assertThat(scores.get(ScentName.SWEET)).isGreaterThan(scores.get(ScentName.FLORAL));
    assertThat(scores.get(ScentName.DESSERT)).isGreaterThan(scores.get(ScentName.FRESH));
    double total = scores.values().stream().mapToDouble(Double::doubleValue).sum();
    assertThat(total).isCloseTo(100.0, within(0.5));
  }

  /** 유효하지 않은 선택지(E)가 포함되면 IllegalArgumentException이 발생해야 한다. */
  @Test
  void calculateTestScoresInvalidChoiceThrows() {
    Map<Integer, String> answers = new HashMap<>();
    for (int i = 1; i <= 12; i++) {
      answers.put(i, "A");
    }
    answers.put(3, "E");

    assertThatThrownBy(() -> PreferenceScoreCalculator.calculateTestScores(answers))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("유효하지 않은 선택지입니다");
  }

  /** 테스트 미완료 ScentPreference는 빈 맵을 반환해야 한다. */
  @Test
  void calculateFinalScoresReturnsEmptyWhenTestNotCompleted() {
    ScentPreference preference = new ScentPreference();
    // testCompletedAt == null

    Map<ScentName, Double> result = PreferenceScoreCalculator.calculateFinalScores(preference);

    assertThat(result).isEmpty();
  }

  /** 테스트 점수만 있고 리뷰 누적이 없는 경우, 최종 점수 합계는 100이어야 한다. */
  @Test
  void calculateFinalScoresNormalizesToHundred() {
    ScentPreference preference = buildCompletedPreference();

    Map<ScentName, Double> result = PreferenceScoreCalculator.calculateFinalScores(preference);

    double total = result.values().stream().mapToDouble(Double::doubleValue).sum();
    assertThat(total).isCloseTo(100.0, within(0.5));
  }

  /** 리뷰 누적 점수가 추가되면 해당 계열의 최종 점수가 높아야 한다. */
  @Test
  void calculateFinalScoresReflectsReviewAccumulation() {
    ScentPreference preference = buildCompletedPreference();
    double floralBefore =
        PreferenceScoreCalculator.calculateFinalScores(preference).get(ScentName.FLORAL);

    preference.addReviewScore(ScentName.FLORAL, 30.0);
    double floralAfter =
        PreferenceScoreCalculator.calculateFinalScores(preference).get(ScentName.FLORAL);

    assertThat(floralAfter).isGreaterThan(floralBefore);
  }

  /** selectTop5는 점수 순으로 5개를 선정해야 한다. */
  @Test
  void selectTop5ReturnsFiveScents() {
    Map<Integer, String> answers = new HashMap<>();
    for (int i = 1; i <= 12; i++) {
      answers.put(i, "A");
    }
    Map<ScentName, Double> scores = PreferenceScoreCalculator.calculateTestScores(answers);

    List<ScentName> top5 = PreferenceScoreCalculator.selectTop5(scores);

    assertThat(top5).hasSize(5);
  }

  /** selectTop5는 가장 높은 점수를 가진 계열을 첫 번째로 반환해야 한다. */
  @Test
  void selectTop5FirstIsHighestScore() {
    Map<Integer, String> answers = new HashMap<>();
    for (int i = 1; i <= 12; i++) {
      answers.put(i, "D");
    }
    Map<ScentName, Double> scores = PreferenceScoreCalculator.calculateTestScores(answers);

    List<ScentName> top5 = PreferenceScoreCalculator.selectTop5(scores);

    ScentName first = top5.get(0);
    assertThat(scores.get(first)).isGreaterThanOrEqualTo(scores.get(top5.get(1)));
  }

  /** selectTop5에 빈 맵을 전달하면 빈 리스트를 반환해야 한다. */
  @Test
  void selectTop5ReturnsEmptyForEmptyScores() {
    List<ScentName> top5 = PreferenceScoreCalculator.selectTop5(Map.of());

    assertThat(top5).isEmpty();
  }

  /** addReviewScore가 음수 결과를 만들 때 0.0으로 고정해야 한다. */
  @Test
  void addReviewScoreFloorAtZero() {
    ScentPreference preference = new ScentPreference();

    preference.addReviewScore(ScentName.FLORAL, -100.0);

    assertThat(preference.getReviewScore(ScentName.FLORAL)).isEqualTo(0.0);
  }

  private ScentPreference buildCompletedPreference() {
    ScentPreference preference = new ScentPreference();
    Map<Integer, String> answers = new HashMap<>();
    for (int i = 1; i <= 12; i++) {
      answers.put(i, "B");
    }
    Map<ScentName, Double> testScores = PreferenceScoreCalculator.calculateTestScores(answers);
    for (ScentName scent : ScentName.values()) {
      preference.setTestScore(scent, testScores.get(scent));
    }
    preference.setTestCompletedAt(LocalDateTime.now(ZoneId.systemDefault()));
    return preference;
  }
}
