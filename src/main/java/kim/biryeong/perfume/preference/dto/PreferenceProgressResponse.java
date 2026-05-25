package kim.biryeong.perfume.preference.dto;

import java.util.Map;

/**
 * 향 선호도 테스트 진행 상태 조회 응답.
 *
 * @param testCompleted 테스트 완료 여부. true이면 answers는 빈 맵
 * @param answers 저장된 진행 답변. 키는 문항 번호(1~12), 값은 선택지(A/B/C/D). 없으면 빈 맵
 */
public record PreferenceProgressResponse(boolean testCompleted, Map<Integer, String> answers) {}
