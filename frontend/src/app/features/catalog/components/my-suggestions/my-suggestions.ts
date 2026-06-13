import { Component, OnInit, inject, signal } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { PatternSuggestion, SuggestionStatus, TopicSuggestion } from '../../../../core/models/suggestion.model';
import { SuggestionsApi } from '../../../../core/services/suggestions-api';

@Component({
  selector: 'app-my-suggestions',
  imports: [LucideAngularModule],
  templateUrl: './my-suggestions.html',
  styleUrl: './my-suggestions.scss',
})
export class MySuggestions implements OnInit {
  private readonly api = inject(SuggestionsApi);

  protected readonly patternSuggestions = signal<PatternSuggestion[]>([]);
  protected readonly topicSuggestions = signal<TopicSuggestion[]>([]);

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.api.myPatternSuggestions().subscribe((s) => this.patternSuggestions.set(s));
    this.api.myTopicSuggestions().subscribe((s) => this.topicSuggestions.set(s));
  }

  statusClasses(status: SuggestionStatus): string {
    switch (status) {
      case 'APPROVED':
        return 'bg-primary/15 text-primary-soft border-primary/30';
      case 'REJECTED':
        return 'bg-accent-pink/15 text-accent-pink border-accent-pink/30';
      default:
        return 'bg-streak/15 text-streak border-streak/30';
    }
  }
}
