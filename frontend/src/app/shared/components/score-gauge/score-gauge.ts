import { Component, input, computed } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-score-gauge',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './score-gauge.html',
})
export class ScoreGauge {
  readonly score = input<number | null>(0);
  readonly showSummary = input<boolean>(true);

  readonly circumference = 2 * Math.PI * 45; // r=45 -> ~282.74

  readonly dashoffset = computed(() => {
    const s = this.score() ?? 0;
    const normalizedScore = Math.max(0, Math.min(100, s));
    return this.circumference - (normalizedScore / 100) * this.circumference;
  });

  readonly colorClass = computed(() => {
    const s = this.score() ?? 0;
    if (s >= 80) return 'text-green';
    if (s >= 60) return 'text-amber-700 dark:text-amber-400';
    return 'text-red';
  });

  readonly summaryText = computed(() => {
    const s = this.score();
    if (s === null || s === undefined) {
      return "En attente d'évaluation";
    }
    if (s >= 80) return 'Profil hautement recommandé';
    if (s >= 60) return 'Profil à considérer';
    return 'Profil non recommandé';
  });
}

