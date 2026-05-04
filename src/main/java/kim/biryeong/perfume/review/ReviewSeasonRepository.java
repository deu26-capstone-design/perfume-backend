package kim.biryeong.perfume.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewSeasonRepository extends JpaRepository<ReviewSeason, ReviewSeasonId> {

    @Query("SELECT rs FROM ReviewSeason rs JOIN FETCH rs.review r JOIN FETCH r.user WHERE r.perfume.id = :perfumeId")
    List<ReviewSeason> findByPerfumeId(@Param("perfumeId") Long perfumeId);

    @Query("SELECT rs FROM ReviewSeason rs JOIN FETCH rs.review r WHERE rs.review.id IN :reviewIds")
    List<ReviewSeason> findByReviewIds(@Param("reviewIds") List<Long> reviewIds);
}
