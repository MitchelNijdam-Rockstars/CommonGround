import { RelativeTimePipe } from './relative-time-pipe';

describe('RelativeTimePipe', () => {
  const pipe = new RelativeTimePipe();
  const now = new Date('2026-06-13T12:00:00Z').getTime();

  it('formats a recent time as "just now"', () => {
    expect(pipe.transform('2026-06-13T11:59:30Z', now)).toBe('just now');
  });

  it('formats hours ago', () => {
    expect(pipe.transform('2026-06-13T09:00:00Z', now)).toBe('3 hours ago');
  });

  it('formats days ago', () => {
    expect(pipe.transform('2026-06-11T12:00:00Z', now)).toBe('2 days ago');
  });

  it('returns empty string for missing or invalid values', () => {
    expect(pipe.transform(null, now)).toBe('');
    expect(pipe.transform('not-a-date', now)).toBe('');
  });
});
