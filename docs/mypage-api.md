# Mypage REST API Specification

이 문서는 마이페이지에서 사용하는 회원 정보 조회/수정, 내 리뷰 관리, 위시리스트 API 명세입니다.

## 공통 규칙

- Base path: `/api`
- Request/response content type: `application/json`
- 모든 Mypage API는 JWT 인증이 필요합니다. `PERFUME_ACCESS_TOKEN` HttpOnly 쿠키 또는 `Authorization: Bearer {token}` 헤더로 전달합니다.
- `PATCH`, `DELETE` 요청은 CSRF 토큰이 필요합니다. `X-XSRF-TOKEN` 헤더에 `XSRF-TOKEN` 쿠키 값을 담아 전달해야 합니다. 프론트엔드가 API와 다른 도메인에 배포된 경우 `GET /api/auth/csrf`로 토큰을 발급받아 사용합니다.
- 검증 또는 비즈니스 오류 응답은 기본적으로 다음 JSON 형태를 사용합니다.

```json
{
  "message": "오류 메시지"
}
```

## 공통 오류

| HTTP status | 발생 조건 | 응답 예시 |
| --- | --- | --- |
| `400 Bad Request` | body 검증 실패 | `{ "message": "..." }` |
| `401 Unauthorized` | JWT가 없거나 유효하지 않음 | `{ "message": "인증이 필요합니다." }` |
| `403 Forbidden` | CSRF 토큰이 없거나 일치하지 않음 | `{ "message": "CSRF 토큰이 필요합니다." }` |
| `409 Conflict` | 다른 사용자가 이미 사용 중인 닉네임 | `{ "message": "nickname already exists" }` |

## Mypage API

### 회원 정보 조회

```http
GET /api/auth/me
```

현재 로그인한 사용자의 프로필 정보를 반환합니다.

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `userId` | number | 사용자 ID |
| `email` | string | 이메일 주소 |
| `name` | string | 이름 |
| `nickname` | string | 닉네임 |
| `gender` | string | 성별 코드 (`M`, `W`, `U`) |
| `birthDate` | string | 생년월일 (`yyyy-MM-dd`) |
| `phoneNumber` | string | 휴대폰 번호 |
| `oauthProvider` | string/null | 연결된 OAuth 제공자. 로컬 계정이면 `null` |
| `profileCompleted` | boolean | 프로필 완성 여부 |

```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "nickname": "gildong",
  "gender": "M",
  "birthDate": "1995-03-15",
  "phoneNumber": "01012345678",
  "oauthProvider": "GOOGLE",
  "profileCompleted": true
}
```

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아니거나 현재 사용자를 찾을 수 없음 | `인증이 필요합니다.` |

### 회원 정보 수정

```http
PATCH /api/auth/me
```

현재 로그인한 사용자의 닉네임, 휴대폰 번호를 수정합니다.

#### Request body

| 필드 | 타입 | 필수 | 검증 | 설명 |
| --- | --- | --- | --- | --- |
| `nickname` | string | yes | 최대 24자 | 닉네임 |
| `phoneNumber` | string | yes | 최대 15자 | 휴대폰 번호 |

```json
{
  "nickname": "gildong",
  "phoneNumber": "01012345678"
}
```

#### Response `200 OK`

```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "nickname": "gildong",
  "gender": "M",
  "birthDate": "1995-03-15",
  "phoneNumber": "01012345678",
  "oauthProvider": "GOOGLE",
  "profileCompleted": true
}
```

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | 요청 본문 검증 실패 | 검증 메시지 |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아니거나 현재 사용자를 찾을 수 없음 | `인증이 필요합니다.` |
| `403 Forbidden` | JWT 쿠키 기반 요청에서 CSRF 토큰이 없거나 일치하지 않음 | `CSRF 토큰이 필요합니다.` |
| `409 Conflict` | 다른 사용자가 이미 사용 중인 닉네임 | `nickname already exists` |

## Review API

### 향수 리뷰 목록 조회

```http
GET /api/perfumes/{id}/reviews
```

특정 향수의 리뷰 목록을 최신순으로 조회합니다.

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
| `content[].id` | number | 리뷰 ID. 수정/삭제 시 사용 |
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
      "id": 42,
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

### 내가 작성한 리뷰 목록 조회

```http
GET /api/auth/me/reviews
```

현재 로그인한 사용자가 작성한 리뷰 목록을 최신순으로 반환합니다.

#### Query parameters

