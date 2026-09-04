import { Component, input, model, output, computed, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucidePlus,
  LucideX,
  LucideCheck,
  LucideAlertTriangle,
  LucideStar,
  LucideSparkles,
  LucideScale,
  LucideGraduationCap,
  LucideGlobe,
  LucideMapPin,
  LucideBriefcase,
} from '@lucide/angular';
import { CategoryWeights, CategoryCriteria } from '../../../core/models/offer.model';

@Component({
  selector: 'app-weighted-criteria-grid',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    LucidePlus,
    LucideX,
    LucideCheck,
    LucideAlertTriangle,
    LucideStar,
    LucideSparkles,
    LucideScale,
    LucideGraduationCap,
    LucideGlobe,
    LucideMapPin,
    LucideBriefcase,
  ],
  templateUrl: './weighted-criteria-grid.html',
})
export class WeightedCriteriaGrid {
  /** Mode: read-only for offer details view, editable for create/edit form */
  readonly readOnly = input<boolean>(false);

  /** Category weights (must sum to 100) */
  readonly weights = model<CategoryWeights>({
    skills: 40,
    experience: 25,
    coursework: 15,
    languages: 10,
    localization: 10,
  });

  /** Detailed criteria tags, months, ratings */
  readonly criteria = model<CategoryCriteria>({
    skills: [],
    skill_weights: {},
    experience: 0,
    coursework: [],
    languages: [],
    localization: '',
  });

  /** Emits whether total weight is valid (exactly 100) */
  readonly sumValid = output<boolean>();

  // Temporary inputs for chip adding
  newSkillName = signal<string>('');
  newSkillWeight = signal<number>(3);
  newDegreeName = signal<string>('');
  newLanguageName = signal<string>('');

  constructor() {
    effect(() => {
      this.sumValid.emit(this.totalWeight() === 100);
    });
  }

  // Total weight computed
  readonly totalWeight = computed(() => {
    const w = this.weights();
    const skills = Number(w?.skills) || 0;
    const exp = Number(w?.experience) || 0;
    const course = Number(w?.coursework) || 0;
    const lang = Number(w?.languages) || 0;
    const loc = Number(w?.localization) || 0;
    return skills + exp + course + lang + loc;
  });

  readonly isWeightValid = computed(() => this.totalWeight() === 100);

  // Helper for humanized experience
  readonly experienceLabel = computed(() => {
    const months = this.criteria()?.experience ?? 0;
    if (!months || months <= 0) return 'Débutant / Non spécifié';
    const years = Math.floor(months / 12);
    const remMonths = months % 12;
    if (years > 0 && remMonths > 0) {
      return `${months} mois (${years} an${years > 1 ? 's' : ''} et ${remMonths} mois)`;
    } else if (years > 0) {
      return `${months} mois (${years} an${years > 1 ? 's' : ''})`;
    } else {
      return `${months} mois`;
    }
  });

  // Category weight change helper
  updateWeight(category: keyof CategoryWeights, value: number) {
    if (this.readOnly()) return;
    const current = { ...this.weights() };
    current[category] = Math.max(0, Math.min(100, Number(value) || 0));
    this.weights.set(current);
  }

  // Preset balances
  applyPreset(preset: 'balanced' | 'tech' | 'junior' | 'exec') {
    if (this.readOnly()) return;
    let newWeights: CategoryWeights;
    switch (preset) {
      case 'tech':
        newWeights = { skills: 50, experience: 25, coursework: 10, languages: 10, localization: 5 };
        break;
      case 'junior':
        newWeights = {
          skills: 35,
          experience: 10,
          coursework: 35,
          languages: 10,
          localization: 10,
        };
        break;
      case 'exec':
        newWeights = { skills: 30, experience: 40, coursework: 15, languages: 10, localization: 5 };
        break;
      case 'balanced':
      default:
        newWeights = {
          skills: 20,
          experience: 20,
          coursework: 20,
          languages: 20,
          localization: 20,
        };
        break;
    }
    this.weights.set(newWeights);
  }

