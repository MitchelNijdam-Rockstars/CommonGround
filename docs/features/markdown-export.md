# Markdown Export

Exports the current top-ranked Patterns as a structured Markdown file, ready to feed an AI coding agent as style instructions. One section per Topic with the winning Pattern's code — the primary integration point between Common Ground and the tools that consume its consensus.

**Roles:** USER / ADMIN
**Status:** Implemented

## Requirements

1. Given the Rankings page, when the user clicks Export, then a Markdown file downloads without navigating away from the page.
2. Given the export, when generated, then it contains one `##` section per Topic that has at least one voted Pattern, headed by the Topic's question.
3. Given each section, when rendered, then it contains the winning (highest-ELO) Pattern's title and its full code in a language-fenced block (e.g. ` ```kotlin `).
4. Given a Topic with no votes, when exporting, then it is excluded.
5. Given the download, when saved, then the filename includes the current date (e.g. `common-ground-2026-06-13.md`).
6. Given the same dataset, when exported twice, then the output is identical (deterministic).

## How to use

On the **Rankings & Insights** page, click **Export**. A dated `.md` file downloads. Drop it into your AI coding agent's instructions (or your repo) as the team's agreed style guide.

## API

| Method & path | Role | Purpose |
|---|---|---|
| `GET /api/rankings/export` | USER | Streams the Markdown file with `Content-Disposition: attachment` and a dated filename |

## Technical details

1. The winning Pattern per Topic is the highest by ELO (the most stable signal), with a deterministic tiebreak; Topics are emitted in a stable order so the output never changes for an unchanged dataset.
2. Only Topics with at least one voted Pattern (`timesShown > 0`) appear. The whole Markdown string is built inside a read-only service transaction.
3. The endpoint returns `text/markdown` with an `attachment` Content-Disposition; the frontend fetches it as a blob and triggers a client-side download, parsing the filename from the response header.

## Related features

- [Rankings Leaderboard](rankings-leaderboard.md) — hosts the Export button and supplies the ranked data
- [Voting Arena](voting-arena.md) — the votes that determine each Topic's winning Pattern
