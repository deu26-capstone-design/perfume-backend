package kim.biryeong.perfume.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kim.biryeong.perfume.audit.AuditEventType;
import kim.biryeong.perfume.audit.AuditLogRequestAttributes;
import kim.biryeong.perfume.auth.dto.AuthUserResponse;
import kim.biryeong.perfume.auth.dto.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 마이페이지에서 사용하는 인증 사용자 전용 API를 제공한다. */
@RestController
@RequestMapping("/api/auth/me")
@RequiredArgsConstructor
@Validated
public class MyPageController {

  private final AuthService authService;

  /**
   * 현재 인증 사용자의 닉네임, 휴대폰 번호를 수정한다.
   *
   * @param authentication JWT subject를 포함한 Spring Security 인증 객체
   * @param request 검증된 프로필 수정 요청 본문
   * @return 갱신된 현재 사용자 프로필
   */
  @PatchMapping
  public AuthUserResponse updateProfile(
      HttpServletRequest servletRequest,
      Authentication authentication,
      @Valid @RequestBody UpdateProfileRequest request) {
    Integer userId = AuthenticatedUserIds.currentUserId(authentication);
    AuditLogRequestAttributes.mark(servletRequest, AuditEventType.AUTH_PROFILE_UPDATE, userId);
    return AuthUserResponse.from(authService.updateProfile(userId, request));
  }
}
