package kim.biryeong.perfume.review;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

/** 리뷰 목록 조회 응답 무한 스크롤 방식으로 동작 최신순으로 정렬 */
@Getter
public class ReviewListResponse {
  /** 리뷰 목록(프로필사진, 닉네임, 작성일자, 만족도, 지속력, 계절목록, 향 목록, 리뷰 텍스트) */
  private final List<ReviewItemDto> content;

  /** 페이지 시작 번호 (0부터 시작) */
  private final int pageNum;

  /** 한 페이지당 항목 수 (환경에 맞게 30, 20, 10 등 변경 가능) */
  private final int size;

  /** 다음 페이지 존재 여부 */
  private final boolean hasNext;

  /** 전체 항목 수 */
  private final long totalElements;

  /** 전체 페이지 수 */
  private final int totalPages;

  /** 생성자 */
  public ReviewListResponse(Page<ReviewItemDto> page) {
    this.content = page.getContent();
    this.pageNum = page.getNumber();
    this.size = page.getSize();
    this.hasNext = page.hasNext();
    this.totalElements = page.getTotalElements();
    this.totalPages = page.getTotalPages();
  }
}
