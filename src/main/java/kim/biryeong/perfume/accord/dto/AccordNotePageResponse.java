package kim.biryeong.perfume.accord.dto;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

/** 향 계열 노트 목록 페이징 응답. */
@Getter
public class AccordNotePageResponse {

  /** 현재 페이지의 노트 목록. 각 항목은 name, imageUrl을 포함한다. */
  private final List<AccordNoteDto> content;

  /** 현재 페이지 번호. 0부터 시작한다. */
  private final int pageNum;

  /** 요청한 한 페이지당 항목 수 */
  private final int size;

  /** 다음 페이지 존재 여부 */
  private final boolean hasNext;

  /** 조건에 맞는 전체 노트 수 */
  private final long totalElements;

  /** 전체 페이지 수 */
  private final int totalPages;

  /** Spring Data {@link Page} 결과를 공개 응답 계약으로 변환한다. */
  public AccordNotePageResponse(Page<AccordNoteDto> page) {
    this.content = page.getContent();
    this.pageNum = page.getNumber();
    this.size = page.getSize();
    this.hasNext = page.hasNext();
    this.totalElements = page.getTotalElements();
    this.totalPages = page.getTotalPages();
  }
}
