package kim.biryeong.perfume.layering.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LayeringRecommendationRequest {

  @NotNull(message = "향수는 정확히 2개를 선택해야 합니다.")
  @Size(min = 2, max = 2, message = "향수는 정확히 2개를 선택해야 합니다.")
  private List<@NotNull @Min(1) Long> perfumeIds;
}
