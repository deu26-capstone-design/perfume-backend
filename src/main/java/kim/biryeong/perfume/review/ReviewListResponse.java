package kim.biryeong.perfume.review;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class ReviewListResponse {
    private final List<ReviewItemDto> content;
    private final int number;
    private final int size;

    public ReviewListResponse(Page<ReviewItemDto> page) {
        this.content = page.getContent();
        this.number = page.getNumber();
        this.size = page.getSize();
    }
}
