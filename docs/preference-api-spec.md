# 향 선호도 API 명세

이 문서는 향 선호도 테스트 및 결과 조회 API 명세입니다.

> **인증**: 모든 API는 JWT 인증이 필요합니다. 요청자 식별은 JWT subject에서 결정됩니다.

## 공통 오류

| HTTP status | 발생 조건 | 응답 예시 |
| --- | --- | --- |
| `400 Bad Request` | body 검증 실패, 유효하지 않은 선택지 | `{ "message": "오류 메시지" }` |
| `404 Not Found` | 존재하지 않는 사용자 | `{ "message": "존재하지 않는 유저 ID입니다." }` |
| `409 Conflict` | 이미 완료한 테스트에 재응시 또는 중간 저장 시도 | `{ "message": "이미 완료한 테스트입니다." }` |

## 향 선호도 테스트

### 테스트 문항 목록 조회

```http
GET /api/preference/test/questions
```

테스트 화면 진입 시 호출합니다. 1번~12번 문항과 선택지(A/B/C/D) 텍스트를 반환합니다. 응답은 항상 고정된 정적 데이터입니다.

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `[].questionNumber` | integer | 문항 번호 (`1`~`12`) |
| `[].question` | string | 문항 질문 텍스트 |
| `[].options` | object | 선택지 맵. 키는 `A`/`B`/`C`/`D`, 값은 선택지 텍스트 |

```json
[
  {
    "questionNumber": 1,
    "question": "이번 주말, 어디서 시간을 보내고 싶어?",
    "options": {
      "A": "꽃향기 가득한 플라워 마켓",
      "B": "비 온 뒤 숲속 산책로",
      "C": "향신료 가득한 빈티지 마켓",
      "D": "바닐라 캔들 켜진 카페"
    }
  }
]
```

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |

### 테스트 진행 상태 조회

```http
GET /api/preference/test/progress
```

테스트 페이지 진입 시 호출합니다. 테스트 완료 여부와 이어하기 데이터를 반환합니다.

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `testCompleted` | boolean | 테스트 완료 여부 |
| `answers` | object | 저장된 진행 답변. 키는 문항 번호(`1`~`12`), 값은 선택지(`A`/`B`/`C`/`D`). 없으면 빈 객체 |

```json
{
  "testCompleted": false,
  "answers": {
    "1": "A",
    "2": "C",
    "3": "B"
  }
}
```

응답 값에 따른 프론트 처리:

| 조건 | 동작 |
| --- | --- |
| `testCompleted: true` | 결과 페이지로 이동 |
| `testCompleted: false`, `answers: {}` | 1번 문항부터 시작 |
| `testCompleted: false`, `answers: {...}` | 마지막 답변 다음 문항부터 이어서 시작 |

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `404 Not Found` | 존재하지 않는 사용자 | `존재하지 않는 유저 ID입니다.` |
| `500 Internal Server Error` | 저장된 진행 데이터가 손상된 경우 | `진행 상태 데이터가 손상되었습니다.` |

### 테스트 진행 상태 저장

```http
PATCH /api/preference/test/progress
```

문항 답변 시마다 현재 전체 답변 상태를 저장합니다. 뒤로가기 후 답변을 수정한 경우에도 현재 전체 상태를 그대로 전달하면 기존 저장값을 덮어씁니다.

#### Request body

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `answers` | object | yes | 현재까지 답변한 전체 문항 맵. 키는 문항 번호(`1`~`12`), 값은 선택지(`A`/`B`/`C`/`D`) |

```json
{
  "answers": {
    "1": "A",
    "2": "C",
    "3": "B"
  }
}
```

#### Response `204 No Content`

응답 본문은 없습니다.

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | `answers`가 null | 검증 메시지 |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `403 Forbidden` | JWT 쿠키 기반 요청에서 CSRF 토큰이 없거나 일치하지 않음 | CSRF 실패 응답 |
| `404 Not Found` | 존재하지 않는 사용자 | `존재하지 않는 유저 ID입니다.` |
| `409 Conflict` | 이미 테스트를 완료한 사용자 | `이미 완료한 테스트입니다.` |

### 테스트 제출

```http
POST /api/preference/test
```

12문항을 모두 완료한 후 테스트를 제출합니다. 테스트는 재응시가 불가능합니다. 제출 성공 시 진행 상태(`in_progress_answers`)는 초기화됩니다.

#### Request body

| 필드 | 타입 | 필수 | 검증 | 설명 |
| --- | --- | --- | --- | --- |
| `answers` | object | yes | 키가 정확히 `1`~`12`, 값은 `A`/`B`/`C`/`D` 중 하나 | 12문항 전체 답변 |

