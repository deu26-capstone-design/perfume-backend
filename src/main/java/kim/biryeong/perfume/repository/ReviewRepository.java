package kim.biryeong.perfume.repository;

import kim.biryeong.perfume.domain.Review;
import kim.biryeong.perfume.domain.ReviewId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, ReviewId> {

    List<Review> findByPerfumeId(Long perfumeId);

    @Query(value = "SELECT r FROM Review r JOIN FETCH r.user WHERE r.perfume.id = :perfumeId ORDER BY r.createdAt DESC",
           countQuery = "SELECT COUNT(r) FROM Review r WHERE r.perfume.id = :perfumeId")
    Page<Review> findByPerfumeIdOrderByCreatedAtDesc(@Param("perfumeId") Long perfumeId, Pageable pageable);
}
