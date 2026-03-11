# FE 학습 Q&A 기록

> Step 1~2 진행하면서 나온 질문과 답변 모음. 복습용.

---

## Step 1: 프로젝트 기반 셋업

### Q1. interface가 "런타임에 사라진다"는 게 무슨 뜻?

TypeScript는 결국 JavaScript로 **변환(컴파일)**되어 브라우저에서 실행된다.

```typescript
// TypeScript (개발할 때 작성하는 코드)
interface Region {
  name: string;
  code: string;
}
const region: Region = { name: "서울", code: "11" };
```

```javascript
// JavaScript (브라우저에서 실제로 실행되는 코드)
const region = { name: "서울", code: "11" };
```

`interface Region`이 통째로 사라진다. JS에는 `interface`라는 개념 자체가 없기 때문. TypeScript 컴파일러가 **"이 객체에 name, code가 있는지?"를 빌드 시점에 검증**하고, 검증이 끝나면 interface 정의는 버려진다.

Kotlin과 비교:
- Kotlin `data class` → **런타임에 존재**. 리플렉션으로 필드 조회 가능
- TypeScript `interface` → **런타임에 없음**. 오직 개발 시 타입 체크 도구

그래서 TypeScript에서는 `if (obj instanceof Region)` 같은 건 못 한다. 런타임에 `Region`이라는 게 없으니까.

---

### Q2. 제네릭 `<T>`는 어떻게 추론하는 거야?

함수 정의:
```typescript
async function apiFetch<T>(path: string): Promise<T> {
  // ...
  return json.data;  // 이 값을 T 타입으로 간주
}
```

호출할 때:
```typescript
const result = apiFetch<Region[]>('/v1/.../sido');
//                      ^^^^^^^^
//                      T = Region[] 로 지정
```

`<Region[]>`을 넣으면 함수의 `T` 자리가 전부 `Region[]`로 **치환**된다:
```typescript
// T = Region[] 이 되니까
async function apiFetch(path: string): Promise<Region[]>
```

`result`의 타입이 `Promise<Region[]>`이 되고, `await`하면 `Region[]`이 된다.

Kotlin 제네릭과 완전히 같은 원리:
```kotlin
fun <T> apiFetch(path: String): T
val result: List<Region> = apiFetch<List<Region>>("/v1/.../sido")
```

**주의**: 이것도 컴파일 타임 검증일 뿐. 실제로 백엔드가 `Region[]` 형태가 아닌 데이터를 보내면, TypeScript는 막지 못한다. "나는 이 API가 Region[]을 줄 거라고 **믿겠다**"는 선언인 거다.

---

### Q3. `NEXT_PUBLIC_` 접두사가 뭐야?

Next.js는 보안을 위해 **환경변수를 기본적으로 서버에서만** 읽을 수 있게 한다.

```bash
# .env.local
SECRET_KEY=abc123                                # 서버에서만 접근 가능
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080    # 브라우저에서도 접근 가능
```

`NEXT_PUBLIC_`이 붙은 변수만 빌드 시점에 JS 번들에 **문자열로 삽입**된다:
```javascript
// 빌드 후 브라우저에 전달되는 코드
const API_BASE = "http://localhost:8080";  // 값이 직접 박힘
```

`api-client.ts`는 `'use client'` 컴포넌트에서 호출되니까 **브라우저에서 실행**된다. `NEXT_PUBLIC_`이 없으면 `undefined`.

Spring Boot로 비유:
- `NEXT_PUBLIC_` 있음 → `application.yml`의 일반 설정 (공개 가능)
- `NEXT_PUBLIC_` 없음 → 서버 내부에서만 쓰는 시크릿 (DB 비밀번호 등)

---

### Q4. queryKey는 뭐야? queryClient는 뭐야?

**QueryClient**는 TanStack Query의 **캐시 저장소**. Spring의 `CacheManager`와 같다.

```
QueryClient (캐시 저장소)
├── ['regions', 'sido']           → [서울, 경기, ...]
├── ['regions', 'sigungu', '11']  → [강남구, 서초구, ...]
├── ['regions', 'sigungu', '41']  → [수원시, 성남시, ...]
└── ['complexes', '11680', ...]   → [래미안, 아크로, ...]
```

**queryKey**는 이 캐시의 **키(key)**. Redis의 key와 동일 개념.

```typescript
useQuery({
  queryKey: ['regions', 'sigungu', sidoCode],  // 캐시 키
  queryFn: () => apiFetch(...),                 // 캐시 미스 시 호출할 함수
  enabled: !!sidoCode,                          // 조건부 실행
});
```

동작 흐름:
1. `queryKey`로 캐시를 조회
2. **캐시 히트** + 아직 fresh → 캐시 데이터 즉시 반환 (API 호출 안 함)
3. **캐시 미스** 또는 stale → `queryFn` 실행 → 응답을 캐시에 저장

`staleTime: 60 * 1000`은 Redis의 TTL과 비슷. 1분간은 같은 키로 조회해도 API를 다시 호출하지 않는다.

