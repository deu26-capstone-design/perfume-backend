package kim.biryeong.perfume.auth;

import kim.biryeong.perfume.domain.User;
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
		User user =
				userRepository.findByEmail(request.email()).orElseThrow(this::invalidCredentials);
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

	private InvalidCredentialsException invalidCredentials() {
		return new InvalidCredentialsException();
	}
}
