import { Component, OnInit, inject, signal, viewChild } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { Label } from '../../core/models/label.model';
import { Pattern } from '../../core/models/pattern.model';
import { Topic } from '../../core/models/topic.model';
import { CatalogApi } from '../../core/services/catalog-api';
import { CodeBlock } from '../../shared/components/code-block/code-block';
import { LabelBadge } from '../../shared/components/label-badge/label-badge';
import { MySuggestions } from './components/my-suggestions/my-suggestions';
import { PatternSuggestionForm } from './components/pattern-suggestion-form/pattern-suggestion-form';
import { TopicSuggestionForm } from './components/topic-suggestion-form/topic-suggestion-form';

@Component({
  selector: 'app-catalog',
  imports: [
    LucideAngularModule,
    LabelBadge,
    CodeBlock,
    MySuggestions,
    PatternSuggestionForm,
    TopicSuggestionForm,
  ],
  templateUrl: './catalog.html',
  styleUrl: './catalog.scss',
})
export class Catalog implements OnInit {
  private readonly api = inject(CatalogApi);
  private searchDebounce?: ReturnType<typeof setTimeout>;

  private readonly mySuggestions = viewChild(MySuggestions);

  protected readonly labels = signal<Label[]>([]);
  protected readonly topics = signal<Topic[]>([]);
  protected readonly loading = signal(true);
  protected readonly search = signal('');
  protected readonly selectedLabelId = signal<number | null>(null);

  protected readonly expandedTopicId = signal<number | null>(null);
  protected readonly patternsByTopic = signal<Map<number, Pattern[]>>(new Map());
  protected readonly patternsLoading = signal(false);

  protected readonly topicFormOpen = signal(false);
  protected readonly patternFormTopicId = signal<number | null>(null);

  ngOnInit(): void {
    this.api.getLabels().subscribe((labels) => this.labels.set(labels));
    this.loadTopics();
  }

  onSearchInput(value: string): void {
    this.search.set(value);
    clearTimeout(this.searchDebounce);
    this.searchDebounce = setTimeout(() => this.loadTopics(), 200);
  }

  toggleLabel(labelId: number): void {
    this.selectedLabelId.set(this.selectedLabelId() === labelId ? null : labelId);
    this.loadTopics();
  }

  toggleTopic(topicId: number): void {
    if (this.expandedTopicId() === topicId) {
      this.expandedTopicId.set(null);
      this.patternFormTopicId.set(null);
      return;
    }
    this.expandedTopicId.set(topicId);
    this.patternFormTopicId.set(null);
    if (!this.patternsByTopic().has(topicId)) {
      this.patternsLoading.set(true);
      this.api.getPatterns(topicId).subscribe({
        next: (patterns) => {
          this.patternsByTopic.update((map) => new Map(map).set(topicId, patterns));
          this.patternsLoading.set(false);
        },
        error: () => this.patternsLoading.set(false),
      });
    }
  }

  patternsFor(topicId: number): Pattern[] {
    return this.patternsByTopic().get(topicId) ?? [];
  }

  /** Best guess for the suggestion form's language: the topic's first LANGUAGE label. */
  defaultLanguageFor(topic: Topic): string {
    return topic.labels.find((l) => l.labelType === 'LANGUAGE')?.name.toLowerCase() ?? '';
  }

  onPatternSuggestionSubmitted(): void {
    this.patternFormTopicId.set(null);
    this.mySuggestions()?.reload();
  }

  onTopicSuggestionSubmitted(): void {
    this.topicFormOpen.set(false);
    this.mySuggestions()?.reload();
  }

  protected loadTopics(): void {
    this.loading.set(true);
    this.api.getTopics(this.search(), this.selectedLabelId() ?? undefined).subscribe({
      next: (topics) => {
        this.topics.set(topics);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
