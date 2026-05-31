# Frontend Auth Integration Guide

이 문서는 Vercel 프론트엔드에서 Perfume Backend 인증을 붙일 때 필요한 내용만 정리합니다.

현재 프론트 주소:

```text
https://thescentlab.vercel.app/
```

백엔드는 별도 API 도메인으로 호출한다고 가정합니다.

```text
https://perfume.biryeong.kim
```

프론트 코드에서는 아래 값만 실제 API 도메인으로 바꿔서 사용하면 됩니다.

```ts
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL!;
```

## 핵심 규칙

- 모든 인증 API 요청에는 `credentials: "include"`를 넣습니다.
- JWT는 `PERFUME_ACCESS_TOKEN` `HttpOnly` 쿠키로만 전달됩니다.
- 프론트는 JWT를 직접 읽거나 저장하지 않습니다.
- 로그인 또는 OAuth 성공 후 `GET /api/auth/csrf`를 호출해서 `csrfToken`을 받아둡니다.
- `POST`, `PATCH`, `DELETE` 요청에는 `X-XSRF-TOKEN` 헤더를 보냅니다. 로그아웃은 만료되었거나 손상된 HttpOnly 쿠키 제거를 위해 인증 실패 시에도 만료 쿠키를 내려주지만, 유효한 쿠키 인증 요청에는 CSRF 토큰이 필요합니다.
- `401`이면 로그인되지 않은 상태로 처리합니다.
- `403`이면 CSRF 토큰이 없거나 오래된 상태이므로 `/api/auth/csrf`를 다시 호출합니다.

## 공통 Fetch Helper

```ts
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL!;

let csrfToken: string | null = null;

async function apiFetch(path: string, init: RequestInit = {}) {
  const isFormData = init.body instanceof FormData;

  return fetch(`${API_BASE_URL}${path}`, {
    ...init,
    credentials: "include",
    headers: {
      ...(init.body && !isFormData ? { "Content-Type": "application/json" } : {}),
      ...(csrfToken ? { "X-XSRF-TOKEN": csrfToken } : {}),
      ...init.headers,
    },
  });
}

export async function refreshCsrfToken() {
  const response = await apiFetch("/api/auth/csrf");

  if (!response.ok) {
    csrfToken = null;
    return null;
  }

  const body = (await response.json()) as { csrfToken: string };
  csrfToken = body.csrfToken;
  return csrfToken;
}
```

## 로컬 회원가입

```ts
export async function signup() {
  const response = await apiFetch("/api/auth/signup", {
    method: "POST",
    body: JSON.stringify({
      email: "user@example.com",
      password: "secret-password",
      name: "김향수",
      nickname: "perfume_user",
      gender: "F",
      birthDate: "1999-05-01",
      phoneNumber: "01012345678",
    }),
  });

  if (!response.ok) {
    throw new Error("signup failed");
  }

  const currentUser = await response.json();
  await refreshCsrfToken();
  return currentUser;
}
```

## 로컬 로그인

```ts
export async function login(email: string, password: string) {
  const response = await apiFetch("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });

  if (response.status === 401) {
    throw new Error("invalid credentials");
  }

  if (!response.ok) {
    throw new Error("login failed");
  }

  const currentUser = await response.json();
  await refreshCsrfToken();
  return currentUser;
}
```

## OAuth 로그인 시작

브라우저를 백엔드 OAuth 시작 URL로 이동시키면 됩니다.

