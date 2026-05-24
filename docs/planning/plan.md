# Common Ground — Product Plan

## Problem Statement

1. Engineering teams lack agreed-upon coding standards, resulting in inconsistent codebases that are harder to maintain, review, and onboard into.
2. Coding standards imposed top-down are frequently distrusted or ignored by developers who had no say in defining them — democratizing the process builds buy-in.
3. AI coding agents need explicit, structured style guidance to produce code that matches a team's actual preferences. This guidance is currently absent, manually maintained, or never updated.
4. There is no community-wide, data-driven source of truth for preferred coding practices across languages and frameworks — only blog posts, style guides, and tribal knowledge.

---

## Solution

Common Ground is a voting platform where developers compare competing coding Patterns head-to-head. Two random Patterns from the same Topic are shown at a time; the developer casts a Vote or Skips with a reason. Votes accumulate into a ranked leaderboard (ELO or win rate) that reflects genuine team or community consensus. The top-ranked Patterns per Topic can be exported as a Markdown file for direct use as AI coding agent instructions.

The platform is designed to run as independent instances — one for a company's internal team, one for the public developer community — each with its own curated dataset of Topics and Patterns.

---

## User Stories

### Authentication & Identity

1. As a User, I want to authenticate with a one-time PIN sent to my email address, so that I can access the platform without managing a password.
2. As a User, I want my role (USER or ADMIN) to be assigned automatically based on my email address, so that I do not need to request elevated access manually.
3. As a User, I want to remain logged in across browser sessions, so that I do not have to authenticate on every visit.
4. As a User, I want to be redirected to the login page if I am not authenticated, so that protected actions are not accessible without an account.

### Expertise & Personalisation

5. As a User, I want to select which programming languages I am familiar with from a managed list, so that I only see Topics relevant to my expertise.
6. As a User, I want to update my language expertise selection at any time, so that my voting feed stays relevant as my skills grow.
7. As a User, I want to see how many open Topics match my current expertise filter, so that I know how much I can contribute before running out of matchups.

### Voting Arena (Landing Page)

8. As a User, I want the landing page to immediately present a voting matchup without requiring navigation, so that I can start contributing the moment I arrive.
9. As a User, I want to see the Topic question and optional baseline context before the Patterns are shown, so that I understand what I am comparing.
10. As a User, I want to see the Labels attached to a Topic on the voting screen, so that I know which domain the standard belongs to.
11. As a User, I want to see exactly two Patterns side by side as code blocks with syntax highlighting, so that I can make a focused, concrete comparison.
12. As a User, I want to click on a Pattern card to cast my Vote, so that my preference is recorded immediately without extra confirmation steps.
13. As a User, I want the next matchup to load immediately after I cast a Vote, so that my voting session flows without interruption.
14. As a User, I want to skip a matchup by selecting "No preference", so that genuine ties are recorded accurately rather than forcing an arbitrary choice.
15. As a User, I want to skip a matchup by selecting "Not familiar enough", so that my knowledge gaps do not skew the rankings of Patterns I cannot fairly judge.
16. As a User, I want to see my progress through a voting session (e.g. "Matchup 3 of 10"), so that I know how many matchups remain in my current batch.
17. As a User, I want to see my current voting streak in days, so that I am motivated to return and vote consistently.
18. As a User, I want to see how many other engineers have voted on a Topic, so that I can trust the rankings are based on meaningful participation.
19. As a User, I want the two Patterns shown to be randomly selected from all available Patterns for that Topic, so that no Pattern is systematically advantaged by being paired only with weak opponents.
20. As a User, I want to optionally add a short comment when casting a Vote explaining my reasoning, so that my rationale contributes to the team's understanding of the winning Pattern.

### Dataset Catalog

21. As a User, I want to browse all approved Topics in a searchable list, so that I can find standards relevant to my current work.
22. As a User, I want to filter the Topic list by Label, so that I can focus on a specific language, framework, or architecture area.
23. As a User, I want to expand a Topic in the catalog to see all its approved Patterns simultaneously, so that I can understand the full range of competing options.
24. As a User, I want to submit a PatternSuggestion for an existing Topic when my preferred approach is not listed, so that my coding style enters the voting pool after review.
25. As a User, I want to submit a TopicSuggestion when a whole subject area is missing from the dataset, so that new coding standard decisions can be added.
26. As a User, I want to see the current status (pending, approved, rejected) of PatternSuggestions I have submitted, so that I know whether my contribution was accepted.
27. As a User, I want to see the rejection reason when a suggestion is declined, so that I understand the feedback and can improve future suggestions.

### Rankings & Insights

