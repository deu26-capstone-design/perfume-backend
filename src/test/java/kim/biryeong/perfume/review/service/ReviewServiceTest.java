package kim.biryeong.perfume.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.dto.StatsDto;
import kim.biryeong.perfume.perfume.repository.PerfumeRepository;
import kim.biryeong.perfume.review.domain.Review;
import kim.biryeong.perfume.review.domain.ReviewScent;
import kim.biryeong.perfume.review.domain.ReviewSeason;
import kim.biryeong.perfume.review.domain.ScentName;
import kim.biryeong.perfume.review.domain.Season;
import kim.biryeong.perfume.review.dto.ReviewDetailResponse;
import kim.biryeong.perfume.review.dto.ReviewRequest;
import kim.biryeong.perfume.review.repository.ReviewRepository;
import kim.biryeong.perfume.review.repository.ReviewScentRepository;
import kim.biryeong.perfume.review.repository.ReviewSeasonRepository;
import kim.biryeong.perfume.user.domain.User;
import kim.biryeong.perfume.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class ReviewServiceTest {

  private PerfumeRepository perfumeRepository;
  private UserRepository userRepository;
  private ReviewRepository reviewRepository;
  private ReviewSeasonRepository reviewSeasonRepository;
  private ReviewScentRepository reviewScentRepository;
  private ReviewService reviewService;

  @BeforeEach
  void setUp() {
    perfumeRepository = mock(PerfumeRepository.class);
    userRepository = mock(UserRepository.class);
    reviewRepository = mock(ReviewRepository.class);
    reviewSeasonRepository = mock(ReviewSeasonRepository.class);
    reviewScentRepository = mock(ReviewScentRepository.class);
    reviewService =
        new ReviewService(
            perfumeRepository,
            userRepository,
            reviewRepository,
            reviewSeasonRepository,
            reviewScentRepository);
  }

  @Test
  void createReviewRejectsDuplicateReview() {
    when(perfumeRepository.findById(10L)).thenReturn(Optional.of(new Perfume()));
    when(userRepository.findById(7)).thenReturn(Optional.of(new User()));
    when(reviewRepository.existsByPerfumeIdAndUserId(10L, 7)).thenReturn(true);

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> reviewService.createReview(10L, 7, request(List.of("봄"), List.of())));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    verify(reviewRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void createReviewRejectsDuplicateSeasonsBeforeSavingReview() {
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> reviewService.createReview(10L, 7, request(List.of("봄", "봄"), List.of())));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(reviewRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void createReviewRejectsDuplicateScentsBeforeSavingReview() {
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> reviewService.createReview(10L, 7, request(List.of(), List.of("꽃 향", "꽃 향"))));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(reviewRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void createReviewRejectsMissingPerfume() {
    when(perfumeRepository.findById(10L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> reviewService.createReview(10L, 7, request(List.of(), List.of())));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void createReviewRejectsMissingUser() {
    when(perfumeRepository.findById(10L)).thenReturn(Optional.of(new Perfume()));
    when(userRepository.findById(7)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> reviewService.createReview(10L, 7, request(List.of(), List.of())));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void getReviewSummaryReturnsZeroStatsWithoutReviews() {
    when(perfumeRepository.existsById(10L)).thenReturn(true);
    when(reviewRepository.findByPerfumeId(10L)).thenReturn(List.of());
    when(reviewSeasonRepository.findByPerfumeId(10L)).thenReturn(List.of());

    StatsDto stats = reviewService.getReviewSummary(10L);

    assertEquals(0, stats.getSatisfaction().get(1));
    assertEquals(0, stats.getLongevity().get(1));
    assertEquals(0, stats.getSeasons().get("봄"));
  }

  @Test
  void getCurrentUserReviewReturnsReviewData() {
    Review review =
        new Review(
            55L,
            new Perfume(),
            new User(),
            4,
            2,
            "데일리로 좋아요.",
            true,
            LocalDateTime.of(2026, 5, 20, 9, 30));
    when(perfumeRepository.existsById(10L)).thenReturn(true);
    when(userRepository.existsById(7)).thenReturn(true);
    when(reviewRepository.findByPerfumeIdAndUserId(10L, 7)).thenReturn(Optional.of(review));
    when(reviewSeasonRepository.findByReviewIds(List.of(55L)))
        .thenReturn(List.of(new ReviewSeason(review, Season.SPRING)));
    when(reviewScentRepository.findByReviewIds(List.of(55L)))
        .thenReturn(List.of(new ReviewScent(1L, review, ScentName.FLORAL)));

    ReviewDetailResponse response = reviewService.getCurrentUserReview(10L, 7).orElseThrow();

    assertEquals(55L, response.getId());
    assertEquals(4, response.getSatisfaction());
    assertEquals(2, response.getLongevity());
    assertEquals(List.of("봄"), response.getSeasons());
    assertEquals(List.of("꽃 향"), response.getScents());
    assertEquals("데일리로 좋아요.", response.getComment());
    assertTrue(response.getDisclaimerAgreed());
    assertEquals(java.time.LocalDate.of(2026, 5, 20), response.getCreatedAt());
  }

  @Test
  void getCurrentUserReviewReturnsEmptyWhenUserHasNoReview() {
    when(perfumeRepository.existsById(10L)).thenReturn(true);
    when(userRepository.existsById(7)).thenReturn(true);
    when(reviewRepository.findByPerfumeIdAndUserId(10L, 7)).thenReturn(Optional.empty());

    Optional<ReviewDetailResponse> response = reviewService.getCurrentUserReview(10L, 7);

    assertTrue(response.isEmpty());
    verify(reviewSeasonRepository, never()).findByReviewIds(org.mockito.ArgumentMatchers.any());
    verify(reviewScentRepository, never()).findByReviewIds(org.mockito.ArgumentMatchers.any());
  }

  private static ReviewRequest request(List<String> seasons, List<String> scents) {
    ReviewRequest request = new ReviewRequest();
    ReflectionTestUtils.setField(request, "satisfaction", 5);
    ReflectionTestUtils.setField(request, "longevity", 3);
    ReflectionTestUtils.setField(request, "seasons", seasons);
    ReflectionTestUtils.setField(request, "scents", scents);
    ReflectionTestUtils.setField(request, "comment", "좋아요.");
    ReflectionTestUtils.setField(request, "disclaimerAgreed", true);
    return request;
  }
}
