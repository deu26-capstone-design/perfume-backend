package kim.biryeong.perfume.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import kim.biryeong.perfume.perfume.Perfume;
import kim.biryeong.perfume.perfume.PerfumeRepository;
import kim.biryeong.perfume.perfume.StatsDto;
import kim.biryeong.perfume.user.User;
import kim.biryeong.perfume.user.UserRepository;
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
