package kim.biryeong.perfume.repository;

import kim.biryeong.perfume.domain.Wishlist;
import kim.biryeong.perfume.domain.WishlistId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {
}
