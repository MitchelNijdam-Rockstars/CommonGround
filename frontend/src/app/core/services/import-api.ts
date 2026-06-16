import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ImportRequest, ImportResult } from '../models/import.model';

@Injectable({
  providedIn: 'root',
})
export class ImportApi {
  private readonly http = inject(HttpClient);

  import(request: ImportRequest): Observable<ImportResult> {
    return this.http.post<ImportResult>('/api/admin/import', request);
  }
}
