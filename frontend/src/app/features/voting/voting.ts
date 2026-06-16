import { Component, OnInit, computed, inject, isDevMode, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Matchup, SkipReason } from '../../core/models/matchup.model';
import { Pattern } from '../../core/models/pattern.model';
import { Auth } from '../../core/services/auth';
import { CatalogApi } from '../../core/services/catalog-api';
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
  private readonly catalog = inject(CatalogApi);
  private readonly router = inject(Router);

  protected readonly batch = signal<Matchup[]>([]);
  protected readonly index = signal(0);
  protected readonly loading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly allCaughtUp = signal(false);
  /** True when the whole system has no topics yet (a fresh install), not just this user being done. */
  protected readonly noTopics = signal(false);
  protected readonly selectedId = signal<number | null>(null);
  protected comment = '';

  protected readonly isAdmin = this.auth.isAdmin;

  protected readonly current = computed<Matchup | null>(() => this.batch()[this.index()] ?? null);
  protected readonly progress = computed(() => `${this.index() + 1} / ${this.batch().length}`);
  protected readonly streak = signal(0);

  /** True when served by `ng serve` (dev/local), not a production build — gates dev-only controls. */
  protected readonly isDev = isDevMode();

  ngOnInit(): void {
    this.streak.set(this.auth.user()?.currentStreak ?? 0);
    this.loadBatch();
  }

  /** First click picks a pattern (so the comment can still be filled in); a second confirms it. */
  select(winner: Pattern): void {
    if (this.submitting()) return;
    if (this.selectedId() === winner.id) {
      this.submitVote(winner);
    } else {
      this.selectedId.set(winner.id);
    }
  }

  submitSelected(): void {
    const matchup = this.current();
    const winner = matchup?.patterns.find((p) => p.id === this.selectedId());
    if (winner) this.submitVote(winner);
  }

  private submitVote(winner: Pattern): void {
    const matchup = this.current();
    if (!matchup || this.submitting()) return;
    this.submitting.set(true);
    const beatenPatternIds = matchup.patterns.filter((p) => p.id !== winner.id).map((p) => p.id);
    this.api.vote(winner.id, beatenPatternIds, this.comment).subscribe({
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
    this.api.skip(matchup.topic.id, reason).subscribe({
      next: () => this.afterSubmission(),
      error: () => this.submitting.set(false),
    });
  }

  protected afterSubmission(): void {
    this.submitting.set(false);
    this.comment = '';
    this.selectedId.set(null);
    if (this.index() + 1 < this.batch().length) {
      this.index.update((i) => i + 1);
    } else {
      this.loadBatch();
    }
  }

  /** Dev-only: wipe this user's votes/skips, then reload the feed so every topic reappears. */
  resetDev(): void {
    if (this.submitting()) return;
    this.submitting.set(true);
    this.api.resetMyVotes().subscribe({
      next: () => {
        this.submitting.set(false);
        this.selectedId.set(null);
        this.comment = '';
        this.allCaughtUp.set(false);
        this.loadBatch();
      },
      error: () => this.submitting.set(false),
    });
  }

  protected loadBatch(): void {
    this.loading.set(true);
    this.api.getMatchups(BATCH_SIZE).subscribe({
      next: (matchups) => {
        this.batch.set(matchups);
        this.index.set(0);
        this.allCaughtUp.set(matchups.length === 0);
        this.loading.set(false);
        if (matchups.length === 0) this.checkForTopics();
      },
      error: () => {
        this.batch.set([]);
        this.allCaughtUp.set(true);
        this.loading.set(false);
      },
    });
  }

  /**
   * Distinguishes an empty feed caused by a fresh, topic-less install from one where the user has
   * simply voted on everything — so admins can be nudged toward importing their first topics.
   */
  private checkForTopics(): void {
    this.catalog.getTopics().subscribe({
      next: (topics) => this.noTopics.set(topics.length === 0),
      error: () => this.noTopics.set(false),
    });
  }

  /** Sends an admin to the review screen with a flag that pulses the import button into focus. */
  goToImport(): void {
    this.router.navigate(['/admin/review'], { queryParams: { setup: 'import' } });
  }
}
