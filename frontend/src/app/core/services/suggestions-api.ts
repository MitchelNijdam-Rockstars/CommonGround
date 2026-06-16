import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PatternSuggestion, TopicSuggestion } from '../models/suggestion.model';

@Injectable({
  providedIn: 'root',
})
export class SuggestionsApi {
  private readonly http = inject(HttpClient);

  submitPatternSuggestion(
    topicId: number,
    suggestion: { title: string | null; code: string },
  ): Observable<PatternSuggestion> {
    return this.http.post<PatternSuggestion>(
      `/api/topics/${topicId}/suggestions/patterns`,
      suggestion,
    );
  }

  myPatternSuggestions(): Observable<PatternSuggestion[]> {
    return this.http.get<PatternSuggestion[]>('/api/users/me/suggestions/patterns');
  }

  submitTopicSuggestion(suggestion: {
    question: string;
    context: string | null;
    language: string | null;
    labelIds: number[];
    patterns: { title: string | null; code: string }[];
  }): Observable<TopicSuggestion> {
    return this.http.post<TopicSuggestion>('/api/suggestions/topics', suggestion);
  }

  myTopicSuggestions(): Observable<TopicSuggestion[]> {
    return this.http.get<TopicSuggestion[]>('/api/users/me/suggestions/topics');
  }

  pendingPatternSuggestions(): Observable<PatternSuggestion[]> {
    return this.http.get<PatternSuggestion[]>('/api/admin/suggestions/patterns', {
      params: { status: 'PENDING' },
    });
  }

  pendingTopicSuggestions(): Observable<TopicSuggestion[]> {
    return this.http.get<TopicSuggestion[]>('/api/admin/suggestions/topics', {
      params: { status: 'PENDING' },
    });
  }

  approvePatternSuggestion(id: number): Observable<PatternSuggestion> {
    return this.http.post<PatternSuggestion>(`/api/admin/suggestions/patterns/${id}/approve`, {});
  }

  rejectPatternSuggestion(id: number, reason: string | null): Observable<PatternSuggestion> {
    return this.http.post<PatternSuggestion>(`/api/admin/suggestions/patterns/${id}/reject`, {
      reason,
    });
  }

  approveTopicSuggestion(id: number): Observable<TopicSuggestion> {
    return this.http.post<TopicSuggestion>(`/api/admin/suggestions/topics/${id}/approve`, {});
  }

  rejectTopicSuggestion(id: number, reason: string | null): Observable<TopicSuggestion> {
    return this.http.post<TopicSuggestion>(`/api/admin/suggestions/topics/${id}/reject`, {
      reason,
    });
  }
}
