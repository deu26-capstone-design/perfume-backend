package kim.biryeong.perfume.preference.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import kim.biryeong.perfume.preference.domain.ScentPreference;
import kim.biryeong.perfume.preference.dto.PreferenceProgressResponse;
import kim.biryeong.perfume.preference.dto.PreferenceResponse;
import kim.biryeong.perfume.preference.dto.TestQuestionResponse;
import kim.biryeong.perfume.preference.dto.Top5PreferenceResponse;
import kim.biryeong.perfume.preference.repository.ScentPreferenceRepository;
import kim.biryeong.perfume.review.domain.ScentName;
import kim.biryeong.perfume.review.service.ReviewService;
import kim.biryeong.perfume.user.domain.User;
import kim.biryeong.perfume.user.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** 향 선호도 테스트 제출, 진행 상태 저장/조회, 결과 조회, 리뷰 점수 반영을 담당한다. */
@Service
public class PreferenceService {

  private static final List<TestQuestionResponse> TEST_QUESTIONS =
      List.of(
          TestQuestionResponse.of(
              1,
              "이번 주말, 어디서 시간을 보내고 싶어?",
              options("꽃향기 가득한 플라워 마켓", "비 온 뒤 숲속 산책로", "향신료 가득한 빈티지 마켓", "바닐라 캔들 켜진 카페")),
          TestQuestionResponse.of(
              2,
              "훌쩍 어디론가 떠나고 싶을 때, 어디로 가고 싶어?",
              options("프로방스 라벤더 밭", "지중해 레몬 농장", "이국적인 향신료 가득한 중동 바자르", "달콤한 디저트 카페 가득한 파리 골목")),
          TestQuestionResponse.of(
              3,
              "오늘따라 내 방 분위기가 뭔가 마음에 안 들어.. 어떻게 바꿔볼까?",
              options(
                  "꽃과 과일 향이 가득한 내추럴 공간",
                  "화이트와 민트 톤의 깔끔하고 시원한 공간",
                  "원목 가구와 가죽 소파가 있는 클래식한 공간",
                  "따뜻한 조명과 패브릭 소품이 가득한 포근한 공간")),
          TestQuestionResponse.of(
              4,
              "오늘따라 카페 메뉴가 다 맛있어 보이네.. 뭘 시킬까?",
              options(
                  "로즈 라떼, 플로럴 티 같은 꽃향 음료",
                  "레몬에이드, 자몽주스 같은 상큼한 음료",
                  "아메리카노, 얼그레이 티 같은 쌉쌀한 음료",
                  "바닐라 라떼, 카라멜 마끼아또 같은 달달한 음료")),
          TestQuestionResponse.of(
              5,
              "난 이런 날에 기분이 좋더라!",
              options(
                  "따스하고 포근한 봄바람 부는 날",
                  "소나기 온 뒤 풀내음 가득한 맑은 날",
                  "허브와 나무 향이 은은히 퍼지는 선선한 날",
                  "추운 날씨에 따뜻한 코코아 한 모금 마시는 날")),
          TestQuestionResponse.of(
              6,
              "나는 어떤 분위기를 가지고 있을까?",
              options("부드럽고 은은한 분위기", "깔끔하고 활기찬 분위기", "강렬하고 개성있는 분위기", "달콤하고 포근한 분위기")),
          TestQuestionResponse.of(
              7,
              "길을 걷다가 향에 이끌려서 나도 모르게 멈춰버렸어. 어디 앞이었을까?",
              options(
                  "꽃 향기 가득한 꽃집 앞",
                  "비 온 뒤 맑은 공기의 새벽 공원 앞",
                  "나무 향기 가득한 공방 앞",
                  "향신료 냄새 가득한 이국적인 분위기의 가게 앞")),
          TestQuestionResponse.of(
              8,
              "이 냄새를 맡으면 나도 모르게 기분이 좋아진단 말이지..",
              options(
                  "포근하고 따뜻한 살냄새 같은 향", "상큼하고 깔끔한 시트러스 향", "허브와 나무가 섞인 은은한 향", "비 온 뒤 흙내음과 나무 냄새")),
          TestQuestionResponse.of(
              9,
              "생일 선물로 향수를 받는다면 어떤 향이었으면 좋겠어?",
              options("달콤하고 생동감 있는 과일향", "싱그럽고 자연스러운 풀내음", "강렬하고 이국적인 향신료 향", "신비롭고 스모키한 흙내음")),
          TestQuestionResponse.of(
              10,
              "대문자 N인 친구가 또 만약에를 시전한다. 타임머신이 생기면 어디로 갈 거냐는데 흠..",
              options(
                  "왈츠가 흐르는 19세기 유럽 무도회",
                  "레몬 향 가득한 고대 지중해 도시",
                  "향료 무역이 활발하던 실크로드 시대",
                  "깊은 숲 속 나무 향 가득한 중세시대")),
          TestQuestionResponse.of(
              11,
              "아무도 모르는 나만의 공간이 생긴다면?",
              options(
                  "캔들과 패브릭이 가득한 아늑한 다락방",
                  "통유리창 너머 도시가 보이는 루프탑",
                  "흙냄새 가득한 지하 비밀 서재",
                  "향신료 가득한 이국적인 비밀 상점")),
          TestQuestionResponse.of(
              12,
              "어느날 판타지 소설의 주인공이 되었다. 여긴 어디지..?",
              options(
                  "달콤한 과일이 가득한 신비한 섬",
                  "싱그러운 자연이 드리워지는 외딴 숲",
                  "달콤한 디저트 파티가 매일 열리는 마을",
                  "초콜릿과 캐러멜 향 가득한 동화 세계")));

