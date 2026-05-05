package kim.biryeong.perfume.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;

@Getter
public class ReviewRequest {

  @NotNull private Integer userId;

  @NotNull
  @Min(1)
  @Max(5)
  private Integer satisfaction;

  @Min(1)
  @Max(3)
  private Integer longevity;

  private List<String> seasons;

  @Size(max = 5)
  private List<String> scents;

  @Size(max = 1000)
  private String comment;

  @NotNull private Boolean disclaimerAgreed;
}
