package kim.biryeong.perfume.preference.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kim.biryeong.perfume.preference.domain.ScentPreference;
import kim.biryeong.perfume.review.domain.ScentName;

/**
 * 향 선호도 점수 계산 유틸리티.
 *
 * <p>테스트 답변으로부터 향 계열별 원점수를 계산하고, 리뷰 누적 점수와 합산하여 최종 퍼센트 점수를 산출한다. Top5 선정 시 동점 처리 우선순위를 적용한다.
 */
public final class PreferenceScoreCalculator {

  // 그룹 정의: Top5 동점 처리에 사용
  private static final Set<ScentName> GROUP_A =
      Set.of(ScentName.FLORAL, ScentName.COZY, ScentName.FRUITY);
  private static final Set<ScentName> GROUP_B =
      Set.of(ScentName.FRESH, ScentName.CITRUS, ScentName.GREEN);
  private static final Set<ScentName> GROUP_C =
      Set.of(ScentName.WOODY, ScentName.HERBAL, ScentName.EARTHY, ScentName.SPICY);
  private static final Set<ScentName> GROUP_D = Set.of(ScentName.SWEET, ScentName.DESSERT);
  private static final List<Set<ScentName>> GROUPS = List.of(GROUP_A, GROUP_B, GROUP_C, GROUP_D);

  /**
   * 문항별 선택지 가중치 테이블. 인덱스는 문항번호-1(0-based). 각 항목은 선택지("A"/"B"/"C"/"D") → 향계열-점수 맵.
   *
   * <p>문서 기준 매핑: Musky→COZY, Aromatic→HERBAL, Gourmand→DESSERT, Earthy/Smoky→EARTHY
   */
  private static final List<Map<String, Map<ScentName, Double>>> QUESTION_WEIGHTS =
      List.of(
          // Q1
          Map.of(
              "A", scents(ScentName.FLORAL, 4.0, ScentName.COZY, 1.0, ScentName.FRUITY, 1.0),
              "B", scents(ScentName.GREEN, 3.0, ScentName.FRESH, 2.0, ScentName.CITRUS, 1.0),
              "C", scents(ScentName.WOODY, 4.0, ScentName.HERBAL, 1.0, ScentName.EARTHY, 1.0),
              "D", scents(ScentName.SWEET, 4.0, ScentName.DESSERT, 2.0)),
          // Q2
          Map.of(
              "A", scents(ScentName.COZY, 4.0, ScentName.FLORAL, 1.0, ScentName.FRUITY, 1.0),
              "B", scents(ScentName.CITRUS, 3.0, ScentName.FRESH, 2.0, ScentName.GREEN, 1.0),
              "C", scents(ScentName.HERBAL, 3.0, ScentName.SPICY, 2.0, ScentName.EARTHY, 1.0),
              "D", scents(ScentName.DESSERT, 4.0, ScentName.SWEET, 2.0)),
          // Q3
          Map.of(
              "A", scents(ScentName.FRUITY, 4.0, ScentName.FLORAL, 1.0, ScentName.COZY, 1.0),
              "B", scents(ScentName.FRESH, 3.0, ScentName.GREEN, 2.0, ScentName.CITRUS, 1.0),
              "C", scents(ScentName.WOODY, 2.0, ScentName.SPICY, 3.0, ScentName.EARTHY, 1.0),
              "D", scents(ScentName.SWEET, 3.0, ScentName.DESSERT, 3.0)),
          // Q4
          Map.of(
              "A", scents(ScentName.FLORAL, 3.0, ScentName.COZY, 2.0, ScentName.FRUITY, 1.0),
              "B", scents(ScentName.CITRUS, 3.0, ScentName.FRESH, 2.0, ScentName.GREEN, 1.0),
              "C", scents(ScentName.WOODY, 3.0, ScentName.HERBAL, 2.0, ScentName.SPICY, 1.0),
              "D", scents(ScentName.DESSERT, 4.0, ScentName.SWEET, 2.0)),
          // Q5
          Map.of(
              "A", scents(ScentName.COZY, 3.0, ScentName.FRUITY, 2.0, ScentName.FLORAL, 1.0),
              "B", scents(ScentName.GREEN, 3.0, ScentName.CITRUS, 2.0, ScentName.FRESH, 1.0),
              "C", scents(ScentName.HERBAL, 4.0, ScentName.EARTHY, 1.0, ScentName.WOODY, 1.0),
              "D", scents(ScentName.SWEET, 4.0, ScentName.DESSERT, 2.0)),
          // Q6
          Map.of(
              "A", scents(ScentName.FLORAL, 2.0, ScentName.COZY, 2.0, ScentName.FRUITY, 2.0),
              "B", scents(ScentName.CITRUS, 3.0, ScentName.GREEN, 2.0, ScentName.FRESH, 1.0),
              "C", scents(ScentName.EARTHY, 4.0, ScentName.HERBAL, 1.0, ScentName.SPICY, 1.0),
              "D", scents(ScentName.SWEET, 3.0, ScentName.DESSERT, 3.0)),
          // Q7
          Map.of(
              "A", scents(ScentName.FLORAL, 3.0),
              "B", scents(ScentName.FRESH, 3.0),
              "C", scents(ScentName.WOODY, 3.0),
              "D", scents(ScentName.SPICY, 3.0)),
          // Q8
          Map.of(
              "A", scents(ScentName.COZY, 3.0),
              "B", scents(ScentName.CITRUS, 3.0),
              "C", scents(ScentName.HERBAL, 3.0),
              "D", scents(ScentName.EARTHY, 3.0)),
          // Q9
          Map.of(
              "A", scents(ScentName.FRUITY, 3.0),
              "B", scents(ScentName.GREEN, 3.0),
              "C", scents(ScentName.SPICY, 3.0),
              "D", scents(ScentName.EARTHY, 3.0)),
          // Q10
          Map.of(
              "A", scents(ScentName.FLORAL, 3.0),
              "B", scents(ScentName.FRESH, 3.0),
              "C", scents(ScentName.HERBAL, 3.0),
              "D", scents(ScentName.WOODY, 3.0)),
          // Q11
          Map.of(
              "A", scents(ScentName.COZY, 3.0),
              "B", scents(ScentName.CITRUS, 3.0),
              "C", scents(ScentName.EARTHY, 3.0),
              "D", scents(ScentName.SPICY, 3.0)),
          // Q12
          Map.of(
              "A", scents(ScentName.FRUITY, 3.0),
              "B", scents(ScentName.GREEN, 3.0),
              "C", scents(ScentName.SWEET, 3.0),
              "D", scents(ScentName.DESSERT, 3.0)));

