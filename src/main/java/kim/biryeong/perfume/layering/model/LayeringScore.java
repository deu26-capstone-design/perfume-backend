package kim.biryeong.perfume.layering.model;

import java.util.List;

public record LayeringScore(
    int finalScore,
    int matrixScore,
    int structureScore,
    int balanceScore,
    int penaltyScore,
    AccordPair dominantPair,
    List<LayeringEvidence> evidences) {}
