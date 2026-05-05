package kim.biryeong.perfume.perfume;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfumeNoteRepository extends JpaRepository<PerfumeNote, Long> {

  List<PerfumeNote> findByPerfumeId(Long perfumeId);
}
