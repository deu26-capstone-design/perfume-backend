package kim.biryeong.perfume.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kim.biryeong.perfume.audit.AuditEventType;
import kim.biryeong.perfume.audit.AuditLogService;
import kim.biryeong.perfume.audit.AuditOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2LoginFailureHandler.class);

  private final OAuth2RedirectProperties redirectProperties;
  private final AuditLogService auditLogService;

  public OAuth2LoginFailureHandler(
      OAuth2RedirectProperties redirectProperties, AuditLogService auditLogService) {
    this.redirectProperties = redirectProperties;
    this.auditLogService = auditLogService;
  }

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {
    String errorCode = "oauth_login_failed";
    if (exception instanceof OAuth2AuthenticationException oauthException) {
      errorCode = oauthException.getError().getErrorCode();
    }
    recordFailure(request, errorCode);
    String redirectUri =
        UriComponentsBuilder.fromUriString(redirectProperties.getFailureRedirectUri())
            .queryParam("error", errorCode)
            .build()
            .toUriString();
    response.sendRedirect(redirectUri);
  }

  private void recordFailure(HttpServletRequest request, String errorCode) {
    try {
      auditLogService.record(
          request,
          AuditEventType.OAUTH_LOGIN,
          AuditOutcome.FAILURE,
          HttpServletResponse.SC_FOUND,
          null,
          errorCode);
    } catch (RuntimeException exception) {
      LOGGER.warn("Failed to persist OAuth login failure audit log", exception);
    }
  }
}
