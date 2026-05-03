package kim.biryeong.perfume.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, ReviewId> {

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.perfume.id = :perfumeId")
    List<Review> findByPerfumeId(@Param("perfumeId") Long perfumeId);

    long countByPerfumeId(Long perfumeId);

    @Query("SELECT AVG(r.satisfaction) FROM Review r WHERE r.perfume.id = :perfumeId")
    Double findAvgSatisfactionByPerfumeId(@Param("perfumeId") Long perfumeId);

    @Query(value = "SELECT r FROM Review r JOIN FETCH r.user WHERE r.perfume.id = :perfumeId ORDER BY r.createdAt DESC",
           countQuery = "SELECT COUNT(r) FROM Review r WHERE r.perfume.id = :perfumeId")
    Page<Review> findByPerfumeIdOrderByCreatedAtDesc(@Param("perfumeId") Long perfumeId, Pageable pageable);
}
