package kim.biryeong.perfume.repository;

import kim.biryeong.perfume.domain.ReviewSeason;
import kim.biryeong.perfume.domain.ReviewSeasonId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewSeasonRepository extends JpaRepository<ReviewSeason, ReviewSeasonId> {

    List<ReviewSeason> findByPerfumeId(Long perfumeId);

    @Query("SELECT rs FROM ReviewSeason rs WHERE rs.perfume.id = :perfumeId AND rs.user.userId IN :userIds")
    List<ReviewSeason> findByPerfumeIdAndUserIds(@Param("perfumeId") Long perfumeId, @Param("userIds") List<Integer> userIds);
}
