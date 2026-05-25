# Perfume Layering Recommendation Implementation Plan

이 문서는 사용자가 선택한 향수 2개를 입력받아, 백엔드 내부 규칙만으로 레이어링 추천 여부, 설명, 어울리는 컬러를 반환하는 기능의 구현 계획이다. 외부 AI 호출 없이 재현 가능한 추천 결과를 만드는 것을 목표로 한다.

## 1. 목표

### 1.1 사용자 목표

사용자는 보유하거나 관심 있는 향수 2개를 선택한다. 서비스는 두 향수가 레이어링에 적합한지 평가하고 다음 정보를 반환한다.

- 두 향수를 레이어링하는 것을 추천하는지
- 어떤 분위기의 조합인지
- 왜 추천하는지
- 어떤 점을 조심해야 하는지
- 이 레이어링 무드에 어울리는 컬러는 무엇인지

### 1.2 제품 목표

- 추천 결과가 매번 같은 입력에 대해 동일하게 재현되어야 한다.
- 추천 이유가 점수 계산 근거와 연결되어야 한다.
- 모든 어코드 조합별 문장을 직접 관리하지 않는다.
- `Accord` 매트릭스, 향수의 어코드 비율, top/mid/base 노트를 함께 사용한다.
- 첫 구현에서는 개인화 학습이나 AI 문장 생성 없이 운영 가능해야 한다.
- 결과 응답에는 레이어링 방법, 순서, 분사량을 포함하지 않는다.
- 결과 응답에는 어코드 페어 기반 컬러를 포함한다.

## 2. 참고 근거

### 2.1 도메인 기준

리서치와 Perfumer 스킬 기준으로 다음 원칙을 반영한다.

- 향수는 top, heart/middle, base로 시간에 따라 전개된다. The Perfume Society는 top notes가 먼저 느껴지고, heart notes가 중심을 만들며, base notes가 지속력과 dry-down에 직접 연결된다고 설명한다.
  Source: https://perfumesociety.org/frequently-asked-questions/
- base notes는 lighter note의 증발을 늦추고 향을 오래 붙잡는 역할을 한다. 따라서 레이어링 설명에서 `base anchor`, `fresh lift`, `heart bridge` 같은 구조적 근거를 사용한다.
  Source: https://perfumesociety.org/make-perfume-last-longer/
- Jo Malone London은 scent layering을 서로 다른 cologne을 결합해 향의 깊이와 복합성을 높이는 방식으로 설명하며, 계절과 선호 강도에 따라 양을 조절하라고 안내한다.
  Source: https://www.jomalone.com/scent-layering
- Fragrance 또는 perfume 성분은 일부 사용자에게 접촉성 피부 반응을 일으킬 수 있으므로, 민감 피부 사용자는 적은 양으로 테스트해야 한다. FDA와 Cleveland Clinic은 fragrance mixes/perfumes가 patch test 대상이 될 수 있다고 설명한다.
  Sources: https://www.fda.gov/vaccines-blood-biologics/allergenics/allergen-patch-tests, https://my.clevelandclinic.org/health/diagnostics/patch-test

### 2.2 현재 저장소 기준

현재 저장소에는 다음 데이터가 이미 있다.

- `perfumes`: 향수 기본 정보
- `perfume_accords`: 향수별 어코드와 비율
- `perfume_notes`: 향수별 top/mid/base 노트
- `accords`: 12개 어코드 기준 정보

현재 어코드 목록:

```text
Aromatic, Citrus, Earthy/Smoky, Floral, Fresh, Fruity,
Gourmand, Green, Musky, Spicy, Sweet, Woody
```

### 2.3 어코드 설명 데이터

추천 결과 문장은 외부 AI 호출 없이 생성해야 하므로, 어코드별 설명 데이터도 resource로 관리한다. 구현 시 다음 CSV를 추가한다.

```text
src/main/resources/data/layering_accord_narratives.csv
```

권장 컬럼:

```csv
accord_name,display_name_ko,display_name_en,primary_role,pyramid_tendency,volatility,weight,texture,impression,emotion,season_tags,occasion_tags,representative_notes,positive_phrase,risk_phrase,title_adjective,summary_phrase
```

#### 2.3.1 어코드 정체성/구조 테이블

| Accord | 표시명 | Primary role | Pyramid tendency | Volatility | Weight | Texture | 대표 노트 예시 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Floral | 플로럴 | 향의 표정과 감정 중심 | Heart | Medium | Medium | 부드럽고 풍성한 꽃잎감 | rose, jasmine, ylang-ylang, tuberose, orange blossom, peony |
| Woody | 우디 | 잔향과 구조를 잡는 골격 | Base | Slow | Medium-heavy | 건조하고 따뜻한 목재감 | cedarwood, sandalwood, vetiver, guaiac wood, fir, pine |
| Fresh | 프레시 | 깨끗한 공기감과 투명함 | Top-Heart | Fast-medium | Light | 물기 있고 맑은 청량감 | aquatic accord, aldehydes, ozonic notes, mint, watery green notes |
| Spicy | 스파이시 | 온기와 긴장감을 주는 포인트 | Heart-Base | Medium | Medium-heavy | 따뜻하고 자극적인 열감 | pepper, cinnamon, cardamom, clove, nutmeg, ginger |
| Sweet | 스위트 | 부드러운 단맛과 친근함 | Heart-Base | Medium-slow | Medium | 둥글고 포근한 단맛 | vanilla, tonka, honey, sugar accord, heliotrope |
| Gourmand | 구르망 | 디저트 같은 농도와 식감 | Base | Slow | Heavy | 크리미하고 먹음직한 밀도 | caramel, chocolate, coffee, praline, cream, almond |
| Musky | 머스키 | 살결 같은 잔향과 밀착감 | Base | Slow | Medium | 깨끗하고 포근한 피부감 | clean musk, white musk, powder musk, ambrette |
| Green | 그린 | 잎과 줄기의 생기, 풋풋함 | Top-Heart | Fast-medium | Light-medium | 초록 잎, 줄기, 수액의 질감 | galbanum, violet leaf, grass, basil, fig leaf, tomato leaf |
| Citrus | 시트러스 | 밝은 첫인상과 산뜻한 리프트 | Top | Fast | Light | 과피의 반짝임과 쌉쌀함 | bergamot, lemon, orange, grapefruit, mandarin, yuzu |
| Fruity | 프루티 | 과즙감과 명랑한 볼륨 | Top-Heart | Medium | Light-medium | 즙이 많고 생동감 있는 단맛 | peach, pear, apple, berries, cassis, plum |
| Aromatic | 아로마틱 | 허브의 단정함과 균형감 | Top-Heart | Medium | Light-medium | 허브, 라벤더, 차분한 청량감 | lavender, rosemary, sage, thyme, basil, mint |
| Earthy/Smoky | 어시/스모키 | 어두운 깊이와 그림자 | Base | Slow | Heavy | 흙, 이끼, 연기, 그을음 | patchouli, oakmoss, birch tar, incense, petrichor, vetiver |

#### 2.3.2 어코드 설명 문구 테이블

| Accord | Impression | Emotion | Positive phrase | Risk phrase | Title adjective | Summary phrase |
| --- | --- | --- | --- | --- | --- | --- |
| Floral | 활짝 핀 정원처럼 부드럽고 풍성한 중심감 | 로맨틱, 우아함, 설렘 | 조합에 감정과 부드러운 표정을 더합니다. | 과하면 향이 답답하거나 화장품처럼 느껴질 수 있습니다. | 부드러운 | 플로럴의 꽃잎감이 조합을 우아하고 편안하게 만듭니다. |
| Woody | 차분한 목재감과 안정적인 깊이 | 안정감, 성숙함, 차분함 | 향의 구조를 잡고 잔향을 단단하게 만듭니다. | 과하면 건조하거나 무겁게 느껴질 수 있습니다. | 차분한 | 우디의 따뜻한 깊이가 조합을 안정적으로 받쳐줍니다. |
| Fresh | 깨끗한 공기와 물기 어린 투명함 | 청결함, 가벼움, 리셋 | 조합을 맑고 가볍게 정리해 줍니다. | 베이스가 약하면 금방 흐려지거나 밋밋할 수 있습니다. | 맑은 | 프레시한 투명감이 조합을 깨끗하고 부담 없게 만듭니다. |
| Spicy | 향신료의 따뜻한 자극과 생동감 | 활력, 긴장감, 개성 | 단조로운 조합에 온기와 포인트를 더합니다. | 과하면 날카롭거나 자극적으로 느껴질 수 있습니다. | 따뜻한 | 스파이시한 온기가 조합에 선명한 개성을 만듭니다. |
| Sweet | 둥글고 친근한 단맛 | 포근함, 친밀함, 편안함 | 향을 부드럽고 쉽게 다가오게 만듭니다. | 단 향이 겹치면 끈적하거나 답답할 수 있습니다. | 달콤한 | 스위트한 부드러움이 조합을 포근하고 친근하게 만듭니다. |
| Gourmand | 캐러멜과 디저트 같은 농도 | 포만감, 따뜻함, 유혹적임 | 조합에 크리미한 밀도와 기억에 남는 인상을 줍니다. | 과하면 무겁고 음식 같은 인상이 강해질 수 있습니다. | 크리미한 | 구르망의 밀도가 조합을 풍성하고 달콤하게 만듭니다. |
| Musky | 깨끗한 살결과 파우더리한 잔향 | 친밀함, 깨끗함, 안정감 | 두 향을 부드럽게 묶고 착용감을 깨끗하게 만듭니다. | 과하면 파우더리하거나 답답하게 남을 수 있습니다. | 깨끗한 | 머스크의 살결감이 조합을 부드럽고 자연스럽게 이어줍니다. |
| Green | 잎과 줄기의 풋풋하고 생기 있는 느낌 | 생기, 자연스러움, 선명함 | 단맛이나 꽃향을 산뜻하게 정리합니다. | 과하면 풀비린내나 쌉쌀함이 도드라질 수 있습니다. | 싱그러운 | 그린의 풋풋함이 조합에 자연스러운 생기를 줍니다. |
| Citrus | 과피가 터지는 듯한 밝고 쌉쌀한 첫인상 | 활기, 명쾌함, 기분 전환 | 무거운 향을 밝게 열고 첫인상을 선명하게 만듭니다. | 베이스가 부족하면 빠르게 사라져 가볍게 느껴질 수 있습니다. | 밝은 | 시트러스의 산뜻함이 조합의 첫인상을 환하게 엽니다. |
| Fruity | 과즙감 있는 명랑함과 달콤한 볼륨 | 발랄함, 즐거움, 친근함 | 플로럴이나 머스크에 생동감 있는 단맛을 더합니다. | 과하면 유치하거나 시럽 같은 인상이 날 수 있습니다. | 생기 있는 | 프루티한 과즙감이 조합을 밝고 활기 있게 만듭니다. |
| Aromatic | 허브의 단정하고 차분한 청량감 | 집중, 균형, 안정 | 조합의 단맛과 무게를 정돈하고 균형을 잡습니다. | 과하면 약초 같거나 차갑게 느껴질 수 있습니다. | 단정한 | 아로마틱한 허브감이 조합을 차분하고 균형 있게 만듭니다. |
| Earthy/Smoky | 흙, 이끼, 연기 같은 어두운 깊이 | 사색적, 고요함, 미스터리 | 밝은 향에 그림자와 입체감을 더합니다. | 과하면 축축하거나 탄 냄새처럼 무겁게 느껴질 수 있습니다. | 깊은 | 어시/스모키한 그림자가 조합에 깊이와 분위기를 만듭니다. |

