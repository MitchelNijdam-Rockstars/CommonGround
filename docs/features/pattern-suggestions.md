# Pattern Suggestions

When a user's preferred coding approach isn't among a Topic's Patterns, they can submit a PatternSuggestion. Admins review pending suggestions and either approve one — which creates a real Pattern in the voting pool — or reject it with a reason. The suggestion itself is kept as a historical record, never promoted.

**Roles:** USER (submit, track) / ADMIN (review)
**Status:** Implemented

## Requirements

1. Given an expanded Topic, when a user submits a PatternSuggestion (code, optional title, language), then it is created with status PENDING.
2. Given a user, when they view "My suggestions", then they see their suggestions' status (pending/approved/rejected) and any rejection reason.
3. Given an admin approves a PENDING suggestion, then a new active Pattern is created (ELO 1500, zero counters) and the suggestion moves to APPROVED.
4. Given an admin rejects a PENDING suggestion, then it moves to REJECTED with an optional reason, and no Pattern is created.
5. Given a suggestion that is already APPROVED or REJECTED, when an admin tries to review it again, then it returns 409 (invalid transition).

## How to use

**As a user:** in the Catalog, expand a Topic and click **Add an alternative**, fill in the code (with live syntax-highlighted preview) and optional title, and submit. Track it in the "My suggestions" section of the Catalog.

**As an admin:** open the **Review** tab; pending pattern suggestions are listed with Approve and Reject (Reject opens a reason input).

## API

| Method & path | Role | Purpose |
|---|---|---|
| `POST /api/topics/{topicId}/suggestions/patterns` | USER | Submit a PatternSuggestion (PENDING) |
| `GET /api/users/me/suggestions/patterns` | USER | The user's own suggestions with status & reason |
| `GET /api/admin/suggestions/patterns?status=PENDING` | ADMIN | List suggestions by status |
| `POST /api/admin/suggestions/patterns/{id}/approve` | ADMIN | Approve → creates a Pattern, status APPROVED |
| `POST /api/admin/suggestions/patterns/{id}/reject` | ADMIN | Reject with optional reason, status REJECTED |

## Technical details

1. State machine: `PENDING → APPROVED` or `PENDING → REJECTED`; any other transition returns 409.
2. Approval is a side effect, not a promotion: a brand-new Pattern entity is created from the suggestion's code/title/language with initial ELO and zero counters; the suggestion is retained as a record (linked to the created Pattern). A suggestion with no title gets a fallback title on approval.
3. Entity→DTO mapping is done inside the service transaction to avoid lazy-loading issues.

## Related features

- [Topic Suggestions](topic-suggestions.md) — the parallel workflow at the Topic level (shares the admin Review screen)
- [Dataset Catalog](dataset-catalog.md) — where suggestions are submitted and tracked
- [Voting Arena](voting-arena.md) — approved Patterns enter the matchup pool
