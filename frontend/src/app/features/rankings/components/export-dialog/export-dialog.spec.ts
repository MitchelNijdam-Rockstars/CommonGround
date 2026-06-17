import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { importProvidersFrom } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LucideAngularModule, icons } from 'lucide-angular';
import { ExportDialog } from './export-dialog';

describe('ExportDialog', () => {
  let fixture: ComponentFixture<ExportDialog>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExportDialog],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        importProvidersFrom(LucideAngularModule.pick(icons)),
      ],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ExportDialog);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('renders all agent options', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Claude Code');
    expect(el.textContent).toContain('GitHub Copilot');
    expect(el.textContent).toContain('Cursor');
    expect(el.textContent).toContain('Windsurf');
    expect(el.textContent).toContain('Generic');
  });

  it('selecting Cursor updates the file path to the .mdc path', () => {
    const el = fixture.nativeElement as HTMLElement;
    const cursorBtn = Array.from(el.querySelectorAll('button')).find((b) =>
      b.textContent?.includes('Cursor'),
    ) as HTMLButtonElement;
    cursorBtn.click();
    fixture.detectChanges();
    expect(el.textContent).toContain('.cursor/rules/coding-standards.mdc');
  });

  it('Download calls the API with the selected format and closes the dialog on success', () => {
    const createObjectURL = vi.fn(() => 'blob:mock');
    const revokeObjectURL = vi.fn();
    URL.createObjectURL = createObjectURL as unknown as typeof URL.createObjectURL;
    URL.revokeObjectURL = revokeObjectURL as unknown as typeof URL.revokeObjectURL;
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});

    const closedSpy = vi.fn();
    fixture.componentInstance.closed.subscribe(closedSpy);

    const el = fixture.nativeElement as HTMLElement;
    (el.querySelector('[data-testid="export-dialog-download"]') as HTMLButtonElement).click();

    const req = http.expectOne((r) => r.url === '/api/rankings/export');
    expect(req.request.params.get('format')).toBe('CLAUDE');
    req.flush(new Blob(['# Coding Standards'], { type: 'text/markdown' }), {
      headers: { 'Content-Disposition': 'attachment; filename="CLAUDE.md"' },
    });

    expect(createObjectURL).toHaveBeenCalled();
    expect(closedSpy).toHaveBeenCalled();
  });
});
