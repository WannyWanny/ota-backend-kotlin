# OTA 숙박 플랫폼 도메인 리서치

## 1. 개요

Online Travel Agency(OTA)는 고객이 숙박 시설을 검색, 비교, 예약할 수 있는 온라인 플랫폼이다. 본 프로젝트에서 다루는 OTA 모델은 다음 두 가지 공급 채널을 통합한다:

1. **직계약 모델 (Direct Contracts)**: 파트너(숙소 운영자)와 직접 계약하여 요금, 재고, 정책을 관리하고 노출
2. **외부 공급사 통합 (Supplier Integration)**: 국내외 숙박 공급사(GDS, 호텔 체인 등)의 상품을 API 또는 XML 피드를 통해 수집하여 통합 검색 결과에 제공

이러한 이원적 모델은 공급량 확대와 파트너 수익 보호를 동시에 달성하는 전략적 선택이다. 플랫폼의 핵심 가치는 고객에게는 폭넓은 선택지를, 파트너에게는 공정한 수익 분배를 제공하는 것이다.

## 2. 이해관계자 (Stakeholders)

| 이해관계자 | 역할 | 주요 관심사 | 주 시스템 |
|-----------|------|-----------|---------|
| **Partner (파트너)** | 숙소 운영자: 호텔, 펜션, 게스트하우스 등 | 예약 수 증대, 수수료 최소화, 즉각적 정산 | Extranet (관리 웹사이트) |
| **Customer (고객)** | 최종 숙박 이용자 | 가격 비교, 쉬운 예약, 신뢰성 있는 취소 정책 | Mobile App, Web Portal |
| **Operator (운영자)** | 내부 운영 담당자 | 분쟁 해결, 통계 분석, 파트너 관리, 요금 검증 | Admin Dashboard |
| **Supplier (공급사)** | 외부 숙박 공급사, GDS 제공업체 | 효율적 재고 동기화, API 가용성 | B2B API |

각 이해관계자는 고유한 권한 모델과 데이터 접근 정책을 갖는다. 이는 바운디드 컨텍스트 설계의 핵심 고려 사항이다.

## 3. 핵심 도메인 개념 (Core Concepts)

### Property (숙소)
숙박 시설의 기본 단위. 이름, 위치(위도/경도), 주소, 시설 정보(Wi-Fi, 주차, 반려동물 정책 등)를 포함. 각 Property는 Partner 또는 Supplier에 속하며, Property 수정 권한은 소유자에게만 있다.

### Room Type (객실 타입)
같은 Property 내의 객실 분류(예: Single, Double Deluxe, Suite). Rate Plan과 Inventory는 이 단위로 관리되며, 하나의 Property는 여러 Room Type을 가질 수 있다.

### Rate Plan (요금 플랜)
객실 타입 + 숙박 규칙(체크인/체크아웃, 최소 숙박일 등)의 조합에 대한 요금 전략. 예: "Non-Refundable Advance", "Free Cancellation Until 48h Before", "Flexible" 등. 각 Rate Plan은 취소 정책과 조건을 포함한다.

### Inventory (재고)
특정 날짜의 특정 Room Type에 대한 예약 가능 객실 수. 단위: (Property, Room Type, Check-In Date, Rate Plan) = Inventory Count. 재고 업데이트는 Partner 권한이며, 예약 발생 시 실시간 감소.

### Availability (가용성)
Inventory > 0이고 업체의 운영 상태가 정상인 상태. 검색 응답에서 가용성 있음으로 표시되는 Inventory.

### Rate Calendar (요금 캘린더)
날짜별 (Room Type, Rate Plan) 조합에 대한 가격과 재고를 일괄 관리하는 데이터 구조. 예를 들어, 2025-05-01부터 2025-05-07까지 Double Room의 "Non-Refundable" 플랜을 가격 120,000 KRW, 재고 5개로 설정할 수 있다. Rate Calendar는 Partner가 직접 입력하거나 Supplier 피드에서 자동 갱신된다.

