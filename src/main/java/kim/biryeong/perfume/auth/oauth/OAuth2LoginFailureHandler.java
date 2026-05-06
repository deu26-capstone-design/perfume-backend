package kim.biryeong.perfume.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

  private final OAuth2RedirectProperties redirectProperties;

  public OAuth2LoginFailureHandler(OAuth2RedirectProperties redirectProperties) {
    this.redirectProperties = redirectProperties;
  }

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {
    String errorCode = "oauth_login_failed";
    if (exception instanceof OAuth2AuthenticationException oauthException) {
      errorCode = oauthException.getError().getErrorCode();
    }
    String redirectUri =
        UriComponentsBuilder.fromUriString(redirectProperties.getFailureRedirectUri())
            .queryParam("error", errorCode)
            .build()
            .toUriString();
    response.sendRedirect(redirectUri);
  }
}
