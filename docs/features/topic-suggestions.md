# Topic Suggestions

When a whole subject area is missing from the dataset, a user can submit a TopicSuggestion — a proposed question, optional context, and suggested Labels. Admins review these and approve one to create a new Topic, or reject it with a reason. Mirrors the PatternSuggestion workflow but at the Topic level.

**Roles:** USER (submit, track) / ADMIN (review)
**Status:** Implemented

## Requirements

1. Given the Catalog, when a user submits a TopicSuggestion (question, optional context, Labels), then it is created with status PENDING.
2. Given a user, when they view "My suggestions", then they see their TopicSuggestions' status and any rejection reason.
3. Given an admin approves a PENDING suggestion, then a new Topic is created carrying the suggested Labels and starting with no Patterns; the suggestion moves to APPROVED.
4. Given an admin rejects a PENDING suggestion, then it moves to REJECTED with an optional reason and no Topic is created.
5. Given an approved Topic with no Patterns, when matchups are generated, then it is excluded until it has at least two active Patterns (though it is visible in the Catalog).
6. Given an already-reviewed suggestion, when reviewed again, then it returns 409.

## How to use

**As a user:** on the Catalog page click **Suggest a new topic**, enter the question, optional context, and pick Labels, then submit. Track it under "My suggestions".

**As an admin:** the **Review** tab lists pending topic suggestions alongside pattern suggestions, each with Approve and Reject (Reject opens a reason input).

## API

| Method & path | Role | Purpose |
|---|---|---|
| `POST /api/suggestions/topics` | USER | Submit a TopicSuggestion (PENDING) |
| `GET /api/users/me/suggestions/topics` | USER | The user's own TopicSuggestions with status & reason |
| `GET /api/admin/suggestions/topics?status=PENDING` | ADMIN | List suggestions by status |
| `POST /api/admin/suggestions/topics/{id}/approve` | ADMIN | Approve → creates a Topic, status APPROVED |
| `POST /api/admin/suggestions/topics/{id}/reject` | ADMIN | Reject with optional reason, status REJECTED |

## Technical details

1. State machine identical to PatternSuggestion: `PENDING → APPROVED | REJECTED`; re-review returns 409.
2. Approval creates a Topic (question + context + Labels) but never any Patterns — so the new Topic shows in the Catalog yet stays out of matchups until Patterns are added (by an admin or via approved PatternSuggestions).
3. Suggested Labels are stored on a join table; entity→DTO mapping happens inside the service transaction.

## Related features

- [Pattern Suggestions](pattern-suggestions.md) — the parallel Pattern-level workflow; shares the admin Review screen
- [Dataset Administration](dataset-administration.md) — admins can add Patterns to an approved Topic
- [Dataset Catalog](dataset-catalog.md) — where new Topics become visible
