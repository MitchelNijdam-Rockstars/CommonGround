# Pattern Detail & Anonymised Comments

The qualitative companion to the leaderboard. Clicking any Pattern on the Rankings page opens a detail view with the full syntax-highlighted code and the compiled list of voter comments — shown without any attribution. It answers "why does the community prefer this Pattern?"

**Roles:** USER / ADMIN
**Status:** Implemented

## Requirements

1. Given the Rankings page, when a user clicks a Pattern, then a detail view opens showing the full code, the Pattern's rank/score, and its comments.
2. Given the detail endpoint, when called, then it returns the Pattern's title, code, language, ELO rating, win rate, and a list of anonymised comments (text + relative timestamp only — no voter identity).
3. Given comments exist, when shown, then they are ordered most-recent first and only non-empty comments are included.
4. Given a Pattern with no comments, when opened, then an empty state ("No comments yet") is shown.
5. Given an unknown Pattern id, when the endpoint is called, then it returns 404.
6. Given the detail view, when dismissed, then the user returns to the Rankings page with the same algorithm and scroll position.

## How to use

On the **Rankings & Insights** page, click any Pattern entry. An overlay opens with the full code and the anonymous reasoning left by voters. Close it (X, backdrop, or back) to return to exactly where you were.

## API

| Method & path | Role | Purpose |
|---|---|---|
| `GET /api/patterns/{patternId}` | USER | Pattern detail + anonymised comment list; 404 if not found |

## Technical details

1. Comments are the optional text from Votes where this Pattern was the chosen winner; the query returns them newest-first, excluding null/blank comments, exposing only the text and `createdAt` — never the voter.
2. The detail is rendered as an overlay over the Rankings page, which naturally preserves the algorithm selection and scroll position.
3. Relative timestamps ("2 days ago") are formatted client-side from the returned instant.

## Related features

- [Rankings Leaderboard](rankings-leaderboard.md) — the entry point to this view
- [Vote Comments & Voting Streak](vote-comments-and-streak.md) — where the comments originate
