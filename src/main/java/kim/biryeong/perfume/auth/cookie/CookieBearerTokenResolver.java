package kim.biryeong.perfume.auth.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

public class CookieBearerTokenResolver implements BearerTokenResolver {

  public static final String TOKEN_FROM_COOKIE_ATTRIBUTE =
      CookieBearerTokenResolver.class.getName() + ".TOKEN_FROM_COOKIE";
  private static final Set<String> COOKIE_OPTIONAL_POST_PATHS =
      Set.of("/api/layering/recommendations");

  private final DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
  private final String cookieName;

  public CookieBearerTokenResolver(String cookieName) {
    this.cookieName = cookieName;
  }

  @Override
  public String resolve(HttpServletRequest request) {
    String bearerToken = delegate.resolve(request);
    if (bearerToken != null) {
      return bearerToken;
    }
    if (isCookieOptionalPublicPost(request)) {
      return null;
    }

    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (cookieName.equals(cookie.getName())) {
        request.setAttribute(TOKEN_FROM_COOKIE_ATTRIBUTE, true);
        return cookie.getValue();
      }
    }
    return null;
  }

  private boolean isCookieOptionalPublicPost(HttpServletRequest request) {
    return "POST".equals(request.getMethod())
        && COOKIE_OPTIONAL_POST_PATHS.contains(applicationPath(request));
  }

  private static String applicationPath(HttpServletRequest request) {
    String contextPath = request.getContextPath();
    String requestUri = request.getRequestURI();
    if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
      return requestUri.substring(contextPath.length());
    }
    return requestUri;
  }
}
