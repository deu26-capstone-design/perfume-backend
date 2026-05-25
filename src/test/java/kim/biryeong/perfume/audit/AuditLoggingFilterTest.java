package kim.biryeong.perfume.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AuditLoggingFilterTest {

  @Test
  void unhandledExceptionIsAuditedAsFailure() {
    RecordingAuditLogService auditLogService = new RecordingAuditLogService();
    AuditLoggingFilter filter = new AuditLoggingFilter(auditLogService);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain throwingChain =
        (servletRequest, servletResponse) -> {
          throw new IllegalStateException("boom");
        };

    assertThatThrownBy(() -> filter.doFilter(request, response, throwingChain))
        .isInstanceOf(IllegalStateException.class);

    assertThat(auditLogService.outcome).isEqualTo(AuditOutcome.FAILURE);
    assertThat(auditLogService.statusCode).isEqualTo(500);
    assertThat(auditLogService.failureReason).isEqualTo("UNHANDLED_EXCEPTION");
  }

  @Test
  void mutatingApiRequestWithContextPathIsAuditedAndClassified() throws Exception {
    RecordingAuditLogService auditLogService = new RecordingAuditLogService();
    AuditLoggingFilter filter = new AuditLoggingFilter(auditLogService);
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/app/api/wishlist/1");
    request.setContextPath("/app");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = (servletRequest, servletResponse) -> {};

    filter.doFilter(request, response, chain);

    assertThat(auditLogService.recorded).isTrue();
    assertThat(auditLogService.eventType).isEqualTo(AuditEventType.WISHLIST_ADD);
    assertThat(auditLogService.outcome).isEqualTo(AuditOutcome.SUCCESS);
    assertThat(auditLogService.statusCode).isEqualTo(200);
  }

  @Test
  void layeringRecommendationPostWithContextPathIsNotAuditedAsMutation() throws Exception {
    RecordingAuditLogService auditLogService = new RecordingAuditLogService();
    AuditLoggingFilter filter = new AuditLoggingFilter(auditLogService);
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/app/api/layering/recommendations");
    request.setContextPath("/app");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = (servletRequest, servletResponse) -> {};

    filter.doFilter(request, response, chain);

    assertThat(auditLogService.recorded).isFalse();
  }

  @Test
  void layeringRecommendationPostIsNotAuditedAsMutation() throws Exception {
    RecordingAuditLogService auditLogService = new RecordingAuditLogService();
    AuditLoggingFilter filter = new AuditLoggingFilter(auditLogService);
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/api/layering/recommendations");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = (servletRequest, servletResponse) -> {};

    filter.doFilter(request, response, chain);

    assertThat(auditLogService.recorded).isFalse();
  }

  private static class RecordingAuditLogService extends AuditLogService {

    private AuditOutcome outcome;
    private AuditEventType eventType;
    private Integer statusCode;
    private String failureReason;
    private boolean recorded;

    RecordingAuditLogService() {
      super(null, null);
    }

    @Override
    public void record(
        HttpServletRequest request,
        AuditEventType eventType,
        AuditOutcome outcome,
        Integer statusCode,
        Integer userId,
        String failureReason) {
      this.recorded = true;
      this.eventType = eventType;
      this.outcome = outcome;
      this.statusCode = statusCode;
      this.failureReason = failureReason;
    }
  }
}
