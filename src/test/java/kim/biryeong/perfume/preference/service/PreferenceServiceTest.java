package kim.biryeong.perfume.preference.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kim.biryeong.perfume.preference.domain.ScentPreference;
import kim.biryeong.perfume.preference.dto.PreferenceProgressResponse;
import kim.biryeong.perfume.preference.dto.PreferenceResponse;
import kim.biryeong.perfume.preference.dto.Top5PreferenceResponse;
import kim.biryeong.perfume.preference.repository.ScentPreferenceRepository;
import kim.biryeong.perfume.review.domain.ScentName;
import kim.biryeong.perfume.review.service.ReviewService;
import kim.biryeong.perfume.user.domain.User;
import kim.biryeong.perfume.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

class PreferenceServiceTest {

  private ScentPreferenceRepository scentPreferenceRepository;
  private UserRepository userRepository;
  private ReviewService reviewService;
  private PreferenceService preferenceService;

  @BeforeEach
  void setUp() {
    scentPreferenceRepository = mock(ScentPreferenceRepository.class);
    userRepository = mock(UserRepository.class);
    reviewService = mock(ReviewService.class);
    preferenceService =
        new PreferenceService(
            scentPreferenceRepository, userRepository, new ObjectMapper(), reviewService);
  }

  // --- submitTest ---

