package kim.biryeong.perfume.perfume.service;

import java.util.List;
import java.util.stream.Collectors;
import kim.biryeong.perfume.accord.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.domain.PerfumeNote;
import kim.biryeong.perfume.perfume.dto.AccordDto;
import kim.biryeong.perfume.perfume.dto.NoteGroupDto;
import kim.biryeong.perfume.perfume.dto.PerfumeCardProjection;
import kim.biryeong.perfume.perfume.dto.PerfumeDetailResponse;
import kim.biryeong.perfume.perfume.dto.PerfumeListResponse;
import kim.biryeong.perfume.perfume.dto.StatsDto;
import kim.biryeong.perfume.perfume.repository.PerfumeNoteRepository;
import kim.biryeong.perfume.perfume.repository.PerfumeRepository;
import kim.biryeong.perfume.review.repository.ReviewRepository;
import kim.biryeong.perfume.review.service.ReviewService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PerfumeService {

  private final PerfumeRepository perfumeRepository;
  private final PerfumeNoteRepository perfumeNoteRepository;
  private final PerfumeAccordRepository perfumeAccordRepository;
  private final ReviewRepository reviewRepository;
  private final ReviewService reviewService;

  public PerfumeService(
      PerfumeRepository perfumeRepository,
      PerfumeNoteRepository perfumeNoteRepository,
      PerfumeAccordRepository perfumeAccordRepository,
      ReviewRepository reviewRepository,
      @Lazy ReviewService reviewService) {
    this.perfumeRepository = perfumeRepository;
    this.perfumeNoteRepository = perfumeNoteRepository;
    this.perfumeAccordRepository = perfumeAccordRepository;
    this.reviewRepository = reviewRepository;
    this.reviewService = reviewService;
  }

  @Transactional(readOnly = true)
  public Page<PerfumeCardProjection> getPerfumes(
      String keyword, String gender, List<String> accords, String sort, int page, int size) {
    PageRequest pageable = PageRequest.of(page, size);
    List<String> accordFilter = (accords == null || accords.isEmpty()) ? List.of() : accords;
    int accordCount = accordFilter.size();
    if ("rating_asc".equals(sort)) {
      return perfumeRepository.findAllByFiltersOrderByRatingAsc(
          keyword, gender, accordFilter, accordCount, pageable);
    } else {
      return perfumeRepository.findAllByFiltersOrderByRatingDesc(
          keyword, gender, accordFilter, accordCount, pageable);
    }
  }

  @Transactional(readOnly = true)
  public PerfumeListResponse getAccordPerfumes(String accordName, int page, int size) {
    PageRequest pageable = PageRequest.of(page, size);
    return new PerfumeListResponse(
        perfumeRepository.findAllByAccordNameOrderByRatioDesc(accordName, pageable));
  }

  @Transactional(readOnly = true)
  public PerfumeDetailResponse getPerfumeDetail(Long id) {
    Perfume perfume =
        perfumeRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 향수 ID입니다."));

    List<AccordDto> accords =
        perfumeAccordRepository.findByPerfumeId(perfume.getId()).stream()
            .map(a -> new AccordDto(a.getAccordName(), a.getRatio()))
            .collect(Collectors.toList());

    var notesByType =
        perfumeNoteRepository.findByPerfumeId(perfume.getId()).stream()
            .collect(
                Collectors.groupingBy(
                    PerfumeNote::getNoteType,
                    Collectors.mapping(PerfumeNote::getNoteName, Collectors.toList())));
    NoteGroupDto notes =
        new NoteGroupDto(
            notesByType.getOrDefault("top", List.of()),
            notesByType.getOrDefault("mid", List.of()),
            notesByType.getOrDefault("base", List.of()));

    long reviewCount = reviewRepository.countByPerfumeId(perfume.getId());
    Double avgSatisfaction = reviewRepository.findAvgSatisfactionByPerfumeId(perfume.getId());
    double rating = avgSatisfaction == null ? 0.0 : Math.round(avgSatisfaction * 10.0) / 10.0;

    StatsDto stats = reviewService.getReviewSummary(id);

    return new PerfumeDetailResponse(
        perfume.getId(),
        perfume.getImageUrl(),
        perfume.getBrand(),
        perfume.getName(),
        perfume.getGender().name(),
        perfume.getDescription(),
        rating,
        reviewCount,
        notes,
        accords,
        stats.getSatisfaction(),
        stats.getLongevity(),
        stats.getSeasons());
  }
}
