package kim.biryeong.perfume.layering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kim.biryeong.perfume.accord.domain.PerfumeAccord;
import kim.biryeong.perfume.accord.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.layering.model.AccordWeight;
import kim.biryeong.perfume.layering.model.LayeringPerfumeProfile;
import kim.biryeong.perfume.layering.model.NoteProfile;
import kim.biryeong.perfume.layering.model.RoleVector;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.domain.PerfumeNote;
import kim.biryeong.perfume.perfume.repository.PerfumeNoteRepository;
import kim.biryeong.perfume.perfume.repository.PerfumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LayeringPerfumeQueryRepository {

  private final PerfumeRepository perfumeRepository;
  private final PerfumeAccordRepository perfumeAccordRepository;
  private final PerfumeNoteRepository perfumeNoteRepository;

  public List<LayeringPerfumeProfile> findProfiles(List<Long> perfumeIds) {
    Map<Long, Perfume> perfumes =
        perfumeRepository.findAllById(perfumeIds).stream()
            .collect(Collectors.toMap(Perfume::getId, perfume -> perfume));
    Map<Long, List<PerfumeAccord>> accordsByPerfume =
        perfumeAccordRepository.findByPerfumeIdIn(perfumeIds).stream()
            .collect(Collectors.groupingBy(accord -> accord.getPerfume().getId()));
    Map<Long, List<PerfumeNote>> notesByPerfume =
        perfumeNoteRepository.findByPerfumeIdIn(perfumeIds).stream()
            .collect(Collectors.groupingBy(note -> note.getPerfume().getId()));

    List<LayeringPerfumeProfile> profiles = new ArrayList<>();
    for (Long perfumeId : perfumeIds) {
      Perfume perfume = perfumes.get(perfumeId);
      if (perfume == null) {
        continue;
      }
      List<AccordWeight> accordWeights =
          toAccordWeights(accordsByPerfume.getOrDefault(perfumeId, List.of()));
      NoteProfile notes = toNoteProfile(notesByPerfume.getOrDefault(perfumeId, List.of()));
      profiles.add(
          new LayeringPerfumeProfile(
              perfume.getId(),
              perfume.getBrand(),
              perfume.getName(),
              accordWeights,
              notes,
              RoleVector.from(toWeightMap(accordWeights))));
    }
    return profiles;
  }

  private static List<AccordWeight> toAccordWeights(Collection<PerfumeAccord> accords) {
    int ratioSum =
        accords.stream()
            .map(PerfumeAccord::getRatio)
            .filter(ratio -> ratio != null)
            .mapToInt(Integer::intValue)
            .sum();
    if (ratioSum <= 0) {
      return List.of();
    }
    return accords.stream()
        .filter(accord -> accord.getRatio() != null && accord.getRatio() > 0)
        .map(
            accord ->
                new AccordWeight(
                    AccordNameNormalizer.normalize(accord.getAccordName()),
                    accord.getRatio(),
                    accord.getRatio() / (double) ratioSum))
        .sorted(
            Comparator.comparingInt(AccordWeight::ratio)
                .reversed()
                .thenComparing(AccordWeight::name))
        .toList();
  }

  private static Map<String, Double> toWeightMap(List<AccordWeight> accords) {
    Map<String, Double> weights = new LinkedHashMap<>();
    for (AccordWeight accord : accords) {
      weights.put(accord.name(), accord.normalizedWeight());
    }
    return weights;
  }

  private static NoteProfile toNoteProfile(Collection<PerfumeNote> notes) {
    Map<String, List<String>> notesByType =
        notes.stream()
            .collect(
                Collectors.groupingBy(
                    PerfumeNote::getNoteType,
                    Collectors.mapping(PerfumeNote::getNoteName, Collectors.toList())));
    return new NoteProfile(
        notesByType.getOrDefault("top", List.of()),
        notesByType.getOrDefault("mid", List.of()),
        notesByType.getOrDefault("base", List.of()));
  }
}
