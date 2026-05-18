package kim.biryeong.perfume.accord.repository;

import static org.assertj.core.api.Assertions.assertThat;

import kim.biryeong.perfume.accord.domain.Accord;
import kim.biryeong.perfume.accord.domain.AccordNote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class AccordNoteRepositoryTest {

  @Autowired private AccordRepository accordRepository;

  @Autowired private AccordNoteRepository accordNoteRepository;

  @Test
  void findByAccordNameOrderByNoteNameAscFiltersAndSortsByNoteName() {
    accordRepository.save(
        new Accord(1L, "Citrus", "description", "https://example.com/citrus.jpg"));
    accordRepository.save(new Accord(2L, "Woody", "description", "https://example.com/woody.jpg"));
    accordNoteRepository.save(
        new AccordNote(null, "Citrus", "레몬", "https://example.com/lemon.jpg"));
    accordNoteRepository.save(
        new AccordNote(null, "Citrus", "베르가못", "https://example.com/bergamot.jpg"));
    accordNoteRepository.save(
        new AccordNote(null, "Woody", "샌달우드", "https://example.com/sandal.jpg"));

    Page<AccordNote> page =
        accordNoteRepository.findByAccordNameOrderByNoteNameAsc("Citrus", PageRequest.of(0, 1));

    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(page.hasNext()).isTrue();
    assertThat(page.getContent()).extracting(AccordNote::getNoteName).containsExactly("레몬");
  }
}
