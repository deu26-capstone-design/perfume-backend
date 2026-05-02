package kim.biryeong.perfume.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReviewSeasonId implements Serializable {
    private Long perfume;
    private Integer user;
    private String season;
}
