package kim.biryeong.perfume.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewSeasonRepository extends JpaRepository<ReviewSeason, ReviewSeasonId> {

    @Query("SELECT rs FROM ReviewSeason rs JOIN FETCH rs.user WHERE rs.perfume.id = :perfumeId")
    List<ReviewSeason> findByPerfumeId(@Param("perfumeId") Long perfumeId);

    @Query("SELECT rs FROM ReviewSeason rs JOIN FETCH rs.user WHERE rs.perfume.id = :perfumeId AND rs.user.userId IN :userIds")
    List<ReviewSeason> findByPerfumeIdAndUserIds(@Param("perfumeId") Long perfumeId, @Param("userIds") List<Integer> userIds);
}
