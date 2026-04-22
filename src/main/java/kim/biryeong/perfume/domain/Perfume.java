package kim.biryeong.perfume.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "perfumes")
@IdClass(PerfumeId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Perfume {

    @Id
    @Column(length = 80)
    private String name;

    @Id
    @Column(length = 40)
    private String brand;

    @Column(columnDefinition = "ENUM('W','M','U')")
    private String gender;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;
}