#### 2.3.3 어코드 상황/궁합 테이블

| Accord | Season tags | Occasion tags | 잘 맞는 방향 | 주의할 방향 |
| --- | --- | --- | --- | --- |
| Floral | spring, autumn, all-season | date, daily, special | Citrus, Fruity, Musky, Woody, Sweet | Earthy/Smoky가 강하면 꽃향이 어둡게 눌릴 수 있음 |
| Woody | autumn, winter, all-season | daily, office, night, special | Citrus, Aromatic, Green, Earthy/Smoky, Gourmand | Sweet/Gourmand와 겹치면 무거워질 수 있음 |
| Fresh | spring, summer | office, daily, casual | Citrus, Green, Aromatic, Musky, Floral | Gourmand, heavy Sweet와 만나면 방향성이 흐려질 수 있음 |
| Spicy | autumn, winter | night, special, date | Woody, Gourmand, Citrus, Aromatic, Earthy/Smoky | Fresh가 강하면 차갑고 따뜻한 축이 충돌할 수 있음 |
| Sweet | autumn, winter, spring | date, casual, night | Floral, Musky, Fruity, Citrus, Woody | Sweet/Gourmand 중복은 답답함 위험 |
| Gourmand | autumn, winter | night, date, special | Spicy, Woody, Musky, Floral | Fresh/Citrus가 너무 가벼우면 디저트감과 분리될 수 있음 |
| Musky | all-season | office, daily, date | Floral, Sweet, Fresh, Citrus, Woody | Aromatic이 강하면 비누/허브감이 건조해질 수 있음 |
| Green | spring, summer, autumn | daily, office, casual | Fresh, Citrus, Aromatic, Woody, Floral | Sweet/Gourmand와 만나면 풋내와 단맛이 어색할 수 있음 |
| Citrus | spring, summer, all-season | office, daily, casual | Fresh, Floral, Green, Aromatic, Woody | Earthy/Smoky가 강하면 밝음이 꺼질 수 있음 |
| Fruity | spring, summer, autumn | casual, date, daily | Floral, Citrus, Musky, Sweet, Fresh | Earthy/Smoky와 강하게 만나면 과즙감이 탁해질 수 있음 |
| Aromatic | spring, summer, autumn | office, daily, casual | Citrus, Green, Woody, Fresh, Spicy | Sweet/Gourmand가 강하면 허브감이 약초처럼 느껴질 수 있음 |
| Earthy/Smoky | autumn, winter | night, special, contemplative | Woody, Spicy, Aromatic, Floral, Musky | Citrus/Fruity가 약하면 밝은 인상이 묻힐 수 있음 |

#### 2.3.4 구현용 CSV 예시

아래는 실제 CSV에 넣을 수 있는 값의 예시다. 구현 시 12개 행을 그대로 사용한다.

```csv
accord_name,display_name_ko,display_name_en,primary_role,pyramid_tendency,volatility,weight,texture,impression,emotion,season_tags,occasion_tags,representative_notes,positive_phrase,risk_phrase,title_adjective,summary_phrase
Floral,플로럴,Floral,향의 표정과 감정 중심,Heart,Medium,Medium,부드럽고 풍성한 꽃잎감,활짝 핀 정원처럼 부드럽고 풍성한 중심감,"로맨틱;우아함;설렘","spring;autumn;all-season","date;daily;special","rose;jasmine;ylang-ylang;tuberose;orange blossom;peony",조합에 감정과 부드러운 표정을 더합니다.,과하면 향이 답답하거나 화장품처럼 느껴질 수 있습니다.,부드러운,플로럴의 꽃잎감이 조합을 우아하고 편안하게 만듭니다.
Woody,우디,Woody,잔향과 구조를 잡는 골격,Base,Slow,Medium-heavy,건조하고 따뜻한 목재감,차분한 목재감과 안정적인 깊이,"안정감;성숙함;차분함","autumn;winter;all-season","daily;office;night;special","cedarwood;sandalwood;vetiver;guaiac wood;fir;pine",향의 구조를 잡고 잔향을 단단하게 만듭니다.,과하면 건조하거나 무겁게 느껴질 수 있습니다.,차분한,우디의 따뜻한 깊이가 조합을 안정적으로 받쳐줍니다.
Fresh,프레시,Fresh,깨끗한 공기감과 투명함,Top-Heart,Fast-medium,Light,물기 있고 맑은 청량감,깨끗한 공기와 물기 어린 투명함,"청결함;가벼움;리셋","spring;summer","office;daily;casual","aquatic accord;aldehydes;ozonic notes;mint;watery green notes",조합을 맑고 가볍게 정리해 줍니다.,베이스가 약하면 금방 흐려지거나 밋밋할 수 있습니다.,맑은,프레시한 투명감이 조합을 깨끗하고 부담 없게 만듭니다.
Spicy,스파이시,Spicy,온기와 긴장감을 주는 포인트,Heart-Base,Medium,Medium-heavy,따뜻하고 자극적인 열감,향신료의 따뜻한 자극과 생동감,"활력;긴장감;개성","autumn;winter","night;special;date","pepper;cinnamon;cardamom;clove;nutmeg;ginger",단조로운 조합에 온기와 포인트를 더합니다.,과하면 날카롭거나 자극적으로 느껴질 수 있습니다.,따뜻한,스파이시한 온기가 조합에 선명한 개성을 만듭니다.
Sweet,스위트,Sweet,부드러운 단맛과 친근함,Heart-Base,Medium-slow,Medium,둥글고 포근한 단맛,둥글고 친근한 단맛,"포근함;친밀함;편안함","autumn;winter;spring","date;casual;night","vanilla;tonka;honey;sugar accord;heliotrope",향을 부드럽고 쉽게 다가오게 만듭니다.,단 향이 겹치면 끈적하거나 답답할 수 있습니다.,달콤한,스위트한 부드러움이 조합을 포근하고 친근하게 만듭니다.
Gourmand,구르망,Gourmand,디저트 같은 농도와 식감,Base,Slow,Heavy,크리미하고 먹음직한 밀도,캐러멜과 디저트 같은 농도,"포만감;따뜻함;유혹적임","autumn;winter","night;date;special","caramel;chocolate;coffee;praline;cream;almond",조합에 크리미한 밀도와 기억에 남는 인상을 줍니다.,과하면 무겁고 음식 같은 인상이 강해질 수 있습니다.,크리미한,구르망의 밀도가 조합을 풍성하고 달콤하게 만듭니다.
Musky,머스키,Musky,살결 같은 잔향과 밀착감,Base,Slow,Medium,깨끗하고 포근한 피부감,깨끗한 살결과 파우더리한 잔향,"친밀함;깨끗함;안정감",all-season,"office;daily;date","clean musk;white musk;powder musk;ambrette",두 향을 부드럽게 묶고 착용감을 깨끗하게 만듭니다.,과하면 파우더리하거나 답답하게 남을 수 있습니다.,깨끗한,머스크의 살결감이 조합을 부드럽고 자연스럽게 이어줍니다.
Green,그린,Green,잎과 줄기의 생기와 풋풋함,Top-Heart,Fast-medium,Light-medium,초록 잎과 줄기와 수액의 질감,잎과 줄기의 풋풋하고 생기 있는 느낌,"생기;자연스러움;선명함","spring;summer;autumn","daily;office;casual","galbanum;violet leaf;grass;basil;fig leaf;tomato leaf",단맛이나 꽃향을 산뜻하게 정리합니다.,과하면 풀비린내나 쌉쌀함이 도드라질 수 있습니다.,싱그러운,그린의 풋풋함이 조합에 자연스러운 생기를 줍니다.
Citrus,시트러스,Citrus,밝은 첫인상과 산뜻한 리프트,Top,Fast,Light,과피의 반짝임과 쌉쌀함,과피가 터지는 듯한 밝고 쌉쌀한 첫인상,"활기;명쾌함;기분 전환","spring;summer;all-season","office;daily;casual","bergamot;lemon;orange;grapefruit;mandarin;yuzu",무거운 향을 밝게 열고 첫인상을 선명하게 만듭니다.,베이스가 부족하면 빠르게 사라져 가볍게 느껴질 수 있습니다.,밝은,시트러스의 산뜻함이 조합의 첫인상을 환하게 엽니다.
Fruity,프루티,Fruity,과즙감과 명랑한 볼륨,Top-Heart,Medium,Light-medium,즙이 많고 생동감 있는 단맛,과즙감 있는 명랑함과 달콤한 볼륨,"발랄함;즐거움;친근함","spring;summer;autumn","casual;date;daily","peach;pear;apple;berries;cassis;plum",플로럴이나 머스크에 생동감 있는 단맛을 더합니다.,과하면 유치하거나 시럽 같은 인상이 날 수 있습니다.,생기 있는,프루티한 과즙감이 조합을 밝고 활기 있게 만듭니다.
Aromatic,아로마틱,Aromatic,허브의 단정함과 균형감,Top-Heart,Medium,Light-medium,허브와 라벤더의 차분한 청량감,허브의 단정하고 차분한 청량감,"집중;균형;안정","spring;summer;autumn","office;daily;casual","lavender;rosemary;sage;thyme;basil;mint",조합의 단맛과 무게를 정돈하고 균형을 잡습니다.,과하면 약초 같거나 차갑게 느껴질 수 있습니다.,단정한,아로마틱한 허브감이 조합을 차분하고 균형 있게 만듭니다.
Earthy/Smoky,어시/스모키,Earthy/Smoky,어두운 깊이와 그림자,Base,Slow,Heavy,흙과 이끼와 연기의 질감,흙과 이끼와 연기 같은 어두운 깊이,"사색적;고요함;미스터리","autumn;winter","night;special;contemplative","patchouli;oakmoss;birch tar;incense;petrichor;vetiver",밝은 향에 그림자와 입체감을 더합니다.,과하면 축축하거나 탄 냄새처럼 무겁게 느껴질 수 있습니다.,깊은,어시/스모키한 그림자가 조합에 깊이와 분위기를 만듭니다.
```

