import { Component, effect, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Label } from '../../core/models/label.model';
import { Auth } from '../../core/services/auth';
import { CatalogApi } from '../../core/services/catalog-api';
import { Expertise } from '../../core/services/expertise';
import { AboutDialog } from '../../shared/components/about-dialog/about-dialog';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, LucideAngularModule, AboutDialog],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {
  protected readonly auth = inject(Auth);
  protected readonly expertise = inject(Expertise);
  private readonly catalogApi = inject(CatalogApi);

  protected readonly menuOpen = signal(false);
  protected readonly expertisePanelOpen = signal(false);
  protected readonly aboutOpen = signal(false);
  protected readonly languageLabels = signal<Label[]>([]);

  protected readonly tabs = [
    { path: '/', label: 'Vote', icon: 'swords', exact: true },
    { path: '/catalog', label: 'Catalog', icon: 'library-big', exact: false },
    { path: '/rankings', label: 'Rankings & Insights', icon: 'trending-up', exact: false },
  ];

  constructor() {
    effect(() => {
      if (this.auth.user()) {
        this.expertise.loadOnce();
      }
    });
  }

  toggleExpertisePanel(): void {
    this.expertisePanelOpen.update((open) => !open);
    if (this.expertisePanelOpen() && this.languageLabels().length === 0) {
      this.catalogApi.getLabels('LANGUAGE').subscribe((labels) => this.languageLabels.set(labels));
    }
  }

  isSelected(labelId: number): boolean {
    return this.expertise.selection().some((label) => label.id === labelId);
  }

  logout(): void {
    this.menuOpen.set(false);
    this.auth.logout();
  }
}
