# Voting Arena

The landing page and core product experience. The user is shown two competing Patterns from the same Topic and picks the one they prefer, or skips. Each Vote updates the Patterns' rankings. Matchups stream in batches so voting flows without interruption.

**Roles:** USER / ADMIN
**Status:** Implemented

## Requirements

1. Given an authenticated user, when they open `/`, then a matchup is shown immediately — the Topic question, its Labels, a vote count, and two Pattern cards with syntax-highlighted code.
2. Given a matchup, when the user clicks a Pattern card, then a Vote is recorded and the next matchup renders without a page reload.
3. Given a matchup, when the user picks "No preference" or "Not familiar enough", then a Skip is recorded with that reason and no ranking changes.
4. Given a Vote, when it is recorded, then both Patterns' ELO ratings update (winner up, loser down by a complementary amount) and `timesShown` increments on both while `timesChosen` increments on the winner — atomically.
5. Given a session, when the user votes, then a progress indicator shows their position in the batch (e.g. "3 / 10"); when the batch is exhausted a fresh batch loads or an "all caught up" state shows.
6. Given the same user in a session, when matchups are generated, then no Topic and no Pattern pair repeats while unseen pairs remain.

## How to use

Open the **Vote** tab (the landing page). Read the question, compare the two code snippets, and click the one you prefer — or use a Skip button if you genuinely have no preference or aren't familiar enough to judge. The next matchup appears instantly.

## API

| Method & path | Role | Purpose |
|---|---|---|
| `GET /api/voting/matchup` | USER | A single matchup (Topic + two active Patterns) |
| `GET /api/voting/matchups?count=` | USER | A batch of matchups for client-side cycling |
| `POST /api/voting/vote` | USER | Record a Vote (winner + loser Pattern ids); updates ELO & counters |
| `POST /api/voting/skip` | USER | Record a Skip (both Pattern ids + SkipReason) |

## Technical details

1. ELO: expected = `1 / (1 + 10^((opponent - player) / 400))`, new = `old + K * (actual - expected)`, K = 32, actual = 1 for the winner and 0 for the loser. Initial rating 1500.
2. Win rate is stored as counters on each Pattern: `timesChosen / timesShown`. Skips touch neither counter.
3. Matchup generation picks a random eligible Topic (≥2 active Patterns) then two random Patterns; pairs already voted/skipped by the user in the session are avoided as long as unseen pairs exist. SkipReason is a fixed enum (`NO_PREFERENCE`, `NOT_FAMILIAR`).
4. The Vote and both rating/counter updates happen in one transaction. The frontend fetches a batch of matchups upfront and posts each Vote/Skip individually as it is cast.

## Related features

- [Expertise Filter](expertise-filter.md) — restricts which Topics appear in matchups
- [Vote Comments & Voting Streak](vote-comments-and-streak.md) — optional comment and the daily streak shown here
- [Rankings Leaderboard](rankings-leaderboard.md) — where the accumulated ELO / win rate is surfaced
