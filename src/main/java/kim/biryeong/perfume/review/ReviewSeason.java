package kim.biryeong.perfume.review;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review_seasons")
@IdClass(ReviewSeasonId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSeason {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('봄','여름','가을','겨울')", nullable = false)
    private Season season;
}
