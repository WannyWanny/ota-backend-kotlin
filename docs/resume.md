# Resume Guide — 세션 재개 치트시트

> **이 파일을 먼저 읽으세요.** 지금 프로젝트가 어디까지 왔고 다음에 뭘 해야 할지 한눈에 파악할 수 있습니다.

## 📍 현재 상태

| 지표 | 값 |
|------|---|
| 최근 완료 Day | **Day 1** (2026-04-17) |
| 다음 예정 Day | Day 2 — Property Management (Skeleton) |
| 저장소 | https://github.com/WannyWanny/ota-backend-kotlin |
| 로컬 경로 | `/Users/wanny/Repository/ota-backend-kotlin` |
| 브랜치 | `main` (origin 동기화됨) |

## 📚 필독 순서 (새 세션 진입 시)

1. **이 파일** (`docs/resume.md`) — 현재 위치
2. **`docs/plan.md`** §2~§5 — 전체 L0~L10 의사결정 요약 + 7일 백로그
3. **`docs/progress/day-NN.md` (최신)** — 가장 최근 Day 무엇을 했는지
4. **`docs/plan/day-NN.md` (다음)** — 지금 해야 할 구체 스펙 (없으면 작성부터)

## 🗣️ 세션 재개 한 줄 프롬프트

새 Claude Code 세션을 `ota-backend-kotlin` 디렉터리에서 시작한 뒤:

> "`docs/resume.md` 읽고 Day N 스펙대로 이어서 시작해줘"

Claude가 자동으로 현재 상태 → 다음 Day 스펙 → 실행 흐름으로 이어간다.

## 🎯 이 프로젝트의 북극성 (5초 요약)

> **"모듈러 모놀리스 + CQRS-lite로 쓰기 정합성(A축)과 읽기 성능(D축)을 독립 최적화"**

- 베팅 축: **A** (Reservation 동시성) + **C** (DDD 골조) + **D** (Pricing 성능)
- 7 Capability × 3-tier: Full 3 / Skeleton 3 / Design-only 1

## 📅 7일 백로그 체크

- [x] **Day 1** (2026-04-17) 스캐폴드 + 아키텍처 골격 + ADR 0001–0003 + C4 L1
- [ ] Day 2 Property (Skeleton)
- [ ] Day 3 Rate & Inventory (Full)
- [ ] Day 4 Reservation (Full, **A축 증명**)
- [ ] Day 5 Pricing (Full, **D축 증명**) ← **Must-have 체크포인트**
- [ ] Day 6 Search + Supplier (Skeleton) + k6 튜닝
- [ ] Day 7 Admin Design + 문서 마감 + 제출

## 🛠️ 로컬 명령어 모음

```bash
cd /Users/wanny/Repository/ota-backend-kotlin

docker compose up -d          # PostgreSQL + Redis 기동
docker compose ps             # 상태 확인
docker compose down           # 정리

./gradlew bootRun             # 앱 실행 (포트 8080)
./gradlew build -x test       # 빌드 (테스트 제외)
./gradlew test                # 전체 테스트
./gradlew ktlintCheck         # 스타일 검사
./gradlew bootJar -x test     # 실행 JAR 생성
```

## 🚨 커밋 전 필수 안전 체크

아래 grep 레시피는 기업명·프로젝트 성격 노출 검사용. 정규식 character class로 작성되어 자기 자신은 매치하지 않음.

```bash
grep -riEl "여기[어]때|채[용]|코딩[ ]테스트|과제임[을]|mvl[c]hain" docs/ src/ README.md
# 결과 비어 있어야 함 (character class로 작성되어 이 파일 자체는 매치되지 않음)
```

## 🔗 외부 레퍼런스

- 플랜 원본 백업: `~/.claude/plans/layer-squishy-iverson.md`
- 과제 안내서 PDF: `~/Downloads/Server+Engineer+[숙박플랫폼개발]_과제+테스트+상세+안내.pdf`

## 🧭 어떻게 일하는가 (사용자 선호)

- **Top-down 한 단계씩** — 구체 결정(도메인명·라이브러리)은 상위 원칙 확정 후
- **3-file 제한** — 한 sub-task는 3파일 이하로
- **코드 수정은 executor 에이전트에 위임**
- **커밋은 Conventional Commits** (`feat:` / `fix:` / `refactor:` / `chore:` / `docs:` / `test:`)
- **모든 텍스트는 한국어 가능, 커밋 메시지/PR은 영어만**
