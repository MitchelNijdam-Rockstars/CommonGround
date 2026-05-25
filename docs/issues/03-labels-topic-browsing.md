# 03 — Labels & Topic Browsing

## What to build

Introduce the Label and Topic domain entities and deliver the Dataset Catalog page in read-only form.

A Label belongs to a LabelType (`LANGUAGE`, `FRAMEWORK`, `ARCHITECTURE`, `PARADIGM`, `STYLE`) and is part of a managed vocabulary controlled by Admins. A Topic has a question, optional baseline context, and one or more Labels. Both entities are stored via Flyway migrations.

Admins can create Labels and Topics via API. Regular Users can browse and search Topics. The Dataset Catalog page renders a searchable, filterable list of Topics — filtering by Label. Each Topic card shows its question and Labels; the Pattern list inside each Topic is not yet shown (that is slice 04).

## Acceptance criteria

- [ ] Flyway migration creates the `label` and `topic` tables (and the join table between them) in the `common_ground` schema
- [ ] `POST /api/admin/labels` creates a Label with a name and LabelType; Admin-only
- [ ] `GET /api/labels` returns all Labels, optionally filtered by LabelType
- [ ] `POST /api/admin/topics` creates a Topic with a question, optional context, and Label assignments; Admin-only
- [ ] `GET /api/topics` returns all Topics with their Labels, supporting full-text search on the question and filter by Label ID
- [ ] The Catalog page renders the Topic list with search input and Label filter chips
- [ ] Selecting a Label filter chip narrows the Topic list to matching Topics
- [ ] Typing in the search input filters Topics by question text
- [ ] Each Topic card displays its question and attached Labels with correct LabelType colors
- [ ] A User without the ADMIN role receives 403 on Admin-only endpoints

## Blocked by

- [02 — Authentication: Cloudflare Access JWT + User Roles](02-auth-cloudflare-user-roles.md)
