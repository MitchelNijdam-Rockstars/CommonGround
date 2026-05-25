# 05 — Voting Arena: Core (Vote, Skip, ELO, Matchup)

## What to build

Deliver the full end-to-end voting flow — the core product experience. This is the landing page that Users land on immediately after authentication.

The backend exposes a matchup endpoint that selects a random Topic (with at least two active Patterns) and returns two randomly selected Patterns from it. The same pair is not served twice to the same User within a session. When a User casts a Vote, the backend records the winning and losing Pattern, updates both Patterns' ELO ratings, and increments their `timesShown` and `timesChosen` counters. When a User submits a Skip, the backend records the two Patterns shown and the SkipReason — no ELO or counter update occurs for Skips.

ELO update formula: expected score = `1 / (1 + 10^((opponentRating - playerRating) / 400))`, new rating = `oldRating + K * (actual - expected)` where K=32, actual=1 for winner and 0 for loser.

On the frontend, the Voting Arena renders the Topic question, Labels, vote count, two Pattern cards with syntax-highlighted code, two Skip buttons ("No preference" / "Not familiar enough"), and a session progress indicator. Clicking a Pattern card immediately submits the Vote and loads the next matchup. The frontend fetches a batch of matchups upfront and cycles through them client-side.

Tests cover ELO computation correctness (expected rating changes for various starting ratings), matchup generation (no duplicate pairs per session), and Vote/Skip recording.

## Acceptance criteria

- [ ] Flyway migration creates `vote` and `skip` tables in the `common_ground` schema
- [ ] `GET /api/voting/matchup` returns a Topic and two randomly selected active Patterns from it; excludes pairs already seen by this User in the current session
- [ ] `POST /api/voting/vote` records a Vote with winner Pattern, loser Pattern, and authenticated User; updates ELO ratings and counters for both Patterns atomically
- [ ] `POST /api/voting/skip` records a Skip with both Patterns shown, the SkipReason, and authenticated User; does not modify ELO or counters
- [ ] ELO ratings are updated correctly: winner's rating increases, loser's rating decreases by a complementary amount, using K=32
- [ ] `timesShown` increments on both Patterns for every Vote; `timesChosen` increments only on the winning Pattern
- [ ] The Voting Arena landing page (`/`) displays the Topic question, Labels, a vote count, and two Pattern cards with syntax-highlighted code
- [ ] Clicking a Pattern card submits the Vote and immediately renders the next matchup without a full page reload
- [ ] "No preference" and "Not familiar enough" Skip buttons are clearly distinct and each submits with the correct SkipReason
- [ ] A session progress indicator shows the current matchup position (e.g. "3 / 10")
- [ ] When the batch is exhausted, the page fetches a fresh batch or shows an "all caught up" state
- [ ] Unit tests: ELO computation for multiple rating combinations; matchup uniqueness within a session
- [ ] Integration test: Vote submission updates both Patterns' ratings and counters in the database

## Blocked by

- [04 — Pattern Management](04-pattern-management.md)
