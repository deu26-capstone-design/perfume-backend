package kim.biryeong.perfume.repository;

import kim.biryeong.perfume.domain.PerfumeNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfumeNoteRepository extends JpaRepository<PerfumeNote, Long> {}
