# Rankings Leaderboard

The Rankings & Insights page shows the current community consensus for every Topic: its Patterns listed in ranked order. Users can switch between two ranking algorithms — ELO rating or win-rate percentage — and the choice is reflected in the URL so a view can be bookmarked and shared.

**Roles:** USER / ADMIN
**Status:** Implemented

## Requirements

1. Given the Rankings page, when it loads with no algorithm in the URL, then it defaults to ELO and shows Topics as sections with their active Patterns in ranked order.
2. Given `?algorithm=WIN_RATE` in the URL, when the page loads, then Patterns are ranked by win rate descending, with zero-vote Patterns last.
3. Given the algorithm toggle, when switched, then the URL query parameter updates without a full page reload and the list re-sorts.
4. Given an unknown algorithm value, when the rankings endpoint is called, then it returns 400.
5. Given each Topic section, when rendered, then it shows the total number of Votes cast across its Patterns, and each Pattern entry shows its rank, title, and a compact code preview.

## How to use

Open the **Rankings & Insights** tab. Use the ELO / Win-rate toggle at the top to change how Patterns are ordered; the URL updates so you can copy and share the exact view. Each Topic is a section listing its Patterns best-first.

## API

| Method & path | Role | Purpose |
|---|---|---|
| `GET /api/rankings?algorithm=ELO\|WIN_RATE` | USER | Topics with active Patterns sorted by the chosen algorithm; 400 on an unknown value, defaults to ELO |

## Technical details

1. ELO sorts by `eloRating` descending. WIN_RATE sorts by `timesChosen / timesShown` descending; Patterns with zero `timesShown` (no win rate) rank last.
2. Only Topics with at least one active Pattern are included; per-Topic vote totals come from the vote count.
3. The algorithm is parsed from a query string so an invalid value yields a clean 400. Entity→DTO mapping happens inside a read-only service transaction.
4. Frontend persistence uses the Angular Router query parameter (`?algorithm=...`), applied on load and updated via navigation with merge handling.

## Related features

- [Voting Arena](voting-arena.md) — produces the ELO and win-rate data shown here
- [Pattern Detail & Anonymised Comments](pattern-detail.md) — opened by clicking a Pattern on this page
- [Markdown Export](markdown-export.md) — exports the top-ranked Pattern per Topic from this data
