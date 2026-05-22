package kim.biryeong.perfume.perfume.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kim.biryeong.perfume.accord.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.domain.PerfumeNote;
import kim.biryeong.perfume.perfume.dto.AccordDto;
import kim.biryeong.perfume.perfume.dto.NoteGroupDto;
import kim.biryeong.perfume.perfume.dto.PerfumeCardDto;
import kim.biryeong.perfume.perfume.dto.PerfumeCardProjection;
import kim.biryeong.perfume.perfume.dto.PerfumeDetailResponse;
import kim.biryeong.perfume.perfume.dto.PerfumeListResponse;
import kim.biryeong.perfume.perfume.dto.StatsDto;
import kim.biryeong.perfume.perfume.repository.PerfumeNoteRepository;
import kim.biryeong.perfume.perfume.repository.PerfumeRepository;
import kim.biryeong.perfume.review.repository.ReviewRepository;
import kim.biryeong.perfume.review.service.ReviewService;
import kim.biryeong.perfume.wishlist.service.WishlistService;
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
  private final WishlistService wishlistService;

  public PerfumeService(
      PerfumeRepository perfumeRepository,
      PerfumeNoteRepository perfumeNoteRepository,
      PerfumeAccordRepository perfumeAccordRepository,
      ReviewRepository reviewRepository,
      @Lazy ReviewService reviewService,
      WishlistService wishlistService) {
    this.perfumeRepository = perfumeRepository;
    this.perfumeNoteRepository = perfumeNoteRepository;
    this.perfumeAccordRepository = perfumeAccordRepository;
    this.reviewRepository = reviewRepository;
    this.reviewService = reviewService;
    this.wishlistService = wishlistService;
  }

  /**
   * 향수 목록을 필터/정렬 조건에 따라 페이징하여 반환한다.
   *
   * <p>accords 필터는 AND 조건이다. 로그인 사용자의 경우 위시리스트 여부를 함께 반환한다.
   *
   * @param keyword 검색어 (향수명 또는 브랜드명). null이면 전체 조회
   * @param gender 성별 필터 ("M", "W", "U"). null이면 전체 조회
   * @param accords 향 계열 필터 목록 (AND 조건). null 또는 빈 목록이면 전체 조회
   * @param sort 정렬 기준 ("rating_asc" 외에는 내림차순)
   * @param page 페이지 번호 (0-based)
   * @param size 페이지당 항목 수
   * @param userId 로그인 사용자 ID. 비로그인 시 null
   * @return 향수 카드 목록과 페이징 메타데이터
   */
  @Transactional(readOnly = true)
  public PerfumeListResponse getPerfumes(
      String keyword,
      String gender,
      List<String> accords,
      String sort,
      int page,
      int size,
      Integer userId) {
    PageRequest pageable = PageRequest.of(page, size);
    List<String> accordFilter = (accords == null || accords.isEmpty()) ? List.of() : accords;
    int accordCount = accordFilter.size();

    Page<PerfumeCardProjection> projectionPage;
    if ("rating_asc".equals(sort)) {
      projectionPage =
          perfumeRepository.findAllByFiltersOrderByRatingAsc(
              keyword, gender, accordFilter, accordCount, pageable);
    } else {
      projectionPage =
          perfumeRepository.findAllByFiltersOrderByRatingDesc(
              keyword, gender, accordFilter, accordCount, pageable);
    }

    List<Long> perfumeIds =
        projectionPage.getContent().stream()
            .map(PerfumeCardProjection::getId)
            .collect(Collectors.toList());

    Set<Long> wishlistedIds = wishlistService.findWishlistedPerfumeIds(userId, perfumeIds);

    List<PerfumeCardDto> cards =
        projectionPage.getContent().stream()
            .map(p -> new PerfumeCardDto(p, wishlistedIds.contains(p.getId())))
            .collect(Collectors.toList());

    return new PerfumeListResponse(
        cards,
        projectionPage.getNumber(),
        projectionPage.getSize(),
        projectionPage.hasNext(),
        projectionPage.getTotalElements(),
        projectionPage.getTotalPages());
  }

  /**
   * 특정 향 계열에 속한 향수 목록을 향 계열 비율 내림차순으로 페이징하여 반환한다.
   *
   * @param accordName 향 계열 이름
   * @param page 페이지 번호 (0-based)
   * @param size 페이지당 항목 수
   * @param userId 로그인 사용자 ID. 비로그인 시 null
   * @return 향수 카드 목록과 페이징 메타데이터
   */
  @Transactional(readOnly = true)
  public PerfumeListResponse getAccordPerfumes(
      String accordName, int page, int size, Integer userId) {
    PageRequest pageable = PageRequest.of(page, size);
    Page<PerfumeCardProjection> projectionPage =
        perfumeRepository.findAllByAccordNameOrderByRatioDesc(accordName, pageable);

    List<Long> perfumeIds =
        projectionPage.getContent().stream()
            .map(PerfumeCardProjection::getId)
            .collect(Collectors.toList());

    Set<Long> wishlistedIds = wishlistService.findWishlistedPerfumeIds(userId, perfumeIds);

    List<PerfumeCardDto> cards =
        projectionPage.getContent().stream()
            .map(p -> new PerfumeCardDto(p, wishlistedIds.contains(p.getId())))
            .collect(Collectors.toList());

    return new PerfumeListResponse(
        cards,
        projectionPage.getNumber(),
        projectionPage.getSize(),
        projectionPage.hasNext(),
        projectionPage.getTotalElements(),
        projectionPage.getTotalPages());
  }

  /**
   * 향수 상세 정보를 조회한다.
   *
   * @param id 향수 ID
   * @param userId 로그인 사용자 ID. 비로그인 시 null (위시리스트 여부가 항상 false로 반환됨)
   * @return 향수 상세 정보 (향 계열, 노트, 통계, 위시리스트 여부 포함)
   */
  @Transactional(readOnly = true)
  public PerfumeDetailResponse getPerfumeDetail(Long id, Integer userId) {
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

    boolean isWishlisted =
        !wishlistService.findWishlistedPerfumeIds(userId, List.of(perfume.getId())).isEmpty();

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
        stats.getSeasons(),
        isWishlisted);
  }
}
