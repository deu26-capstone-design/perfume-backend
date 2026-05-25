package kim.biryeong.perfume.review.repository;

import java.util.List;
import kim.biryeong.perfume.review.domain.ReviewScent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewScentRepository extends JpaRepository<ReviewScent, Long> {

  @Query("SELECT rs FROM ReviewScent rs JOIN FETCH rs.review r WHERE rs.review.id IN :reviewIds")
  List<ReviewScent> findByReviewIds(@Param("reviewIds") List<Long> reviewIds);

  @Query("SELECT rs FROM ReviewScent rs JOIN rs.review r JOIN r.user u WHERE u.userId = :userId")
  List<ReviewScent> findAllByReviewUserId(@Param("userId") Integer userId);
}
