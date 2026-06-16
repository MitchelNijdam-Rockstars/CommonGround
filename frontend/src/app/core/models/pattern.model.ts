export interface Pattern {
  id: number;
  topicId: number;
  title: string;
  code: string;
  language: string | null;
  eloRating: number;
  timesShown: number;
  timesChosen: number;
  winRate: number | null;
  active: boolean;
}
