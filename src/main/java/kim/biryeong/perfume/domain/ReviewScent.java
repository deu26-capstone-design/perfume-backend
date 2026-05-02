package kim.biryeong.perfume.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review_scents")
@IdClass(ReviewScentId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewScent {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfume_id", nullable = false)
    private Perfume perfume;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @Column(columnDefinition = "ENUM('꽃향기','나무향','청량한향','매콤한향','달콤한향','음식향','포근한향','풀향','시트러스향','과일향','허브향','흙내음')", nullable = false)
    private String scentName;
}
