import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { LucideAngularModule, icons } from 'lucide-angular';
import { Matchup } from '../../core/models/matchup.model';
import { Voting } from './voting';

const matchup = (id: number, patternAId: number, patternBId: number): Matchup => ({
  topic: { id, question: `Question ${id}?`, context: null, labels: [] },
  patternA: {
    id: patternAId,
    topicId: id,
    title: `Pattern ${patternAId}`,
    code: 'fun a() = 1',
    language: 'kotlin',
    eloRating: 1500,
    timesShown: 0,
    timesChosen: 0,
    winRate: null,
    active: true,
  },
  patternB: {
    id: patternBId,
    topicId: id,
    title: `Pattern ${patternBId}`,
    code: 'fun b() = 2',
    language: 'kotlin',
    eloRating: 1500,
    timesShown: 0,
    timesChosen: 0,
    winRate: null,
    active: true,
  },
  topicVoteCount: 5,
});

describe('Voting', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Voting],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        importProvidersFrom(LucideAngularModule.pick(icons)),
      ],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function createWithBatch(batch: Matchup[]) {
    const fixture = TestBed.createComponent(Voting);
    fixture.detectChanges();
    http.expectOne((req) => req.url === '/api/voting/matchups').flush(batch);
    fixture.detectChanges();
    return fixture;
  }

  it('renders the first matchup of the batch with progress', () => {
    const fixture = createWithBatch([matchup(1, 11, 12), matchup(2, 21, 22)]);
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('h1')?.textContent).toContain('Question 1?');
    expect(el.querySelector('[data-testid="progress"]')?.textContent?.trim()).toBe('1 / 2');
  });

  it('clicking a pattern card submits the vote and advances to the next matchup', () => {
    const fixture = createWithBatch([matchup(1, 11, 12), matchup(2, 21, 22)]);
    const el = fixture.nativeElement as HTMLElement;

    (el.querySelector('[data-testid="pattern-a"]') as HTMLButtonElement).click();

    const voteRequest = http.expectOne('/api/voting/vote');
    expect(voteRequest.request.body).toEqual({
      winnerPatternId: 11,
      loserPatternId: 12,
      comment: null,
    });
    voteRequest.flush({ voteId: 1, winnerNewRating: 1516, loserNewRating: 1484, currentStreak: 1 });
    fixture.detectChanges();

    expect(el.querySelector('h1')?.textContent).toContain('Question 2?');
    expect(el.querySelector('[data-testid="progress"]')?.textContent?.trim()).toBe('2 / 2');
  });

  it('sends the optional comment with the vote and clears it afterwards', async () => {
    const fixture = createWithBatch([matchup(1, 11, 12), matchup(2, 21, 22)]);
    const el = fixture.nativeElement as HTMLElement;

    const textarea = el.querySelector('[data-testid="comment"]') as HTMLTextAreaElement;
    textarea.value = 'Cleaner null handling';
    textarea.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    (el.querySelector('[data-testid="pattern-a"]') as HTMLButtonElement).click();

    const voteRequest = http.expectOne('/api/voting/vote');
    expect(voteRequest.request.body.comment).toBe('Cleaner null handling');
    voteRequest.flush({ voteId: 1, winnerNewRating: 1516, loserNewRating: 1484, currentStreak: 1 });
    fixture.detectChanges();

    const nextTextarea = el.querySelector('[data-testid="comment"]') as HTMLTextAreaElement;
    expect(nextTextarea.value).toBe('');
  });

  it('skip buttons submit the matching SkipReason', () => {
    const fixture = createWithBatch([matchup(1, 11, 12), matchup(2, 21, 22)]);
    const el = fixture.nativeElement as HTMLElement;

    (el.querySelector('[data-testid="skip-not-familiar"]') as HTMLButtonElement).click();

    const skipRequest = http.expectOne('/api/voting/skip');
    expect(skipRequest.request.body).toEqual({
      patternAId: 11,
      patternBId: 12,
      reason: 'NOT_FAMILIAR',
    });
    skipRequest.flush(null);
    fixture.detectChanges();

    expect(el.querySelector('h1')?.textContent).toContain('Question 2?');
  });

  it('fetches a fresh batch after the last matchup and shows the caught-up state when empty', () => {
    const fixture = createWithBatch([matchup(1, 11, 12)]);
    const el = fixture.nativeElement as HTMLElement;

    (el.querySelector('[data-testid="pattern-b"]') as HTMLButtonElement).click();
    http
      .expectOne('/api/voting/vote')
      .flush({ voteId: 1, winnerNewRating: 1516, loserNewRating: 1484, currentStreak: 1 });
    fixture.detectChanges();

    http.expectOne((req) => req.url === '/api/voting/matchups').flush([]);
    fixture.detectChanges();

    expect(el.textContent).toContain('All caught up!');
  });
});
