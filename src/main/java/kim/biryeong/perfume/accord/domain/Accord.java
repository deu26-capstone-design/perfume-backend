package kim.biryeong.perfume.accord.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accords")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Accord {

  @Id private Long id;

  @Column(unique = true, nullable = false, length = 20)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String imageUrl;
}
