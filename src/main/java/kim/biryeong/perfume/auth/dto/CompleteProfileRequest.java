package kim.biryeong.perfume.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * {@code PATCH /api/auth/me/profile} 요청 본문입니다.
 *
 * <p>현재 인증 사용자의 필수 프로필 필드를 제공하며, 처리에 성공하면 프로필 완성 상태가 됩니다.
 *
 * @param name 사용자 실명 또는 표시 이름
 * @param nickname 공개되는 고유 닉네임
 * @param gender 현재 API 계약에서 요구하는 한 글자 성별 코드
 * @param birthDate 사용자 생년월일이며 과거 날짜여야 합니다
 * @param phoneNumber 사용자 전화번호이며 최대 15자까지 허용됩니다
 */
public record CompleteProfileRequest(
		@NotBlank @Size(max = 24) String name,
		@NotBlank @Size(max = 24) String nickname,
		@NotBlank @Size(max = 1) String gender,
		@NotNull @Past LocalDate birthDate,
		@NotBlank @Size(max = 15) String phoneNumber) {}
