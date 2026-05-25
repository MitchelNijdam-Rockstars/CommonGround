# 01 — Project Foundation & Dark Theme

## What to build

Establish the visual and structural foundation that every subsequent slice builds on. Configure Tailwind with a dark-only color palette matching the design reference: near-black background, cyan/teal as the primary action color, purple/pink for gradient accents, and amber for streak indicators. No light mode toggle — dark only.

On the frontend, wire up the Angular Router with placeholder routes for the three pages (Voting Arena `/`, Catalog `/catalog`, Rankings `/rankings`) and render the persistent navbar shell with the three navigation tabs. The navbar is the only UI component that appears on every page, so it must be structurally correct from the start.

On the backend, add a `/api/health` endpoint that returns the application status. This serves as a smoke-test that the Spring Boot app is running and the Angular proxy is wired correctly.

## Acceptance criteria

- [ ] Tailwind is configured with a custom dark-only palette; background, surface, and accent tokens are defined as design tokens (not hardcoded hex values inline)
- [ ] The Angular app renders with the dark background by default; no white flash on load
- [ ] The navbar displays three tabs: Vote, Catalog, Rankings & Insights; the active route tab is visually highlighted
- [ ] Navigating to `/`, `/catalog`, and `/rankings` renders each route's placeholder component without errors
- [ ] `GET /api/health` returns HTTP 200 with a simple status payload
- [ ] The Angular dev proxy forwards `/api/*` requests to the Spring Boot backend correctly in local dev
- [ ] Lucide icons are available and render correctly in the navbar

## Blocked by

None — can start immediately.