## 3. 매트릭스 적용 방향

### 3.1 제공된 엑셀 파일 확인 결과

파일:

```text
C:/Users/kb100/Documents/카카오톡 받은 파일/레이어링 매트릭스 값.xlsx
```

확인된 구조:

- `Sheet1`
- `B3:M15`: 향수 휠 기반 초기 매트릭스
- `B19:M31`: 블로그 및 커뮤니티 의견을 반영한 변화값 매트릭스

구현에는 두 번째 매트릭스(`B19:M31`)를 기본값으로 사용한다. 이 매트릭스가 사용자의 최신 의도에 가까우며, 실제 레이어링 감각을 반영하려는 값이기 때문이다.

주의할 매핑:

```text
엑셀: Earthy
현재 DB/CSV: Earthy/Smoky
```

구현에서는 `Earthy`를 `Earthy/Smoky`로 정규화한다.

### 3.2 매트릭스 저장 방식

엑셀을 런타임에 직접 읽지 않는다. 구현 시 다음 CSV를 추가한다.

```text
src/main/resources/data/layering_accord_compatibility.csv
```

권장 포맷:

```csv
source_accord,target_accord,score
Floral,Floral,0.88
Floral,Woody,0.85
Floral,Fresh,0.80
...
Earthy/Smoky,Earthy/Smoky,0.72
```

144개 행을 모두 저장한다. 대칭 매트릭스처럼 보이더라도 전부 저장하는 편이 좋다. 향후 “A를 먼저 뿌릴 때”와 “B를 먼저 뿌릴 때”를 다르게 보정할 수 있기 때문이다.

### 3.3 매트릭스 값의 의미

매트릭스 점수는 최종 추천 점수가 아니라 `compatibility prior`로 사용한다.

```text
0.85 이상: 강한 궁합
0.75 이상: 좋은 궁합
0.60 이상: 무난한 궁합
0.45 이상: 중립 또는 취향 의존
0.45 미만: 충돌 가능성
```

점수 공식에서는 다음 offset으로 변환한다.

```text
matrixOffset = (weightedCompatibility - 0.50) * 40
```

예:

```text
weightedCompatibility = 0.90 -> +16점
weightedCompatibility = 0.75 -> +10점
weightedCompatibility = 0.50 -> 0점
weightedCompatibility = 0.30 -> -8점
```

## 4. 추천 범위

### 4.1 입력

첫 구현은 향수 ID 2개를 받는다.

```json
{
  "perfumeIds": [4367, 10806]
}
```

향후 확장을 위해 선택 입력을 둘 수 있다.

```json
{
  "perfumeIds": [4367, 10806],
  "season": "SUMMER",
  "occasion": "OFFICE",
  "intensityPreference": "MODERATE"
}
```

MVP에서는 선택 입력을 받지 않아도 된다. 다만 DTO와 서비스 내부 모델은 확장 가능하게 둔다.

### 4.2 출력 범위

향수 2개 `A`, `B`가 들어오면 후보는 하나다.

```text
A + B
```

따라서 이 기능은 여러 후보를 랭킹하거나 착용 방법을 안내하는 추천이 아니다. 두 향수의 궁합을 평가하고, 레이어링을 추천하는지 여부와 이유, warning, 무드 컬러를 반환하는 평가형 추천이다. 궁합 점수가 낮아도 조합을 숨기지 않고, 낮은 점수와 이유를 함께 반환한다.

## 5. API 계약

### 5.1 Endpoint

```http
POST /api/layering/recommendations
Content-Type: application/json
```

인증:

- MVP는 공개 API로 시작한다.
- 개인화 피드백 저장을 추가할 때만 JWT 인증 API를 별도로 추가한다.

### 5.2 Request

| 필드 | 타입 | 필수 | 검증 | 설명 |
| --- | --- | --- | --- | --- |
| `perfumeIds` | number[] | yes | 정확히 2개, 각 값 1 이상, 중복 불가 | 추천에 사용할 향수 ID |
| `season` | string | no | `SPRING`, `SUMMER`, `AUTUMN`, `WINTER` | 착용 계절 |
| `occasion` | string | no | `OFFICE`, `DAILY`, `DATE`, `NIGHT`, `SPECIAL` | 사용 상황 |
| `intensityPreference` | string | no | `SOFT`, `MODERATE`, `STRONG` | 선호 확산/강도 |

MVP 최소 DTO:

```java
public class LayeringRecommendationRequest {
  @Size(min = 2, max = 2, message = "향수는 정확히 2개를 선택해야 합니다.")
  private List<@Min(1) Long> perfumeIds;
}
```

중복 검증은 Bean Validation만으로는 메시지 제어가 애매하므로 서비스 또는 request validator에서 처리한다.

### 5.3 Response

```json
{
  "inputPerfumes": [
    {
      "id": 4367,
      "brand": "Lush",
      "name": "Karma",
      "dominantAccords": [
        { "name": "Citrus", "ratio": 100 },
        { "name": "Woody", "ratio": 95 }
      ]
    },
    {
      "id": 10806,
      "brand": "Lush",
      "name": "Lust",
      "dominantAccords": [
        { "name": "Floral", "ratio": 100 },
        { "name": "Woody", "ratio": 57 }
      ]
    }
  ],
  "recommendation": {
    "candidateType": "PAIR",
    "recommended": true,
    "decision": "RECOMMENDED",
    "score": 86,
    "title": "밝은 시트러스와 안정적인 우디 베이스",
    "summary": "산뜻한 첫인상과 따뜻한 잔향이 균형을 이루는 조합입니다.",
    "color": {
      "name": "Luminous Garden",
      "hex": "#F4B7A8",
      "sourceAccord": "Citrus",
      "targetAccord": "Floral",
      "description": "밝은 시트러스와 부드러운 플로럴의 연결감을 표현하는 코랄 핑크입니다."
    },
    "bestFor": ["가을", "저녁", "캐주얼"],
    "reasons": [
      "Citrus와 Woody의 궁합 점수가 높아 첫 향과 잔향의 연결이 자연스럽습니다.",
      "Woody와 Sweet가 베이스를 잡아주고 Citrus가 무게감을 밝게 열어줍니다."
    ],
    "warnings": [
      "단 향이 강하게 느껴질 수 있어 산뜻한 분위기를 원할 때는 호불호가 갈릴 수 있습니다."
    ],
    "scoreBreakdown": {
      "matrix": 84,
      "structure": 88,
      "balance": 82,
      "penalty": 4
    }
  }
}
```

#### Response field contract

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `inputPerfumes` | array | 요청으로 들어온 2개 향수의 기본 정보와 주요 어코드 |
| `inputPerfumes[].id` | number | 향수 ID |
| `inputPerfumes[].brand` | string | 브랜드명 |
| `inputPerfumes[].name` | string | 향수명 |
| `inputPerfumes[].dominantAccords` | array | 추천 판단에 주로 기여한 상위 어코드 목록 |
| `inputPerfumes[].dominantAccords[].name` | string | 어코드 이름. `accord.csv`의 정규화 이름 |
| `inputPerfumes[].dominantAccords[].ratio` | number | 원본 `perfume_accords.ratio` 값 |
| `recommendation` | object | 두 향수 레이어링 평가 결과 |
| `recommendation.candidateType` | string | MVP에서는 항상 `PAIR` |
| `recommendation.recommended` | boolean | 최종 점수 75점 이상이면 `true` |
| `recommendation.decision` | string | `RECOMMENDED`, `TRY_IF_YOU_LIKE_THIS_MOOD`, `NOT_RECOMMENDED` 중 하나 |
| `recommendation.score` | number | 0~100 정수 점수 |
| `recommendation.title` | string | dominant accord와 evidence 기반 제목 |
| `recommendation.summary` | string | 사용자에게 보여줄 한 문장 요약 |
| `recommendation.color` | object | dominant accord pair에 매핑된 컬러 |
| `recommendation.color.name` | string | 컬러 이름. `layering_accord_colors.csv.color_name` |
| `recommendation.color.hex` | string | `#RRGGBB` 형식의 대표 컬러 |
| `recommendation.color.sourceAccord` | string | 컬러 산정에 사용한 source accord |
| `recommendation.color.targetAccord` | string | 컬러 산정에 사용한 target accord |
| `recommendation.color.description` | string | 컬러가 표현하는 향 무드 설명 |
| `recommendation.bestFor` | string[] | season/occasion 추론 태그. MVP에서는 narrative와 evidence 기반으로 생성 |
| `recommendation.reasons` | string[] | 추천 또는 평가 근거. 최대 3개 |
| `recommendation.warnings` | string[] | 주의점. 최대 2개 |
| `recommendation.scoreBreakdown.matrix` | number | matrix compatibility 기반 점수 |
| `recommendation.scoreBreakdown.structure` | number | top/heart/base 구조 기반 점수 |
| `recommendation.scoreBreakdown.balance` | number | 조합 균형 점수 |
| `recommendation.scoreBreakdown.penalty` | number | overload, volatility, muddiness penalty 합계 |

#### Decision enum

| 값 | 조건 | `recommended` | 사용자 표시 의도 |
| --- | --- | --- | --- |
| `RECOMMENDED` | `score >= 75` | `true` | 두 향수의 레이어링을 추천 |
| `TRY_IF_YOU_LIKE_THIS_MOOD` | `60 <= score < 75` | `false` | 취향이 맞으면 시도 가능하지만 기본 추천은 아님 |
| `NOT_RECOMMENDED` | `score < 60` | `false` | 충돌 가능성이 커 추천하지 않음 |

### 5.4 Error

| HTTP status | 조건 | 메시지 |
| --- | --- | --- |
| `400 Bad Request` | 향수 ID가 2개가 아님 | `향수는 정확히 2개를 선택해야 합니다.` |
| `400 Bad Request` | 향수 ID 중복 | `서로 다른 향수 2개를 선택해야 합니다.` |
| `400 Bad Request` | enum 값 오류 | 기존 validation 오류 형식 사용 |
| `404 Not Found` | 존재하지 않는 향수 ID 포함 | `존재하지 않는 향수 ID가 포함되어 있습니다.` |

## 6. 패키지 구조

기능 중심 패키지 원칙에 따라 새 패키지는 다음처럼 둔다.

