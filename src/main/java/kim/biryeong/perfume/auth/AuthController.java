package kim.biryeong.perfume.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kim.biryeong.perfume.audit.AuditEventType;
import kim.biryeong.perfume.audit.AuditLogRequestAttributes;
import kim.biryeong.perfume.auth.cookie.AuthCookieFactory;
import kim.biryeong.perfume.auth.dto.AuthUserResponse;
import kim.biryeong.perfume.auth.dto.CompleteProfileRequest;
import kim.biryeong.perfume.auth.dto.CsrfTokenResponse;
import kim.biryeong.perfume.auth.dto.LoginRequest;
import kim.biryeong.perfume.auth.dto.SignupRequest;
import kim.biryeong.perfume.auth.jwt.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원가입, 로그인, 내 정보 조회, 프로필 완성, 로그아웃을 제공하는 로컬 인증 API 컨트롤러입니다.
 *
 * <p>회원가입과 로그인에 성공하면 인증 API 요청에 사용할 액세스 토큰 쿠키를 응답에 설정합니다. 인증이 필요한 엔드포인트는 JWT subject에서 현재 사용자 식별자를
 * 확인합니다.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final JwtService jwtService;
  private final AuthCookieFactory authCookieFactory;

  public AuthController(
      AuthService authService, JwtService jwtService, AuthCookieFactory authCookieFactory) {
    this.authService = authService;
    this.jwtService = jwtService;
    this.authCookieFactory = authCookieFactory;
  }

  /**
   * 로컬 계정을 등록하고 인증된 사용자 응답을 반환합니다.
   *
   * @param request 검증된 회원가입 요청 본문
   * @param response 액세스 토큰 쿠키를 추가할 서블릿 응답
   * @return 생성된 사용자 정보와 액세스 토큰 쿠키를 포함한 HTTP 200 응답
   */
  @PostMapping("/signup")
  public ResponseEntity<AuthUserResponse> signup(
      @Valid @RequestBody SignupRequest requestBody,
      HttpServletRequest request,
      HttpServletResponse response) {
    kim.biryeong.perfume.user.domain.User user = authService.signup(requestBody);
    AuditLogRequestAttributes.mark(request, AuditEventType.AUTH_SIGNUP, user.getUserId());
    return authenticatedResponse(user, response);
  }

  /**
   * 로컬 계정을 인증하고 인증된 사용자 응답을 반환합니다.
   *
   * @param request 검증된 로그인 요청 본문
   * @param response 액세스 토큰 쿠키를 추가할 서블릿 응답
   * @return 인증된 사용자 정보와 액세스 토큰 쿠키를 포함한 HTTP 200 응답
   */
  @PostMapping("/login")
  public ResponseEntity<AuthUserResponse> login(
      @Valid @RequestBody LoginRequest requestBody,
      HttpServletRequest request,
      HttpServletResponse response) {
    kim.biryeong.perfume.user.domain.User user = authService.login(requestBody);
    AuditLogRequestAttributes.mark(request, AuditEventType.AUTH_LOGIN, user.getUserId());
    return authenticatedResponse(user, response);
  }

  /**
   * 현재 JWT 인증이 나타내는 사용자 정보를 반환합니다.
   *
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @return 현재 인증된 사용자 프로필
   */
  @GetMapping("/me")
  public AuthUserResponse me(Authentication authentication) {
    return AuthUserResponse.from(
        authService.getCurrentUser(AuthenticatedUserIds.currentUserId(authentication)));
  }

  /**
   * 현재 JWT 쿠키 인증에 연결할 CSRF 토큰을 새로 발급합니다.
   *
   * <p>프론트엔드가 API와 다른 사이트에서 호스팅되는 경우 API 도메인의 쿠키를 직접 읽을 수 없으므로, 이 응답 본문의 {@code csrfToken} 값을 이후 상태
   * 변경 요청의 {@code X-XSRF-TOKEN} 헤더로 전송해야 합니다.
   *
   * @param response CSRF 쿠키를 추가할 서블릿 응답
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @return 새 CSRF 토큰과 같은 값을 담은 {@code XSRF-TOKEN} 쿠키
   */
  @GetMapping("/csrf")
  public CsrfTokenResponse csrf(HttpServletResponse response, Authentication authentication) {
    AuthenticatedUserIds.currentUserId(authentication);
    String csrfToken = authCookieFactory.createCsrfTokenValue();
    response.addHeader(
        HttpHeaders.SET_COOKIE, authCookieFactory.createCsrfTokenCookie(csrfToken).toString());
    return new CsrfTokenResponse(csrfToken);
  }

  /**
   * 현재 인증 사용자의 프로필 필드를 갱신하고 프로필 완성 상태로 전환합니다.
   *
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @param request 검증된 프로필 완성 요청 본문
   * @return 갱신된 현재 사용자 프로필
   */
  @PatchMapping("/me/profile")
  public AuthUserResponse completeProfile(
      HttpServletRequest servletRequest,
      Authentication authentication,
      @Valid @RequestBody CompleteProfileRequest request) {
    Integer userId = AuthenticatedUserIds.currentUserId(authentication);
    AuditLogRequestAttributes.mark(servletRequest, AuditEventType.AUTH_PROFILE_UPDATE, userId);
    return AuthUserResponse.from(authService.completeProfile(userId, request));
  }

  /**
   * 기존 서버 세션을 정리하고 액세스 토큰 쿠키를 만료시킵니다.
   *
   * @param request 기존 세션을 조회할 서블릿 요청
   * @param response 만료 쿠키를 추가할 서블릿 응답
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @return 로그아웃 처리가 완료되면 HTTP 204 응답
   */
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    try {
      Integer userId = AuthenticatedUserIds.currentUserId(authentication);
      AuditLogRequestAttributes.mark(request, AuditEventType.AUTH_LOGOUT, userId);
    } finally {
      expireAuthCookies(request, response);
    }
    return ResponseEntity.noContent().build();
  }

  private void expireAuthCookies(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    response.addHeader(
        HttpHeaders.SET_COOKIE, authCookieFactory.expireAccessTokenCookie().toString());
    response.addHeader(
        HttpHeaders.SET_COOKIE, authCookieFactory.expireCsrfTokenCookie().toString());
  }

  private ResponseEntity<AuthUserResponse> authenticatedResponse(
      kim.biryeong.perfume.user.domain.User user, HttpServletResponse response) {
    String accessToken = jwtService.issueAccessToken(user);
    response.addHeader(
        HttpHeaders.SET_COOKIE, authCookieFactory.createAccessTokenCookie(accessToken).toString());
    response.addHeader(
        HttpHeaders.SET_COOKIE, authCookieFactory.createCsrfTokenCookie().toString());
    return ResponseEntity.ok(AuthUserResponse.from(user));
  }
}
