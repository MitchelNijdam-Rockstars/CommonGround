import { Component, OnInit, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SuggestionsApi } from '../../../../core/services/suggestions-api';
import { CodeBlock } from '../../../../shared/components/code-block/code-block';

@Component({
  selector: 'app-pattern-suggestion-form',
  imports: [FormsModule, CodeBlock],
  templateUrl: './pattern-suggestion-form.html',
  styleUrl: './pattern-suggestion-form.scss',
})
export class PatternSuggestionForm implements OnInit {
  private readonly api = inject(SuggestionsApi);

  readonly topicId = input.required<number>();
  readonly defaultLanguage = input<string>('');
  readonly submitted = output<void>();
  readonly cancelled = output<void>();

  protected title = '';
  protected code = '';
  protected language = '';
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.language = this.defaultLanguage();
  }

  submit(): void {
    if (!this.code.trim() || !this.language.trim() || this.saving()) return;
    this.saving.set(true);
    this.error.set(null);
    this.api
      .submitPatternSuggestion(this.topicId(), {
        title: this.title.trim() || null,
        code: this.code,
        language: this.language.trim(),
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