```text
src/main/java/kim/biryeong/perfume/layering
  LayeringRecommendationController.java
  LayeringRecommendationService.java
  LayeringPerfumeQueryRepository.java
  LayeringCompatibilityMatrix.java
  LayeringCompatibilityProperties.java
  LayeringScoreCalculator.java
  LayeringCandidateFactory.java
  LayeringColorResolver.java
  LayeringExplanationAssembler.java
  LayeringEnums.java

src/main/java/kim/biryeong/perfume/layering/dto
  LayeringRecommendationRequest.java
  LayeringRecommendationResponse.java
  LayeringInputPerfumeDto.java
  LayeringRecommendationDto.java
  LayeringAccordDto.java
  LayeringColorDto.java
  LayeringScoreBreakdownDto.java

src/main/java/kim/biryeong/perfume/layering/model
  LayeringPerfumeProfile.java
  AccordWeight.java
  NoteProfile.java
  LayeringCandidate.java
  LayeringScore.java
  LayeringColor.java
  LayeringEvidence.java
```

Repository를 기존 `perfume` 패키지에 억지로 추가하지 않는다. 레이어링 기능이 필요한 조회 모델은 `layering` 패키지 안의 query repository에서 명시적으로 관리한다.

## 7. 조회 모델

### 7.1 LayeringPerfumeProfile

추천 엔진 내부 입력 모델이다.

```java
public record LayeringPerfumeProfile(
    Long id,
    String brand,
    String name,
    List<AccordWeight> accords,
    NoteProfile notes,
    RoleVector roleVector,
    double estimatedIntensity) {}
```

### 7.2 AccordWeight

```java
public record AccordWeight(String name, int ratio, double normalizedWeight) {}
```

정규화:

```text
normalizedWeight = ratio / sum(all selected accord ratios)
```

향수 하나가 다음 어코드를 가진다고 하자.

```text
Citrus 100
Woody 95
Aromatic 86
Spicy 65
```

정규화 후에는 각 어코드가 향수 내 영향도를 갖는다. 이렇게 해야 단순 dominant accord 하나만 보는 추천보다 정확해진다.

### 7.3 RoleVector

레이어링 결과를 노출하기 위한 값이 아니라, 궁합 점수와 warning을 계산하기 위한 구조 벡터다.

```java
public record RoleVector(
    double topLift,
    double heartBridge,
    double baseAnchor,
    double sweetness,
    double freshness,
    double darkness) {}
```

권장 계산:

```text
topLift =
  Citrus + Fresh + Green + Fruity + 0.5 * Aromatic

heartBridge =
  Floral + Aromatic + Spicy + 0.5 * Sweet

baseAnchor =
  Woody + Musky + Earthy/Smoky + Gourmand + 0.5 * Sweet

sweetness =
  Sweet + Gourmand + 0.5 * Fruity

freshness =
  Citrus + Fresh + Green + Aromatic

darkness =
  Earthy/Smoky + Woody + Spicy
```

모든 값은 해당 향수의 `normalizedWeight` 합산으로 계산한다.

## 8. 점수 계산

### 8.1 후보 생성

```java
LayeringCandidate create(List<LayeringPerfumeProfile> profiles) {
  return pair(profiles.get(0), profiles.get(1));
}
```

### 8.2 향수 간 궁합 점수

두 향수 `P1`, `P2`의 궁합은 모든 어코드 쌍의 가중 평균이다.

```text
pairCompatibility(P1, P2) =
  sum(P1.accord[i].normalizedWeight
    * P2.accord[j].normalizedWeight
    * matrix[P1.accord[i]][P2.accord[j]])
```

예:

```text
P1 = Citrus 0.55, Woody 0.45
P2 = Floral 0.70, Musky 0.30

score =
  0.55 * 0.70 * matrix[Citrus][Floral]
+ 0.55 * 0.30 * matrix[Citrus][Musky]
+ 0.45 * 0.70 * matrix[Woody][Floral]
+ 0.45 * 0.30 * matrix[Woody][Musky]
```

입력이 항상 2개이므로 `candidateCompatibility`는 `pairCompatibility(P1, P2)`와 같다.

### 8.3 기본 점수

```text
score = 50
      + matrixOffset
      + structureOffset
      + balanceOffset
      + contextOffset
      - overloadPenalty
      - volatilityPenalty
      - muddinessPenalty
```

MVP에서 `contextOffset`은 0으로 둔다. 계절/상황 입력을 받을 때 활성화한다.

### 8.4 Matrix Offset

```text
matrixOffset = (candidateCompatibility - 0.50) * 40
```

### 8.5 Structure Offset

레이어링이 시간 전개상 자연스러운지 평가한다.

```text
baseCandidate = baseAnchor가 가장 높은 향수
topCandidate = topLift가 가장 높은 향수

structureOffset =
  +6 if 후보 안에 baseAnchor 0.35 이상인 향수가 있음
  +5 if 후보 안에 topLift 0.35 이상인 향수가 있음
  +4 if baseAnchor 향수와 topLift 향수가 서로 다름
  -6 if 모든 향수가 topLift 중심이고 baseAnchor가 부족함
  -5 if 모든 향수가 baseAnchor 중심이고 topLift가 부족함
```

### 8.6 Balance Offset

너무 비슷하지도, 너무 충돌하지도 않는 조합을 선호한다.

```text
accordDiversity = uniqueAccordCount(candidate top accords) / totalAccordCount
dominantOverlap = dominant accord가 같은 향수 쌍의 수

balanceOffset =
  +5 if accordDiversity is moderate
  +3 if shared accord가 1개 이상 있고 dominant가 완전히 같지는 않음
  -4 if dominantOverlap이 높고 새로움이 낮음
  -6 if weightedCompatibility가 낮은 쌍이 포함됨
```

### 8.7 Penalties

#### Sweet/Gourmand overload

```text
sweetLoad = average(candidate.roleVector.sweetness)

penalty =
  +6 if sweetLoad >= 0.62
  +5 if Sweet + Gourmand dominant perfume가 2개 이상
```

#### Dark/Smoky overload

```text
darkLoad = average(candidate.roleVector.darkness)

penalty =
  +6 if darkLoad >= 0.62
  +4 if Earthy/Smoky + Spicy pair가 모두 강함
```

#### Volatility risk

```text
freshLoad = average(candidate.roleVector.freshness)
baseLoad = average(candidate.roleVector.baseAnchor)

penalty =
  +5 if freshLoad >= 0.60 and baseLoad < 0.25
```

이 경우 결과를 막지는 않고 warning을 붙인다.

### 8.8 Score Clamp

```text
finalScore = round(clamp(score, 0, 100))
```

입력이 2개라 후보 정렬은 필요 없다. 같은 입력 순서가 바뀌어도 같은 점수가 나오도록 scoring은 대칭으로 계산한다. 응답에는 착용 순서나 분사량을 노출하지 않는다.

## 9. 추천 여부와 컬러

### 9.1 추천 여부 결정

최종 점수에 따라 레이어링 추천 여부를 결정한다.

```text
score >= 75: RECOMMENDED
60 <= score < 75: TRY_IF_YOU_LIKE_THIS_MOOD
score < 60: NOT_RECOMMENDED
```

응답의 `recommended`는 `score >= 75`일 때만 `true`다. 60점대는 취향에 따라 시도 가능하지만 기본 추천은 아닌 것으로 둔다.

### 9.2 컬러 결정

컬러는 두 향수에서 가장 크게 기여한 어코드 페어를 기준으로 결정한다.

기여도 계산:

```text
pairContribution =
  perfumeA.accord[i].normalizedWeight
* perfumeB.accord[j].normalizedWeight
* matrix[accordI][accordJ]
```

가장 높은 `pairContribution`을 가진 어코드 페어를 `dominantPair`로 잡고, 다음 CSV에서 컬러를 조회한다.

```text
src/main/resources/data/layering_accord_colors.csv
```

권장 포맷:

```csv
source_accord,target_accord,color_name,hex,description
Citrus,Floral,Luminous Garden,#F4B7A8,밝은 시트러스와 부드러운 플로럴의 연결감을 표현하는 코랄 핑크
Woody,Musky,Soft Cedar Skin,#B88A68,우디의 따뜻함과 머스크의 부드러운 잔향을 표현하는 세더 베이지
```

컬러 매핑은 compatibility matrix처럼 144개 조합을 직접 관리한다. 이유는 다음과 같다.

- 색상은 점수보다 감성 표현에 가깝기 때문에 평균값으로 블렌딩하면 결과가 탁해질 수 있다.
- 프론트엔드에서 카드, 배경, 태그 컬러로 바로 쓰려면 사람이 검수한 안정적인 hex 값이 필요하다.
- 12x12는 144개라 수작업 관리가 가능한 크기다.

`source_accord`, `target_accord`는 matrix와 같은 정규화 규칙을 사용한다. `Earthy`는 `Earthy/Smoky`로 정규화한다.

## 10. 설명 생성 방식

### 10.1 원칙

모든 어코드 조합별 문장을 만들지 않는다. 대신 다음 작은 단위를 조합한다.

```text
AccordNarrative
RuleExplanation
CandidateEvidence
Template
```

### 10.2 AccordNarrative

코드 또는 resource JSON으로 관리한다.

```java
public record AccordNarrative(
    String accordName,
    String displayName,
    String displayNameEn,
    String primaryRole,
    String pyramidTendency,
    String volatility,
    String weight,
    String texture,
    String impression,
    String emotion,
    List<String> seasonTags,
    List<String> occasionTags,
    List<String> representativeNotes,
    String rolePhrase,
    String positivePhrase,
    String riskPhrase,
    String titleAdjective,
    String summaryPhrase) {}
```

초기 데이터는 `2.3 어코드 설명 데이터`의 테이블과 `layering_accord_narratives.csv` 예시를 그대로 사용한다.

### 10.3 RuleExplanation

점수 계산 중 활성화된 evidence code를 문장으로 변환한다.

```text
HIGH_MATRIX_COMPATIBILITY
  "{accordA}와 {accordB}의 궁합 점수가 높아 두 향의 연결이 자연스럽습니다."

BASE_ANCHOR_PRESENT
  "{perfumeName}의 {accord} 계열이 조합에 안정적인 깊이감을 더합니다."

TOP_LIFT_PRESENT
  "{perfumeName}의 {accord} 계열이 조합의 인상을 밝고 산뜻하게 만듭니다."

SWEET_OVERLOAD
  "단 향이 겹쳐 답답하게 느껴질 수 있어 산뜻한 조합을 원한다면 맞지 않을 수 있습니다."

FRESH_WITHOUT_BASE
  "상쾌한 향이 중심인 조합이라 지속감은 가볍게 느껴질 수 있습니다."

COLOR_PAIR_SELECTED
  "{accordA}와 {accordB}의 무드를 {colorName} 컬러로 표현했습니다."
```

### 10.4 제목 생성

제목은 dominant evidence 2개를 이용한다.

```text
"{topAccordAdjective} {topAccordName}와 {baseAccordAdjective} {baseAccordName}"
```

예:

