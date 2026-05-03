package kim.biryeong.perfume.perfume;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerfumeNoteRepository extends JpaRepository<PerfumeNote, Long> {

    List<PerfumeNote> findByPerfumeId(Long perfumeId);
}
