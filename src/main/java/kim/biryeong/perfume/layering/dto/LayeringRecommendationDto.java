package kim.biryeong.perfume.layering.dto;

import java.util.List;

public record LayeringRecommendationDto(
    String candidateType,
    boolean recommended,
    String decision,
    int score,
    String title,
    String summary,
    LayeringColorResponse color,
    List<String> bestFor,
    List<String> reasons,
    List<String> warnings,
    ScoreBreakdownResponse scoreBreakdown) {}
