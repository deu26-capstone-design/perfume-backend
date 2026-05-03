package kim.biryeong.perfume.perfume;

import kim.biryeong.perfume.accord.PerfumeAccordRepository;
import kim.biryeong.perfume.review.Review;
import kim.biryeong.perfume.review.ReviewItemDto;
import kim.biryeong.perfume.review.ReviewListResponse;
import kim.biryeong.perfume.review.ReviewRepository;
import kim.biryeong.perfume.review.ReviewSeason;
import kim.biryeong.perfume.review.ReviewSeasonRepository;
import kim.biryeong.perfume.review.ReviewScent;
import kim.biryeong.perfume.review.ReviewScentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerfumeService {

    private static final int PAGE_SIZE = 30;

    private final PerfumeRepository perfumeRepository;
    private final PerfumeNoteRepository perfumeNoteRepository;
    private final PerfumeAccordRepository perfumeAccordRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewSeasonRepository reviewSeasonRepository;
    private final ReviewScentRepository reviewScentRepository;

    @Transactional(readOnly = true)
    public Page<PerfumeCardProjection> getPerfumes(String keyword, String gender, String accord, String sort, int page) {
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE);
        if ("rating_asc".equals(sort)) {
            return perfumeRepository.findAllByFiltersOrderByRatingAsc(keyword, gender, accord, pageable);
        }
        return perfumeRepository.findAllByFiltersOrderByRatingDesc(keyword, gender, accord, pageable);
    }

    @Transactional(readOnly = true)
    public PerfumeDetailResponse getPerfumeDetail(Long id) {
        Perfume perfume = perfumeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<AccordDto> accords = perfumeAccordRepository.findByPerfumeId(perfume.getId()).stream()
                .map(a -> new AccordDto(a.getAccordName(), a.getRatio()))
                .collect(Collectors.toList());

        var notesByType = perfumeNoteRepository.findByPerfumeId(perfume.getId()).stream()
                .collect(Collectors.groupingBy(
                        PerfumeNote::getNoteType,
                        Collectors.mapping(PerfumeNote::getNoteName, Collectors.toList())
                ));
        NoteGroupDto notes = new NoteGroupDto(
                notesByType.getOrDefault("top", List.of()),
                notesByType.getOrDefault("mid", List.of()),
                notesByType.getOrDefault("base", List.of())
        );

        long reviewCount = reviewRepository.countByPerfumeId(perfume.getId());
        Double avgSatisfaction = reviewRepository.findAvgSatisfactionByPerfumeId(perfume.getId());
        double rating = avgSatisfaction == null ? 0.0 : Math.round(avgSatisfaction * 10.0) / 10.0;

        return new PerfumeDetailResponse(
                perfume.getId(), perfume.getImageUrl(), perfume.getBrand(), perfume.getName(),
                perfume.getGender(), perfume.getDescription(),
                rating, reviewCount, notes, accords
        );
    }

    @Transactional(readOnly = true)
    public StatsDto getReviewSummary(Long id) {
        if (!perfumeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<Review> reviews = reviewRepository.findByPerfumeId(id);
        List<ReviewSeason> seasons = reviewSeasonRepository.findByPerfumeId(id);
        return buildStats(reviews, seasons, reviews.size());
    }

    @Transactional(readOnly = true)
    public ReviewListResponse getReviews(Long perfumeId, int page) {
        if (!perfumeRepository.existsById(perfumeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        PageRequest pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Review> reviewPage = reviewRepository.findByPerfumeIdOrderByCreatedAtDesc(perfumeId, pageable);

        List<Integer> userIds = reviewPage.getContent().stream()
                .map(r -> r.getUser().getUserId())
                .collect(Collectors.toList());

        Map<Integer, List<String>> seasonsByUser = userIds.isEmpty() ? Map.of() :
                reviewSeasonRepository.findByPerfumeIdAndUserIds(perfumeId, userIds).stream()
                .collect(Collectors.groupingBy(
                        rs -> rs.getUser().getUserId(),
                        Collectors.mapping(ReviewSeason::getSeason, Collectors.toList())
                ));

        Map<Integer, List<String>> scentsByUser = userIds.isEmpty() ? Map.of() :
                reviewScentRepository.findByPerfumeIdAndUserIds(perfumeId, userIds).stream()
                .collect(Collectors.groupingBy(
                        rs -> rs.getUser().getUserId(),
                        Collectors.mapping(ReviewScent::getScentName, Collectors.toList())
                ));

        List<ReviewItemDto> dtos = reviewPage.getContent().stream().map(r -> {
            Integer userId = r.getUser().getUserId();
            return new ReviewItemDto(
                    r.getUser().getNickname(),
                    r.getUser().getProfileImageUrl(),
                    r.getSatisfaction(),
                    r.getLongevity(),
                    seasonsByUser.getOrDefault(userId, List.of()),
                    scentsByUser.getOrDefault(userId, List.of()),
                    r.getComment(),
                    r.getCreatedAt().toLocalDate()
            );
        }).collect(Collectors.toList());

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
                .map(rs -> rs.getUser().getUserId())
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
