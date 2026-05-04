package kim.biryeong.perfume.wishlist;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Validated
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{perfumeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addWishlist(
            @PathVariable @Min(1) Long perfumeId,
            @RequestParam @Min(1) Integer userId) {
        wishlistService.addWishlist(perfumeId, userId);
    }

    @DeleteMapping("/{perfumeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeWishlist(
            @PathVariable @Min(1) Long perfumeId,
            @RequestParam @Min(1) Integer userId) {
        wishlistService.removeWishlist(perfumeId, userId);
    }

    @GetMapping
    public List<WishlistResponse> getWishlist(@RequestParam @Min(1) Integer userId) {
        return wishlistService.getWishlist(userId);
    }
}