  private static final double REVIEW_SCORE_INCREMENT = 2.0;

  private final ScentPreferenceRepository scentPreferenceRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;
  // ReviewService와 상호 의존 관계이므로 @Lazy 프록시로 순환 의존을 해소한다.
  private final ReviewService reviewService;

  public PreferenceService(
      ScentPreferenceRepository scentPreferenceRepository,
      UserRepository userRepository,
      ObjectMapper objectMapper,
      @Lazy ReviewService reviewService) {
    this.scentPreferenceRepository = scentPreferenceRepository;
    this.userRepository = userRepository;
    this.objectMapper = objectMapper;
    this.reviewService = reviewService;
  }

  /**
   * 향 선호도 테스트를 제출하고 결과를 저장한다.
   *
   * <p>테스트는 재응시가 불가능하다. 이미 완료한 사용자가 다시 제출하면 409를 반환한다.
   *
   * <p>테스트 완료 시점에 이미 작성된 리뷰의 향 계열 점수를 모두 소급 적용한다. 이후 리뷰 작성·수정·삭제는 즉시 반영된다.
   *
   * @param userId 테스트를 제출하는 사용자 ID
   * @param answers 문항번호(1~12)를 키, 선택지("A"/"B"/"C"/"D")를 값으로 하는 답변 맵
   * @throws ResponseStatusException 존재하지 않는 사용자(404), 이미 완료한 테스트(409)
   */
  @Transactional
  public void submitTest(Integer userId, Map<Integer, String> answers) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저 ID입니다."));

