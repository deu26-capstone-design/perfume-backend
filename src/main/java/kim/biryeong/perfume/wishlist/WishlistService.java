package kim.biryeong.perfume.wishlist;

import kim.biryeong.perfume.perfume.Perfume;
import kim.biryeong.perfume.perfume.PerfumeRepository;
import kim.biryeong.perfume.user.User;
import kim.biryeong.perfume.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final PerfumeRepository perfumeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addWishlist(Long perfumeId, Integer userId) {
        Perfume perfume = perfumeRepository.findById(perfumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 향수 ID입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저 ID입니다."));

        WishlistId id = new WishlistId(perfumeId, userId);
        if (wishlistRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 위시리스트에 추가된 향수입니다.");
        }
        wishlistRepository.save(new Wishlist(perfume, user));
    }

    @Transactional
    public void removeWishlist(Long perfumeId, Integer userId) {
        WishlistId id = new WishlistId(perfumeId, userId);
        if (!wishlistRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "위시리스트에 없는 향수입니다.");
        }
        wishlistRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse> getWishlist(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저 ID입니다."));
        return wishlistRepository.findByUserId(userId);
    }
}
