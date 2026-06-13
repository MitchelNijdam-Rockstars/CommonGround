# Dataset Catalog

A searchable, filterable browser of every approved Topic and its competing Patterns. Users come here to understand which coding-standard decisions exist, find ones relevant to their work, and read the full range of competing approaches side by side.

**Roles:** USER / ADMIN
**Status:** Implemented

## Requirements

1. Given the Catalog page, when it loads, then it lists all Topics with their question and attached Labels (colour-coded by LabelType).
2. Given the search box, when the user types, then the Topic list narrows to questions matching the text.
3. Given a Label filter chip, when the user selects it, then the list narrows to Topics carrying that Label.
4. Given a Topic, when the user expands it, then its active Patterns are shown as syntax-highlighted code blocks.
5. Given an inactive (deactivated) Pattern, when a Topic is expanded, then that Pattern is not shown.

## How to use

Open the **Catalog** tab. Type in the search box to find a Topic by its question, or tap a Label chip to filter by language/framework/etc. Click a Topic to expand it and read its competing Patterns.

## API

| Method & path | Role | Purpose |
|---|---|---|
| `GET /api/topics?search=&labelId=` | USER | List Topics with Labels; optional full-text search on the question and filter by Label id |
| `GET /api/labels?type=` | USER | List Labels, optionally filtered by LabelType |
| `GET /api/topics/{topicId}/patterns` | USER | List only the active Patterns of a Topic |

## Technical details

1. Search and label filtering are applied server-side; the frontend debounces the search input.
2. Patterns are loaded lazily per Topic on first expand and cached client-side.
3. Code blocks are highlighted with highlight.js; the language comes from the Pattern's `language` field.
4. Labels are rendered with a shared badge component whose colour is keyed to the LabelType.

## Related features

- [Dataset Administration](dataset-administration.md) — how Labels, Topics, and Patterns get into the catalog
- [Pattern & Topic suggestions](pattern-suggestions.md) — users propose new Patterns from within an expanded Topic
- [Voting Arena](voting-arena.md) — Topics with two or more active Patterns become matchups
