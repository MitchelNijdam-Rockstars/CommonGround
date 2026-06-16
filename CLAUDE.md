# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Common Ground is a quiz-style ranking game for democratizing coding standards. Teams vote on preferred coding standards; the highest-ranked results can be exported as a file for AI coding agents.

## Tech Stack

- **Backend**: Kotlin + Spring Boot 4.0.6, JPA, Flyway, PostgreSQL
- **Frontend**: Angular 21, TypeScript 5.9, Vitest for tests, Prettier for formatting, Tailwind, Lucide icons
- **Build**: Gradle with `com.github.node-gradle.node` plugin (auto-downloads Node 24.15.0)
- **Database**: PostgreSQL 16 (Docker), schema `common_ground`, managed by Flyway

## Domain language
See [docs/CONTEXT.md](docs/CONTEXT.md) for the domain-specific definitions.

## Running Locally

**Prerequisite**: Set the Spring profile to `local` and start the database:
```shell
docker compose up -d
```

**Option 1 — integrated build** (no live-reload), served at http://localhost:8012:
```shell
./gradlew clean build
```

**Option 2 — separate processes** (live-reload), frontend at http://localhost:4200:
```shell
./gradlew bootRun          # terminal 1
cd frontend && npm start   # terminal 2
```

## Common Commands

| Task | Command |
|------|---------|
| Build everything (backend + frontend) | `./gradlew build` |
| Run backend only | `./gradlew bootRun` |
| Run backend tests | `./gradlew test` |
| Run a single backend test | `./gradlew test --tests "com.mitchelnijdam.commonground.SomeTest"` |
| Run frontend dev server | `cd frontend && npm start` |
| Run frontend tests | `cd frontend && npm test` |
| Format frontend | `cd frontend && npx prettier --write .` |

## Architecture

The Angular frontend is built by Gradle (via the Node plugin) and copied into `build/resources/main/static`, where Spring Boot serves it as static content. This means `./gradlew build` produces a single deployable JAR. During development, run them separately so Angular's live-reload works.

**Backend** (`src/main/kotlin/com/mitchelnijdam/commonground/`): Standard Spring Boot layered structure. JPA with `ddl-auto: validate` — schema changes must be done via Flyway migrations, never by modifying entities alone.

**Frontend** (`frontend/src/app/`): Angular 21 standalone components with the Angular Router.

**Database**: Local Docker runs on port `5433` (not the standard 5432). The `docker/init/01_create_app_user.sql` script creates the `common_ground_user` app account on first container start. Flyway owns the `common_ground` schema.


## Project structure

The root project is Spring Boot app, using `build.gradle.kts`. The backends source code can be found in
`src/main/kotlin/com/mitchelnijdam/commonground`.
In the root of the project, the Angular frontend is placed inside the `frontend` folder.


commonground/                         # root
├── docker-compose.yaml               # local dev DB (postgres + init scripts)
├── Dockerfile                        # builds the fat JAR image for deployment
├── build.gradle.kts                  # Gradle build: backend + Angular frontend
│
├── src/main/
│   ├── kotlin/com/mitchelnijdam/commonground/
│   │   └── Application.kt        # Spring Boot entry point
│   └── resources/
│       ├── application.yaml          # base config (flyway schema, JPA dialect)
│       ├── application-local.yaml    # local profile (DB url/credentials, port)
│       ├── application-prod.yaml     # production profile
│       └── db/migration/             # Flyway migrations
│
└── frontend/
    ├── proxy.conf.json               # dev proxy: /api/* → Spring Boot :8011
    ├── angular.json
    └─ src/
        ├── main.ts
        ├── index.html
        ├── styles.scss
        └── app/
            ├── app.ts                # root component
            ├── app.html
            ├── app.routes.ts         # route definitions
            └── app.config.ts         # providers (router, HttpClient)

### File structure frontend
The frontend uses a feature-based structure:

frontend/src/app/
├── core/                        # App-wide singletons
│   ├── services/                # HTTP services, state
│   ├── guards/                  # Route guards
│   └── models/                  # Shared TypeScript interfaces/types
│
├── shared/                      # Reusable UI building blocks
│   ├── components/              # Generic components (card, badge, etc.)
│   └── pipes/                   # Custom pipes
│
├── layout/                      # App shell (rendered on every page)
│   └── navbar/
│
└── features/                    # One folder per page/feature area
    └── [feature-name]/          # Future pages (finances, bookmarks, etc.)
        ├── [feature].ts
        ├── [feature].spec.ts
        ├── [feature].html
        ├── [feature].scss
        └── components/

### Package structure backend
The Spring Boot backend uses a feature-based package structure, also know as domain layered:

src/main/kotlin/com/mitchelnijdam/commonground/
├── Application.kt                    # Entrypoint
│
├── common/                           # Non-feature related classes, grouped by commonality
│   ├── web/                          # Example grouping, all web related configuration files
│   └── JacksonConfig.kt              # Single configuration files directly under common
│
└── [feature-name]/                   # domain / feature based package
    ├── FeatureRepository.kt
    ├── FeatureService.kt
    ├── FeatureController.kt
    ├── FeatureMapper.kt
    ├── Feature.kt                   # Database entity data class
    └── FeatureDto.kt                # Controller response & request DTO data classes

Of course, when extra grouping within a feature package is practical and clean, feel free to create extra layers.

## Backend rules

- All endpoints meant for the frontend should be prefixed by `/api`
- Only use interfaces when there are multiple implementations needed (not by default)

## Frontend rules

- Always use Angular CLI for creating new assets, using `ng g`. Use `ng add <package>` to add packages.
- Add tests to new components when they will contain some logic. Only test important flows
- The website should be mobile-friendly
- Tailwind is used for styling
- Lucide is used for icons (<lucide-icon name="home" />)

## Code style

### Tests

- Use **AssertJ** (`org.assertj.core.api.Assertions.assertThat`) for all assertions in backend (Kotlin/JUnit) tests. Do not use `kotlin.test` (`assertEquals`, `assertTrue`, `assertNull`, …) or JUnit's `Assertions`. AssertJ ships transitively via Spring Boot's test starters — no extra dependency is needed.

## Configuration

| Profile | Port | Notes |
|---------|------|-------|
| `local` | 8012 | Connects to Docker DB on localhost:5433, verbose SQL logging |
| `prod` | — | Logstash-format structured logging |
