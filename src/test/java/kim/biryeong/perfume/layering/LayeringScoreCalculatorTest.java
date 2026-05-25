package kim.biryeong.perfume.layering;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import kim.biryeong.perfume.layering.model.AccordWeight;
import kim.biryeong.perfume.layering.model.LayeringCandidate;
import kim.biryeong.perfume.layering.model.LayeringPerfumeProfile;
import kim.biryeong.perfume.layering.model.LayeringScore;
import kim.biryeong.perfume.layering.model.NoteProfile;
import kim.biryeong.perfume.layering.model.RoleVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LayeringScoreCalculatorTest {

  private LayeringScoreCalculator calculator;

  @BeforeEach
  void setUp() {
    LayeringCompatibilityMatrix matrix = new LayeringCompatibilityMatrix();
    matrix.load();
    calculator = new LayeringScoreCalculator(matrix);
  }

  @Test
  void floralAndMuskyReceivesHighMatrixScore() {
    LayeringScore score = calculator.score(candidate(profile(1L, "Floral"), profile(2L, "Musky")));

    assertThat(score.matrixScore()).isGreaterThanOrEqualTo(90);
    assertThat(score.finalScore()).isGreaterThanOrEqualTo(70);
  }

  @Test
  void gourmandAndFreshReceivesLowScoreOrWarning() {
    LayeringScore score =
        calculator.score(candidate(profile(1L, "Gourmand"), profile(2L, "Fresh")));

    assertThat(score.matrixScore()).isLessThanOrEqualTo(25);
    assertThat(score.finalScore()).isLessThan(60);
    assertThat(score.evidences()).isNotEmpty();
  }

  @Test
  void scoreIsDeterministicAndOrderIndependent() {
    LayeringPerfumeProfile citrus = profile(1L, "Citrus", "Woody");
    LayeringPerfumeProfile floral = profile(2L, "Floral", "Musky");

    LayeringScore first = calculator.score(candidate(citrus, floral));
    LayeringScore second = calculator.score(candidate(citrus, floral));
    LayeringScore reversed = calculator.score(candidate(floral, citrus));

    assertThat(first.finalScore()).isEqualTo(second.finalScore());
    assertThat(first.finalScore()).isEqualTo(reversed.finalScore());
    assertThat(first.dominantPair()).isEqualTo(reversed.dominantPair());
    assertThat(first.evidences()).isEqualTo(reversed.evidences());
  }

  private static LayeringCandidate candidate(
      LayeringPerfumeProfile first, LayeringPerfumeProfile second) {
    return new LayeringCandidate(first, second);
  }

  private static LayeringPerfumeProfile profile(Long id, String... accordNames) {
    double normalizedWeight = 1.0 / accordNames.length;
    List<AccordWeight> accords =
        List.of(accordNames).stream()
            .map(accord -> new AccordWeight(accord, 100, normalizedWeight))
            .toList();
    Map<String, Double> weights =
        accords.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    AccordWeight::name, AccordWeight::normalizedWeight));
    return new LayeringPerfumeProfile(
        id,
        "Brand",
        "Perfume " + id,
        accords,
        new NoteProfile(List.of(), List.of(), List.of()),
        RoleVector.from(weights));
  }
}
