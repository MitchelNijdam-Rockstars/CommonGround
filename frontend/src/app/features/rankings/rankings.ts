import { DecimalPipe, PercentPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { RankingAlgorithm, TopicRanking } from '../../core/models/ranking.model';
import { RankingsApi } from '../../core/services/rankings-api';
import { CodeBlock } from '../../shared/components/code-block/code-block';
import { LabelBadge } from '../../shared/components/label-badge/label-badge';
import { PatternDetail } from './components/pattern-detail/pattern-detail';

const DEFAULT_ALGORITHM: RankingAlgorithm = 'ELO';

interface SelectedPattern {
  id: number;
  rank: number;
}

@Component({
  selector: 'app-rankings',
  imports: [LucideAngularModule, CodeBlock, LabelBadge, PatternDetail, DecimalPipe, PercentPipe],
  templateUrl: './rankings.html',
  styleUrl: './rankings.scss',
})
export class Rankings implements OnInit {
  private readonly api = inject(RankingsApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly algorithm = signal<RankingAlgorithm>(DEFAULT_ALGORITHM);
  protected readonly rankings = signal<TopicRanking[]>([]);
  protected readonly loading = signal(true);
  protected readonly exporting = signal(false);
  protected readonly selected = signal<SelectedPattern | null>(null);

  protected exportMarkdown(): void {
    if (this.exporting()) return;
    this.exporting.set(true);
    this.api.exportMarkdown().subscribe({
      next: (response) => {
        this.api.saveExport(response);
        this.exporting.set(false);
      },
      error: () => this.exporting.set(false),
    });
  }

  protected openPattern(patternId: number, rank: number): void {
    this.selected.set({ id: patternId, rank });
  }

  protected closePattern(): void {
    this.selected.set(null);
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      const next = params['algorithm'] === 'WIN_RATE' ? 'WIN_RATE' : DEFAULT_ALGORITHM;
      this.algorithm.set(next);
      this.load(next);
    });
  }

  protected selectAlgorithm(algorithm: RankingAlgorithm): void {
    if (algorithm === this.algorithm()) return;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { algorithm },
      queryParamsHandling: 'merge',
    });
  }

  private load(algorithm: RankingAlgorithm): void {
    this.loading.set(true);
    this.api.getRankings(algorithm).subscribe({
      next: (rankings) => {
        this.rankings.set(rankings);
        this.loading.set(false);
      },
      error: () => {
        this.rankings.set([]);
        this.loading.set(false);
      },
    });
  }
}
