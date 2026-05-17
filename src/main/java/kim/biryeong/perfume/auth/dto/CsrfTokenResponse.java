package kim.biryeong.perfume.auth.dto;

/**
 * JWT 쿠키 기반 상태 변경 요청에 사용할 CSRF 토큰 응답입니다.
 *
 * @param csrfToken {@code X-XSRF-TOKEN} 헤더로 다시 보내야 하는 double-submit CSRF 토큰
 */
public record CsrfTokenResponse(String csrfToken) {}
