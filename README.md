# Common Ground

Have you ever tried to define coding standards for your team or department? And everyone had different opinions?
Democratize your coding standards with Common Ground!

In a fun quiz-style ranking game you pick your preferred coding standards. After collecting everyone's votes,
you can export the highest ranked coding standards to a file for your AI coding agent, so that it adheres to your
standards.

## Running locally

First:
- Set the spring profile to `local`
- Start the database: `docker compose up -d`

### Option 1: Angular app inside Spring Boot (deployed version, no live-reload)

```shell
./gradlew clean build
```

Access website on: http://localhost:8012

### Option 2: running frontend & backend separately (live-reload enabled)

In different terminals:

```shell
./gradlew bootRun
```

```shell
cd frontend && npm start
```

Access website on: http://localhost:4200

## Detailed docs

See for more in depth docs of this project the [CLAUDE.md](CLAUDE.md) file.