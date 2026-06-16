import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Observable } from 'rxjs';
import { PatternSuggestion, TopicSuggestion } from '../../core/models/suggestion.model';
import { SuggestionsApi } from '../../core/services/suggestions-api';
import { CodeBlock } from '../../shared/components/code-block/code-block';
import { LabelBadge } from '../../shared/components/label-badge/label-badge';
import { ImportDialog } from './components/import-dialog/import-dialog';

@Component({
  selector: 'app-admin-review',
  imports: [FormsModule, LucideAngularModule, CodeBlock, LabelBadge, ImportDialog],
  templateUrl: './admin-review.html',
  styleUrl: './admin-review.scss',
})
export class AdminReview implements OnInit {
  private readonly api = inject(SuggestionsApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly patternSuggestions = signal<PatternSuggestion[]>([]);
  protected readonly topicSuggestions = signal<TopicSuggestion[]>([]);
  protected readonly loading = signal(true);
  protected readonly importOpen = signal(false);
  /** Pulses the import button when an admin is sent here to set up their first topics. */
  protected readonly highlightImport = signal(false);

  /** id of the suggestion whose rejection-reason input is open, prefixed by kind */
  protected readonly rejectingKey = signal<string | null>(null);
  protected rejectionReason = '';

  ngOnInit(): void {
    this.reload();
    if (this.route.snapshot.queryParamMap.get('setup') === 'import') {
      this.highlightImport.set(true);
      // Drop the flag from the URL so a refresh or back-navigation doesn't re-trigger the nudge.
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { setup: null },
        replaceUrl: true,
      });
    }
  }

  openImport(): void {
    this.highlightImport.set(false);
    this.importOpen.set(true);
  }

  onImportClosed(imported: boolean): void {
    this.importOpen.set(false);
    if (imported) {
      this.reload();
    }
  }

  reload(): void {
    this.loading.set(true);
    this.api.pendingPatternSuggestions().subscribe((s) => {
      this.patternSuggestions.set(s);
      this.loading.set(false);
    });
    this.api.pendingTopicSuggestions().subscribe((s) => this.topicSuggestions.set(s));
  }

  approvePattern(id: number): void {
    this.api.approvePatternSuggestion(id).subscribe(() => this.reload());
  }

  approveTopic(id: number): void {
    this.api.approveTopicSuggestion(id).subscribe(() => this.reload());
  }

  openReject(kind: 'pattern' | 'topic', id: number): void {
    this.rejectingKey.set(`${kind}-${id}`);
    this.rejectionReason = '';
  }

  confirmReject(kind: 'pattern' | 'topic', id: number): void {
    const reason = this.rejectionReason.trim() || null;
    const request: Observable<unknown> =
      kind === 'pattern'
        ? this.api.rejectPatternSuggestion(id, reason)
        : this.api.rejectTopicSuggestion(id, reason);
    request.subscribe(() => {
      this.rejectingKey.set(null);
      this.reload();
    });
  }
}