```ts
export function startGoogleLogin() {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
}

export function startNaverLogin() {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/naver`;
}
```

OAuth 성공 후 백엔드는 프론트 성공 페이지로 리다이렉트합니다.

```text
https://thescentlab.vercel.app/oauth2/success
```

성공 페이지에서는 바로 인증 상태를 초기화합니다.

```ts
export async function bootstrapAfterOAuthSuccess() {
  await refreshCsrfToken();
  return getCurrentUser();
}
```

OAuth 실패 시에는 실패 페이지로 리다이렉트됩니다.

```text
https://thescentlab.vercel.app/oauth2/failure?error=oauth_login_failed
```

프론트는 `error` query parameter를 읽어서 로그인 실패 화면을 보여주면 됩니다.

## 현재 사용자 조회

```ts
export async function getCurrentUser() {
  const response = await apiFetch("/api/auth/me");

  if (response.status === 401) {
    return null;
  }

  if (!response.ok) {
    throw new Error("failed to load current user");
  }

  return response.json();
}
```

응답의 `profileCompleted`가 `false`이면 추가 프로필 입력 화면으로 보내면 됩니다.

```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "Naver User",
  "nickname": null,
  "gender": null,
  "birthDate": null,
  "phoneNumber": null,
  "profileImageUrl": null,
  "oauthProvider": "NAVER",
  "profileCompleted": false
}
```

## 프로필 완성

`PATCH`, `POST`, `DELETE` 요청은 CSRF 토큰이 필요합니다. 로그인 직후 또는 OAuth 성공 직후 `refreshCsrfToken()`을 한 번 호출해둔 상태여야 합니다.

```ts
export async function completeProfile(profile: {
  name: string;
  nickname: string;
  gender: string;
  birthDate: string;
  phoneNumber: string;
}) {
  if (!csrfToken) {
    await refreshCsrfToken();
  }

  const response = await apiFetch("/api/auth/me/profile", {
    method: "PATCH",
    body: JSON.stringify(profile),
  });

  if (response.status === 403) {
    await refreshCsrfToken();
    throw new Error("csrf token refreshed; retry the request");
  }

  if (!response.ok) {
    throw new Error("profile update failed");
  }

  return response.json();
}
```

## 프로필 사진 변경

`FormData` 요청은 브라우저가 `multipart/form-data` boundary를 자동으로 붙여야 하므로 `Content-Type: application/json`을 직접 설정하면 안 됩니다. 위 `apiFetch` 헬퍼는 `FormData` 본문일 때 `Content-Type`을 생략합니다.

```ts
export async function updateProfileImage(image: File) {
  if (!csrfToken) {
    await refreshCsrfToken();
  }

  const formData = new FormData();
  formData.append("image", image);

  const response = await apiFetch("/api/auth/me/profile-image", {
    method: "POST",
    body: formData,
  });

  if (response.status === 403) {
    await refreshCsrfToken();
    throw new Error("csrf token refreshed; retry the request");
  }

  if (!response.ok) {
    throw new Error("profile image update failed");
  }

  return response.json();
}
```

## 로그아웃

```ts
export async function logout() {
  if (!csrfToken) {
    await refreshCsrfToken();
  }

  const response = await apiFetch("/api/auth/logout", {
    method: "POST",
  });

  csrfToken = null;

  if (!response.ok && response.status !== 401) {
    throw new Error("logout failed");
  }
}
```

만료되었거나 손상된 인증 쿠키 때문에 `/api/auth/csrf`가 실패해도 `/api/auth/logout`은 호출할 수 있습니다. 이 경우 백엔드는 인증 쿠키와 CSRF 쿠키를 만료시키는 `204` 응답을 반환합니다.

## 위시리스트와 리뷰 작성

위시리스트 추가, 위시리스트 삭제, 리뷰 작성도 같은 규칙을 사용합니다.

```ts
await apiFetch(`/api/wishlist/${perfumeId}`, {
  method: "POST",
});

await apiFetch(`/api/wishlist/${perfumeId}`, {
  method: "DELETE",
});

await apiFetch(`/api/perfumes/${perfumeId}/reviews`, {
  method: "POST",
  body: JSON.stringify({
    satisfaction: 5,
    longevity: 2,
    seasons: ["봄"],
    scents: ["꽃 향"],
    comment: "좋아요.",
    disclaimerAgreed: true,
  }),
});
```

## Vercel 환경 변수

Vercel 프로젝트에는 API 도메인만 넣으면 됩니다.

```text
NEXT_PUBLIC_API_BASE_URL=https://perfume.biryeong.kim
```

프론트에서 `https://thescentlab.vercel.app/api/...`로 프록시하지 않고 API 도메인을 직접 호출하는 구조입니다.

## 서버 컴퓨터에서 해야 할 작업

서버 컴퓨터에서는 백엔드가 Vercel 프론트에서 오는 쿠키 인증 요청을 받을 수 있게 환경 변수와 Nginx를 맞춰야 합니다.

### 1. 백엔드 최신 코드 반영

```powershell
cd H:\perfume-backend
git pull
.\gradlew.bat build
```

이미 배포용 jar를 따로 복사해서 실행하는 구조라면 새로 빌드된 jar로 교체합니다.

### 2. 백엔드 운영 환경 변수 설정

운영 환경에서는 반드시 HTTPS 쿠키 설정을 사용합니다.

```text
JWT_SECRET=최소_32바이트_이상의_랜덤_문자열
APP_AUTH_COOKIE_SECURE=true
APP_AUTH_COOKIE_SAME_SITE=None
APP_CORS_ALLOWED_ORIGINS=https://thescentlab.vercel.app
APP_OAUTH2_SUCCESS_REDIRECT_URI=https://thescentlab.vercel.app/oauth2/success
APP_OAUTH2_FAILURE_REDIRECT_URI=https://thescentlab.vercel.app/oauth2/failure
APP_R2_ACCOUNT_ID=...
APP_R2_ACCESS_KEY_ID=...
APP_R2_SECRET_ACCESS_KEY=...
APP_R2_BUCKET=...
APP_R2_PUBLIC_BASE_URL=https://cdn.example.com
APP_R2_KEY_PREFIX=profile-images
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
NAVER_CLIENT_ID=...
NAVER_CLIENT_SECRET=...
```

