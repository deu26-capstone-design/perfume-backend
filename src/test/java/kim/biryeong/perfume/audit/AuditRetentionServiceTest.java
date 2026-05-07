package kim.biryeong.perfume.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuditRetentionServiceTest {

  @Test
  void deletesRowsOlderThanRetentionDays() {
    AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
    AuditProperties auditProperties = new AuditProperties();
    auditProperties.setRetentionDays(30);
    AuditRetentionService retentionService =
        new AuditRetentionService(auditLogRepository, auditProperties);

    retentionService.deleteExpiredAuditLogs();

    verify(auditLogRepository).deleteByOccurredAtBefore(any(Instant.class));
  }

  @Test
  void skipsDeleteWhenRetentionIsDisabled() {
    AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
    AuditProperties auditProperties = new AuditProperties();
    auditProperties.setRetentionDays(0);
    AuditRetentionService retentionService =
        new AuditRetentionService(auditLogRepository, auditProperties);

    retentionService.deleteExpiredAuditLogs();

    verify(auditLogRepository, never()).deleteByOccurredAtBefore(any());
  }
}
