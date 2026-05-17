package kim.biryeong.perfume.auth.oauth;

import java.util.Locale;
import java.util.Map;
import kim.biryeong.perfume.domain.OAuthProvider;
import kim.biryeong.perfume.user.domain.User;
import kim.biryeong.perfume.user.repository.UserRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OAuthAccountService {

  private static final int MAX_NAME_LENGTH = 24;

  private final UserRepository userRepository;

  public OAuthAccountService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public User findOrCreateUser(String registrationId, OAuth2User oauth2User) {
    OAuthProfile profile = oauthProfile(registrationId, oauth2User.getAttributes());
    return userRepository
        .findByOauthProviderAndOauthProviderId(profile.provider(), profile.providerId())
        .orElseGet(() -> findByEmailOrCreate(profile));
  }

  private User findByEmailOrCreate(OAuthProfile profile) {
    return userRepository
        .findByEmail(profile.email())
        .map(user -> connectOAuthAccount(user, profile))
        .orElseGet(() -> createPendingOAuthUser(profile));
  }

  private User connectOAuthAccount(User user, OAuthProfile profile) {
    if (user.getPassword() != null) {
      throw oauthFailure("oauth_account_conflict");
    }
    if (!profile.emailTrustedForLinking()) {
      throw oauthFailure("oauth_account_conflict");
    }
    if (user.getOauthProviderId() != null
        && !(profile.provider().equals(user.getOauthProvider())
            && profile.providerId().equals(user.getOauthProviderId()))) {
      throw oauthFailure("oauth_account_conflict");
    }
    if (user.getOauthProvider() != null && user.getOauthProviderId() == null) {
      throw oauthFailure("oauth_account_conflict");
    }
    user.setOauthProvider(profile.provider());
    user.setOauthProviderId(profile.providerId());
    return user;
  }

  private User createPendingOAuthUser(OAuthProfile profile) {
    User user = new User();
    user.setEmail(profile.email());
    user.setName(displayName(profile.displayName(), profile.email()));
    user.setOauthProvider(profile.provider());
    user.setOauthProviderId(profile.providerId());
    user.setProfileCompleted(false);
    return userRepository.save(user);
  }

  private OAuthProfile oauthProfile(String registrationId, Map<String, Object> attributes) {
    String normalizedRegistrationId =
        registrationId == null ? "" : registrationId.toLowerCase(Locale.ROOT);
    return switch (normalizedRegistrationId) {
      case "google" -> googleProfile(attributes);
      case "naver" -> naverProfile(attributes);
      default -> throw oauthFailure("unsupported_oauth_provider");
    };
  }

  private OAuthProfile googleProfile(Map<String, Object> attributes) {
    String providerId = requiredString(attributes, "sub", "google");
    String email = requiredString(attributes, "email", "google");
    if (!Boolean.TRUE.equals(attributes.get("email_verified"))) {
      throw oauthFailure("email_not_verified");
    }
    return new OAuthProfile(
        OAuthProvider.GOOGLE, providerId, email, optionalString(attributes, "name"), true);
  }

  private OAuthProfile naverProfile(Map<String, Object> attributes) {
    Map<String, Object> response = requiredMap(attributes, "response", "naver");
    String providerId = requiredString(response, "id", "naver_response");
    String email = requiredString(response, "email", "naver_response");
    String displayName = optionalString(response, "name");
    if (!StringUtils.hasText(displayName)) {
      displayName = optionalString(response, "nickname");
    }
    return new OAuthProfile(OAuthProvider.NAVER, providerId, email, displayName, false);
  }

  private String displayName(String name, String email) {
    String displayName = StringUtils.hasText(name) ? name : email;
    return displayName.length() <= MAX_NAME_LENGTH
        ? displayName
        : displayName.substring(0, MAX_NAME_LENGTH);
  }

  private String requiredString(Map<String, Object> attributes, String key, String provider) {
    String value = optionalString(attributes, key);
    if (StringUtils.hasText(value)) {
      return value;
    }
    throw oauthFailure("missing_" + provider + "_" + key);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> requiredMap(
      Map<String, Object> attributes, String key, String provider) {
    Object value = attributes.get(key);
    if (value instanceof Map<?, ?> mapValue) {
      return (Map<String, Object>) mapValue;
    }
    throw oauthFailure("missing_" + provider + "_" + key);
  }

  private String optionalString(Map<String, Object> attributes, String key) {
    Object value = attributes.get(key);
    return value instanceof String stringValue && StringUtils.hasText(stringValue)
        ? stringValue
        : null;
  }

  private OAuth2AuthenticationException oauthFailure(String code) {
    return new OAuth2AuthenticationException(new OAuth2Error(code));
  }

  private record OAuthProfile(
      OAuthProvider provider,
      String providerId,
      String email,
      String displayName,
      boolean emailTrustedForLinking) {}
}
