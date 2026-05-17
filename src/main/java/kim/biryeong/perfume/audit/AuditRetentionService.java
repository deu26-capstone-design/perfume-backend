package kim.biryeong.perfume.audit;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditRetentionService {

  private final AuditLogRepository auditLogRepository;
  private final AuditProperties auditProperties;

  public AuditRetentionService(
      AuditLogRepository auditLogRepository, AuditProperties auditProperties) {
    this.auditLogRepository = auditLogRepository;
    this.auditProperties = auditProperties;
  }

  @Scheduled(cron = "${app.audit.retention-cleanup-cron:0 30 3 * * *}")
  @Transactional
  public void deleteExpiredAuditLogs() {
    int retentionDays = auditProperties.getRetentionDays();
    if (retentionDays <= 0) {
      return;
    }
    auditLogRepository.deleteExpiredBefore(Instant.now().minus(retentionDays, ChronoUnit.DAYS));
  }
}
