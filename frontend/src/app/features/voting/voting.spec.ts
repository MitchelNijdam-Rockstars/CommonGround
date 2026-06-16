import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { LucideAngularModule, icons } from 'lucide-angular';
import { Matchup } from '../../core/models/matchup.model';
import { Voting } from './voting';

const matchup = (id: number, patternIds: number[]): Matchup => ({
  topic: { id, question: `Question ${id}?`, context: null, language: 'kotlin', labels: [] },
  patterns: patternIds.map((patternId) => ({
    id: patternId,
    topicId: id,
    title: `Pattern ${patternId}`,
    code: `fun p${patternId}() = ${patternId}`,
    language: 'kotlin',
    eloRating: 1500,
    timesShown: 0,
    timesChosen: 0,
    winRate: null,
    active: true,
  })),
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

  function cards(el: HTMLElement): HTMLButtonElement[] {
    return Array.from(el.querySelectorAll('[data-testid="pattern"]'));
  }

  function submitButton(el: HTMLElement): HTMLButtonElement {
    return el.querySelector('[data-testid="submit-vote"]') as HTMLButtonElement;
  }

  it('renders the first matchup of the batch with all its patterns and progress', () => {
    const fixture = createWithBatch([matchup(1, [11, 12, 13]), matchup(2, [21, 22])]);
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('h1')?.textContent).toContain('Question 1?');
    expect(cards(el)).toHaveLength(3);
    expect(el.querySelector('[data-testid="progress"]')?.textContent?.trim()).toBe('1 / 2');
  });

  it('selecting a card does not vote until the submit button is pressed', () => {
    const fixture = createWithBatch([matchup(1, [11, 12, 13]), matchup(2, [21, 22])]);
    const el = fixture.nativeElement as HTMLElement;

    // submit is disabled until a card is picked
    expect(submitButton(el).disabled).toBe(true);

    cards(el)[1].click(); // pick pattern 12
    fixture.detectChanges();
    http.expectNone('/api/voting/vote'); // no vote yet
    expect(submitButton(el).disabled).toBe(false);
    expect(cards(el)[1].getAttribute('aria-pressed')).toBe('true');

    submitButton(el).click();
    const voteRequest = http.expectOne('/api/voting/vote');
    expect(voteRequest.request.body).toEqual({
      winnerPatternId: 12,
      beatenPatternIds: [11, 13],
      comment: null,
    });
    voteRequest.flush({ voteId: 1, winnerNewRating: 1516, currentStreak: 1 });
    fixture.detectChanges();

    expect(el.querySelector('h1')?.textContent).toContain('Question 2?');
    expect(el.querySelector('[data-testid="progress"]')?.textContent?.trim()).toBe('2 / 2');
    expect(submitButton(el).disabled).toBe(true); // selection cleared for the next matchup
  });

  it('clicking the already-selected card a second time confirms the vote', () => {
    const fixture = createWithBatch([matchup(1, [11, 12]), matchup(2, [21, 22])]);
    const el = fixture.nativeElement as HTMLElement;

    cards(el)[0].click(); // select
    fixture.detectChanges();
    http.expectNone('/api/voting/vote');

    cards(el)[0].click(); // confirm
    const voteRequest = http.expectOne('/api/voting/vote');
    expect(voteRequest.request.body).toEqual({
      winnerPatternId: 11,
      beatenPatternIds: [12],
      comment: null,
    });
    voteRequest.flush({ voteId: 1, winnerNewRating: 1516, currentStreak: 1 });
    fixture.detectChanges();

    expect(el.querySelector('h1')?.textContent).toContain('Question 2?');
  });

  it('sends the optional comment with the vote and clears it afterwards', async () => {
    const fixture = createWithBatch([matchup(1, [11, 12]), matchup(2, [21, 22])]);
    const el = fixture.nativeElement as HTMLElement;

    const textarea = el.querySelector('[data-testid="comment"]') as HTMLTextAreaElement;
    textarea.value = 'Cleaner null handling';
    textarea.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    cards(el)[0].click(); // select
    fixture.detectChanges();
    submitButton(el).click(); // confirm — comment filled in after picking

    const voteRequest = http.expectOne('/api/voting/vote');
    expect(voteRequest.request.body.comment).toBe('Cleaner null handling');
    voteRequest.flush({ voteId: 1, winnerNewRating: 1516, currentStreak: 1 });
    fixture.detectChanges();

    const nextTextarea = el.querySelector('[data-testid="comment"]') as HTMLTextAreaElement;
    expect(nextTextarea.value).toBe('');
  });

  it('skip buttons submit the topic id with the matching SkipReason', () => {
    const fixture = createWithBatch([matchup(1, [11, 12]), matchup(2, [21, 22])]);
    const el = fixture.nativeElement as HTMLElement;

    (el.querySelector('[data-testid="skip-not-familiar"]') as HTMLButtonElement).click();

    const skipRequest = http.expectOne('/api/voting/skip');
    expect(skipRequest.request.body).toEqual({
      topicId: 1,
      reason: 'NOT_FAMILIAR',
    });
    skipRequest.flush(null);
    fixture.detectChanges();

    expect(el.querySelector('h1')?.textContent).toContain('Question 2?');
  });

  it('fetches a fresh batch after the last matchup and shows the caught-up state when empty', () => {
    const fixture = createWithBatch([matchup(1, [11, 12])]);
    const el = fixture.nativeElement as HTMLElement;

    cards(el)[0].click(); // select
    fixture.detectChanges();
    submitButton(el).click(); // confirm
    http
      .expectOne('/api/voting/vote')
      .flush({ voteId: 1, winnerNewRating: 1516, currentStreak: 1 });
    fixture.detectChanges();

    http.expectOne((req) => req.url === '/api/voting/matchups').flush([]);
    // An empty feed triggers a topics lookup; topics still exist, so it's a caught-up state.
    http.expectOne((req) => req.url === '/api/topics').flush([{ id: 1 }]);
    fixture.detectChanges();

    expect(el.textContent).toContain('All caught up!');
    expect(el.textContent).not.toContain('No topics yet');
  });

  it('shows the empty-system onboarding state when there are no topics at all', () => {
    const fixture = TestBed.createComponent(Voting);
    fixture.detectChanges();
    http.expectOne((req) => req.url === '/api/voting/matchups').flush([]);
    http.expectOne((req) => req.url === '/api/topics').flush([]);
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('No topics yet');
    expect(el.textContent).not.toContain('All caught up!');
  });
});