queryKey가 배열인 이유: `['regions', 'sigungu', '11']`과 `['regions', 'sigungu', '41']`이 **다른 캐시 항목**이 되어야 하니까. 시도를 바꾸면 시군구 목록도 달라져야 한다.

---

## Step 2: 지역 선택 + 단지 목록 (리스트 페이지)

### Q5. `?`(optional)와 `| null` 중에 뭐가 더 안전해?

**`| null`이 더 안전하다.**

```typescript
interface A { builtYear?: number }       // 필드가 없어도 OK
interface B { builtYear: number | null }  // 필드는 반드시 있어야 함, 값만 null 허용
```

차이가 드러나는 상황:
```typescript
// interface A (optional)
const a: A = {};                       // OK — builtYear 자체가 없어도 됨
const b: A = { builtYear: null };      // 컴파일 에러! — undefined는 되지만 null은 안 됨

// interface B (null union)
const c: B = {};                       // 컴파일 에러! — builtYear 필드가 반드시 있어야 함
const d: B = { builtYear: null };      // OK
```

백엔드 API 응답은 보통 JSON으로 오는데, JSON에서는 필드가 항상 존재하고 값만 `null`인 경우가 대부분:
```json
{ "builtYear": null, "name": "래미안" }
```

`| null`이 **백엔드 응답의 실제 형태에 더 정확히 대응**하고, "이 필드는 반드시 존재해야 한다"는 제약이 추가되니 더 안전.

`?`는 **함수 파라미터처럼 "안 넘겨도 되는" 경우**에 쓴다:
```typescript
interface SearchParams {
  keyword: string;
  page?: number;    // 안 넘기면 서버 기본값 사용 → optional이 맞음
}
```

---

### Q6. 왜 상태를 `apt-list-page`가 갖고, 자식은 props로 받게 했어?

**자식들이 같은 상태를 공유해야 하기 때문.**

만약 `RegionSelector`가 `sidoCode` 상태를 직접 갖고 있으면:
```
RegionSelector: sidoCode = '11' (서울)
useGetComplexes: sidoCode = ??? ← 어떻게 알지?
```

`RegionSelector` 안에서 "서울"을 선택했는데, 단지 목록 API가 이 값을 모른다. 별개의 컴포넌트니까.

**공유가 필요한 상태는 두 컴포넌트의 공통 부모로 올린다**:
```
apt-list-page: sidoCode = '11'  ← 여기서 관리
  ├── RegionSelector(sidoCode='11')  ← props로 읽기
  └── useGetComplexes(sidoCode='11') ← 같은 값 참조
```

이게 React의 "상태 끌어올리기(Lifting State Up)" 패턴.

Spring 비유:
```kotlin
// Bad — 각 서비스가 자기 상태를 따로 관리
class RegionService {
    private var selectedSido = ""  // 여기서만 알고 있음
}
class ComplexService {
    private var selectedSido = ""  // 동기화 어떻게?
}

// Good — 상위에서 관리하고 주입
class ApartmentFacade(
    private val regionService: RegionService,
    private val complexService: ComplexService,
) {
    fun selectSido(code: String) {
        regionService.loadSigungu(code)     // 같은 code를
        complexService.loadComplexes(code)  // 둘 다에게 전달
    }
}
```

---

### Q7. props 쓰는 이유는? store나 provider 대신?

**이 규모에서는 props가 가장 단순하고 추적이 쉽기 때문.**

| 방법 | 장점 | 단점 | 적합한 상황 |
|------|------|------|------------|
| **props** | 데이터 흐름 명확 (위→아래), 디버깅 쉬움 | 깊이 깊으면 번거로움 (prop drilling) | 부모→자식 1~2단계 |
| **Context/Provider** | 중간 컴포넌트 건너뛰고 전달 가능 | 값 바뀌면 구독한 모든 컴포넌트 리렌더 | 테마, 인증, 언어 같은 전역 설정 |
| **Store (Zustand 등)** | 어디서든 접근, 세밀한 구독 가능 | 의존성 추가, 어디서 바꾸는지 추적 어려움 | 복잡한 전역 상태 |

현재 구조에서 props 전달은 **1단계**:
```
apt-list-page → RegionSelector    (1단계)
apt-list-page → ComplexCard       (1단계)
```

store로 바꾸면 `sidoCode`가 어디서 바뀌고 어디서 읽히는지 **파일을 돌아다니면서 찾아야** 한다. props면 컴포넌트 선언부만 보면 데이터 흐름이 한눈에 보인다.

**prop drilling이 문제가 되는 경우**:
```
Page → Layout → Section → Card → Header → Title
                                            └── sidoCode가 필요
```
5단계를 거쳐야 하면, 중간 4개 컴포넌트가 `sidoCode`를 그냥 통과시키기만 하는 거라 번거롭다. 그때 Context나 Store를 쓴다.

현재는 1단계라 **props가 정답**.

---

## 페이지 렌더링 흐름 (전체 과정)

