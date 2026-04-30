package kim.biryeong.perfume.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import kim.biryeong.perfume.domain.Perfume;
import kim.biryeong.perfume.domain.PerfumeAccord;
import kim.biryeong.perfume.domain.PerfumeNote;
import kim.biryeong.perfume.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.repository.PerfumeNoteRepository;
import kim.biryeong.perfume.repository.PerfumeRepository;
import java.util.List;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;

class DataInitializerTest {

    private PerfumeRepository perfumeRepository;
    private PerfumeAccordRepository accordRepository;
    private PerfumeNoteRepository noteRepository;
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        perfumeRepository = mock(PerfumeRepository.class);
        accordRepository = mock(PerfumeAccordRepository.class);
        noteRepository = mock(PerfumeNoteRepository.class);
        dataInitializer = new DataInitializer(perfumeRepository, accordRepository, noteRepository);
    }

    @Test
    void runDoesNotInitializeDataWithoutOption() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);

        dataInitializer.run(args);

        verify(args).containsOption("initializeData");
        verifyNoInteractions(perfumeRepository, accordRepository, noteRepository);
    }

    @Test
    void runChecksExistingDataWithInitializeDataOption() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.containsOption("initializeData")).thenReturn(true);
        when(perfumeRepository.count()).thenReturn(1L);

        dataInitializer.run(args);

        verify(args).containsOption("initializeData");
        verify(perfumeRepository).count();
        verifyNoMoreInteractions(perfumeRepository);
        verifyNoInteractions(accordRepository, noteRepository);
    }

    @Test
    void runInitializesDataWithImageIds() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.containsOption("initializeData")).thenReturn(true);
        when(perfumeRepository.count()).thenReturn(0L);
        ArgumentCaptor<Iterable<Perfume>> perfumeCaptor = ArgumentCaptor.captor();
        ArgumentCaptor<Iterable<PerfumeAccord>> accordCaptor = ArgumentCaptor.captor();
        ArgumentCaptor<Iterable<PerfumeNote>> noteCaptor = ArgumentCaptor.captor();

        dataInitializer.run(args);

        verify(perfumeRepository).saveAll(perfumeCaptor.capture());
        verify(accordRepository).saveAll(accordCaptor.capture());
        verify(noteRepository).saveAll(noteCaptor.capture());

        List<Perfume> perfumes = toList(perfumeCaptor.getValue());
        List<PerfumeAccord> accords = toList(accordCaptor.getValue());
        List<PerfumeNote> notes = toList(noteCaptor.getValue());

        assertEquals(877, perfumes.size());
        assertEquals(5430, accords.size());
        assertEquals(8438, notes.size());
        assertEquals(877, perfumes.stream().map(Perfume::getId).distinct().count());
        assertEquals(10806L, perfumes.getFirst().getId());
        assertTrue(perfumes.stream().anyMatch(perfume -> perfume.getId().equals(35244L)
                && perfume.getName().equals("Skin")
                && perfume.getBrand().equals("Clean")));
        assertTrue(perfumes.stream().anyMatch(perfume -> perfume.getId().equals(16603L)
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
