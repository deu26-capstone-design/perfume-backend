package kim.biryeong.perfume.review;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import kim.biryeong.perfume.perfume.PerfumeRepository;
import kim.biryeong.perfume.perfume.StatsDto;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final PerfumeRepository perfumeRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewSeasonRepository reviewSeasonRepository;
    private final ReviewScentRepository reviewScentRepository;

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
        Page<Review> reviewPage = reviewRepository.findByPerfumeIdOrderByCreatedAtDesc(perfumeId, pageable);

        List<Long> reviewIds = reviewPage.getContent().stream()
                .map(Review::getId)
                .collect(Collectors.toList());

        Map<Long, List<String>> seasonsByReview = reviewIds.isEmpty() ? Map.of() :
                reviewSeasonRepository.findByReviewIds(reviewIds).stream()
                .collect(Collectors.groupingBy(
                        rs -> rs.getReview().getId(),
                        Collectors.mapping(ReviewSeason::getSeason, Collectors.toList())
                ));

        Map<Long, List<String>> scentsByReview = reviewIds.isEmpty() ? Map.of() :
                reviewScentRepository.findByReviewIds(reviewIds).stream()
                .collect(Collectors.groupingBy(
                        rs -> rs.getReview().getId(),
                        Collectors.mapping(rs -> rs.getScentName().getValue(), Collectors.toList())
                ));

        List<ReviewItemDto> dtos = reviewPage.getContent().stream().map(r -> new ReviewItemDto(
                r.getUser().getNickname(),
                r.getUser().getProfileImageUrl(),
                r.getSatisfaction(),
                r.getLongevity(),
                seasonsByReview.getOrDefault(r.getId(), List.of()),
                scentsByReview.getOrDefault(r.getId(), List.of()),
                r.getComment(),
                r.getCreatedAt().toLocalDate()
        )).collect(Collectors.toList());

        Page<ReviewItemDto> dtoPage = new PageImpl<>(dtos, pageable, reviewPage.getTotalElements());
        return new ReviewListResponse(dtoPage);
    }

    private StatsDto buildStats(List<Review> reviews, List<ReviewSeason> seasons, long reviewCount) {
        Map<Integer, Integer> satisfactionMap = new LinkedHashMap<>();
        Map<Integer, Integer> longevityMap = new LinkedHashMap<>();
        Map<String, Integer> seasonMap = new LinkedHashMap<>();

        for (int i = 1; i <= 5; i++) satisfactionMap.put(i, 0);
        for (int i = 1; i <= 3; i++) longevityMap.put(i, 0);
        for (String s : List.of("봄", "여름", "가을", "겨울")) seasonMap.put(s, 0);

        if (reviewCount == 0) return new StatsDto(satisfactionMap, longevityMap, seasonMap);

        reviews.stream()
                .collect(Collectors.groupingBy(Review::getSatisfaction, Collectors.counting()))
                .forEach((k, v) -> satisfactionMap.put(k, (int) Math.round(v * 100.0 / reviewCount)));

        List<Review> reviewsWithLongevity = reviews.stream()
                .filter(r -> r.getLongevity() != null)
                .toList();
        long longevityCount = reviewsWithLongevity.size();
        if (longevityCount > 0) {
            reviewsWithLongevity.stream()
                    .collect(Collectors.groupingBy(Review::getLongevity, Collectors.counting()))
                    .forEach((k, v) -> longevityMap.put(k, (int) Math.round(v * 100.0 / longevityCount)));
        }

        long seasonRespondentCount = seasons.stream()
                .map(rs -> rs.getReview().getId())
                .distinct()
                .count();
        if (seasonRespondentCount > 0) {
            seasons.stream()
                    .collect(Collectors.groupingBy(ReviewSeason::getSeason, Collectors.counting()))
                    .forEach((k, v) -> seasonMap.put(k, (int) Math.round(v * 100.0 / seasonRespondentCount)));
        }

        return new StatsDto(satisfactionMap, longevityMap, seasonMap);
    }
}
