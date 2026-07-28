# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ACG_Space is an anime content + digital gacha trading community platform. Three independent projects:

| Project | Directory | Stack |
|---|---|---|
| Backend | `backend/` | Java 17, Spring Boot 3, MyBatis-Plus, RuoYi-Vue |
| Admin UI | `admin-ui/` | Vue 3 + Vite + Element Plus + Pinia + TypeScript |
| Front UI | `front-ui/` | Nuxt 3 + Tailwind CSS + Pinia + TypeScript |

## Environment Setup

This machine uses:
- **JDK 21** at `%USERPROFILE%\.vscode\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64` (VS Code extension, compatible with Java 17 target)
- **Maven 3.9.16** at `C:\tools\apache-maven-3.9.16`
- User-level env vars: `JAVA_HOME`, `MAVEN_HOME`, and `%JAVA_HOME%\bin;%MAVEN_HOME%\bin` in PATH

## Essential Commands

### Docker (Infrastructure)
```bash
cd ACG_Space
docker-compose up -d      # Start MySQL, Redis, RocketMQ NameServer + Broker
docker-compose down -v    # Tear down (wipes DB data)
```

Local image tags used (do not change without verifying availability): `mysql:8.0`, `redis:7-alpine`, `apache/rocketmq:5.2.0`.

### Backend
```bash
cd backend
# Build (skip test compilation — test code has outdated API references)
mvn clean install -Dmaven.test.skip=true
# Run
java -jar target/acg-space-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### Admin UI
```bash
cd admin-ui
npm install                # First time only
npm run dev                # http://localhost:5173
```

### Front UI
```bash
cd front-ui
npm install                # First time only
npm run dev                # http://localhost:3000
```

## Startup Order

1. `docker-compose up -d` — MySQL must finish init scripts before step 2 (check: `docker logs acg_mysql | grep "ready for start up"`)
2. Start backend — depends on MySQL/Redis/RocketMQ being up
3. Start front-ui / admin-ui — either or both, order doesn't matter

## Docker Services

| Container | Port | Credentials |
|---|---|---|
| `acg_mysql` | 3306 | root / 123456, database: `acg_space` |
| `acg_redis` | 6379 | no password |
| `acg_rmqnamesrv` | 9876 | — |
| `acg_rmqbroker` | 10909, 10911 | — |

RocketMQ 5.2.0 uses `nameserver` and `broker` commands (not `sh mqnamesrv`/`sh mqbroker` from 4.x). The entrypoint script translates these.

Broker config mounts `./broker.conf` into the container — it includes `brokerIP1 = 127.0.0.1` to fix connectivity between Docker and host.

## Database Initialization

MySQL auto-runs files in `/docker-entrypoint-initdb.d/` alphabetically:

1. `./backend/sql/schema.sql` → creates `sys_user` base table (required by the migration script)
2. `./backend/sql/ACG_Space_V3.0_Complete.sql` → creates all `biz_*` business tables (use `ACG_Space_init.sql` for fresh install)

**Known issue**: The migration SQL is outdated vs. the entity classes. `biz_anime` column names differ (e.g., `name` vs `title`, `type` vs `genre`). The existing fix script `./backend/sql/fix_missing_columns.sql` must be run after first `docker-compose up -d`:
```bash
docker cp backend/sql/fix_missing_columns.sql acg_mysql:/tmp/fix.sql
docker exec acg_mysql mysql -uroot -p123456 acg_space -e "source /tmp/fix.sql"
```

These fixes are already applied to the current database.

**Additional schema issue**: `biz_anime.genre` is `varchar(100)` but Bangumi API returns genre strings that can exceed 100 chars (e.g., "搞笑, 奇幻, 冒险, 热血, 战斗, ..."). This causes `MysqlDataTruncation` on `syncFromBangumi()`. Applied fix:
```bash
docker exec acg_mysql mysql -uroot -p123456 acg_space -e "ALTER TABLE biz_anime MODIFY COLUMN genre VARCHAR(500) DEFAULT NULL COMMENT '类型/标签';"
```

**Diagnosing backend 500**: Always `tail -50 /tmp/acg-backend.log` and look for the deepest `Caused by` — the root cause is often a SQL error buried under layers of framework stack traces.

## Bangumi API

The Bangumi API (`api.bgm.tv`) is blocked in China. Configured to use mirror:
- **API**: `https://bgmapi.anibt.net` (set in `application.yml` → `bangumi.api.base-url`)
- **Images**: `https://bgmimg.anibt.net` (auto-followed from API responses)

If the mirror goes down, check `backend/src/main/java/com/ruoyi/project/integration/BangumiApiClient.java` for all endpoint paths.

## Architecture

### Backend (`com.ruoyi.project`)

Standard RuoYi-Vue layered architecture:

- **`controller/`** — REST controllers (27 total). All return unified `Result<T>` responses. Parameters validated with `@Validated`. Get current user via `SecurityUtils.getUserId()`.
- **`service/` + `service/impl/`** — Business logic layer
- **`mapper/`** — MyBatis-Plus mappers
- **`domain/`** — `entity/` (DB entities, extend `BaseEntity`), `dto/`, `vo/`
- **`config/`** — Spring config beans. **All `@Bean` methods must have explicit names** to prevent `BeanDefinitionOverrideException`.
- **`mq/`** — RocketMQ producers, consumers, and listeners. **All consumers must include idempotency checks.**
- **`integration/`** — External API clients (e.g., `BangumiApiClient`)
- **`common/`** — Annotations, interceptors, utilities, constants

