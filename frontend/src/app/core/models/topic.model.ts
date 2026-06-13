import { Label } from './label.model';

export interface Topic {
  id: number;
  question: string;
  context: string | null;
  labels: Label[];
}
