package kim.biryeong.perfume.repository;

import kim.biryeong.perfume.domain.PerfumeNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerfumeNoteRepository extends JpaRepository<PerfumeNote, Long> {

    List<PerfumeNote> findByPerfumeId(Long perfumeId);
}
