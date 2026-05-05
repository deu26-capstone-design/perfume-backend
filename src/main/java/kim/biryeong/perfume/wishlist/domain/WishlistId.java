package kim.biryeong.perfume.wishlist.domain;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WishlistId implements Serializable {
  private Long perfume;
  private Integer user;
}
