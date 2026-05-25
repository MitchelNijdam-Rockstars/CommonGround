# 02 — Authentication: Cloudflare Access JWT + User Roles

## What to build

Implement end-to-end authentication using Cloudflare Access as the identity provider. Cloudflare handles the One-Time PIN flow externally and injects a signed JWT into every request via the `Cf-Access-Jwt-Assertion` header.

The backend verifies this JWT using Cloudflare's public key, extracts the authenticated email, and looks up or creates a User record for that email. The User's role (`USER` or `ADMIN`) is resolved from a config-defined allowlist of admin email addresses in `application.yaml` — if the email is in the list the User is an Admin, otherwise they are a regular User.

In local development, a bypass mechanism allows requests without a Cloudflare header to proceed as a configurable test User (controlled by a Spring profile flag), so the rest of the app can be developed without a live Cloudflare tunnel.

On the frontend, an Angular auth service reads the current User (name, email, role) from a `/api/users/me` endpoint. An auth guard redirects unauthenticated users to a login/holding page. An HTTP interceptor attaches any necessary credentials to every API request.

## Acceptance criteria

- [ ] `GET /api/users/me` returns the authenticated User's email and role; returns 401 if no valid JWT is present
- [ ] An email listed in the admin allowlist receives role `ADMIN`; all other authenticated emails receive role `USER`
- [ ] A User record is created on first login and reused on subsequent logins (upsert by email)
- [ ] The local development bypass allows the app to run fully without Cloudflare — configurable via the `local` Spring profile
- [ ] The Angular auth guard redirects to a login/holding page for unauthenticated requests
- [ ] The navbar displays the authenticated User's avatar or initials in the top-right corner
- [ ] Logging out clears the session and returns the user to the holding page

## Blocked by

- [01 — Project Foundation & Dark Theme](01-foundation-dark-theme.md)
