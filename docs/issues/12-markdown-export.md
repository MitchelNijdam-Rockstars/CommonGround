# 12 — Markdown Export

## What to build

Allow Users to export the current top-ranked Patterns as a structured Markdown file for use as AI coding agent style instructions. The export reflects the Rankings at the moment the request is made, using ELO as the default ranking algorithm (most stable signal).

The backend streams the Markdown file as a download. Structure: one `##` heading per Topic (the question text), followed by the winning Pattern's title and its code block with language-appropriate fencing. Topics with no Votes are omitted. The file is deterministic — the same dataset always produces the same output.

On the Rankings page, an "Export" button triggers the download. The exported filename includes the instance name and date for traceability (e.g. `common-ground-2026-05-24.md`).

## Acceptance criteria

- [ ] `GET /api/rankings/export` returns a Markdown file download with `Content-Disposition: attachment`
- [ ] The exported file contains one section per Topic that has at least one voted Pattern, headed by the Topic's question
- [ ] Each section contains the winning Pattern's title and full code block with correct language fence (e.g. ` ```kotlin `)
- [ ] Topics with no Votes are excluded from the export
- [ ] The filename includes the current date
- [ ] The "Export" button on the Rankings page triggers the file download without navigating away from the page
- [ ] The exported Markdown is valid and renders correctly in standard Markdown viewers

## Blocked by

- [10 — Rankings Leaderboard](10-rankings-leaderboard.md)
