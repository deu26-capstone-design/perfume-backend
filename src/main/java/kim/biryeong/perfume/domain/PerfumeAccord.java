package kim.biryeong.perfume.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "perfume_accords")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfumeAccord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80)
    private String perfumeName;

    @Column(length = 40)
    private String brand;

    @Column(length = 20)
    private String accordName;

    @Column(columnDefinition = "TINYINT")
    private Integer ratio;
}
