# Auth and OAuth Login Integration

이 문서는 프론트엔드에서 백엔드 인증 API, Google OAuth, Naver OAuth를 연동하기 위한 공개 계약입니다.

## Local Signup

```text
POST /api/auth/signup
Content-Type: application/json
```

요청:

```json
{
  "email": "user@example.com",
  "password": "secret-password",
  "name": "김향수",
  "nickname": "perfume_user",
  "gender": "F",
  "birthDate": "1999-05-01",
  "phoneNumber": "01012345678"
}
```

성공 시 백엔드는 자체 JWT를 `PERFUME_ACCESS_TOKEN` `HttpOnly` 쿠키로 설정하고 `AuthUserResponse`를 반환합니다. 로컬 회원가입 사용자는 `profileCompleted=true`입니다.

비밀번호는 10자 이상 72자 이하입니다. 중복 email 또는 nickname은 `409 Conflict`, validation 실패는 `400 Bad Request`를 반환합니다.

## Local Login

```text
POST /api/auth/login
Content-Type: application/json
```

요청:

```json
{
  "email": "user@example.com",
  "password": "secret-password"
}
```

성공 시 백엔드는 자체 JWT를 `PERFUME_ACCESS_TOKEN` `HttpOnly` 쿠키로 설정하고, 상태 변경 API용 `XSRF-TOKEN` 쿠키를 함께 내려줍니다. JWT 문자열은 응답 본문에 포함하지 않습니다.

응답 헤더:

```http
Set-Cookie: PERFUME_ACCESS_TOKEN={jwt}; Path=/; HttpOnly; SameSite=Lax
Set-Cookie: XSRF-TOKEN={csrfToken}; Path=/; SameSite=Lax
```

응답 본문:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "김향수",
  "nickname": "perfume_user",
  "gender": "F",
  "birthDate": "1999-05-01",
  "phoneNumber": "01012345678",
  "oauthProvider": null,
  "profileCompleted": true
}
```

fetch 예시:

```ts
const response = await fetch(`${BACKEND_BASE_URL}/api/auth/login`, {
  method: "POST",
  credentials: "include",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    email: "user@example.com",
    password: "secret-password",
  }),
});

if (!response.ok) {
  throw new Error("login failed");
}

const currentUser = await response.json();
```

axios 예시:

```ts
axios.defaults.withCredentials = true;
axios.defaults.xsrfCookieName = "XSRF-TOKEN";
axios.defaults.xsrfHeaderName = "X-XSRF-TOKEN";

const { data: currentUser } = await axios.post(
  `${BACKEND_BASE_URL}/api/auth/login`,
  {
    email: "user@example.com",
    password: "secret-password",
  }
);
```

email이 없거나 비밀번호가 맞지 않거나 OAuth 전용 계정이면 `401 Unauthorized`를 반환합니다. 요청 본문 형식이 맞지 않으면 `400 Bad Request`를 반환합니다.

## OAuth Login Start

브라우저를 아래 URL로 이동시키면 OAuth 로그인 플로우가 시작됩니다.

```text
GET {BACKEND_BASE_URL}/oauth2/authorization/google
GET {BACKEND_BASE_URL}/oauth2/authorization/naver
```

예시:

```ts
window.location.href = `${BACKEND_BASE_URL}/oauth2/authorization/naver`;
```

## OAuth Redirect Flow

1. 프론트가 사용자를 `/oauth2/authorization/{provider}`로 이동시킵니다.
2. 백엔드는 provider 로그인 화면으로 리다이렉트합니다.
3. 인증이 완료되면 provider가 백엔드 콜백으로 리다이렉트합니다.
   - Google 로컬 예: `http://localhost:8080/login/oauth2/code/google`
   - Naver 로컬 예: `http://localhost:8080/login/oauth2/code/naver`
4. 백엔드는 provider 사용자 정보를 확인합니다.
   - Google은 `sub`, `email`, `email_verified`, `name`을 사용하며, `email_verified=true`가 아니면 실패 처리합니다.
   - Naver는 `response.id`, `response.email`, `response.name`, `response.nickname`, `response.gender`, `response.birthyear`, `response.birthday`, `response.mobile` 형태의 프로필 응답을 받습니다. 계정 식별과 생성에는 `response.id`, `response.email`, 이름 표시에 `response.name` 또는 `response.nickname`을 사용합니다.
   - 신규 OAuth 사용자는 `profileCompleted=false` 상태로 생성합니다.
   - Google은 `email_verified=true`인 경우 동일 email의 기존 로컬 사용자에 provider 정보를 연결합니다.
   - Naver는 현재 응답에서 email 검증 보증을 확인하지 않으므로 동일 email의 기존 로컬 사용자에 자동 연결하지 않고 실패 처리합니다.
   - 이미 다른 provider 계정이 연결된 email이면 실패 처리합니다.
5. 백엔드는 자체 JWT를 `HttpOnly` 쿠키로 설정한 뒤 성공 URL로 리다이렉트합니다.

성공 URL은 `app.oauth2.success-redirect-uri`, 실패 URL은 `app.oauth2.failure-redirect-uri`로 설정합니다. 실패 URL에는 `error` 쿼리 파라미터가 붙을 수 있습니다.

```text
{FAILURE_REDIRECT_URI}?error=email_not_verified
{FAILURE_REDIRECT_URI}?error=oauth_account_conflict
{FAILURE_REDIRECT_URI}?error=oauth_login_failed
{FAILURE_REDIRECT_URI}?error=missing_naver_response
{FAILURE_REDIRECT_URI}?error=missing_naver_response_id
{FAILURE_REDIRECT_URI}?error=missing_naver_response_email
{FAILURE_REDIRECT_URI}?error=unsupported_oauth_provider
```

