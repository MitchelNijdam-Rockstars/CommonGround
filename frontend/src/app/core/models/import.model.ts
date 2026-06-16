export interface ImportPattern {
  title: string;
  code: string;
}

export interface ImportTopic {
  question: string;
  context?: string | null;
  language?: string | null;
  labels?: string[];
  patterns?: ImportPattern[];
}

export interface ImportRequest {
  topics: ImportTopic[];
}

export interface ImportResult {
  topicsCreated: number;
  topicsReused: number;
  patternsCreated: number;
  patternsSkipped: number;
  labelsCreated: number;
}
