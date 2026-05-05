package kim.biryeong.perfume.review;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReviewSeasonId implements Serializable {
  private Long review;
  private Season season;
}
