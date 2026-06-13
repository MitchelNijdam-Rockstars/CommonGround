import { Label } from './label.model';

export type SuggestionStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface PatternSuggestion {
  id: number;
  topicId: number;
  topicQuestion: string;
  title: string | null;
  code: string;
  language: string;
  status: SuggestionStatus;
  rejectionReason: string | null;
  createdAt: string;
}

export interface TopicSuggestion {
  id: number;
  question: string;
  context: string | null;
  labels: Label[];
  status: SuggestionStatus;
  rejectionReason: string | null;
  createdTopicId: number | null;
  createdAt: string;
}
