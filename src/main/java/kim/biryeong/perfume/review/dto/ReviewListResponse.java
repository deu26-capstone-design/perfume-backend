package kim.biryeong.perfume.review.dto;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

/** 리뷰 목록 조회 응답. 최신순 리뷰 카드 목록과 클라이언트 페이징 메타데이터를 포함한다. */
@Getter
public class ReviewListResponse {
  /** 현재 페이지의 리뷰 카드 목록 */
  private final List<ReviewItemDto> content;

  /** 현재 페이지 번호. 0부터 시작한다. */
  private final int pageNum;

  /** 요청한 한 페이지당 항목 수 */
  private final int size;

  /** 해당 향수의 리뷰 목록에서 다음 페이지가 존재하는지 여부 */
  private final boolean hasNext;

  /** 해당 향수에 작성된 전체 리뷰 수 */
  private final long totalElements;

  /** 해당 향수의 전체 리뷰 수와 size 기준으로 계산된 전체 페이지 수 */
  private final int totalPages;

  /** Spring Data {@link Page} 결과를 공개 응답 계약으로 변환한다. */
  public ReviewListResponse(Page<ReviewItemDto> page) {
    this.content = page.getContent();
    this.pageNum = page.getNumber();
    this.size = page.getSize();
    this.hasNext = page.hasNext();
    this.totalElements = page.getTotalElements();
    this.totalPages = page.getTotalPages();
  }
}
