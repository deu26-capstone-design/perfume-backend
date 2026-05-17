package kim.biryeong.perfume.audit;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

  @Modifying
  @Query("DELETE FROM AuditLog auditLog WHERE auditLog.occurredAt < :threshold")
  int deleteExpiredBefore(@Param("threshold") Instant threshold);
}
