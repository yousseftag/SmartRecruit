import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'experienceFormat',
  standalone: true,
})
export class ExperienceFormatPipe implements PipeTransform {
  transform(months: number | null | undefined, includeSuffix = false): string {
    if (months === undefined || months === null) return 'Non spécifié';
    if (months === 0) return 'Débutant (0 mois)';

    const suffix = includeSuffix ? " d'expérience" : '';

    if (months < 12) return `${months} mois${suffix}`;

    const years = Math.floor(months / 12);
    const remaining = months % 12;
    const yearStr = years > 1 ? `${years} ans` : `${years} an`;

    if (remaining === 0) {
      return `${yearStr}${suffix}`;
    }
    return `${yearStr} et ${remaining} mois${suffix}`;
  }
}
