package kim.biryeong.perfume.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import kim.biryeong.perfume.auth.dto.LoginRequest;
import kim.biryeong.perfume.auth.dto.SignupRequest;
import kim.biryeong.perfume.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
@Import({AuthService.class, LocalAuthServiceTest.PasswordEncoderConfig.class})
class LocalAuthServiceTest {

	@Autowired private AuthService authService;

	@Autowired private UserRepository userRepository;

	@Autowired private PasswordEncoder passwordEncoder;

	@Test
	void signupCreatesCompletedUserWithEncodedPassword() {
		User user = authService.signup(signupRequest("new@example.com", "newnick"));

		assertThat(user.getUserId()).isNotNull();
		assertThat(user.getEmail()).isEqualTo("new@example.com");
		assertThat(user.getName()).isEqualTo("Local User");
		assertThat(user.getNickname()).isEqualTo("newnick");
		assertThat(user.getGender()).isEqualTo("F");
		assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1999, 5, 1));
		assertThat(user.getPhoneNumber()).isEqualTo("01012345678");
		assertThat(user.isProfileCompleted()).isTrue();
		assertThat(user.getOauthProvider()).isNull();
		assertThat(user.getOauthProviderId()).isNull();
		assertThat(user.getPassword()).isNotEqualTo("secret-password");
		assertThat(passwordEncoder.matches("secret-password", user.getPassword())).isTrue();
	}

	@Test
	void signupRejectsDuplicateEmail() {
		authService.signup(signupRequest("duplicate@example.com", "first"));

		assertThatThrownBy(
						() -> authService.signup(signupRequest("duplicate@example.com", "second")))
				.isInstanceOf(AuthConflictException.class)
				.hasMessage("email already exists");
	}

	@Test
	void signupRejectsDuplicateNickname() {
		authService.signup(signupRequest("first@example.com", "duplicate"));

		assertThatThrownBy(
						() -> authService.signup(signupRequest("second@example.com", "duplicate")))
				.isInstanceOf(AuthConflictException.class)
				.hasMessage("nickname already exists");
	}

	@Test
	void loginReturnsUserWhenPasswordMatches() {
		User signedUp = authService.signup(signupRequest("login@example.com", "login"));

		User user = authService.login(new LoginRequest("login@example.com", "secret-password"));

		assertThat(user.getUserId()).isEqualTo(signedUp.getUserId());
	}

	@Test
	void loginRejectsInvalidPassword() {
		authService.signup(signupRequest("bad-password@example.com", "badpassword"));

		assertThatThrownBy(
						() ->
								authService.login(
										new LoginRequest(
												"bad-password@example.com", "wrong-password")))
				.isInstanceOf(InvalidCredentialsException.class)
				.hasMessage("invalid credentials");
	}

	@Test
	void loginRejectsOAuthOnlyUser() {
		User user = new User();
		user.setEmail("oauth-only@example.com");
		user.setName("OAuth Only");
		user.setProfileCompleted(false);
		userRepository.saveAndFlush(user);

		assertThatThrownBy(
						() ->
								authService.login(
										new LoginRequest(
												"oauth-only@example.com", "secret-password")))
				.isInstanceOf(InvalidCredentialsException.class)
				.hasMessage("invalid credentials");
	}

	private SignupRequest signupRequest(String email, String nickname) {
		return new SignupRequest(
				email,
				"secret-password",
				"Local User",
				nickname,
				"F",
				LocalDate.of(1999, 5, 1),
				"01012345678");
	}

	static class PasswordEncoderConfig {

		@Bean
		PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}
	}
}
