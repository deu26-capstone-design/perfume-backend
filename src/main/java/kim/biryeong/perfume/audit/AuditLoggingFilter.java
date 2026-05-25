package kim.biryeong.perfume.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;
import kim.biryeong.perfume.auth.AuthenticatedUserIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuditLoggingFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuditLoggingFilter.class);
  private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
  private static final Set<String> READ_ONLY_POST_PATHS = Set.of("/api/layering/recommendations");
  private static final Pattern REVIEW_CREATE_PATH = Pattern.compile("^/api/perfumes/\\d+/reviews$");
  private static final Pattern WISHLIST_ITEM_PATH = Pattern.compile("^/api/wishlist/\\d+$");

  private final AuditLogService auditLogService;

  public AuditLoggingFilter(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Throwable chainFailure = null;
    try {
      filterChain.doFilter(request, response);
    } catch (IOException | ServletException | RuntimeException exception) {
      chainFailure = exception;
      throw exception;
    } finally {
      recordAuditLog(request, response, chainFailure);
    }
  }

  private void recordAuditLog(
      HttpServletRequest request, HttpServletResponse response, Throwable chainFailure) {
    try {
      int statusCode = statusCode(response, chainFailure);
      AuditOutcome outcome = outcome(statusCode, chainFailure);
      if (!shouldAudit(request, outcome)) {
        return;
      }
      auditLogService.record(
          request,
          eventType(request),
          outcome,
          statusCode,
          userId(request),
          failureReason(outcome, statusCode, chainFailure));
    } catch (RuntimeException exception) {
      LOGGER.warn(
          "Failed to persist audit log for {} {}",
          request.getMethod(),
          request.getRequestURI(),
          exception);
    }
  }

  private boolean shouldAudit(HttpServletRequest request, AuditOutcome outcome) {
    String path = applicationPath(request);
    if (!path.startsWith("/api/")) {
      return false;
    }
    if ("POST".equals(request.getMethod()) && READ_ONLY_POST_PATHS.contains(path)) {
      return false;
    }
    return MUTATING_METHODS.contains(request.getMethod())
        || (AuditOutcome.FAILURE.equals(outcome) && isAuthCheckPath(request));
  }

  private boolean isAuthCheckPath(HttpServletRequest request) {
    String path = applicationPath(request);
    return "GET".equals(request.getMethod())
        && ("/api/auth/me".equals(path) || "/api/auth/csrf".equals(path));
  }

  private AuditEventType eventType(HttpServletRequest request) {
    Object explicitEventType = request.getAttribute(AuditLogRequestAttributes.EVENT_TYPE_ATTRIBUTE);
    if (explicitEventType instanceof AuditEventType auditEventType) {
      return auditEventType;
    }

    String method = request.getMethod();
    String path = applicationPath(request);
    if ("POST".equals(method) && "/api/auth/signup".equals(path)) {
      return AuditEventType.AUTH_SIGNUP;
    }
    if ("POST".equals(method) && "/api/auth/login".equals(path)) {
      return AuditEventType.AUTH_LOGIN;
    }
    if ("POST".equals(method) && "/api/auth/logout".equals(path)) {
      return AuditEventType.AUTH_LOGOUT;
    }
    if ("PATCH".equals(method) && "/api/auth/me/profile".equals(path)) {
      return AuditEventType.AUTH_PROFILE_UPDATE;
    }
    if ("GET".equals(method) && ("/api/auth/me".equals(path) || "/api/auth/csrf".equals(path))) {
      return AuditEventType.AUTH_CHECK;
    }
    if ("POST".equals(method) && REVIEW_CREATE_PATH.matcher(path).matches()) {
      return AuditEventType.REVIEW_CREATE;
    }
    if ("POST".equals(method) && WISHLIST_ITEM_PATH.matcher(path).matches()) {
      return AuditEventType.WISHLIST_ADD;
    }
    if ("DELETE".equals(method) && WISHLIST_ITEM_PATH.matcher(path).matches()) {
      return AuditEventType.WISHLIST_REMOVE;
    }
    return AuditEventType.API_MUTATION;
  }

  private Integer userId(HttpServletRequest request) {
    Object explicitUserId = request.getAttribute(AuditLogRequestAttributes.USER_ID_ATTRIBUTE);
    if (explicitUserId instanceof Integer integer) {
      return integer;
    }
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    try {
      return AuthenticatedUserIds.currentUserId(authentication);
    } catch (RuntimeException exception) {
      return null;
    }
  }

  private int statusCode(HttpServletResponse response, Throwable chainFailure) {
    if (chainFailure != null && response.getStatus() < 400) {
      return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }
    return response.getStatus();
  }

  private AuditOutcome outcome(int statusCode, Throwable chainFailure) {
    if (chainFailure != null) {
      return AuditOutcome.FAILURE;
    }
    return statusCode >= 200 && statusCode < 400 ? AuditOutcome.SUCCESS : AuditOutcome.FAILURE;
  }

  private String failureReason(AuditOutcome outcome, int statusCode, Throwable chainFailure) {
    if (AuditOutcome.SUCCESS.equals(outcome)) {
      return null;
    }
    if (chainFailure != null) {
      return "UNHANDLED_EXCEPTION";
    }
    return "HTTP_" + statusCode;
  }

  private static String applicationPath(HttpServletRequest request) {
    String contextPath = request.getContextPath();
    String requestUri = request.getRequestURI();
    if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
      return requestUri.substring(contextPath.length());
    }
    return requestUri;
  }
}
