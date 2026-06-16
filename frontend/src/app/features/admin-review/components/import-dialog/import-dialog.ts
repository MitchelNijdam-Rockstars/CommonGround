import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { ImportRequest, ImportResult } from '../../../../core/models/import.model';
import { ImportApi } from '../../../../core/services/import-api';

const EXAMPLE_JSON = `{
  "topics": [
    {
      "question": "How should you name boolean variables?",
      "context": "Applies to local variables and fields.",
      "language": "kotlin",
      "labels": ["naming", "readability"],
      "patterns": [
        {
          "title": "Prefix with is/has/should",
          "code": "val isActive = user.active\\nval hasAccess = roles.isNotEmpty()"
        },
        {
          "title": "Plain noun",
          "code": "val active = user.active"
        }
      ]
    }
  ]
}`;

@Component({
  selector: 'app-import-dialog',
  imports: [FormsModule, LucideAngularModule],
  templateUrl: './import-dialog.html',
  styleUrl: './import-dialog.scss',
})
export class ImportDialog {
  private readonly api = inject(ImportApi);

  readonly closed = output<boolean>();

  protected readonly example = EXAMPLE_JSON;
  protected raw = '';
  protected readonly helpOpen = signal(true);
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly result = signal<ImportResult | null>(null);

  useExample(): void {
    this.raw = this.example;
    this.error.set(null);
  }

  toggleHelp(): void {
    this.helpOpen.update((open) => !open);
  }

  dismiss(): void {
    this.closed.emit(this.result() !== null);
  }

  submit(): void {
    this.error.set(null);
    this.result.set(null);

    let request: ImportRequest;
    try {
      request = JSON.parse(this.raw);
    } catch {
      this.error.set('That is not valid JSON. Check for a missing comma or bracket.');
      return;
    }

    if (!request?.topics?.length) {
      this.error.set('The JSON must contain a non-empty "topics" array.');
      return;
    }

    this.submitting.set(true);
    this.api.import(request).subscribe({
      next: (result) => {
        this.result.set(result);
        this.submitting.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.error?.message ??
            'Import failed. Make sure each topic has a question and each pattern a title and code.',
        );
        this.submitting.set(false);
      },
    });
  }
}
