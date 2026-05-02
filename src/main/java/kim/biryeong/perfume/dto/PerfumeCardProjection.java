package kim.biryeong.perfume.dto;

public interface PerfumeCardProjection {
    Long getId();
    String getImageUrl();
    String getBrand();
    String getName();
    String getGender();
    Double getRating();
    Long getReviewCount();
}
