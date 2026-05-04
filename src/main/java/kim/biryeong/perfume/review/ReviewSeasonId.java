package kim.biryeong.perfume.review;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReviewSeasonId implements Serializable {
    private Long review;
    private Season season;
}
