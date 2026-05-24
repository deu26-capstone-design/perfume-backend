package kim.biryeong.perfume.perfume.dto;

import java.util.List;
import lombok.Getter;

/** 향수 목록 조회 응답. 카드 목록과 클라이언트 페이징에 필요한 메타데이터를 포함한다. */
@Getter
public class PerfumeListResponse {

  /**
   * 현재 페이지의 향수 카드 목록. 각 항목은 id, imageUrl, brand, name, gender, rating, reviewCount, isWishlisted를
   * 포함한다.
   */
  private final List<PerfumeCardDto> content;

  /** 현재 페이지 번호. 0부터 시작한다. */
  private final int pageNum;

  /** 요청한 한 페이지당 항목 수 */
  private final int size;

  /** 현재 필터/검색 조건에서 다음 페이지가 존재하는지 여부 */
  private final boolean hasNext;

  /** 현재 필터/검색 조건에 맞는 전체 향수 수 */
  private final long totalElements;

  /** 현재 필터/검색 조건과 size 기준으로 계산된 전체 페이지 수 */
  private final int totalPages;

  public PerfumeListResponse(
      List<PerfumeCardDto> content,
      int pageNum,
      int size,
      boolean hasNext,
      long totalElements,
      int totalPages) {
    this.content = content;
    this.pageNum = pageNum;
    this.size = size;
    this.hasNext = hasNext;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
  }
}
