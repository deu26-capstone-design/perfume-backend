package kim.biryeong.perfume.preference.controller;

import jakarta.validation.Valid;
import java.util.List;
import kim.biryeong.perfume.auth.AuthenticatedUserIds;
import kim.biryeong.perfume.preference.dto.PreferenceProgressRequest;
import kim.biryeong.perfume.preference.dto.PreferenceProgressResponse;
import kim.biryeong.perfume.preference.dto.PreferenceResponse;
import kim.biryeong.perfume.preference.dto.TestQuestionResponse;
import kim.biryeong.perfume.preference.dto.TestSubmitRequest;
import kim.biryeong.perfume.preference.dto.Top5PreferenceResponse;
import kim.biryeong.perfume.preference.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** 향 선호도 테스트 제출 및 결과 조회 API를 제공한다. */
@RestController
@RequestMapping("/api/preference")
@RequiredArgsConstructor
public class PreferenceController {

  private final PreferenceService preferenceService;

  /**
   * 향 선호도 테스트 전체 문항 목록을 반환한다.
   *
   * <p>1번~12번 문항과 각 선택지(A/B/C/D) 텍스트를 반환한다.
   */
  @GetMapping("/test/questions")
  public List<TestQuestionResponse> getTestQuestions() {
    return preferenceService.getTestQuestions();
  }

  /**
   * 향 선호도 테스트를 제출한다.
   *
   * <p>12문항에 대한 답변(A/B/C/D)을 제출하면 테스트 점수를 계산하여 저장한다. 테스트는 재응시가 불가능하다.
   *
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @param request 문항번호(1~12)를 키, 선택지(A/B/C/D)를 값으로 하는 답변 맵
   */
  @PostMapping("/test")
  @ResponseStatus(HttpStatus.CREATED)
  public void submitTest(
      Authentication authentication, @RequestBody @Valid TestSubmitRequest request) {
    preferenceService.submitTest(
        AuthenticatedUserIds.currentUserId(authentication), request.getAnswers());
  }

  /**
   * 테스트 진행 중 답변 상태를 저장한다.
   *
   * <p>현재까지 답변한 전체 상태를 전달하면 기존 저장값을 덮어쓴다. 뒤로가기 후 수정도 동일하게 처리된다.
   *
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @param request 현재까지 답변한 문항 맵
   */
  @PatchMapping("/test/progress")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void saveProgress(
      Authentication authentication, @RequestBody @Valid PreferenceProgressRequest request) {
    preferenceService.saveProgress(
        AuthenticatedUserIds.currentUserId(authentication), request.getAnswers());
  }

  /**
   * 테스트 진행 중 저장된 답변 상태를 조회한다.
   *
   * <p>저장된 진행 상태가 없으면 빈 맵을 반환한다.
   *
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @return 저장된 답변 맵
   */
  @GetMapping("/test/progress")
  public PreferenceProgressResponse getProgress(Authentication authentication) {
    return preferenceService.getProgress(AuthenticatedUserIds.currentUserId(authentication));
  }

  /**
   * 사용자의 향 선호도 전체 점수를 조회한다.
   *
   * <p>테스트 미완료 시 testCompleted=false, scores=빈 맵으로 응답한다.
   *
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   */
  @GetMapping
  public PreferenceResponse getPreference(Authentication authentication) {
    return preferenceService.getPreference(AuthenticatedUserIds.currentUserId(authentication));
  }

  /**
   * 사용자의 향 선호도 Top5를 조회한다.
   *
   * <p>테스트 미완료 시 testCompleted=false, top5=빈 리스트로 응답한다.
   *
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   */
  @GetMapping("/top5")
  public Top5PreferenceResponse getTop5(Authentication authentication) {
    return preferenceService.getTop5(AuthenticatedUserIds.currentUserId(authentication));
  }
}
