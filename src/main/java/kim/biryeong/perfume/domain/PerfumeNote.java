package kim.biryeong.perfume.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "perfume_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfumeNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80)
    private String perfumeName;

    @Column(length = 40)
    private String brand;

    @Column(length = 30)
    private String noteName;

    @Column(columnDefinition = "ENUM('top','mid','base')")
    private String noteType;
}
