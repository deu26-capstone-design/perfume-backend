package kim.biryeong.perfume.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import kim.biryeong.perfume.audit.AuditEventType;
import kim.biryeong.perfume.audit.AuditLogService;
import kim.biryeong.perfume.audit.AuditOutcome;
import kim.biryeong.perfume.auth.cookie.AuthCookieFactory;
import kim.biryeong.perfume.auth.jwt.JwtService;
import kim.biryeong.perfume.user.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

  private final OAuthAccountService oauthAccountService;
  private final JwtService jwtService;
  private final AuthCookieFactory authCookieFactory;
  private final OAuth2RedirectProperties redirectProperties;
  private final AuthenticationFailureHandler failureHandler;
  private final AuditLogService auditLogService;

  public OAuth2LoginSuccessHandler(
      OAuthAccountService oauthAccountService,
      JwtService jwtService,
      AuthCookieFactory authCookieFactory,
      OAuth2RedirectProperties redirectProperties,
      OAuth2LoginFailureHandler failureHandler,
      AuditLogService auditLogService) {
    this.oauthAccountService = oauthAccountService;
    this.jwtService = jwtService;
    this.authCookieFactory = authCookieFactory;
    this.redirectProperties = redirectProperties;
    this.failureHandler = failureHandler;
    this.auditLogService = auditLogService;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    try {
      OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
      User user = oauthAccountService.findOrCreateUser(registrationId(authentication), oauth2User);
      String accessToken = jwtService.issueAccessToken(user);
      response.addHeader(
          HttpHeaders.SET_COOKIE,
          authCookieFactory.createAccessTokenCookie(accessToken).toString());
      response.addHeader(
          HttpHeaders.SET_COOKIE, authCookieFactory.createCsrfTokenCookie().toString());
      clearOAuthSession(request);
      recordSuccess(request, user.getUserId());
      response.sendRedirect(redirectProperties.getSuccessRedirectUri());
    } catch (OAuth2AuthenticationException exception) {
      failureHandler.onAuthenticationFailure(request, response, exception);
    }
  }

  private void recordSuccess(HttpServletRequest request, Integer userId) {
    try {
      auditLogService.record(
          request,
          AuditEventType.OAUTH_LOGIN,
          AuditOutcome.SUCCESS,
          HttpServletResponse.SC_FOUND,
          userId,
          null);
    } catch (RuntimeException exception) {
      LOGGER.warn("Failed to persist OAuth login success audit log", exception);
    }
  }

  private String registrationId(Authentication authentication) {
    if (authentication instanceof OAuth2AuthenticationToken oauth2Authentication) {
      return oauth2Authentication.getAuthorizedClientRegistrationId();
    }
    throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_oauth_provider"));
  }

  private void clearOAuthSession(HttpServletRequest request) {
    SecurityContextHolder.clearContext();
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
  }
}
