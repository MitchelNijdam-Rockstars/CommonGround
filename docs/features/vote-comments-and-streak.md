# Vote Comments & Voting Streak

Two motivating touches on top of voting. While voting, a user can optionally add a short comment explaining their reasoning (shown anonymously later on the rankings). And a daily streak counter rewards consistent participation with a flame indicator.

**Roles:** USER / ADMIN
**Status:** Implemented

## Requirements

1. Given a matchup, when the user votes, then they may optionally include a short comment; the comment box is below the cards and never blocks or delays the Vote.
2. Given a comment longer than 500 characters, when submitted, then the Vote is rejected with 400.
3. Given a user votes on a day they have not yet voted, when the Vote lands, then their streak increments by one; if their previous vote was more than one calendar day ago, the streak resets to 1.
4. Given the user already voted today, when they vote again, then the streak is unchanged.
5. Given the Voting Arena, when the streak is greater than 1, then a flame indicator shows it (a streak of 0 or 1 is not highlighted).
6. Given `GET /api/users/me`, when called, then it includes the user's current streak (stale streaks read as 0).

## How to use

Below the two Pattern cards there's an optional "Why?" text area — type a sentence if you like, then click a card to vote (or just vote without commenting). Your current streak appears as a 🔥 indicator near the top of the arena once it passes one day.

## API

| Method & path | Role | Purpose |
|---|---|---|
| `POST /api/voting/vote` | USER | Accepts an optional `comment` (≤500 chars) alongside the Vote |
| `GET /api/users/me` | USER | Includes `currentStreak` in the response |

## Technical details

1. The comment is stored on the `vote` row (nullable, max 500) and tied to the user internally for spam detection, but it is surfaced without attribution on the [Pattern detail](pattern-detail.md) view.
2. Streak state lives on the User as `lastVotedDate` and `currentStreak`, updated inside the Vote transaction. A streak is "effective" only if the last vote was today or yesterday; otherwise it displays as 0.
3. The Vote response returns the freshly computed `currentStreak` so the arena can update instantly.

## Related features

- [Voting Arena](voting-arena.md) — where comments are entered and the streak is shown
- [Pattern Detail & Anonymised Comments](pattern-detail.md) — where comments are displayed (anonymised)
