package kim.biryeong.perfume.layering;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kim.biryeong.perfume.layering.dto.InputPerfumeResponse;
import kim.biryeong.perfume.layering.dto.LayeringAccordResponse;
import kim.biryeong.perfume.layering.dto.LayeringRecommendationRequest;
import kim.biryeong.perfume.layering.dto.LayeringRecommendationResponse;
import kim.biryeong.perfume.layering.model.LayeringCandidate;
import kim.biryeong.perfume.layering.model.LayeringColor;
import kim.biryeong.perfume.layering.model.LayeringPerfumeProfile;
import kim.biryeong.perfume.layering.model.LayeringScore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LayeringRecommendationService {

  private static final String INVALID_SIZE_MESSAGE = "향수는 정확히 2개를 선택해야 합니다.";
  private static final String DUPLICATE_IDS_MESSAGE = "서로 다른 향수 2개를 선택해야 합니다.";
  private static final String MISSING_PERFUME_MESSAGE = "존재하지 않는 향수 ID가 포함되어 있습니다.";
  private static final String MISSING_ACCORD_DATA_MESSAGE = "향수 어코드 데이터가 부족합니다.";

  private final LayeringPerfumeQueryRepository queryRepository;
  private final LayeringScoreCalculator scoreCalculator;
  private final LayeringColorPalette colorPalette;
  private final LayeringExplanationAssembler explanationAssembler;

  @Transactional(readOnly = true)
  public LayeringRecommendationResponse recommend(LayeringRecommendationRequest request) {
    List<Long> perfumeIds = request.getPerfumeIds();
    validatePerfumeIds(perfumeIds);
    List<LayeringPerfumeProfile> profiles = queryRepository.findProfiles(perfumeIds);
    if (profiles.size() != 2) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, MISSING_PERFUME_MESSAGE);
    }
    if (profiles.stream().anyMatch(profile -> profile.accords().isEmpty())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MISSING_ACCORD_DATA_MESSAGE);
    }

    LayeringCandidate candidate = new LayeringCandidate(profiles.get(0), profiles.get(1));
    LayeringScore score = scoreCalculator.score(candidate);
    LayeringColor color = colorPalette.findByPair(score.dominantPair());
    return new LayeringRecommendationResponse(
        profiles.stream().map(LayeringRecommendationService::toResponse).toList(),
        explanationAssembler.assemble(score, color));
  }

  private static void validatePerfumeIds(List<Long> perfumeIds) {
    if (perfumeIds == null || perfumeIds.size() != 2) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_SIZE_MESSAGE);
    }
    Set<Long> distinctIds = new HashSet<>(perfumeIds);
    if (distinctIds.size() != 2) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DUPLICATE_IDS_MESSAGE);
    }
  }

  private static InputPerfumeResponse toResponse(LayeringPerfumeProfile profile) {
    return new InputPerfumeResponse(
        profile.id(),
        profile.brand(),
        profile.name(),
        profile.accords().stream()
            .limit(3)
            .map(accord -> new LayeringAccordResponse(accord.name(), accord.ratio()))
            .toList());
  }
}
