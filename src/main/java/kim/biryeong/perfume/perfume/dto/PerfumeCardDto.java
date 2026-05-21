package kim.biryeong.perfume.perfume.dto;

import lombok.Getter;

/** 향수 목록 카드 응답. PerfumeCardProjection에 로그인 사용자 위시리스트 여부를 추가한다. */
@Getter
public class PerfumeCardDto {

  private final Long id;
  private final String imageUrl;
  private final String brand;
  private final String name;
  private final String gender;
  private final Double rating;
  private final Long reviewCount;

  /** 현재 로그인한 사용자의 위시리스트 포함 여부. 비로그인 시 항상 false다. */
  private final boolean isWishlisted;

  public PerfumeCardDto(PerfumeCardProjection projection, boolean isWishlisted) {
    this.id = projection.getId();
    this.imageUrl = projection.getImageUrl();
    this.brand = projection.getBrand();
    this.name = projection.getName();
    this.gender = projection.getGender();
    this.rating = projection.getRating();
    this.reviewCount = projection.getReviewCount();
    this.isWishlisted = isWishlisted;
  }
}
