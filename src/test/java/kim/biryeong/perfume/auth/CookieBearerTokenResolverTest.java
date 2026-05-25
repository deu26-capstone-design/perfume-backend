package kim.biryeong.perfume.auth;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import kim.biryeong.perfume.auth.cookie.CookieBearerTokenResolver;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

class CookieBearerTokenResolverTest {

  @Test
  void ignoresCookieTokenOnLayeringRecommendationPublicPost() {
    CookieBearerTokenResolver resolver = new CookieBearerTokenResolver("PERFUME_ACCESS_TOKEN");
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/api/layering/recommendations");
    request.setCookies(new Cookie("PERFUME_ACCESS_TOKEN", "stale-token"));

    String token = resolver.resolve(request);

    assertThat(token).isNull();
    assertThat(request.getAttribute(CookieBearerTokenResolver.TOKEN_FROM_COOKIE_ATTRIBUTE))
        .isNull();
  }

  @Test
  void ignoresCookieTokenOnLayeringRecommendationPublicPostWithContextPath() {
    CookieBearerTokenResolver resolver = new CookieBearerTokenResolver("PERFUME_ACCESS_TOKEN");
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/app/api/layering/recommendations");
    request.setContextPath("/app");
    request.setCookies(new Cookie("PERFUME_ACCESS_TOKEN", "stale-token"));

    String token = resolver.resolve(request);

    assertThat(token).isNull();
    assertThat(request.getAttribute(CookieBearerTokenResolver.TOKEN_FROM_COOKIE_ATTRIBUTE))
        .isNull();
  }

  @Test
  void keepsExplicitAuthorizationHeaderOnLayeringRecommendationPublicPost() {
    CookieBearerTokenResolver resolver = new CookieBearerTokenResolver("PERFUME_ACCESS_TOKEN");
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/api/layering/recommendations");
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer explicit-token");
    request.setCookies(new Cookie("PERFUME_ACCESS_TOKEN", "stale-token"));

    String token = resolver.resolve(request);

    assertThat(token).isEqualTo("explicit-token");
  }

  @Test
  void resolvesCookieTokenForAuthenticatedPost() {
    CookieBearerTokenResolver resolver = new CookieBearerTokenResolver("PERFUME_ACCESS_TOKEN");
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/wishlist/1");
    request.setCookies(new Cookie("PERFUME_ACCESS_TOKEN", "cookie-token"));

    String token = resolver.resolve(request);

    assertThat(token).isEqualTo("cookie-token");
    assertThat(request.getAttribute(CookieBearerTokenResolver.TOKEN_FROM_COOKIE_ATTRIBUTE))
        .isEqualTo(true);
  }
}