28. As a User, I want to see a leaderboard of Patterns ranked by score, grouped by Topic, so that I can view the current consensus for every coding standard at a glance.
29. As a User, I want to toggle the ranking algorithm between ELO rating and win rate percentage, so that I can evaluate the data from different perspectives.
30. As a User, I want the selected ranking algorithm to be reflected in the page URL, so that I can share a link to the exact view I am looking at.
31. As a User, I want to click into any Pattern on the leaderboard to see its full code and anonymised voter comments, so that I understand why the community prefers it.
32. As a User, I want to export the top-ranked Pattern per Topic as a structured Markdown file, so that I can feed the results directly into an AI coding agent as style instructions.

### Admin — Dataset Management

33. As an Admin, I want to create a new Topic with a question, optional context, and Labels, so that I can expand the dataset with new coding standard decisions.
34. As an Admin, I want to add Patterns to an existing Topic, so that the Topic has at least two competing options in the voting pool.
35. As an Admin, I want to deactivate a Pattern without deleting it, so that poor-quality options are removed from voting while historical vote data is preserved.
36. As an Admin, I want to manage the Label vocabulary (create, rename), so that the controlled list of Labels stays clean and consistent.
37. As an Admin, I want to assign Labels to Topics, so that users can filter effectively by expertise and domain.
38. As an Admin, I want to import a batch of Topics and Patterns from a structured file, so that I can seed the dataset from an AI codebase analysis without manually entering each item.

### Admin — Suggestion Review

39. As an Admin, I want to see all pending PatternSuggestions in a dedicated review view, so that I can process community contributions efficiently.
40. As an Admin, I want to approve a PatternSuggestion, so that it becomes an active Pattern in the voting pool with an initial score of zero.
41. As an Admin, I want to reject a PatternSuggestion with an optional written reason, so that the submitter understands why it was declined.
42. As an Admin, I want to see all pending TopicSuggestions in the review view, so that I can evaluate whether new subject areas should be added.
43. As an Admin, I want to approve a TopicSuggestion to create a new Topic, so that community-identified gaps in the dataset are filled.
44. As an Admin, I want to reject a TopicSuggestion with an optional written reason, so that off-topic or duplicate subjects are clearly declined with context.

---

## Implementation Decisions

### Modules to build

**Backend** (Kotlin + Spring Boot, feature-based package structure):

| Module | Responsibility |
|---|---|
| `user` | User entity, email lookup or creation on login, role resolution from config |
| `label` | Label entity, LabelType enum, Admin CRUD |
| `topic` | Topic entity, Admin CRUD, User browse and search |
| `pattern` | Pattern entity per Topic, active/inactive flag, ELO score and win rate fields |
| `voting` | Vote and Skip entities, matchup generation, ELO computation on vote submission |
| `suggestion` | PatternSuggestion and TopicSuggestion entities, PENDING/APPROVED/REJECTED state machine, approval actions |
| `ranking` | Read-focused: ranked Patterns by Topic, algorithm selection, Markdown export |
| `common` | Security config (Cloudflare Access JWT verification), CORS, Jackson config, web MVC config |

**Frontend** (Angular 21 standalone components):

| Module | Responsibility |
|---|---|
| `core` | Auth service, HTTP interceptors (attach auth token), shared TypeScript models |
| `layout/navbar` | Top navigation: Vote / Catalog / Rankings tabs, user avatar, expertise chip selector |
| `features/voting` | Landing page — Topic question, two Pattern cards, Vote and Skip actions, session progress, streak |
| `features/catalog` | Topic browser — search, Label filter, expandable Pattern list, PatternSuggestion and TopicSuggestion forms, suggestion status view |
| `features/rankings` | Leaderboard — algorithm toggle, Pattern detail overlay with code and comments, export trigger |

### Technical decisions

**Authentication**: Cloudflare Access handles the OTP flow externally. The backend trusts the `Cf-Access-Jwt-Assertion` header, verifies its signature using the Cloudflare public key, and extracts the authenticated email. It then looks up or creates a User record for that email and resolves the role from a config-defined list of admin email addresses.

**Schema management**: All schema changes via Flyway migrations. JPA runs with `ddl-auto: validate`. No entity changes without a corresponding migration.

**ELO computation**: Computed server-side on every Vote. Initial rating: 1500. K-factor starts at 32 (standard for new players). Both the winner's and loser's ratings are updated atomically in the same transaction as the Vote record.

**Win rate**: Stored as two counters on the Pattern — `timesShown` (incremented on both patterns in every Vote) and `timesChosen` (incremented on the winning Pattern). Win rate = `timesChosen / timesShown`. Skips do not update either counter.

