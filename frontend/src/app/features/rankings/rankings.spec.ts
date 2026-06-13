import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { importProvidersFrom } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { LucideAngularModule, icons } from 'lucide-angular';
import { TopicRanking } from '../../core/models/ranking.model';
import { Rankings } from './rankings';

const section = (eloRating: number): TopicRanking => ({
  topic: { id: 1, question: 'How to handle nulls?', context: null, labels: [] },
  totalVotes: 7,
  patterns: [
    {
      id: 11,
      topicId: 1,
      title: 'Nullable',
      code: 'fun a() = 1',
      language: 'kotlin',
      eloRating,
      timesShown: 4,
      timesChosen: 3,
      winRate: 0.75,
      active: true,
    },
  ],
});

describe('Rankings', () => {
  let http: HttpTestingController;
  let router: Router;

  async function setup(initialUrl = '/'): Promise<ComponentFixture<Rankings>> {
    await TestBed.configureTestingModule({
      imports: [Rankings],
      providers: [
        provideRouter([{ path: '**', component: Rankings }]),
        provideHttpClient(),
        provideHttpClientTesting(),
        importProvidersFrom(LucideAngularModule.pick(icons)),
      ],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    await router.navigateByUrl(initialUrl);
    const fixture = TestBed.createComponent(Rankings);
    fixture.detectChanges();
    return fixture;
  }

  afterEach(() => http.verify());

  it('loads with the default ELO algorithm when no query param is present', async () => {
    const fixture = await setup('/');
    const req = http.expectOne((r) => r.url === '/api/rankings');
    expect(req.request.params.get('algorithm')).toBe('ELO');
    req.flush([section(1600)]);
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('[data-testid="algorithm-elo"]')?.getAttribute('aria-selected')).toBe(
      'true',
    );
    expect(el.querySelector('[data-testid="rank"]')?.textContent?.trim()).toBe('1');
    expect(el.textContent).toContain('How to handle nulls?');
  });

  it('applies WIN_RATE immediately when the URL carries ?algorithm=WIN_RATE', async () => {
    const fixture = await setup('/?algorithm=WIN_RATE');
    const req = http.expectOne((r) => r.url === '/api/rankings');
    expect(req.request.params.get('algorithm')).toBe('WIN_RATE');
    req.flush([section(1600)]);
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(
      el.querySelector('[data-testid="algorithm-win-rate"]')?.getAttribute('aria-selected'),
    ).toBe('true');
  });

  it('clicking Export requests the markdown export as a blob', async () => {
    const createObjectURL = vi.fn(() => 'blob:mock');
    const revokeObjectURL = vi.fn();
    URL.createObjectURL = createObjectURL as unknown as typeof URL.createObjectURL;
    URL.revokeObjectURL = revokeObjectURL as unknown as typeof URL.revokeObjectURL;
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});

    const fixture = await setup('/');
    http.expectOne((r) => r.url === '/api/rankings').flush([section(1600)]);
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    (el.querySelector('[data-testid="export-button"]') as HTMLButtonElement).click();

    const req = http.expectOne((r) => r.url === '/api/rankings/export');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['# md'], { type: 'text/markdown' }), {
      headers: { 'Content-Disposition': 'attachment; filename="common-ground-2026-06-13.md"' },
    });

    expect(createObjectURL).toHaveBeenCalled();
  });

  it('switching algorithm updates the URL and re-fetches', async () => {
    const fixture = await setup('/');
    http.expectOne((r) => r.url === '/api/rankings').flush([section(1600)]);
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    (el.querySelector('[data-testid="algorithm-win-rate"]') as HTMLButtonElement).click();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(router.url).toContain('algorithm=WIN_RATE');
    const req = http.expectOne((r) => r.url === '/api/rankings');
    expect(req.request.params.get('algorithm')).toBe('WIN_RATE');
    req.flush([section(1600)]);
  });
});
