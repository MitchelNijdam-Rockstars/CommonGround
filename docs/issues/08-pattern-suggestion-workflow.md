# 08 — PatternSuggestion Workflow

## What to build

Allow Users to suggest new Patterns for existing Topics when their preferred coding style is not in the dataset. Admins review pending suggestions and either approve or reject them.

A PatternSuggestion captures the suggested code snippet, an optional title, the target Topic, and the submitting User. It moves through three states: `PENDING` → `APPROVED` or `REJECTED`. Approving a suggestion creates a new active Pattern entity (ELO: 1500, counters: 0) and transitions the suggestion to APPROVED — the suggestion itself is never promoted into a Pattern, it remains a historical record. Rejecting a suggestion records an optional rejection reason.

On the Catalog page, an "Add an alternative" button inside an expanded Topic opens a form where a User can submit a PatternSuggestion. A "My suggestions" section shows the User's own suggestions with their current status and any rejection reason.

The Admin review view (accessible to ADMIN role only) lists all pending PatternSuggestions grouped by Topic, with approve and reject actions.

Tests cover the state machine: valid transitions (PENDING → APPROVED, PENDING → REJECTED), invalid transitions (APPROVED → REJECTED), and the side effect that approval creates a Pattern with correct initial values.

## Acceptance criteria

- [ ] Flyway migration creates the `pattern_suggestion` table in the `common_ground` schema
- [ ] `POST /api/topics/{topicId}/suggestions/patterns` submits a PatternSuggestion; authenticated Users only; initial state is PENDING
- [ ] `GET /api/users/me/suggestions/patterns` returns the authenticated User's PatternSuggestions with status and rejection reason
- [ ] `GET /api/admin/suggestions/patterns?status=PENDING` returns all pending PatternSuggestions; Admin-only
- [ ] `POST /api/admin/suggestions/patterns/{id}/approve` transitions to APPROVED and creates a new active Pattern; Admin-only
- [ ] `POST /api/admin/suggestions/patterns/{id}/reject` transitions to REJECTED with an optional reason; Admin-only
- [ ] Attempting an invalid state transition returns 409
- [ ] The Catalog page shows an "Add an alternative" button within expanded Topics; clicking it opens the suggestion form
- [ ] The suggestion form includes a code input with syntax highlighting and an optional title field
- [ ] The "My suggestions" section shows status (pending / approved / rejected) and rejection reason when available
- [ ] The Admin review view lists pending suggestions with approve and reject actions; rejecting opens a reason input
- [ ] Unit tests: valid and invalid state machine transitions; approval side-effect creates Pattern with ELO 1500 and zero counters

## Blocked by

- [04 — Pattern Management](04-pattern-management.md)
