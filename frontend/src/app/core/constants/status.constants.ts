import { WorkflowStatus } from '../models/application.model';

export interface WorkflowStatusDefinition {
  value: WorkflowStatus;
  label: string;
  description: string;
  badgeClass: string;
  borderClass: string;
  dotClass: string;
  filterColor: string;
  promptEmail: boolean;
}

export const WORKFLOW_STATUSES: Record<WorkflowStatus, WorkflowStatusDefinition> = {
  NEW: {
    value: 'NEW',
    label: 'Nouveau',
    description: 'Nouvelles candidatures',
    badgeClass: 'bg-blue-50 text-blue-700 border border-blue-200/80',
    borderClass: 'border-blue-200',
    dotClass: 'bg-blue-500',
    filterColor: 'bg-blue-500',
    promptEmail: false,
  },
  SHORTLISTED: {
    value: 'SHORTLISTED',
    label: 'Présélectionné',
    description: 'Profils retenus pour examen',
    badgeClass: 'bg-indigo-50 text-indigo-700 border border-indigo-200/80',
    borderClass: 'border-indigo-200',
    dotClass: 'bg-indigo-500',
    filterColor: 'bg-indigo-500',
    promptEmail: false,
  },
  INTERVIEWING: {
    value: 'INTERVIEWING',
    label: 'En entretien',
    description: 'Entretiens programmés ou en cours',
    badgeClass: 'bg-amber-50 text-amber-800 border border-amber-300/80 font-semibold',
    borderClass: 'border-amber-300',
    dotClass: 'bg-amber-500',
    filterColor: 'bg-amber-500',
    promptEmail: true,
  },
  FOLLOW_UP: {
    value: 'FOLLOW_UP',
    label: 'À relancer',
    description: 'En attente de réponse / relance',
    badgeClass: 'bg-purple-50 text-purple-700 border border-purple-200/80',
    borderClass: 'border-purple-200',
    dotClass: 'bg-purple-500',
    filterColor: 'bg-purple-500',
    promptEmail: true,
  },
  HIRED: {
    value: 'HIRED',
    label: 'Embauché',
    description: 'Offre acceptée et validée',
    badgeClass: 'bg-emerald-50 text-emerald-800 border border-emerald-300/80 font-bold',
    borderClass: 'border-emerald-300',
    dotClass: 'bg-emerald-600',
    filterColor: 'bg-emerald-600',
    promptEmail: false,
  },
  REJECTED: {
    value: 'REJECTED',
    label: 'Refusé',
    description: 'Candidature non retenue',
    badgeClass: 'bg-rose-50 text-rose-700 border border-rose-200/80',
    borderClass: 'border-rose-200',
    dotClass: 'bg-rose-500',
    filterColor: 'bg-rose-500',
    promptEmail: true,
  },
  ARCHIVED: {
    value: 'ARCHIVED',
    label: 'Archivé',
    description: 'Dossiers clôturés / vivier',
    badgeClass: 'bg-slate-100 text-slate-600 border border-slate-200',
    borderClass: 'border-slate-200',
    dotClass: 'bg-slate-400',
    filterColor: 'bg-slate-400',
    promptEmail: false,
  },
};

export const WORKFLOW_STATUS_LIST: WorkflowStatusDefinition[] = [
  WORKFLOW_STATUSES.NEW,
  WORKFLOW_STATUSES.SHORTLISTED,
  WORKFLOW_STATUSES.INTERVIEWING,
  WORKFLOW_STATUSES.FOLLOW_UP,
  WORKFLOW_STATUSES.HIRED,
  WORKFLOW_STATUSES.REJECTED,
  WORKFLOW_STATUSES.ARCHIVED,
];
