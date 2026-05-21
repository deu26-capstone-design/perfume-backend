# Perfume Backend REST API Specification

이 문서는 현재 Spring MVC 컨트롤러와 공개 DTO 기준의 REST API 명세입니다.

## 공통 규칙

- Base path: `/api`
- Request/response content type: `application/json`
- 페이지 번호는 `0`부터 시작합니다.
- 검증 또는 비즈니스 오류 응답은 기본적으로 다음 JSON 형태를 사용합니다.

```json
{
  "message": "오류 메시지"
}
```

## 공통 오류

| HTTP status | 발생 조건 | 응답 예시 |
| --- | --- | --- |
| `400 Bad Request` | path/query/body 검증 실패, 잘못된 enum 값, 필수 query parameter 누락, query parameter 타입 불일치 | `{ "message": "page 요청 파라미터 형식이 올바르지 않습니다." }` |
| `401 Unauthorized` | 인증이 필요한 API에 유효한 JWT가 없거나 JWT subject가 올바르지 않음 | 인증 실패 응답 |
| `403 Forbidden` | JWT 쿠키 기반 상태 변경 요청에서 CSRF 토큰이 없거나 일치하지 않음 | CSRF 실패 응답 |
| `404 Not Found` | 존재하지 않는 향수 또는 사용자 조회 | `{ "message": "존재하지 않는 향수 ID입니다." }` |
| `409 Conflict` | 중복 리뷰, 중복 위시리스트, DB unique constraint 충돌 | `{ "message": "이미 작성한 리뷰가 있습니다." }` |

## 인증

- 공개 조회 API는 인증 없이 호출할 수 있습니다.
- `/api/auth/me`, `/api/auth/me/profile`, 로그인 사용자 리뷰 조회, 리뷰 작성, 위시리스트 API는 JWT 인증이 필요합니다.
- `/api/auth/logout`은 만료되었거나 손상된 HttpOnly 인증 쿠키도 브라우저에서 제거할 수 있도록 인증 실패 시에도 쿠키 만료 응답을 반환합니다. 유효한 JWT 쿠키 기반 요청은 다른 상태 변경 API와 같이 CSRF 토큰이 필요합니다.
- JWT는 `Authorization: Bearer {token}` 헤더 또는 `PERFUME_ACCESS_TOKEN` HttpOnly 쿠키로 전달합니다.
- 회원가입, 로그인, OAuth 로그인 성공 응답은 `PERFUME_ACCESS_TOKEN`과 함께 브라우저에서 읽을 수 있는 `XSRF-TOKEN` 쿠키를 발급합니다.
- 프론트엔드가 API와 다른 사이트에서 호스팅되면 API 도메인의 쿠키를 JavaScript로 읽을 수 없습니다. 이 경우 인증 후 `GET /api/auth/csrf`를 호출해 응답 본문의 `csrfToken`을 보관하고 상태 변경 요청의 `X-XSRF-TOKEN` 헤더로 보내야 합니다.
- JWT 쿠키로 `POST`, `DELETE`, `PATCH` 요청을 보내는 경우 `XSRF-TOKEN` 쿠키 값과 같은 값을 `X-XSRF-TOKEN` 헤더에도 전달해야 합니다.
- 인증된 사용자 ID는 JWT subject에서 결정되며, 클라이언트가 `userId` query parameter로 지정할 수 없습니다.

## 값 목록

### Gender

| 값 | 의미 |
| --- | --- |
| `W` | 여성 |
| `M` | 남성 |
| `U` | 유니섹스 |

### Review season

요청/응답에서 한국어 표시값을 사용합니다.

```json
["봄", "여름", "가을", "겨울"]
```

### Review scent

요청/응답에서 한국어 표시값을 사용합니다.

```json
[
  "꽃 향",
  "나무 향",
  "청량한 향",
  "스파이시한 향",
  "달콤한 향",
  "디저트 향",
  "포근한 향",
  "풀 향",
  "상큼한 향",
  "과일 향",
  "허브 향",
  "흙내음"
]
```

