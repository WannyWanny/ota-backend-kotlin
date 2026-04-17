# Day 2 Spec — Property Management (Skeleton)

> **상위 Plan**: [`docs/plan.md`](../plan.md) §5 Day 2 (5–6h)
> **스코프**: Property Management Capability를 **Skeleton 티어**로 구현

## 🎯 목표

1. Partner가 Extranet 경유로 숙소(Property)와 객실(Room)을 등록·조회할 수 있다
2. 공통 패키지·레이어 컨벤션이 확정된다 (Controller → UseCase → Repository)
3. 후속 Day(Rate & Inventory, Reservation)가 올라탈 **엔티티 기반**이 놓인다

Skeleton 티어이므로 수정·삭제·이미지 업로드 등은 제외. "등록과 조회가 된다"가 기준.

## 📦 Capability 경계

- **모듈 루트 패키지**: `com.ota.property`
- **내부 레이어**: `api` / `application` / `domain` / `infrastructure`
- **외부 노출**: REST API `/extranet/properties/**`
- **의존 방향**: `api` → `application` → `domain` ← `infrastructure` (포트-아답터)

## 📋 작업 블록

### Block 1 — Liquibase 스키마 (약 1h)

- `src/main/resources/db/changelog/changesets/001-property-room.xml`
  - 테이블 3개: `partner`, `property`, `room`
  - 주요 컬럼:
    - `partner`: id (UUID, PK, default `gen_random_uuid()`), name, email (unique), created_at, updated_at
    - `property`: id, partner_id (FK), name, address, description, created_at, updated_at
    - `room`: id, property_id (FK), name, capacity (INT), base_price_cents (BIGINT), currency (VARCHAR(3)), created_at, updated_at
  - FK: `property.partner_id` → `partner.id`, `room.property_id` → `property.id`
  - Rollback 포함
- `master-changelog.xml`에 `<include file="changesets/001-property-room.xml" ... />` 추가

### Block 2 — 도메인 모델 (약 1h)

- `com.ota.property.domain/Partner.kt` — `data class Partner(val id: UUID, val name: String, val email: String)`
- `com.ota.property.domain/Property.kt` — id, partnerId, name, address, description
- `com.ota.property.domain/Room.kt` — id, propertyId, name, capacity, basePrice (`Money`)
- `com.ota.property.domain/Money.kt` — `data class Money(val amountCents: Long, val currency: String)`
- 도메인 규칙은 최소(이름 공백 거부 수준만)

### Block 3 — 영속 계층 (약 1h)

- `com.ota.property.infrastructure/PartnerEntity.kt` — `@Entity`, `toModel()` / `fromModel()` 쌍
- `com.ota.property.infrastructure/PropertyEntity.kt`
- `com.ota.property.infrastructure/RoomEntity.kt`
- `com.ota.property.infrastructure/PartnerRepository.kt` — `JpaRepository<PartnerEntity, UUID>`
- `com.ota.property.infrastructure/PropertyRepository.kt`
- `com.ota.property.infrastructure/RoomRepository.kt`

### Block 4 — UseCase (약 0.5h)

- `com.ota.property.application/RegisterPropertyUc.kt` — 파트너가 property 등록
- `com.ota.property.application/AddRoomUc.kt` — 기존 property에 room 추가
- `com.ota.property.application/GetPropertyUc.kt` — 조회 (room 포함)

### Block 5 — API 계층 (약 1h)

- `com.ota.property.api/PropertyExtranetController.kt`
  - `POST /extranet/properties` — property 등록
  - `POST /extranet/properties/{propertyId}/rooms` — room 추가
  - `GET /extranet/properties/{propertyId}` — 조회
- `com.ota.property.api.dto/PropertyDtos.kt` — Request/Response records
- Partner 식별: 임시로 `@RequestHeader("X-Partner-Id") partnerId: UUID` 방식 (JWT는 Day 6+)

### Block 6 — 테스트 (약 1h)

- `com.ota.property.domain/PropertyTest.kt` — 도메인 규칙 1~2개 (이름 공백 금지 등)
- `com.ota.property.api/PropertyExtranetControllerIT.kt` — `@SpringBootTest` + `@Testcontainers` + `PostgreSQLContainer` + MockMvc. 시나리오: property 등록 → room 추가 → 조회

### Block 7 — Progress Journal (약 0.5h)

- `docs/progress/day-02.md` — 오늘 수행 내용, 결정, 다음 Day 계획, AI 활용 요약

## ✅ Definition of Done

- [ ] `./gradlew build` 성공 (테스트 포함)
- [ ] `docker compose up -d && ./gradlew bootRun` → `/swagger-ui.html`에서 `POST /extranet/properties` 호출 가능
- [ ] Liquibase 마이그레이션 적용 확인: `psql ... -c "SELECT id, filename FROM databasechangelog ORDER BY orderexecuted"` 에 `001-property-room.xml` 기록
- [ ] Testcontainers 통합 테스트 그린
- [ ] 회사명 누출 스캔 클린
- [ ] Progress Journal 커밋 완료
- [ ] Day 3 스펙 `docs/plan/day-03.md` 작성
- [ ] `docs/resume.md` 7일 체크리스트에서 Day 2 `[x]` 전환

## 🎯 의도적 제외 (scope guard)

- Property 수정/삭제 — 시간되면 Day 6
- 이미지 업로드, 편의시설 태그, 위치 좌표
- Partner 가입/로그인 — Day 1 시드 또는 수동 INSERT로 충분
- 위치 기반 검색 (위도/경도) — Day 6 Search에서 판단

## 💡 의사결정 포인트

- **Partner 인증**: 헤더 방식 유지 → Day 6+에서 JWT로 일괄 교체
- **Location 모델**: 주소 문자열로 시작. PostGIS 도입 여부는 Day 6 Search 시 결정
- **Currency**: VARCHAR(3) ISO 4217 코드 (예: `KRW`, `USD`). 다통화 고려는 Day 3 Rate Plan에서

## ⏱ 예상 시간

| 블록 | 소요 |
|------|------|
| 1. 스키마 | 1.0h |
| 2. 도메인 | 1.0h |
| 3. 영속 | 1.0h |
| 4. UseCase | 0.5h |
| 5. API | 1.0h |
| 6. 테스트 | 1.0h |
| 7. Journal | 0.5h |
| **합계** | **~6.0h** |

## 🧩 커밋 순서 (권장)

1. `feat: add liquibase changeset for partner, property, room tables`
2. `feat: add property domain model and jpa entities`
3. `feat: add property extranet api with registration endpoints`
4. `test: add property integration test with testcontainers`
5. `docs: add day-02 progress journal and day-03 spec`

## 🚨 주의

- `ddl-auto: validate` 고정 — 엔티티 추가 시 반드시 Liquibase changeset 먼저
- Liquibase 순서: `000-baseline` → `001-property-room`
- `Entity` ↔ `Domain Model` 분리 유지 (사용자 팀 tada-ride-service 관례)
- Controller에 비즈니스 로직 금지 (UseCase로 위임)
- 와일드카드 import 금지
- 매 커밋마다 `Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>` 트레일러