```json
{
  "answers": {
    "1": "A",
    "2": "C",
    "3": "B",
    "4": "D",
    "5": "A",
    "6": "C",
    "7": "B",
    "8": "D",
    "9": "A",
    "10": "C",
    "11": "B",
    "12": "D"
  }
}
```

#### Response `201 Created`

응답 본문은 없습니다.

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `400 Bad Request` | `answers` 크기가 12가 아닌 경우 | `12문항 모두 응답해야 합니다.` |
| `400 Bad Request` | 키가 정확히 `1`~`12`가 아닌 경우 | `1번~12번 문항을 모두 응답해야 합니다.` |
| `400 Bad Request` | 유효하지 않은 선택지 | `유효하지 않은 선택지입니다: {value}` |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `403 Forbidden` | JWT 쿠키 기반 요청에서 CSRF 토큰이 없거나 일치하지 않음 | CSRF 실패 응답 |
| `404 Not Found` | 존재하지 않는 사용자 | `존재하지 않는 유저 ID입니다.` |
| `409 Conflict` | 이미 테스트를 완료한 사용자 | `이미 완료한 테스트입니다.` |

## 향 선호도 결과 조회

### 향 선호도 Top 5 조회

```http
GET /api/preference/top5
```

사용자의 향 선호도 Top 5를 조회합니다. 마이페이지 선호도 그래프 렌더링에 사용합니다.

점수는 테스트 원점수와 리뷰 누적 점수를 합산한 뒤 100%로 정규화한 실제 퍼센트 값입니다. 프론트에서 1위 점수를 기준으로 막대 길이를 보정합니다.

리뷰 작성·수정·삭제 시 점수가 즉시 반영됩니다.

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `testCompleted` | boolean | 테스트 완료 여부. `false`이면 `top5`는 빈 배열 |
| `top5` | array | 상위 5개 향 계열 목록. 점수 내림차순 정렬 |
| `top5[].scentName` | string | 향 계열 영어 이름 (`Floral`, `Woody`, `Fresh`, `Spicy`, `Sweet`, `Gourmand`, `Musky`, `Green`, `Citrus`, `Fruity`, `Aromatic`, `Earthy/Smoky` 중 하나) |
| `top5[].score` | number | 실제 퍼센트 점수 (소수점 1자리) |

```json
{
  "testCompleted": true,
  "top5": [
    { "scentName": "Floral",  "score": 22.2 },
    { "scentName": "Musky",   "score": 18.5 },
    { "scentName": "Citrus",  "score": 15.1 },
    { "scentName": "Fruity",  "score": 12.7 },
    { "scentName": "Green",   "score": 9.8  }
  ]
}
```

테스트 미완료 시:

```json
{
  "testCompleted": false,
  "top5": []
}
```

동점 처리 우선순위:

1. 점수 높은 순
2. 해당 계열 그룹이 Top 5 안에 많이 포함된 순 (그룹: A — Floral·Musky·Fruity, B — Fresh·Citrus·Green, C — Woody·Aromatic·Earthy/Smoky·Spicy, D — Sweet·Gourmand)
3. 해당 계열 그룹의 총점 높은 순
4. 계열명 알파벳 순

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `404 Not Found` | 존재하지 않는 사용자 | `존재하지 않는 유저 ID입니다.` |

### 향 선호도 전체 점수 조회

```http
GET /api/preference
```

사용자의 12개 향 계열 전체 점수를 조회합니다.

#### Response `200 OK`

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `testCompleted` | boolean | 테스트 완료 여부. `false`이면 `scores`는 빈 객체 |
| `scores` | object | 12개 향 계열 전체 점수. 키는 향 계열 영어 이름 (`Aromatic`, `Citrus`, `Earthy/Smoky`, `Floral`, `Fresh`, `Fruity`, `Gourmand`, `Green`, `Musky`, `Spicy`, `Sweet`, `Woody`), 값은 퍼센트 점수 (소수점 1자리) |

```json
{
  "testCompleted": true,
  "scores": {
    "Floral":      22.2,
    "Woody":        8.3,
    "Fresh":       11.2,
    "Spicy":        6.1,
    "Sweet":        9.8,
    "Gourmand":     7.4,
    "Musky":       18.5,
    "Green":        9.8,
    "Citrus":      15.1,
    "Fruity":      12.7,
    "Aromatic":     5.6,
    "Earthy/Smoky": 3.1
  }
}
```

테스트 미완료 시:

```json
{
  "testCompleted": false,
  "scores": {}
}
```

#### Error cases

| HTTP status | 조건 | 대표 메시지 |
| --- | --- | --- |
| `401 Unauthorized` | 유효한 JWT가 없거나 JWT subject가 정수 사용자 ID가 아님 | 인증 실패 응답 |
| `404 Not Found` | 존재하지 않는 사용자 | `존재하지 않는 유저 ID입니다.` |
