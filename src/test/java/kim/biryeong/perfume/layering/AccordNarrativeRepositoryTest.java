package kim.biryeong.perfume.layering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kim.biryeong.perfume.layering.model.AccordNarrative;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccordNarrativeRepositoryTest {

  private AccordNarrativeRepository repository;

  @BeforeEach
  void setUp() {
    repository = new AccordNarrativeRepository();
    repository.load();
  }

  @Test
  void loadsNarrativesForAllSupportedAccords() {
    assertThat(repository.size()).isEqualTo(12);

    AccordNarrative citrus = repository.findByAccord("Citrus");

    assertThat(citrus.displayNameKo()).isEqualTo("시트러스");
    assertThat(citrus.seasonTags()).contains("spring", "summer");
    assertThat(citrus.occasionTags()).contains("daily");
    assertThat(citrus.representativeNotes()).contains("bergamot");
  }

  @Test
  void rejectsUnknownAccordNarrativeLookup() {
    assertThrows(IllegalArgumentException.class, () -> repository.findByAccord("Amber"));
  }
}
