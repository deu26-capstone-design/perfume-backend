package kim.biryeong.perfume.repository;

import kim.biryeong.perfume.domain.ReviewScent;
import kim.biryeong.perfume.domain.ReviewScentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewScentRepository extends JpaRepository<ReviewScent, ReviewScentId> {

    @Query("SELECT rs FROM ReviewScent rs WHERE rs.perfume.id = :perfumeId AND rs.user.userId IN :userIds")
    List<ReviewScent> findByPerfumeIdAndUserIds(@Param("perfumeId") Long perfumeId, @Param("userIds") List<Integer> userIds);
}