```text
"밝은 시트러스와 차분한 우디 베이스"
"부드러운 플로럴과 깨끗한 머스크 잔향"
"스파이시한 온기와 어시한 깊이감"
```

### 10.5 Summary 생성

템플릿:

```text
"{openingPhrase}에 {basePhrase}이 더해져 {moodPhrase} 조합입니다."
```

예:

```text
"시트러스의 산뜻한 첫인상에 우디의 차분한 잔향이 더해져 낮부터 저녁까지 쓰기 좋은 조합입니다."
```

### 10.6 설명 품질 규칙

- `reasons`는 최대 3개.
- `warnings`는 최대 2개.
- 점수에 실제 반영된 evidence만 설명한다.
- 사용자가 이해하기 어려운 내부 수식은 응답에 직접 노출하지 않는다.
- `scoreBreakdown`은 디버깅과 프론트 상세 표시용으로 제공하되, 화면 기본 문구는 자연어 설명을 우선한다.

## 11. 구현 클래스 상세

### 11.1 LayeringRecommendationController

책임:

- HTTP request validation
- service 호출
- DTO response 반환

```java
@RestController
@RequestMapping("/api/layering/recommendations")
@RequiredArgsConstructor
@Validated
public class LayeringRecommendationController {

  private final LayeringRecommendationService layeringRecommendationService;

  @PostMapping
  public LayeringRecommendationResponse recommend(
      @Valid @RequestBody LayeringRecommendationRequest request) {
    return layeringRecommendationService.recommend(request);
  }
}
```

### 11.2 LayeringRecommendationService

책임:

- 입력 ID 중복 검증
- 향수 프로필 조회
- 후보 생성
- 점수 계산
- 추천 여부 결정
- 컬러 결정
- 설명 조립

흐름:

```java
public LayeringRecommendationResponse recommend(LayeringRecommendationRequest request) {
  validateDistinctTwoIds(request.getPerfumeIds());
  List<LayeringPerfumeProfile> profiles = queryRepository.findProfiles(request.getPerfumeIds());
  validateAllFound(request.getPerfumeIds(), profiles);

  LayeringCandidate candidate = candidateFactory.create(profiles);
  LayeringScore score = scoreCalculator.score(candidate);
  LayeringColor color = colorResolver.resolve(candidate, score);
  LayeringRecommendationDto recommendation = explanationAssembler.assemble(candidate, score, color);

  return LayeringRecommendationResponse.of(profiles, recommendation);
}
```

### 11.3 LayeringCandidateFactory

책임:

- 검증이 끝난 향수 프로필 2개를 단일 `LayeringCandidate`로 묶는다.
- 입력 순서가 점수 계산에 영향을 주지 않도록 내부 후보 키를 정규화한다.
- 원래 입력 순서는 response의 `inputPerfumes` 표시에만 사용한다.

```java
public LayeringCandidate create(List<LayeringPerfumeProfile> profiles) {
  if (profiles.size() != 2) {
    throw new IllegalArgumentException("향수는 정확히 2개를 선택해야 합니다.");
  }
  LayeringPerfumeProfile first = profiles.get(0);
  LayeringPerfumeProfile second = profiles.get(1);
  return LayeringCandidate.pair(first, second);
}
```

### 11.4 LayeringPerfumeQueryRepository

책임:

- 2개 향수의 기본 정보, 어코드, 노트를 조회한다.
- N+1을 만들지 않는다.

권장 구현:

- `PerfumeRepository.findAllById(ids)`로 기본 정보 조회
- `PerfumeAccordRepository.findByPerfumeIdIn(ids)` 추가
- `PerfumeNoteRepository.findByPerfumeIdIn(ids)` 추가

기존 repository에 `findByPerfumeIdIn`을 추가하는 것은 기능에 필요한 최소 변경이다.

대안:

- `LayeringPerfumeQueryRepository`에서 `EntityManager`로 한번에 조회 후 조립

MVP에서는 기존 repository 메서드 확장이 단순하다.

추가할 메서드:

```java
List<PerfumeAccord> findByPerfumeIdIn(Collection<Long> perfumeIds);
List<PerfumeNote> findByPerfumeIdIn(Collection<Long> perfumeIds);
```

### 11.5 LayeringCompatibilityMatrix

책임:

- CSV 로딩
- 어코드 이름 정규화
- score lookup
- 매트릭스 완전성 검증

```java
public class LayeringCompatibilityMatrix {

  private final Map<String, Map<String, Double>> scores;

  public double score(String sourceAccord, String targetAccord) {
    String source = normalize(sourceAccord);
    String target = normalize(targetAccord);
    return Optional.ofNullable(scores.get(source))
        .map(row -> row.get(target))
        .orElseThrow(() -> new IllegalArgumentException("Unknown layering accord pair"));
  }
}
```

애플리케이션 시작 시 검증:

- 현재 `accord.csv`의 12개 어코드가 모두 포함되어야 한다.
- 각 source accord마다 target accord 12개가 있어야 한다.
- score는 `0.0 <= score <= 1.0`이어야 한다.

### 11.6 LayeringScoreCalculator

책임:

- matrix compatibility 계산
- structure/balance/penalty 계산
- evidence code 수집

결과:

```java
public record LayeringScore(
    int finalScore,
    int matrixScore,
    int structureScore,
    int balanceScore,
    int penaltyScore,
    List<LayeringEvidence> evidences) {}
```

### 11.7 LayeringColorResolver

책임:

- 점수 계산에서 가장 크게 기여한 어코드 페어를 찾는다.
- `layering_accord_colors.csv`에서 해당 페어의 컬러를 조회한다.
- 컬러 매핑 누락 시 애플리케이션 시작 또는 테스트에서 실패하게 한다.

```java
public LayeringColor resolve(LayeringCandidate candidate, LayeringScore score) {
  AccordPair dominantPair = score.highestContributionPair();
  return colorPalette.findByPair(dominantPair);
}
```

`LayeringColor`:

```java
public record LayeringColor(
    String name,
    String hex,
    String sourceAccord,
    String targetAccord,
    String description) {}
```

### 11.8 LayeringExplanationAssembler

책임:

- evidence code를 사용자 문장으로 변환
- title, summary, reasons, warnings 생성
- 다른 AI 호출 없이 deterministic output 보장

이 클래스에는 외부 API 클라이언트 의존성을 절대 넣지 않는다.

## 12. 데이터 추가 계획

### 12.1 CSV 추가

```text
src/main/resources/data/layering_accord_compatibility.csv
```

엑셀의 변화값 매트릭스 기준으로 작성한다.

핵심 값 예:

```csv
source_accord,target_accord,score
Floral,Floral,0.88
Floral,Woody,0.85
Floral,Fresh,0.80
Floral,Spicy,0.40
Floral,Sweet,0.88
Floral,Gourmand,0.78
Floral,Musky,0.93
Floral,Green,0.82
Floral,Citrus,0.88
Floral,Fruity,0.85
Floral,Aromatic,0.30
Floral,Earthy/Smoky,0.78
```

전체 CSV 작성값:

```csv
source_accord,target_accord,score
Floral,Floral,0.88
Floral,Woody,0.85
Floral,Fresh,0.80
Floral,Spicy,0.40
Floral,Sweet,0.88
Floral,Gourmand,0.78
Floral,Musky,0.93
Floral,Green,0.82
Floral,Citrus,0.88
Floral,Fruity,0.85
Floral,Aromatic,0.30
Floral,Earthy/Smoky,0.78
Woody,Floral,0.85
Woody,Woody,0.85
Woody,Fresh,0.60
Woody,Spicy,0.40
Woody,Sweet,0.45
Woody,Gourmand,0.75
Woody,Musky,0.75
Woody,Green,0.82
Woody,Citrus,0.85
Woody,Fruity,0.78
Woody,Aromatic,0.88
Woody,Earthy/Smoky,0.82
Fresh,Floral,0.80
Fresh,Woody,0.60
Fresh,Fresh,0.80
Fresh,Spicy,0.35
Fresh,Sweet,0.45
Fresh,Gourmand,0.20
Fresh,Musky,0.85
Fresh,Green,0.90
Fresh,Citrus,0.85
Fresh,Fruity,0.85
Fresh,Aromatic,0.80
Fresh,Earthy/Smoky,0.75
Spicy,Floral,0.40
Spicy,Woody,0.40
Spicy,Fresh,0.35
Spicy,Spicy,0.85
Spicy,Sweet,0.40
Spicy,Gourmand,0.82
Spicy,Musky,0.60
Spicy,Green,0.45
Spicy,Citrus,0.88
Spicy,Fruity,0.38
Spicy,Aromatic,0.88
Spicy,Earthy/Smoky,0.78
Sweet,Floral,0.88
Sweet,Woody,0.45
Sweet,Fresh,0.45
Sweet,Spicy,0.40
Sweet,Sweet,0.35
Sweet,Gourmand,0.35
Sweet,Musky,0.90
Sweet,Green,0.75
Sweet,Citrus,0.40
Sweet,Fruity,0.75
Sweet,Aromatic,0.80
Sweet,Earthy/Smoky,0.72
Gourmand,Floral,0.78
Gourmand,Woody,0.75
Gourmand,Fresh,0.20
Gourmand,Spicy,0.82
Gourmand,Sweet,0.35
Gourmand,Gourmand,0.25
Gourmand,Musky,0.35
Gourmand,Green,0.30
Gourmand,Citrus,0.30
Gourmand,Fruity,0.35
Gourmand,Aromatic,0.35
Gourmand,Earthy/Smoky,0.25
Musky,Floral,0.93
Musky,Woody,0.75
Musky,Fresh,0.85
Musky,Spicy,0.60
Musky,Sweet,0.90
Musky,Gourmand,0.35
Musky,Musky,0.85
Musky,Green,0.78
Musky,Citrus,0.80
Musky,Fruity,0.70
Musky,Aromatic,0.78
Musky,Earthy/Smoky,0.78
Green,Floral,0.82
Green,Woody,0.82
Green,Fresh,0.90
Green,Spicy,0.45
Green,Sweet,0.75
Green,Gourmand,0.30
Green,Musky,0.78
Green,Green,0.80
Green,Citrus,0.82
Green,Fruity,0.60
Green,Aromatic,0.75
Green,Earthy/Smoky,0.60
Citrus,Floral,0.88
Citrus,Woody,0.85
Citrus,Fresh,0.85
Citrus,Spicy,0.88
Citrus,Sweet,0.40
Citrus,Gourmand,0.30
Citrus,Musky,0.80
Citrus,Green,0.82
Citrus,Citrus,0.85
Citrus,Fruity,0.85
Citrus,Aromatic,0.93
Citrus,Earthy/Smoky,0.25
Fruity,Floral,0.85
Fruity,Woody,0.78
Fruity,Fresh,0.85
Fruity,Spicy,0.38
Fruity,Sweet,0.75
Fruity,Gourmand,0.35
Fruity,Musky,0.70
Fruity,Green,0.60
Fruity,Citrus,0.85
Fruity,Fruity,0.85
Fruity,Aromatic,0.38
Fruity,Earthy/Smoky,0.35
Aromatic,Floral,0.30
Aromatic,Woody,0.88
Aromatic,Fresh,0.80
Aromatic,Spicy,0.88
Aromatic,Sweet,0.80
Aromatic,Gourmand,0.35
Aromatic,Musky,0.78
Aromatic,Green,0.75
Aromatic,Citrus,0.93
Aromatic,Fruity,0.38
Aromatic,Aromatic,0.85
Aromatic,Earthy/Smoky,0.75
Earthy/Smoky,Floral,0.78
Earthy/Smoky,Woody,0.82
Earthy/Smoky,Fresh,0.75
Earthy/Smoky,Spicy,0.78
Earthy/Smoky,Sweet,0.72
Earthy/Smoky,Gourmand,0.25
Earthy/Smoky,Musky,0.78
Earthy/Smoky,Green,0.60
Earthy/Smoky,Citrus,0.25
Earthy/Smoky,Fruity,0.35
Earthy/Smoky,Aromatic,0.75
Earthy/Smoky,Earthy/Smoky,0.72
```

