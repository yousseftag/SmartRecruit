import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

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
    switch (raw) {
      // Workflow Statuses
      case 'NEW':
        return {
          label: 'En attente',
          classes: 'bg-blue-100 text-blue border-blue/20',
          dotClass: 'bg-blue',
          pulsing: false,
        };
      case 'SHORTLISTED':
        return {
          label: 'Convoqué',
          classes: 'bg-green-bg text-green border-green/20',
          dotClass: 'bg-green',
          pulsing: false,
        };
      case 'INTERVIEWING':
        return {
          label: 'Entretien',
          classes:
            'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300 border border-amber-300 dark:border-amber-700/50',
          dotClass: 'bg-amber-600 dark:bg-amber-400',
          pulsing: false,
        };
      case 'FOLLOW_UP':
        return {
          label: 'Suivi RH',
          classes: 'bg-violet-50 text-violet-700 border-violet-200',
          dotClass: 'bg-violet',
          pulsing: false,
        };
      case 'HIRED':
        return {
          label: 'Recruté',
          classes: 'bg-green-bg text-green border-green/30 font-bold',
          dotClass: 'bg-green',
          pulsing: false,
        };
      case 'REJECTED':
        return {
          label: 'Refusé',
          classes: 'bg-red-bg text-red-d border-red/20',
          dotClass: 'bg-red',
          pulsing: false,
        };
      case 'ARCHIVED':
        return {
          label: 'Archivé',
          classes: 'bg-bg text-muted border-line',
          dotClass: 'bg-faint',
          pulsing: false,
        };

      // AI Extraction Statuses
      case 'PENDING':
        return {
          label: 'Analyse IA en cours...',
          classes: 'bg-blue-50 text-blue border-blue/20 animate-pulse',
          dotClass: 'bg-blue animate-ping',
          pulsing: true,
        };
      case 'SUCCESS':
        return {
          label: 'Analyse terminée',
          classes: 'bg-green-bg text-green border-green/20',
          dotClass: 'bg-green',
          pulsing: false,
        };
      case 'FAILED':
        return {
          label: 'Échec analyse IA',
          classes: 'bg-red-bg text-red-d border-red/20',
          dotClass: 'bg-red',
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