  private PreferenceScoreCalculator() {}

  /**
   * 12문항 답변 맵으로부터 향 계열별 테스트 점수를 계산한다. 결과는 전체 합계=100인 퍼센트로 정규화된다.
   *
   * @param answers 문항번호(1~12)를 키로, 선택지("A"/"B"/"C"/"D")를 값으로 하는 맵
   * @return 12개 ScentName 각각의 퍼센트 점수
   * @throws IllegalArgumentException 선택지가 A/B/C/D 외의 값이거나 해당 문항에 없는 선택지인 경우
   */
  public static Map<ScentName, Double> calculateTestScores(Map<Integer, String> answers) {
    Map<ScentName, Double> raw = new EnumMap<>(ScentName.class);
    for (ScentName scent : ScentName.values()) {
      raw.put(scent, 0.0);
    }

    for (Map.Entry<Integer, String> entry : answers.entrySet()) {
      int questionNumber = entry.getKey();
      String choice = entry.getValue();

      if (questionNumber < 1 || questionNumber > 12) {
        throw new IllegalArgumentException("유효하지 않은 문항 번호입니다: " + questionNumber);
      }
      if (!Set.of("A", "B", "C", "D").contains(choice)) {
        throw new IllegalArgumentException("유효하지 않은 선택지입니다: " + choice);
      }

      Map<String, Map<ScentName, Double>> questionWeights =
          QUESTION_WEIGHTS.get(questionNumber - 1);
      Map<ScentName, Double> choiceWeights = questionWeights.get(choice);
      if (choiceWeights == null) {
        throw new IllegalArgumentException(questionNumber + "번 문항에 선택지 " + choice + "가 없습니다.");
      }

      for (Map.Entry<ScentName, Double> w : choiceWeights.entrySet()) {
        raw.merge(w.getKey(), w.getValue(), Double::sum);
      }
    }

    return normalize(raw);
  }

