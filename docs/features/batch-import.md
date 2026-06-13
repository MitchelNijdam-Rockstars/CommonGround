# Batch Import

Lets an admin seed the dataset in bulk from a structured JSON file — the integration point for an external AI codebase scanner that emits Topics and Patterns. The import is idempotent: re-running the same file does not create duplicates.

**Roles:** ADMIN
**Status:** Implemented

## Requirements

1. Given an admin posts a JSON body of Topics and Patterns, when imported, then new Topics and Patterns are created with correct initial values (Pattern: ELO 1500, zero counters, active).
2. Given a Topic whose question already exists (case-insensitive), when imported, then the existing Topic is reused, not duplicated.
3. Given a Pattern whose title already exists for a Topic (case-insensitive), when imported, then it is skipped, not duplicated.
4. Given Labels referenced by name, when imported, then they are matched case-insensitively to existing Labels; unrecognised names are auto-created with a type inferred from a name→type mapping, defaulting to STYLE.
5. Given an import, when it completes, then the response reports counts: Topics created vs reused, Patterns created vs skipped (and Labels created).
6. Given a malformed body or schema violation, when posted, then it returns 400.
7. Given the same file imported twice, then the second import changes nothing (idempotent).

## How to use

This is an API integration, not a UI. POST the scanner's JSON to `/api/admin/import` as an admin. Example body:

```json
{
  "topics": [
    {
      "question": "How should null values be handled in Kotlin service methods?",
      "context": "Optional baseline context shown to voters.",
      "labels": ["Kotlin", "Spring Boot"],
      "patterns": [
        { "title": "Return nullable type", "language": "kotlin", "code": "fun findUser(id: Long): User? { ... }" }
      ]
    }
  ]
}
```

## API

| Method & path | Role | Purpose |
|---|---|---|
| `POST /api/admin/import` | ADMIN | Idempotent bulk import; returns created/reused/skipped counts |

## Technical details

1. Idempotent upserts: Topic matched by question (case-insensitive), Pattern by title within the Topic (case-insensitive), Label by name (case-insensitive). The whole import runs in one transaction.
2. The JSON schema above is the stable contract with the scanner tool. Unknown Labels are created via a configurable name→type mapping (e.g. kotlin/java/typescript → LANGUAGE; spring/angular/react → FRAMEWORK; unknown → STYLE).
3. New Patterns use the same initial values as manually created ones (ELO 1500, counters 0, active).
4. Lives under `/api/admin/**`, so a non-admin receives 403; the backend package is named `dataimport` (`import` is a Kotlin keyword).

## Related features

- [Dataset Administration](dataset-administration.md) — the manual equivalent of what this automates
- [Authentication & User Roles](authentication.md) — supplies the ADMIN gate
