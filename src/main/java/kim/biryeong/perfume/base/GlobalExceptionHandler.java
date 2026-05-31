package kim.biryeong.perfume.base;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String DUPLICATE_DATA_MESSAGE = "중복된 데이터입니다.";

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleConstraintViolation(ConstraintViolationException e) {
    return Map.of("message", e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse(e.getMessage());
    return Map.of("message", message);
  }

  @ExceptionHandler(MethodValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleMethodValidation(MethodValidationException e) {
    return Map.of("message", e.getMessage());
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleHandlerMethodValidation(HandlerMethodValidationException e) {
    return Map.of("message", e.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleMissingServletRequestParameter(
      MissingServletRequestParameterException e) {
    return Map.of("message", e.getParameterName() + " 요청 파라미터는 필수입니다.");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException e) {
    return Map.of("message", e.getName() + " 요청 파라미터 형식이 올바르지 않습니다.");
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
  public Map<String, String> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
    return Map.of(
        "message", e.getMessage() != null ? e.getMessage() : "profile image must be 5MB or smaller");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String, String> handleDataIntegrityViolation(DataIntegrityViolationException e) {
    return Map.of("message", DUPLICATE_DATA_MESSAGE);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(Map.of("message", e.getReason() != null ? e.getReason() : e.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
    return Map.of("message", e.getMessage());
  }
}