## Auth API

### 로컬 회원가입

```http
POST /api/auth/signup
```

로컬 비밀번호 기반 계정을 생성합니다. 회원가입이 성공하면 백엔드는 `PERFUME_ACCESS_TOKEN` HttpOnly 쿠키와 `XSRF-TOKEN` 쿠키를 발급하고, 생성된 사용자 프로필을 반환합니다.

#### Request body

| 필드 | 타입 | 필수 | 검증 | 설명 |
| --- | --- | --- | --- | --- |
| `email` | string | yes | 이메일 형식, 최대 100자 | 로그인에 사용할 고유 이메일 |
| `password` | string | yes | 10~72자 | 로컬 로그인 비밀번호 |
| `name` | string | yes | 최대 24자 | 사용자 실명 또는 표시 이름 |
| `nickname` | string | yes | 최대 24자 | 공개되는 고유 닉네임 |
| `gender` | string | yes | 최대 1자 | 성별 코드 |
| `birthDate` | string | yes | 과거 날짜 | 생년월일. `yyyy-MM-dd` |
| `phoneNumber` | string | yes | 최대 15자 | 전화번호 |

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

#### Response `200 OK`

`Set-Cookie` 헤더:

```http
Set-Cookie: PERFUME_ACCESS_TOKEN={jwt}; Path=/; HttpOnly; Secure; SameSite=None
Set-Cookie: XSRF-TOKEN={csrfToken}; Path=/; Secure; SameSite=None
```

`SameSite` 값은 `app.auth.cookie.same-site` 설정을 따릅니다. `https://thescentlab.vercel.app`에서 API 도메인을 직접 호출하는 운영 배포에서는 `SameSite=None; Secure`가 필요합니다.

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

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | 요청 본문 검증 실패 | 검증 메시지 |
| `409 Conflict` | email 또는 nickname 중복 | `email already exists`, `nickname already exists` |

### 로컬 로그인

```http
POST /api/auth/login
```

로컬 비밀번호 기반 계정을 인증합니다. OAuth 전용 계정은 로컬 비밀번호가 없으므로 이 로그인 흐름에서 거부됩니다. 로그인 성공 시 JWT는 응답 본문에 노출하지 않고 `PERFUME_ACCESS_TOKEN` HttpOnly 쿠키로만 전달합니다.

#### Request body

| 필드 | 타입 | 필수 | 검증 | 설명 |
| --- | --- | --- | --- | --- |
| `email` | string | yes | 이메일 형식, 최대 100자 | 로컬 계정 이메일 |
| `password` | string | yes | 최대 72자 | 로컬 계정 비밀번호 |

```json
{
  "email": "user@example.com",
  "password": "secret-password"
}
```

#### Response `200 OK`

`Set-Cookie` 헤더:

```http
Set-Cookie: PERFUME_ACCESS_TOKEN={jwt}; Path=/; HttpOnly; Secure; SameSite=None
Set-Cookie: XSRF-TOKEN={csrfToken}; Path=/; Secure; SameSite=None
```

`SameSite` 값은 `app.auth.cookie.same-site` 설정을 따릅니다. `https://thescentlab.vercel.app`에서 API 도메인을 직접 호출하는 운영 배포에서는 `SameSite=None; Secure`가 필요합니다.

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

브라우저 클라이언트는 이후 요청에 쿠키가 포함되도록 credentials를 활성화해야 합니다.

```ts
await fetch(`${BACKEND_BASE_URL}/api/auth/login`, {
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
```

JWT 쿠키 기반 `POST`, `PATCH`, `DELETE` 요청에서는 로그인 이후 `GET /api/auth/csrf`로 받은 `csrfToken` 값을 `X-XSRF-TOKEN` 헤더로 보내야 합니다. 프론트와 API가 같은 사이트라면 로그인 응답으로 받은 `XSRF-TOKEN` 쿠키 값을 사용해도 됩니다.

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | 요청 본문 검증 실패 | 검증 메시지 |
| `401 Unauthorized` | email이 없거나 비밀번호가 맞지 않거나 OAuth 전용 계정임 | `invalid credentials` |

