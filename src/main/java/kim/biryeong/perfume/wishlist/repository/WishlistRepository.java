package kim.biryeong.perfume.wishlist.repository;

import java.util.List;
import kim.biryeong.perfume.wishlist.domain.Wishlist;
import kim.biryeong.perfume.wishlist.domain.WishlistId;
import kim.biryeong.perfume.wishlist.dto.WishlistResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {

  @Query(
      "SELECT w.perfume.id AS perfumeId, w.perfume.imageUrl AS imageUrl, w.perfume.brand AS brand, w.perfume.name AS name FROM Wishlist w WHERE w.user.userId = :userId ORDER BY w.createdAt DESC, w.perfume.id DESC")
  List<WishlistResponse> findByUserId(@Param("userId") Integer userId);

  /**
   * 사용자의 위시리스트 향수 목록을 최신순으로 페이징하여 반환한다.
   *
   * @param userId 조회할 사용자 ID
   * @param pageable 페이징 정보
   * @return 위시리스트 항목 페이지
   */
  @Query(
      value =
          "SELECT w.perfume.id AS perfumeId, w.perfume.imageUrl AS imageUrl, w.perfume.brand AS brand, w.perfume.name AS name FROM Wishlist w WHERE w.user.userId = :userId ORDER BY w.createdAt DESC, w.perfume.id DESC",
      countQuery = "SELECT COUNT(w) FROM Wishlist w WHERE w.user.userId = :userId")
  Page<WishlistResponse> findPageByUserId(@Param("userId") Integer userId, Pageable pageable);

  /**
   * 향수 목록 중 사용자가 위시리스트에 추가한 향수 ID만 반환한다.
   *
   * <p>향수 카드 목록의 위시리스트 여부를 한 번의 IN 쿼리로 확인하기 위해 사용한다.
   *
   * @param userId 조회할 사용자 ID
   * @param perfumeIds 위시리스트 여부를 확인할 향수 ID 목록
   * @return 위시리스트에 등록된 향수 ID 목록
   */
  @Query(
      "SELECT w.perfume.id FROM Wishlist w WHERE w.user.userId = :userId AND w.perfume.id IN :perfumeIds")
  List<Long> findWishlistedPerfumeIds(
      @Param("userId") Integer userId, @Param("perfumeIds") List<Long> perfumeIds);
}
