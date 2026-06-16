import { Label } from './label.model';

export type SuggestionStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface PatternSuggestion {
  id: number;
  topicId: number;
  topicQuestion: string;
  title: string | null;
  code: string;
  language: string | null;
  submittedBy: string;
  status: SuggestionStatus;
  rejectionReason: string | null;
  createdAt: string;
}

export interface TopicSuggestionPattern {
  id: number;
  title: string | null;
  code: string;
}

export interface TopicSuggestion {
  id: number;
  question: string;
  context: string | null;
  language: string | null;
  labels: Label[];
  patterns: TopicSuggestionPattern[];
  submittedBy: string;
  status: SuggestionStatus;
  rejectionReason: string | null;
  createdTopicId: number | null;
  createdAt: string;
}
