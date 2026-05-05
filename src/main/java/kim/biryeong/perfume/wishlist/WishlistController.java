package kim.biryeong.perfume.wishlist;

import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Validated
public class WishlistController {

  private final WishlistService wishlistService;

  /**
   * Temporary service-prep contract: userId is accepted as a request parameter only until
   * authentication is integrated. This is not production authorization.
   */
  @PostMapping("/{perfumeId}")
  @ResponseStatus(HttpStatus.CREATED)
  public void addWishlist(
      @PathVariable @Min(1) Long perfumeId, @RequestParam @Min(1) Integer userId) {
    wishlistService.addWishlist(perfumeId, userId);
  }

  /**
   * Temporary service-prep contract: userId is accepted as a request parameter only until
   * authentication is integrated. This is not production authorization.
   */
  @DeleteMapping("/{perfumeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeWishlist(
      @PathVariable @Min(1) Long perfumeId, @RequestParam @Min(1) Integer userId) {
    wishlistService.removeWishlist(perfumeId, userId);
  }

  /**
   * Temporary service-prep contract: userId is accepted as a request parameter only until
   * authentication is integrated. This is not production authorization.
   */
  @GetMapping
  public List<WishlistResponse> getWishlist(@RequestParam @Min(1) Integer userId) {
    return wishlistService.getWishlist(userId);
  }
}
