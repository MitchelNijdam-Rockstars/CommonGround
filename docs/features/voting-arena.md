# Voting Arena

The landing page and core product experience. The user is shown **all** competing Patterns of a Topic at once and picks their single favorite, or skips the whole Topic. Each Vote updates the Patterns' rankings. Matchups stream in batches so voting flows without interruption.

**Roles:** USER / ADMIN
**Status:** Implemented

## Requirements

1. Given an authenticated user, when they open `/`, then a matchup is shown immediately — the Topic question, its Labels, a vote count, and a card per active Pattern with syntax-highlighted code.
2. Given a matchup, when the user clicks a Pattern card, then a Vote is recorded with that Pattern as winner over all the others shown, and the next matchup renders without a page reload.
3. Given a matchup, when the user picks "No preference" or "Not familiar enough", then a Skip is recorded for the whole Topic with that reason and no ranking changes.
4. Given a Vote, when it is recorded, then the winner's ELO climbs against each beaten Pattern and each beaten Pattern's ELO drops, while `timesShown` increments on every shown Pattern and `timesChosen` increments on the winner — atomically.
5. Given a session, when the user votes, then a progress indicator shows their position in the batch (e.g. "3 / 10"); when the batch is exhausted a fresh batch loads or an "all caught up" state shows.
6. Given the same user, when matchups are generated, then Topics they have already voted on or skipped are not shown again, so each Topic is seen once.

## How to use

Open the **Vote** tab (the landing page). Read the question, compare all the code snippets, and click your favorite — or use a Skip button if you genuinely have no preference or aren't familiar enough to judge. The next matchup appears instantly.

## API

| Method & path | Role | Purpose |
|---|---|---|
| `GET /api/voting/matchup` | USER | A single matchup (Topic + all its active Patterns) |
| `GET /api/voting/matchups?count=` | USER | A batch of matchups for client-side cycling |
| `POST /api/voting/vote` | USER | Record a Vote (winner Pattern id + the beaten Pattern ids); updates ELO & counters |
| `POST /api/voting/skip` | USER | Record a Skip (Topic id + SkipReason) |

## Technical details

1. ELO: expected = `1 / (1 + 10^((opponent - player) / 400))`, new = `old + K * (actual - expected)`, K = 32, initial rating 1500. Picking one favorite is treated as the winner beating each other shown Pattern: each pairwise delta is computed against the pre-vote ratings (so it is order-independent) and the winner's gains are summed. Two Patterns reduce this to a single head-to-head outcome.
2. Win rate is stored as counters on each Pattern: `timesChosen / timesShown`. A Vote increments `timesShown` on every shown Pattern and `timesChosen` on the winner. Skips touch neither counter.
3. Matchup generation lists eligible Topics (≥2 active Patterns) the user has not yet voted on or skipped, then returns every active Pattern of each in random order. SkipReason is a fixed enum (`NO_PREFERENCE`, `NOT_FAMILIAR`).
4. The Vote and all rating/counter updates happen in one transaction. The frontend fetches a batch of matchups upfront and posts each Vote/Skip individually as it is cast.

## Related features

- [Expertise Filter](expertise-filter.md) — restricts which Topics appear in matchups
- [Vote Comments & Voting Streak](vote-comments-and-streak.md) — optional comment and the daily streak shown here
- [Rankings Leaderboard](rankings-leaderboard.md) — where the accumulated ELO / win rate is surfaced

## History

- Originally pairwise (two Patterns per matchup). Switched to showing all Patterns of a Topic at once; see [ADR 0001](../adr/0001-all-patterns-voting.md).
