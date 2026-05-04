package kim.biryeong.perfume.perfume;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 향수 목록 조회 응답
 * 무한 스크롤 방식으로 동작
 */
@Getter
public class PerfumeListResponse {

    /** 향수 카드 목록 (id, imageUrl, brand, name, gender, rating, reviewCount)7개 필드로 구성*/
    private final List<PerfumeCardProjection> content;

    /** 페이지 시작 번호 (0부터 시작) */
    private final int page;

    /** 한 페이지당 항목 수 (환경에 맞게 30, 20, 10 등 변경 가능) */
    private final int size;

    /** 생성자 */
    public PerfumeListResponse(Page<PerfumeCardProjection> page) {
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
    }
}
