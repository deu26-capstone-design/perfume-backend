package kim.biryeong.perfume.perfume;

public interface PerfumeCardProjection {
    Long getId();
    String getImageUrl();
    String getBrand();
    String getName();
    String getGender();
    Double getRating();
    Long getReviewCount();
}