| 이름 | 타입 | 필수 | 기본값 | 검증 | 설명 |
| --- | --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | `0` 이상 | 0부터 시작하는 페이지 번호 |
| `size` | integer | no | `30` | `1` 이상, `100` 이하 | 한 페이지 항목 수 |

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `content` | array | 현재 페이지의 리뷰 목록 |
| `content[].reviewId` | number | 리뷰 ID. 수정/삭제 시 사용 |
| `content[].perfumeId` | number | 향수 ID |
| `content[].perfumeImageUrl` | string/null | 향수 이미지 URL |
| `content[].perfumeName` | string | 향수 이름 |
| `content[].brand` | string | 브랜드명 |
| `content[].satisfaction` | number | 만족도. `1`~`5` |
| `content[].longevity` | number/null | 지속력. `1`~`3`. 선택하지 않으면 `null` |
| `content[].seasons` | array[string] | 선택한 계절 목록 |
| `content[].scents` | array[string] | 선택한 향 목록 |
| `content[].createdAt` | string | 작성일. `yyyy-MM-dd` |
| `content[].comment` | string/null | 리뷰 본문 |
| `pageNum` | number | 현재 페이지 번호 |
| `size` | number | 페이지 크기 |
| `hasNext` | boolean | 다음 페이지 존재 여부 |
| `totalElements` | number | 전체 리뷰 수 |
| `totalPages` | number | 전체 페이지 수 |

