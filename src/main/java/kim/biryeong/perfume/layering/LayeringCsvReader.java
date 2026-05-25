package kim.biryeong.perfume.layering;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;

final class LayeringCsvReader {

  private LayeringCsvReader() {}

  static List<String[]> read(String path, int minColumns) {
    List<String[]> rows = new ArrayList<>();
    try (InputStreamReader isr =
            new InputStreamReader(
                new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8);
        CSVReader reader = new CSVReaderBuilder(isr).withSkipLines(1).build()) {
      String[] row;
      int rowNumber = 2;
      while ((row = reader.readNext()) != null) {
        if (row.length < minColumns) {
          throw new IllegalStateException(
              path
                  + " row "
                  + rowNumber
                  + " has "
                  + row.length
                  + " columns, expected at least "
                  + minColumns);
        }
        rows.add(row);
        rowNumber++;
      }
    } catch (Exception e) {
      throw new IllegalStateException("Failed to read " + path, e);
    }
    return rows;
  }
}
