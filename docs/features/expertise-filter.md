# Expertise Filter

Users declare which programming languages they know, and the Voting Arena then only shows them Topics tagged with those languages. This keeps a TypeScript developer from being asked to judge Kotlin-specific standards they can't fairly evaluate.

**Roles:** USER / ADMIN
**Status:** Implemented

## Requirements

1. Given a user, when they open the expertise panel, then they can select from the available LANGUAGE Labels and see their current selection.
2. Given a user with expertise set, when matchups are generated, then they only receive Topics tagged with at least one of their LANGUAGE Labels.
3. Given a user with no expertise set, when matchups are generated, then no filter is applied (all Topics are eligible).
4. Given the expertise selection changes, when saved, then the navbar's open-Topic count updates immediately without a page reload.
5. Given a request to set expertise to a non-LANGUAGE Label, when submitted, then it is rejected with 400.

## How to use

In the navbar's expertise strip, your current languages show as removable chips. Click **+ Add expertise** to open a panel of available languages and toggle them on or off. The count of "topics open for you" updates as you change the selection.

## API

| Method & path | Role | Purpose |
|---|---|---|
| `GET /api/users/me/expertise` | USER | The user's selected LANGUAGE Labels |
| `PUT /api/users/me/expertise` | USER | Replace the selection with the given Label ids (LANGUAGE only; else 400) |
| `GET /api/voting/open-topic-count` | USER | How many matchup-eligible Topics match the current filter |

## Technical details

1. Expertise is a many-to-many join (`user_expertise`) between User and Label, restricted to LabelType `LANGUAGE` in v1.
2. The matchup generator filters candidate Topics by the user's expertise Labels; an empty selection means "no filter".
3. The selection set is fetched eagerly with the User since it is needed on most matchup requests.

## Related features

- [Voting Arena](voting-arena.md) — consumes this filter when generating matchups
- [Dataset Catalog](dataset-catalog.md) — where the Labels themselves can be browsed
