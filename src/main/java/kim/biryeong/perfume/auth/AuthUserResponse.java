package kim.biryeong.perfume.auth;

import java.time.LocalDate;
import kim.biryeong.perfume.domain.OAuthProvider;
import kim.biryeong.perfume.domain.User;

public record AuthUserResponse(
		Integer userId,
		String email,
		String name,
		String nickname,
		String gender,
		LocalDate birthDate,
		String phoneNumber,
		OAuthProvider oauthProvider,
		boolean profileCompleted) {

	public static AuthUserResponse from(User user) {
		return new AuthUserResponse(
				user.getUserId(),
				user.getEmail(),
				user.getName(),
				user.getNickname(),
				user.getGender(),
				user.getBirthDate(),
				user.getPhoneNumber(),
				user.getOauthProvider(),
				user.isProfileCompleted());
	}
}
