package kim.biryeong.perfume.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import kim.biryeong.perfume.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class OAuth2LoginSuccessHandlerTest {

	@Test
	void oauthSuccessIssuesCookieAndUsesRegistrationId() throws Exception {
		OAuth2RedirectProperties redirectProperties = new OAuth2RedirectProperties();
		redirectProperties.setSuccessRedirectUri("http://localhost:3000/oauth2/success");
		RecordingOAuthAccountService accountService = new RecordingOAuthAccountService();
		OAuth2LoginSuccessHandler successHandler =
				new OAuth2LoginSuccessHandler(
						accountService,
						new FixedJwtService(),
						cookieFactory(),
						redirectProperties,
						new OAuth2LoginFailureHandler(redirectProperties));
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession();
		MockHttpServletResponse response = new MockHttpServletResponse();

		successHandler.onAuthenticationSuccess(
				request, response, new OAuth2AuthenticationToken(naverUser(), List.of(), "naver"));

		assertThat(accountService.registrationId).isEqualTo("naver");
		assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:3000/oauth2/success");
		assertThat(response.getCookie("PERFUME_ACCESS_TOKEN").getValue())
				.isEqualTo("fixed-access-token");
		assertThat(response.getCookie("PERFUME_ACCESS_TOKEN").isHttpOnly()).isTrue();
		assertThat(request.getSession(false)).isNull();
	}

	@Test
	void oauthValidationFailureRedirectsToFailureUri() throws Exception {
		OAuth2RedirectProperties redirectProperties = new OAuth2RedirectProperties();
		redirectProperties.setFailureRedirectUri("http://localhost:3000/oauth2/failure");
		OAuth2LoginFailureHandler failureHandler =
				new OAuth2LoginFailureHandler(redirectProperties);
		OAuth2LoginSuccessHandler successHandler =
				new OAuth2LoginSuccessHandler(
						new RejectingOAuthAccountService(),
						null,
						null,
						redirectProperties,
						failureHandler);
		MockHttpServletResponse response = new MockHttpServletResponse();

		successHandler.onAuthenticationSuccess(
				new MockHttpServletRequest(),
				response,
				new OAuth2AuthenticationToken(googleUser(), List.of(), "google"));

		assertThat(response.getRedirectedUrl())
				.isEqualTo("http://localhost:3000/oauth2/failure?error=email_not_verified");
	}

	@Test
	void unsupportedAuthenticationRedirectsToFailureUri() throws Exception {
		OAuth2RedirectProperties redirectProperties = new OAuth2RedirectProperties();
		redirectProperties.setFailureRedirectUri("http://localhost:3000/oauth2/failure");
		OAuth2LoginFailureHandler failureHandler =
				new OAuth2LoginFailureHandler(redirectProperties);
		OAuth2LoginSuccessHandler successHandler =
				new OAuth2LoginSuccessHandler(
						new RejectingOAuthAccountService(),
						null,
						null,
						redirectProperties,
						failureHandler);
		MockHttpServletResponse response = new MockHttpServletResponse();

		successHandler.onAuthenticationSuccess(
				new MockHttpServletRequest(),
				response,
				new TestingAuthenticationToken(googleUser(), null));

		assertThat(response.getRedirectedUrl())
				.isEqualTo("http://localhost:3000/oauth2/failure?error=unsupported_oauth_provider");
	}

	private OAuth2User googleUser() {
		return new DefaultOAuth2User(
				List.of(new SimpleGrantedAuthority("ROLE_USER")),
				Map.of(
						"sub", "google-sub",
						"email", "unverified@example.com",
						"email_verified", false,
						"name", "Unverified"),
				"sub");
	}

	private OAuth2User naverUser() {
		return new DefaultOAuth2User(
				List.of(new SimpleGrantedAuthority("ROLE_USER")),
				Map.of("response", Map.of("id", "naver-id", "email", "naver@example.com")),
				"response");
	}

	private AuthCookieFactory cookieFactory() {
		AuthCookieProperties cookieProperties = new AuthCookieProperties();
		cookieProperties.setName("PERFUME_ACCESS_TOKEN");
		JwtProperties jwtProperties = new JwtProperties();
		return new AuthCookieFactory(cookieProperties, jwtProperties);
	}

	private static class RejectingOAuthAccountService extends OAuthAccountService {

		RejectingOAuthAccountService() {
			super(null);
		}

		@Override
		public kim.biryeong.perfume.domain.User findOrCreateUser(
				String registrationId, OAuth2User oauth2User) {
			throw new OAuth2AuthenticationException(new OAuth2Error("email_not_verified"));
		}
	}

	private static class RecordingOAuthAccountService extends OAuthAccountService {

		private String registrationId;

		RecordingOAuthAccountService() {
			super(null);
		}

		@Override
		public User findOrCreateUser(String registrationId, OAuth2User oauth2User) {
			this.registrationId = registrationId;
			User user = new User();
			user.setUserId(1);
			user.setEmail("naver@example.com");
			user.setName("Naver User");
			user.setProfileCompleted(false);
			return user;
		}
	}

	private static class FixedJwtService extends JwtService {

		FixedJwtService() {
			super(null, null);
		}

		@Override
		public String issueAccessToken(User user) {
			return "fixed-access-token";
		}
	}
}
