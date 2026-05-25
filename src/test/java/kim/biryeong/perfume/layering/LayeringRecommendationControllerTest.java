package kim.biryeong.perfume.layering;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import kim.biryeong.perfume.accord.domain.PerfumeAccord;
import kim.biryeong.perfume.accord.repository.PerfumeAccordRepository;
import kim.biryeong.perfume.auth.jwt.JwtService;
import kim.biryeong.perfume.perfume.domain.Gender;
import kim.biryeong.perfume.perfume.domain.Perfume;
import kim.biryeong.perfume.perfume.domain.PerfumeNote;
import kim.biryeong.perfume.perfume.repository.PerfumeNoteRepository;
import kim.biryeong.perfume.perfume.repository.PerfumeRepository;
import kim.biryeong.perfume.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LayeringRecommendationControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private PerfumeRepository perfumeRepository;

  @Autowired private PerfumeAccordRepository perfumeAccordRepository;

  @Autowired private PerfumeNoteRepository perfumeNoteRepository;

  @Autowired private JwtService jwtService;

  private Perfume citrusWoody;
  private Perfume floralMusky;

  @BeforeEach
  void setUp() {
    perfumeNoteRepository.deleteAll();
    perfumeAccordRepository.deleteAll();
    perfumeRepository.deleteAll();

    citrusWoody = perfumeRepository.save(perfume(1L, "Bright Wood"));
    floralMusky = perfumeRepository.save(perfume(2L, "Soft Bloom"));
    saveAccord(citrusWoody, "Citrus", 100);
    saveAccord(citrusWoody, "Woody", 80);
    saveAccord(floralMusky, "Floral", 100);
    saveAccord(floralMusky, "Musky", 70);
    perfumeNoteRepository.save(new PerfumeNote(null, citrusWoody, "bergamot", "top"));
    perfumeNoteRepository.save(new PerfumeNote(null, citrusWoody, "cedarwood", "base"));
    perfumeNoteRepository.save(new PerfumeNote(null, floralMusky, "rose", "mid"));
    perfumeNoteRepository.save(new PerfumeNote(null, floralMusky, "musk", "base"));
  }

  @Test
  void recommendsLayeringForTwoPerfumeIds() throws Exception {
    mockMvc
        .perform(
            post("/api/layering/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"perfumeIds\":[1,2]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.inputPerfumes[0].id").value(1))
        .andExpect(jsonPath("$.inputPerfumes[1].id").value(2))
        .andExpect(jsonPath("$.recommendation.candidateType").value("PAIR"))
        .andExpect(jsonPath("$.recommendation.score").isNumber())
        .andExpect(jsonPath("$.recommendation.decision").isString())
        .andExpect(
            jsonPath("$.recommendation.color.hex")
                .value(org.hamcrest.Matchers.matchesRegex("^#[0-9A-Fa-f]{6}$")))
        .andExpect(jsonPath("$.recommendation.reasons").isArray())
        .andExpect(jsonPath("$.recommendation.scoreBreakdown.matrix").isNumber());
  }

  @Test
  void recommendsLayeringWithoutCsrfEvenWhenAuthenticationCookieExists() throws Exception {
    mockMvc
        .perform(
            post("/api/layering/recommendations")
                .cookie(authCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"perfumeIds\":[1,2]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.recommendation.candidateType").value("PAIR"));
  }

  @Test
  void rejectsOnePerfumeId() throws Exception {
    mockMvc
        .perform(
            post("/api/layering/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"perfumeIds\":[1]}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("향수는 정확히 2개를 선택해야 합니다."));
  }

  @Test
  void rejectsDuplicatePerfumeIds() throws Exception {
    mockMvc
        .perform(
            post("/api/layering/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"perfumeIds\":[1,1]}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("서로 다른 향수 2개를 선택해야 합니다."));
  }

  @Test
  void rejectsUnknownPerfumeId() throws Exception {
    mockMvc
        .perform(
            post("/api/layering/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"perfumeIds\":[1,999]}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("존재하지 않는 향수 ID가 포함되어 있습니다."));
  }

  @Test
  void rejectsPerfumeWithoutAccordData() throws Exception {
    perfumeRepository.save(perfume(3L, "No Accord"));

    mockMvc
        .perform(
            post("/api/layering/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"perfumeIds\":[1,3]}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("향수 어코드 데이터가 부족합니다."));
  }

  private static Perfume perfume(Long id, String name) {
    return new Perfume(
        id, name, "Test Brand", Gender.U, "https://example.com/perfume.jpg", "description");
  }

  private void saveAccord(Perfume perfume, String accordName, int ratio) {
    perfumeAccordRepository.save(new PerfumeAccord(null, perfume, accordName, ratio));
  }

  private Cookie authCookie() {
    User user = new User();
    user.setUserId(1);
    user.setEmail("layering-user@example.com");
    user.setProfileCompleted(true);
    return new Cookie("PERFUME_ACCESS_TOKEN", jwtService.issueAccessToken(user));
  }
}
