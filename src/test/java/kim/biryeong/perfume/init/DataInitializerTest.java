package kim.biryeong.perfume.init;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import kim.biryeong.perfume.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.repository.PerfumeNoteRepository;
import kim.biryeong.perfume.repository.PerfumeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

class DataInitializerTest {

    private final PerfumeRepository perfumeRepository = mock(PerfumeRepository.class);
    private final PerfumeAccordRepository accordRepository = mock(PerfumeAccordRepository.class);
    private final PerfumeNoteRepository noteRepository = mock(PerfumeNoteRepository.class);
    private final DataInitializer dataInitializer =
            new DataInitializer(perfumeRepository, accordRepository, noteRepository);

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
}
