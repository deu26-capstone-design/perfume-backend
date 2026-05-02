package kim.biryeong.perfume.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
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
		assertThat(cookie).contains("SameSite=Lax");
	}
}
