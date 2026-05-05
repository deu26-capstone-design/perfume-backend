package kim.biryeong.perfume.review;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ReviewSeasonTest {

  @Test
  void storesSeasonAsDisplayValueAndExposesEnum() {
    Review review = new Review();
    ReviewSeason reviewSeason = new ReviewSeason(review, Season.SPRING);

    assertEquals(review, reviewSeason.getReview());
    assertEquals(Season.SPRING, reviewSeason.getSeason());
  }
}
