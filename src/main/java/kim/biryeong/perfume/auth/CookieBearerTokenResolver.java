package kim.biryeong.perfume.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

public class CookieBearerTokenResolver implements BearerTokenResolver {

	public static final String TOKEN_FROM_COOKIE_ATTRIBUTE =
			CookieBearerTokenResolver.class.getName() + ".TOKEN_FROM_COOKIE";

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
}
