package kim.biryeong.perfume.auth;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {AuthController.class, MyPageController.class})
public class AuthExceptionHandler {

  @ExceptionHandler(AuthConflictException.class)
  public ResponseEntity<Map<String, String>> handleConflict(AuthConflictException exception) {
    return problem(HttpStatus.CONFLICT, exception.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, String>> handleDataIntegrityViolation() {
    return problem(HttpStatus.CONFLICT, "user already exists");
  }

  @ExceptionHandler({AuthUnauthorizedException.class, InvalidCredentialsException.class})
  public ResponseEntity<Map<String, String>> handleUnauthorized(RuntimeException exception) {
    return problem(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  private ResponseEntity<Map<String, String>> problem(HttpStatus status, String detail) {
    return ResponseEntity.status(status).body(Map.of("message", detail));
  }
}
