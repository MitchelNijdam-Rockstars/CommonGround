import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { CurrentUser } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly user = signal<CurrentUser | null>(null);
  readonly isAdmin = () => this.user()?.role === 'ADMIN';

  /** Loads the current user from the backend, caching it for the session. */
  async load(): Promise<CurrentUser | null> {
    if (this.user()) return this.user();
    try {
      const user = await firstValueFrom(this.http.get<CurrentUser>('/api/users/me'));
      this.user.set(user);
      return user;
    } catch {
      this.user.set(null);
      return null;
    }
  }

  initials(): string {
    const name = this.user()?.displayName ?? '';
    return name
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0].toUpperCase())
      .join('');
  }

  logout(): void {
    const logoutUrl = this.user()?.logoutUrl;
    this.user.set(null);
    if (logoutUrl) {
      window.location.assign(logoutUrl);
    } else {
      this.router.navigate(['/login']);
    }
  }
}
