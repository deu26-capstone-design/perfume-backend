package kim.biryeong.perfume.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import kim.biryeong.perfume.domain.User;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final OAuthAccountService oauthAccountService;
	private final JwtService jwtService;
	private final AuthCookieFactory authCookieFactory;
	private final OAuth2RedirectProperties redirectProperties;
	private final AuthenticationFailureHandler failureHandler;

	public OAuth2LoginSuccessHandler(
			OAuthAccountService oauthAccountService,
			JwtService jwtService,
			AuthCookieFactory authCookieFactory,
			OAuth2RedirectProperties redirectProperties,
			OAuth2LoginFailureHandler failureHandler) {
		this.oauthAccountService = oauthAccountService;
		this.jwtService = jwtService;
		this.authCookieFactory = authCookieFactory;
		this.redirectProperties = redirectProperties;
		this.failureHandler = failureHandler;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		try {
			OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
			User user =
					oauthAccountService.findOrCreateUser(
							registrationId(authentication), oauth2User);
			String accessToken = jwtService.issueAccessToken(user);
			response.addHeader(
					HttpHeaders.SET_COOKIE,
					authCookieFactory.createAccessTokenCookie(accessToken).toString());
			clearOAuthSession(request);
			response.sendRedirect(redirectProperties.getSuccessRedirectUri());
		} catch (OAuth2AuthenticationException exception) {
			failureHandler.onAuthenticationFailure(request, response, exception);
		}
	}

	private String registrationId(Authentication authentication) {
		if (authentication instanceof OAuth2AuthenticationToken oauth2Authentication) {
			return oauth2Authentication.getAuthorizedClientRegistrationId();
		}
		throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_oauth_provider"));
	}

	private void clearOAuthSession(HttpServletRequest request) {
		SecurityContextHolder.clearContext();
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}
}
