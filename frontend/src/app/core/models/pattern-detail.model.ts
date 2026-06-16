export interface PatternComment {
  comment: string;
  createdAt: string;
}

export interface PatternDetail {
  id: number;
  topicId: number;
  title: string;
  code: string;
  language: string | null;
  eloRating: number;
  winRate: number | null;
  comments: PatternComment[];
}
