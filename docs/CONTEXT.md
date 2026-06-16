# Common Ground

A voting platform that democratizes coding standards. Developers vote on competing coding style options; the results are ranked by algorithm and can be exported as a Markdown file for AI coding agents.

## Language

### Core voting concepts

**Topic**:
A subject area representing a coding standard decision to be made (e.g., "Null handling in Kotlin"). A Topic groups N Patterns and carries a question, optional baseline context, an optional language (shared by all its Patterns, used for code-fence highlighting and export), and Labels.
_Avoid_: Dilemma, Question, Subject

**Pattern**:
A single competing coding style option that belongs to a Topic. Patterns are what get ranked; the winning Pattern of a Topic is the highest-ranked one. A Pattern carries code and a title but no language of its own — the language lives on its Topic.
_Avoid_: Option, Approach, Variant, Style, Alternative

**Label**:
A tag attached to a Topic from a managed vocabulary, used to filter and browse Topics. Each Label has a LabelType.
_Avoid_: Tag, Category

**LabelType**:
The classification of a Label. Fixed enum: `LANGUAGE`, `FRAMEWORK`, `ARCHITECTURE`, `PARADIGM`, `STYLE`.
_Avoid_: LabelKind, LabelCategory

### Voting actions

**Vote**:
A record of a User picking one favorite Pattern out of all the Patterns shown for a Topic. Captures the winning Pattern, the Patterns it beat (the other options shown), and the voter. The winner is treated as beating each of those Patterns, so ELO and win rate are still computed from pairwise outcomes — one outcome when two Patterns are shown, N-1 when all are.
_Avoid_: Choice, Selection, Pick

**Skip**:
A record of a User declining to pick a favorite for a Topic. Captures the Topic, the voter, and a mandatory SkipReason. The whole Topic is skipped, not an individual pair.
_Avoid_: Pass, Abstain

**SkipReason**:
The reason a User skipped a Topic. Fixed enum: `NO_PREFERENCE` (saw the options, genuinely no preference), `NOT_FAMILIAR` (insufficient domain knowledge to judge).
_Avoid_: SkipType, SkipCause

### Dataset management

**PatternSuggestion**:
A candidate Pattern submitted by a User for an existing Topic. Moves through states: `PENDING` → `APPROVED` or `REJECTED`. Approval creates a real Pattern in the voting pool with zero votes; it does not promote the suggestion itself.
_Avoid_: Suggestion (alone — too ambiguous), OptionSuggestion

**TopicSuggestion**:
A candidate Topic submitted by a User when a whole subject area is missing from the dataset. Moves through the same states as PatternSuggestion. It may carry candidate Patterns (see TopicSuggestionPattern) submitted in the same step. Approval creates a new Topic as one unit: the Topic plus a real Pattern for each candidate Pattern.
_Avoid_: Suggestion (alone — too ambiguous)

**TopicSuggestionPattern**:
A candidate Pattern submitted together with a TopicSuggestion, before the Topic exists. It has no language of its own (that lives on the suggestion/Topic) and becomes a real Pattern when the TopicSuggestion is approved.
_Avoid_: Suggestion (alone — too ambiguous); PatternSuggestion (that is a candidate for an *existing* Topic)

### Users

**User**:
A person authenticated via Cloudflare One-Time PIN who interacts with the system. Has a role attribute: `USER` (can vote, view rankings, submit suggestions) or `ADMIN` (all User privileges plus approving/rejecting suggestions, managing Topics, Labels, and Patterns).
_Avoid_: Voter, Developer, Participant

## Example dialogue

> **Dev**: "I want to add a new option for how we handle exceptions — can I just add a Pattern directly?"
>
> **Domain expert**: "No — you'd submit a PatternSuggestion for the Exception Handling Topic. An Admin reviews it and approves it, at which point the Pattern enters the voting pool."
>
> **Dev**: "What if there's no Topic for it yet?"
>
> **Domain expert**: "Then you submit a TopicSuggestion — and you can attach your candidate Patterns to it right away. When an Admin approves it, the Topic and all those Patterns are created together in one step."
>
> **Dev**: "And when I vote, what exactly am I doing?"
>
> **Domain expert**: "You're shown all the Patterns for one Topic at once. You either cast a Vote — picking your single favorite, which is treated as beating every other shown Pattern — or you Skip the whole Topic with a reason. Every Vote updates the ELO score and win rate of all the Patterns involved."

## Flagged ambiguities

- **"Suggestion"** used alone is ambiguous — always qualify as PatternSuggestion or TopicSuggestion.
- **"Option"** must not be used as a domain term; it collides with general UI vocabulary and was considered and rejected in favour of Pattern.
