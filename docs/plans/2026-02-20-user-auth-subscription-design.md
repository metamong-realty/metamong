# User 인증 + 구독 기능 설계

## 개요

Metamong 서비스에 소셜 로그인 기반 유저 인증과 아파트 거래 구독 기능을 추가한다.

**범위:**
- 소셜 로그인 (카카오, 네이버, 구글) + JWT 인증
- 구독 기능 (단지/지역/조건 3가지 타입, 매매 거래만 1차 구현)
- 구독 매칭 배치 Job

---

## 1. User 도메인

### 1.1 Entity 구조

```
users
├── id (PK, BIGINT, AUTO_INCREMENT)
├── email (VARCHAR(255), UNIQUE, NOT NULL)
├── nickname (VARCHAR(20), NOT NULL)
├── status (ENUM: ACTIVE / INACTIVE / WITHDRAWN)
├── kakao_id (VARCHAR(255), nullable, UNIQUE)
├── naver_id (VARCHAR(255), nullable, UNIQUE)
├── google_id (VARCHAR(255), nullable, UNIQUE)
├── created_at (DATETIME, NOT NULL)
└── updated_at (DATETIME, NOT NULL)
```

### 1.2 가입/로그인 플로우

1. 유저가 소셜 로그인 버튼 클릭
2. 백엔드가 OAuth2 인증 URL로 리다이렉트 (`/oauth2/authorization/{provider}`)
3. 소셜에서 인증 완료 → 백엔드 콜백 URL로 code 전달 (`/login/oauth2/code/{provider}`)
4. 백엔드가 code → access_token 교환 → 유저 정보 조회
5. **provider_id** (kakao_id/naver_id/google_id)로 기존 유저 검색
   - 있으면 → 해당 유저로 로그인
   - 없으면 → **verified email** 기준으로 기존 유저 검색
     - 있으면 → 기존 유저에 provider_id 업데이트 (자동 연동)
     - 없으면 → 신규 유저 생성 (소셜 프로필 이름을 기본 닉네임으로 설정)
6. JWT 토큰 (Access + Refresh) 발급
7. 프론트엔드로 리다이렉트 (토큰 포함)

**계정 연동 규칙:**
- verified email 기준으로만 자동 연동 (보안)
- 소셜 provider가 email_verified = false인 경우 연동하지 않고 별도 계정 생성

---

## 2. 인증 플로우

### 2.1 기술 스택

- Spring Security OAuth2 Client (백엔드 주도)
- JWT (Access Token + Refresh Token)

### 2.2 토큰 관리

| 토큰 | 유효기간 | 저장 위치 | 전달 방식 |
|------|---------|----------|----------|
| Access Token | 1시간 | 클라이언트 | `Authorization: Bearer` 헤더 |
| Refresh Token | 7일 | Redis | `/api/v1/auth/refresh` API |

- 로그아웃: Refresh Token 삭제 + Access Token 블랙리스트 (Redis, TTL = 남은 만료시간)

### 2.3 API 보호

| 경로 | 인증 |
|------|------|
| `/oauth2/**`, `/login/oauth2/**` | 불필요 |
| `/api/v1/public/**` | 불필요 |
| Swagger (`/swagger-ui/**`, `/v3/api-docs/**`) | 불필요 |
| `/api/v1/**` (그 외 전체) | 필요 |

### 2.4 Auth API

```
POST /api/v1/auth/refresh    -- Access Token 갱신
POST /api/v1/auth/logout     -- 로그아웃
GET  /api/v1/auth/me         -- 현재 유저 정보 조회
```

---

## 3. Subscription 도메인

### 3.1 Entity 구조

```
subscriptions
├── id (PK, BIGINT, AUTO_INCREMENT)
├── user_id (BIGINT, FK → users, NOT NULL)
├── type (ENUM: COMPLEX / REGION / CONDITION)
├── trade_type (ENUM: TRADE)              -- 1차는 TRADE(매매)만. 추후 RENT 추가
├── apartment_complex_id (BIGINT, nullable)   -- COMPLEX 타입: 단지 ID
├── region_code (VARCHAR(10), nullable)       -- REGION/CONDITION 타입: 법정동코드
├── area (INT, nullable)                      -- CONDITION 타입: 전용면적 평형 (24, 34 등)
├── min_price (BIGINT, nullable)              -- CONDITION 타입: 최소 가격 (만원)
├── max_price (BIGINT, nullable)              -- CONDITION 타입: 최대 가격 (만원)
├── is_active (BOOLEAN, default true)
├── created_at (DATETIME, NOT NULL)
└── updated_at (DATETIME, NOT NULL)
```

### 3.2 인덱스

```sql
INDEX idx_sub_complex (apartment_complex_id, is_active)  -- COMPLEX 매칭
INDEX idx_sub_region  (region_code, is_active)            -- REGION + CONDITION 매칭
INDEX idx_sub_user    (user_id)                           -- 내 구독 목록 조회
```

- `type` 단독 인덱스 불필요: 카디널리티 낮음 (3개 값), 기존 인덱스가 타입을 암묵적으로 분리
- `region_code + is_active` 인덱스가 REGION과 CONDITION 매칭을 모두 커버

### 3.3 타입별 사용 컬럼

| 컬럼 | COMPLEX | REGION | CONDITION |
|------|---------|--------|-----------|
| apartment_complex_id | O | - | - |
| region_code | - | O | O |
| area | - | - | O (선택) |
| min_price | - | - | O (선택) |
| max_price | - | - | O (선택) |

### 3.4 검증 규칙

