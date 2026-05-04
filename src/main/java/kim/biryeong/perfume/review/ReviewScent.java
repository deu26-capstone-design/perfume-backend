package kim.biryeong.perfume.review;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review_scents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewScent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Convert(converter = ScentNameConverter.class)
    @Column(columnDefinition = "ENUM('꽃 향','나무 향','청량한 향','스파이시한 향','달콤한 향','디저트 향','포근한 향','풀 향','상큼한 향','과일 향','허브 향','흙내음')", nullable = false)
    private ScentName scentName;
}
