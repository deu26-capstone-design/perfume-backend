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

	@PostMapping("/signup")
	public ResponseEntity<AuthUserResponse> signup(
			@Valid @RequestBody SignupRequest request, HttpServletResponse response) {
		return authenticatedResponse(authService.signup(request), response);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthUserResponse> login(
			@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
		return authenticatedResponse(authService.login(request), response);
	}

	@GetMapping("/me")
	public AuthUserResponse me(Authentication authentication) {
		return AuthUserResponse.from(authService.getCurrentUser(currentUserId(authentication)));
	}

	@PatchMapping("/me/profile")
	public AuthUserResponse completeProfile(
			Authentication authentication, @Valid @RequestBody CompleteProfileRequest request) {
		return AuthUserResponse.from(
				authService.completeProfile(currentUserId(authentication), request));
	}

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
