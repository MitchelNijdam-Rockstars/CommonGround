# 04 — Pattern Management

## What to build

Introduce the Pattern entity — the individual competing coding style option that belongs to a Topic. Each Pattern has a title, a code snippet, the programming language for syntax highlighting, an ELO rating (default 1500), win/loss counters, and an active flag.

Admins can add new Patterns to an existing Topic and deactivate Patterns without deleting them (so historical vote data is preserved). Regular Users can view all active Patterns for a Topic.

On the Catalog page, clicking (or expanding) a Topic now reveals its active Patterns, each shown as a code block with syntax highlighting.

## Acceptance criteria

- [ ] Flyway migration creates the `pattern` table in the `common_ground` schema
- [ ] `POST /api/admin/topics/{topicId}/patterns` creates a Pattern with a title, code snippet, and language; Admin-only
- [ ] `PATCH /api/admin/patterns/{patternId}/deactivate` marks a Pattern inactive; Admin-only; the Pattern's vote history is preserved
- [ ] `GET /api/topics/{topicId}/patterns` returns only active Patterns for a Topic
- [ ] The Catalog page expands a Topic to display its active Patterns as syntax-highlighted code blocks
- [ ] Inactive Patterns do not appear in the public Pattern list
- [ ] A Topic with fewer than two active Patterns is excluded from matchup generation (enforced at the API level, not just UI)

## Blocked by

- [03 — Labels & Topic Browsing](03-labels-topic-browsing.md)
