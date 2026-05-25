package kim.biryeong.perfume.layering.model;

import java.util.Comparator;
import java.util.List;

public record LayeringPerfumeProfile(
    Long id,
    String brand,
    String name,
    List<AccordWeight> accords,
    NoteProfile notes,
    RoleVector roleVector) {

  public AccordWeight dominantAccord() {
    return accords.stream()
        .max(Comparator.comparingInt(AccordWeight::ratio).thenComparing(AccordWeight::name))
        .orElseThrow(() -> new IllegalStateException("Perfume profile has no accords"));
  }
}