### 12.2 컬러 매핑 CSV 추가

```text
src/main/resources/data/layering_accord_colors.csv
```

#### 12.2.1 컬러 설계 원칙

컬러는 추천 점수처럼 계산 결과만으로 만들지 않고, 12x12 어코드 페어에 직접 매핑한다. 색은 사용자에게 "이 조합이 어떤 분위기인지"를 즉시 전달하는 UI 신호이기 때문에, 사람이 검수 가능한 고정값으로 유지한다.

기본 원칙:

- 향조의 감각을 색상 온도와 채도로 표현한다.
- Floral, Sweet, Fruity는 부드럽고 따뜻한 pink/coral 계열을 쓴다.
- Citrus는 밝은 yellow/gold 계열을 쓴다.
- Fresh, Aromatic은 청량한 blue-green/teal 계열을 쓴다.
- Green은 leaf/olive 계열을 쓴다.
- Woody, Gourmand, Earthy/Smoky는 brown/amber/smoke 계열을 쓴다.
- Musky는 skin beige, muted ivory 계열을 쓴다.
- 너무 원색적이거나 형광에 가까운 컬러는 피한다. 향수 카드 배경과 태그에 쓰기 쉬운 중간 채도 색을 우선한다.
- `hex`는 화면 배경으로 쓸 수 있는 대표색이다. 텍스트를 직접 얹을 때는 프론트엔드에서 별도 foreground color를 계산한다.

기본 어코드 컬러:

| Accord | Base color name | Hex | 기획 의도 |
| --- | --- | --- | --- |
| Floral | Bloom | `#E8A7C8` | 꽃잎, 로즈, 부드러운 플로럴 볼륨 |
| Woody | Cedar | `#9B6B43` | 마른 나무, 세더, 따뜻한 목재감 |
| Fresh | Mist | `#8FD7D2` | 물기, 공기감, 투명한 청량함 |
| Spicy | Ember | `#C45A3A` | 향신료, 열감, 붉은 온기 |
| Sweet | Blush | `#F2A7B5` | 달콤함, 부드러운 핑크, 설탕감 |
| Gourmand | Caramel | `#B77945` | 캐러멜, 바닐라, 디저트의 농도 |
| Musky | Skin | `#D8C9BA` | 살결, 파우더, 깨끗한 잔향 |
| Green | Leaf | `#82B366` | 잎, 풀, 생기 있는 그린감 |
| Citrus | Zest | `#F7C948` | 과피, 햇빛, 밝은 산뜻함 |
| Fruity | Orchard | `#F07F5A` | 과즙, 복숭아/베리, 생동감 |
| Aromatic | Herb | `#7EA6A1` | 허브, 라벤더, 차분한 청량감 |
| Earthy/Smoky | Smoke | `#6F5A4A` | 흙, 그을음, 묵직한 그림자 |

#### 12.2.2 12x12 컬러 매핑 테이블

각 셀은 `color_name`과 `hex`를 의미한다. 구현 시에는 이 표를 `source_accord,target_accord,color_name,hex,description` 형태의 144행 CSV로 펼친다.

