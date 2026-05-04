package kim.biryeong.perfume.auth;

import java.time.LocalDate;
import kim.biryeong.perfume.domain.OAuthProvider;
import kim.biryeong.perfume.domain.User;

/**
 * 인증 API가 반환하는 사용자 응답입니다.
 *
 * <p>회원가입, 로그인, 내 정보 조회, 프로필 갱신 이후 클라이언트에 필요한 프로필과 OAuth 연결 상태만 노출합니다. 비밀번호와 영속성 전용 필드는 포함하지 않습니다.
 *
 * @param userId JWT subject로 사용하는 내부 사용자 식별자
 * @param email 사용자 이메일 주소
 * @param name 사용자 실명 또는 표시 이름
 * @param nickname 사용자 공개 닉네임
 * @param gender 현재 API 계약의 한 글자 성별 코드
 * @param birthDate 사용자 생년월일
 * @param phoneNumber 사용자 전화번호
 * @param oauthProvider 연결된 OAuth 제공자이며 로컬 계정이면 {@code null}
 * @param profileCompleted 모든 필수 프로필 필드가 존재하는지 여부
 */
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
