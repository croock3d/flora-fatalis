import { Routes } from '@angular/router';

import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/register/register-page').then((m) => m.RegisterPage),
  },
  {
    path: 'household',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./household/household-page/household-page').then((m) => m.HouseholdPage),
  },
  {
    path: 'species',
    canActivate: [authGuard],
    loadComponent: () => import('./species/species-page').then((m) => m.SpeciesPage),
  },
  {
    path: 'locations',
    canActivate: [authGuard],
    loadComponent: () => import('./locations/locations-page').then((m) => m.LocationsPage),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./dashboard/dashboard-page').then((m) => m.DashboardPage),
  },
  {
    path: 'plants',
    canActivate: [authGuard],
    loadComponent: () => import('./plants/plants-page').then((m) => m.PlantsPage),
  },
  {
    path: 'plants/new',
    canActivate: [authGuard],
    loadComponent: () => import('./plants/plant-form-page').then((m) => m.PlantFormPage),
  },
  {
    path: 'plants/:id/edit',
    canActivate: [authGuard],
    loadComponent: () => import('./plants/plant-form-page').then((m) => m.PlantFormPage),
  },
  {
    path: 'plants/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./plants/plant-details-page').then((m) => m.PlantDetailsPage),
  },
  { path: '**', redirectTo: 'dashboard' },
];