| Source \ Target | Floral | Woody | Fresh | Spicy | Sweet | Gourmand | Musky | Green | Citrus | Fruity | Aromatic | Earthy/Smoky |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Floral | Bloom Bloom<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#E8A7C8;border:1px solid #ccc;"></span> `#E8A7C8` | Bloom Cedar<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C88E90;border:1px solid #ccc;"></span> `#C88E90` | Bloom Mist<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C3BBCC;border:1px solid #ccc;"></span> `#C3BBCC` | Bloom Ember<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D9878C;border:1px solid #ccc;"></span> `#D9878C` | Bloom Blush<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#ECA7C0;border:1px solid #ccc;"></span> `#ECA7C0` | Bloom Caramel<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D39491;border:1px solid #ccc;"></span> `#D39491` | Bloom Skin<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#E1B5C2;border:1px solid #ccc;"></span> `#E1B5C2` | Bloom Leaf<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#BDAC9F;border:1px solid #ccc;"></span> `#BDAC9F` | Bloom Zest<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#EEB592;border:1px solid #ccc;"></span> `#EEB592` | Bloom Orchard<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#EB969A;border:1px solid #ccc;"></span> `#EB969A` | Bloom Herb<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#BBA7B8;border:1px solid #ccc;"></span> `#BBA7B8` | Bloom Smoke<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B58793;border:1px solid #ccc;"></span> `#B58793` |
| Woody | Cedar Bloom<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#BB847B;border:1px solid #ccc;"></span> `#BB847B` | Cedar Cedar<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#9B6B43;border:1px solid #ccc;"></span> `#9B6B43` | Cedar Mist<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#96987F;border:1px solid #ccc;"></span> `#96987F` | Cedar Ember<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#AC643F;border:1px solid #ccc;"></span> `#AC643F` | Cedar Blush<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C08473;border:1px solid #ccc;"></span> `#C08473` | Cedar Caramel<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A77144;border:1px solid #ccc;"></span> `#A77144` | Cedar Skin<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B59275;border:1px solid #ccc;"></span> `#B59275` | Cedar Leaf<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#918952;border:1px solid #ccc;"></span> `#918952` | Cedar Zest<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C29245;border:1px solid #ccc;"></span> `#C29245` | Cedar Orchard<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#BF734D;border:1px solid #ccc;"></span> `#BF734D` | Cedar Herb<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#8F846A;border:1px solid #ccc;"></span> `#8F846A` | Cedar Smoke<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#896446;border:1px solid #ccc;"></span> `#896446` |
| Fresh | Mist Bloom<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B4C3CE;border:1px solid #ccc;"></span> `#B4C3CE` | Mist Cedar<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#94AA96;border:1px solid #ccc;"></span> `#94AA96` | Mist Mist<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#8FD7D2;border:1px solid #ccc;"></span> `#8FD7D2` | Mist Ember<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A5A392;border:1px solid #ccc;"></span> `#A5A392` | Mist Blush<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B9C3C6;border:1px solid #ccc;"></span> `#B9C3C6` | Mist Caramel<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A0B097;border:1px solid #ccc;"></span> `#A0B097` | Mist Skin<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#AED1C8;border:1px solid #ccc;"></span> `#AED1C8` | Mist Leaf<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#8AC8A5;border:1px solid #ccc;"></span> `#8AC8A5` | Mist Zest<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#BBD198;border:1px solid #ccc;"></span> `#BBD198` | Mist Orchard<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B8B2A0;border:1px solid #ccc;"></span> `#B8B2A0` | Mist Herb<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#88C2BD;border:1px solid #ccc;"></span> `#88C2BD` | Mist Smoke<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#82A399;border:1px solid #ccc;"></span> `#82A399` |
| Spicy | Ember Bloom<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D37A76;border:1px solid #ccc;"></span> `#D37A76` | Ember Cedar<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B3613E;border:1px solid #ccc;"></span> `#B3613E` | Ember Mist<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#AE8F7A;border:1px solid #ccc;"></span> `#AE8F7A` | Ember Ember<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C45A3A;border:1px solid #ccc;"></span> `#C45A3A` | Ember Blush<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D77A6E;border:1px solid #ccc;"></span> `#D77A6E` | Ember Caramel<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#BF673F;border:1px solid #ccc;"></span> `#BF673F` | Ember Skin<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#CC8970;border:1px solid #ccc;"></span> `#CC8970` | Ember Leaf<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A87F4C;border:1px solid #ccc;"></span> `#A87F4C` | Ember Zest<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D98940;border:1px solid #ccc;"></span> `#D98940` | Ember Orchard<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D66A47;border:1px solid #ccc;"></span> `#D66A47` | Ember Herb<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A77A65;border:1px solid #ccc;"></span> `#A77A65` | Ember Smoke<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A05A41;border:1px solid #ccc;"></span> `#A05A41` |
| Sweet | Blush Bloom<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#EEA7BD;border:1px solid #ccc;"></span> `#EEA7BD` | Blush Cedar<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#CD8E85;border:1px solid #ccc;"></span> `#CD8E85` | Blush Mist<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C8BBC1;border:1px solid #ccc;"></span> `#C8BBC1` | Blush Ember<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#DF8781;border:1px solid #ccc;"></span> `#DF8781` | Blush Blush<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#F2A7B5;border:1px solid #ccc;"></span> `#F2A7B5` | Blush Caramel<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D99486;border:1px solid #ccc;"></span> `#D99486` | Blush Skin<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#E7B5B7;border:1px solid #ccc;"></span> `#E7B5B7` | Blush Leaf<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C3AC94;border:1px solid #ccc;"></span> `#C3AC94` | Blush Zest<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#F4B587;border:1px solid #ccc;"></span> `#F4B587` | Blush Orchard<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#F1968F;border:1px solid #ccc;"></span> `#F1968F` | Blush Herb<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C1A7AD;border:1px solid #ccc;"></span> `#C1A7AD` | Blush Smoke<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#BB8788;border:1px solid #ccc;"></span> `#BB8788` |
| Gourmand | Caramel Bloom<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#CC8C7C;border:1px solid #ccc;"></span> `#CC8C7C` | Caramel Cedar<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#AB7344;border:1px solid #ccc;"></span> `#AB7344` | Caramel Mist<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A6A080;border:1px solid #ccc;"></span> `#A6A080` | Caramel Ember<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#BC6C40;border:1px solid #ccc;"></span> `#BC6C40` | Caramel Blush<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D08C74;border:1px solid #ccc;"></span> `#D08C74` | Caramel Caramel<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B77945;border:1px solid #ccc;"></span> `#B77945` | Caramel Skin<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C59B76;border:1px solid #ccc;"></span> `#C59B76` | Caramel Leaf<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A19153;border:1px solid #ccc;"></span> `#A19153` | Caramel Zest<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D29B46;border:1px solid #ccc;"></span> `#D29B46` | Caramel Orchard<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#CF7C4E;border:1px solid #ccc;"></span> `#CF7C4E` | Caramel Herb<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#9F8C6C;border:1px solid #ccc;"></span> `#9F8C6C` | Caramel Smoke<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#996C47;border:1px solid #ccc;"></span> `#996C47` |
| Musky | Skin Bloom<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#DFBBC0;border:1px solid #ccc;"></span> `#DFBBC0` | Skin Cedar<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#BEA288;border:1px solid #ccc;"></span> `#BEA288` | Skin Mist<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B9CFC4;border:1px solid #ccc;"></span> `#B9CFC4` | Skin Ember<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D09A84;border:1px solid #ccc;"></span> `#D09A84` | Skin Blush<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#E3BBB8;border:1px solid #ccc;"></span> `#E3BBB8` | Skin Caramel<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#CAA789;border:1px solid #ccc;"></span> `#CAA789` | Skin Skin<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D8C9BA;border:1px solid #ccc;"></span> `#D8C9BA` | Skin Leaf<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B4C097;border:1px solid #ccc;"></span> `#B4C097` | Skin Zest<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#E5C98A;border:1px solid #ccc;"></span> `#E5C98A` | Skin Orchard<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#E2AA92;border:1px solid #ccc;"></span> `#E2AA92` | Skin Herb<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B2BAB0;border:1px solid #ccc;"></span> `#B2BAB0` | Skin Smoke<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#AC9A8B;border:1px solid #ccc;"></span> `#AC9A8B` |
| Green | Leaf Bloom<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#ADAE8F;border:1px solid #ccc;"></span> `#ADAE8F` | Leaf Cedar<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#8D9557;border:1px solid #ccc;"></span> `#8D9557` | Leaf Mist<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#87C293;border:1px solid #ccc;"></span> `#87C293` | Leaf Ember<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#9E8E54;border:1px solid #ccc;"></span> `#9E8E54` | Leaf Blush<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B1AE87;border:1px solid #ccc;"></span> `#B1AE87` | Leaf Caramel<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#989B58;border:1px solid #ccc;"></span> `#989B58` | Leaf Skin<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A6BC89;border:1px solid #ccc;"></span> `#A6BC89` | Leaf Leaf<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#82B366;border:1px solid #ccc;"></span> `#82B366` | Leaf Zest<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B3BC59;border:1px solid #ccc;"></span> `#B3BC59` | Leaf Orchard<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B09D61;border:1px solid #ccc;"></span> `#B09D61` | Leaf Herb<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#80AE7F;border:1px solid #ccc;"></span> `#80AE7F` | Leaf Smoke<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#7A8E5A;border:1px solid #ccc;"></span> `#7A8E5A` |
| Citrus | Zest Bloom<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#F1BB7E;border:1px solid #ccc;"></span> `#F1BB7E` | Zest Cedar<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D0A246;border:1px solid #ccc;"></span> `#D0A246` | Zest Mist<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#CBCF82;border:1px solid #ccc;"></span> `#CBCF82` | Zest Ember<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#E29A42;border:1px solid #ccc;"></span> `#E29A42` | Zest Blush<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#F5BB76;border:1px solid #ccc;"></span> `#F5BB76` | Zest Caramel<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#DCA747;border:1px solid #ccc;"></span> `#DCA747` | Zest Skin<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#EAC978;border:1px solid #ccc;"></span> `#EAC978` | Zest Leaf<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C6C055;border:1px solid #ccc;"></span> `#C6C055` | Zest Zest<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#F7C948;border:1px solid #ccc;"></span> `#F7C948` | Zest Orchard<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#F4AA50;border:1px solid #ccc;"></span> `#F4AA50` | Zest Herb<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C4BA6D;border:1px solid #ccc;"></span> `#C4BA6D` | Zest Smoke<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#BE9A49;border:1px solid #ccc;"></span> `#BE9A49` |
| Fruity | Orchard Bloom<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#ED9088;border:1px solid #ccc;"></span> `#ED9088` | Orchard Cedar<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#CC7750;border:1px solid #ccc;"></span> `#CC7750` | Orchard Mist<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C7A48C;border:1px solid #ccc;"></span> `#C7A48C` | Orchard Ember<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#DE6F4D;border:1px solid #ccc;"></span> `#DE6F4D` | Orchard Blush<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#F19080;border:1px solid #ccc;"></span> `#F19080` | Orchard Caramel<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#D87C51;border:1px solid #ccc;"></span> `#D87C51` | Orchard Skin<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#E69E82;border:1px solid #ccc;"></span> `#E69E82` | Orchard Leaf<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C2955F;border:1px solid #ccc;"></span> `#C2955F` | Orchard Zest<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#F39E52;border:1px solid #ccc;"></span> `#F39E52` | Orchard Orchard<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#F07F5A;border:1px solid #ccc;"></span> `#F07F5A` | Orchard Herb<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#C08F78;border:1px solid #ccc;"></span> `#C08F78` | Orchard Smoke<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#BA6F53;border:1px solid #ccc;"></span> `#BA6F53` |
| Aromatic | Herb Bloom<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#ABA6B1;border:1px solid #ccc;"></span> `#ABA6B1` | Herb Cedar<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#8A8D7A;border:1px solid #ccc;"></span> `#8A8D7A` | Herb Mist<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#85BBB6;border:1px solid #ccc;"></span> `#85BBB6` | Herb Ember<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#9B8676;border:1px solid #ccc;"></span> `#9B8676` | Herb Blush<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#AFA6A9;border:1px solid #ccc;"></span> `#AFA6A9` | Herb Caramel<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#96937A;border:1px solid #ccc;"></span> `#96937A` | Herb Skin<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A4B5AC;border:1px solid #ccc;"></span> `#A4B5AC` | Herb Leaf<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#80AB88;border:1px solid #ccc;"></span> `#80AB88` | Herb Zest<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#B1B57C;border:1px solid #ccc;"></span> `#B1B57C` | Herb Orchard<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#AE9683;border:1px solid #ccc;"></span> `#AE9683` | Herb Herb<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#7EA6A1;border:1px solid #ccc;"></span> `#7EA6A1` | Herb Smoke<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#78867C;border:1px solid #ccc;"></span> `#78867C` |
| Earthy/Smoky | Smoke Bloom<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A27A7F;border:1px solid #ccc;"></span> `#A27A7F` | Smoke Cedar<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#816147;border:1px solid #ccc;"></span> `#816147` | Smoke Mist<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#7C8F83;border:1px solid #ccc;"></span> `#7C8F83` | Smoke Ember<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#935A43;border:1px solid #ccc;"></span> `#935A43` | Smoke Blush<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A67A77;border:1px solid #ccc;"></span> `#A67A77` | Smoke Caramel<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#8D6748;border:1px solid #ccc;"></span> `#8D6748` | Smoke Skin<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#9B8979;border:1px solid #ccc;"></span> `#9B8979` | Smoke Leaf<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#777F56;border:1px solid #ccc;"></span> `#777F56` | Smoke Zest<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A88949;border:1px solid #ccc;"></span> `#A88949` | Smoke Orchard<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#A56A51;border:1px solid #ccc;"></span> `#A56A51` | Smoke Herb<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#757A6F;border:1px solid #ccc;"></span> `#757A6F` | Smoke Smoke<br><span style="display:inline-block;width:14px;height:14px;border-radius:3px;background:#6F5A4A;border:1px solid #ccc;"></span> `#6F5A4A` |

#### 12.2.3 CSV 포맷

권장 포맷:

```csv
source_accord,target_accord,color_name,hex,description
Floral,Floral,Bloom Veil,#E8A7C8,풍성한 꽃다발의 부드러운 겹을 표현하는 로즈 핑크
Floral,Woody,Petal Cedar,#C99A7A,플로럴의 부드러움과 우디의 따뜻한 결을 표현하는 로즈우드
Citrus,Floral,Luminous Garden,#F4B7A8,밝은 시트러스와 부드러운 플로럴의 연결감을 표현하는 코랄 핑크
Earthy/Smoky,Woody,Smoke Cedar,#6F5A4A,스모키한 흙내음과 나무의 깊이를 표현하는 스모크 브라운
```

위 예시는 포맷 샘플이다. 실제 값은 12x12 컬러 매핑 테이블의 `color_name`, `hex`를 사용한다. `description`은 다음 규칙으로 짧게 작성한다.

```text
{source_accord_ko}와 {target_accord_ko} 무드를 함께 표현하는 {color_name} 컬러입니다.
```

예:

```csv
Citrus,Floral,Zest Bloom,#F1BB7E,시트러스와 플로럴 무드를 함께 표현하는 Zest Bloom 컬러입니다.
```

#### 12.2.4 검증 규칙

구현 시 검증 규칙:

- `layering_accord_compatibility.csv`에 존재하는 모든 source/target pair가 컬러 CSV에도 있어야 한다.
- 컬러 CSV의 pair 수는 정확히 144개여야 한다.
- 중복 pair가 있으면 애플리케이션 시작 또는 테스트에서 실패한다.
- `hex`는 `^#[0-9A-Fa-f]{6}$` 형식이어야 한다.
- `color_name`과 `description`은 비어 있으면 안 된다.
- `Earthy`는 `Earthy/Smoky`로 정규화한다.
- API 응답에서 `sourceAccord`, `targetAccord`는 실제 선택된 dominant contribution pair와 일치해야 한다.
- 프론트엔드가 텍스트를 컬러 위에 직접 얹는 경우, 백엔드 색을 그대로 텍스트 배경으로 쓰지 말고 프론트엔드에서 foreground contrast를 별도 계산한다.

### 12.3 설명 문구 CSV

설명용 문구는 resource CSV로 관리한다.

필수 리소스:

```text
src/main/resources/data/layering_accord_narratives.csv
src/main/resources/data/layering_rule_explanations.csv
```

`layering_accord_narratives.csv`는 `2.3 어코드 설명 데이터`의 구현용 CSV 값을 사용한다.

`layering_rule_explanations.csv` 권장 포맷:

