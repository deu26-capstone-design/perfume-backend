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

  /**
   * 사용자가 작성한 리뷰 목록을 최신순으로 페이징하여 반환한다.
   *
   * <p>JOIN FETCH로 perfume을 함께 로딩하여 N+1 문제를 방지한다.
   *
   * @param userId 조회할 사용자 ID
   * @param pageable 페이징 정보
   * @return 리뷰 페이지
   */
  @Query(
      value =
          "SELECT r FROM Review r JOIN FETCH r.perfume WHERE r.user.userId = :userId"
              + " ORDER BY r.createdAt DESC, r.id DESC",
      countQuery = "SELECT COUNT(r) FROM Review r WHERE r.user.userId = :userId")
  Page<Review> findByUserIdOrderByCreatedAtDescIdDesc(
      @Param("userId") Integer userId, Pageable pageable);
}
