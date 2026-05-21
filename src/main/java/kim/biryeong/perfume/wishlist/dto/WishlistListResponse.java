package kim.biryeong.perfume.wishlist.dto;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

/** 위시리스트 목록 조회 응답. 향수 카드 목록과 페이징 메타데이터를 포함한다. */
@Getter
public class WishlistListResponse {

  /** 현재 페이지의 위시리스트 향수 카드 목록 */
  private final List<WishlistResponse> content;

  /** 현재 페이지 번호. 0부터 시작한다. */
  private final int pageNum;

  /** 요청한 한 페이지당 항목 수 */
  private final int size;

  /** 다음 페이지 존재 여부 */
  private final boolean hasNext;

  /** 위시리스트 전체 항목 수 */
  private final long totalElements;

  /** 전체 페이지 수 */
  private final int totalPages;

  public WishlistListResponse(Page<WishlistResponse> page) {
    this.content = page.getContent();
    this.pageNum = page.getNumber();
    this.size = page.getSize();
    this.hasNext = page.hasNext();
    this.totalElements = page.getTotalElements();
    this.totalPages = page.getTotalPages();
  }
}
