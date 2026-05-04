package kim.biryeong.perfume.perfume;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 향수 상세 조회
 * 기본 정보, 노트(탑/미들/베이스), 어코드 목록 및 비율을 포함
 * 리뷰 통계는 GET /api/perfumes/{id}/review-summary 에서 별도로 제공(프론트 엔드에서 어떻게 할지 정해지면 수정)
 */
@Getter
@AllArgsConstructor
public class PerfumeDetailResponse {
    /** 향수 ID */
    private Long id;
    /** 향수 이미지 URL */
    private String imageUrl;
    /** 브랜드명 */
    private String brand;
    /** 향수명 */
    private String name;
    /** 성별 (W: 여성, M: 남성, U: 유니섹스) */
    private String gender;
    /** 향수 설명 */
    private String description;
    /** 평균 만족도 (1.0 ~ 5.0, 리뷰 없으면 0.0) */
    private double rating;
    /** 리뷰 수 */
    private long reviewCount;
    /** 탑/미들/베이스 노트 */
    private NoteGroupDto notes;
    /** 어코드 목록 (이름 + 비율) */
    private List<AccordDto> accords;
}