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
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String INITIALIZE_DATA_OPTION = "initializeData";

    private final PerfumeRepository perfumeRepository;
    private final PerfumeAccordRepository accordRepository;
    private final PerfumeNoteRepository noteRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (!args.containsOption(INITIALIZE_DATA_OPTION)) {
            return;
        }
        if (perfumeRepository.count() > 0) return;
        Map<Long, Perfume> perfumes = loadPerfumes();
        loadAccords(perfumes);
        loadNotes(perfumes);
    }

    private Map<Long, Perfume> loadPerfumes() throws Exception {
        List<Perfume> list = new ArrayList<>();
        Map<Long, Perfume> perfumes = new LinkedHashMap<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/perfumes.csv").getInputStream(), StandardCharsets.UTF_8))
                .withSkipLines(1).build()) {
            String[] row;
            int rowNumber = 2;
            while ((row = reader.readNext()) != null) {
                requireColumnCount("data/perfumes.csv", rowNumber, row, 6);
                Long perfumeId = parseLong("data/perfumes.csv", rowNumber, "향수ID", row[0]);
                String description = row[5].trim();
                Perfume perfume = new Perfume(
                        perfumeId,
                        row[1].trim(),
                        row[2].trim(),
                        row[3].trim(),
                        row[4].trim(),
                        description);
                if (perfumes.put(perfumeId, perfume) != null) {
                    throw new IllegalStateException("Duplicate perfume id " + perfumeId + " in data/perfumes.csv");
                }
                list.add(perfume);
                rowNumber++;
            }
        }
        perfumeRepository.saveAll(list);
        return perfumes;
    }

    private void loadAccords(Map<Long, Perfume> perfumes) throws Exception {
        List<PerfumeAccord> list = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/perfume_accords.csv").getInputStream(), StandardCharsets.UTF_8))
                .withSkipLines(1).build()) {
            String[] row;
            int rowNumber = 2;
            while ((row = reader.readNext()) != null) {
                requireColumnCount("data/perfume_accords.csv", rowNumber, row, 5);
                Long perfumeId = parseLong("data/perfume_accords.csv", rowNumber, "향수ID", row[0]);
                Integer ratio = parseInteger("data/perfume_accords.csv", rowNumber, "비율", row[4]);
                list.add(new PerfumeAccord(
                        null,
                        requirePerfume(perfumes, perfumeId, "data/perfume_accords.csv", rowNumber),
                        row[3].trim(),
                        ratio));
                rowNumber++;
            }
        }
        accordRepository.saveAll(list);
    }

    private void loadNotes(Map<Long, Perfume> perfumes) throws Exception {
        List<PerfumeNote> list = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new ClassPathResource("data/perfume_notes.csv").getInputStream(), StandardCharsets.UTF_8))
                .withSkipLines(1).build()) {
            String[] row;
            int rowNumber = 2;
            while ((row = reader.readNext()) != null) {
                requireColumnCount("data/perfume_notes.csv", rowNumber, row, 5);
                Long perfumeId = parseLong("data/perfume_notes.csv", rowNumber, "향수ID", row[0]);
                list.add(new PerfumeNote(
                        null,
                        requirePerfume(perfumes, perfumeId, "data/perfume_notes.csv", rowNumber),
                        row[3].trim(),
                        row[4].trim()));
                rowNumber++;
            }
        }
        noteRepository.saveAll(list);
    }

    private static void requireColumnCount(String path, int rowNumber, String[] row, int expected) {
        if (row.length < expected) {
            throw new IllegalStateException(
                    path + " row " + rowNumber + " has " + row.length + " columns, expected at least " + expected);
        }
    }

    private static Long parseLong(String path, int rowNumber, String column, String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(path + " row " + rowNumber + " has invalid " + column + ": " + value, e);
        }
    }

    private static Integer parseInteger(String path, int rowNumber, String column, String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(path + " row " + rowNumber + " has invalid " + column + ": " + value, e);
        }
    }

    private static Perfume requirePerfume(
            Map<Long, Perfume> perfumes, Long perfumeId, String path, int rowNumber) {
        Perfume perfume = perfumes.get(perfumeId);
        if (perfume == null) {
            throw new IllegalStateException(path + " row " + rowNumber + " references unknown perfume id " + perfumeId);
        }
        return perfume;
    }
}
