import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-score-gauge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './score-gauge.html',
})
export class ScoreGauge implements OnChanges {
  @Input() score: number = 0;
  
  // SVG Circle properties
  circumference = 2 * Math.PI * 45; // r=45
  dashoffset = this.circumference;
  
  ngOnChanges() {
    // Calculate the stroke-dashoffset based on the score (0-100)
    const normalizedScore = Math.max(0, Math.min(100, this.score));
    this.dashoffset = this.circumference - (normalizedScore / 100) * this.circumference;
  }

  get colorClass(): string {
    if (this.score >= 80) return 'text-green';
    if (this.score >= 60) return 'text-amber';
    return 'text-red';
  }

  get summaryText(): string {
    if (this.score >= 80) return 'Profil hautement recommandé';
    if (this.score >= 60) return 'Profil à considérer';
    return 'Profil non recommandé';
  }
}
