package kim.biryeong.perfume.preference.dto;

import java.util.List;

/**
 * 향 선호도 Top5 조회 응답.
 *
 * @param testCompleted 테스트 완료 여부
 * @param top5 상위 5개 향 계열 목록. 테스트 미완료 시 빈 리스트.
 */
public record Top5PreferenceResponse(boolean testCompleted, List<ScentEntry> top5) {

  /**
   * 향 계열 항목.
   *
   * @param scentName 영어 향 계열명(ScentName.getEnglishName())
   * @param score 퍼센트 점수
   */
  public record ScentEntry(String scentName, double score) {}
}
