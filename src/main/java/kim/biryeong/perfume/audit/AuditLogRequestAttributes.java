package kim.biryeong.perfume.audit;

import jakarta.servlet.http.HttpServletRequest;

public final class AuditLogRequestAttributes {

  public static final String EVENT_TYPE_ATTRIBUTE = attributeName("eventType");
  public static final String USER_ID_ATTRIBUTE = attributeName("userId");

  private AuditLogRequestAttributes() {}

  public static void mark(HttpServletRequest request, AuditEventType eventType, Integer userId) {
    request.setAttribute(EVENT_TYPE_ATTRIBUTE, eventType);
    request.setAttribute(USER_ID_ATTRIBUTE, userId);
  }

  private static String attributeName(String name) {
    return AuditLogRequestAttributes.class.getName() + "." + name;
  }
}
