package kim.biryeong.perfume.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import kim.biryeong.perfume.accord.domain.Accord;
import kim.biryeong.perfume.accord.domain.AccordNote;
import kim.biryeong.perfume.accord.domain.PerfumeAccord;
import kim.biryeong.perfume.accord.repository.AccordNoteRepository;
import kim.biryeong.perfume.accord.repository.AccordRepository;
import kim.biryeong.perfume.accord.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.domain.PerfumeNote;
import kim.biryeong.perfume.perfume.repository.PerfumeNoteRepository;
import kim.biryeong.perfume.perfume.repository.PerfumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;

class DataInitializerTest {

  private PerfumeRepository perfumeRepository;
  private PerfumeAccordRepository perfumeAccordRepository;
  private PerfumeNoteRepository noteRepository;
  private AccordRepository accordRepository;
  private AccordNoteRepository accordNoteRepository;
  private DataInitializer dataInitializer;

  @BeforeEach
  void setUp() {
    perfumeRepository = mock(PerfumeRepository.class);
    perfumeAccordRepository = mock(PerfumeAccordRepository.class);
    noteRepository = mock(PerfumeNoteRepository.class);
    accordRepository = mock(AccordRepository.class);
    accordNoteRepository = mock(AccordNoteRepository.class);
    dataInitializer =
        new DataInitializer(
            perfumeRepository,
            perfumeAccordRepository,
            noteRepository,
            accordRepository,
            accordNoteRepository);
  }

  @Test
  void runDoesNotInitializeDataWithoutOption() throws Exception {
    ApplicationArguments args = mock(ApplicationArguments.class);

    dataInitializer.run(args);

    verify(args).containsOption("initializeData");
    verifyNoInteractions(
        perfumeRepository,
        perfumeAccordRepository,
        noteRepository,
        accordRepository,
        accordNoteRepository);
  }

  @Test
  void runInitializesAccordReferenceDataEvenWhenPerfumesAlreadyExist() throws Exception {
    ApplicationArguments args = mock(ApplicationArguments.class);
    when(args.containsOption("initializeData")).thenReturn(true);
    when(perfumeRepository.count()).thenReturn(1L);
    ArgumentCaptor<Iterable<Accord>> accordCaptor = ArgumentCaptor.captor();
    ArgumentCaptor<Iterable<AccordNote>> accordNoteCaptor = ArgumentCaptor.captor();

    dataInitializer.run(args);

    verify(args).containsOption("initializeData");
    verify(accordRepository).count();
    verify(accordRepository).saveAll(accordCaptor.capture());
    verify(accordNoteRepository).count();
    verify(accordNoteRepository).saveAll(accordNoteCaptor.capture());
    verify(perfumeRepository).count();
    verifyNoMoreInteractions(perfumeRepository);
    verifyNoInteractions(perfumeAccordRepository, noteRepository);

    List<Accord> accords = toList(accordCaptor.getValue());
    List<AccordNote> accordNotes = toList(accordNoteCaptor.getValue());

    assertEquals(12, accords.size());
    assertEquals(385, accordNotes.size());
    assertTrue(accords.stream().anyMatch(accord -> accord.getName().equals("Citrus")));
    assertTrue(accordNotes.stream().allMatch(note -> note.getAccordName() != null));
  }

  @Test
  void runRejectsPartiallyLoadedAccordReferenceData() throws Exception {
    ApplicationArguments args = mock(ApplicationArguments.class);
    when(args.containsOption("initializeData")).thenReturn(true);
    when(accordRepository.count()).thenReturn(1L);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> dataInitializer.run(args));

    assertEquals("accords has 1 rows, expected either 0 or 12", exception.getMessage());
  }

  @Test
  void runInitializesDataWithImageIds() throws Exception {
    ApplicationArguments args = mock(ApplicationArguments.class);
    when(args.containsOption("initializeData")).thenReturn(true);
    when(perfumeRepository.count()).thenReturn(0L);
    ArgumentCaptor<Iterable<Perfume>> perfumeCaptor = ArgumentCaptor.captor();
    ArgumentCaptor<Iterable<PerfumeAccord>> perfumeAccordCaptor = ArgumentCaptor.captor();
    ArgumentCaptor<Iterable<PerfumeNote>> noteCaptor = ArgumentCaptor.captor();
    ArgumentCaptor<Iterable<Accord>> accordCaptor = ArgumentCaptor.captor();
    ArgumentCaptor<Iterable<AccordNote>> accordNoteCaptor = ArgumentCaptor.captor();

    dataInitializer.run(args);

    verify(accordRepository).saveAll(accordCaptor.capture());
    verify(accordNoteRepository).saveAll(accordNoteCaptor.capture());
    verify(perfumeRepository).saveAll(perfumeCaptor.capture());
    verify(perfumeAccordRepository).saveAll(perfumeAccordCaptor.capture());
    verify(noteRepository).saveAll(noteCaptor.capture());

    List<Accord> accordDetails = toList(accordCaptor.getValue());
    List<AccordNote> accordNotes = toList(accordNoteCaptor.getValue());
    List<Perfume> perfumes = toList(perfumeCaptor.getValue());
    List<PerfumeAccord> accords = toList(perfumeAccordCaptor.getValue());
    List<PerfumeNote> notes = toList(noteCaptor.getValue());

    assertEquals(12, accordDetails.size());
    assertEquals(385, accordNotes.size());
    assertEquals(12, accordDetails.stream().map(Accord::getId).distinct().count());
    assertEquals(12, accordDetails.stream().map(Accord::getName).distinct().count());
    assertTrue(
        accordDetails.stream()
            .anyMatch(accord -> accord.getId().equals(1L) && accord.getName().equals("Aromatic")));
    Set<String> accordNames =
        accordDetails.stream().map(Accord::getName).collect(Collectors.toSet());
    assertTrue(accordNotes.stream().allMatch(note -> accordNames.contains(note.getAccordName())));
    assertEquals(
        accordNotes.size(),
        accordNotes.stream()
            .map(note -> note.getAccordName() + "\n" + note.getNoteName())
            .distinct()
            .count());
    assertEquals(874, perfumes.size());
    assertEquals(5164, accords.size());
    assertEquals(8404, notes.size());
    assertEquals(874, perfumes.stream().map(Perfume::getId).distinct().count());
    assertEquals(10806L, perfumes.getFirst().getId());
    assertTrue(
        perfumes.stream()
            .anyMatch(
                perfume ->
                    perfume.getId().equals(35244L)
                        && perfume.getName().equals("Skin")
                        && perfume.getBrand().equals("Clean")));
    assertTrue(accords.stream().allMatch(accord -> accord.getPerfume() != null));
    assertTrue(notes.stream().allMatch(note -> note.getPerfume() != null));
    assertNotNull(accords.getFirst().getPerfume().getId());
    assertNotNull(notes.getFirst().getPerfume().getId());
  }

  private static <T> List<T> toList(Iterable<T> iterable) {
    return StreamSupport.stream(iterable.spliterator(), false).toList();
  }
}
