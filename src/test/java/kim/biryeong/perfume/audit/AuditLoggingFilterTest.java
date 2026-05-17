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

  private static class RecordingAuditLogService extends AuditLogService {

    private AuditOutcome outcome;
    private Integer statusCode;
    private String failureReason;

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
      this.outcome = outcome;
      this.statusCode = statusCode;
      this.failureReason = failureReason;
    }
  }
}
