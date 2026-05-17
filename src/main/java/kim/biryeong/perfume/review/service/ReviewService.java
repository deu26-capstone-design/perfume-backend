package kim.biryeong.perfume.review.service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.dto.StatsDto;
import kim.biryeong.perfume.perfume.repository.PerfumeRepository;
import kim.biryeong.perfume.review.domain.Review;
import kim.biryeong.perfume.review.domain.ReviewScent;
import kim.biryeong.perfume.review.domain.ReviewSeason;
import kim.biryeong.perfume.review.domain.ScentName;
import kim.biryeong.perfume.review.domain.Season;
import kim.biryeong.perfume.review.dto.ReviewCreateResponse;
import kim.biryeong.perfume.review.dto.ReviewItemDto;
import kim.biryeong.perfume.review.dto.ReviewListResponse;
import kim.biryeong.perfume.review.dto.ReviewRequest;
import kim.biryeong.perfume.review.dto.ReviewUpdateRequest;
import kim.biryeong.perfume.review.repository.ReviewRepository;
import kim.biryeong.perfume.review.repository.ReviewScentRepository;
import kim.biryeong.perfume.review.repository.ReviewSeasonRepository;
import kim.biryeong.perfume.user.domain.User;
import kim.biryeong.perfume.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final PerfumeRepository perfumeRepository;
  private final UserRepository userRepository;
  private final ReviewRepository reviewRepository;
  private final ReviewSeasonRepository reviewSeasonRepository;
  private final ReviewScentRepository reviewScentRepository;

  @Transactional
  public ReviewCreateResponse createReview(Long perfumeId, Integer userId, ReviewRequest request) {
    if (request.getDisclaimerAgreed() == null || !request.getDisclaimerAgreed()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "면책 조항에 동의해야 합니다.");
    }

    List<Season> seasonEnums = toSeasonEnums(request.getSeasons());
    List<ScentName> scentEnums = toScentEnums(request.getScents());

    Perfume perfume =
        perfumeRepository
            .findById(perfumeId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 향수 ID입니다."));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저 ID입니다."));

    if (reviewRepository.existsByPerfumeIdAndUserId(perfumeId, userId)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 작성한 리뷰가 있습니다.");
    }

    Review review =
        new Review(
            null,
            perfume,
            user,
            request.getSatisfaction(),
            request.getLongevity(),
            request.getComment(),
            request.getDisclaimerAgreed(),
            null);
    reviewRepository.save(review);

    if (!seasonEnums.isEmpty()) {
      List<ReviewSeason> seasons =
          seasonEnums.stream().map(season -> new ReviewSeason(review, season)).toList();
      reviewSeasonRepository.saveAll(seasons);
    }

    if (!scentEnums.isEmpty()) {
      List<ReviewScent> scents =
          scentEnums.stream().map(scent -> new ReviewScent(null, review, scent)).toList();
      reviewScentRepository.saveAll(scents);
    }

    List<Review> reviews = reviewRepository.findByPerfumeId(perfumeId);
    List<ReviewSeason> reviewSeasons = reviewSeasonRepository.findByPerfumeId(perfumeId);
    long reviewCount = reviews.size();
    double rating =
        Math.round(reviews.stream().mapToInt(Review::getSatisfaction).average().orElse(0.0) * 10.0)
            / 10.0;
    StatsDto stats = buildStats(reviews, reviewSeasons, reviewCount);

    return new ReviewCreateResponse(
        rating, reviewCount, stats.getSatisfaction(), stats.getLongevity(), stats.getSeasons());
  }

  @Transactional(readOnly = true)
  public StatsDto getReviewSummary(Long perfumeId) {
    if (!perfumeRepository.existsById(perfumeId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 향수 ID입니다.");
    }
    List<Review> reviews = reviewRepository.findByPerfumeId(perfumeId);
    List<ReviewSeason> seasons = reviewSeasonRepository.findByPerfumeId(perfumeId);
    return buildStats(reviews, seasons, reviews.size());
  }

  @Transactional(readOnly = true)
  public ReviewListResponse getReviews(Long perfumeId, int page, int size) {
    if (!perfumeRepository.existsById(perfumeId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 향수 ID입니다.");
    }

    PageRequest pageable = PageRequest.of(page, size);
    Page<Review> reviewPage =
        reviewRepository.findByPerfumeIdOrderByCreatedAtDescIdDesc(perfumeId, pageable);

    List<Long> reviewIds =
        reviewPage.getContent().stream().map(Review::getId).collect(Collectors.toList());

    Map<Long, List<String>> seasonsByReview =
        reviewIds.isEmpty()
            ? Map.of()
            : reviewSeasonRepository.findByReviewIds(reviewIds).stream()
                .collect(
                    Collectors.groupingBy(
                        reviewSeason -> reviewSeason.getReview().getId(),
                        Collectors.mapping(
                            reviewSeason -> reviewSeason.getSeason().getValue(),
                            Collectors.toList())));

    Map<Long, List<String>> scentsByReview =
        reviewIds.isEmpty()
            ? Map.of()
            : reviewScentRepository.findByReviewIds(reviewIds).stream()
                .collect(
                    Collectors.groupingBy(
                        reviewScent -> reviewScent.getReview().getId(),
                        Collectors.mapping(
                            reviewScent -> reviewScent.getScentName().getValue(),
                            Collectors.toList())));

    List<ReviewItemDto> dtos =
        reviewPage.getContent().stream()
            .map(
                review ->
                    new ReviewItemDto(
                        review.getUser().getNickname(),
                        review.getUser().getProfileImageUrl(),
                        review.getSatisfaction(),
                        review.getLongevity(),
                        seasonsByReview.getOrDefault(review.getId(), List.of()),
                        scentsByReview.getOrDefault(review.getId(), List.of()),
                        review.getComment(),
                        review.getCreatedAt().toLocalDate()))
            .collect(Collectors.toList());

    Page<ReviewItemDto> dtoPage = new PageImpl<>(dtos, pageable, reviewPage.getTotalElements());
    return new ReviewListResponse(dtoPage);
  }

  @Transactional
  public void updateReview(Long reviewId, Integer userId, ReviewUpdateRequest request) {
    if (request.getDisclaimerAgreed() == null || !request.getDisclaimerAgreed()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "면책 조항에 동의해야 합니다.");
    }

    Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰입니다."));

    if (!review.getUser().getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정할 수 있습니다.");
    }

    List<Season> seasonEnums = toSeasonEnums(request.getSeasons());
    List<ScentName> scentEnums = toScentEnums(request.getScents());

    review.setSatisfaction(request.getSatisfaction());
    review.setLongevity(request.getLongevity());
    review.setComment(request.getComment());

    reviewSeasonRepository.deleteAll(reviewSeasonRepository.findByReviewIds(List.of(reviewId)));
    reviewSeasonRepository.flush();
    if (!seasonEnums.isEmpty()) {
      reviewSeasonRepository.saveAll(
          seasonEnums.stream().map(season -> new ReviewSeason(review, season)).toList());
    }

    reviewScentRepository.deleteAll(reviewScentRepository.findByReviewIds(List.of(reviewId)));
    reviewScentRepository.flush();
    if (!scentEnums.isEmpty()) {
      reviewScentRepository.saveAll(
          scentEnums.stream().map(scent -> new ReviewScent(null, review, scent)).toList());
    }
  }

  @Transactional
  public void deleteReview(Long reviewId, Integer userId) {
    Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰입니다."));

    if (!review.getUser().getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 리뷰만 삭제할 수 있습니다.");
    }

    reviewSeasonRepository.deleteAll(reviewSeasonRepository.findByReviewIds(List.of(reviewId)));
    reviewScentRepository.deleteAll(reviewScentRepository.findByReviewIds(List.of(reviewId)));
    reviewRepository.delete(review);
  }

  private List<Season> toSeasonEnums(List<String> seasonValues) {
    if (seasonValues == null) {
      return List.of();
    }
    List<Season> seasons = seasonValues.stream().map(Season::from).toList();
    if (seasons.size() != new HashSet<>(seasons).size()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "중복된 계절 값이 있습니다.");
    }
    return seasons;
  }

  private List<ScentName> toScentEnums(List<String> scentValues) {
    if (scentValues == null) {
      return List.of();
    }
    List<ScentName> scents = scentValues.stream().map(ScentName::from).toList();
    if (scents.size() != new HashSet<>(scents).size()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "중복된 향 값이 있습니다.");
    }
    return scents;
  }

  private StatsDto buildStats(List<Review> reviews, List<ReviewSeason> seasons, long reviewCount) {
    Map<Integer, Integer> satisfactionMap = new LinkedHashMap<>();
    Map<Integer, Integer> longevityMap = new LinkedHashMap<>();
    Map<String, Integer> seasonMap = new LinkedHashMap<>();

    for (int i = 1; i <= 5; i++) {
      satisfactionMap.put(i, 0);
    }
    for (int i = 1; i <= 3; i++) {
      longevityMap.put(i, 0);
    }
    for (Season season : Season.values()) {
      seasonMap.put(season.getValue(), 0);
    }

    if (reviewCount == 0) {
      return new StatsDto(satisfactionMap, longevityMap, seasonMap);
    }

    reviews.stream()
        .collect(Collectors.groupingBy(Review::getSatisfaction, Collectors.counting()))
        .forEach((k, v) -> satisfactionMap.put(k, (int) Math.round(v * 100.0 / reviewCount)));

    List<Review> reviewsWithLongevity =
        reviews.stream().filter(review -> review.getLongevity() != null).toList();
    long longevityCount = reviewsWithLongevity.size();
    if (longevityCount > 0) {
      reviewsWithLongevity.stream()
          .collect(Collectors.groupingBy(Review::getLongevity, Collectors.counting()))
          .forEach((k, v) -> longevityMap.put(k, (int) Math.round(v * 100.0 / longevityCount)));
    }

    long seasonRespondentCount =
        seasons.stream().map(reviewSeason -> reviewSeason.getReview().getId()).distinct().count();
    if (seasonRespondentCount > 0) {
      seasons.stream()
          .collect(
              Collectors.groupingBy(
                  reviewSeason -> reviewSeason.getSeason().getValue(), Collectors.counting()))
          .forEach((k, v) -> seasonMap.put(k, (int) Math.round(v * 100.0 / seasonRespondentCount)));
    }

    return new StatsDto(satisfactionMap, longevityMap, seasonMap);
  }
}
