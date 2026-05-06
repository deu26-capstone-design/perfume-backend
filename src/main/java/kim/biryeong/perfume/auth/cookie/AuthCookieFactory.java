package kim.biryeong.perfume.auth.cookie;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import kim.biryeong.perfume.auth.jwt.JwtProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieFactory {

  public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";

  private static final int CSRF_TOKEN_BYTES = 32;

  private final SecureRandom secureRandom = new SecureRandom();

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

  public ResponseCookie createCsrfTokenCookie() {
    byte[] tokenBytes = new byte[CSRF_TOKEN_BYTES];
    secureRandom.nextBytes(tokenBytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    return csrfCookie(token).maxAge(jwtProperties.getAccessTokenValidity()).build();
  }

  public ResponseCookie expireCsrfTokenCookie() {
    return csrfCookie("").maxAge(Duration.ZERO).build();
  }

  private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
    return ResponseCookie.from(cookieProperties.getName(), value)
        .httpOnly(true)
        .secure(cookieProperties.isSecure())
        .sameSite("Lax")
        .path("/");
  }

  private ResponseCookie.ResponseCookieBuilder csrfCookie(String value) {
    return ResponseCookie.from(CSRF_COOKIE_NAME, value)
        .httpOnly(false)
        .secure(cookieProperties.isSecure())
        .sameSite("Lax")
        .path("/");
  }
}