### Q8. 홈페이지로 진입하면 어떻게 해서 화면이 그려지는 거야?

#### 1단계: 서버에서 HTML 뼈대 생성

```
브라우저: GET http://localhost:3000
    ↓
Next.js 서버
    ↓
layout.tsx (Server Component) 실행
  → <html><body><Providers>{children}</Providers></body></html>
    ↓
page.tsx (Server Component) 실행
  → <Suspense fallback={로딩스피너}><AptListPage /></Suspense>
```

`AptListPage`는 Client Component라서 서버에서는 자리만 마련. 사용자는 **로딩 스피너를 먼저** 본다.

#### 2단계: 브라우저에서 JS 실행 (Hydration)

```
JS 번들 다운로드 + 실행
    ↓
Providers 초기화 (QueryClient, NuqsAdapter)
    ↓
AptListPage 렌더링 시작
```

AptListPage 첫 렌더:
```typescript
sidoCode = ''          // URL에서 읽기 (아무것도 없음)
sigunguCode = ''
eupmyeondongCode = ''

useGetComplexes({ sidoSigunguCode: '' })
→ enabled: false → API 호출 안 함
```

RegionSelector 첫 렌더:
```typescript
useGetSidoList()              → API 호출! (enabled 없으니 항상 실행)
useGetSigunguList('')         → enabled: false → 호출 안 함
useGetEupmyeondongList('')    → enabled: false → 호출 안 함
```

→ 화면: Select 3개 + "시/도와 시/군/구를 선택해주세요"

#### 3단계: "서울" 선택

```
Select에서 "서울" 클릭
    ↓
onValueChange → onSidoChange('11') → handleSidoChange('11')
    ↓
setSidoCode('11'), setSigunguCode(''), setEupmyeondongCode('')
    ↓
상태 변경 → 리렌더!
    ↓
URL: /?sido=11
```

리렌더 시:
```typescript
useGetSidoList()
→ queryKey ['regions', 'sido'] → 캐시 히트! → API 호출 안 함

useGetSigunguList('11')
→ queryKey ['regions', 'sigungu', '11'] → 캐시 미스 + enabled: true
→ API 호출! GET /v1/apartments/regions/sigungu?sidoCode=11
```

#### 4단계: "강남구" 선택

```
setSigunguCode('11680')
    ↓
URL: /?sido=11&sigungu=11680
    ↓
리렌더!
```

**2곳에서 API 호출**:
```
RegionSelector:
  useGetEupmyeondongList('11680') → API 호출!

AptListPage:
  useGetComplexes({ sidoSigunguCode: '11680' }) → API 호출!
```

→ 응답 도착 → 리렌더 → 단지 카드 목록 표시

#### 전체 흐름 요약

```
[진입] layout → page → Suspense → AptListPage 렌더
  └── useGetSidoList() → API 호출 1개

[서울 선택] setSidoCode('11') → 리렌더
  └── useGetSigunguList('11') → API 호출

[강남구 선택] setSigunguCode('11680') → 리렌더
  ├── useGetEupmyeondongList('11680') → API 호출
  └── useGetComplexes('11680') → API 호출

[응답 도착] → 리렌더 → 카드 목록 표시

[래미안 클릭] <Link href="/12345"> → 상세 페이지 이동
```

핵심: **"상태 변경 → 리렌더 → 새 props/파라미터로 자동 API 호출"** 사이클.
명령형(`fetch().then()...`)이 아니라 **선언형**("이 상태일 때 이 데이터가 필요해")으로 동작.

---

## 파일별 역할 요약

| 파일 | 역할 | 핵심 개념 |
|------|------|----------|
| `types/index.ts` | 백엔드 DTO 대응 타입 정의 | interface, `\| null` vs `?`, 제네릭 |
| `lib/api-client.ts` | fetch 래퍼, 공통 에러 처리 | 제네릭 `<T>`, `NEXT_PUBLIC_` 환경변수 |
| `lib/format.ts` | 가격/면적 포맷 유틸 | 순수 함수 |
| `app/providers.tsx` | QueryClient + NuqsAdapter 제공 | `'use client'`, Provider 패턴, lazy init |
| `app/layout.tsx` | HTML 뼈대 + Provider 감싸기 | Server Component, Root Layout |
| `app/page.tsx` | Suspense 래퍼 | Server Component, Suspense 경계 |
| `components/apt-list-page.tsx` | 리스트 페이지 본체 | useQueryState, 조건부 렌더링, 상태 끌어올리기 |
| `hooks/use-regions.ts` | 지역 API 훅 3개 | useQuery, enabled, queryKey |
| `hooks/use-complexes.ts` | 단지 목록 API 훅 | URLSearchParams, PaginatedResponse |
| `components/region-selector.tsx` | 지역 선택 (데스크톱 Select + 모바일 Sheet) | props/콜백, map, 반응형, Fragment |
| `components/complex-card.tsx` | 단지 카드 UI | 조건부 렌더링 (`&&`), toLocaleString |
