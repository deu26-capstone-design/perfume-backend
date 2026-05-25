package kim.biryeong.perfume.layering.dto;

import java.util.List;

public record LayeringRecommendationResponse(
    List<InputPerfumeResponse> inputPerfumes, LayeringRecommendationDto recommendation) {}
