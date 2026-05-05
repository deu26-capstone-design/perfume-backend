package kim.biryeong.perfume.perfume;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatsDto {
  private Map<Integer, Integer> satisfaction;
  private Map<Integer, Integer> longevity;
  private Map<String, Integer> seasons;
}
