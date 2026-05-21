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
      "SELECT w.perfume.id AS perfumeId, w.perfume.imageUrl AS imageUrl, w.perfume.brand AS brand, w.perfume.name AS name FROM Wishlist w WHERE w.user.userId = :userId ORDER BY w.createdAt DESC")
  List<WishlistResponse> findByUserId(@Param("userId") Integer userId);

  @Query(
      value =
          "SELECT w.perfume.id AS perfumeId, w.perfume.imageUrl AS imageUrl, w.perfume.brand AS brand, w.perfume.name AS name FROM Wishlist w WHERE w.user.userId = :userId ORDER BY w.createdAt DESC",
      countQuery = "SELECT COUNT(w) FROM Wishlist w WHERE w.user.userId = :userId")
  Page<WishlistResponse> findPageByUserId(@Param("userId") Integer userId, Pageable pageable);

  @Query(
      "SELECT w.perfume.id FROM Wishlist w WHERE w.user.userId = :userId AND w.perfume.id IN :perfumeIds")
  List<Long> findWishlistedPerfumeIds(
      @Param("userId") Integer userId, @Param("perfumeIds") List<Long> perfumeIds);
}
