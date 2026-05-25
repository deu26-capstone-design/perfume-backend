package kim.biryeong.perfume.layering.dto;

public record LayeringColorResponse(
    String name, String hex, String sourceAccord, String targetAccord, String description) {}
