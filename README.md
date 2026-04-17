# OTA Accommodation Backend

Backend service for an online travel agency (OTA) accommodation platform — covering property onboarding, rate and inventory management, search, reservation, and external supplier integration.

## Tech Stack

- **Kotlin** 2.x + **Spring Boot** 3.4.x
- **PostgreSQL** 16, **Redis** 7
- **Gradle** (Kotlin DSL)
- **Liquibase** for schema migration
- **Caffeine** (L1) + **Redis** (L2) two-tier cache
- **Testcontainers** + JUnit 5 for integration testing
- **k6** for load testing
- **springdoc-openapi** for API docs

## Quick Start

```bash
docker compose up -d
./gradlew bootRun
```

After startup, visit http://localhost:8080/swagger-ui.html for API documentation.

## Documentation

See [`docs/`](./docs/) for:

- [Architecture Decision Records](./docs/adr/) (ADR)
- [C4 Architecture Diagrams](./docs/architecture/)
- [Domain Research & Bounded Contexts](./docs/domain/)
- [Performance Reports](./docs/perf/)
- [Progress Journal](./docs/progress/)
- [AI Usage Log](./docs/ai-usage/)

## License

[MIT](./LICENSE)
