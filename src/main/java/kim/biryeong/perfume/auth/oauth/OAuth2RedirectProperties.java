package kim.biryeong.perfume.auth.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth2")
public class OAuth2RedirectProperties {

  private String successRedirectUri = "https://thescentlab.vercel.app/oauth2/success";

  private String failureRedirectUri = "https://thescentlab.vercel.app/oauth2/failure";

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
