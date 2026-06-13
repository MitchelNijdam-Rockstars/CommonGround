import { Routes } from '@angular/router';
import { adminGuard } from './core/guards/admin-guard';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Common Ground — Sign in',
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  {
    path: '',
    title: 'Common Ground — Vote',
    canActivate: [authGuard],
    loadComponent: () => import('./features/voting/voting').then((m) => m.Voting),
  },
  {
    path: 'catalog',
    title: 'Common Ground — Catalog',
    canActivate: [authGuard],
    loadComponent: () => import('./features/catalog/catalog').then((m) => m.Catalog),
  },
  {
    path: 'rankings',
    title: 'Common Ground — Rankings & Insights',
    canActivate: [authGuard],
    loadComponent: () => import('./features/rankings/rankings').then((m) => m.Rankings),
  },
  {
    path: 'admin/review',
    title: 'Common Ground — Suggestion review',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin-review/admin-review').then((m) => m.AdminReview),
  },
  { path: '**', redirectTo: '' },
];
