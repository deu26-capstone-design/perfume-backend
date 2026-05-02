package kim.biryeong.perfume.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NoteGroupDto {
    private List<String> top;
    private List<String> mid;
    private List<String> base;
}
