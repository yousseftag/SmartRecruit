import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WORKFLOW_STATUSES } from '../../../core/constants/status.constants';
import { WorkflowStatus } from '../../../core/models/application.model';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './status-badge.html',
})
export class StatusBadge {
  readonly status = input<string | null | undefined>('');
  readonly size = input<'sm' | 'md'>('sm');

  readonly statusConfig = computed(() => {
    const raw = (this.status() || '').toUpperCase();

    // 1. Workflow Statuses (Single Source of Truth)
    if (raw in WORKFLOW_STATUSES) {
      const def = WORKFLOW_STATUSES[raw as WorkflowStatus];
      return {
        label: def.label,
        classes: def.badgeClass,
        dotClass: def.dotClass,
        pulsing: false,
      };
    }

    // 2. AI Extraction Statuses
    switch (raw) {
      case 'PENDING':
        return {
          label: 'Analyse IA en cours...',
          classes: 'bg-blue-50 text-blue-700 border border-blue-200/80 animate-pulse',
          dotClass: 'bg-blue-500 animate-ping',
          pulsing: true,
        };
      case 'STALLED':
        return {
          label: 'Analyse bloquée',
          classes: 'bg-amber-50 text-amber-800 border border-amber-300/80 font-semibold',
          dotClass: 'bg-amber-500',
          pulsing: false,
        };
      case 'SUCCESS':
        return {
          label: 'Analyse terminée',
          classes: 'bg-emerald-50 text-emerald-800 border border-emerald-300/80',
          dotClass: 'bg-emerald-600',
          pulsing: false,
        };
      case 'FAILED':
        return {
          label: 'Échec analyse IA',
          classes: 'bg-rose-50 text-rose-700 border border-rose-200/80 font-semibold',
          dotClass: 'bg-rose-500',
          pulsing: false,
        };

      // Offer Lifecycle Statuses
      case 'DRAFT':
        return {
          label: 'Brouillon',
          classes: 'bg-slate-100 text-slate-700 border border-slate-200 font-medium',
          dotClass: 'bg-slate-400',
          pulsing: false,
        };
      case 'ACTIVE':
        return {
          label: 'Active',
          classes: 'bg-green-bg text-green border border-green/30 font-semibold',
          dotClass: 'bg-green',
          pulsing: false,
        };
      case 'CLOSED':
        return {
          label: 'Clôturée',
          classes: 'bg-slate-100 text-muted border border-line',
          dotClass: 'bg-faint',
          pulsing: false,
        };

      default:
        return {
          label: this.status() || 'Inconnu',
          classes: 'bg-bg text-slate border-line',
          dotClass: 'bg-slate',
          pulsing: false,
        };
    }
  });
}
