# 10 — Rankings Leaderboard

## What to build

Deliver the Rankings & Insights page — a leaderboard that surfaces the current community consensus for every Topic, grouped by Topic and sortable by two ranking algorithms.

The backend exposes a rankings endpoint that returns all Topics with their active Patterns sorted by the selected algorithm. Two algorithms are supported:

- **ELO**: Patterns sorted by their current ELO rating, descending.
- **WIN_RATE**: Patterns sorted by `timesChosen / timesShown`, descending. Patterns with zero `timesShown` are ranked last.

The frontend renders the Rankings page with Topics as sections, each showing their Patterns in ranked order. A toggle at the top switches between ELO and WIN_RATE. The selected algorithm is persisted in the URL query parameter (`?algorithm=ELO`), making the view bookmarkable and shareable. The default algorithm when no query param is present is ELO.

## Acceptance criteria

- [ ] `GET /api/rankings?algorithm=ELO` returns all Topics with their active Patterns sorted by ELO rating descending
- [ ] `GET /api/rankings?algorithm=WIN_RATE` returns all Topics with their active Patterns sorted by win rate descending; Patterns with no votes appear last
- [ ] An unknown `algorithm` value returns 400
- [ ] The Rankings page renders Topics as sections with Patterns shown in ranked order under each
- [ ] A toggle (or segmented control) switches between ELO and WIN_RATE views
- [ ] Switching the algorithm updates the URL query parameter without a full page reload
- [ ] Loading the page with `?algorithm=WIN_RATE` in the URL applies that algorithm immediately
- [ ] Loading the page with no algorithm param defaults to ELO
- [ ] Each Pattern entry shows its rank position, title, and a compact code preview
- [ ] Each Topic section shows the total number of Votes cast across all its Patterns

## Blocked by

- [05 — Voting Arena: Core](05-voting-arena-core.md)
