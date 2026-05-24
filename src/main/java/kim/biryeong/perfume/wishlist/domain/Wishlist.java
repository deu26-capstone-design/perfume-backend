package kim.biryeong.perfume.wishlist.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "wishlist",
    indexes =
        @Index(
            name = "idx_wishlist_user_created_perfume",
            columnList = "user_id, created_at, perfume_id"))
@IdClass(WishlistId.class)
@Getter
@Setter
@NoArgsConstructor
public class Wishlist {

  @Id
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "perfume_id", nullable = false)
  private Perfume perfume;

  @Id
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public Wishlist(Perfume perfume, User user) {
    this.perfume = perfume;
    this.user = user;
  }
}
