package kim.biryeong.perfume.audit;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

  long deleteByOccurredAtBefore(Instant threshold);
}
