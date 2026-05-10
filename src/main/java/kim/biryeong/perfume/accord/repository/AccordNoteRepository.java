package kim.biryeong.perfume.accord.repository;

import java.util.List;
import kim.biryeong.perfume.accord.domain.AccordNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccordNoteRepository extends JpaRepository<AccordNote, Long> {

  List<AccordNote> findByAccordName(String accordName);
}
