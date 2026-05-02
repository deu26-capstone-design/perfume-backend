package kim.biryeong.perfume.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class ReviewListResponse {
    private final List<ReviewItemDto> content;
    private final int totalPages;
    private final long totalElements;
    private final int number;
    private final int size;

    public ReviewListResponse(Page<ReviewItemDto> page) {
        this.content = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.number = page.getNumber();
        this.size = page.getSize();
    }
}