  // --- Skills management with skill_weights (1 to 5) ---

  addSkill() {
    if (this.readOnly()) return;
    const name = this.newSkillName().trim();
    if (!name) return;

    const currentCrit = { ...this.criteria() };
    const skills = [...(currentCrit.skills || [])];
    const skillWeights = { ...(currentCrit.skill_weights || {}) };

    if (!skills.includes(name)) {
      skills.push(name);
    }
    skillWeights[name] = this.newSkillWeight();

    currentCrit.skills = skills;
    currentCrit.skill_weights = skillWeights;
    this.criteria.set(currentCrit);

    this.newSkillName.set('');
    this.newSkillWeight.set(3);
  }

  removeSkill(skill: string) {
    if (this.readOnly()) return;
    const currentCrit = { ...this.criteria() };
    currentCrit.skills = (currentCrit.skills || []).filter((s) => s !== skill);
    if (currentCrit.skill_weights) {
      const copy = { ...currentCrit.skill_weights };
      delete copy[skill];
      currentCrit.skill_weights = copy;
    }
    this.criteria.set(currentCrit);
  }

  setSkillRating(skill: string, rating: number) {
    if (this.readOnly()) return;
    const currentCrit = { ...this.criteria() };
    const skillWeights = { ...(currentCrit.skill_weights || {}) };
    skillWeights[skill] = rating;
    currentCrit.skill_weights = skillWeights;
    this.criteria.set(currentCrit);
  }

  getSkillRating(skill: string): number {
    return this.criteria()?.skill_weights?.[skill] ?? 3;
  }

  getRatingLabel(rating: number): string {
    switch (rating) {
      case 5:
        return 'Critique / Expert (5/5)';
      case 4:
        return 'Avancé (4/5)';
      case 3:
        return 'Opérationnel (3/5)';
      case 2:
        return 'Intermédiaire (2/5)';
      case 1:
      default:
        return 'Notions (1/5)';
    }
  }

  // --- Coursework management ---

  addDegree() {
    if (this.readOnly()) return;
    const name = this.newDegreeName().trim();
    if (!name) return;

    const currentCrit = { ...this.criteria() };
    const coursework = [...(currentCrit.coursework || [])];
    if (!coursework.includes(name)) {
      coursework.push(name);
    }
    currentCrit.coursework = coursework;
    this.criteria.set(currentCrit);
    this.newDegreeName.set('');
  }

  removeDegree(degree: string) {
    if (this.readOnly()) return;
    const currentCrit = { ...this.criteria() };
    currentCrit.coursework = (currentCrit.coursework || []).filter((d) => d !== degree);
    this.criteria.set(currentCrit);
  }

  // --- Languages management ---

  addLanguage() {
    if (this.readOnly()) return;
    const name = this.newLanguageName().trim();
    if (!name) return;

    const currentCrit = { ...this.criteria() };
    const languages = [...(currentCrit.languages || [])];
    if (!languages.includes(name)) {
      languages.push(name);
    }
    currentCrit.languages = languages;
    this.criteria.set(currentCrit);
    this.newLanguageName.set('');
  }

  removeLanguage(lang: string) {
    if (this.readOnly()) return;
    const currentCrit = { ...this.criteria() };
    currentCrit.languages = (currentCrit.languages || []).filter((l) => l !== lang);
    this.criteria.set(currentCrit);
  }

  // --- Experience & Location ---

  updateExperience(months: number) {
    if (this.readOnly()) return;
    const currentCrit = { ...this.criteria() };
    currentCrit.experience = Math.max(0, Number(months) || 0);
    this.criteria.set(currentCrit);
  }

  updateLocalization(loc: string) {
    if (this.readOnly()) return;
    const currentCrit = { ...this.criteria() };
    currentCrit.localization = loc;
    this.criteria.set(currentCrit);
  }
}
