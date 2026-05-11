package kim.biryeong.perfume.accord.repository;

import kim.biryeong.perfume.accord.domain.AccordNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccordNoteRepository extends JpaRepository<AccordNote, Long> {

  Page<AccordNote> findByAccordNameOrderByNoteNameAsc(String accordName, Pageable pageable);
}
