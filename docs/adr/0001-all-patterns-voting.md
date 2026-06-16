# Show all Patterns of a Topic at once, not two at a time

**Status:** accepted (reverses the original pairwise design)

The Voting Arena originally showed two Patterns from a Topic per matchup, chosen because
head-to-head comparisons make ELO and win rate trivial to compute. Real-world testing showed
two problems: the same Topic reappeared many times (once per unseen pair), which felt
repetitive, and users were forced to choose between two options when their real preference was a
third Pattern not on screen. We now show **all** active Patterns of a Topic at once and let the
user pick a single favorite.

## Why the rating model survives

Picking one favorite out of N is treated as the winner **beating each of the other N-1 shown
Patterns**. That decomposes into exactly the pairwise outcomes we already computed, applied N-1
times in one transaction (ELO deltas summed against the pre-vote ratings; `timesShown` on all,
`timesChosen` on the winner). The old two-Pattern case is just the N=2 special case, so ELO and
win rate are unchanged in spirit — only the unit of a Vote grew from one loser to a set of
beaten Patterns. A `Vote` now stores a winner plus a set of beaten Patterns
(`vote_beaten_pattern`), and a `Skip` is Topic-level. Each Topic is seen once per user (voted or
skipped Topics are hidden).

## Considered but not built

A per-user toggle between pairwise and all-Patterns voting. It is cheap to add later precisely
because the costly layers — Vote storage (winner + beaten set), the ELO/win-rate math, and the
arena rendering a Pattern list of any size — are already mode-agnostic. Adding it would only
require a `votingMode` on the User and a second selection/dedup strategy in `MatchupService`
(pairwise pair-keys vs Topic-level). We kept the baseline single-mode for now.
