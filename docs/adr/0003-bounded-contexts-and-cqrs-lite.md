# ADR-0003: 바운디드 컨텍스트 분할과 CQRS-lite

- **Status**: Accepted
- **Date**: 2026-04-17
- **Deciders**: wanny

## Context

ADR-0002에서 모듈러 모놀리스 아키텍처를 채택했다. 이제 모듈 내에서 바운디드 컨텍스트를 어떻게 분할할지 결정해야 한다.

핵심 문제:
- 평가 기준 A축(쓰기 정합성)과 D축(읽기 성능)은 본질적으로 다른 최적화 경로가 필요하다.
- A축의 최적화 신호(트랜잭션, 잠금, cascade)는 D축의 최적화(조회 캐시, 비정규화, 이벤트 소싱)와 충돌한다.
- 따라서 단일 모듈 내에서 쓰기/읽기 경계를 명확하게 분리하지 않으면 두 축을 동시에 증명할 수 없다.

추가 제약:
- 7개 Capability의 구현 깊이를 3단계(Full, Skeleton, Design-only)로 분류 필요
- 각 단계별로 코드 복잡도와 시간 배분 필요

## Decision

1. **Business Capability 중심 분할:** Actor 중심이 아닌, 비즈니스 기능(무엇을 하는가?)을 기준으로 분할
2. **쓰기/읽기 Capability 분리 (CQRS-lite):** 쓰기 모델과 읽기 모델을 서로 다른 Capability으로 분리
3. **Capability 7개 및 3-tier 배정:**

   | 이름 | Tier | 책임 | 비고 |
   |------|------|------|------|
   | Rate & Inventory | Full | 숙박 이용 가능성, 환율·인벤토리 쓰기 | A축 중심 (트랜잭션) |
   | Reservation | Full | 예약 생성·변경·취소의 원자성 | A축 중심 (잠금 제어) |
   | Pricing | Full (읽기) | 동적 가격 조회, 계산 최적화 | D축 중심 (캐시, 비정규화) |
   | Property | Skeleton | 숙소 정보 조회·기본 CRUD | 조회 특화 |
   | Search | Skeleton | 검색 인덱스, 필터링 쿼리 | D축 중심 |
   | Supplier | Skeleton | 공급자 정보 조회 | 조회 특화 |
   | Operations | Design-only | 운영 대시보드, 모니터링 | 문서만 작성 |

## Alternatives Considered

### Option A: Actor 중심 분할 (Extranet / Customer / Admin / Supplier)

**장점:**
- 요구사항 문구와 1:1 매핑 (직관적)
- API 엔드포인트와 액터 계층이 정렬됨

**단점:**
- 쓰기/읽기 경계와 어긋남
  - 예: Reservation과 Pricing이 모두 "Customer" 컨텍스트에 섞임
  - A축(트랜잭션)과 D축(캐시)이 동일 모듈 안에 혼합됨
- CQRS-lite를 코드 구조에 드러낼 수 없음
- 평가 A·D 축을 분리 증명할 수 없음

**선택하지 않은 이유:** 아키텍처 신호가 불명확하여 A·D 축 동시 증명 불가능

### Option B: Business Capability + CQRS-lite 분리 (선택)

**장점:**
- A축(트랜잭션)의 신호: Reservation, Rate & Inventory 모듈에 귀속
  - `@Transactional(isolation = SERIALIZABLE)`, 낙관적/비관적 잠금, cascade 등이 자연스럽게 드러남
- D축(읽기 성능)의 신호: Pricing, Search, Property 모듈에 귀속
  - 캐시 전략, 읽기 모델 비정규화, 도메인 이벤트 기반 동기화 등이 명확
- Business Capability마다 **모델 · 일관성 전략 · 트랜잭션 경계를 독립적으로 결정**
  - Reservation: Strong consistency (ACID)
  - Pricing/Search: Eventual consistency (도메인 이벤트)
- 7일 기간 내에 구현 가능

**단점:**
- 읽기 모듈(Pricing, Search)이 쓰기 모듈(Reservation, Rate & Inventory)의 도메인 이벤트에 의존
  - 이벤트 유실 또는 지연 시 stale read 발생 가능
  - TTL 기반 eventual consistency 모델로 수용해야 함
- 구현 복잡도 증가: 이벤트 발행·구독, 읽기 모델 동기화

**선택 이유:** A·D 축을 모듈별로 독립적으로 최적화하며 평가 기준 명확 증명 가능

### Option C: Capability 단일 (쓰기/읽기 통합)

**장점:**
- 모델 일관성이 단순하고 명확 (single source of truth)
- 이벤트 동기화 복잡도 없음

**단점:**
- CQRS 의도가 코드 구조에 드러나지 않음
- D축 최적화 신호(캐시, 비정규화)가 A축(트랜잭션)과 혼합됨
- 평가자 입장에서 "읽기 성능 최적화를 어디서 했는가?"를 명확하게 읽을 수 없음

**선택하지 않은 이유:** 아키텍처 신호 약화로 D축 증명 불충분

## Consequences

### Positive
- **A축 명확성:** Reservation, Rate & Inventory에서 ACID 트랜잭션, 잠금, cascade 최적화를 명확하게 증명
- **D축 명확성:** Pricing, Search에서 캐시, 읽기 모델 비정규화, 도메인 이벤트 기반 동기화를 명확하게 증명
- **모듈별 독립 최적화:** 각 Capability이 자신의 일관성 요구사항에 따라 최적 구현 선택
- **확장성:** 추후 Pricing, Search를 독립 마이크로서비스로 분리할 때 이벤트 모델이 이미 자리잡혀 있음
- **코드 가독성:** 패키지 이름만으로도 "이 모듈은 쓰기인가, 읽기인가"를 판단 가능

### Negative / Trade-offs
- **Eventual Consistency 리스크:** Pricing 읽기 모델이 Reservation 쓰기 이벤트 지연 시 최대 몇 초의 stale read 발생
  - 완화: 도메인 이벤트에 타임스탬프 포함, 클라이언트가 캐시 TTL 인식하도록 설계
- **이벤트 동기화 복잡도:** DomainEvent 발행, EventHandler, 재시도 로직, 멱등성 보장 등 추가 구현 필요
- **운영 복잡도:** 읽기 모델 동기화 지연 시 모니터링 및 복구 프로세스 필요
  - 예: Search 인덱스 재구성, Pricing 캐시 무효화

### Follow-up / Revisit Triggers
1. **Pricing 처리량 폭증 (QPS >500):** Pricing을 독립 마이크로서비스로 승격하고 이벤트 스트림(Kafka) 도입
2. **Search 조회 지연 (P99 >1s):** ElasticSearch 같은 전문 검색 엔진 도입 및 Search 서비스 분리
3. **도메인 이벤트 유실 빈도 증가:** Event Sourcing 또는 메시지 큐(RabbitMQ, Kafka)로 마이그레이션
4. **Capability 신규 추가:** Refund, Promotion 등 새 Capability 추가 시 쓰기/읽기 분류 원칙 준용

## References

- ADR-0001 (언어·프레임워크 선택)
- ADR-0002 (모듈러 모놀리스 아키텍처)
- `docs/domain/research.md` §5-6 (Capability 분석)
- Martin Fowler의 "CQRS" 패턴 문서
- Vaughn Vernon "Domain-Driven Design Distilled" Chapter 4-5 (Bounded Contexts)
