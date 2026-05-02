package kim.biryeong.perfume.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class StatsDto {
    private Map<Integer, Integer> satisfaction;
    private Map<Integer, Integer> longevity;
    private Map<String, Integer> seasons;
}
