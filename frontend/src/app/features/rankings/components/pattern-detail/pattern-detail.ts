import { DecimalPipe, PercentPipe } from '@angular/common';
import { Component, effect, HostListener, inject, input, output, signal } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { PatternDetail as PatternDetailData } from '../../../../core/models/pattern-detail.model';
import { RankingsApi } from '../../../../core/services/rankings-api';
import { CodeBlock } from '../../../../shared/components/code-block/code-block';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time-pipe';

@Component({
  selector: 'app-pattern-detail',
  imports: [LucideAngularModule, CodeBlock, RelativeTimePipe, DecimalPipe, PercentPipe],
  templateUrl: './pattern-detail.html',
  styleUrl: './pattern-detail.scss',
})
export class PatternDetail {
  private readonly api = inject(RankingsApi);

  /** Id of the pattern to show; the component is only rendered when this is set. */
  readonly patternId = input.required<number>();
  /** The pattern's 1-based rank within its topic, as shown on the leaderboard. */
  readonly rank = input<number>();
  readonly close = output<void>();

  protected readonly detail = signal<PatternDetailData | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal(false);

  constructor() {
    effect(() => {
      const id = this.patternId();
      this.loading.set(true);
      this.error.set(false);
      this.detail.set(null);
      this.api.getPatternDetail(id).subscribe({
        next: (detail) => {
          this.detail.set(detail);
          this.loading.set(false);
        },
        error: () => {
          this.error.set(true);
          this.loading.set(false);
        },
      });
    });
  }

  @HostListener('document:keydown.escape')
  protected onEscape(): void {
    this.close.emit();
  }

  protected dismiss(): void {
    this.close.emit();
  }
}
