# Google OAuth Login

Google OAuth를 포함한 프론트엔드 인증 연동은 [Frontend Auth Integration Guide](oauth-login.md)를 기준으로 봅니다.

프론트에서 Google 로그인을 시작할 때는 아래 URL로 브라우저를 이동시키면 됩니다.

```ts
window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
```

OAuth 성공 후에는 `https://thescentlab.vercel.app/oauth2/success` 페이지에서 `GET /api/auth/csrf`와 `GET /api/auth/me`를 순서대로 호출해 로그인 상태를 복구합니다.
