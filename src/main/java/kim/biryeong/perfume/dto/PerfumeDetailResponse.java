package kim.biryeong.perfume.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PerfumeDetailResponse {
    private Long id;
    private String imageUrl;
    private String brand;
    private String name;
    private String gender;
    private String description;
    private double rating;
    private long reviewCount;
    private NoteGroupDto notes;
    private List<AccordDto> accords;
    private StatsDto stats;
}
