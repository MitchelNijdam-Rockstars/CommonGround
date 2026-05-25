# 13 — Batch Import

## What to build

Allow Admins to seed the dataset by importing a structured JSON file containing Topics and Patterns in bulk. This is the primary integration point for the AI codebase scanner output — the scanner analyses a codebase, identifies inconsistencies, and emits a JSON file in the format this endpoint expects.

The import endpoint is Admin-only and idempotent where possible: if a Topic with the same question already exists it is reused rather than duplicated; if a Pattern with the same title already exists for that Topic it is skipped. New Topics and Patterns are created; no existing data is modified or deleted.

Each imported Pattern starts with ELO 1500 and zero vote counters, identical to manually created Patterns.

The JSON format is the canonical contract between the import tool and Common Ground. It must be stable and is documented here:

```json
{
  "topics": [
    {
      "question": "How should null values be handled in Kotlin service methods?",
      "context": "Optional baseline context shown to voters.",
      "labels": ["Kotlin", "Spring Boot"],
      "patterns": [
        {
          "title": "Return nullable type",
          "language": "kotlin",
          "code": "fun findUser(id: Long): User? { ... }"
        }
      ]
    }
  ]
}
```

Label values in the import file are matched by name (case-insensitive). Labels that do not exist in the system are created automatically with their type inferred from a name-to-type mapping config; unknown names default to type `STYLE`.

## Acceptance criteria

- [ ] `POST /api/admin/import` accepts a JSON body matching the schema above; Admin-only
- [ ] Topics that already exist (matched by question text, case-insensitive) are reused; no duplicate Topics are created
- [ ] Patterns that already exist for a Topic (matched by title, case-insensitive) are skipped; no duplicate Patterns are created
- [ ] New Topics and Patterns are created with correct initial values (ELO 1500, counters 0, active)
- [ ] Labels referenced by name are matched case-insensitively to existing Labels; unrecognised Labels are created automatically
- [ ] The response body reports how many Topics were created vs reused, and how many Patterns were created vs skipped
- [ ] A malformed JSON body or schema violation returns 400 with a descriptive error
- [ ] Importing the same file twice produces the same dataset state (idempotent)

## Blocked by

- [04 — Pattern Management](04-pattern-management.md)
