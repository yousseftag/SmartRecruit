import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { MainLayout } from './layout/main-layout/main-layout';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

  {
    path: 'careers',
    loadComponent: () => import('./pages/public/careers-list/careers-list').then((c) => c.CareersList),
  },
  {
    path: 'careers/:id',
    loadComponent: () => import('./pages/public/career-detail/career-detail').then((c) => c.CareerDetail),
  },

  {
    path: '',
    component: MainLayout,
    canActivateChild: [authGuard],
    children: [
      // --- Read-Only / General Routes (Accessible to HR_ADMIN, RECRUITER, VIEWER) ---
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard').then((c) => c.Dashboard),
      },
      {
        path: 'offers',
        loadComponent: () =>
          import('./pages/offers/offers-list/offers-list').then((c) => c.OffersList),
      },
      {
        path: 'offers/:id',
        loadComponent: () =>
          import('./pages/offers/offer-detail/offer-detail').then((c) => c.OfferDetail),
      },
      {
        path: 'candidates',
        loadComponent: () =>
          import('./pages/candidates/candidates-list/candidates-list').then(
            (c) => c.CandidatesList,
          ),
      },
      {
        path: 'candidates/:id',
        loadComponent: () =>
          import('./pages/candidates/candidate-profile/candidate-profile').then(
            (c) => c.CandidateProfile,
          ),
      },
      {
        path: 'workflow',
        loadComponent: () =>
          import('./pages/workflow/workflow-board/workflow-board').then((c) => c.WorkflowBoard),
      },
      {
        path: 'reporting',
        loadComponent: () => import('./pages/reporting/reporting').then((c) => c.Reporting),
      },

      // --- Write/Action Routes (Accessible ONLY to HR_ADMIN and RECRUITER) ---
      {
        path: 'offers/new',
        loadComponent: () =>
          import('./pages/offers/offer-form/offer-form').then((c) => c.OfferForm),
        data: { roles: ['HR_ADMIN', 'RECRUITER'] },
      },
      {
        path: 'offers/:id/edit',
        loadComponent: () =>
          import('./pages/offers/offer-form/offer-form').then((c) => c.OfferForm),
        data: { roles: ['HR_ADMIN', 'RECRUITER'] },
      },
      {
        path: 'candidates/import',
        loadComponent: () =>
          import('./pages/candidates/candidate-import/candidate-import').then(
            (c) => c.CandidateImport,
          ),
        data: { roles: ['HR_ADMIN', 'RECRUITER'] },
      },

      // --- Admin/Settings Routes (Accessible ONLY to HR_ADMIN) ---
      {
        path: 'administration',
        loadComponent: () =>
          import('./pages/administration/user-management/user-management').then(
            (c) => c.UserManagement,
          ),
        data: { roles: ['HR_ADMIN'] },
      },
      {
        path: 'settings/general',
        loadComponent: () =>
          import('./pages/settings/general-settings/general-settings').then(
            (c) => c.GeneralSettings,
          ),
        data: { roles: ['HR_ADMIN'] },
      },
      {
        path: 'settings/templates',
        loadComponent: () =>
          import('./pages/settings/template-settings/template-settings').then(
            (c) => c.TemplateSettings,
          ),
        data: { roles: ['HR_ADMIN'] },
      },
    ],
  },

  {
    path: '**',
    loadComponent: () =>
      import('./pages/not-found/not-found.component').then((c) => c.NotFoundComponent),
  },
];
