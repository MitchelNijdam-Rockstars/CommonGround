# Authentication & User Roles

Users sign in with a one-time PIN sent to their email — handled entirely by Cloudflare Access at the edge, so there are no passwords to manage. Once authenticated, each user is automatically a regular USER or an ADMIN based on a configured allowlist of admin emails.

**Roles:** USER / ADMIN
**Status:** Implemented

## Requirements

1. Given a valid Cloudflare session, when any API endpoint is called, then the backend resolves the authenticated email and the request proceeds.
2. Given no valid token, when a protected endpoint is called, then it returns 401; the frontend redirects the user to a holding/login page.
3. Given an email on the admin allowlist, when the user logs in, then they receive role ADMIN; otherwise USER.
4. Given a first-time email, when it logs in, then a User record is created; subsequent logins reuse it (upsert by email).
5. Given a non-ADMIN user, when an `/api/admin/**` endpoint is called, then it returns 403.
6. Given local development, when no Cloudflare header is present, then a configurable bypass authenticates a test user so the app runs without a tunnel.

## How to use

Visit any page. If you are not authenticated, you land on the holding page; complete the Cloudflare PIN flow and return. Your initials appear in the top-right avatar, which opens a menu showing your email, role, and a Sign out action.

## API

| Method & path | Role | Purpose |
|---|---|---|
| `GET /api/users/me` | USER | Returns the authenticated user's email, display name, role, current streak, and logout URL; 401 if unauthenticated |

## Technical details

1. Cloudflare Access injects a signed JWT in the `Cf-Access-Jwt-Assertion` header. A servlet filter verifies it against Cloudflare's JWKS (using `nimbus-jose-jwt`), extracts the email, and upserts the User; the role is re-resolved from the `commonground.auth.admin-emails` allowlist on every login.
2. The current user is injected into controllers via a `@CurrentUser` argument resolver; admin endpoints under `/api/admin/**` are guarded by an interceptor.
3. Local dev uses `commonground.auth.dev-bypass` (enabled in the `local` profile) to skip Cloudflare and act as a configured admin email. `/api/health` is exempt from auth.
4. Frontend: an auth service caches `GET /api/users/me`; an auth guard redirects unauthenticated users to `/login`; an HTTP interceptor sends credentials and routes 401s to the login page.

## Related features

- [Application Shell & Dark Theme](application-shell.md) — renders the avatar and role-gated tabs
- [Dataset Administration](dataset-administration.md) — gated behind the ADMIN role
