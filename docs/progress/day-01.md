# Day 1 — 스캐폴드와 아키텍처 골격 수립

- **날짜**: 2026-04-17
- **투입 시간**: 약 4–5h
- **목표**: 실행 가능한 "Hello World" + 아키텍처 골격 + 주요 ADR 초안

## 수행 내용

1. GitHub Public 저장소 생성 (`ota-backend-kotlin`)
2. 초기 파일(README, .gitignore, LICENSE) + 첫 커밋
3. Gradle Kotlin DSL 스캐폴드 — Kotlin 2.1.20, Spring Boot 3.4.4, JVM 17
4. Spring Boot 부트스트랩 — `OtaApplication.kt` + `application.yml` (ddl-auto=validate)
5. Docker Compose — PostgreSQL 16 + Redis 7 with healthcheck
6. Liquibase 초기화 — `master-changelog.xml` + `000-baseline.xml` (pgcrypto 확장)
7. OTA 도메인 리서치 문서 작성 — `docs/domain/research.md`
8. ADR 0001–0003 작성 (언어/아키텍처/컨텍스트)
9. C4 Level 1 다이어그램 (이 커밋 포함)

## 주요 의사결정

- **Kotlin + JVM 17**: 본인 숙련도 + 프로젝트 요구사항 충족. Virtual Threads 불필요 (DB 락 중심 증명).
- **모듈러 모놀리스**: 바운디드 컨텍스트 경계를 패키지로 표현, ArchUnit으로 의존 방향 검증 예정.
- **Business Capability 기준 분할 + CQRS-lite**: 쓰기·읽기 Capability 분리로 A축(Reservation)과 D축(Pricing)이 독립 최적화.
- **ddl-auto=validate + Liquibase**: 스키마는 Liquibase가 단일 소스. Hibernate는 검증만.

## 기술적 이슈

- 없음. Day 1은 갈등 지점 없이 진행.

## 내일(Day 2) 계획

- **Property Management (Skeleton)** 구현
- Partner, Property, Room 엔티티 + Extranet 등록 API 최소 버전
- 공통 패키지 컨벤션 확정 (Controller → UseCase → Repository)
- Liquibase changeset 추가 (property, room 테이블)

## 참고 자료

- ADR 0001: `docs/adr/0001-language-framework.md`
- ADR 0002: `docs/adr/0002-modular-monolith.md`
- ADR 0003: `docs/adr/0003-bounded-contexts-and-cqrs-lite.md`
- 도메인 리서치: `docs/domain/research.md`

## AI 활용 요약

Day 1은 설계·문서 위주. AI는 설계 토론 / ADR 초안 / 스캐폴드 파일 작성에 활용.

구체적 프롬프트·수용·기각 이력은 향후 `docs/ai-usage/log.md`에 누적 기록 예정 (Day 2 이후 템플릿 채움).
