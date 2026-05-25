# 06 — Expertise Filter

## What to build

Allow Users to declare which programming languages they are familiar with, so that matchups on the Voting Arena only surface Topics tagged with their LANGUAGE Labels. This prevents a TypeScript developer from being shown Kotlin-specific Topics they cannot fairly judge.

The backend adds a join between User and Label (restricted to Labels with type `LANGUAGE`). A dedicated endpoint lets the frontend read and update a User's expertise selection. The matchup endpoint from slice 05 is updated to filter candidate Topics by the authenticated User's LANGUAGE Labels — if a User has no expertise set, all Topics are eligible (no filter applied).

On the frontend, the navbar displays the User's current expertise as removable chips (e.g. "Kotlin", "TypeScript"). An "+ Add expertise" button opens a panel to select from available LANGUAGE Labels. A counter shows how many Topics match the current filter. Changes take effect immediately without a page reload.

## Acceptance criteria

- [ ] Flyway migration creates the `user_expertise` join table linking Users to LANGUAGE-typed Labels
- [ ] `GET /api/users/me/expertise` returns the authenticated User's selected LANGUAGE Labels
- [ ] `PUT /api/users/me/expertise` replaces the User's expertise selection with the provided Label IDs; only LANGUAGE-typed Labels are accepted — others are rejected with 400
- [ ] The matchup endpoint filters candidate Topics to those tagged with at least one of the User's LANGUAGE Labels; if the User has no expertise set, no filter is applied
- [ ] The navbar renders the User's expertise as colored, removable chips matching LabelType color coding
- [ ] The expertise panel lists all available LANGUAGE Labels and reflects the current selection
- [ ] Adding or removing expertise updates the open-Topic count in the navbar immediately
- [ ] A User with expertise set never receives a matchup for a Topic with no matching LANGUAGE Label

## Blocked by

- [03 — Labels & Topic Browsing](03-labels-topic-browsing.md)
- [05 — Voting Arena: Core](05-voting-arena-core.md)
