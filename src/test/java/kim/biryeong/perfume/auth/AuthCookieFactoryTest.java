package kim.biryeong.perfume.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import kim.biryeong.perfume.auth.cookie.AuthCookieFactory;
import kim.biryeong.perfume.auth.cookie.AuthCookieProperties;
import kim.biryeong.perfume.auth.jwt.JwtProperties;
import org.junit.jupiter.api.Test;

class AuthCookieFactoryTest {

  @Test
  void expireAccessTokenCookieCreatesExpiredHttpOnlyCookie() {
    AuthCookieProperties cookieProperties = new AuthCookieProperties();
    cookieProperties.setName("PERFUME_ACCESS_TOKEN");
    JwtProperties jwtProperties = new JwtProperties();
    jwtProperties.setAccessTokenValidity(Duration.ofHours(1));
    AuthCookieFactory cookieFactory = new AuthCookieFactory(cookieProperties, jwtProperties);

    String cookie = cookieFactory.expireAccessTokenCookie().toString();

    assertThat(cookie).contains("PERFUME_ACCESS_TOKEN=");
    assertThat(cookie).contains("Max-Age=0");
    assertThat(cookie).contains("Path=/");
    assertThat(cookie).contains("HttpOnly");
    assertThat(cookie).contains("SameSite=None");
  }

  @Test
  void createAccessTokenCookieUsesConfiguredSameSiteAndSecureAttributes() {
    AuthCookieProperties cookieProperties = new AuthCookieProperties();
    cookieProperties.setName("PERFUME_ACCESS_TOKEN");
    cookieProperties.setSecure(true);
    cookieProperties.setSameSite("None");
    JwtProperties jwtProperties = new JwtProperties();
    jwtProperties.setAccessTokenValidity(Duration.ofHours(1));
    AuthCookieFactory cookieFactory = new AuthCookieFactory(cookieProperties, jwtProperties);

    String cookie = cookieFactory.createAccessTokenCookie("jwt-token").toString();

    assertThat(cookie).contains("PERFUME_ACCESS_TOKEN=jwt-token");
    assertThat(cookie).contains("Secure");
    assertThat(cookie).contains("SameSite=None");
  }

  @Test
  void createCsrfTokenCookieCreatesReadableDoubleSubmitCookie() {
    AuthCookieProperties cookieProperties = new AuthCookieProperties();
    JwtProperties jwtProperties = new JwtProperties();
    jwtProperties.setAccessTokenValidity(Duration.ofHours(1));
    AuthCookieFactory cookieFactory = new AuthCookieFactory(cookieProperties, jwtProperties);

    String cookie = cookieFactory.createCsrfTokenCookie().toString();

    assertThat(cookie).contains("XSRF-TOKEN=");
    assertThat(cookie).contains("Max-Age=3600");
    assertThat(cookie).contains("Path=/");
    assertThat(cookie).doesNotContain("HttpOnly");
    assertThat(cookie).contains("SameSite=None");
  }

  @Test
  void createCsrfTokenCookieUsesProvidedTokenValue() {
    AuthCookieProperties cookieProperties = new AuthCookieProperties();
    JwtProperties jwtProperties = new JwtProperties();
    jwtProperties.setAccessTokenValidity(Duration.ofHours(1));
    AuthCookieFactory cookieFactory = new AuthCookieFactory(cookieProperties, jwtProperties);

    String cookie = cookieFactory.createCsrfTokenCookie("csrf-token").toString();

    assertThat(cookie).contains("XSRF-TOKEN=csrf-token");
  }

  @Test
  void expireCsrfTokenCookieCreatesExpiredReadableCookie() {
    AuthCookieProperties cookieProperties = new AuthCookieProperties();
    JwtProperties jwtProperties = new JwtProperties();
    AuthCookieFactory cookieFactory = new AuthCookieFactory(cookieProperties, jwtProperties);

    String cookie = cookieFactory.expireCsrfTokenCookie().toString();

    assertThat(cookie).contains("XSRF-TOKEN=");
    assertThat(cookie).contains("Max-Age=0");
    assertThat(cookie).doesNotContain("HttpOnly");
  }
}