### Reservation (예약)
고객이 특정 숙소, 객실 타입, 요금 플랜, 숙박 날짜에 대해 예약한 기록. 상태 머신: PENDING(예약 생성, 결제 대기) → CONFIRMED(결제 완료) → CANCELLED(고객 취소) | COMPLETED(숙박 종료). 각 예약은 예약 시점의 취소 정책, 가격 등을 스냅샷으로 저장.

### Cancellation Policy (취소 정책)
요금 플랜에 종속된 환불 규칙. 예: "Check-in 72시간 전까지 100% 환불, 이후 수수료 20% 공제". 예약 시점에 정책을 스냅샷하여 이후 취소 시에는 정책 변경과 무관하게 적용.

### Search Criteria (검색 조건)
고객이 제시하는 검색 질의: 위치(도시 또는 좌표), Check-in Date, Check-out Date, 인원(성인/어린이), 객실 수. 이 조건으로 검색 인덱스 또는 쿼리를 통해 예약 가능한 Property List + Price를 반환.

### Pricing Query (요금 조회)
특정 숙소의 특정 날짜 범위, 객실 타입, 인원에 대한 가격과 재고를 조회하는 연산. 대규모 동시 요청이 발생할 수 있는 성능 핵심 지점이며, 캐싱과 비동기 처리가 필수.

### Supplier Product (공급사 상품)
외부 Supplier에서 제공하는 Property + Room Type + Rate Plan의 조합. 직계약 상품과 다르게, Supplier의 API 응답을 변환하여 통합 검색 결과에 포함. 재고와 가격은 공급사 피드의 동기화 주기에 따라 갱신.

## 4. 핵심 비즈니스 규칙 (Business Rules)

- **동시성 제어**: 동일 재고 단위(Room Type × Date)에 대한 중복 예약은 불가능. 예약 생성 시 Optimistic Lock 또는 SELECT FOR UPDATE를 통해 보장.

- **권한 분리**: 요금·재고 수정은 Property Owner(Partner) 또는 Supplier API만 가능. 고객은 조회만.

- **정책 스냅샷**: Reservation 생성 시, 당시의 Rate Plan 취소 정책을 record에 저장. 이후 정책 변경은 신규 예약에만 영향.

- **Supplier 공존**: 동일 검색 결과에 직계약 상품(Partner Property)과 외부 Supplier 상품이 함께 노출. 가격 비교 유연성 제공.

- **통화 및 시간대**: 모든 가격은 통화(KRW, USD 등) 포함 저장. 날짜/시간은 UTC 기반으로 저장하고, 클라이언트 타임존 변환은 응답 시점에 수행.

- **재고 오버부킹 금지 (기본값)**: 특정 Room × Date의 Inventory 초과 예약 방지. Rate Plan별 "오버부킹 허용" 옵션은 향후 확장 포인트로 설계.

- **취소 수수료 계산**: Cancellation Policy에 명시된 규칙에 따라 자동 계산. 예: "Check-in D-2 이전: 100% 환불, 이후: 50% 환불". 규칙 변경 시 기존 예약에 영향 없음.

- **검색 성능**: 대규모 동시 검색 요청(초당 수천 건)을 1-2초 내에 처리해야 함. 읽기 최적화(캐싱, 인덱싱, CQRS-lite)는 필수.

## 5. 바운디드 컨텍스트 후보 (Bounded Context Candidates)

### 초기 후보 브레인스토밍
Partner Management, Property, Room, Inventory, Rate, Pricing, Search, Reservation, Payment, Supplier Integration, Admin, Identity/Auth, Notification, Analytics, Reporting.

### 분할 기준 비교

| 기준 | 설명 | 적합도 | 비고 |
|-----|------|--------|------|
| **Actor** | 각 사용자 역할별로 컨텍스트 분리 | 중간 | Partner, Customer, Operator, Supplier 기준이나, 데이터 흐름 겹침 |
| **Business Capability** | 비즈니스 기능(검색, 예약, 관리)별로 분리 | 높음 | 명확한 경계, 책임 할당 용이, 팀 조직과 일치 |
| **Data Flow** | 데이터 읽기/쓰기 분리 | 높음 | 성능 최적화(CQRS), Write Model과 Read Model 독립 |