### CSRF 토큰 발급

```http
GET /api/auth/csrf
```

현재 JWT 쿠키 인증에 연결할 CSRF 토큰을 새로 발급합니다. 프론트엔드가 API와 다른 사이트에서 호스팅되는 경우, 상태 변경 API 호출 전에 이 엔드포인트를 호출해 응답 본문의 `csrfToken` 값을 보관해야 합니다.

#### Response `200 OK`

`Set-Cookie` 헤더:

```http
Set-Cookie: XSRF-TOKEN={csrfToken}; Path=/; Secure; SameSite=None
```

응답 본문:

```json
{
  "csrfToken": "base64url-random-token"
}
```

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `401 Unauthorized` | 유효한 JWT가 없음 | 인증 실패 응답 |

### 현재 사용자 조회

```http
GET /api/auth/me
```

현재 JWT 인증이 나타내는 사용자 정보를 조회합니다.

#### Response `200 OK`

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

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |

### 로그아웃

```http
POST /api/auth/logout
```

현재 인증 쿠키와 CSRF 쿠키를 만료시킵니다. 유효한 JWT 쿠키 기반 요청은 `X-XSRF-TOKEN` 헤더가 필요합니다. JWT가 없거나 디코딩할 수 없는 경우에도 브라우저의 HttpOnly 쿠키를 제거할 수 있도록 만료 쿠키를 응답합니다.

#### Response `204 No Content`

응답 본문은 없습니다.

`Set-Cookie` 헤더:

```http
Set-Cookie: PERFUME_ACCESS_TOKEN=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None
Set-Cookie: XSRF-TOKEN=; Path=/; Max-Age=0; Secure; SameSite=None
```

## Accord API

Accord API는 모두 공개 조회 API입니다. JWT 인증과 CSRF 토큰 없이 호출할 수 있습니다. 프론트엔드가 API와 다른 도메인에서
호스팅되고 공통 fetch 설정으로 쿠키를 포함한다면, 해당 프론트엔드 origin은 서버 CORS 허용 목록에 포함되어야 합니다.

### 어코드 목록 조회

```http
GET /api/accords
```

등록된 향수 데이터에서 사용할 수 있는 어코드 이름을 중복 없이 조회합니다. 향수 목록 필터 UI의 `accord` query parameter 후보로 사용할 수 있습니다.

#### Response `200 OK`

```json
[
  "citrus",
  "woody",
  "floral"
]
```

### 향 계열 상세 목록 조회

```http
GET /api/accords/detail
```

12개 향 계열의 ID, 이름, 설명, 대표 이미지 URL을 이름 오름차순으로 조회합니다. 향 계열 설명 페이지의 사이드바와 상세
본문 표시용 공개 API입니다. 상세 필드와 예시는 [Accord Detail REST API Specification](./accord-detail-api-spec.md)을
따릅니다.

#### Response `200 OK`

```json
[
  {
    "id": 1,
    "name": "Aromatic",
    "description": "아로마틱(Aromatic)은 고대 그리스어에서 출발한...",
    "imageUrl": "https://example.com/aromatic.jpg"
  }
]
```

### 향 계열 노트 목록 조회

```http
GET /api/accords/detail/{id}/notes
```

특정 향 계열에 속한 노트를 이름 오름차순으로 페이징 조회합니다.

#### Query parameters

| 이름 | 타입 | 필수 | 기본값 | 검증 | 설명 |
| --- | --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | `0` 이상 | 0부터 시작하는 페이지 번호 |
| `size` | integer | no | `30` | `1` 이상, `100` 이하 | 한 페이지 항목 수 |

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | `id`, `page`, `size` 검증 실패 | 검증 메시지 |
| `404 Not Found` | 향 계열 ID가 존재하지 않음 | `존재하지 않는 향 계열입니다.` |

### 향 계열 향수 목록 조회

```http
GET /api/accords/detail/{id}/perfumes
```

