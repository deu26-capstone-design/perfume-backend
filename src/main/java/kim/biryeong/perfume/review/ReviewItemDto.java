package kim.biryeong.perfume.review;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewItemDto {
  private String nickname;
  private String profileImageUrl;
  private int satisfaction;
  private Integer longevity;
  private List<String> seasons;
  private List<String> scents;
  private String comment;
  private LocalDate createdAt;
}
