# 09 — TopicSuggestion Workflow

## What to build

Allow Users to suggest entirely new subject areas when a Topic they care about is absent from the dataset. This mirrors the PatternSuggestion workflow (slice 08) but targets the Topic level.

A TopicSuggestion captures a proposed question, optional context, suggested Labels, and the submitting User. It moves through the same three states: `PENDING` → `APPROVED` or `REJECTED`. Approving a TopicSuggestion creates a new Topic entity — the Topic starts with no Patterns and is not immediately eligible for voting (Patterns must be added by an Admin or via PatternSuggestions). Rejecting records an optional reason.

On the Catalog page, a "Suggest a new topic" button (distinct from the per-Topic "Add an alternative") opens a form for submitting a TopicSuggestion. The Admin review view includes a tab or section for pending TopicSuggestions alongside PatternSuggestions.

## Acceptance criteria

- [ ] Flyway migration creates the `topic_suggestion` table in the `common_ground` schema
- [ ] `POST /api/suggestions/topics` submits a TopicSuggestion; authenticated Users only; initial state is PENDING
- [ ] `GET /api/users/me/suggestions/topics` returns the authenticated User's TopicSuggestions with status and rejection reason
- [ ] `GET /api/admin/suggestions/topics?status=PENDING` returns all pending TopicSuggestions; Admin-only
- [ ] `POST /api/admin/suggestions/topics/{id}/approve` transitions to APPROVED and creates a new Topic with no Patterns; Admin-only
- [ ] `POST /api/admin/suggestions/topics/{id}/reject` transitions to REJECTED with an optional reason; Admin-only
- [ ] The created Topic is visible in the Catalog but excluded from matchup generation until it has at least two active Patterns
- [ ] The Catalog page includes a "Suggest a new topic" button that opens a form with a question field, optional context, and Label selection
- [ ] The Admin review view surfaces pending TopicSuggestions alongside PatternSuggestions

## Blocked by

- [03 — Labels & Topic Browsing](03-labels-topic-browsing.md)