`APP_AUTH_COOKIE_SAME_SITE=None`은 프론트와 API가 서로 다른 사이트일 때 쿠키를 보내기 위해 필요합니다. 이 설정은 `APP_AUTH_COOKIE_SECURE=true`와 HTTPS가 같이 있어야 브라우저에서 정상 동작합니다.

### 3. Nginx HTTPS 프록시 설정

API 도메인이 `perfume.biryeong.kim`이면 Nginx는 백엔드 앱의 `localhost:8080`으로 프록시합니다.

```nginx
server {
    listen 80;
    server_name perfume.biryeong.kim;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name perfume.biryeong.kim;

    ssl_certificate /etc/letsencrypt/live/perfume.biryeong.kim/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/perfume.biryeong.kim/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header Forwarded "";
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $remote_addr;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port 443;
        proxy_set_header X-Forwarded-Prefix "";
        proxy_set_header CF-Connecting-IP "";
        proxy_set_header X-Forwarded-Proto https;
    }
}
```

CORS는 Spring 백엔드가 처리하므로 Nginx에서 `Access-Control-Allow-Origin: *` 같은 헤더를 따로 추가하지 마십시오. 쿠키 인증에서는 `*` origin과 credentials를 같이 쓸 수 없습니다.

Spring은 `server.forward-headers-strategy=framework`로 프록시 헤더를 해석합니다. 따라서 Nginx는 클라이언트가 보낸 `Forwarded`, `X-Forwarded-*` 값을 그대로 전달하지 말고 위 예시처럼 덮어쓰거나 빈 값으로 제거해야 합니다.

감사 로그의 `client_ip`는 백엔드가 신뢰한 프록시에서 온 요청일 때만 `X-Forwarded-For` 또는 `X-Real-IP`를 사용합니다. 운영에서 Nginx 컨테이너/호스트 IP가 `127.0.0.1`이 아니면 `APP_AUDIT_TRUSTED_PROXY_ADDRESSES`에 해당 IP를 추가하십시오. 공격자가 보낸 기존 `X-Forwarded-For` 값을 이어붙이지 않도록 `$proxy_add_x_forwarded_for` 대신 `$remote_addr`로 덮어씁니다. Cloudflare를 Nginx 앞에 직접 두지 않는 구성에서는 클라이언트가 보낸 `CF-Connecting-IP`를 빈 값으로 덮어씁니다.

### 4. OAuth 제공자 콘솔 설정

Google Cloud Console과 Naver Developers의 redirect URI에 백엔드 콜백 주소를 등록합니다.

```text
https://perfume.biryeong.kim/login/oauth2/code/google
https://perfume.biryeong.kim/login/oauth2/code/naver
```

프론트 주소가 아니라 백엔드 API 도메인입니다.

### 5. 재시작과 확인

백엔드 앱과 Nginx를 재시작한 뒤 공개 API와 CORS preflight를 확인합니다.

```bash
curl -i https://perfume.biryeong.kim/api/perfumes?page=0&size=1

curl -i -X OPTIONS https://perfume.biryeong.kim/api/auth/me \
  -H "Origin: https://thescentlab.vercel.app" \
  -H "Access-Control-Request-Method: GET"
```

응답에 아래 헤더가 보여야 합니다.

```http
Access-Control-Allow-Origin: https://thescentlab.vercel.app
Access-Control-Allow-Credentials: true
```

로컬 로그인 테스트에서는 `Set-Cookie`에 다음 속성이 보여야 합니다.

```http
Set-Cookie: PERFUME_ACCESS_TOKEN=...; Path=/; Max-Age=...; Expires=...; HttpOnly; Secure; SameSite=None
```

## 자주 나는 문제

### 로그인은 200인데 `/api/auth/me`가 401

- 프론트 요청에 `credentials: "include"`가 빠졌는지 확인합니다.
- 서버 쿠키가 `SameSite=None; Secure`로 내려오는지 확인합니다.
- API가 HTTPS인지 확인합니다.
- `APP_CORS_ALLOWED_ORIGINS`가 정확히 `https://thescentlab.vercel.app`인지 확인합니다.

### 상태 변경 요청이 403

- 로그인 또는 OAuth 성공 후 `GET /api/auth/csrf`를 호출했는지 확인합니다.
- `X-XSRF-TOKEN` 헤더에 `csrfToken` 값을 보냈는지 확인합니다.
- 오래된 토큰일 수 있으니 `/api/auth/csrf`를 다시 호출한 뒤 재시도합니다.

### OAuth 성공 후 로그인 상태가 안 잡힘

- OAuth provider redirect URI가 API 도메인인지 확인합니다.
- 백엔드 성공 리다이렉트가 `https://thescentlab.vercel.app/oauth2/success`인지 확인합니다.
- 성공 페이지에서 `refreshCsrfToken()`과 `/api/auth/me`를 호출하는지 확인합니다.
