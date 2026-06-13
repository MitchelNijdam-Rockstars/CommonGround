import { Pipe, PipeTransform } from '@angular/core';

const FORMATTER = new Intl.RelativeTimeFormat('en', { numeric: 'auto' });

const UNITS: { unit: Intl.RelativeTimeFormatUnit; seconds: number }[] = [
  { unit: 'year', seconds: 31_536_000 },
  { unit: 'month', seconds: 2_592_000 },
  { unit: 'week', seconds: 604_800 },
  { unit: 'day', seconds: 86_400 },
  { unit: 'hour', seconds: 3_600 },
  { unit: 'minute', seconds: 60 },
];

/** Formats an ISO instant as an approximate relative time, e.g. "2 days ago". */
@Pipe({ name: 'relativeTime' })
export class RelativeTimePipe implements PipeTransform {
  transform(value: string | null | undefined, now: number = Date.now()): string {
    if (!value) return '';
    const then = new Date(value).getTime();
    if (Number.isNaN(then)) return '';

    const diffSeconds = (then - now) / 1000;
    const abs = Math.abs(diffSeconds);
    if (abs < 60) return 'just now';

    const match = UNITS.find((u) => abs >= u.seconds)!;
    const amount = Math.round(diffSeconds / match.seconds);
    return FORMATTER.format(amount, match.unit);
  }
}
