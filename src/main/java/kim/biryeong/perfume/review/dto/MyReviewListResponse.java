package kim.biryeong.perfume.review.dto;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

/** 내가 작성한 리뷰 목록 조회 응답. 리뷰 항목 목록과 페이징 메타데이터를 포함한다. */
@Getter
public class MyReviewListResponse {

  /** 현재 페이지의 리뷰 항목 목록 */
  private final List<MyReviewItemDto> content;

  /** 현재 페이지 번호. 0부터 시작한다. */
  private final int pageNum;

  /** 요청한 한 페이지당 항목 수 */
  private final int size;

  /** 다음 페이지 존재 여부 */
  private final boolean hasNext;

  /** 내가 작성한 전체 리뷰 수 */
  private final long totalElements;

  /** 전체 페이지 수 */
  private final int totalPages;

  public MyReviewListResponse(Page<MyReviewItemDto> page) {
    this.content = page.getContent();
    this.pageNum = page.getNumber();
    this.size = page.getSize();
    this.hasNext = page.hasNext();
    this.totalElements = page.getTotalElements();
    this.totalPages = page.getTotalPages();
  }
}