특정 향 계열에 속한 향수 목록을 해당 계열 비율 내림차순, 비율이 같으면 향수명 오름차순으로 페이징 조회합니다. 응답 형식은
향수 목록 조회와 같은 `PerfumeListResponse`입니다.

#### Query parameters

| 이름 | 타입 | 필수 | 기본값 | 검증 | 설명 |
| --- | --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | `0` 이상 | 0부터 시작하는 페이지 번호 |
| `size` | integer | no | `30` | `1` 이상, `100` 이하 | 한 페이지 항목 수 |

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | `id`, `page`, `size` 검증 실패 | 검증 메시지 |
| `404 Not Found` | 향 계열 ID가 존재하지 않음 | `존재하지 않는 향 계열입니다.` |

## Perfume API

### 향수 목록 조회

```http
GET /api/perfumes
```

향수 목록을 검색어, 성별, 어코드로 필터링하고 만족도 평점 기준으로 정렬해 조회합니다.

#### Query parameters

| 이름 | 타입 | 필수 | 기본값 | 검증 | 설명 |
| --- | --- | --- | --- | --- | --- |
| `keyword` | string | no | 없음 | 없음 | 향수명 또는 브랜드명 부분 일치 검색어 |
| `gender` | string | no | 없음 | `W`, `M`, `U` 중 하나 | 성별 필터 |
| `accord` | string | no | 없음 | 없음 | 어코드 이름 필터 |
| `sort` | string | no | `rating_desc` | `rating_asc`, `rating_desc` 중 하나 | 평균 만족도 정렬 방향 |
| `page` | integer | no | `0` | `0` 이상 | 0부터 시작하는 페이지 번호 |
| `size` | integer | no | `30` | `1` 이상, `100` 이하 | 한 페이지 항목 수 |

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `content` | array | 현재 페이지의 향수 카드 목록 |
| `content[].id` | number | 향수 ID |
| `content[].imageUrl` | string/null | 향수 이미지 URL |
| `content[].brand` | string | 브랜드명 |
| `content[].name` | string | 향수명 |
| `content[].gender` | string | `W`, `M`, `U` 중 하나 |
| `content[].rating` | number | 평균 만족도. 리뷰가 없으면 `0.0` |
| `content[].reviewCount` | number | 해당 향수 리뷰 수 |
| `pageNum` | number | 현재 페이지 번호 |
| `size` | number | 요청한 페이지 크기 |
| `hasNext` | boolean | 다음 페이지 존재 여부 |
| `totalElements` | number | 현재 필터 조건에 맞는 전체 향수 수 |
| `totalPages` | number | 현재 필터 조건과 `size` 기준 전체 페이지 수 |

```json
{
  "content": [
    {
      "id": 10806,
      "imageUrl": "https://example.com/perfume.jpg",
      "brand": "Clean",
      "name": "Skin",
      "gender": "U",
      "rating": 4.5,
      "reviewCount": 12
    }
  ],
  "pageNum": 0,
  "size": 30,
  "hasNext": true,
  "totalElements": 874,
  "totalPages": 30
}
```

#### Error cases

| HTTP status | 조건 |
| --- | --- |
| `400 Bad Request` | `gender`, `sort`, `page`, `size` 검증 실패 |

### 향수 상세 조회

```http
GET /api/perfumes/{id}
```

향수 기본 정보, 노트, 어코드, 리뷰 기반 통계를 조회합니다.

#### Path parameters

