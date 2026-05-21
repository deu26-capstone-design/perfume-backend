package kim.biryeong.perfume.wishlist.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.repository.PerfumeRepository;
import kim.biryeong.perfume.user.domain.User;
import kim.biryeong.perfume.user.repository.UserRepository;
import kim.biryeong.perfume.wishlist.domain.WishlistId;
import kim.biryeong.perfume.wishlist.dto.WishlistListResponse;
import kim.biryeong.perfume.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
  void addWishlistRejectsMissingPerfume() {
    when(perfumeRepository.findById(1L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> wishlistService.addWishlist(1L, 7));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void addWishlistRejectsMissingUser() {
    when(perfumeRepository.findById(1L)).thenReturn(Optional.of(new Perfume()));
    when(userRepository.findById(7)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> wishlistService.addWishlist(1L, 7));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void addWishlistRejectsDuplicateWishlist() {
    when(perfumeRepository.findById(1L)).thenReturn(Optional.of(new Perfume()));
    when(userRepository.findById(7)).thenReturn(Optional.of(new User()));
    when(wishlistRepository.existsById(new WishlistId(1L, 7))).thenReturn(true);

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> wishlistService.addWishlist(1L, 7));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
  }

  @Test
  void removeWishlistRejectsMissingPerfume() {
    when(perfumeRepository.findById(1L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> wishlistService.removeWishlist(1L, 7));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void removeWishlistRejectsNotInWishlist() {
    when(perfumeRepository.findById(1L)).thenReturn(Optional.of(new Perfume()));
    when(userRepository.findById(7)).thenReturn(Optional.of(new User()));
    when(wishlistRepository.existsById(new WishlistId(1L, 7))).thenReturn(false);

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> wishlistService.removeWishlist(1L, 7));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void getWishlistRejectsMissingUser() {
    when(userRepository.findById(99)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> wishlistService.getWishlist(99));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void findWishlistedPerfumeIdsReturnsEmptySetForNullUserId() {
    Set<Long> result = wishlistService.findWishlistedPerfumeIds(null, List.of(1L, 2L));

    assertTrue(result.isEmpty());
  }

  @Test
  void findWishlistedPerfumeIdsReturnsEmptySetForEmptyPerfumeIds() {
    Set<Long> result = wishlistService.findWishlistedPerfumeIds(7, List.of());

    assertTrue(result.isEmpty());
  }

  @Test
  void findWishlistedPerfumeIdsReturnsMatchingIds() {
    when(wishlistRepository.findWishlistedPerfumeIds(7, List.of(1L, 2L, 3L)))
        .thenReturn(List.of(1L, 3L));

    Set<Long> result = wishlistService.findWishlistedPerfumeIds(7, List.of(1L, 2L, 3L));

    assertEquals(Set.of(1L, 3L), result);
  }

  @Test
  void getWishlistPageRejectsMissingUser() {
    when(userRepository.findById(99)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> wishlistService.getWishlistPage(99, 0, 30));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void getWishlistPageReturnsResultForValidUser() {
    when(userRepository.findById(7)).thenReturn(Optional.of(new User()));
    when(wishlistRepository.findPageByUserId(eq(7), any()))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 30), 0));

    WishlistListResponse response = wishlistService.getWishlistPage(7, 0, 30);

    assertTrue(response.getContent().isEmpty());
    assertEquals(0, response.getTotalElements());
  }
}
