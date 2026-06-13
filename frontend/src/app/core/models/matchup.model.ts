import { Pattern } from './pattern.model';
import { Topic } from './topic.model';

export interface Matchup {
  topic: Topic;
  patternA: Pattern;
  patternB: Pattern;
  topicVoteCount: number;
}

export type SkipReason = 'NO_PREFERENCE' | 'NOT_FAMILIAR';

export interface VoteResult {
  voteId: number;
  winnerNewRating: number;
  loserNewRating: number;
  currentStreak: number;
}