| 이름 | 타입 | 검증 | 설명 |
| --- | --- | --- | --- |
| `id` | number | `1` 이상 | 향수 ID |

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | number | 향수 ID |
| `imageUrl` | string/null | 향수 이미지 URL |
| `brand` | string | 브랜드명 |
| `name` | string | 향수명 |
| `gender` | string | `W`, `M`, `U` 중 하나 |
| `description` | string/null | 향수 설명 |
| `rating` | number | 평균 만족도. 리뷰가 없으면 `0.0` |
| `reviewCount` | number | 리뷰 수 |
| `notes.top` | array[string] | 탑 노트 목록 |
| `notes.mid` | array[string] | 미들 노트 목록 |
| `notes.base` | array[string] | 베이스 노트 목록 |
| `accords[].accordName` | string | 어코드 이름 |
| `accords[].ratio` | number | 어코드 비율 |
| `satisfaction` | object | 키 `1`~`5`, 값은 전체 리뷰 대비 비율 |
| `longevity` | object | 키 `1`~`3`, 값은 지속력 응답 리뷰 대비 비율 |
| `seasons` | object | 키 `봄`, `여름`, `가을`, `겨울`, 값은 계절 응답 리뷰 대비 비율 |

```json
{
  "id": 10806,
  "imageUrl": "https://example.com/perfume.jpg",
  "brand": "Clean",
  "name": "Skin",
  "gender": "U",
  "description": "Fresh musk perfume",
  "rating": 4.5,
  "reviewCount": 12,
  "notes": {
    "top": ["bergamot"],
    "mid": ["musk"],
    "base": ["amber"]
  },
  "accords": [
    {
      "accordName": "fresh",
      "ratio": 45
    }
  ],
  "satisfaction": {
    "1": 0,
    "2": 0,
    "3": 10,
    "4": 40,
    "5": 50
  },
  "longevity": {
    "1": 10,
    "2": 60,
    "3": 30
  },
  "seasons": {
    "봄": 60,
    "여름": 20,
    "가을": 20,
    "겨울": 0
  }
}
```

#### Error cases

| HTTP status | 조건 |
| --- | --- |
| `400 Bad Request` | `id`가 1보다 작음 |
| `404 Not Found` | 향수 ID가 존재하지 않음 |

## Review API

### 리뷰 목록 조회

```http
GET /api/perfumes/{id}/reviews
```

특정 향수의 리뷰 목록을 최신순으로 조회합니다. 동일한 작성 시각의 리뷰는 ID 내림차순으로 정렬됩니다.

#### Path parameters

| 이름 | 타입 | 검증 | 설명 |
| --- | --- | --- | --- |
| `id` | number | `1` 이상 | 향수 ID |

#### Query parameters

| 이름 | 타입 | 필수 | 기본값 | 검증 | 설명 |
| --- | --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | `0` 이상 | 0부터 시작하는 페이지 번호 |
| `size` | integer | no | `30` | `1` 이상, `100` 이하 | 한 페이지 항목 수 |

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `content` | array | 현재 페이지의 리뷰 목록 |
| `content[].nickname` | string | 리뷰 작성자 닉네임 |
| `content[].profileImageUrl` | string/null | 리뷰 작성자 프로필 이미지 URL |
| `content[].satisfaction` | number | 만족도 점수. `1`~`5` |
| `content[].longevity` | number/null | 지속력 점수. `1`~`3` |
| `content[].seasons` | array[string] | 선택한 계절 목록 |
| `content[].scents` | array[string] | 선택한 향 느낌 목록 |
| `content[].comment` | string/null | 리뷰 본문 |
| `content[].createdAt` | string | 리뷰 작성일. `yyyy-MM-dd` |
| `pageNum` | number | 현재 페이지 번호 |
| `size` | number | 요청한 페이지 크기 |
| `hasNext` | boolean | 다음 페이지 존재 여부 |
| `totalElements` | number | 해당 향수의 전체 리뷰 수 |
| `totalPages` | number | 해당 향수 리뷰의 전체 페이지 수 |

```json
{
  "content": [
    {
      "nickname": "scentlover",
      "profileImageUrl": "https://example.com/profile.png",
      "satisfaction": 5,
      "longevity": 2,
      "seasons": ["봄", "여름"],
      "scents": ["꽃 향", "청량한 향"],
      "comment": "가볍고 산뜻합니다.",
      "createdAt": "2026-05-05"
    }
  ],
  "pageNum": 0,
  "size": 30,
  "hasNext": false,
  "totalElements": 1,
  "totalPages": 1
}
```

#### Error cases

