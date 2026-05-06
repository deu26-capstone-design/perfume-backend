package kim.biryeong.perfume.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import kim.biryeong.perfume.auth.dto.CompleteProfileRequest;
import kim.biryeong.perfume.auth.oauth.OAuthAccountService;
import kim.biryeong.perfume.domain.OAuthProvider;
import kim.biryeong.perfume.user.domain.User;
import kim.biryeong.perfume.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@DataJpaTest
@Import({
  OAuthAccountService.class,
  AuthService.class,
  GoogleOAuthUserServiceTest.PasswordEncoderConfig.class
})
class GoogleOAuthUserServiceTest {

  @Autowired private OAuthAccountService oauthAccountService;

  @Autowired private AuthService authService;

  @Autowired private UserRepository userRepository;

  @Test
  void firstGoogleLoginCreatesPendingUser() {
    User user =
        oauthAccountService.findOrCreateUser(
            "google", googleUser("google-sub-1", "new@example.com", true, "Google User"));

    assertThat(user.getUserId()).isNotNull();
    assertThat(user.getEmail()).isEqualTo("new@example.com");
    assertThat(user.getOauthProvider()).isEqualTo(OAuthProvider.GOOGLE);
    assertThat(user.getOauthProviderId()).isEqualTo("google-sub-1");
    assertThat(user.isProfileCompleted()).isFalse();
    assertThat(user.getPassword()).isNull();
    assertThat(user.getNickname()).isNull();
    assertThat(user.getGender()).isNull();
    assertThat(user.getBirthDate()).isNull();
    assertThat(user.getPhoneNumber()).isNull();
  }

  @Test
  void googleLoginConnectsExistingEmailUser() {
    User existing = completedUser("existing@example.com", "existing");
    userRepository.saveAndFlush(existing);

    User user =
        oauthAccountService.findOrCreateUser(
            "google", googleUser("google-sub-2", "existing@example.com", true, "Existing"));

    assertThat(user.getUserId()).isEqualTo(existing.getUserId());
    assertThat(user.getOauthProvider()).isEqualTo(OAuthProvider.GOOGLE);
    assertThat(user.getOauthProviderId()).isEqualTo("google-sub-2");
    assertThat(user.isProfileCompleted()).isTrue();
  }

  @Test
  void googleLoginRejectsUnverifiedEmail() {
    assertThatThrownBy(
            () ->
                oauthAccountService.findOrCreateUser(
                    "google",
                    googleUser("google-sub-3", "unverified@example.com", false, "Unverified")))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .extracting("error.errorCode")
        .isEqualTo("email_not_verified");

    assertThat(userRepository.count()).isZero();
  }

  @Test
  void googleLoginRejectsProviderIdConflictForSameEmail() {
    User existing = completedUser("conflict@example.com", "conflict");
    existing.setOauthProvider(OAuthProvider.GOOGLE);
    existing.setOauthProviderId("existing-google-sub");
    userRepository.saveAndFlush(existing);

    assertThatThrownBy(
            () ->
                oauthAccountService.findOrCreateUser(
                    "google",
                    googleUser("new-google-sub", "conflict@example.com", true, "Conflict")))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .extracting("error.errorCode")
        .isEqualTo("oauth_account_conflict");
  }

  @Test
  void firstNaverLoginCreatesPendingUser() {
    User user =
        oauthAccountService.findOrCreateUser(
            "naver",
            naverUser(
                Map.of(
                    "id", "naver-id-1",
                    "email", "naver@example.com",
                    "name", "Naver User",
                    "nickname", "NaverNick",
                    "gender", "F",
                    "birthyear", "1999",
                    "birthday", "05-01",
                    "mobile", "010-1111-2222")));

    assertThat(user.getUserId()).isNotNull();
    assertThat(user.getEmail()).isEqualTo("naver@example.com");
    assertThat(user.getName()).isEqualTo("Naver User");
    assertThat(user.getOauthProvider()).isEqualTo(OAuthProvider.NAVER);
    assertThat(user.getOauthProviderId()).isEqualTo("naver-id-1");
    assertThat(user.isProfileCompleted()).isFalse();
    assertThat(user.getPassword()).isNull();
    assertThat(user.getNickname()).isNull();
    assertThat(user.getGender()).isNull();
    assertThat(user.getBirthDate()).isNull();
    assertThat(user.getPhoneNumber()).isNull();
  }

