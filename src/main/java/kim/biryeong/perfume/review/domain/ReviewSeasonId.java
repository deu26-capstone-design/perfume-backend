package kim.biryeong.perfume.review.domain;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReviewSeasonId implements Serializable {
  private Long review;
  private String season;
}
