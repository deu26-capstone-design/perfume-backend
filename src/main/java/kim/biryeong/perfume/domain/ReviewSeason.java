package kim.biryeong.perfume.domain;

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
    @JoinColumn(name = "perfume_id", nullable = false)
    private Perfume perfume;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @Column(columnDefinition = "ENUM('봄','여름','가을','겨울')", nullable = false)
    private String season;
}
