package kim.biryeong.perfume.layering;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kim.biryeong.perfume.accord.domain.PerfumeAccord;
import kim.biryeong.perfume.accord.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.perfume.domain.Gender;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.domain.PerfumeNote;
import kim.biryeong.perfume.perfume.repository.PerfumeNoteRepository;
import kim.biryeong.perfume.perfume.repository.PerfumeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class LayeringRepositoryTest {

  @Autowired private PerfumeRepository perfumeRepository;

  @Autowired private PerfumeAccordRepository perfumeAccordRepository;

  @Autowired private PerfumeNoteRepository perfumeNoteRepository;

  @Test
  void findByPerfumeIdInReturnsAccordsAndNotesForInputIds() {
    Perfume first = perfumeRepository.save(perfume(1L, "First"));
    Perfume second = perfumeRepository.save(perfume(2L, "Second"));
    Perfume third = perfumeRepository.save(perfume(3L, "Third"));
    perfumeAccordRepository.save(new PerfumeAccord(null, first, "Citrus", 100));
    perfumeAccordRepository.save(new PerfumeAccord(null, second, "Floral", 100));
    perfumeAccordRepository.save(new PerfumeAccord(null, third, "Woody", 100));
    perfumeNoteRepository.save(new PerfumeNote(null, first, "bergamot", "top"));
    perfumeNoteRepository.save(new PerfumeNote(null, second, "rose", "mid"));
    perfumeNoteRepository.save(new PerfumeNote(null, third, "cedarwood", "base"));

    List<PerfumeAccord> accords = perfumeAccordRepository.findByPerfumeIdIn(List.of(1L, 2L));
    List<PerfumeNote> notes = perfumeNoteRepository.findByPerfumeIdIn(List.of(1L, 2L));

    assertThat(accords).extracting(accord -> accord.getPerfume().getId()).containsOnly(1L, 2L);
    assertThat(notes).extracting(note -> note.getPerfume().getId()).containsOnly(1L, 2L);
  }

  private static Perfume perfume(Long id, String name) {
    return new Perfume(
        id, name, "Test Brand", Gender.U, "https://example.com/perfume.jpg", "description");
  }
}
