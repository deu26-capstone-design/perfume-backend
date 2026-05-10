package kim.biryeong.perfume.accord.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accord_notes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccordNote {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String accordName;

  @Column(nullable = false, length = 30)
  private String noteName;

  @Column(columnDefinition = "TEXT")
  private String imageUrl;
}
