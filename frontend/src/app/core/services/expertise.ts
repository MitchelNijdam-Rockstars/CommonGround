import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Label } from '../models/label.model';

@Injectable({
  providedIn: 'root',
})
export class Expertise {
  private readonly http = inject(HttpClient);

  readonly selection = signal<Label[]>([]);
  readonly openTopicCount = signal<number | null>(null);
  private loaded = false;

  /** Loads the current selection and open-topic count once per session. */
  loadOnce(): void {
    if (this.loaded) return;
    this.loaded = true;
    this.http.get<Label[]>('/api/users/me/expertise').subscribe((labels) => {
      this.selection.set(labels);
    });
    this.refreshOpenTopicCount();
  }

  update(labelIds: number[]): void {
    this.http
      .put<Label[]>('/api/users/me/expertise', { labelIds })
      .subscribe((labels) => {
        this.selection.set(labels);
        this.refreshOpenTopicCount();
      });
  }

  remove(labelId: number): void {
    this.update(
      this.selection()
        .filter((label) => label.id !== labelId)
        .map((label) => label.id),
    );
  }

  toggle(labelId: number): void {
    const current = this.selection().map((label) => label.id);
    this.update(
      current.includes(labelId) ? current.filter((id) => id !== labelId) : [...current, labelId],
    );
  }

  private refreshOpenTopicCount(): void {
    this.http
      .get<{ count: number }>('/api/voting/open-topic-count')
      .subscribe(({ count }) => this.openTopicCount.set(count));
  }
}
