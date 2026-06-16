import { Component, OnInit, inject, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Label } from '../../../../core/models/label.model';
import { CatalogApi } from '../../../../core/services/catalog-api';
import { SuggestionsApi } from '../../../../core/services/suggestions-api';
import { CodeBlock } from '../../../../shared/components/code-block/code-block';

interface PatternRow {
  title: string;
  code: string;
}

@Component({
  selector: 'app-topic-suggestion-form',
  imports: [FormsModule, LucideAngularModule, CodeBlock],
  templateUrl: './topic-suggestion-form.html',
  styleUrl: './topic-suggestion-form.scss',
})
export class TopicSuggestionForm implements OnInit {
  private readonly api = inject(SuggestionsApi);
  private readonly catalogApi = inject(CatalogApi);

  readonly submitted = output<void>();
  readonly cancelled = output<void>();

  protected question = '';
  protected context = '';
  protected language = '';
  protected readonly labels = signal<Label[]>([]);
  protected readonly selectedLabelIds = signal<Set<number>>(new Set());
  protected readonly patterns = signal<PatternRow[]>([{ title: '', code: '' }]);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.catalogApi.getLabels().subscribe((labels) => this.labels.set(labels));
  }

  toggleLabel(labelId: number): void {
    this.selectedLabelIds.update((ids) => {
      const next = new Set(ids);
      if (next.has(labelId)) {
        next.delete(labelId);
      } else {
        next.add(labelId);
      }
      return next;
    });
  }

  addPattern(): void {
    this.patterns.update((rows) => [...rows, { title: '', code: '' }]);
  }

  removePattern(index: number): void {
    this.patterns.update((rows) => rows.filter((_, i) => i !== index));
  }

  submit(): void {
    if (!this.question.trim() || this.saving()) return;
    this.saving.set(true);
    this.error.set(null);
    const patterns = this.patterns()
      .filter((row) => row.code.trim())
      .map((row) => ({ title: row.title.trim() || null, code: row.code }));
    this.api
      .submitTopicSuggestion({
        question: this.question.trim(),
        context: this.context.trim() || null,
        language: this.language.trim() || null,
        labelIds: [...this.selectedLabelIds()],
        patterns,
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.submitted.emit();
        },
        error: () => {
          this.saving.set(false);
          this.error.set('Could not submit your suggestion. Please try again.');
        },
      });
  }
}
