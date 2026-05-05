package kim.biryeong.perfume.review;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_seasons")
@IdClass(ReviewSeasonId.class)
@NoArgsConstructor
public class ReviewSeason {

  @Id
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @Id
  @Column(columnDefinition = "ENUM('봄','여름','가을','겨울')", nullable = false)
  private String season;

  public ReviewSeason(Review review, Season season) {
    this.review = review;
    setSeason(season);
  }

  public Review getReview() {
    return review;
  }

  public void setReview(Review review) {
    this.review = review;
  }

  public Season getSeason() {
    return Season.from(season);
  }

  public void setSeason(Season season) {
    this.season = season == null ? null : season.getValue();
  }
}
