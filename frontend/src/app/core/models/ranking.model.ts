import { Pattern } from './pattern.model';
import { Topic } from './topic.model';

export type RankingAlgorithm = 'ELO' | 'WIN_RATE';

export interface TopicRanking {
  topic: Topic;
  totalVotes: number;
  patterns: Pattern[];
}