### 채택 방향
**Business Capability 기준 주 분리 + Data Flow 기반 보조 분리(CQRS-lite)**. 이는 마이크로서비스로의 미래 전환을 고려하면서도 모놀리식 아키텍처의 응집력을 유지한다.

## 6. 채택된 Capability 맵

| # | Capability | 측면 | 티어 | 주요 책임 |
|---|-----------|------|------|---------|
| 1 | **Property Management** | Write | Skeleton | 파트너의 숙소, 객실 타입, 시설 정보 등록·수정. Supplier 상품 메타데이터 관리. |
| 2 | **Rate & Inventory Mgmt** | Write | Full | Rate Calendar 생성, 요금·재고 일괄 업데이트. Supplier 피드 동기화. |
| 3 | **Reservation** | Write | Full (A축: 동시성) | 예약 생성, 취소, 상태 전이. 동시성 제어 핵심 지점. 중복 예약 방지. |
| 4 | **Search & Discovery** | Read | Skeleton | 검색 조건 입력 수용, 기본 필터링. 상세 조회는 Pricing & Availability에서 담당. |
| 5 | **Pricing & Availability Query** | Read | Full (D축: 성능) | 요금·재고 조회 대규모 동시 요청 처리. 캐싱, 읽기 최적화, 성능 A축. |
| 6 | **Supplier Integration** | Integration | Skeleton | 외부 공급사 API 수집, 데이터 변환, 통합 검색 제공. Anti-Corruption Layer 포함. |
| 7 | **Operations (Admin)** | Cross | Design-Only | 운영자 대시보드, 분쟁 처리, 통계 조회. 향후 상세 설계. |

**티어 정의**: Full(완전 구현), Skeleton(기본 구조), Design-Only(설계 단계).

## 7. Ubiquitous Language

| 한국어 | 영문 | 정의 |
|--------|------|------|
| 숙소 | Property | 호텔, 펜션, 게스트하우스 등 숙박 시설의 기본 단위. |
| 객실 타입 | Room Type | 숙소 내 객실의 분류(Single, Double, Suite 등). |
| 요금 플랜 | Rate Plan | 객실 타입에 대한 요금 전략 및 예약 규칙(체크인/아웃, 취소 정책 등). |
| 재고 | Inventory | 특정 날짜의 특정 객실 타입에 대한 예약 가능 객실 수. |
| 가용성 | Availability | Inventory > 0이고 업체 운영 상태가 정상인 상태. |
| 요금 캘린더 | Rate Calendar | 날짜별 (Room Type, Rate Plan) 조합의 가격·재고를 관리하는 구조. |
| 예약 | Reservation | 고객의 숙박 예약 기록. 상태: PENDING → CONFIRMED → CANCELLED\|COMPLETED. |
| 취소 정책 | Cancellation Policy | 환불 규칙. Rate Plan에 종속. 예약 시점에 스냅샷 저장. |
| 검색 조건 | Search Criteria | 고객의 검색 질의(위치, 체크인/아웃, 인원). |
| 요금 조회 | Pricing Query | 특정 숙소·날짜·인원에 대한 가격·재고 조회 연산. 성능 핵심 지점. |
| 파트너 | Partner | 직계약 숙소 운영자. 파트너 포털에서 요금·재고 관리. |
| 외부 공급사 | Supplier | GDS, 호텔 체인 등 외부 숙박 상품 제공자. API 또는 피드 연동. |
| 파트너 포털 | Extranet | Partner가 숙소를 관리하는 관리 웹사이트. |
| 오버부킹 | Overbooking | 재고 초과 예약. 기본적으로 금지. 정책별 예외 허용 가능. |
| 멱등성 | Idempotency | 동일 요청의 반복 실행이 부작용 없음. 예약 생성의 중복 방지 메커니즘. |
| 동기화 | Synchronization | Supplier 피드의 정기적 갱신으로 외부 요금·재고를 플랫폼에 반영. |
| 스냅샷 | Snapshot | 예약 생성 시점의 가격·정책을 record에 저장하여 이후 변경과 격리. |
| 안티 부패 레이어 | Anti-Corruption Layer | Supplier API의 이질적 데이터 형식을 내부 도메인 모델로 변환. |
| 바운디드 컨텍스트 | Bounded Context | 명확한 경계를 갖는 도메인 서브시스템. 유비쿼터스 언어 일관성 영역. |
| 읽기/쓰기 분리 | CQRS-lite | 읽기 모델과 쓰기 모델 분리로 성능 최적화. 작은 규모에서는 경량 CQRS 패턴 적용. |

