# OTA 숙박 플랫폼 백엔드 — 실행 계획

## Context

숙박 플랫폼 백엔드 시스템을 **7일 내 설계·구현**. 목표는 **상위 10% 품질 진입**.
이 문서는 L0 → L10의 **Top-down 합의를 통해 누적 확정된 전체 실행 계획**이다. 모든 레이어의 결정 근거는 섹션 2의 테이블에, 실행 흐름은 섹션 5~9에 담겨 있다.

---

## 1. "상위 10%"의 정의 (합의된 메타 전략)

요구사항 문서가 반복해서 강조하는 평가 신호:
- "완벽한 구현보다 **설계와 사고 과정**이 더 중요"
- "**선택과 집중**을 보여 주세요"
- "설계 문서에 적은 내용이 **실제 코드에 반영**되어야 합니다"
- "AI 활용은 **얼마나 효과적으로 활용했는가**"

**→ 분량이 아니라 의사결정의 깊이 + 증거로 차별화한다.**
- 많이 구현하는 평범한 후보와 경쟁하지 않는다.
- **좁은 영역을 깊게 파고**, 그 깊이를 문서·코드·테스트로 동시에 증명한다.

---

## 2. 확정된 기본 선택

| 항목 | 결정 | 근거 |
|------|------|------|
| 투입 시간 | 30–50h (Medium) | 사용자 확정 |
| 언어 | **Kotlin** | 사용자 확정 |
| 평가자 가정 | 시니어 백엔드 + 아키텍트 혼합 | 정보 없음 → 균형 포지셔닝 |
| 베팅 축 | **A. 동시성 제어 + C. 도메인 설계(DDD) + D. 요금 조회 성능** | 사용자 확정 |
| 아키텍처(L2) | **모듈러 모놀리스 (단일 Gradle 모듈 + 패키지로 바운디드 컨텍스트 분리)** | 7일 스케일과 C축(DDD) 증명 비용의 균형점 |
| 분할 원칙(L3) | **Business Capability 중심** (예: 예약/가격/재고/검색 같은 비즈니스 역량 단위) | 쓰기/읽기 관심사가 자연 분리되어 A·D 축 증명에 최적. Actor 분리는 Controller 레이어(URL 네임스페이스)로 내려보냄 |
| 깊이 티어(L4) | **3-tier: Full / Skeleton / Design-only** | A·D 증명 영역은 Full+테스트, 보조 영역은 Skeleton(엔티티+최소 API), 주변 영역은 설계 문서만. "선택과 집중"을 평가자가 한눈에 읽을 수 있는 구조 |
| 쓰기/읽기 경계(L5) | **분리 (CQRS-lite)** — 쓰기 Capability 와 읽기 Capability 를 다른 모듈로 | A·D 축이 모듈 경계 자체에서 드러남. 모델·트랜잭션 경계·일관성 모델이 모듈별로 독립 선택 가능 |
| Capability 스코프(L6) | **7개**: Property · Rate&Inventory · Reservation · Search · Pricing · Supplier · Operations | 필수 6개 + 가산점 핵심 1개(Admin). Identity는 cross-cutting 인프라로 처리 |
| 티어 배정(L7) | **Full**: Rate&Inventory · Reservation · Pricing / **Skeleton**: Property · Search · Supplier / **Design-only**: Operations | A·D 축 증명 + 그 데이터 원천까지 Full로 집중. "왜 이것만 Full인가"에 일관된 답 가능 |
| 기술 스택(L8) | PostgreSQL · Liquibase · Spring Data JPA · **Caffeine(L1) + Redis(L2)** · Spring local events · springdoc · k6 · Testcontainers · JWT · Docker Compose | 2계층 캐시로 D축 스토리 풍부. Kafka는 오버킬이라 설계만 커버 |
| 일자별 순서(L9) | Day1 스캐폴드 → Day2 Property → Day3 Rate&Inventory → **Day4 Reservation(A축)** → **Day5 Pricing(D축, Must-have 체크포인트)** → Day6 Search+Supplier → Day7 Admin+마감 | 가장 어려운 작업(Reservation)을 Day4에 배치. Day5 종료 시점에 A·D 축 증명 완료 |
| docs/ 구조(L10) | **완전한 증거 자산**: 8 ADR + 5 C4 + 4 domain + perf + progress + ai-usage | ADR 1개당 15–30분 투자로 "체계적 의사결정" 시그널 최대화. C4 Level 3는 Reservation/Pricing만 |

