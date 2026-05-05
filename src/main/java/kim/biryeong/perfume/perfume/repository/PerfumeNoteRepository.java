package kim.biryeong.perfume.perfume.repository;

import java.util.List;
import kim.biryeong.perfume.perfume.domain.PerfumeNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfumeNoteRepository extends JpaRepository<PerfumeNote, Long> {

  List<PerfumeNote> findByPerfumeId(Long perfumeId);
}