## 8. 설계 의사결정 연결

본 리서치의 결과는 다음과 같이 아키텍처 선택으로 이어진다:

### 모듈러 모놀리스 선택 이유
- **마이크로서비스 대비 장점**: 배포 복잡성 낮음, 트랜잭션 관리 간단, 로컬 개발 용이.
- **확장성**: Business Capability 기준 분리로 향후 마이크로서비스 전환 가능.
- **초기 단계 최적화**: 7일 그린필드 프로젝트에서 팀 생산성과 안정성 우선.

### Business Capability 기준 분리 이유
- **책임 명확화**: 각 기능의 소유팀 할당이 용이.
- **도메인 모델 일관성**: 유비쿼터스 언어를 컨텍스트별로 관리.
- **확장성**: 새로운 기능(결제, 리뷰 등) 추가 시 새 컨텍스트로 구성 가능.

### 쓰기/읽기 분리(CQRS-lite) 이유
- **성능 최적화**: Pricing Query(읽기)와 Rate Update(쓰기)의 요구사항이 완전히 다름.
  - 읽기: 초당 수천 건, 1-2초 응답, 캐싱 적극 활용.
  - 쓰기: 일일 수십-수백 건, 정확성 우선.
- **캐싱 전략**: 읽기 모델은 Redis 캐싱, 쓰기 모델은 DB 일관성 보장.

### A축(동시성) & D축(성능) 집중 이유
- **A축(Reservation)**: 동시에 같은 객실을 예약하려는 고객 충돌 해결. Optimistic Lock / SELECT FOR UPDATE.
- **D축(Pricing Query)**: 검색 시 예약 시 예약 생성 요청이 폭증하는 시점의 성능 보장. 비동기 캐시 갱신, 읽기 복제본.

### Supplier Integration 수준
- **Tier: Skeleton** → 초기에는 기본 피드 수집만. 향후 실시간 동기화, 가격 동적 조정 등으로 확장.
- **Anti-Corruption Layer**: 공급사의 이질적 데이터 형식(XML, JSON, 비표준 필드)을 내부 도메인 모델로 변환하여 순수성 유지.

## 9. 참고 및 리서치 출처

### 참고 자료
- **업계 표준**: OpenTravel Alliance(OTA) 명세, HSMAI(Hospitality Sales & Marketing Association) 인더스트리 가이드. 이들은 Rate Plan, Inventory, Cancellation Policy 등 표준 용어와 개념을 정의.
- **공개 엔지니어링 블로그**: Booking.com, Expedia 등 대규모 OTA의 기술 블로그에서 발표한 스케일링 전략, 동시성 제어, 검색 인덱싱 사례 참고.
- **도메인 주도 설계(DDD)**: Evans, Eric의 "Domain-Driven Design"(2003)과 Newman, Sam의 "Building Microservices"(2015) 등 이론 기반.

### 작성 방식
본 리서치는 OTA 업계의 일반 공개 지식과 도메인 모델링 원칙을 바탕으로 작성되었으며, LLM 기반 분석을 활용하였다. 특정 상용 제품 구현의 상세나 비공개 자료는 포함하지 않으며, 모두 공개된 엔지니어링 관례와 학술 이론에 근거한다.

### 향후 리서치 범위
- Supplier 다양성(GDS, 호텔 체인, 개인 게스트하우스)에 따른 데이터 변환 전략 상세화.
- 국가별 세금, 규제(숙세, 개인정보보호)에 따른 결제 및 정산 로직 설계.
- 다국어, 다중 통화 지원에 따른 가격 표시 및 환율 관리 전략.
