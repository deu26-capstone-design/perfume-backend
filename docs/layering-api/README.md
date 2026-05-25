# Layering API Guide

레이어링 추천 기능만 분리한 프론트엔드 연동 문서입니다.

## 문서 구성

- [recommendations.md](recommendations.md): 추천 API 호출 방법, 예시, 에러 처리
- [schemas.md](schemas.md): 요청/응답 필드 정의, enum/값 목록

## 빠른 요약

| Method | Path | 인증 | CSRF | 설명 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/layering/recommendations` | 불필요 | 불필요 | 향수 2개 기반 레이어링 추천 |

## 기본 호출

```ts
const response = await fetch(`${API_BASE_URL}/api/layering/recommendations`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    perfumeIds: [1, 2],
  }),
});

if (!response.ok) {
  const error = await response.json();
  throw new Error(error.message);
}

const data = await response.json();
```

## 프론트 구현 포인트

- 이 API는 공개 API입니다. 로그인 쿠키, `Authorization` 헤더, `X-XSRF-TOKEN` 헤더가 필요 없습니다.
- `perfumeIds`는 정확히 2개여야 합니다.
- 같은 향수 ID 2개를 보내면 `400`이 반환됩니다.
- 결과의 `recommended`는 빠른 UI 분기에 쓰고, 상세 배지는 `decision`을 기준으로 표시하면 됩니다.
- 점수 설명 UI는 `scoreBreakdown`, `reasons`, `warnings`, `bestFor`, `color`를 조합하면 됩니다.
