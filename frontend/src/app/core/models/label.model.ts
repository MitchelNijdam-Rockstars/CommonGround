export type LabelType = 'LANGUAGE' | 'FRAMEWORK' | 'ARCHITECTURE' | 'PARADIGM' | 'STYLE';

export interface Label {
  id: number;
  name: string;
  labelType: LabelType;
}

/** Color coding per LabelType; full class strings so Tailwind picks them up. */
export const LABEL_TYPE_CLASSES: Record<LabelType, string> = {
  LANGUAGE: 'bg-primary/15 text-primary-soft border-primary/30',
  FRAMEWORK: 'bg-accent/15 text-accent border-accent/30',
  ARCHITECTURE: 'bg-accent-pink/15 text-accent-pink border-accent-pink/30',
  PARADIGM: 'bg-emerald-400/15 text-emerald-300 border-emerald-400/30',
  STYLE: 'bg-streak/15 text-streak border-streak/30',
};
