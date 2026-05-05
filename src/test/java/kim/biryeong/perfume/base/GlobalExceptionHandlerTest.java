package kim.biryeong.perfume.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolationException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handlesBodyValidationWithMessageResponse() throws NoSuchMethodException {
    TestBody body = new TestBody();
    DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(body, "body");
    bindingResult.rejectValue("name", "NotBlank", "이름은 필수입니다.");
    MethodParameter parameter =
        new MethodParameter(
            GlobalExceptionHandlerTest.class.getDeclaredMethod(
                "sampleBodyEndpoint", TestBody.class),
            0);

    Map<String, String> response =
        handler.handleMethodArgumentNotValid(
            new MethodArgumentNotValidException(parameter, bindingResult));

    assertEquals("이름은 필수입니다.", response.get("message"));
  }

  @Test
  void handlesParamValidationWithMessageResponse() {
    Map<String, String> response =
        handler.handleConstraintViolation(
            new ConstraintViolationException("userId는 1 이상이어야 합니다.", Set.of()));

    assertEquals("userId는 1 이상이어야 합니다.", response.get("message"));
  }

  @Test
  void handlesMissingRequestParameterWithMessageResponse() {
    Map<String, String> response =
        handler.handleMissingServletRequestParameter(
            new MissingServletRequestParameterException("userId", "Integer"));

    assertEquals("userId 요청 파라미터는 필수입니다.", response.get("message"));
  }

  @Test
  void handlesRequestParameterTypeMismatchWithMessageResponse() throws NoSuchMethodException {
    MethodParameter parameter =
        new MethodParameter(
            GlobalExceptionHandlerTest.class.getDeclaredMethod(
                "sampleParamEndpoint", Integer.class),
            0);
    Map<String, String> response =
        handler.handleMethodArgumentTypeMismatch(
            new MethodArgumentTypeMismatchException(
                "abc", Integer.class, "userId", parameter, null));

    assertEquals("userId 요청 파라미터 형식이 올바르지 않습니다.", response.get("message"));
  }

  @Test
  void mapsDuplicateDataIntegrityToConflictMessage() throws NoSuchMethodException {
    Map<String, String> response =
        handler.handleDataIntegrityViolation(new DataIntegrityViolationException("duplicate"));

    assertEquals("중복된 데이터입니다.", response.get("message"));
    assertResponseStatus(
        "handleDataIntegrityViolation", HttpStatus.CONFLICT, DataIntegrityViolationException.class);
  }

  @Test
  void mapsResponseStatusExceptionReason() {
    var response =
        handler.handleResponseStatus(
            new ResponseStatusException(HttpStatus.NOT_FOUND, "찾을 수 없습니다."));

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("찾을 수 없습니다.", response.getBody().get("message"));
  }

  private static void assertResponseStatus(
      String methodName, HttpStatus expectedStatus, Class<?>... parameterTypes)
      throws NoSuchMethodException {
    Method method = GlobalExceptionHandler.class.getDeclaredMethod(methodName, parameterTypes);
    ResponseStatus responseStatus = method.getAnnotation(ResponseStatus.class);

    assertTrue(responseStatus != null);
    assertEquals(expectedStatus, responseStatus.value());
  }

  @SuppressWarnings("unused")
  private static void sampleBodyEndpoint(TestBody body) {}

  @SuppressWarnings("unused")
  private static void sampleParamEndpoint(Integer userId) {}

  private static class TestBody {
    @SuppressWarnings("unused")
    private String name;
  }
}
