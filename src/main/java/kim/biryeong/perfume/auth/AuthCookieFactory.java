package kim.biryeong.perfume.auth;

import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieFactory {

	private final AuthCookieProperties cookieProperties;
	private final JwtProperties jwtProperties;

	public AuthCookieFactory(AuthCookieProperties cookieProperties, JwtProperties jwtProperties) {
		this.cookieProperties = cookieProperties;
		this.jwtProperties = jwtProperties;
	}

	public ResponseCookie createAccessTokenCookie(String token) {
		return baseCookie(token).maxAge(jwtProperties.getAccessTokenValidity()).build();
	}

	public ResponseCookie expireAccessTokenCookie() {
		return baseCookie("").maxAge(Duration.ZERO).build();
	}

	private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
		return ResponseCookie.from(cookieProperties.getName(), value)
				.httpOnly(true)
				.secure(cookieProperties.isSecure())
				.sameSite("Lax")
				.path("/");
	}
}
