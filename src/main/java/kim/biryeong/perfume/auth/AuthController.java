package kim.biryeong.perfume.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
			@Valid @RequestBody SignupRequest request, HttpServletResponse response) {
		return authenticatedResponse(authService.signup(request), response);
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
			@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
		return authenticatedResponse(authService.login(request), response);
	}

	/**
	 * 현재 JWT 인증이 나타내는 사용자 정보를 반환합니다.
	 *
	 * @param authentication JWT subject를 포함한 Spring Security 인증 객체
	 * @return 현재 인증된 사용자 프로필
	 */
	@GetMapping("/me")
	public AuthUserResponse me(Authentication authentication) {
		return AuthUserResponse.from(authService.getCurrentUser(currentUserId(authentication)));
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
			Authentication authentication, @Valid @RequestBody CompleteProfileRequest request) {
		return AuthUserResponse.from(
				authService.completeProfile(currentUserId(authentication), request));
	}

	/**
	 * 기존 서버 세션을 정리하고 액세스 토큰 쿠키를 만료시킵니다.
	 *
	 * @param request 기존 세션을 조회할 서블릿 요청
	 * @param response 만료 쿠키를 추가할 서블릿 응답
	 * @return 로그아웃 처리가 완료되면 HTTP 204 응답
	 */
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		response.addHeader(
				HttpHeaders.SET_COOKIE, authCookieFactory.expireAccessTokenCookie().toString());
		return ResponseEntity.noContent().build();
	}

	private ResponseEntity<AuthUserResponse> authenticatedResponse(
			kim.biryeong.perfume.domain.User user, HttpServletResponse response) {
		String accessToken = jwtService.issueAccessToken(user);
		response.addHeader(
				HttpHeaders.SET_COOKIE,
				authCookieFactory.createAccessTokenCookie(accessToken).toString());
		return ResponseEntity.ok(AuthUserResponse.from(user));
	}

	private Integer currentUserId(Authentication authentication) {
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

	private ResponseStatusException unauthorized() {
		return new ResponseStatusException(
				org.springframework.http.HttpStatus.UNAUTHORIZED, "JWT authentication is required");
	}
}
