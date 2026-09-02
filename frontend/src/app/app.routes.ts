import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { UserRole } from './core/models/user.model';
import { MainLayout } from './layout/main-layout/main-layout';

export const routes: Routes = [
  { path: '', redirectTo: 'careers', pathMatch: 'full' },
  {
    path: 'sandbox',
    loadComponent: () => import('./pages/sandbox/sandbox').then((c) => c.Sandbox),
  },

  {
    path: 'careers',
    loadComponent: () =>
      import('./pages/public/careers-list/careers-list').then((c) => c.CareersList),
  },
  {
    path: 'careers/:id',
    loadComponent: () =>
      import('./pages/public/career-detail/career-detail').then((c) => c.CareerDetail),
  },

  {
    path: 'hr',
    component: MainLayout,
    canActivateChild: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      // --- Read-Only / General Routes (Accessible to HR_ADMIN, RECRUITER, VIEWER) ---
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard').then((c) => c.Dashboard),
      },
      // --- Offers Routes ---
      {
        path: 'offers',
        loadComponent: () =>
          import('./pages/offers/offers-list/offers-list').then((c) => c.OffersList),
      },
      {
        path: 'offers/new',
        loadComponent: () =>
          import('./pages/offers/offer-form/offer-form').then((c) => c.OfferForm),
        data: { roles: [UserRole.HR_ADMIN, UserRole.RECRUITER] },
      },
      {
        path: 'offers/:id/edit',
        loadComponent: () =>
          import('./pages/offers/offer-form/offer-form').then((c) => c.OfferForm),
        data: { roles: [UserRole.HR_ADMIN, UserRole.RECRUITER] },
      },
      {
        path: 'offers/:id',
        loadComponent: () =>
          import('./pages/offers/offer-detail/offer-detail').then((c) => c.OfferDetail),
      },

      // --- Candidates Routes ---
      {
        path: 'candidates',
        loadComponent: () =>
          import('./pages/candidates/candidates-list/candidates-list').then(
            (c) => c.CandidatesList,
          ),
      },
      {
        path: 'candidates/import',
        loadComponent: () =>
          import('./pages/candidates/candidate-import/candidate-import').then(
            (c) => c.CandidateImport,
          ),
        data: { roles: [UserRole.HR_ADMIN, UserRole.RECRUITER] },
      },
      {
        path: 'candidates/:id',
        loadComponent: () =>
          import('./pages/candidates/candidate-profile/candidate-profile').then(
            (c) => c.CandidateProfile,
          ),
      },

      // --- Workflow & Reporting Routes ---
      {
        path: 'workflow',
        loadComponent: () =>
          import('./pages/workflow/workflow-board/workflow-board').then((c) => c.WorkflowBoard),
      },
      {
        path: 'reporting',
        loadComponent: () => import('./pages/reporting/reporting').then((c) => c.Reporting),
      },

      // --- Admin/Settings Routes (Accessible ONLY to HR_ADMIN) ---
      {
        path: 'administration',
        loadComponent: () =>
          import('./pages/administration/user-management/user-management').then(
            (c) => c.UserManagement,
          ),
        data: { roles: [UserRole.HR_ADMIN] },
      },
      {
        path: 'settings/general',
        loadComponent: () =>
          import('./pages/settings/general-settings/general-settings').then(
            (c) => c.GeneralSettings,
          ),
        data: { roles: [UserRole.HR_ADMIN] },
      },
      {
        path: 'settings/templates',
        loadComponent: () =>
          import('./pages/settings/template-settings/template-settings').then(
            (c) => c.TemplateSettings,
          ),
        data: { roles: [UserRole.HR_ADMIN] },
      },
    ],
  },

  {
    path: '**',
    loadComponent: () =>
      import('./pages/not-found/not-found.component').then((c) => c.NotFoundComponent),
  },
];
