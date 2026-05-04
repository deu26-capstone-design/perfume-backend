package kim.biryeong.perfume.wishlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {

    @Query("SELECT w.perfume.id AS perfumeId, w.perfume.imageUrl AS imageUrl, w.perfume.brand AS brand, w.perfume.name AS name FROM Wishlist w WHERE w.user.userId = :userId")
    List<WishlistResponse> findByUserId(@Param("userId") Integer userId);
}
