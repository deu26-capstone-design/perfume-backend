package kim.biryeong.perfume.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** Operational audit record for authentication and mutating API requests. */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
      @Index(name = "idx_audit_logs_occurred_at", columnList = "occurred_at"),
      @Index(name = "idx_audit_logs_user_id_occurred_at", columnList = "user_id, occurred_at"),
      @Index(
          name = "idx_audit_logs_event_type_occurred_at",
          columnList = "event_type, occurred_at"),
      @Index(name = "idx_audit_logs_outcome_occurred_at", columnList = "outcome, occurred_at")
    })
@Getter
@Setter
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, length = 48)
  private AuditEventType eventType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private AuditOutcome outcome;

  @Column(name = "http_method", nullable = false, length = 12)
  private String httpMethod;

  @Column(name = "request_path", nullable = false, length = 512)
  private String requestPath;

  @Column(name = "status_code", nullable = false)
  private Integer statusCode;

  @Column(name = "user_id")
  private Integer userId;

  @Column(name = "client_ip", length = 64)
  private String clientIp;

  @Column(name = "user_agent", length = 512)
  private String userAgent;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "failure_reason", length = 128)
  private String failureReason;
}
