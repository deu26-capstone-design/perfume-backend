package kim.biryeong.perfume.layering.model;

import java.util.List;

public record AccordNarrative(
    String accordName,
    String displayNameKo,
    String displayNameEn,
    String primaryRole,
    String pyramidTendency,
    String volatility,
    String weight,
    String texture,
    String impression,
    String emotion,
    List<String> seasonTags,
    List<String> occasionTags,
    List<String> representativeNotes,
    String positivePhrase,
    String riskPhrase,
    String titleAdjective,
    String summaryPhrase) {}
