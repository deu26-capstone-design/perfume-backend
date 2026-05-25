package kim.biryeong.perfume.auth.csrf;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import kim.biryeong.perfume.auth.cookie.AuthCookieFactory;
import kim.biryeong.perfume.auth.cookie.CookieBearerTokenResolver;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class CookieCsrfEnforcementFilter extends OncePerRequestFilter {

  private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
  private static final Set<String> CSRF_EXEMPT_UNSAFE_PATHS =
      Set.of("/api/layering/recommendations");
  private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (requiresCookieCsrfCheck(request) && !hasValidDoubleSubmitToken(request)) {
      response.sendError(HttpStatus.FORBIDDEN.value(), "CSRF token is required");
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean requiresCookieCsrfCheck(HttpServletRequest request) {
    return Boolean.TRUE.equals(
            request.getAttribute(CookieBearerTokenResolver.TOKEN_FROM_COOKIE_ATTRIBUTE))
        && !SAFE_METHODS.contains(request.getMethod())
        && !CSRF_EXEMPT_UNSAFE_PATHS.contains(request.getRequestURI());
  }

  private boolean hasValidDoubleSubmitToken(HttpServletRequest request) {
    String csrfCookie = csrfCookie(request);
    String csrfHeader = request.getHeader(CSRF_HEADER_NAME);
    return StringUtils.hasText(csrfCookie) && csrfCookie.equals(csrfHeader);
  }

  private String csrfCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (AuthCookieFactory.CSRF_COOKIE_NAME.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
