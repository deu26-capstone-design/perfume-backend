package kim.biryeong.perfume.auth.profileimage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(R2ProfileImageProperties.class)
public class ProfileImageConfig {

  @Bean
  @ConditionalOnMissingBean(ProfileImageStorage.class)
  ProfileImageStorage profileImageStorage(R2ProfileImageProperties properties) {
    return new R2ProfileImageStorage(properties);
  }
}
