package kim.biryeong.perfume.review.repository;

import java.util.List;
import java.util.Optional;
import kim.biryeong.perfume.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.perfume.id = :perfumeId")
  List<Review> findByPerfumeId(@Param("perfumeId") Long perfumeId);

  long countByPerfumeId(Long perfumeId);

  @Query(
      "SELECT COUNT(r) > 0 FROM Review r WHERE r.perfume.id = :perfumeId AND r.user.userId = :userId")
  boolean existsByPerfumeIdAndUserId(
      @Param("perfumeId") Long perfumeId, @Param("userId") Integer userId);

  @Query("SELECT r FROM Review r WHERE r.perfume.id = :perfumeId AND r.user.userId = :userId")
  Optional<Review> findByPerfumeIdAndUserId(
      @Param("perfumeId") Long perfumeId, @Param("userId") Integer userId);

  @Query("SELECT AVG(r.satisfaction) FROM Review r WHERE r.perfume.id = :perfumeId")
  Double findAvgSatisfactionByPerfumeId(@Param("perfumeId") Long perfumeId);

  @Query(
      value =
          "SELECT r FROM Review r JOIN FETCH r.user WHERE r.perfume.id = :perfumeId"
              + " ORDER BY r.createdAt DESC, r.id DESC",
      countQuery = "SELECT COUNT(r) FROM Review r WHERE r.perfume.id = :perfumeId")
  Page<Review> findByPerfumeIdOrderByCreatedAtDescIdDesc(
      @Param("perfumeId") Long perfumeId, Pageable pageable);

  @Query(
      value =
          "SELECT r FROM Review r JOIN FETCH r.perfume WHERE r.user.userId = :userId"
              + " ORDER BY r.createdAt DESC, r.id DESC",
      countQuery = "SELECT COUNT(r) FROM Review r WHERE r.user.userId = :userId")
  Page<Review> findByUserIdOrderByCreatedAtDescIdDesc(
      @Param("userId") Integer userId, Pageable pageable);
}
