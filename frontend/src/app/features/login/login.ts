import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Auth } from '../../core/services/auth';

@Component({
  selector: 'app-login',
  imports: [LucideAngularModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  protected checking = false;

  /** Cloudflare Access authenticates at the edge — retrying simply re-checks the session. */
  async retry(): Promise<void> {
    this.checking = true;
    const user = await this.auth.load();
    this.checking = false;
    if (user) {
      this.router.navigate(['/']);
    }
  }
}
