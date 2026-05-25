package kim.biryeong.perfume.preference.dto;

import java.util.Map;

/**
 * 향 선호도 전체 점수 조회 응답.
 *
 * @param testCompleted 테스트 완료 여부
 * @param scores 향 계열별 퍼센트 점수. 키는 영어 향 계열명(ScentName.getEnglishName()), 값은 퍼센트. 테스트 미완료 시 빈 맵.
 */
public record PreferenceResponse(boolean testCompleted, Map<String, Double> scores) {}
