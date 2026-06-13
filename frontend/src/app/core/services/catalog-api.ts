import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Label, LabelType } from '../models/label.model';
import { Pattern } from '../models/pattern.model';
import { Topic } from '../models/topic.model';

@Injectable({
  providedIn: 'root',
})
export class CatalogApi {
  private readonly http = inject(HttpClient);

  getLabels(type?: LabelType): Observable<Label[]> {
    const params = type ? new HttpParams().set('type', type) : undefined;
    return this.http.get<Label[]>('/api/labels', { params });
  }

  getTopics(search?: string, labelId?: number): Observable<Topic[]> {
    let params = new HttpParams();
    if (search?.trim()) params = params.set('search', search.trim());
    if (labelId != null) params = params.set('labelId', labelId);
    return this.http.get<Topic[]>('/api/topics', { params });
  }

  getPatterns(topicId: number): Observable<Pattern[]> {
    return this.http.get<Pattern[]>(`/api/topics/${topicId}/patterns`);
  }
}