```json
{
  "content": [
    {
      "reviewId": 42,
      "perfumeId": 1234,
      "perfumeImageUrl": "https://example.com/perfume.jpg",
      "perfumeName": "Air",
      "brand": "Clean",
      "satisfaction": 4,
      "longevity": 2,
      "seasons": ["봄", "여름"],
      "scents": ["청량한 향", "꽃 향"],
      "createdAt": "2026-05-19",
      "comment": "가볍고 산뜻합니다."
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

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | `page`, `size` 검증 실패 | 검증 메시지 |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아니거나 현재 사용자를 찾을 수 없음 | `인증이 필요합니다.` |

### 리뷰 수정

```http
PATCH /api/perfumes/reviews/{id}
```

본인이 작성한 리뷰를 수정합니다.

수정 권한은 JWT subject의 현재 인증 사용자 ID로 판별합니다.

> ⚠️ PATCH이지만 전체 교체 방식입니다. 기존 값을 유지하려면 현재 값을 그대로 담아서 보내야 합니다.

#### Path parameters

| 이름 | 타입 | 검증 | 설명 |
| --- | --- | --- | --- |
| `id` | number | `1` 이상 | 수정할 리뷰 ID |

#### Request body

| 필드 | 타입 | 필수 | 검증 | 설명 |
| --- | --- | --- | --- | --- |
| `satisfaction` | integer | yes | `1`~`5` | 향수 만족도 |
| `longevity` | integer/null | no | `1`~`3` | 지속력. 안 보내면 삭제됨 |
| `seasons` | array[string]/null | no | 최대 4개, 중복 불가 | 계절 목록. 안 보내면 삭제됨 |
| `scents` | array[string]/null | no | 최대 5개, 중복 불가 | 향 느낌 목록. 안 보내면 삭제됨 |
| `comment` | string/null | no | 최대 1000자 | 리뷰 본문. 안 보내면 삭제됨 |
| `disclaimerAgreed` | boolean | yes | `true`여야 함 | 면책 조항 동의 여부 |

`seasons` 유효값: `"봄"`, `"여름"`, `"가을"`, `"겨울"`

`scents` 유효값: `"꽃 향"`, `"나무 향"`, `"청량한 향"`, `"스파이시한 향"`, `"달콤한 향"`, `"디저트 향"`, `"포근한 향"`, `"풀 향"`, `"상큼한 향"`, `"과일 향"`, `"허브 향"`, `"흙내음"`

```json
{
  "satisfaction": 4,
  "longevity": 1,
  "seasons": ["가을", "겨울"],
  "scents": ["나무 향"],
  "comment": "잔향이 은은합니다.",
  "disclaimerAgreed": true
}
```

#### Response `204 No Content`

응답 본문은 없습니다.

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | path/body 검증 실패 | 검증 메시지 |
| `400 Bad Request` | `disclaimerAgreed`가 `false` 또는 `null` | `면책 조항에 동의해야 합니다.` |
| `400 Bad Request` | 유효하지 않은 계절 값 | `유효하지 않은 계절 값입니다: {value}` |
| `400 Bad Request` | 유효하지 않은 향 값 | `유효하지 않은 향 값입니다: {value}` |
| `400 Bad Request` | 중복 계절 값 | `중복된 계절 값이 있습니다.` |
| `400 Bad Request` | 중복 향 값 | `중복된 향 값이 있습니다.` |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `403 Forbidden` | JWT 쿠키 기반 요청에서 CSRF 토큰이 없거나 일치하지 않음 | CSRF 실패 응답 |
| `403 Forbidden` | 다른 사용자의 리뷰를 수정하려는 경우 | `본인의 리뷰만 수정할 수 있습니다.` |
| `404 Not Found` | 리뷰 ID가 존재하지 않음 | `존재하지 않는 리뷰입니다.` |

### 리뷰 삭제

```http
DELETE /api/perfumes/reviews/{id}
```

본인이 작성한 리뷰를 삭제합니다.

삭제 권한은 JWT subject의 현재 인증 사용자 ID로 판별합니다.

#### Path parameters

| 이름 | 타입 | 검증 | 설명 |
| --- | --- | --- | --- |
| `id` | number | `1` 이상 | 삭제할 리뷰 ID |

#### Response `204 No Content`

응답 본문은 없습니다.

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | `id` 검증 실패 | 검증 메시지 |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `403 Forbidden` | JWT 쿠키 기반 요청에서 CSRF 토큰이 없거나 일치하지 않음 | CSRF 실패 응답 |
| `403 Forbidden` | 다른 사용자의 리뷰를 삭제하려는 경우 | `본인의 리뷰만 삭제할 수 있습니다.` |
| `404 Not Found` | 리뷰 ID가 존재하지 않음 | `존재하지 않는 리뷰입니다.` |

## Wishlist API

### 위시리스트 추가

```http
POST /api/wishlist/{perfumeId}
```

향수를 위시리스트에 추가합니다.

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
| `409 Conflict` | 이미 위시리스트에 추가된 향수 | `이미 위시리스트에 추가된 향수입니다.` |

### 위시리스트 삭제

```http
DELETE /api/wishlist/{perfumeId}
```

향수를 위시리스트에서 제거합니다.

#### Path parameters

| 이름 | 타입 | 검증 | 설명 |
| --- | --- | --- | --- |
| `perfumeId` | number | `1` 이상 | 제거할 향수 ID |

#### Response `204 No Content`

응답 본문은 없습니다.

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | `perfumeId` 검증 실패 | 검증 메시지 |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `403 Forbidden` | JWT 쿠키 기반 요청에서 CSRF 토큰이 없거나 일치하지 않음 | CSRF 실패 응답 |
| `404 Not Found` | 향수 ID가 존재하지 않음 또는 위시리스트에 없는 향수 | `위시리스트에 없는 향수입니다.` |

### 위시리스트 목록 조회

```http
GET /api/wishlist/page
```

현재 로그인한 사용자의 위시리스트 향수 목록을 페이징하여 조회합니다.

#### Query parameters

| 이름 | 타입 | 필수 | 기본값 | 검증 | 설명 |
| --- | --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | `0` 이상 | 0부터 시작하는 페이지 번호 |
| `size` | integer | no | `30` | `1` 이상, `100` 이하 | 한 페이지 항목 수 |

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `content` | array | 현재 페이지의 위시리스트 향수 목록 |
| `content[].perfumeId` | number | 향수 ID |
| `content[].imageUrl` | string/null | 향수 이미지 URL |
| `content[].brand` | string | 브랜드명 |
| `content[].name` | string | 향수 이름 |
| `pageNum` | number | 현재 페이지 번호 |
| `size` | number | 페이지 크기 |
| `hasNext` | boolean | 다음 페이지 존재 여부 |
| `totalElements` | number | 위시리스트 전체 항목 수 |
| `totalPages` | number | 전체 페이지 수 |

```json
{
  "content": [
    {
      "perfumeId": 1234,
      "imageUrl": "https://example.com/perfume.jpg",
      "brand": "Clean",
      "name": "Air"
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

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | `page`, `size` 검증 실패 | 검증 메시지 |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |

## 향수 응답 변경 사항

이번 마이페이지 기능 추가로 인해 다음 향수 조회 API 응답에 `wishlisted` 필드가 추가되었습니다.

- `GET /api/perfumes` — `content[].wishlisted`
- `GET /api/perfumes/{id}` — `wishlisted`
- `GET /api/accords/detail/{id}/perfumes` — `content[].wishlisted`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `wishlisted` | boolean | 현재 로그인한 사용자의 위시리스트 포함 여부. 비로그인 시 항상 `false` |
