package kim.biryeong.perfume.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * {@code POST /api/auth/login} 요청 본문입니다.
 *
 * <p>로컬 비밀번호 기반 계정을 인증하는 자격 증명입니다. OAuth 전용 계정은 로컬 비밀번호가 없으므로 이 로그인 흐름에서 거부됩니다.
 *
 * @param email 로그인 이메일 주소
 * @param password 저장된 비밀번호 해시와 비교할 원문 비밀번호
 */
public record LoginRequest(
    @NotBlank @Email @Size(max = 100) String email, @NotBlank @Size(max = 72) String password) {}