| HTTP status | 조건 |
| --- | --- |
| `400 Bad Request` | `id`, `page`, `size` 검증 실패 |
| `404 Not Found` | 향수 ID가 존재하지 않음 |

### 로그인 사용자 리뷰 조회

```http
GET /api/perfumes/{id}/reviews/me
```

현재 JWT 인증 사용자가 특정 향수에 작성한 리뷰를 조회합니다. 리뷰가 없으면 응답 본문 없이 `204 No Content`를 반환합니다.

#### Path parameters

| 이름 | 타입 | 검증 | 설명 |
| --- | --- | --- | --- |
| `id` | number | `1` 이상 | 리뷰 작성 여부를 확인할 향수 ID |

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | number | 리뷰 ID |
| `satisfaction` | number | 만족도 점수. `1`~`5` |
| `longevity` | number/null | 지속력 점수. `1`~`3` |
| `seasons` | array[string] | 선택한 계절 목록 |
| `scents` | array[string] | 선택한 향 느낌 목록 |
| `comment` | string/null | 리뷰 본문 |
| `disclaimerAgreed` | boolean | 면책 조항 동의 여부 |
| `createdAt` | string | 리뷰 작성일. `yyyy-MM-dd` |

```json
{
  "id": 55,
  "satisfaction": 4,
  "longevity": 2,
  "seasons": ["봄"],
  "scents": ["꽃 향"],
  "comment": "데일리로 좋아요.",
  "disclaimerAgreed": true,
  "createdAt": "2026-05-20"
}
```

#### Response `204 No Content`

현재 사용자가 해당 향수에 작성한 리뷰가 없으면 응답 본문은 없습니다.

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | `id` 검증 실패 | 검증 메시지 |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `404 Not Found` | 향수 ID가 존재하지 않음 | `존재하지 않는 향수 ID입니다.` |
| `404 Not Found` | JWT subject의 사용자 ID가 존재하지 않음 | `존재하지 않는 유저 ID입니다.` |

### 리뷰 작성

```http
POST /api/perfumes/{id}/reviews
```

특정 향수에 리뷰를 작성합니다.

리뷰 작성자는 JWT subject의 현재 인증 사용자 ID로 결정됩니다.

#### Path parameters

| 이름 | 타입 | 검증 | 설명 |
| --- | --- | --- | --- |
| `id` | number | `1` 이상 | 리뷰를 작성할 향수 ID |

#### Request body

| 필드 | 타입 | 필수 | 검증 | 설명 |
| --- | --- | --- | --- | --- |
| `satisfaction` | integer | yes | `1`~`5` | 향수 만족도 |
| `longevity` | integer/null | no | `1`~`3` | 지속력 |
| `seasons` | array[string]/null | no | 최대 4개, 중복 불가 | 계절 목록 |
| `scents` | array[string]/null | no | 최대 5개, 중복 불가 | 향 느낌 목록 |
| `comment` | string/null | no | 최대 1000자 | 리뷰 본문 |
| `disclaimerAgreed` | boolean | yes | `true`여야 함 | 면책 조항 동의 여부 |

```json
{
  "satisfaction": 5,
  "longevity": 2,
  "seasons": ["봄", "여름"],
  "scents": ["꽃 향", "청량한 향"],
  "comment": "가볍고 산뜻합니다.",
  "disclaimerAgreed": true
}
```

#### Response `201 Created`

