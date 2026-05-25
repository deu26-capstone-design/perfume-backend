package kim.biryeong.perfume.accord.repository;

import java.util.Collection;
import java.util.List;
import kim.biryeong.perfume.accord.domain.PerfumeAccord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PerfumeAccordRepository extends JpaRepository<PerfumeAccord, Long> {

  List<PerfumeAccord> findByPerfumeId(Long perfumeId);

  List<PerfumeAccord> findByPerfumeIdIn(Collection<Long> perfumeIds);

  @Query("SELECT DISTINCT pa.accordName FROM PerfumeAccord pa ORDER BY pa.accordName ASC")
  List<String> findDistinctAccordNames();
}
