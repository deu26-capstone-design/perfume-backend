package kim.biryeong.perfume.repository;

import kim.biryeong.perfume.domain.Perfume;
import kim.biryeong.perfume.domain.PerfumeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfumeRepository extends JpaRepository<Perfume, PerfumeId> {}
