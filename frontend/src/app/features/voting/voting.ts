import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Matchup, SkipReason } from '../../core/models/matchup.model';
import { Auth } from '../../core/services/auth';
import { VotingApi } from '../../core/services/voting-api';
import { CodeBlock } from '../../shared/components/code-block/code-block';
import { LabelBadge } from '../../shared/components/label-badge/label-badge';

const BATCH_SIZE = 10;

@Component({
  selector: 'app-voting',
  imports: [LucideAngularModule, FormsModule, CodeBlock, LabelBadge],
  templateUrl: './voting.html',
  styleUrl: './voting.scss',
})
export class Voting implements OnInit {
  private readonly api = inject(VotingApi);
  private readonly auth = inject(Auth);

  protected readonly batch = signal<Matchup[]>([]);
  protected readonly index = signal(0);
  protected readonly loading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly allCaughtUp = signal(false);
  protected comment = '';

  protected readonly current = computed<Matchup | null>(() => this.batch()[this.index()] ?? null);
  protected readonly progress = computed(() => `${this.index() + 1} / ${this.batch().length}`);
  protected readonly streak = signal(0);

  ngOnInit(): void {
    this.streak.set(this.auth.user()?.currentStreak ?? 0);
    this.loadBatch();
  }

  vote(winnerPatternId: number, loserPatternId: number): void {
    if (this.submitting()) return;
    this.submitting.set(true);
    this.api.vote(winnerPatternId, loserPatternId, this.comment).subscribe({
      next: (result) => {
        this.streak.set(result.currentStreak);
        this.afterSubmission();
      },
      error: () => this.submitting.set(false),
    });
  }

  skip(reason: SkipReason): void {
    const matchup = this.current();
    if (!matchup || this.submitting()) return;
    this.submitting.set(true);
    this.api.skip(matchup.patternA.id, matchup.patternB.id, reason).subscribe({
      next: () => this.afterSubmission(),
      error: () => this.submitting.set(false),
    });
  }

  protected afterSubmission(): void {
    this.submitting.set(false);
    this.comment = '';
    if (this.index() + 1 < this.batch().length) {
      this.index.update((i) => i + 1);
    } else {
      this.loadBatch();
    }
  }

  protected loadBatch(): void {
    this.loading.set(true);
    this.api.getMatchups(BATCH_SIZE).subscribe({
      next: (matchups) => {
        this.batch.set(matchups);
        this.index.set(0);
        this.allCaughtUp.set(matchups.length === 0);
        this.loading.set(false);
      },
      error: () => {
        this.batch.set([]);
        this.allCaughtUp.set(true);
        this.loading.set(false);
      },
    });
  }
}
