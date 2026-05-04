package kim.biryeong.perfume.wishlist;

/**
 * 위시리스트 향수 카드 응답
 */
public interface WishlistResponse {
    /** 향수 ID */
    Long getPerfumeId();
    /** 향수 이미지 URL */
    String getImageUrl();
    /** 브랜드명 */
    String getBrand();
    /** 향수명 */
    String getName();
}
