package kim.biryeong.perfume.wishlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kim.biryeong.perfume.perfume.Perfume;
import kim.biryeong.perfume.perfume.PerfumeRepository;
import kim.biryeong.perfume.user.User;
import kim.biryeong.perfume.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class WishlistServiceTest {

  private WishlistRepository wishlistRepository;
  private PerfumeRepository perfumeRepository;
  private UserRepository userRepository;
  private WishlistService wishlistService;

  @BeforeEach
  void setUp() {
    wishlistRepository = mock(WishlistRepository.class);
    perfumeRepository = mock(PerfumeRepository.class);
    userRepository = mock(UserRepository.class);
    wishlistService = new WishlistService(wishlistRepository, perfumeRepository, userRepository);
  }

  @Test
  void addWishlistRejectsDuplicateWishlist() {
    when(perfumeRepository.findById(10L)).thenReturn(Optional.of(new Perfume()));
    when(userRepository.findById(7)).thenReturn(Optional.of(new User()));
    when(wishlistRepository.existsById(new WishlistId(10L, 7))).thenReturn(true);

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> wishlistService.addWishlist(10L, 7));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    verify(wishlistRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void removeWishlistRejectsMissingWishlistAfterValidatingPerfumeAndUser() {
    when(perfumeRepository.findById(10L)).thenReturn(Optional.of(new Perfume()));
    when(userRepository.findById(7)).thenReturn(Optional.of(new User()));
    when(wishlistRepository.existsById(new WishlistId(10L, 7))).thenReturn(false);

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> wishlistService.removeWishlist(10L, 7));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    verify(perfumeRepository).findById(10L);
    verify(userRepository).findById(7);
    verify(wishlistRepository, never()).deleteById(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void addWishlistRejectsMissingPerfume() {
    when(perfumeRepository.findById(10L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> wishlistService.addWishlist(10L, 7));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void addWishlistRejectsMissingUser() {
    when(perfumeRepository.findById(10L)).thenReturn(Optional.of(new Perfume()));
    when(userRepository.findById(7)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> wishlistService.addWishlist(10L, 7));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void getWishlistRejectsMissingUser() {
    when(userRepository.findById(7)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> wishlistService.getWishlist(7));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }
}
