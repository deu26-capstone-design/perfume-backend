package kim.biryeong.perfume.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import kim.biryeong.perfume.perfume.dto.PerfumeCardProjection;
import kim.biryeong.perfume.perfume.dto.PerfumeListResponse;
import kim.biryeong.perfume.review.dto.ReviewItemDto;
import kim.biryeong.perfume.review.dto.ReviewListResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PaginationResponseTest {

  @Test
  void perfumeListResponseIncludesPaginationMetadata() {
    PerfumeListResponse response =
        new PerfumeListResponse(
            new PageImpl<>(List.of(new TestPerfumeCardProjection()), PageRequest.of(0, 1), 2));

    assertTrue(response.isHasNext());
    assertEquals(2, response.getTotalElements());
    assertEquals(2, response.getTotalPages());
  }

  @Test
  void reviewListResponseIncludesPaginationMetadata() {
    ReviewListResponse response =
        new ReviewListResponse(
            new PageImpl<>(
                List.of(
                    new ReviewItemDto(
                        "nick",
                        null,
                        5,
                        3,
                        List.of("봄"),
                        List.of("꽃 향"),
                        "좋아요.",
                        LocalDate.of(2026, 5, 5))),
                PageRequest.of(0, 1),
                2));

    assertTrue(response.isHasNext());
    assertEquals(2, response.getTotalElements());
    assertEquals(2, response.getTotalPages());
  }

  private static class TestPerfumeCardProjection implements PerfumeCardProjection {

    @Override
    public Long getId() {
      return 1L;
    }

    @Override
    public String getImageUrl() {
      return null;
    }

    @Override
    public String getBrand() {
      return "brand";
    }

    @Override
    public String getName() {
      return "name";
    }

    @Override
    public String getGender() {
      return "U";
    }

    @Override
    public Double getRating() {
      return 5.0;
    }

    @Override
    public Long getReviewCount() {
      return 1L;
    }
  }
}
