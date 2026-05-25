# Layering API Schemas

## LayeringRecommendationRequest

```ts
type LayeringRecommendationRequest = {
  perfumeIds: [number, number];
};
```

| field | type | nullable | description |
| --- | --- | --- | --- |
| `perfumeIds` | `number[]` | no | 추천에 사용할 서로 다른 향수 ID 2개 |

## LayeringRecommendationResponse

```ts
type LayeringRecommendationResponse = {
  inputPerfumes: InputPerfumeResponse[];
  recommendation: LayeringRecommendation;
};
```

## InputPerfumeResponse

```ts
type InputPerfumeResponse = {
  id: number;
  brand: string;
  name: string;
  dominantAccords: LayeringAccordResponse[];
};
```

| field | type | nullable | description |
| --- | --- | --- | --- |
| `id` | `number` | no | 향수 ID |
| `brand` | `string` | no | 브랜드명 |
| `name` | `string` | no | 향수명 |
| `dominantAccords` | `LayeringAccordResponse[]` | no | 추천 계산에 사용된 주요 어코드 |

## LayeringAccordResponse

```ts
type LayeringAccordResponse = {
  name: string;
  ratio: number;
};
```

| field | type | nullable | description |
| --- | --- | --- | --- |
| `name` | `string` | no | 어코드 이름 |
| `ratio` | `number` | no | 향수 내 어코드 비율 |

## LayeringRecommendation

```ts
type LayeringRecommendation = {
  candidateType: "PAIR";
  recommended: boolean;
  decision: LayeringDecision;
  score: number;
  title: string;
  summary: string;
  color: LayeringColorResponse;
  bestFor: string[];
  reasons: string[];
  warnings: string[];
  scoreBreakdown: ScoreBreakdownResponse;
};
```

| field | type | nullable | description |
| --- | --- | --- | --- |
| `candidateType` | `"PAIR"` | no | 현재는 향수 2개 조합만 지원 |
| `recommended` | `boolean` | no | 추천 여부 |
| `decision` | `LayeringDecision` | no | 점수 기반 추천 등급 |
| `score` | `number` | no | 최종 점수 |
| `title` | `string` | no | 결과 제목 |
| `summary` | `string` | no | 결과 요약 |
| `color` | `LayeringColorResponse` | no | 조합 무드 컬러 |
| `bestFor` | `string[]` | no | 어울리는 계절/상황 태그, 최대 3개 |
| `reasons` | `string[]` | no | 추천 근거, 최대 3개 |
| `warnings` | `string[]` | no | 주의점, 최대 2개 |
| `scoreBreakdown` | `ScoreBreakdownResponse` | no | 점수 세부 항목 |

## LayeringColorResponse

```ts
type LayeringColorResponse = {
  name: string;
  hex: string;
  sourceAccord: string;
  targetAccord: string;
  description: string;
};
```

| field | type | nullable | description |
| --- | --- | --- | --- |
| `name` | `string` | no | 컬러 이름 |
| `hex` | `string` | no | HEX 컬러 코드 |
| `sourceAccord` | `string` | no | 첫 번째 향수의 대표 어코드 |
| `targetAccord` | `string` | no | 두 번째 향수의 대표 어코드 |
| `description` | `string` | no | 컬러/무드 설명 |

## ScoreBreakdownResponse

```ts
type ScoreBreakdownResponse = {
  matrix: number;
  structure: number;
  balance: number;
  penalty: number;
};
```

| field | type | nullable | description |
| --- | --- | --- | --- |
| `matrix` | `number` | no | 어코드 조합 기본 점수 |
| `structure` | `number` | no | 노트 구조 보정 점수 |
| `balance` | `number` | no | 비율/밸런스 점수 |
| `penalty` | `number` | no | 감점 |

## LayeringDecision

```ts
type LayeringDecision =
  | "RECOMMENDED"
  | "TRY_IF_YOU_LIKE_THIS_MOOD"
  | "NOT_RECOMMENDED";
```

| value | score range | suggested label |
| --- | --- | --- |
| `RECOMMENDED` | `75+` | 추천 |
| `TRY_IF_YOU_LIKE_THIS_MOOD` | `60~74` | 취향에 따라 시도 |
| `NOT_RECOMMENDED` | `0~59` | 비추천 |

## Supported Accord Names

```json
[
  "Floral",
  "Woody",
  "Fresh",
  "Spicy",
  "Sweet",
  "Gourmand",
  "Musky",
  "Green",
  "Citrus",
  "Fruity",
  "Aromatic",
  "Earthy/Smoky"
]
```

`Earthy`도 내부적으로 `Earthy/Smoky`로 정규화됩니다.
