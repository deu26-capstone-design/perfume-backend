package kim.biryeong.perfume.auth.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.cookie")
public class AuthCookieProperties {

  private String name = "PERFUME_ACCESS_TOKEN";

  private boolean secure;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isSecure() {
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }
}
