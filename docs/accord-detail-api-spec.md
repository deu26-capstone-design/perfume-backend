# Accord Detail REST API Specification

이 문서는 향 계열 상세 페이지에서 사용하는 API 명세입니다.

## 공통 규칙

- Base path: `/api`
- Request/response content type: `application/json`
- 페이지 번호는 `0`부터 시작합니다.
- 모든 Accord Detail API는 공개 `GET` 조회 API입니다. JWT 인증과 CSRF 토큰 없이 호출할 수 있습니다.
- 프론트엔드와 API가 다른 도메인에 배포되어 있고 공통 fetch 설정으로 쿠키를 포함한다면, 프론트엔드 origin이 서버 CORS
  허용 목록에 포함되어야 합니다.
- 검증 또는 비즈니스 오류 응답은 기본적으로 다음 JSON 형태를 사용합니다.

```json
{
  "message": "오류 메시지"
}
```

## 공통 오류

| HTTP status | 발생 조건 | 응답 예시 |
| --- | --- | --- |
| `400 Bad Request` | path/query 검증 실패, query parameter 타입 불일치 | `{ "message": "..." }` |
| `404 Not Found` | 존재하지 않는 향 계열 ID 조회 | `{ "message": "존재하지 않는 향 계열입니다." }` |

## Accord Detail API

### 향 계열 전체 목록 조회

```http
GET /api/accords/detail
```

12개 향 계열의 기본 정보를 이름 오름차순으로 조회합니다. 좌측 사이드바 목록 및 상세 정보 표시에 사용합니다.

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `[].id` | number | 향 계열 ID |
| `[].name` | string | 향 계열 이름 |
| `[].description` | string | 향 계열 설명 |
| `[].imageUrl` | string | 향 계열 대표 이미지 URL |

```json
[
  {
    "id": 1,
    "name": "Aromatic",
    "description": "아로마틱(Aromatic)은 고대 그리스어에서 출발한...",
    "imageUrl": "https://example.com/aromatic.jpg"
  },
  {
    "id": 2,
    "name": "Citrus",
    "description": "시트러스(Citrus)는 지중해 햇살 아래...",
    "imageUrl": "https://example.com/citrus.jpg"
  }
]
```

---

### 향 계열 노트 목록 조회

```http
GET /api/accords/detail/{id}/notes
```

특정 향 계열에 속한 노트 목록을 이름 오름차순, 페이징하여 조회합니다.

#### Path parameters

| 이름 | 타입 | 검증 | 설명 |
| --- | --- | --- | --- |
| `id` | number | `1` 이상 | 향 계열 ID |

#### Query parameters

| 이름 | 타입 | 필수 | 기본값 | 검증 | 설명 |
| --- | --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | `0` 이상 | 0부터 시작하는 페이지 번호 |
| `size` | integer | no | `30` | `1` 이상, `100` 이하 | 한 페이지 항목 수 |

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `content` | array | 현재 페이지의 노트 목록 |
| `content[].name` | string | 노트 이름 |
| `content[].imageUrl` | string | 노트 이미지 URL |
| `pageNum` | number | 현재 페이지 번호 |
| `size` | number | 요청한 페이지 크기 |
| `hasNext` | boolean | 다음 페이지 존재 여부 |
| `totalElements` | number | 해당 향 계열의 전체 노트 수 |
| `totalPages` | number | 전체 페이지 수 |

```json
{
  "content": [
    {
      "name": "건초",
      "imageUrl": "https://fimgs.net/mdimg/sastojci/t.395.jpg"
    },
    {
      "name": "다바나",
      "imageUrl": "https://fimgs.net/mdimg/sastojci/t.907.jpg"
    }
  ],
  "pageNum": 0,
  "size": 30,
  "hasNext": false,
  "totalElements": 28,
  "totalPages": 1
}
```

#### Error cases

| HTTP status | 조건 |
| --- | --- |
| `400 Bad Request` | `id`, `page`, `size` 검증 실패 |
| `404 Not Found` | 향 계열 ID가 존재하지 않음 |

---

### 향 계열 향수 목록 조회

```http
GET /api/accords/detail/{id}/perfumes
```

특정 향 계열에 속한 향수 목록을 해당 계열 비율 내림차순으로 페이징하여 조회합니다. 비율이 같은 경우 향수명 오름차순으로 정렬합니다.

#### Path parameters

| 이름 | 타입 | 검증 | 설명 |
| --- | --- | --- | --- |
| `id` | number | `1` 이상 | 향 계열 ID |

#### Query parameters

| 이름 | 타입 | 필수 | 기본값 | 검증 | 설명 |
| --- | --- | --- | --- | --- | --- |
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
| `content[].reviewCount` | number | 리뷰 수 |
| `pageNum` | number | 현재 페이지 번호 |
| `size` | number | 요청한 페이지 크기 |
| `hasNext` | boolean | 다음 페이지 존재 여부 |
| `totalElements` | number | 해당 향 계열 전체 향수 수 |
| `totalPages` | number | 전체 페이지 수 |

```json
{
  "content": [
    {
      "id": 10806,
      "imageUrl": "https://fimgs.net/mdimg/perfume-thumbs/375x500.10806.webp",
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
  "totalElements": 502,
  "totalPages": 17
}
```

#### Error cases

| HTTP status | 조건 |
| --- | --- |
| `400 Bad Request` | `id`, `page`, `size` 검증 실패 |
| `404 Not Found` | 향 계열 ID가 존재하지 않음 |
