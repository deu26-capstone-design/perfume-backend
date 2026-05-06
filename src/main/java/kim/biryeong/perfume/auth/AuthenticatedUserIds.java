package kim.biryeong.perfume.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

/** JWT 인증 객체에서 현재 사용자 식별자를 추출하는 인증 계층 계약이다. */
public final class AuthenticatedUserIds {

  private AuthenticatedUserIds() {}

  public static Integer currentUserId(Authentication authentication) {
    if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
      Jwt jwt = jwtAuthentication.getToken();
      try {
        return Integer.valueOf(jwt.getSubject());
      } catch (NumberFormatException exception) {
        throw unauthorized();
      }
    }
    throw unauthorized();
  }

  private static ResponseStatusException unauthorized() {
    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT authentication is required");
  }
}