```json
{
  "rating": 4.6,
  "totalReviewCount": 12,
  "satisfaction": {
    "1": 0,
    "2": 0,
    "3": 8,
    "4": 25,
    "5": 67
  },
  "longevity": {
    "1": 10,
    "2": 50,
    "3": 40
  },
  "seasons": {
    "봄": 42,
    "여름": 17,
    "가을": 33,
    "겨울": 8
  }
}
```

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | path/query/body 검증 실패 | 검증 메시지 |
| `400 Bad Request` | `disclaimerAgreed`가 `false` 또는 `null` | `면책 조항에 동의해야 합니다.` |
| `400 Bad Request` | 유효하지 않은 계절 값 | `유효하지 않은 계절 값입니다: {value}` |
| `400 Bad Request` | 유효하지 않은 향 값 | `유효하지 않은 향 값입니다: {value}` |
| `400 Bad Request` | 중복 계절 값 | `중복된 계절 값이 있습니다.` |
| `400 Bad Request` | 중복 향 값 | `중복된 향 값이 있습니다.` |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `403 Forbidden` | JWT 쿠키 기반 요청에서 CSRF 토큰이 없거나 일치하지 않음 | CSRF 실패 응답 |
| `404 Not Found` | 향수 ID가 존재하지 않음 | `존재하지 않는 향수 ID입니다.` |
| `404 Not Found` | JWT subject의 사용자 ID가 존재하지 않음 | `존재하지 않는 유저 ID입니다.` |
| `409 Conflict` | 같은 사용자가 같은 향수에 이미 리뷰를 작성함 | `이미 작성한 리뷰가 있습니다.` |

## Wishlist API

### 위시리스트 추가

```http
POST /api/wishlist/{perfumeId}
```

사용자의 위시리스트에 향수를 추가합니다.

위시리스트 소유자는 JWT subject의 현재 인증 사용자 ID로 결정됩니다.

#### Path parameters

| 이름 | 타입 | 검증 | 설명 |
| --- | --- | --- | --- |
| `perfumeId` | number | `1` 이상 | 추가할 향수 ID |

#### Response `201 Created`

응답 본문은 없습니다.

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | `perfumeId` 검증 실패 | 검증 메시지 |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `403 Forbidden` | JWT 쿠키 기반 요청에서 CSRF 토큰이 없거나 일치하지 않음 | CSRF 실패 응답 |
| `404 Not Found` | 향수 ID가 존재하지 않음 | `존재하지 않는 향수 ID입니다.` |
| `404 Not Found` | JWT subject의 사용자 ID가 존재하지 않음 | `존재하지 않는 유저 ID입니다.` |
| `409 Conflict` | 이미 위시리스트에 존재함 | `이미 위시리스트에 추가된 향수입니다.` |

### 위시리스트 삭제

```http
DELETE /api/wishlist/{perfumeId}
```

사용자의 위시리스트에서 향수를 제거합니다.

위시리스트 소유자는 JWT subject의 현재 인증 사용자 ID로 결정됩니다.

#### Path parameters

| 이름 | 타입 | 검증 | 설명 |
| --- | --- | --- | --- |
| `perfumeId` | number | `1` 이상 | 삭제할 향수 ID |

#### Response `204 No Content`

응답 본문은 없습니다.

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | `perfumeId` 검증 실패 | 검증 메시지 |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `403 Forbidden` | JWT 쿠키 기반 요청에서 CSRF 토큰이 없거나 일치하지 않음 | CSRF 실패 응답 |
| `404 Not Found` | 향수 ID가 존재하지 않음 | `존재하지 않는 향수 ID입니다.` |
| `404 Not Found` | JWT subject의 사용자 ID가 존재하지 않음 | `존재하지 않는 유저 ID입니다.` |
| `404 Not Found` | 위시리스트에 없는 향수 삭제 요청 | `위시리스트에 없는 향수입니다.` |

### 위시리스트 조회

```http
GET /api/wishlist
```

사용자의 위시리스트에 등록된 향수 카드 목록을 조회합니다.

위시리스트 소유자는 JWT subject의 현재 인증 사용자 ID로 결정됩니다.

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `[].perfumeId` | number | 향수 ID |
| `[].imageUrl` | string/null | 향수 이미지 URL |
| `[].brand` | string | 브랜드명 |
| `[].name` | string | 향수명 |

```json
[
  {
    "perfumeId": 10806,
    "imageUrl": "https://example.com/perfume.jpg",
    "brand": "Clean",
    "name": "Skin"
  }
]
```

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `404 Not Found` | JWT subject의 사용자 ID가 존재하지 않음 | `존재하지 않는 유저 ID입니다.` |
