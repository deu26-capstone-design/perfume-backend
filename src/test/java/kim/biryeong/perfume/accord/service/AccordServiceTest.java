package kim.biryeong.perfume.accord.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kim.biryeong.perfume.accord.domain.Accord;
import kim.biryeong.perfume.accord.repository.AccordNoteRepository;
import kim.biryeong.perfume.accord.repository.AccordRepository;
import kim.biryeong.perfume.accord.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.perfume.service.PerfumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class AccordServiceTest {

  private PerfumeAccordRepository perfumeAccordRepository;
  private AccordRepository accordRepository;
  private AccordNoteRepository accordNoteRepository;
  private PerfumeService perfumeService;
  private AccordService accordService;

  @BeforeEach
  void setUp() {
    perfumeAccordRepository = mock(PerfumeAccordRepository.class);
    accordRepository = mock(AccordRepository.class);
    accordNoteRepository = mock(AccordNoteRepository.class);
    perfumeService = mock(PerfumeService.class);
    accordService =
        new AccordService(
            perfumeAccordRepository, accordRepository, accordNoteRepository, perfumeService);
  }

  @Test
  void getAccordNotesUsesAccordNameForLookup() {
    when(accordRepository.findById(1L)).thenReturn(Optional.of(accord("Citrus")));
    when(accordNoteRepository.findByAccordNameOrderByNoteNameAsc("Citrus", PageRequest.of(0, 30)))
        .thenReturn(Page.empty(PageRequest.of(0, 30)));

    accordService.getAccordNotes(1L, 0, 30);

    verify(accordNoteRepository)
        .findByAccordNameOrderByNoteNameAsc("Citrus", PageRequest.of(0, 30));
  }

  @Test
  void getAccordNotesRejectsUnknownAccordId() {
    when(accordRepository.findById(999L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> accordService.getAccordNotes(999L, 0, 30));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("존재하지 않는 향 계열입니다.", exception.getReason());
  }

  @Test
  void getAccordPerfumesUsesAccordNameForLookup() {
    when(accordRepository.findById(2L)).thenReturn(Optional.of(accord("Woody")));

    accordService.getAccordPerfumes(2L, 1, 20, null);

    verify(perfumeService).getAccordPerfumes("Woody", 1, 20, null);
  }

  private static Accord accord(String name) {
    return new Accord(1L, name, "description", "https://example.com/image.jpg");
  }
}
