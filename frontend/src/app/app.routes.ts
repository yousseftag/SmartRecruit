import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { 
    path: 'dashboard', 
    loadComponent: () => import('./features/dashboard/dashboard').then(c => c.Dashboard)
  },
  {
    path: 'offres',
    loadComponent: () => import('./features/offers/offers-list/offers-list').then(c => c.OffersList)
  },
  {
    path: 'offres/nouvelle',
    loadComponent: () => import('./features/offers/offer-create/offer-create').then(c => c.OfferCreate)
  },
  {
    path: 'offres/:id',
    loadComponent: () => import('./features/offers/offer-detail/offer-detail').then(c => c.OfferDetail)
  },
  {
    path: 'offres/:id/modifier',
    loadComponent: () => import('./features/offers/offer-edit/offer-edit').then(c => c.OfferEdit)
  },
  {
    path: 'candidats',
    loadComponent: () => import('./features/candidates/candidates-list/candidates-list').then(c => c.CandidatesList)
  },
  {
    path: 'candidats/depot',
    loadComponent: () => import('./features/candidates/candidate-dropzone/candidate-dropzone').then(c => c.CandidateDropzone)
  },
  {
    path: 'candidats/import',
    loadComponent: () => import('./features/candidates/candidate-import/candidate-import').then(c => c.CandidateImport)
  },
  {
    path: 'candidats/:id',
    loadComponent: () => import('./features/candidates/candidate-profile/candidate-profile').then(c => c.CandidateProfile)
  },
  {
    path: 'workflow',
    loadComponent: () => import('./features/workflow/workflow-board/workflow-board').then(c => c.WorkflowBoard)
  },
  {
    path: 'administration',
    loadComponent: () => import('./features/administration/user-management/user-management').then(c => c.UserManagement)
  },
  {
    path: 'parametres/general',
    loadComponent: () => import('./features/settings/general-settings/general-settings').then(c => c.GeneralSettings)
  },
  {
    path: 'parametres/templates',
    loadComponent: () => import('./features/settings/template-settings/template-settings').then(c => c.TemplateSettings)
  },
  { path: '**', redirectTo: 'dashboard' }
];
