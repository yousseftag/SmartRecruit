import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { 
    path: 'dashboard', 
    loadComponent: () => import('./pages/dashboard/dashboard').then(c => c.Dashboard)
  },
  {
    path: 'offres',
    loadComponent: () => import('./pages/offers/offers-list/offers-list').then(c => c.OffersList)
  },
  {
    path: 'offres/nouvelle',
    loadComponent: () => import('./pages/offers/offer-form/offer-form').then(c => c.OfferForm)
  },
  {
    path: 'offres/:id',
    loadComponent: () => import('./pages/offers/offer-detail/offer-detail').then(c => c.OfferDetail)
  },
  {
    path: 'offres/:id/modifier',
    loadComponent: () => import('./pages/offers/offer-form/offer-form').then(c => c.OfferForm)
  },
  {
    path: 'candidats',
    loadComponent: () => import('./pages/candidates/candidates-list/candidates-list').then(c => c.CandidatesList)
  },
  {
    path: 'candidats/depot',
    loadComponent: () => import('./pages/candidates/candidate-dropzone/candidate-dropzone').then(c => c.CandidateDropzone)
  },
  {
    path: 'candidats/import',
    loadComponent: () => import('./pages/candidates/candidate-import/candidate-import').then(c => c.CandidateImport)
  },
  {
    path: 'candidats/:id',
    loadComponent: () => import('./pages/candidates/candidate-profile/candidate-profile').then(c => c.CandidateProfile)
  },
  {
    path: 'workflow',
    loadComponent: () => import('./pages/workflow/workflow-board/workflow-board').then(c => c.WorkflowBoard)
  },
  {
    path: 'administration',
    loadComponent: () => import('./pages/administration/user-management/user-management').then(c => c.UserManagement)
  },
  {
    path: 'parametres/general',
    loadComponent: () => import('./pages/settings/general-settings/general-settings').then(c => c.GeneralSettings)
  },
  {
    path: 'parametres/templates',
    loadComponent: () => import('./pages/settings/template-settings/template-settings').then(c => c.TemplateSettings)
  },
  { path: '**', redirectTo: 'dashboard' }
];
