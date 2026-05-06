package kim.biryeong.perfume.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.time.LocalDate;
import kim.biryeong.perfume.auth.jwt.JwtService;
import kim.biryeong.perfume.user.domain.User;
import kim.biryeong.perfume.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerSecurityTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserRepository userRepository;

  @Autowired private JwtService jwtService;

  @Autowired private JwtEncoder jwtEncoder;

  private User user;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    user = userRepository.save(completedUser());
  }

  @Test
  void signupIssuesAuthCookieWithoutCsrfToken() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
										{
										"email": "signup@example.com",
										"password": "secret-password",
										"name": "Signup User",
										"nickname": "signup",
										"gender": "F",
										"birthDate": "1999-05-01",
										"phoneNumber": "01012345678"
										}
										"""))
        .andExpect(status().isOk())
        .andExpect(cookie().exists("PERFUME_ACCESS_TOKEN"))
        .andExpect(cookie().httpOnly("PERFUME_ACCESS_TOKEN", true))
        .andExpect(cookie().exists("XSRF-TOKEN"))
        .andExpect(cookie().httpOnly("XSRF-TOKEN", false))
        .andExpect(jsonPath("$.email").value("signup@example.com"))
        .andExpect(jsonPath("$.profileCompleted").value(true));
  }

  @Test
  void signupRejectsDuplicateEmail() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
										{
										"email": "security@example.com",
										"password": "secret-password",
										"name": "Signup User",
										"nickname": "new_signup",
										"gender": "F",
										"birthDate": "1999-05-01",
										"phoneNumber": "01012345678"
										}
										"""))
        .andExpect(status().isConflict());
  }

  @Test
  void signupRejectsValidationFailure() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
										{
										"email": "invalid@example.com",
										"password": "short",
										"name": "Invalid User",
										"nickname": "invalid",
										"gender": "F",
										"birthDate": "1999-05-01",
										"phoneNumber": "01012345678"
										}
										"""))
        .andExpect(status().isBadRequest());
  }

  @Test
  void loginIssuesAuthCookieWithoutCsrfToken() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
										{
										"email": "security@example.com",
										"password": "secret-password"
										}
										"""))
        .andExpect(status().isOk())
        .andExpect(cookie().exists("PERFUME_ACCESS_TOKEN"))
        .andExpect(cookie().httpOnly("PERFUME_ACCESS_TOKEN", true))
        .andExpect(cookie().exists("XSRF-TOKEN"))
        .andExpect(cookie().httpOnly("XSRF-TOKEN", false))
        .andExpect(jsonPath("$.email").value("security@example.com"));
  }

  @Test
  void loginRejectsInvalidPassword() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
										{
										"email": "security@example.com",
										"password": "wrong-password"
										}
										"""))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void meReturnsCurrentUser() throws Exception {
    mockMvc
        .perform(get("/api/auth/me").cookie(authCookie()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("security@example.com"))
        .andExpect(jsonPath("$.nickname").value("security"))
        .andExpect(jsonPath("$.profileCompleted").value(true));
  }

  @Test
  void meRejectsMissingAuthentication() throws Exception {
    mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void meRejectsMalformedJwtSubject() throws Exception {
    mockMvc
        .perform(get("/api/auth/me").cookie(invalidSubjectAuthCookie()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void csrfIssuesTokenAndReadableCookieForAuthenticatedCookieRequest() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/auth/csrf").cookie(authCookie()))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("XSRF-TOKEN"))
            .andExpect(cookie().httpOnly("XSRF-TOKEN", false))
            .andExpect(jsonPath("$.csrfToken").isString())
            .andReturn();

    Cookie csrfCookie = result.getResponse().getCookie("XSRF-TOKEN");
    assertThat(csrfCookie).isNotNull();
    assertThat(result.getResponse().getContentAsString())
        .contains("\"csrfToken\":\"" + csrfCookie.getValue() + "\"");
  }

  @Test
  void csrfRejectsMissingAuthentication() throws Exception {
    mockMvc.perform(get("/api/auth/csrf")).andExpect(status().isUnauthorized());
  }

  @Test
  void wishlistReturnsCurrentUsersItemsFromJwtSubject() throws Exception {
    mockMvc
        .perform(get("/api/wishlist").cookie(authCookie()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void wishlistRejectsMalformedJwtSubject() throws Exception {
    mockMvc
        .perform(get("/api/wishlist").cookie(invalidSubjectAuthCookie()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void wishlistCreateRejectsMissingAuthentication() throws Exception {
    mockMvc.perform(post("/api/wishlist/999999")).andExpect(status().isUnauthorized());
  }

  @Test
  void wishlistCreateUsesJwtSubjectWithoutUserIdQueryParameter() throws Exception {
    mockMvc
        .perform(
            post("/api/wishlist/999999")
                .cookie(authCookie(), new Cookie("XSRF-TOKEN", "csrf-token"))
                .header("X-XSRF-TOKEN", "csrf-token"))
        .andExpect(status().isNotFound());
  }

  @Test
  void wishlistDeleteRejectsMissingAuthentication() throws Exception {
    mockMvc.perform(delete("/api/wishlist/999999")).andExpect(status().isUnauthorized());
  }

  @Test
  void reviewCreateRejectsMissingAuthentication() throws Exception {
    mockMvc
        .perform(
            post("/api/perfumes/999999/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                    "satisfaction": 5,
                    "longevity": 2,
                    "seasons": ["봄"],
                    "scents": ["꽃 향"],
                    "comment": "좋아요.",
                    "disclaimerAgreed": true
                    }
                    """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void reviewCreateUsesJwtSubjectWithoutUserIdQueryParameter() throws Exception {
    mockMvc
        .perform(
            post("/api/perfumes/999999/reviews")
                .cookie(authCookie(), new Cookie("XSRF-TOKEN", "csrf-token"))
                .header("X-XSRF-TOKEN", "csrf-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
										{
										"satisfaction": 5,
										"longevity": 2,
										"seasons": ["봄"],
										"scents": ["꽃 향"],
										"comment": "좋아요.",
										"disclaimerAgreed": true
										}
										"""))
        .andExpect(status().isNotFound());
  }

  @Test
  void reviewCreateAllowsBearerAuthorizationWithoutCsrfToken() throws Exception {
    mockMvc
        .perform(
            post("/api/perfumes/999999/reviews")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.issueAccessToken(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                    "satisfaction": 5,
                    "longevity": 2,
                    "seasons": ["봄"],
                    "scents": ["꽃 향"],
                    "comment": "좋아요.",
                    "disclaimerAgreed": true
                    }
                    """))
        .andExpect(status().isNotFound());
  }

  @Test
  void profileCompletionRequiresCsrfToken() throws Exception {
    mockMvc
        .perform(
            patch("/api/auth/me/profile")
                .cookie(authCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
										{
										"name": "No Csrf",
										"nickname": "no_csrf",
										"gender": "M",
										"birthDate": "2000-01-01",
										"phoneNumber": "01012345678"
										}
										"""))
        .andExpect(status().isForbidden());
  }

  @Test
  void profileCompletionRejectsMissingAuthentication() throws Exception {
    mockMvc
        .perform(
            patch("/api/auth/me/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                    "name": "No Auth",
                    "nickname": "no_auth",
                    "gender": "M",
                    "birthDate": "2000-01-01",
                    "phoneNumber": "01012345678"
                    }
                    """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void profileCompletionUpdatesCurrentUserWithCsrfToken() throws Exception {
    mockMvc
        .perform(
            patch("/api/auth/me/profile")
                .cookie(authCookie(), new Cookie("XSRF-TOKEN", "csrf-token"))
                .header("X-XSRF-TOKEN", "csrf-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
										{
										"name": "Updated User",
										"nickname": "updated",
										"gender": "F",
										"birthDate": "2000-01-01",
										"phoneNumber": "01012345678"
										}
										"""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated User"))
        .andExpect(jsonPath("$.nickname").value("updated"))
        .andExpect(jsonPath("$.gender").value("F"))
        .andExpect(jsonPath("$.birthDate").value("2000-01-01"))
        .andExpect(jsonPath("$.phoneNumber").value("01012345678"))
        .andExpect(jsonPath("$.profileCompleted").value(true));
  }

  @Test
  void profileCompletionRejectsDuplicateNickname() throws Exception {
    userRepository.saveAndFlush(completedUser("taken@example.com", "taken"));

    mockMvc
        .perform(
            patch("/api/auth/me/profile")
                .cookie(authCookie(), new Cookie("XSRF-TOKEN", "csrf-token"))
                .header("X-XSRF-TOKEN", "csrf-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
										{
										"name": "Taken User",
										"nickname": "taken",
										"gender": "M",
										"birthDate": "2000-01-01",
										"phoneNumber": "01012345678"
										}
										"""))
        .andExpect(status().isConflict());
  }

  @Test
  void logoutExpiresAuthCookie() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/logout")
                .cookie(authCookie(), new Cookie("XSRF-TOKEN", "csrf-token"))
                .header("X-XSRF-TOKEN", "csrf-token"))
        .andExpect(status().isNoContent())
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("PERFUME_ACCESS_TOKEN=")))
        .andExpect(cookie().maxAge("PERFUME_ACCESS_TOKEN", 0))
        .andExpect(cookie().maxAge("XSRF-TOKEN", 0));
  }

  @Test
  void logoutRejectsMissingAuthentication() throws Exception {
    mockMvc.perform(post("/api/auth/logout")).andExpect(status().isUnauthorized());
  }

  @Test
  void logoutRejectsNonJwtAuthentication() throws Exception {
    mockMvc
        .perform(post("/api/auth/logout").with(user("session-user")))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("PERFUME_ACCESS_TOKEN=")))
        .andExpect(cookie().maxAge("PERFUME_ACCESS_TOKEN", 0))
        .andExpect(cookie().maxAge("XSRF-TOKEN", 0));
  }

  @Test
  void logoutRejectsMalformedJwtSubjectAndExpiresAuthCookies() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/logout")
                .cookie(invalidSubjectAuthCookie(), new Cookie("XSRF-TOKEN", "csrf-token"))
                .header("X-XSRF-TOKEN", "csrf-token"))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("PERFUME_ACCESS_TOKEN=")))
        .andExpect(cookie().maxAge("PERFUME_ACCESS_TOKEN", 0))
        .andExpect(cookie().maxAge("XSRF-TOKEN", 0));
  }

  private Cookie authCookie() {
    return new Cookie("PERFUME_ACCESS_TOKEN", jwtService.issueAccessToken(user));
  }

  private Cookie invalidSubjectAuthCookie() {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("perfume-backend")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600))
            .subject("not-an-integer")
            .build();
    String token =
        jwtEncoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    return new Cookie("PERFUME_ACCESS_TOKEN", token);
  }

  private User completedUser(String email, String nickname) {
    User completedUser = completedUser();
    completedUser.setEmail(email);
    completedUser.setNickname(nickname);
    return completedUser;
  }

  private User completedUser() {
    User completedUser = new User();
    completedUser.setEmail("security@example.com");
    completedUser.setPassword(
        new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
            .encode("secret-password"));
    completedUser.setName("Security User");
    completedUser.setNickname("security");
    completedUser.setGender("M");
    completedUser.setBirthDate(LocalDate.of(1990, 1, 1));
    completedUser.setPhoneNumber("01000000000");
    completedUser.setProfileCompleted(true);
    return completedUser;
  }
}
