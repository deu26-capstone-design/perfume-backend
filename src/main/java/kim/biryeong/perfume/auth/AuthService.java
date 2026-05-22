package kim.biryeong.perfume.auth;

import kim.biryeong.perfume.auth.dto.CompleteProfileRequest;
import kim.biryeong.perfume.auth.dto.LoginRequest;
import kim.biryeong.perfume.auth.dto.SignupRequest;
import kim.biryeong.perfume.auth.dto.UpdateProfileRequest;
import kim.biryeong.perfume.user.domain.User;
import kim.biryeong.perfume.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public User signup(SignupRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new AuthConflictException("email already exists");
    }
    if (userRepository.existsByNickname(request.nickname())) {
      throw new AuthConflictException("nickname already exists");
    }
    User user = new User();
    user.setEmail(request.email());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setName(request.name());
    user.setNickname(request.nickname());
    user.setGender(request.gender());
    user.setBirthDate(request.birthDate());
    user.setPhoneNumber(request.phoneNumber());
    user.setProfileCompleted(true);
    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public User login(LoginRequest request) {
    User user = userRepository.findByEmail(request.email()).orElseThrow(this::invalidCredentials);
    if (user.getPassword() == null
        || !passwordEncoder.matches(request.password(), user.getPassword())) {
      throw invalidCredentials();
    }
    return user;
  }

  @Transactional(readOnly = true)
  public User getCurrentUser(Integer userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new AuthUnauthorizedException("authentication is required"));
  }

  @Transactional
  public User completeProfile(Integer userId, CompleteProfileRequest request) {
    User user = getCurrentUser(userId);
    if (userRepository.existsByNicknameAndUserIdNot(request.nickname(), userId)) {
      throw new AuthConflictException("nickname already exists");
    }
    user.setName(request.name());
    user.setNickname(request.nickname());
    user.setGender(request.gender());
    user.setBirthDate(request.birthDate());
    user.setPhoneNumber(request.phoneNumber());
    user.setProfileCompleted(true);
    return user;
  }

  /**
   * 사용자 프로필을 수정한다. 닉네임과 전화번호만 변경 가능하다.
   *
   * <p>닉네임 중복 시 {@link AuthConflictException}을 던진다.
   *
   * @param userId 수정할 사용자 ID
   * @param request 프로필 수정 요청 (닉네임, 전화번호)
   * @return 갱신된 사용자 엔티티
   */
  @Transactional
  public User updateProfile(Integer userId, UpdateProfileRequest request) {
    User user = getCurrentUser(userId);
    if (userRepository.existsByNicknameAndUserIdNot(request.nickname(), userId)) {
      throw new AuthConflictException("nickname already exists");
    }
    user.setNickname(request.nickname());
    user.setPhoneNumber(request.phoneNumber());
    return user;
  }

  private InvalidCredentialsException invalidCredentials() {
    return new InvalidCredentialsException();
  }
}