**Matchup generation**: The backend selects a Topic at random (filtered by the User's LANGUAGE expertise), then selects two Patterns at random from that Topic. The same pair is never served twice to the same User in the same session. The same topic is never served to the same User twice in the same session.

**Rankings API**: A single `/api/rankings` endpoint accepts an `algorithm` query param (`ELO` or `WIN_RATE`). Returns Topics with their Patterns sorted by the selected metric.

**Markdown export**: A dedicated `/api/rankings/export` endpoint streams a Markdown file. Structure: one `##` heading per Topic (the question), followed by the winning Pattern's title and code block.

**Suggestion approval**: Approving a PatternSuggestion creates a new Pattern entity (status: active, ELO: 1500, counters: 0) and transitions the suggestion to APPROVED. Approving a TopicSuggestion creates a new Topic entity (no Patterns initially). The suggestion itself is never promoted — it remains a historical record.

**User expertise**: Stored as a join between User and Label, restricted to Labels with type `LANGUAGE` in v1. Managed via a dedicated `/api/users/me/expertise` endpoint.

**Dark mode**: Tailwind configured with a custom dark-only color palette. No light mode. Accent palette: cyan/teal for primary actions, purple/pink for gradients and highlights, amber for streak indicators.

**Syntax highlighting**: A syntax highlighting library (selected at implementation time) renders Pattern code blocks. The language for highlighting is derived from the Topic's LANGUAGE Label.

**Voting session**: The frontend fetches a batch of matchups (e.g. 10) in one request. The user cycles through them client-side. Each Vote or Skip is posted individually as it is cast.

**Skip UI**: Two distinct labelled buttons per matchup — "No preference" and "Not familiar enough" — rather than a single generic Skip action. Each maps to a SkipReason enum value.

**Algorithm toggle persistence**: The selected ranking algorithm is stored as a URL query parameter (`?algorithm=ELO` or `?algorithm=WIN_RATE`) so the view is bookmarkable and shareable.

**Vote comments**: Stored on the Vote record and tied to the User internally. Displayed in the Pattern detail view on the Rankings page with no attribution (anonymised). This allows spam detection without exposing commenter identity.

**Tests**:
- Backend `voting` module: ELO computation correctness, win rate counter updates, matchup generation (no duplicate pairs per session)
- Backend `suggestion` module: state machine transitions (valid and invalid), approval side effects (Pattern/Topic creation)
- Frontend `voting` feature: Vote submission flow, Skip submission flow, session progression through a batch

### All frontend-facing endpoints prefixed `/api`

### No interfaces without multiple implementations

---

## Out of Scope

- **AI codebase scanner**: Automated analysis of a codebase to generate Topics and Patterns is a separate tool; importing its output via a structured file is in scope.
- **Label types beyond LANGUAGE for expertise filtering**: FRAMEWORK, ARCHITECTURE, PARADIGM, and STYLE Labels exist in the dataset but are not used for the expertise filter in v1.
- **Light mode**: The UI is dark mode only; no theme toggle.
- **Notifications**: No email alerts for suggestion approval or rejection — users check status in the Catalog.
- **Anonymous voting**: Every Vote and Skip is tied to a User. Anonymous participation is not supported.
- **User-defined SkipReasons**: SkipReason is a fixed enum; users cannot add custom reasons.
- **Social features**: No public profiles, no following, no activity feeds.
- **Mobile native apps**: The web app is mobile-friendly but no native iOS/Android apps are planned.
- **Multi-tenancy**: Each deployment instance has its own isolated dataset. There is no shared user pool between company and public instances.
- **Admin-triggered AI scanner**: Triggering codebase analysis from within the Admin panel is deferred.

---

## Further Notes

- The platform is intentionally designed for independent deployment: spin up one instance for a company team, another for the public community. Dataset separation is by deployment, not by tenant flag.
- The Cloudflare Access + OTP setup means zero password management. Role assignment is purely config-driven (a list of admin emails in `application.yaml`).
- ELO K-factor of 32 is a starting point. After observing real vote distributions, it may need tuning — 16 for experienced voters with many matches, or higher to allow faster convergence on small datasets.
- The Markdown export format should be specified as a schema before implementation, as it is the primary integration point with AI coding agents and must be stable.
- The batch import format (for seeding from AI codebase analysis) should likewise be defined as a JSON schema before the import feature is built.
- Vote comments are optional. The UI should make it easy to skip leaving a comment without friction — the goal is maximising votes cast, not maximising comments.
- The "Your streak" feature requires tracking the last vote date per User. A streak resets if a User goes a full calendar day without casting any Vote.
