# Layering Recommendations API

## POST `/api/layering/recommendations`

서로 다른 향수 2개의 어코드와 노트 데이터를 기반으로 deterministic 레이어링 추천 결과를 반환합니다.

## 인증

필요 없습니다.

- `PERFUME_ACCESS_TOKEN` 쿠키 불필요
- `Authorization` 헤더 불필요
- `X-XSRF-TOKEN` 헤더 불필요

## Request

```json
{
  "perfumeIds": [1, 2]
}
```

| field | type | required | rule |
| --- | --- | --- | --- |
| `perfumeIds` | `number[]` | yes | 정확히 2개 |
| `perfumeIds[]` | `number` | yes | 1 이상 |

## Response `200 OK`

```json
{
  "inputPerfumes": [
    {
      "id": 1,
      "brand": "Brand A",
      "name": "Perfume A",
      "dominantAccords": [
        {
          "name": "Floral",
          "ratio": 45
        }
      ]
    },
    {
      "id": 2,
      "brand": "Brand B",
      "name": "Perfume B",
      "dominantAccords": [
        {
          "name": "Woody",
          "ratio": 40
        }
      ]
    }
  ],
  "recommendation": {
    "candidateType": "PAIR",
    "recommended": true,
    "decision": "RECOMMENDED",
    "score": 82,
    "title": "밝은 플로럴와 차분한 우디",
    "summary": "조화로운 설명 문장입니다.",
    "color": {
      "name": "Clear Mint",
      "hex": "#CCDDEE",
      "sourceAccord": "Floral",
      "targetAccord": "Woody",
      "description": "무드 컬러 설명"
    },
    "bestFor": ["봄", "오피스", "데이트"],
    "reasons": ["추천 이유"],
    "warnings": ["주의점"],
    "scoreBreakdown": {
      "matrix": 40,
      "structure": 25,
      "balance": 17,
      "penalty": 0
    }
  }
}
```

## Error Response

에러 응답은 공통적으로 아래 형태입니다.

```json
{
  "message": "오류 메시지"
}
```

| status | case | message |
| --- | --- | --- |
| `400` | 향수 ID 개수가 2개가 아님 | `향수는 정확히 2개를 선택해야 합니다.` |
| `400` | 같은 향수 ID 2개 요청 | `서로 다른 향수 2개를 선택해야 합니다.` |
| `400` | 향수 어코드 데이터 부족 | `레이어링 추천에 필요한 향수 어코드 데이터가 부족합니다.` |
| `404` | 존재하지 않는 향수 ID 포함 | `존재하지 않는 향수 ID가 포함되어 있습니다.` |

## UI Mapping

| response field | suggested use |
| --- | --- |
| `recommendation.recommended` | 추천/비추천 주요 상태 분기 |
| `recommendation.decision` | 배지, 상태 텍스트 |
| `recommendation.score` | 점수 표시 |
| `recommendation.title` | 결과 카드 제목 |
| `recommendation.summary` | 결과 카드 설명 |
| `recommendation.color.hex` | 결과 카드 포인트 컬러 |
| `recommendation.bestFor` | 상황/계절 태그 |
| `recommendation.reasons` | 추천 근거 목록 |
| `recommendation.warnings` | 주의점 목록 |
| `recommendation.scoreBreakdown` | 상세 점수 그래프 |

## TypeScript Example

```ts
type LayeringRecommendationRequest = {
  perfumeIds: [number, number];
};

async function getLayeringRecommendation(
  apiBaseUrl: string,
  request: LayeringRecommendationRequest,
) {
  const response = await fetch(`${apiBaseUrl}/api/layering/recommendations`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const error = (await response.json()) as { message?: string };
    throw new Error(error.message ?? "레이어링 추천 요청에 실패했습니다.");
  }

  return response.json();
}
```
