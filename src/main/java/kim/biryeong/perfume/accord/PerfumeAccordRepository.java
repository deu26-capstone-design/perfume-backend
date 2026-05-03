package kim.biryeong.perfume.accord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PerfumeAccordRepository extends JpaRepository<PerfumeAccord, Long> {

    List<PerfumeAccord> findByPerfumeId(Long perfumeId);

    @Query("SELECT DISTINCT pa.accordName FROM PerfumeAccord pa ORDER BY pa.accordName ASC")
    List<String> findDistinctAccordNames();
}
