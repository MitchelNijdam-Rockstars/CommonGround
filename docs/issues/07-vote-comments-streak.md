# 07 — Vote Comments & Voting Streak

## What to build

Two small but motivating additions to the voting experience: optional Vote comments and a daily streak counter.

**Vote comments**: When casting a Vote, a User may optionally add a short text comment explaining their reasoning. The comment is stored on the Vote record and tied to the User internally (for spam detection), but it will be displayed without attribution in the Rankings page (slice 11). The comment field in the UI is unobtrusive — the primary action is clicking a Pattern card; the comment box appears below and is clearly optional.

**Voting streak**: Track each User's voting activity to compute a streak in calendar days. The streak increments when a User casts at least one Vote on a given day and resets if a full calendar day passes without any Vote. The current streak is displayed on the Voting Arena as a flame indicator (e.g. "🔥 7 days").

## Acceptance criteria

- [ ] The `vote` table gains an optional `comment` column (nullable text, max 500 characters); added via Flyway migration
- [ ] `POST /api/voting/vote` accepts an optional `comment` field; long comments are rejected with 400
- [ ] The Voting Arena renders an optional comment text area below the Pattern cards; it does not block or delay the Vote action
- [ ] The User entity (or a related streak record) tracks `lastVotedDate` and `currentStreak`; updated on every Vote
- [ ] Streak increments when a User votes on a day they have not yet voted; resets to 1 if the previous vote was more than one calendar day ago
- [ ] `GET /api/users/me` includes the User's current streak count
- [ ] The Voting Arena displays the streak with a visual indicator; a streak of 0 or 1 is not highlighted

## Blocked by

- [05 — Voting Arena: Core](05-voting-arena-core.md)
