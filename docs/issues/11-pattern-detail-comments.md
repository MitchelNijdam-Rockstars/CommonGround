# 11 — Pattern Detail & Anonymised Comments

## What to build

Add a Pattern detail view accessible from the Rankings leaderboard. Clicking any Pattern on the Rankings page opens a detail overlay (or dedicated route) showing the full code block with syntax highlighting and a compiled list of anonymised voter comments.

Comments are the optional text left by voters during the Voting Arena (slice 07). They are displayed without any attribution — no username, no avatar. The only metadata shown alongside a comment is the approximate time it was submitted (e.g. "2 days ago"). This makes the detail view the qualitative companion to the quantitative leaderboard: it answers "why does the community prefer this Pattern?"

The backend exposes a Pattern detail endpoint that returns the Pattern's full data plus its associated comments.

## Acceptance criteria

- [ ] `GET /api/patterns/{patternId}` returns the Pattern's title, code, language, ELO rating, win rate, and a list of its anonymised Vote comments (text + relative timestamp only, no voter identity)
- [ ] Comments are ordered by most recent first
- [ ] Only non-empty comments are returned; Votes with no comment are excluded from the list
- [ ] The Rankings page renders each Pattern entry as clickable; clicking opens the detail view
- [ ] The detail view shows the full syntax-highlighted code block, the Pattern's rank and score, and the comment list
- [ ] The detail view is closeable (overlay dismissed or back navigation) and returns the user to the Rankings page with the same algorithm and scroll position
- [ ] Patterns with no comments display an appropriate empty state ("No comments yet")

## Blocked by

- [07 — Vote Comments & Voting Streak](07-vote-comments-streak.md)
- [10 — Rankings Leaderboard](10-rankings-leaderboard.md)
