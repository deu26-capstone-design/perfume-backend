package kim.biryeong.perfume.perfume;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "perfumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Perfume {

  @Id private Long id;

  @Column(length = 80)
  private String name;

  @Column(length = 40)
  private String brand;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "ENUM('W','M','U')")
  private Gender gender;

  @Column(columnDefinition = "TEXT")
  private String imageUrl;

  @Column(columnDefinition = "TEXT")
  private String description;
}
