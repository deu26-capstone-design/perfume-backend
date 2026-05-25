package kim.biryeong.perfume.layering.dto;

import java.util.List;

public record InputPerfumeResponse(
    Long id, String brand, String name, List<LayeringAccordResponse> dominantAccords) {}
