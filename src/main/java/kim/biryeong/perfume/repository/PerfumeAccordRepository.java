package kim.biryeong.perfume.repository;

import kim.biryeong.perfume.domain.PerfumeAccord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerfumeAccordRepository extends JpaRepository<PerfumeAccord, Long> {

    List<PerfumeAccord> findByPerfumeId(Long perfumeId);
}
