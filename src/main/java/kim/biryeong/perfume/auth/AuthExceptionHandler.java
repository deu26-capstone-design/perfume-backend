package kim.biryeong.perfume.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

  @ExceptionHandler(AuthConflictException.class)
  public ResponseEntity<ProblemDetail> handleConflict(AuthConflictException exception) {
    return problem(HttpStatus.CONFLICT, exception.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> handleDataIntegrityViolation() {
    return problem(HttpStatus.CONFLICT, "user already exists");
  }

  @ExceptionHandler({AuthUnauthorizedException.class, InvalidCredentialsException.class})
  public ResponseEntity<ProblemDetail> handleUnauthorized(RuntimeException exception) {
    return problem(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  private ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
    return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
  }
}
