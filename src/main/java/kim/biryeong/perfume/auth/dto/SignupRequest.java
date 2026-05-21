package kim.biryeong.perfume.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * {@code POST /api/auth/signup} 요청 본문입니다.
 *
 * <p>로컬 비밀번호 기반 계정을 생성하며, 로컬 계정은 회원가입 시점에 프로필이 완성된 것으로 간주되므로 모든 프로필 필드를 포함해야 합니다.
 *
 * @param email 로그인에 사용할 고유 이메일 주소
 * @param password 해싱 전 원문 비밀번호이며 BCrypt가 지원하는 길이로 제한됩니다
 * @param name 사용자 실명 또는 표시 이름
 * @param nickname 공개되는 고유 닉네임
 * @param gender 현재 API 계약에서 요구하는 한 글자 성별 코드
 * @param birthDate 사용자 생년월일이며 과거 날짜여야 합니다
 * @param phoneNumber 사용자 전화번호이며 최대 15자까지 허용됩니다
 */
public record SignupRequest(
    @NotBlank @Email @Size(max = 100) String email,
    @NotBlank @Size(min = 10, max = 72) String password,
    @NotBlank @Size(max = 24) String name,
    @NotBlank @Size(max = 10) String nickname,
    @NotBlank @Size(max = 1) String gender,
    @NotNull @Past LocalDate birthDate,
    @NotBlank @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$") String phoneNumber) {}