**Critical data concerns:**
- Snowflake IDs (19-digit `Long`) must be serialized as strings via Fastjson2 `WriteLongAsString` — JavaScript cannot safely represent them as numbers.
- All `BaseEntity` subclasses need audit columns: `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `del_flag`.
- No global `MetaObjectHandler` — controllers must explicitly set `createTime`/`updateTime` on insert/update.
- Image/base64 fields must use `longtext` or `mediumtext`, not `varchar(500)`.

**Redis & Concurrency:**
- Lua scripts in `resources/lua/` (`gacha_deduct_stock.lua`, `gacha_get_stock.lua`) — atomic stock operations for the gacha system.
- Redisson distributed locks (MultiLock) in synthesis module — always release in `finally` blocks.

### Admin UI (`admin-ui/src`)

- **`router/index.ts`** — All routes defined explicitly under `AdminLayout`. URL path → view component. `@` alias = `src/`.
- **`views/`** — Page components organized by domain (`anime/`, `item/`, `gacha/`, `user/`, `article/`, etc.). Standard pattern: search form + data table + pagination + create/edit dialog.
- **`api/`** — Axios-based API modules. Each module exports typed functions and TypeScript interfaces. Base URL is `/api`. Import from `@/api` barrel file.

### Front UI (`front-ui`)

- **`pages/`** — Nuxt file-based routing. File path = URL path. `index.vue` = directory default. `[id].vue` = dynamic route param.
- **`composables/useApi.ts`** — Unified API client. SSR phase hits backend internal address directly; CSR phase goes through Nitro devProxy (`/api-proxy` → `/api`).
- **`stores/`** — Pinia stores: `user.ts` (auth/user state), `app.ts` (app-level state), `anime.ts`.
- **`nuxt.config.ts`** — Hybrid rendering: `routeRules` configure SSR/SWR/ISR per route. `runtimeConfig` separates server-internal and public configuration.
- **Tailwind CSS** — All styling via utility classes. Three themes: light, dark, pink. Every new page must adapt to all three.

**⚠️ Nuxt 3 SSR Pitfalls (critical — these cause bugs that are hard to spot):**

1. **Hydration mismatch**: SSR and client must produce identical DOM on first render. Any `ref` that controls `v-if`/`v-for` output must be initialized to the same value in `<script setup>` top-level (runs on both server and client), NOT in `onMounted` (client-only). If SSR renders 3 items and client renders 5, Vue's hydration fails silently — the page looks fine but ALL event listeners (NuxtLink, @click) are dead. **Symptom**: "page displays but buttons don't work" + Console has `Hydration children mismatch` warning.

2. **useAsyncData null return trap**: The fetcher function passed to `useAsyncData`/`useFetch` must NEVER return `null` or `undefined`. If it does, Nuxt warns "must return a value" and **re-executes the fetcher on the client**, creating an infinite error loop. Store methods called by `useAsyncData` must `throw` on error, not `return null`. The thrown error sets `useAsyncData`'s `error` ref properly and stops retries.

3. **Versions**: Nuxt 3.21.4, Vue 3.5.33, Vue Router 5.0.6. `navigateTo`, `useRouter`, `useAsyncData` are auto-imported — available without explicit import in `<script setup>` and templates.

## API Conventions

- Backend responses: `Result<T>` with `{ code, msg, data }`. Success code is `200`. **HTTP 200 does NOT mean success** — check the `code` field inside the response body.
- Pagination: `{ records, total, size, current, pages }`.
- Auth: JWT tokens, passed via `Authorization: Bearer xxx` header. Frontend interceptors handle this automatically.
- Admin routes are under `/api/*`; public frontend routes may differ.
- When the Bangumi API fails, the backend previously returned `{code:200, data:null}` — silently masking the failure. The `BizAnimeController.searchBangumi` endpoint now returns `code:500` on null result.

## Diagnostics Quick Reference

When the frontend shows errors:
1. **F12 Console** — English TypeError (e.g., `Cannot read properties of undefined`) = frontend issue. Chinese error message ("系统异常") = backend issue.
2. **F12 Network** — Check the API response body's `code` field, not just HTTP status.
3. **Backend log** — `tail -30 /tmp/acg-backend.log` for the latest errors. Always look for the deepest `Caused by` line — the root cause is often a SQL error buried under framework stack traces.

**Hydration mismatch recognition**: "Page displays but all navigation/buttons are dead, yet local state changes (tabs, toggles) work" + Console has `Hydration children mismatch` → check `onMounted` for state changes that affect DOM output. See Nuxt 3 SSR Pitfalls above.

## Critical Rules (from AGENTS.md)

1. **Security config**: New API paths must be added to `permitAll()` in `SecurityConfig`, or they return 403.
2. **TypeScript**: `any` is forbidden in both frontend projects.
3. **Bean naming**: All `@Bean` definitions must have explicit names.
4. **Pre-commit hooks**: Checkstyle, SpotBugs, PMD, and ESLint are configured — code must pass all.
5. **Plan before acting**: If modifying 5+ files or changing core architecture, present a plan and wait for approval before coding.
6. **Proactive documentation**: After completing a feature series, ask whether to update `document/develop/V2/` docs.
7. **Self-study**: Record mistakes in `document/study/study.md` with format: `- [YYYY-MM-DD] 错误：xxx | 原因：xxx | 防范：xxx`
8. **Point mechanism**: Read `document/point/point.md` before every response as source of truth. Update it after completing milestones.
9. **UI themes**: Front-ui has three themes (light/dark/pink) — every new page must work with all three.
10. **Self-testing**: After implementing a feature, test it manually in the browser. If broken, diagnose and fix.
