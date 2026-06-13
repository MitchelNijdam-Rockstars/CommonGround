# Application Shell & Dark Theme

The frame every page lives in: a dark-only visual theme, a persistent top navigation bar with the main tabs (Vote, Catalog, Rankings & Insights), and the routing that moves between them. It is the first thing a user sees and the structure all other features plug into.

**Roles:** USER / ADMIN
**Status:** Implemented

## Requirements

1. Given any page, when it loads, then it renders on a near-black background with no white flash.
2. Given the navbar, when a route is active, then its tab is visually highlighted.
3. Given an ADMIN user, when the navbar renders, then an extra "Review" tab is shown that regular users do not see.
4. Given an unknown path, when navigated to, then the user is redirected to the Voting Arena.
5. Given the running backend, when `GET /api/health` is called, then it returns HTTP 200 with a status payload (smoke test for app + proxy wiring).

## How to use

The shell is always present. Use the tabs in the top bar to switch between **Vote** (`/`), **Catalog** (`/catalog`) and **Rankings & Insights** (`/rankings`). The avatar in the top-right opens the account menu.

## API

| Method & path | Role | Purpose |
|---|---|---|
| `GET /api/health` | Public | Liveness check; returns `{ "status": "UP" }` |

## Technical details

1. Tailwind v4 with a custom dark-only palette defined as design tokens in `frontend/src/styles.css` (`@theme`): `background`, `surface`, `edge`, `primary` (cyan/teal), `accent` (purple/pink), `streak` (amber), `ink`/`ink-muted`. No light mode.
2. Angular standalone components with lazy-loaded routes; the SPA is built by Gradle into the Spring Boot static resources, and a server-side fallback serves `index.html` for unknown non-API paths so deep links survive a refresh.
3. Lucide icons are registered centrally in `app.config.ts` and referenced by kebab-case name.

## Related features

- [Authentication & User Roles](authentication.md) — drives the avatar menu and the ADMIN-only Review tab
- [Expertise Filter](expertise-filter.md) — renders its chips in the navbar
