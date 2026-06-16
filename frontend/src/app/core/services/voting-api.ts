import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Matchup, SkipReason, VoteResult } from '../models/matchup.model';

@Injectable({
  providedIn: 'root',
})
export class VotingApi {
  private readonly http = inject(HttpClient);

  getMatchups(count = 10): Observable<Matchup[]> {
    return this.http.get<Matchup[]>('/api/voting/matchups', { params: { count } });
  }

  vote(
    winnerPatternId: number,
    beatenPatternIds: number[],
    comment?: string,
  ): Observable<VoteResult> {
    return this.http.post<VoteResult>('/api/voting/vote', {
      winnerPatternId,
      beatenPatternIds,
      comment: comment?.trim() || null,
    });
  }

  skip(topicId: number, reason: SkipReason): Observable<void> {
    return this.http.post<void>('/api/voting/skip', { topicId, reason });
  }
}