    Set<Integer> expectedKeys = IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toSet());
    if (!answers.keySet().equals(expectedKeys)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "1번~12번 문항을 모두 응답해야 합니다.");
    }

    ScentPreference preference =
        scentPreferenceRepository.findByUserId(userId).orElseGet(() -> new ScentPreference(user));

    if (preference.getTestCompletedAt() != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 완료한 테스트입니다.");
    }

    Map<ScentName, Double> testScores;
    try {
      testScores = PreferenceScoreCalculator.calculateTestScores(answers);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    for (ScentName scent : ScentName.values()) {
      preference.setTestScore(scent, testScores.get(scent));
    }

    // 테스트 완료 전 작성된 리뷰의 향 계열 점수를 소급 적용한다.
    List<ScentName> existingReviewScents = reviewService.getExistingReviewScents(userId);
    for (ScentName scent : existingReviewScents) {
      preference.addReviewScore(scent, REVIEW_SCORE_INCREMENT);
    }

    preference.setTestCompletedAt(LocalDateTime.now(ZoneId.systemDefault()));
    preference.setInProgressAnswers(null);

    scentPreferenceRepository.save(preference);
  }

  /**
   * 테스트 진행 중 답변 상태를 저장한다.
   *
   * <p>뒤로가기로 수정한 경우에도 현재 전체 상태를 덮어쓰는 방식으로 처리한다. 이미 테스트를 완료한 사용자는 409를 반환한다.
   *
   * @param userId 사용자 ID
   * @param answers 현재까지 답변한 문항 맵 (키: 1~12, 값: A/B/C/D)
   */
  @Transactional
  public void saveProgress(Integer userId, Map<Integer, String> answers) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저 ID입니다."));

    ScentPreference preference =
        scentPreferenceRepository.findByUserId(userId).orElseGet(() -> new ScentPreference(user));

    if (preference.getTestCompletedAt() != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 완료한 테스트입니다.");
    }

    try {
      preference.setInProgressAnswers(objectMapper.writeValueAsString(answers));
    } catch (JacksonException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "답변 형식이 올바르지 않습니다.");
    }
    scentPreferenceRepository.save(preference);
  }

  /**
   * 테스트 진행 중 저장된 답변 상태를 조회한다.
   *
   * <p>저장된 진행 상태가 없으면 빈 맵을 반환한다.
   *
   * @param userId 사용자 ID
   * @return 저장된 답변 맵. 없으면 빈 맵
   */
  @Transactional(readOnly = true)
  public PreferenceProgressResponse getProgress(Integer userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저 ID입니다.");
    }

    ScentPreference preference = scentPreferenceRepository.findByUserId(userId).orElse(null);

    if (preference == null) {
      return new PreferenceProgressResponse(false, Map.of());
    }

    if (preference.getTestCompletedAt() != null) {
      return new PreferenceProgressResponse(true, Map.of());
    }

    if (preference.getInProgressAnswers() == null) {
      return new PreferenceProgressResponse(false, Map.of());
    }

    try {
      Map<Integer, String> answers =
          objectMapper.readValue(
              preference.getInProgressAnswers(), new TypeReference<Map<Integer, String>>() {});
      return new PreferenceProgressResponse(false, answers);
    } catch (JacksonException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "진행 상태 데이터가 손상되었습니다.");
    }
  }

  /**
   * 사용자의 향 선호도 전체 점수를 조회한다.
   *
   * <p>테스트를 완료하지 않은 사용자는 testCompleted=false, scores=빈 맵으로 응답한다.
   *
   * @param userId 조회할 사용자 ID
   * @throws ResponseStatusException 존재하지 않는 사용자(404)
   */
  @Transactional(readOnly = true)
  public PreferenceResponse getPreference(Integer userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저 ID입니다.");
    }

    ScentPreference preference = scentPreferenceRepository.findByUserId(userId).orElse(null);

    if (preference == null || preference.getTestCompletedAt() == null) {
      return new PreferenceResponse(false, Map.of());
    }

    Map<ScentName, Double> finalScores = PreferenceScoreCalculator.calculateFinalScores(preference);
    Map<String, Double> namedScores = new LinkedHashMap<>();
    for (ScentName scent : ScentName.values()) {
      namedScores.put(scent.getEnglishName(), finalScores.getOrDefault(scent, 0.0));
    }

    return new PreferenceResponse(true, namedScores);
  }

  /**
   * 사용자의 향 선호도 Top5를 조회한다.
   *
   * <p>테스트를 완료하지 않은 사용자는 testCompleted=false, top5=빈 리스트로 응답한다.
   *
   * @param userId 조회할 사용자 ID
   * @throws ResponseStatusException 존재하지 않는 사용자(404)
   */
  @Transactional(readOnly = true)
  public Top5PreferenceResponse getTop5(Integer userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저 ID입니다.");
    }

    ScentPreference preference = scentPreferenceRepository.findByUserId(userId).orElse(null);

    if (preference == null || preference.getTestCompletedAt() == null) {
      return new Top5PreferenceResponse(false, List.of());
    }

    Map<ScentName, Double> finalScores = PreferenceScoreCalculator.calculateFinalScores(preference);
    List<ScentName> top5 = PreferenceScoreCalculator.selectTop5(finalScores);

    List<Top5PreferenceResponse.ScentEntry> entries =
        top5.stream()
            .map(
                scent ->
                    new Top5PreferenceResponse.ScentEntry(
                        scent.getEnglishName(), finalScores.getOrDefault(scent, 0.0)))
            .filter(entry -> entry.score() > 0.0)
            .toList();

    return new Top5PreferenceResponse(true, entries);
  }

  /**
   * 향 선호도 테스트 전체 문항 목록을 반환한다.
   *
   * <p>문항 데이터는 서버에 정적으로 정의된 불변 데이터이다.
   *
   * @return 1번~12번 문항 리스트
   */
  public List<TestQuestionResponse> getTestQuestions() {
    return TEST_QUESTIONS;
  }

  /**
   * 리뷰 작성 시 향 선호도 리뷰 누적 점수를 갱신한다.
   *
   * <p>테스트를 완료하지 않은 사용자는 조용히 건너뛴다.
   *
   * @param userId 리뷰를 작성한 사용자 ID
   * @param scents 리뷰에서 선택한 향 계열 목록
   */
  @Transactional
  public void applyReviewCreate(Integer userId, List<ScentName> scents) {
    ScentPreference preference = scentPreferenceRepository.findByUserId(userId).orElse(null);
    if (preference == null || preference.getTestCompletedAt() == null) {
      return;
    }

    for (ScentName scent : scents) {
      preference.addReviewScore(scent, REVIEW_SCORE_INCREMENT);
    }
    scentPreferenceRepository.save(preference);
  }

  /**
   * 리뷰 수정 시 기존 향 계열 점수를 차감하고 새 향 계열 점수를 추가한다.
   *
   * <p>테스트를 완료하지 않은 사용자는 조용히 건너뛴다.
   *
   * @param userId 리뷰를 수정한 사용자 ID
   * @param oldScents 수정 전 선택했던 향 계열 목록
   * @param newScents 수정 후 선택한 향 계열 목록
   */
  @Transactional
  public void applyReviewUpdate(
      Integer userId, List<ScentName> oldScents, List<ScentName> newScents) {
    ScentPreference preference = scentPreferenceRepository.findByUserId(userId).orElse(null);
    if (preference == null || preference.getTestCompletedAt() == null) {
      return;
    }

    for (ScentName scent : oldScents) {
      preference.addReviewScore(scent, -REVIEW_SCORE_INCREMENT);
    }
    for (ScentName scent : newScents) {
      preference.addReviewScore(scent, REVIEW_SCORE_INCREMENT);
    }
    scentPreferenceRepository.save(preference);
  }

  /**
   * 리뷰 삭제 시 해당 향 계열 점수를 차감한다.
   *
   * <p>테스트를 완료하지 않은 사용자는 조용히 건너뛴다.
   *
   * @param userId 리뷰를 삭제한 사용자 ID
   * @param scents 삭제된 리뷰에서 선택했던 향 계열 목록
   */
  @Transactional
  public void applyReviewDelete(Integer userId, List<ScentName> scents) {
    ScentPreference preference = scentPreferenceRepository.findByUserId(userId).orElse(null);
    if (preference == null || preference.getTestCompletedAt() == null) {
      return;
    }

    for (ScentName scent : scents) {
      preference.addReviewScore(scent, -REVIEW_SCORE_INCREMENT);
    }
    scentPreferenceRepository.save(preference);
  }

  private static Map<String, String> options(String a, String b, String c, String d) {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("A", a);
    map.put("B", b);
    map.put("C", c);
    map.put("D", d);
    return map;
  }
}