  @Test
  void submitTestRejectsUnknownUser() {
    when(userRepository.findById(99)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> preferenceService.submitTest(99, validAnswers()))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void submitTestRejectsAlreadyCompletedTest() {
    User user = mock(User.class);
    when(userRepository.findById(1)).thenReturn(Optional.of(user));
    ScentPreference existing = new ScentPreference();
    existing.setTestCompletedAt(LocalDateTime.now(ZoneId.systemDefault()));
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> preferenceService.submitTest(1, validAnswers()))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.CONFLICT));
  }

  @Test
  void submitTestSavesPreferenceForNewUser() {
    User user = mock(User.class);
    when(userRepository.findById(1)).thenReturn(Optional.of(user));
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.empty());
    when(scentPreferenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(reviewService.getExistingReviewScents(1)).thenReturn(List.of());

    preferenceService.submitTest(1, validAnswers());

    verify(scentPreferenceRepository).save(any(ScentPreference.class));
  }

  @Test
  void submitTestSetsTestCompletedAt() {
    User user = mock(User.class);
    when(userRepository.findById(1)).thenReturn(Optional.of(user));
    ScentPreference preference = new ScentPreference();
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));
    when(scentPreferenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(reviewService.getExistingReviewScents(1)).thenReturn(List.of());

    preferenceService.submitTest(1, validAnswers());

    assertThat(preference.getTestCompletedAt()).isNotNull();
  }

  @Test
  void submitTestAppliesExistingReviewScoresRetroactively() {
    User user = mock(User.class);
    when(userRepository.findById(1)).thenReturn(Optional.of(user));
    ScentPreference preference = new ScentPreference();
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));
    when(scentPreferenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(reviewService.getExistingReviewScents(1))
        .thenReturn(List.of(ScentName.FLORAL, ScentName.FLORAL, ScentName.WOODY));

    preferenceService.submitTest(1, validAnswers());

    // FLORAL 리뷰 2개 × 2.0, WOODY 리뷰 1개 × 2.0
    assertThat(preference.getReviewScore(ScentName.FLORAL)).isEqualTo(4.0);
    assertThat(preference.getReviewScore(ScentName.WOODY)).isEqualTo(2.0);
    assertThat(preference.getReviewScore(ScentName.FRESH)).isEqualTo(0.0);
  }

  @Test
  void submitTestRejectsInvalidQuestionKeys() {
    User user = mock(User.class);
    when(userRepository.findById(1)).thenReturn(Optional.of(user));
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.empty());

    Map<Integer, String> answers = new HashMap<>();
    for (int i = 1; i <= 11; i++) {
      answers.put(i, "A");
    }
    answers.put(13, "A"); // 12번 대신 13번

    assertThatThrownBy(() -> preferenceService.submitTest(1, answers))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST));
  }

  // --- getPreference ---

  @Test
  void getPreferenceReturnsNotCompletedWhenNoPreferenceRecord() {
    when(userRepository.existsById(1)).thenReturn(true);
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.empty());

    PreferenceResponse response = preferenceService.getPreference(1);

    assertThat(response.testCompleted()).isFalse();
    assertThat(response.scores()).isEmpty();
  }

  @Test
  void getPreferenceReturnsNotCompletedWhenTestNotDone() {
    when(userRepository.existsById(1)).thenReturn(true);
    ScentPreference preference = new ScentPreference();
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    PreferenceResponse response = preferenceService.getPreference(1);

    assertThat(response.testCompleted()).isFalse();
    assertThat(response.scores()).isEmpty();
  }

  @Test
  void getPreferenceReturnsScoresAfterTestCompletion() {
    when(userRepository.existsById(1)).thenReturn(true);
    ScentPreference preference = buildCompletedPreference();
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    PreferenceResponse response = preferenceService.getPreference(1);

    assertThat(response.testCompleted()).isTrue();
    assertThat(response.scores()).hasSize(12);
    assertThat(response.scores()).containsKey("Floral");
  }

  @Test
  void getPreferenceRejectsUnknownUser() {
    when(userRepository.existsById(99)).thenReturn(false);

    assertThatThrownBy(() -> preferenceService.getPreference(99))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND));
  }

  // --- getTop5 ---

  @Test
  void getTop5ReturnsNotCompletedWhenTestNotDone() {
    when(userRepository.existsById(1)).thenReturn(true);
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.empty());

    Top5PreferenceResponse response = preferenceService.getTop5(1);

    assertThat(response.testCompleted()).isFalse();
    assertThat(response.top5()).isEmpty();
  }

  @Test
  void getTop5ExcludesZeroScoreEntries() {
    when(userRepository.existsById(1)).thenReturn(true);
    // 전부 B 선택 시 FRESH/CITRUS/GREEN만 점수, 나머지 9개 계열은 0점
    ScentPreference preference = buildCompletedPreference();
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    Top5PreferenceResponse response = preferenceService.getTop5(1);

    assertThat(response.testCompleted()).isTrue();
    assertThat(response.top5()).isNotEmpty();
    assertThat(response.top5()).allSatisfy(entry -> assertThat(entry.score()).isGreaterThan(0.0));
  }

  @Test
  void getTop5RejectsUnknownUser() {
    when(userRepository.existsById(99)).thenReturn(false);

    assertThatThrownBy(() -> preferenceService.getTop5(99))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND));
  }

  // --- saveProgress ---

  @Test
  void saveProgressRejectsUnknownUser() {
    when(userRepository.findById(99)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> preferenceService.saveProgress(99, Map.of(1, "A")))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void saveProgressRejectsAlreadyCompletedTest() {
    User user = mock(User.class);
    when(userRepository.findById(1)).thenReturn(Optional.of(user));
    ScentPreference preference = new ScentPreference();
    preference.setTestCompletedAt(LocalDateTime.now(ZoneId.systemDefault()));
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    assertThatThrownBy(() -> preferenceService.saveProgress(1, Map.of(1, "A")))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.CONFLICT));
  }

  @Test
  void saveProgressSavesAnswers() {
    User user = mock(User.class);
    when(userRepository.findById(1)).thenReturn(Optional.of(user));
    ScentPreference preference = new ScentPreference();
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));
    when(scentPreferenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    preferenceService.saveProgress(1, Map.of(1, "A", 2, "B"));

    verify(scentPreferenceRepository).save(any(ScentPreference.class));
    assertThat(preference.getInProgressAnswers()).isNotNull();
  }

  // --- getProgress ---

  @Test
  void getProgressRejectsUnknownUser() {
    when(userRepository.existsById(99)).thenReturn(false);

    assertThatThrownBy(() -> preferenceService.getProgress(99))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void getProgressReturnsNotCompletedWhenNoRecord() {
    when(userRepository.existsById(1)).thenReturn(true);
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.empty());

    PreferenceProgressResponse response = preferenceService.getProgress(1);

    assertThat(response.testCompleted()).isFalse();
    assertThat(response.answers()).isEmpty();
  }

  @Test
  void getProgressReturnsCompletedWhenTestDone() {
    when(userRepository.existsById(1)).thenReturn(true);
    ScentPreference preference = buildCompletedPreference();
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    PreferenceProgressResponse response = preferenceService.getProgress(1);

    assertThat(response.testCompleted()).isTrue();
    assertThat(response.answers()).isEmpty();
  }

  @Test
  void getProgressReturnsEmptyAnswersWhenNoneStored() {
    when(userRepository.existsById(1)).thenReturn(true);
    ScentPreference preference = new ScentPreference();
    // inProgressAnswers == null
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    PreferenceProgressResponse response = preferenceService.getProgress(1);

    assertThat(response.testCompleted()).isFalse();
    assertThat(response.answers()).isEmpty();
  }

  @Test
  void getProgressReturnsSavedAnswers() {
    when(userRepository.existsById(1)).thenReturn(true);
    ScentPreference preference = new ScentPreference();
    preference.setInProgressAnswers("{\"1\":\"A\",\"2\":\"B\"}");
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    PreferenceProgressResponse response = preferenceService.getProgress(1);

    assertThat(response.testCompleted()).isFalse();
    assertThat(response.answers()).containsEntry(1, "A").containsEntry(2, "B");
  }

  @Test
  void getProgressThrowsWhenDataCorrupted() {
    when(userRepository.existsById(1)).thenReturn(true);
    ScentPreference preference = new ScentPreference();
    preference.setInProgressAnswers("invalid json");
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    assertThatThrownBy(() -> preferenceService.getProgress(1))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            e ->
                assertThat(((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
  }

  // --- applyReviewCreate ---

  @Test
  void applyReviewCreateSkipsWhenTestNotCompleted() {
    ScentPreference preference = new ScentPreference();
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    preferenceService.applyReviewCreate(1, List.of(ScentName.FLORAL));

    assertThat(preference.getReviewScore(ScentName.FLORAL)).isEqualTo(0.0);
  }

  @Test
  void applyReviewCreateSkipsWhenNoPreferenceRecord() {
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.empty());

    // 예외 없이 조용히 종료되어야 한다
    preferenceService.applyReviewCreate(1, List.of(ScentName.FLORAL));
  }

  @Test
  void applyReviewCreateAddsTwoPerScent() {
    ScentPreference preference = buildCompletedPreference();
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    preferenceService.applyReviewCreate(1, List.of(ScentName.FLORAL, ScentName.WOODY));

    assertThat(preference.getReviewScore(ScentName.FLORAL)).isEqualTo(2.0);
    assertThat(preference.getReviewScore(ScentName.WOODY)).isEqualTo(2.0);
    assertThat(preference.getReviewScore(ScentName.FRESH)).isEqualTo(0.0);
  }

  // --- applyReviewUpdate ---

  @Test
  void applyReviewUpdateSubtractsOldAndAddsNew() {
    ScentPreference preference = buildCompletedPreference();
    preference.addReviewScore(ScentName.FLORAL, 6.0); // 기존 리뷰로 쌓인 점수
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    preferenceService.applyReviewUpdate(1, List.of(ScentName.FLORAL), List.of(ScentName.WOODY));

    assertThat(preference.getReviewScore(ScentName.FLORAL)).isEqualTo(4.0); // 6 - 2
    assertThat(preference.getReviewScore(ScentName.WOODY)).isEqualTo(2.0); // 0 + 2
  }

  @Test
  void applyReviewUpdateSkipsWhenTestNotCompleted() {
    ScentPreference preference = new ScentPreference();
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    preferenceService.applyReviewUpdate(1, List.of(ScentName.FLORAL), List.of(ScentName.WOODY));

    assertThat(preference.getReviewScore(ScentName.FLORAL)).isEqualTo(0.0);
    assertThat(preference.getReviewScore(ScentName.WOODY)).isEqualTo(0.0);
  }

  // --- applyReviewDelete ---

  @Test
  void applyReviewDeleteSubtractsTwoPerScent() {
    ScentPreference preference = buildCompletedPreference();
    preference.addReviewScore(ScentName.FLORAL, 8.0);
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    preferenceService.applyReviewDelete(1, List.of(ScentName.FLORAL));

    assertThat(preference.getReviewScore(ScentName.FLORAL)).isEqualTo(6.0); // 8 - 2
  }

  @Test
  void applyReviewDeleteFloorAtZero() {
    ScentPreference preference = buildCompletedPreference();
    // reviewFloral == 0.0 상태에서 삭제 시도
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.of(preference));

    preferenceService.applyReviewDelete(1, List.of(ScentName.FLORAL));

    assertThat(preference.getReviewScore(ScentName.FLORAL)).isEqualTo(0.0);
  }

  @Test
  void applyReviewDeleteSkipsWhenNoPreferenceRecord() {
    when(scentPreferenceRepository.findByUserId(1)).thenReturn(Optional.empty());

    // 예외 없이 조용히 종료되어야 한다
    preferenceService.applyReviewDelete(1, List.of(ScentName.FLORAL));

    verify(scentPreferenceRepository, never()).save(any());
  }

  /** 12문항 모두 B를 선택한 완료된 ScentPreference를 생성한다. */
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

  /** 유효한 12문항 답변 맵을 생성한다. */
  private Map<Integer, String> validAnswers() {
    Map<Integer, String> answers = new HashMap<>();
    for (int i = 1; i <= 12; i++) {
      answers.put(i, "A");
    }
    return answers;
  }
}
