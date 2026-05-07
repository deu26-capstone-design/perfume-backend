package kim.biryeong.perfume.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kim.biryeong.perfume.auth.AuthenticatedUserIds;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuditAuthenticatedUserFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    try {
      request.setAttribute(
          AuditLogRequestAttributes.USER_ID_ATTRIBUTE,
          AuthenticatedUserIds.currentUserId(authentication));
    } catch (RuntimeException exception) {
      // Missing or invalid authentication is recorded without a user identifier.
    }
    filterChain.doFilter(request, response);
  }
}
