package kim.biryeong.perfume.accord.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kim.biryeong.perfume.perfume.domain.Perfume;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "perfume_accords",
    indexes = {
      @Index(name = "idx_perfume_accords_perfume_accord", columnList = "perfume_id, accord_name"),
      @Index(
          name = "idx_perfume_accords_accord_ratio",
          columnList = "accord_name, ratio, perfume_id")
    })
@Getter
@Setter
@NoArgsConstructor
public class PerfumeAccord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "perfume_id", nullable = false)
  private Perfume perfume;

  @Column(name = "accord_name", length = 20)
  private String accordName;

  @Column(columnDefinition = "TINYINT")
  private Integer ratio;

  public PerfumeAccord(Long id, Perfume perfume, String accordName, Integer ratio) {
    this.id = id;
    this.perfume = perfume;
    this.accordName = accordName;
    this.ratio = ratio;
  }
}
