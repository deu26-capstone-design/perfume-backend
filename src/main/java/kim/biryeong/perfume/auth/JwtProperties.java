package kim.biryeong.perfume.auth;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.jwt")
public class JwtProperties {

	private String secret = "";

	private Duration accessTokenValidity = Duration.ofHours(1);

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public Duration getAccessTokenValidity() {
		return accessTokenValidity;
	}

	public void setAccessTokenValidity(Duration accessTokenValidity) {
		this.accessTokenValidity = accessTokenValidity;
	}
}
