package kim.biryeong.perfume.auth.oauth;

import kim.biryeong.perfume.domain.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class GoogleOAuthUserService {

	private final OAuthAccountService oauthAccountService;

	public GoogleOAuthUserService(OAuthAccountService oauthAccountService) {
		this.oauthAccountService = oauthAccountService;
	}

	public User findOrCreateUser(OAuth2User oauth2User) {
		return oauthAccountService.findOrCreateUser("google", oauth2User);
	}
}
