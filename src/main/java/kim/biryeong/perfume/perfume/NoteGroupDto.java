package kim.biryeong.perfume.perfume;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoteGroupDto {
  private List<String> top;
  private List<String> mid;
  private List<String> base;
}
