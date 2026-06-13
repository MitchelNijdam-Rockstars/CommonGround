# Features

Per-feature documentation for Common Ground. Each file follows [`feature-template.md`](feature-template.md): a user-facing description, requirements, how to use, the API it exposes, key technical details, and links to related features.

## Index

| Feature | Roles | Summary |
|---|---|---|
| [Application Shell & Dark Theme](application-shell.md) | USER / ADMIN | Dark theme, navbar, routing, health check |
| [Authentication & User Roles](authentication.md) | USER / ADMIN | Cloudflare OTP login; USER/ADMIN role resolution |
| [Dataset Catalog](dataset-catalog.md) | USER / ADMIN | Browse, search, and filter Topics; expand to view Patterns |
| [Dataset Administration](dataset-administration.md) | ADMIN | Manage Labels, Topics, and Patterns |
| [Voting Arena](voting-arena.md) | USER / ADMIN | Head-to-head Pattern voting, Skips, ELO & win rate |
| [Expertise Filter](expertise-filter.md) | USER / ADMIN | Restrict matchups to chosen languages |
| [Vote Comments & Voting Streak](vote-comments-and-streak.md) | USER / ADMIN | Optional vote comment + daily streak |
| [Pattern Suggestions](pattern-suggestions.md) | USER / ADMIN | Suggest Patterns; admins approve/reject |
| [Topic Suggestions](topic-suggestions.md) | USER / ADMIN | Suggest Topics; admins approve/reject |
| [Rankings Leaderboard](rankings-leaderboard.md) | USER / ADMIN | Ranked Patterns per Topic; ELO/win-rate toggle |
| [Pattern Detail & Anonymised Comments](pattern-detail.md) | USER / ADMIN | Full code + anonymous voter comments |
| [Markdown Export](markdown-export.md) | USER / ADMIN | Export top Patterns as Markdown for AI agents |
| [Batch Import](batch-import.md) | ADMIN | Bulk-seed the dataset from a JSON file |

See also [`../CONTEXT.md`](../CONTEXT.md) for the domain language and [`../planning/plan.md`](../planning/plan.md) for the product plan.