### 3축 베팅의 일관성 (중요한 재해석)
3개 축은 분산이 아니라 **하나의 내러티브**로 꿰어진다:
- **C (DDD)** = 뼈대. 쓰기·읽기 관심사 분리의 논리적 근거 제공
- **A (동시성)** = 쓰기 측 증명
- **D (성능)** = 읽기 측 증명

→ 스토리: *"도메인 분리를 통해 쓰기는 정합성, 읽기는 처리량을 독립적으로 최적화한다."*
이 한 줄이 모든 세부 결정의 기준이 된다.

---

## 3. Top-down 의사결정 순서 (전부 확정)

Layer마다 하나씩 합의하며 누적된 결정 트리. 위 결정이 아래를 제약하므로, 역순으로 수정하면 재작업이 생긴다.

```
L0 ─ 메타 전략                              [✅ 확정: 깊이 + 증거]
 └ L1 ─ 기본 스택/시간/베팅 축              [✅ 확정]
     └ L2 ─ 전체 아키텍처 방향성             [✅ 확정: 모듈러 모놀리스]
         └ L3 ─ 모듈 분리 기준(분할 원칙)    [✅ 확정: Business Capability]
            └ L4 ─ 깊이 티어 구조            [✅ 확정: 3-tier]
               └ L5 ─ 쓰기/읽기 경계 원칙    [✅ 확정: CQRS-lite 분리]
                  └ L6 ─ Capability 후보 식별[✅ 확정: 7개]
                     └ L7 ─ Capability × 티어 배정 [✅ 확정]
                        └ L8 ─ 기술 요소 선택 [✅ 확정]
                           └ L9 ─ 일자별 백로그[✅ 확정]
                              └ L10 ─ 증거 자산[✅ 확정]

모든 Layer 확정 — 실행 준비 완료.
```

### 이 순서의 의도
- **위 결정이 아래를 제약**한다. 뒤집으면 재작업이 생긴다.
- 예: "모듈러 모놀리스"로 정해야 "바운디드 컨텍스트 경계"가 물리적으로 의미를 가진다. 순서가 뒤집히면 도메인 그렸다가 모듈 구조와 안 맞아서 다시 그리게 된다.
- 지금은 도메인(L3)이나 라이브러리(L5) 같은 걸 먼저 정하지 않는다.

---

## 4. 확정된 docs/ 증거 자산 구조 (L10)

```
docs/
├─ README.md                          # 문서 탐색 가이드 (채점자 진입점)
├─ adr/                               # 8개 ADR — 의사결정 흔적
│  ├─ 0001-language-framework.md
│  ├─ 0002-modular-monolith.md
│  ├─ 0003-bounded-contexts.md        # Capability 분할 + CQRS-lite 근거
│  ├─ 0004-reservation-concurrency.md # 🎯 A축 핵심
│  ├─ 0005-pricing-cache-strategy.md  # 🎯 D축 핵심
│  ├─ 0006-supplier-integration-acl.md
│  ├─ 0007-event-driven-design.md
│  └─ 0008-testing-strategy.md
├─ architecture/
│  ├─ c4-context.md                   # C4 Level 1
│  ├─ c4-container.md                 # C4 Level 2
│  ├─ c4-component-reservation.md     # C4 Level 3 (Reservation만)
│  ├─ c4-component-pricing.md         # C4 Level 3 (Pricing만)
│  └─ sequence-reservation.md         # 동시성 시퀀스
├─ domain/
│  ├─ research.md                     # OTA 도메인 리서치 (Day 1)
│  ├─ capability-map.md               # 7 Capability 관계 + 쓰기/읽기 경계
│  ├─ erd.md                          # ERD 이미지 + 설명
│  └─ ubiquitous-language.md
├─ api/
│  └─ overview.md                     # Swagger 링크 + 주요 플로우 설명
├─ perf/
│  ├─ rate-query-report.md            # k6 결과 + 그래프 (D축 증명)
│  └─ scripts/                        # k6 스크립트
├─ progress/
│  └─ day-01.md ~ day-07.md           # Progress Journal
└─ ai-usage/
   ├─ log.md                          # 프롬프트·수용·기각 이력
   └─ reflection.md                   # AI 활용 회고
```

**ADR 템플릿**: `Context / Decision / Alternatives Considered / Consequences / Status`

---

## 5. 7일 백로그 (요약)

