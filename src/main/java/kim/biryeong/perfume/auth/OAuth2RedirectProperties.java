package kim.biryeong.perfume.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth2")
public class OAuth2RedirectProperties {

	private String successRedirectUri = "http://localhost:3000/oauth2/success";

	private String failureRedirectUri = "http://localhost:3000/oauth2/failure";

	public String getSuccessRedirectUri() {
		return successRedirectUri;
	}

	public void setSuccessRedirectUri(String successRedirectUri) {
		this.successRedirectUri = successRedirectUri;
	}

	public String getFailureRedirectUri() {
		return failureRedirectUri;
	}

	public void setFailureRedirectUri(String failureRedirectUri) {
		this.failureRedirectUri = failureRedirectUri;
	}
}
