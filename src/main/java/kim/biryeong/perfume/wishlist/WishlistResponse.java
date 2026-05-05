package kim.biryeong.perfume.wishlist;

/** 위시리스트 목록에서 향수 카드에 표시할 공개 필드 projection이다. */
public interface WishlistResponse {
  /** 향수 ID */
  Long getPerfumeId();

  /** 향수 이미지 URL. 등록된 이미지가 없으면 null일 수 있다. */
  String getImageUrl();

  /** 브랜드명 */
  String getBrand();

  /** 향수명 */
  String getName();
}
