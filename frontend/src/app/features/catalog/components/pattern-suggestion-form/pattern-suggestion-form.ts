import { Component, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SuggestionsApi } from '../../../../core/services/suggestions-api';
import { CodeBlock } from '../../../../shared/components/code-block/code-block';

@Component({
  selector: 'app-pattern-suggestion-form',
  imports: [FormsModule, CodeBlock],
  templateUrl: './pattern-suggestion-form.html',
  styleUrl: './pattern-suggestion-form.scss',
})
export class PatternSuggestionForm {
  private readonly api = inject(SuggestionsApi);

  readonly topicId = input.required<number>();
  /** The topic's language, used only to highlight the live preview. */
  readonly language = input<string | null>(null);
  readonly submitted = output<void>();
  readonly cancelled = output<void>();

  protected title = '';
  protected code = '';
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  submit(): void {
    if (!this.code.trim() || this.saving()) return;
    this.saving.set(true);
    this.error.set(null);
    this.api
      .submitPatternSuggestion(this.topicId(), {
        title: this.title.trim() || null,
        code: this.code,
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
