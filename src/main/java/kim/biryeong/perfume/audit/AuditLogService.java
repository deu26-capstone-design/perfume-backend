package kim.biryeong.perfume.audit;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuditLogService {

  private static final int MAX_USER_AGENT_LENGTH = 512;
  private static final int MAX_PATH_LENGTH = 512;
  private static final int MAX_FAILURE_REASON_LENGTH = 128;

  private final AuditLogRepository auditLogRepository;
  private final AuditProperties auditProperties;

  public AuditLogService(AuditLogRepository auditLogRepository, AuditProperties auditProperties) {
    this.auditLogRepository = auditLogRepository;
    this.auditProperties = auditProperties;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void record(
      HttpServletRequest request,
      AuditEventType eventType,
      AuditOutcome outcome,
      Integer statusCode,
      Integer userId,
      String failureReason) {
    AuditLog auditLog = new AuditLog();
    auditLog.setEventType(eventType);
    auditLog.setOutcome(outcome);
    auditLog.setHttpMethod(truncate(request.getMethod(), 12));
    auditLog.setRequestPath(truncate(request.getRequestURI(), MAX_PATH_LENGTH));
    auditLog.setStatusCode(statusCode);
    auditLog.setUserId(userId);
    auditLog.setClientIp(truncate(clientIp(request), 64));
    auditLog.setUserAgent(truncate(request.getHeader("User-Agent"), MAX_USER_AGENT_LENGTH));
    auditLog.setOccurredAt(Instant.now());
    auditLog.setFailureReason(truncate(failureReason, MAX_FAILURE_REASON_LENGTH));
    auditLogRepository.save(auditLog);
  }

  private String clientIp(HttpServletRequest request) {
    if (!isTrustedProxy(request.getRemoteAddr())) {
      return request.getRemoteAddr();
    }
    String realIp = request.getHeader("X-Real-IP");
    if (StringUtils.hasText(realIp)) {
      return realIp;
    }
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (StringUtils.hasText(forwardedFor) && !forwardedFor.contains(",")) {
      return forwardedFor.trim();
    }
    return request.getRemoteAddr();
  }

  private boolean isTrustedProxy(String remoteAddress) {
    return StringUtils.hasText(remoteAddress)
        && auditProperties.getTrustedProxyAddresses().contains(remoteAddress);
  }

  private String truncate(String value, int maxLength) {
    if (value == null || value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength);
  }
}