```csv
evidence_code,severity,message_template
HIGH_MATRIX_COMPATIBILITY,reason,{accordA}와 {accordB}의 궁합 점수가 높아 두 향의 연결이 자연스럽습니다.
BASE_ANCHOR_PRESENT,reason,{perfumeName}의 {accord} 계열이 조합에 안정적인 깊이감을 더합니다.
TOP_LIFT_PRESENT,reason,{perfumeName}의 {accord} 계열이 조합의 인상을 밝고 산뜻하게 만듭니다.
SWEET_OVERLOAD,warning,단 향이 겹쳐 답답하게 느껴질 수 있어 산뜻한 조합을 원한다면 맞지 않을 수 있습니다.
FRESH_WITHOUT_BASE,warning,상쾌한 향이 중심인 조합이라 지속감은 가볍게 느껴질 수 있습니다.
COLOR_PAIR_SELECTED,reason,{accordA}와 {accordB}의 무드를 {colorName} 컬러로 표현했습니다.
```

검증 규칙:

- narrative CSV에는 현재 12개 어코드가 모두 있어야 한다.
- `accord_name`은 `accord.csv` 및 compatibility matrix의 정규화 이름과 일치해야 한다.
- `display_name_ko`, `impression`, `positive_phrase`, `risk_phrase`, `title_adjective`, `summary_phrase`는 비어 있으면 안 된다.
- `season_tags`, `occasion_tags`, `representative_notes`는 세미콜론(`;`) 구분 문자열로 저장한다.
- rule explanation CSV의 `severity`는 `reason` 또는 `warning`만 허용한다.

## 13. 추천 정확도 개선 방향

### 13.1 MVP 정확도

MVP 정확도는 다음 3개 축으로 확보한다.

```text
1. 어코드 비율 기반 weighted matrix score
2. top/mid/base 노트 기반 구조 보정
3. 과밀/과단/휘발 risk penalty
```

이 정도면 “dominant accord 하나만 보고 추천”하는 방식보다 훨씬 안정적이다.

### 13.2 다음 단계

추후 사용자 피드백을 받으면 다음처럼 개선한다.

```text
POST /api/layering/recommendations/{recommendationId}/feedback
```

저장 데이터:

```text
- user_id
- input perfume ids
- recommended perfume ids
- score
- liked/disliked
- too_sweet
- too_heavy
- too_weak
- too_sharp
- free_text(optional)
```

그러나 MVP에는 넣지 않는다. 현재 목표는 deterministic backend recommendation이다.

## 14. 테스트 계획

### 14.1 Unit Tests

`LayeringCompatibilityMatrixTest`

- CSV가 12x12 전체 조합을 포함하는지 검증
- `Earthy` 입력이 `Earthy/Smoky`로 정규화되는지 검증
- 없는 accord lookup 시 예외 발생 검증
- score 범위가 0~1인지 검증

`LayeringScoreCalculatorTest`

- `Floral + Musky`가 높은 matrix score를 받는지 검증
- `Gourmand + Fresh` 조합에 낮은 compatibility 또는 warning evidence가 붙는지 검증
- `Sweet + Gourmand`가 모두 강한 2개 조합에서 sweet overload penalty가 적용되는지 검증
- 같은 입력에 대해 같은 finalScore가 나오는지 검증
- 입력 순서를 바꿔도 같은 finalScore가 나오는지 검증

`LayeringColorResolverTest`

- dominant contribution pair에 맞는 컬러가 반환되는지 검증
- 144개 어코드 페어 컬러 매핑이 모두 존재하는지 검증
- `hex` 값이 `#RRGGBB` 형식인지 검증

`AccordNarrativeRepositoryTest`

- narrative CSV가 12개 어코드를 모두 포함하는지 검증
- 필수 문구 필드가 비어 있지 않은지 검증
- `season_tags`, `occasion_tags`, `representative_notes`가 세미콜론 구분 리스트로 파싱되는지 검증
- 없는 어코드 narrative 조회 시 명확한 예외가 발생하는지 검증

`LayeringExplanationAssemblerTest`

- evidence code가 reason 문장으로 변환되는지 검증
- warning evidence가 warnings에만 들어가는지 검증
- 추천 점수 구간에 따라 `recommended`와 `decision`이 맞게 생성되는지 검증
- reasons 최대 3개, warnings 최대 2개 제한 검증

### 14.2 Controller Tests

`LayeringRecommendationControllerTest`

- 향수 ID 2개 요청 성공
- 향수 ID 3개 요청 `400`
- 향수 ID 1개 요청 `400`
- 향수 ID 중복 요청 `400`
- 존재하지 않는 향수 포함 `404`
- 응답 JSON 필드 계약 검증

### 14.3 Repository Tests

- `findByPerfumeIdIn`이 입력 ID의 accords를 모두 반환하는지 검증
- `findByPerfumeIdIn`이 입력 ID의 notes를 모두 반환하는지 검증

## 15. 구현 순서

### Step 1. 데이터 리소스 추가

- `layering_accord_compatibility.csv` 추가
- `layering_accord_colors.csv` 추가
- `layering_accord_narratives.csv` 추가
- `layering_rule_explanations.csv` 추가

완료 기준:

- matrix loader test 통과
- color mapping completeness test 통과
- narrative completeness test 통과
- 12개 어코드 전체 조합 검증 통과

### Step 2. 내부 모델과 matrix loader 구현

- `LayeringCompatibilityMatrix`
- `AccordNameNormalizer`
- `AccordWeight`
- `LayeringPerfumeProfile`
- `RoleVector`

완료 기준:

- score lookup 가능
- 향수 어코드 비율 정규화 가능

### Step 3. 후보 생성 및 점수 계산 구현

- `LayeringCandidateFactory`
- `LayeringScoreCalculator`
- penalty/evidence rules

완료 기준:

- 2개 입력에서 후보 1개 생성
- 후보에 finalScore와 breakdown 생성

### Step 4. 추천 여부와 컬러 구현

- `LayeringColorResolver`
- `LayeringColor`
- `LayeringDecision`

완료 기준:

- 점수 구간별 `recommended`, `decision` 생성
- dominant accord pair 기준 컬러 조회
- 컬러 매핑 누락 시 테스트 실패

### Step 5. 설명 생성 구현

- `LayeringExplanationAssembler`
- `AccordNarrative`
- `RuleExplanation`

완료 기준:

- 외부 AI 호출 없이 title/summary/reasons/warnings 생성
- evidence와 설명이 불일치하지 않음

### Step 6. API 구현

- `LayeringRecommendationController`
- `LayeringRecommendationService`
- DTO 작성
- `docs/rest-api-spec.md`에 API 명세 추가

완료 기준:

- `POST /api/layering/recommendations` 동작
- validation/error response가 기존 공통 규칙과 일치

### Step 7. 검증

권장 명령:

```powershell
.\gradlew.bat test
.\gradlew.bat spotlessCheck
```

문서만 작성한 현재 단계에서는 빌드 실행이 필수는 아니다. 구현 단계에서는 반드시 실행한다.

## 16. 운영 및 확장 고려

### 16.1 성능

입력은 항상 향수 2개라 계산량은 작다.

```text
후보 수: 1
향수당 어코드 수: 보통 3~8
pair 계산: 최대 수십 회
```

DB 조회만 N+1 없이 처리하면 별도 캐시가 없어도 충분하다.

### 16.2 캐싱

캐싱이 필요한 대상:

- compatibility matrix
- accord narratives
- rule explanations

이들은 애플리케이션 시작 시 메모리에 로드한다.

캐싱이 필요 없는 대상:

- 추천 결과 전체

추천 결과는 향수 데이터/문구 변경 시 무효화가 번거롭다. 초기에는 계산 비용이 낮으므로 매번 계산한다.

### 16.3 관측성

추천 결과의 품질을 운영에서 확인하려면 로그에 다음 정도만 남긴다.

```text
input perfume ids
input perfume ids
final score
score breakdown
warning evidence codes
```

사용자 식별자는 인증 기능과 연결하기 전까지 남기지 않는다.

### 16.4 보안/안전

- 공개 API이므로 request body 크기는 작게 유지한다.
- 입력 ID는 정확히 2개만 허용한다.
- 향수/화장품 사용 관련 의학적 효능을 주장하지 않는다.
- 민감 피부 사용자는 적은 양으로 테스트하라는 warning 문구를 제공할 수 있다.

## 17. 예시 시나리오

입력:

```json
{
  "perfumeIds": [4367, 10806]
}
```

데이터 예:

```text
Karma: Citrus, Woody, Aromatic, Spicy, Sweet, Earthy/Smoky
Lust: Floral, Woody, Sweet
```

예상 추천 성향:

- `Karma + Lust`: Citrus/Floral/Woody 연결이 좋아 밝고 부드러운 조합
- `Lust`의 Floral/Sweet가 중심감을 만들고, `Karma`의 Citrus/Aromatic이 첫인상을 밝게 열어준다.
- 둘 다 Sweet가 포함되어 있으므로 단 향이 부담스러우면 `Lust`를 1회로 제한하는 warning을 붙일 수 있다.

이 경우 응답은 후보 랭킹이 아니라 하나의 평가 결과다. 점수가 낮으면 다른 후보를 만들어 숨기지 않고, 낮은 점수와 이유, warning을 그대로 반환한다.

## 18. 개발 착수 체크리스트

- [ ] `layering` 패키지 생성
- [ ] `layering_accord_compatibility.csv` 추가
- [ ] `layering_accord_colors.csv` 추가
- [ ] matrix loader와 완전성 테스트 작성
- [ ] color mapping 완전성 테스트 작성
- [ ] 향수 2개 profile 조회 구현
- [ ] weighted compatibility 계산 구현
- [ ] structure/balance/penalty/evidence 계산 구현
- [ ] recommendation decision 생성 구현
- [ ] dominant accord pair 기반 color resolver 구현
- [ ] deterministic explanation assembler 구현
- [ ] `POST /api/layering/recommendations` API 구현
- [ ] `docs/rest-api-spec.md`에 공개 계약 추가
- [ ] unit/controller/repository tests 작성
- [ ] `spotlessCheck`와 `test` 실행

## 19. 결정 사항 요약

- 엑셀 변화값 매트릭스는 최종 점수가 아니라 compatibility offset으로 사용한다.
- 향수 2개 입력에서 단일 pair 조합을 평가한다.
- 결과에는 레이어링 방법, 순서, 분사량을 제공하지 않는다.
- 후보 랭킹보다 두 향수의 궁합, 추천 여부, warning, 무드 컬러에 집중한다.
- 컬러는 144개 어코드 페어를 직접 매핑한 `layering_accord_colors.csv`에서 조회한다.
- 설명은 어코드 조합별 수작업 문장이 아니라 evidence 기반 템플릿으로 생성한다.
- 외부 AI 호출 없이 백엔드 내부 데이터와 규칙만으로 추천 결과를 만든다.
- 첫 버전에는 피드백 학습, 사용자 개인화, 별도 추천 저장 테이블을 넣지 않는다.