## Cookie Authentication

JWT는 프론트에 직접 노출하지 않습니다. 백엔드는 기본 쿠키 이름 `PERFUME_ACCESS_TOKEN`에 JWT를 저장합니다.

쿠키 속성:

- `HttpOnly`
- `SameSite=Lax`
- `Path=/`
- 운영 HTTPS 환경에서는 `app.auth.cookie.secure=true` 권장

프론트는 API 요청에 credentials를 포함해야 합니다.

```ts
await fetch(`${BACKEND_BASE_URL}/api/auth/me`, {
  credentials: "include",
});
```

보안상 JWT를 `localStorage` 또는 `sessionStorage`에 저장하지 마십시오.

## CSRF Token

쿠키는 브라우저가 자동으로 전송하므로, 인증 쿠키 기반 상태 변경 API는 CSRF 토큰을 함께 보내야 합니다.
백엔드는 `XSRF-TOKEN` 쿠키를 내려주며, 프론트는 이 값을 `X-XSRF-TOKEN` 헤더로 보내야 합니다.

회원가입, 로컬 로그인, OAuth 로그인 성공 응답은 `XSRF-TOKEN` 쿠키를 함께 발급합니다.

```ts
const csrfToken = document.cookie
  .split("; ")
  .find((row) => row.startsWith("XSRF-TOKEN="))
  ?.split("=")[1];

await fetch(`${BACKEND_BASE_URL}/api/auth/me/profile`, {
  method: "PATCH",
  credentials: "include",
  headers: {
    "Content-Type": "application/json",
    "X-XSRF-TOKEN": decodeURIComponent(csrfToken ?? ""),
  },
  body: JSON.stringify(profile),
});
```

## Check Current User

```text
GET /api/auth/me
```

응답 예시:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "Naver User",
  "nickname": null,
  "gender": null,
  "birthDate": null,
  "phoneNumber": null,
  "oauthProvider": "NAVER",
  "profileCompleted": false
}
```

`profileCompleted=false`이면 추가 프로필 입력 화면으로 이동시킵니다.

## Complete Profile

```text
PATCH /api/auth/me/profile
Content-Type: application/json
```

요청:

```json
{
  "name": "김향수",
  "nickname": "perfume_user",
  "gender": "F",
  "birthDate": "1999-05-01",
  "phoneNumber": "01012345678"
}
```

응답 예시:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "김향수",
  "nickname": "perfume_user",
  "gender": "F",
  "birthDate": "1999-05-01",
  "phoneNumber": "01012345678",
  "oauthProvider": "NAVER",
  "profileCompleted": true
}
```

닉네임이 이미 사용 중이면 `409 Conflict`, 필수 값이 없거나 길이 제한을 초과하면 `400 Bad Request`가 반환됩니다.

## Logout

```text
POST /api/auth/logout
```

요청 예시:

```ts
await fetch(`${BACKEND_BASE_URL}/api/auth/logout`, {
  method: "POST",
  credentials: "include",
  headers: {
    "X-XSRF-TOKEN": csrfToken,
  },
});
```

백엔드는 인증 쿠키를 만료시키고 `204 No Content`를 반환합니다.

## Backend Configuration

Google Cloud Console과 Naver Developers에서 OAuth 클라이언트를 생성하고 승인된 redirect URI에 백엔드 콜백을 등록해야 합니다.

필수 secret:

```text
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
NAVER_CLIENT_ID
NAVER_CLIENT_SECRET
JWT_SECRET
```

`JWT_SECRET`은 HS256 서명을 위해 최소 32바이트 이상의 충분히 긴 랜덤 문자열이어야 합니다.

주요 설정:

```properties
app.auth.jwt.access-token-validity=1h
app.auth.cookie.secure=false
app.oauth2.success-redirect-uri=http://localhost:3000/oauth2/success
app.oauth2.failure-redirect-uri=http://localhost:3000/oauth2/failure
app.cors.allowed-origins=http://localhost:3000
```

Naver provider endpoint는 Naver Developers 문서 기준으로 다음 값을 사용합니다.

```properties
spring.security.oauth2.client.provider.naver.authorization-uri=https://nid.naver.com/oauth2.0/authorize
spring.security.oauth2.client.provider.naver.token-uri=https://nid.naver.com/oauth2.0/token
spring.security.oauth2.client.provider.naver.user-info-uri=https://openapi.naver.com/v1/nid/me
spring.security.oauth2.client.registration.naver.client-authentication-method=client_secret_post
```

로컬 개발에서 프론트와 백엔드 origin이 다르면 `app.cors.allowed-origins`에 프론트 origin을 등록해야 합니다.
쿠키 인증을 쓰므로 CORS 응답은 credentials를 허용해야 하며, 프론트 요청도 credentials를 포함해야 합니다.

## Existing MySQL Schema Migration

기존 `users` 테이블은 password 기반 가입을 전제로 `password`, `nickname`, `gender`, `birth_date`, `phone_number`가 `NOT NULL`일 수 있습니다.
OAuth 신규 사용자는 프로필 완료 전 이 값들이 비어 있으므로, 배포 전 [google-oauth-user-schema.sql](sql/google-oauth-user-schema.sql)을 검토 후 적용해야 합니다.

`spring.jpa.hibernate.ddl-auto=update`는 기존 컬럼의 `NOT NULL` 제약을 안정적으로 완화하지 않으므로, 운영 환경에서는 명시적 SQL 마이그레이션을 사용하십시오.
