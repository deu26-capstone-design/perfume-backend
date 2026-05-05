package kim.biryeong.perfume.review;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import kim.biryeong.perfume.perfume.Perfume;
import kim.biryeong.perfume.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(columnNames = {"perfume_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "perfume_id", nullable = false)
  private Perfume perfume;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, columnDefinition = "TINYINT")
  private Integer satisfaction;

  @Column(columnDefinition = "TINYINT")
  private Integer longevity;

  @Column(columnDefinition = "TEXT")
  private String comment;

  @Column(nullable = false)
  private Boolean disclaimerAgreed;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
