package kim.biryeong.perfume.auth.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import kim.biryeong.perfume.audit.AuditAuthenticatedUserFilter;
import kim.biryeong.perfume.auth.cookie.AuthCookieFactory;
import kim.biryeong.perfume.auth.cookie.AuthCookieProperties;
import kim.biryeong.perfume.auth.cookie.CookieBearerTokenResolver;
import kim.biryeong.perfume.auth.csrf.CookieCsrfEnforcementFilter;
import kim.biryeong.perfume.auth.jwt.JwtProperties;
import kim.biryeong.perfume.auth.oauth.OAuth2LoginFailureHandler;
import kim.biryeong.perfume.auth.oauth.OAuth2LoginSuccessHandler;
import kim.biryeong.perfume.auth.oauth.OAuth2RedirectProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({
  JwtProperties.class,
  AuthCookieProperties.class,
  OAuth2RedirectProperties.class,
  AppCorsProperties.class
})
public class SecurityConfig {

  private static final int MIN_HMAC_SECRET_BYTES = 32;

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      OAuth2LoginSuccessHandler successHandler,
      OAuth2LoginFailureHandler failureHandler,
      BearerTokenResolver bearerTokenResolver,
      AuthCookieFactory authCookieFactory)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/oauth2/**", "/login/oauth2/**", "/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/logout")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/accords",
                        "/api/accords/detail",
                        "/api/accords/detail/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/perfumes", "/api/perfumes/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(oauth2 -> oauth2.successHandler(successHandler).failureHandler(failureHandler))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .bearerTokenResolver(bearerTokenResolver)
                    .jwt(Customizer.withDefaults())
                    .authenticationEntryPoint(authenticationEntryPoint(authCookieFactory)));
    http.addFilterAfter(new AuditAuthenticatedUserFilter(), BearerTokenAuthenticationFilter.class);
    http.addFilterAfter(new CookieCsrfEnforcementFilter(), AuditAuthenticatedUserFilter.class);
    return http.build();
  }

  private AuthenticationEntryPoint authenticationEntryPoint(AuthCookieFactory authCookieFactory) {
    BearerTokenAuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
    return (request, response, exception) -> {
      if (isLogoutRequest(request)) {
        response.addHeader(
            HttpHeaders.SET_COOKIE, authCookieFactory.expireAccessTokenCookie().toString());
        response.addHeader(
            HttpHeaders.SET_COOKIE, authCookieFactory.expireCsrfTokenCookie().toString());
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return;
      }
      delegate.commence(request, response, exception);
    };
  }

  private boolean isLogoutRequest(HttpServletRequest request) {
    return "POST".equals(request.getMethod()) && "/api/auth/logout".equals(request.getRequestURI());
  }

  @Bean
  public BearerTokenResolver bearerTokenResolver(AuthCookieProperties cookieProperties) {
    return new CookieBearerTokenResolver(cookieProperties.getName());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
  }

  @Bean
  public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
    return NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
  }

  @Bean
  public SecretKey jwtSecretKey(JwtProperties jwtProperties) {
    byte[] secretBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
    if (secretBytes.length < MIN_HMAC_SECRET_BYTES) {
      throw new IllegalStateException("JWT_SECRET must be at least 32 bytes for HS256");
    }
    return new SecretKeySpec(secretBytes, "HmacSHA256");
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(AppCorsProperties corsProperties) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
    configuration.addAllowedHeader("*");
    configuration.addAllowedMethod("*");
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