  /**
   * ScentPreference의 테스트 점수와 리뷰 누적 점수를 합산하여 정규화된 최종 점수를 반환한다.
   *
   * <p>테스트 미완료(testCompletedAt == null) 상태이면 빈 맵을 반환한다.
   *
   * @param preference 사용자 향 선호도 엔티티
   * @return 12개 ScentName 각각의 퍼센트 점수. 테스트 미완료 시 빈 맵.
   */
  public static Map<ScentName, Double> calculateFinalScores(ScentPreference preference) {
    if (preference.getTestCompletedAt() == null) {
      return Collections.emptyMap();
    }

    Map<ScentName, Double> combined = new EnumMap<>(ScentName.class);
    for (ScentName scent : ScentName.values()) {
      Double testScore = preference.getTestScore(scent);
      double reviewScore = preference.getReviewScore(scent);
      combined.put(scent, (testScore != null ? testScore : 0.0) + reviewScore);
    }

    return normalize(combined);
  }

  /**
   * 점수 맵에서 상위 5개 ScentName을 선정한다.
   *
   * <p>동점 처리 우선순위:
   *
   * <ol>
   *   <li>점수 높은 순
   *   <li>해당 계열 그룹이 Top5 안에 많이 포함된 순
   *   <li>해당 계열 그룹의 총점 높은 순
   *   <li>계열명 알파벳 순(ScentName.name() 기준)
   * </ol>
   *
   * @param scores 12개 향 계열의 점수 맵
   * @return 점수 순 상위 5개 ScentName 목록
   */
  public static List<ScentName> selectTop5(Map<ScentName, Double> scores) {
    if (scores.isEmpty()) {
      return List.of();
    }

    // 동점 처리 2순위 계산을 위해 정렬 전 점수 기준 임시 Top5를 1회만 미리 계산
    List<ScentName> top5ByScore =
        Arrays.stream(ScentName.values())
            .sorted(
                Comparator.comparingDouble((ScentName s) -> scores.getOrDefault(s, 0.0)).reversed())
            .limit(5)
            .toList();

    List<ScentName> sorted =
        Arrays.stream(ScentName.values())
            .sorted(
                Comparator.comparingDouble((ScentName s) -> scores.getOrDefault(s, 0.0))
                    .reversed()
                    .thenComparingInt(s -> -countGroupMembersInTop5(s, top5ByScore))
                    .thenComparingDouble(s -> -groupTotalScore(s, scores))
                    .thenComparing(ScentName::name))
            .toList();

    return sorted.subList(0, Math.min(5, sorted.size()));
  }

  /** 맵의 값을 전체 합계=100이 되도록 정규화한다. 합계가 0이면 모든 값을 0으로 반환한다. */
  private static Map<ScentName, Double> normalize(Map<ScentName, Double> raw) {
    double total = raw.values().stream().mapToDouble(Double::doubleValue).sum();
    Map<ScentName, Double> result = new EnumMap<>(ScentName.class);
    for (ScentName scent : ScentName.values()) {
      double normalized = total == 0.0 ? 0.0 : Math.round(raw.get(scent) / total * 1000.0) / 10.0;
      result.put(scent, normalized);
    }
    return result;
  }

  /** scent가 속한 그룹에서 top5ByScore 안에 포함된 계열 수를 반환한다. */
  private static int countGroupMembersInTop5(ScentName scent, List<ScentName> top5ByScore) {
    Set<ScentName> group = findGroup(scent);
    if (group == null) {
      return 0;
    }
    return (int) group.stream().filter(top5ByScore::contains).count();
  }

  /** scent가 속한 그룹의 총 점수를 반환한다. */
  private static double groupTotalScore(ScentName scent, Map<ScentName, Double> scores) {
    Set<ScentName> group = findGroup(scent);
    if (group == null) {
      return 0.0;
    }
    return group.stream().mapToDouble(s -> scores.getOrDefault(s, 0.0)).sum();
  }

  /** scent가 속한 그룹을 반환한다. 어느 그룹에도 속하지 않으면 null. */
  private static Set<ScentName> findGroup(ScentName scent) {
    for (Set<ScentName> group : GROUPS) {
      if (group.contains(scent)) {
        return group;
      }
    }
    return null;
  }

  /** 가중치 맵 빌더 헬퍼. */
  private static Map<ScentName, Double> scents(Object... pairs) {
    if (pairs.length % 2 != 0) {
      throw new IllegalArgumentException("pairs 인자는 짝수 개여야 합니다.");
    }
    Map<ScentName, Double> map = new EnumMap<>(ScentName.class);
    for (int i = 0; i < pairs.length; i += 2) {
      map.put((ScentName) pairs[i], (Double) pairs[i + 1]);
    }
    return map;
  }
}