| Day | 시간 | 핵심 작업 |
|-----|------|-----------|
| 1 ✅ | 4–5h (완료 2026-04-17 · [progress](./progress/day-01.md)) | 도메인 리서치 · 스캐폴드 · Docker Compose · Liquibase · ADR-0001~0003 초안 · C4 L1 |
| 2 | 5–6h | **Property (Skeleton)** · 공통 패턴 확정 · [spec](./plan/day-02.md) |
| 3 | 7–8h | **Rate & Inventory (Full)** · Rate Calendar · 이벤트 발행 골격 |
| 4 | 9–10h | **Reservation (Full, A축)** · 락 전략 비교 · 동시성 테스트 · ADR-0004 |
| 5 | 7–8h | **Pricing (Full, D축)** · Caffeine+Redis · Projection · k6 1차 · ADR-0005 · **체크포인트** |
| 6 | 6–7h | Search + Supplier (Skeleton) · k6 2차 · 이벤트 설계 ADR |
| 7 | 4–5h | Operations (Design) · 전체 문서 마감 · 최종 검증 |

**총**: ~42–49h.

---

## 6. 채점자 순회 동선 (검증 체크리스트)

1. `README.md` → 한 줄 실행 가이드 + docs 네비
2. `docs/domain/research.md` → 문제 인식
3. `docs/adr/0003-bounded-contexts.md` → 도메인 분리 근거
4. `docs/adr/0004-reservation-concurrency.md` + 테스트 코드 → **A축 증명**
5. `docs/adr/0005-pricing-cache-strategy.md` + `docs/perf/rate-query-report.md` → **D축 증명**
6. `docs/progress/*.md` + `docs/ai-usage/log.md` → 과정의 품질

---

## 7. 검증 방법 (엔드투엔드)

```bash
# 저장소 클론 후 한 줄 실행 가능해야 함
docker compose up -d
./gradlew bootRun
# → http://localhost:8080/swagger-ui.html 접속 가능

# 전체 테스트
./gradlew test

# A축 증명 — 동시성 통합 테스트
./gradlew test --tests "*ReservationConcurrencyIntegrationTest"
# 기대: 100 동시 요청, 재고 1일 때 오직 1건만 성공

# D축 증명 — k6 부하 테스트
k6 run docs/perf/scripts/rate-query.js
# 결과가 docs/perf/rate-query-report.md 의 그래프와 일치해야 함

# 코드 스타일
./gradlew ktlintCheck
```

**제출 전 최종 체크**:
- [ ] 저장소명/코드/문서에 특정 기업명 없음
- [ ] 프로젝트 성격을 드러내는 표현 없음 (검사어 목록은 `docs/resume.md` 참조)
- [ ] README에 한 줄 실행 가이드 존재
- [ ] 모든 ADR 상태가 `Accepted`
- [ ] Progress Journal Day 1–7 전부 작성
- [ ] AI 활용 로그 존재
- [ ] Swagger UI 정상 동작

---

## 8. 리스크 가드레일

| 리스크 | 완화책 |
|--------|--------|
| 3축 동시 추구로 깊이 희석 | C축(DDD)은 A·D의 **골조**로 소비되어 추가 시간 최소화. A·D만 깊이 파면 됨 |
| Day 4/5 Reservation·Pricing이 밀림 | Day 5 체크포인트에서 판단 → Day 6–7을 문서·설계로 전환, Supplier → Design-only로 강등 |
| Liquibase 실수로 스키마 깨짐 | `ddl-auto: validate` 고정 + Testcontainers로 마이그레이션 자체 테스트 |
| Redis 의존 증가 | Docker Compose로 개발·채점 환경 동일화, Redis 장애 시 Caffeine만으로도 동작하는 fallback 설정 |
| AI 활용 로그 공백 | 작업 시작·종료 시 30초 기록 루틴 (`docs/ai-usage/log.md`에 즉시 추가) |
| 기업명·프로젝트 성격 누출 | `docs/resume.md` 의 안전 체크 grep 레시피를 pre-commit hook에 등록 |

---

## 9. 실행 진입점

플랜 승인 시 즉시 **Day 1** 착수:
1. GitHub Public Repo 생성 (이름 예: `ota-backend-kotlin`, 특정 기업명 배제)
2. 저장소 초기화 + 이 파일을 `docs/plan.md`로 복사
3. Gradle KTS 스캐폴드 + Spring Boot 3.4 + Kotlin 2.x
4. `docker-compose.yml` (PostgreSQL 16 + Redis 7)
5. Liquibase 초기 `master-changelog.xml`
6. 패키지 구조(`com.ota.{capability}.(api|application|domain|infrastructure)`)
7. ADR-0001~0003 초안 + C4 Level 1 작성
8. `docs/domain/research.md` 작성 시작
