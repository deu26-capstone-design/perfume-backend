package kim.biryeong.perfume.init;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kim.biryeong.perfume.accord.domain.Accord;
import kim.biryeong.perfume.accord.domain.AccordNote;
import kim.biryeong.perfume.accord.domain.PerfumeAccord;
import kim.biryeong.perfume.accord.repository.AccordNoteRepository;
import kim.biryeong.perfume.accord.repository.AccordRepository;
import kim.biryeong.perfume.accord.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.perfume.domain.Gender;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.domain.PerfumeNote;
import kim.biryeong.perfume.perfume.repository.PerfumeNoteRepository;
import kim.biryeong.perfume.perfume.repository.PerfumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

  private static final String INITIALIZE_DATA_OPTION = "initializeData";

  private final PerfumeRepository perfumeRepository;
  private final PerfumeAccordRepository perfumeAccordRepository;
  private final PerfumeNoteRepository noteRepository;
  private final AccordRepository accordRepository;
  private final AccordNoteRepository accordNoteRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) throws Exception {
    if (!args.containsOption(INITIALIZE_DATA_OPTION)) return;
    Map<String, Accord> accords = loadAccordReferenceDataIfMissing();
    if (perfumeRepository.count() > 0) return;
    Map<Long, Perfume> perfumes = loadPerfumes();
    loadPerfumeAccords(perfumes, accords.keySet());
    loadNotes(perfumes);
  }

  private Map<String, Accord> loadAccordReferenceDataIfMissing() throws Exception {
    Map<String, Accord> accords = readAccordReferenceData();
    long accordCount = accordRepository.count();
    if (accordCount == 0) {
      accordRepository.saveAll(accords.values());
    } else {
      requireExistingCount("accords", accordCount, accords.size());
    }

    List<AccordNote> accordNotes = readAccordNotes(accords.keySet());
    long accordNoteCount = accordNoteRepository.count();
    if (accordNoteCount == 0) {
      accordNoteRepository.saveAll(accordNotes);
    } else {
      requireExistingCount("accord_notes", accordNoteCount, accordNotes.size());
    }
    return accords;
  }

  private Map<String, Accord> readAccordReferenceData() throws Exception {
    Map<Long, String> ids = new LinkedHashMap<>();
    Map<String, Accord> accords = new LinkedHashMap<>();
    try (InputStreamReader isr =
            new InputStreamReader(
                new ClassPathResource("data/accord.csv").getInputStream(), StandardCharsets.UTF_8);
        CSVReader reader = new CSVReaderBuilder(isr).withSkipLines(1).build()) {
      String[] row;
      int rowNumber = 2;
      while ((row = reader.readNext()) != null) {
        requireColumnCount("data/accord.csv", rowNumber, row, 4);
        Long accordId = parseLong("data/accord.csv", rowNumber, "id", row[0]);
        String name = row[1].trim();
        if (ids.put(accordId, name) != null) {
          throw new IllegalStateException(
              "Duplicate accord id " + accordId + " in data/accord.csv");
        }
        if (accords.put(
                accordName(name, "data/accord.csv", rowNumber),
                new Accord(accordId, name, row[2].trim(), row[3].trim()))
            != null) {
          throw new IllegalStateException("Duplicate accord name " + name + " in data/accord.csv");
        }
        rowNumber++;
      }
    }
    return accords;
  }

  private List<AccordNote> readAccordNotes(Set<String> accordNames) throws Exception {
    List<AccordNote> list = new ArrayList<>();
    Set<Long> ids = new HashSet<>();
    Set<String> noteKeys = new HashSet<>();
    try (InputStreamReader isr =
            new InputStreamReader(
                new ClassPathResource("data/accord_notes.csv").getInputStream(),
                StandardCharsets.UTF_8);
        CSVReader reader = new CSVReaderBuilder(isr).withSkipLines(1).build()) {
      String[] row;
      int rowNumber = 2;
      while ((row = reader.readNext()) != null) {
        requireColumnCount("data/accord_notes.csv", rowNumber, row, 4);
        Long accordNoteId = parseLong("data/accord_notes.csv", rowNumber, "id", row[0]);
        if (!ids.add(accordNoteId)) {
          throw new IllegalStateException(
              "Duplicate accord note id " + accordNoteId + " in data/accord_notes.csv");
        }
        String accordName = accordName(row[1].trim(), "data/accord_notes.csv", rowNumber);
        String noteName = requiredText(row[2].trim(), "data/accord_notes.csv", rowNumber, "노트명");
        if (!accordNames.contains(accordName)) {
          throw new IllegalStateException(
              "data/accord_notes.csv row "
                  + rowNumber
                  + " references unknown accord "
                  + accordName);
        }
        if (!noteKeys.add(accordName + "\n" + noteName)) {
          throw new IllegalStateException(
              "Duplicate accord note " + accordName + "/" + noteName + " in data/accord_notes.csv");
        }
        list.add(new AccordNote(null, accordName, noteName, row[3].trim()));
        rowNumber++;
      }
    }
    return list;
  }

  private Map<Long, Perfume> loadPerfumes() throws Exception {
    List<Perfume> list = new ArrayList<>();
    Map<Long, Perfume> perfumes = new LinkedHashMap<>();
    try (InputStreamReader isr =
            new InputStreamReader(
                new ClassPathResource("data/perfumes.csv").getInputStream(),
                StandardCharsets.UTF_8);
        CSVReader reader = new CSVReaderBuilder(isr).withSkipLines(1).build()) {
      String[] row;
      int rowNumber = 2;
      while ((row = reader.readNext()) != null) {
        requireColumnCount("data/perfumes.csv", rowNumber, row, 6);
        Long perfumeId = parseLong("data/perfumes.csv", rowNumber, "향수ID", row[0]);
        String description = row[5].trim();
        Perfume perfume =
            new Perfume(
                perfumeId,
                row[1].trim(),
                row[2].trim(),
                Gender.valueOf(row[3].trim()),
                row[4].trim(),
                description);
        if (perfumes.put(perfumeId, perfume) != null) {
          throw new IllegalStateException(
              "Duplicate perfume id " + perfumeId + " in data/perfumes.csv");
        }
        list.add(perfume);
        rowNumber++;
      }
    }
    perfumeRepository.saveAll(list);
    return perfumes;
  }

  private void loadPerfumeAccords(Map<Long, Perfume> perfumes, Set<String> accordNames)
      throws Exception {
    List<PerfumeAccord> list = new ArrayList<>();
    try (InputStreamReader isr =
            new InputStreamReader(
                new ClassPathResource("data/perfume_accords.csv").getInputStream(),
                StandardCharsets.UTF_8);
        CSVReader reader = new CSVReaderBuilder(isr).withSkipLines(1).build()) {
      String[] row;
      int rowNumber = 2;
      while ((row = reader.readNext()) != null) {
        requireColumnCount("data/perfume_accords.csv", rowNumber, row, 5);
        Long perfumeId = parseLong("data/perfume_accords.csv", rowNumber, "향수ID", row[0]);
        Integer ratio = parseInteger("data/perfume_accords.csv", rowNumber, "비율", row[4]);
        String accordName = accordName(row[3].trim(), "data/perfume_accords.csv", rowNumber);
        if (!accordNames.contains(accordName)) {
          throw new IllegalStateException(
              "data/perfume_accords.csv row "
                  + rowNumber
                  + " references unknown accord "
                  + accordName);
        }
        list.add(
            new PerfumeAccord(
                null,
                requirePerfume(perfumes, perfumeId, "data/perfume_accords.csv", rowNumber),
                accordName,
                ratio));
        rowNumber++;
      }
    }
    perfumeAccordRepository.saveAll(list);
  }

  private void loadNotes(Map<Long, Perfume> perfumes) throws Exception {
    List<PerfumeNote> list = new ArrayList<>();
    try (InputStreamReader isr =
            new InputStreamReader(
                new ClassPathResource("data/perfume_notes.csv").getInputStream(),
                StandardCharsets.UTF_8);
        CSVReader reader = new CSVReaderBuilder(isr).withSkipLines(1).build()) {
      String[] row;
      int rowNumber = 2;
      while ((row = reader.readNext()) != null) {
        requireColumnCount("data/perfume_notes.csv", rowNumber, row, 5);
        Long perfumeId = parseLong("data/perfume_notes.csv", rowNumber, "향수ID", row[0]);
        list.add(
            new PerfumeNote(
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
          path
              + " row "
              + rowNumber
              + " has "
              + row.length
              + " columns, expected at least "
              + expected);
    }
  }

  private static Long parseLong(String path, int rowNumber, String column, String value) {
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      throw new IllegalStateException(
          path + " row " + rowNumber + " has invalid " + column + ": " + value, e);
    }
  }

  private static Integer parseInteger(String path, int rowNumber, String column, String value) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      throw new IllegalStateException(
          path + " row " + rowNumber + " has invalid " + column + ": " + value, e);
    }
  }

  private static Perfume requirePerfume(
      Map<Long, Perfume> perfumes, Long perfumeId, String path, int rowNumber) {
    Perfume perfume = perfumes.get(perfumeId);
    if (perfume == null) {
      throw new IllegalStateException(
          path + " row " + rowNumber + " references unknown perfume id " + perfumeId);
    }
    return perfume;
  }

  private static String accordName(String name, String path, int rowNumber) {
    if (name.isBlank()) {
      throw new IllegalStateException(path + " row " + rowNumber + " has blank accord name");
    }
    return name;
  }

  private static String requiredText(String value, String path, int rowNumber, String column) {
    if (value.isBlank()) {
      throw new IllegalStateException(path + " row " + rowNumber + " has blank " + column);
    }
    return value;
  }

  private static void requireExistingCount(String tableName, long actual, long expected) {
    if (actual != expected) {
      throw new IllegalStateException(
          tableName + " has " + actual + " rows, expected either 0 or " + expected);
    }
  }
}
