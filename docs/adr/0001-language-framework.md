# ADR-0001: 언어·프레임워크·빌드 도구 선택

- **Status**: Accepted
- **Date**: 2026-04-17
- **Deciders**: wanny

## Context

OTA 플랫폼 백엔드 개발을 위해 언어, 프레임워크, 빌드 도구를 선택해야 한다. 이 선택은 모든 후속 아키텍처, 라이브러리, 개발 워크플로우 결정의 기반이 된다.

요구사항:
- Java 21 이상 또는 Kotlin (선택)
- Spring Boot 3.4 이상
- Gradle (빌드 도구)

개발자의 강점과 기존 경험:
- Kotlin을 주요 언어로 사용
- TADA 팀에서 확립된 Kotlin + Spring Boot 개발 관례 존재
- Spring Boot 3.4 이상의 최신 기능 활용 가능성

## Decision

**Kotlin 2.1.x + Spring Boot 3.4.x + Gradle 8.13 (Kotlin DSL) + JVM 17**

이 조합을 선택한다. 이유:
- Kotlin의 간결성과 null-safety가 복잡한 도메인 로직 표현에 유리
- spring-kotlin 및 spring-jpa 플러그인으로 Spring과의 최적 호환성 보장
- 개발자의 기존 숙련도가 생산성을 극대화
- Gradle Kotlin DSL은 타입 안전성과 IDE 자동완성 제공

## Alternatives Considered

### Option A: Kotlin + Spring Boot 3.4 + Gradle Kotlin DSL (선택)

**장점:**
- 간결한 문법으로 도메인 모델 표현 효율화
- Null-safety와 data class로 DTO/Entity 정의 최소화
- kotlin-spring 플러그인으로 all-open 자동 처리
- 개발자의 기존 경험과 TADA 팀 관례 활용
- Kotlin coroutine 미래 옵션 확보

**단점:**
- Gradle Kotlin DSL이 Groovy 대비 초기 컴파일 느림 (약 5~10초 추가)
- IDE에서 .gradle.kts 인덱싱 시간 소요

**선택 이유:** 생산성과 코드 품질 향상이 빌드 시간 비용을 압도

### Option B: Java 21 + Spring Boot 3.4 + Gradle Groovy

**장점:**
- Virtual Threads를 JVM 21에서 네이티브 지원
- 업계 주류 언어로 팀 협업 용이
- 기존 Spring Boot 예제·문서 풍부

**단점:**
- Java 코드량이 Kotlin 대비 30~40% 증가 (보일러플레이트)
- Null 처리 코드가 복잡 (Optional, @Nullable 체크)
- record + sealed class 지원이 아직 미완성
- 개발자의 경험과 팀 관례가 활용되지 않음

**선택하지 않은 이유:** 코드 길이와 개발 속도 측면에서 Kotlin이 우수

### Option C: Java 21 + Spring Boot 4.x

**장점:**
- Spring 6.x 최신 기능 (native compilation, observability)
- 업계 미래 표준

**단점:**
- 2026년 4월 기준 Spring Boot 4.0은 릴리스 초기 (안정성 미확보)
- 서드파티 라이브러리 호환성 리스크 높음
- 7일 기간 내에 문제 해결 시간 부족

**선택하지 않은 이유:** 개발 기간 내 안정성 보장 불가

## Consequences

### Positive
- Kotlin의 간결성으로 도메인 모델 표현 시간 단축
- Spring Boot 3.4 LTS로 장기 지원 보장 (2026년 이상)
- Gradle Kotlin DSL의 타입 안전성으로 빌드 스크립트 버그 감소
- 팀 관례를 따름으로써 코드 리뷰 및 온보딩 시간 단축

### Negative / Trade-offs
- JVM 17 선택으로 Java 21의 Virtual Threads 데모 불가능
  - 단, 동시성 증명이 데이터베이스 락 및 트랜잭션 중심이므로 영향 미미
  - 상용화 시 JVM 21로 업그레이드 가능
- Gradle Kotlin DSL 초기 컴파일 시간 (약 5~10초)로 개발 반복 주기 약간 증가
  - Build cache 및 daemon 사용으로 완화 가능

### Follow-up / Revisit Triggers
1. **상용화 단계:** JVM 21로 업그레이드하여 Virtual Threads 활용 검토
2. **라이브러리 주요 업데이트:** Spring Boot 4.x 안정화 시 마이그레이션 검토
3. **팀 규모 확대:** Kotlin 경험 부족 인원 합류 시 언어 정책 재검토

## References

- Spring Boot 3.4.x 공식 문서
- Kotlin 2.1.x 릴리스 노트
- TADA 팀 개발 관례 (tada-ride-service)
- ADR-0002 (모듈러 모놀리스 아키텍처)