- COMPLEX → `apartmentComplexId` 필수, 해당 단지 존재 여부 확인
- REGION → `regionCode` 필수, 유효한 법정동코드인지 확인
- CONDITION → `regionCode` 필수 + 최소 1개 조건 (`area`, `minPrice`, `maxPrice` 중 하나) 필수
- `minPrice` <= `maxPrice` 검증
- `area`는 양수 정수만 허용
- 유저당 구독 상한: 최대 20개

### 3.5 API 설계

```
POST   /api/v1/subscriptions              -- 구독 생성
GET    /api/v1/subscriptions              -- 내 구독 목록 조회
GET    /api/v1/subscriptions/{id}         -- 구독 상세 조회
PUT    /api/v1/subscriptions/{id}         -- 구독 수정
DELETE /api/v1/subscriptions/{id}         -- 구독 삭제
PATCH  /api/v1/subscriptions/{id}/toggle  -- 활성/비활성 토글
```

### 3.6 요청 예시

```json
// COMPLEX 타입
{ "type": "COMPLEX", "tradeType": "TRADE", "apartmentComplexId": 123 }

// REGION 타입
{ "type": "REGION", "tradeType": "TRADE", "regionCode": "1168010100" }

// CONDITION 타입
{ "type": "CONDITION", "tradeType": "TRADE",
  "regionCode": "1168000000", "area": 24, "minPrice": 30000, "maxPrice": 50000 }
```

---

## 4. 구독 매칭 로직

### 4.1 실행 구조

데이터 수집과 매칭은 별도 Job으로 분리한다.

```
Job 1: PublicDataWeeklyCollectionJob (기존)
  └── 거래 데이터 수집/저장

Job 2: SubscriptionMatchingJob (신규)
  └── Step 1: 신규 거래 조회 → 구독 매칭 → 알림 이벤트 저장
```

**분리 이유:**
- 관심사 분리: 수집과 매칭은 별개 책임
- 독립 실행: 수집 실패해도 매칭은 이전 데이터로 실행 가능
- 스케줄 분리: 수집은 주 1회, 매칭은 수집 완료 후 트리거 또는 별도 주기
- 재처리 용이: 매칭만 다시 돌릴 수 있음

### 4.2 매칭 알고리즘

새 거래 1건이 들어올 때:

```
새 거래 (단지: 래미안, 지역: 강남구, 24평, 4.5억)
         │
         ▼
1. COMPLEX 매칭:
   WHERE apartment_complex_id = 래미안ID AND is_active = true

2. REGION 매칭:
   WHERE region_code = 강남구코드 AND type = 'REGION' AND is_active = true

3. CONDITION 매칭:
   WHERE region_code = 강남구코드 AND type = 'CONDITION' AND is_active = true
     AND (area IS NULL OR area = 24)
     AND (min_price IS NULL OR min_price <= 45000)
     AND (max_price IS NULL OR max_price >= 45000)
         │
         ▼
유저별 그룹핑 → 중복 제거 → 알림 이벤트 저장
```

### 4.3 중복 방지

같은 유저에 여러 구독이 매칭되면 **1건만 처리**한다.

**우선순위:** COMPLEX (가장 구체적) > CONDITION (지역+조건) > REGION (지역만)

```kotlin
val matchedSubscriptions: List<Subscription> = findAllMatching(trade)

val perUser: Map<Long, List<Subscription>> = matchedSubscriptions
    .groupBy { it.userId }

perUser.forEach { (userId, subscriptions) ->
    val primary = subscriptions.minBy { it.type.priority }
    createNotificationEvent(userId, trade, primary)
}
```

### 4.4 알림 이벤트 저장

```
notification_events
├── id (PK, BIGINT)
├── user_id (BIGINT, FK → users)
├── subscription_id (BIGINT, FK → subscriptions)
├── trade_id (BIGINT, FK → apartment_trades)
├── status (ENUM: PENDING / SENT / FAILED)
├── created_at
└── updated_at
```

알림 발송 채널 (푸시, 이메일 등)은 추후 별도 설계.

---

## 5. 구현 순서

1. **User 도메인**: Entity, Repository, 기본 CRUD
2. **OAuth2 소셜 로그인**: Spring Security OAuth2 Client + JWT 발급
3. **Auth API**: refresh, logout, me
4. **Subscription 도메인**: Entity, Repository, 검증 로직
5. **Subscription API**: CRUD + toggle
6. **SubscriptionMatchingJob**: 배치 매칭 + 알림 이벤트 저장

---

## 결정 사항 요약

| 항목 | 결정 |
|------|------|
| 소셜 로그인 | 카카오, 네이버, 구글 (3종) |
| OAuth 플로우 | 백엔드 주도 (Spring Security OAuth2 Client) |
| 계정 연동 | verified email 기준 자동 연동 |
| 추가 유저 정보 | 닉네임만 |
| User 테이블 | 단일 테이블 (kakao_id, naver_id, google_id 컬럼) |
| 구독 테이블 | 단일 테이블 + type 컬럼 + nullable 조건 컬럼 |
| 구독 조건 | 지역 + 가격범위(min~max) + 면적(평형 지정) |
| 구독 타입 | COMPLEX / REGION / CONDITION |
| 거래 타입 | 1차: TRADE(매매)만. 추후 RENT 추가 |
| 중복 알림 방지 | 유저별 그룹핑, 우선순위: COMPLEX > CONDITION > REGION |
| 매칭 실행 | 별도 Job (SubscriptionMatchingJob) |
| 알림 발송 | 추후 별도 설계 (이벤트만 저장) |
