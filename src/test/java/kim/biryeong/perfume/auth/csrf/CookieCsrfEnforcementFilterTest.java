package kim.biryeong.perfume.auth.csrf;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicBoolean;
import kim.biryeong.perfume.auth.cookie.CookieBearerTokenResolver;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CookieCsrfEnforcementFilterTest {

  @Test
  void exemptsLayeringRecommendationPostWithContextPath() throws Exception {
    CookieCsrfEnforcementFilter filter = new CookieCsrfEnforcementFilter();
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/app/api/layering/recommendations");
    request.setContextPath("/app");
    request.setAttribute(CookieBearerTokenResolver.TOKEN_FROM_COOKIE_ATTRIBUTE, true);
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicBoolean continued = new AtomicBoolean();
    FilterChain chain = (servletRequest, servletResponse) -> continued.set(true);

    filter.doFilter(request, response, chain);

    assertThat(continued).isTrue();
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void requiresCsrfForCookieBackedMutatingPostWithContextPath() throws Exception {
    CookieCsrfEnforcementFilter filter = new CookieCsrfEnforcementFilter();
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/app/api/wishlist/1");
    request.setContextPath("/app");
    request.setAttribute(CookieBearerTokenResolver.TOKEN_FROM_COOKIE_ATTRIBUTE, true);
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicBoolean continued = new AtomicBoolean();
    FilterChain chain = (servletRequest, servletResponse) -> continued.set(true);

    filter.doFilter(request, response, chain);

    assertThat(continued).isFalse();
    assertThat(response.getStatus()).isEqualTo(403);
  }
}
