package kim.biryeong.perfume.perfume.repository;

import java.util.Collection;
import java.util.List;
import kim.biryeong.perfume.perfume.domain.PerfumeNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfumeNoteRepository extends JpaRepository<PerfumeNote, Long> {

  List<PerfumeNote> findByPerfumeId(Long perfumeId);

  List<PerfumeNote> findByPerfumeIdIn(Collection<Long> perfumeIds);
}
