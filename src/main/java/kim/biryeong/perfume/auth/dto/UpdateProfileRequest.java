package kim.biryeong.perfume.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * {@code PATCH /api/auth/me} 요청 본문입니다.
 *
 * <p>현재 인증 사용자의 닉네임, 휴대폰 번호를 수정합니다.
 *
 * @param nickname 공개되는 고유 닉네임
 * @param phoneNumber 사용자 전화번호이며 최대 15자까지 허용됩니다
 */
public record UpdateProfileRequest(
    @NotBlank(message = "닉네임은 필수입니다.") @Size(max = 24, message = "입력한 값을 다시 확인해주세요.")
        String nickname,
    @NotBlank(message = "휴대폰 번호는 필수입니다.") @Size(max = 15, message = "입력한 값을 다시 확인해주세요.")
        String phoneNumber) {}