  @Test
  void naverLoginRejectsExistingEmailUserWithoutVerifiedEmailAssurance() {
    User existing = completedUser("naver-existing@example.com", "naver_existing");
    userRepository.saveAndFlush(existing);

    assertThatThrownBy(
            () ->
                oauthAccountService.findOrCreateUser(
                    "naver",
                    naverUser(
                        Map.of(
                            "id", "naver-id-2",
                            "email", "naver-existing@example.com",
                            "name", "Naver Existing"))))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .extracting("error.errorCode")
        .isEqualTo("oauth_account_conflict");
  }

  @Test
  void naverLoginRejectsDifferentProviderAlreadyConnectedToSameEmail() {
    User existing = completedUser("linked@example.com", "linked");
    existing.setOauthProvider(OAuthProvider.GOOGLE);
    existing.setOauthProviderId("google-sub-linked");
    userRepository.saveAndFlush(existing);

    assertThatThrownBy(
            () ->
                oauthAccountService.findOrCreateUser(
                    "naver",
                    naverUser(
                        Map.of(
                            "id", "naver-id-3",
                            "email", "linked@example.com",
                            "name", "Linked"))))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .extracting("error.errorCode")
        .isEqualTo("oauth_account_conflict");
  }

  @Test
  void naverLoginRejectsMissingResponse() {
    OAuth2User oauth2User =
        new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_USER")), Map.of("other", "value"), "other");

    assertThatThrownBy(() -> oauthAccountService.findOrCreateUser("naver", oauth2User))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .extracting("error.errorCode")
        .isEqualTo("missing_naver_response");
  }

  @Test
  void naverLoginRejectsMissingResponseEmail() {
    assertThatThrownBy(
            () ->
                oauthAccountService.findOrCreateUser(
                    "naver",
                    naverUser(
                        Map.of(
                            "id", "naver-id-without-email",
                            "name", "No Email"))))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .extracting("error.errorCode")
        .isEqualTo("missing_naver_response_email");
  }

  @Test
  void completeProfileRejectsDuplicateNickname() {
    User pending =
        oauthAccountService.findOrCreateUser(
            "google", googleUser("google-sub-4", "pending@example.com", true, "Pending"));
    userRepository.saveAndFlush(completedUser("taken@example.com", "taken"));

    CompleteProfileRequest request =
        new CompleteProfileRequest(
            "Pending User", "taken", "M", LocalDate.of(2000, 1, 1), "01012345678");

    assertThatThrownBy(() -> authService.completeProfile(pending.getUserId(), request))
        .isInstanceOf(AuthConflictException.class)
        .hasMessage("nickname already exists");
  }

  @Test
  void completeProfileMarksUserAsCompleted() {
    User pending =
        oauthAccountService.findOrCreateUser(
            "google", googleUser("google-sub-5", "profile@example.com", true, "Profile"));
    CompleteProfileRequest request =
        new CompleteProfileRequest(
            "Profile User", "profile", "F", LocalDate.of(1999, 5, 1), "01098765432");

    User user = authService.completeProfile(pending.getUserId(), request);

    assertThat(user.isProfileCompleted()).isTrue();
    assertThat(user.getName()).isEqualTo("Profile User");
    assertThat(user.getNickname()).isEqualTo("profile");
    assertThat(user.getGender()).isEqualTo("F");
    assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1999, 5, 1));
    assertThat(user.getPhoneNumber()).isEqualTo("01098765432");
  }

  private OAuth2User googleUser(String subject, String email, boolean emailVerified, String name) {
    return new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        Map.of(
            "sub", subject,
            "email", email,
            "email_verified", emailVerified,
            "name", name),
        "sub");
  }

  private OAuth2User naverUser(Map<String, Object> response) {
    return new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")), Map.of("response", response), "response");
  }

  private User completedUser(String email, String nickname) {
    User user = new User();
    user.setEmail(email);
    user.setPassword("encoded-password");
    user.setName("Local User");
    user.setNickname(nickname);
    user.setGender("M");
    user.setBirthDate(LocalDate.of(1990, 1, 1));
    user.setPhoneNumber("01000000000");
    user.setProfileCompleted(true);
    return user;
  }

  static class PasswordEncoderConfig {

    @org.springframework.context.annotation.Bean
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
      return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
  }
}
