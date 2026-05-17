package kim.biryeong.perfume.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@SpringBootTest
class OAuth2ClientRegistrationTest {

  @Autowired private ClientRegistrationRepository clientRegistrationRepository;

  @Test
  void naverUsesClientSecretPostForTokenRequest() {
    ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("naver");

    assertThat(registration.getClientAuthenticationMethod())
        .isEqualTo(ClientAuthenticationMethod.CLIENT_SECRET_POST);
  }
}
