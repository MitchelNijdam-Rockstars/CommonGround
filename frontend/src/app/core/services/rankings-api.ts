import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PatternDetail } from '../models/pattern-detail.model';
import { RankingAlgorithm, TopicRanking } from '../models/ranking.model';

@Injectable({
  providedIn: 'root',
})
export class RankingsApi {
  private readonly http = inject(HttpClient);

  getRankings(algorithm: RankingAlgorithm): Observable<TopicRanking[]> {
    return this.http.get<TopicRanking[]>('/api/rankings', { params: { algorithm } });
  }

  getPatternDetail(patternId: number): Observable<PatternDetail> {
    return this.http.get<PatternDetail>(`/api/patterns/${patternId}`);
  }

  /** Downloads the Markdown export and triggers a browser save without navigating away. */
  exportMarkdown(): Observable<HttpResponse<Blob>> {
    return this.http.get('/api/rankings/export', { responseType: 'blob', observe: 'response' });
  }

  saveExport(response: HttpResponse<Blob>): void {
    const blob = response.body ?? new Blob([], { type: 'text/markdown' });
    const filename = this.filenameFromResponse(response);
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  private filenameFromResponse(response: HttpResponse<Blob>): string {
    const disposition = response.headers.get('Content-Disposition');
    const match = disposition?.match(/filename="?([^"]+)"?/i);
    if (match) return match[1];
    return `common-ground-${new Date().toISOString().slice(0, 10)}.md`;
  }
}
