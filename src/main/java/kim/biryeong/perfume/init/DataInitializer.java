package kim.biryeong.perfume.init;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import kim.biryeong.perfume.domain.Perfume;
import kim.biryeong.perfume.domain.PerfumeAccord;
import kim.biryeong.perfume.domain.PerfumeNote;
import kim.biryeong.perfume.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.repository.PerfumeNoteRepository;
import kim.biryeong.perfume.repository.PerfumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final PerfumeRepository perfumeRepository;
    private final PerfumeAccordRepository accordRepository;
    private final PerfumeNoteRepository noteRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (perfumeRepository.count() > 0) return;
        loadPerfumes();
        loadAccords();
        loadNotes();
    }

    private void loadPerfumes() throws Exception {
        List<Perfume> list = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/perfumes.csv").getInputStream(), StandardCharsets.UTF_8))
                .withSkipLines(1).build()) {
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length < 4) continue;
                String description = row.length > 4 ? row[4].trim() : "";
                list.add(new Perfume(row[0].trim(), row[1].trim(), row[2].trim(), row[3].trim(), description));
            }
        }
        perfumeRepository.saveAll(list);
    }

    private void loadAccords() throws Exception {
        List<PerfumeAccord> list = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/perfume_accords.csv").getInputStream(), StandardCharsets.UTF_8))
                .withSkipLines(1).build()) {
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length < 4) continue;
                try {
                    list.add(new PerfumeAccord(null, row[0].trim(), row[1].trim(), row[2].trim(), Integer.parseInt(row[3].trim())));
                } catch (NumberFormatException ignored) {}
            }
        }
        accordRepository.saveAll(list);
    }

    private void loadNotes() throws Exception {
        List<PerfumeNote> list = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/perfume_notes.csv").getInputStream(), StandardCharsets.UTF_8))
                .withSkipLines(1).build()) {
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length < 4) continue;
                list.add(new PerfumeNote(null, row[0].trim(), row[1].trim(), row[2].trim(), row[3].trim()));
            }
        }
        noteRepository.saveAll(list);
    }
}
