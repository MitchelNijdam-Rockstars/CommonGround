# Dataset Administration

Admin-only tools to build and curate the dataset: managing the Label vocabulary, creating Topics with their question/context/Labels, adding competing Patterns to a Topic, and deactivating poor Patterns without losing their vote history.

**Roles:** ADMIN
**Status:** Implemented

## Requirements

1. Given an admin, when they create a Label with a name and LabelType, then it joins the managed vocabulary (duplicate names are rejected with 409).
2. Given an admin, when they create a Topic with a question, optional context, and Label ids, then the Topic is added (unknown Label ids are rejected with 400).
3. Given an admin, when they add a Pattern to a Topic, then it enters the voting pool with ELO 1500, zero counters, and active = true.
4. Given an admin, when they deactivate a Pattern, then it stops appearing publicly and in matchups but its row and vote history are preserved.
5. Given a non-admin, when any of these endpoints is called, then it returns 403.

## How to use

These actions are exposed via the admin API (and are also driven internally by the [batch import](batch-import.md) and [suggestion approval](pattern-suggestions.md) features). Labels can also be renamed.

## API

| Method & path | Role | Purpose |
|---|---|---|
| `POST /api/admin/labels` | ADMIN | Create a Label (name + LabelType) |
| `PATCH /api/admin/labels/{labelId}` | ADMIN | Rename a Label |
| `POST /api/admin/topics` | ADMIN | Create a Topic (question, optional context, Label ids) |
| `POST /api/admin/topics/{topicId}/patterns` | ADMIN | Add a Pattern (title, code, language) to a Topic |
| `PATCH /api/admin/patterns/{patternId}/deactivate` | ADMIN | Mark a Pattern inactive (preserves history) |

## Technical details

1. LabelType is a fixed enum: `LANGUAGE`, `FRAMEWORK`, `ARCHITECTURE`, `PARADIGM`, `STYLE`.
2. Schema is owned by Flyway (`ddl-auto: validate`); the `topic`/`label`/`topic_label`/`pattern` tables back these operations.
3. Patterns are never hard-deleted — the `active` flag keeps historical Votes and ELO/win-rate intact. A Topic needs ≥2 active Patterns to be matchup-eligible (enforced at the API level).
4. All endpoints sit under `/api/admin/**` and are guarded by the admin interceptor.

## Related features

- [Authentication & User Roles](authentication.md) — supplies the ADMIN gate
- [Dataset Catalog](dataset-catalog.md) — the public, read-only view of what admins manage
- [Batch Import](batch-import.md) — bulk alternative to manual entry
