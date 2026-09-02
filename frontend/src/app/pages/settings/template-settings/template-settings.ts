import { Component, OnInit, inject, signal, computed, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideDynamicIcon,
  LucideMail,
  LucidePlus,
  LucideTrash2,
  LucideSave,
  LucideEye,
  LucideBold,
  LucideItalic,
  LucideList,
  LucideSparkles,
} from '@lucide/angular';
import { WorkflowService } from '../../../core/services/workflow.service';
import {
  CreateEmailTemplateRequest,
  EmailTemplateResponse,
  UpdateEmailTemplateRequest,
} from '../../../core/models/workflow.model';
import { AuthService } from '../../../core/auth/auth.service';
import { TemplateSelect } from '../../../shared/components/template-select/template-select';

@Component({
  selector: 'app-template-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideDynamicIcon, TemplateSelect],
  templateUrl: './template-settings.html',
})
export class TemplateSettings implements OnInit {
  private workflowService = inject(WorkflowService);
  private authService = inject(AuthService);

  @ViewChild('subjectInput') subjectInputRef?: ElementRef<HTMLInputElement>;
  @ViewChild('bodyTextarea') bodyTextareaRef?: ElementRef<HTMLTextAreaElement>;

  // Icons
  readonly LucideMail = LucideMail;
  readonly LucidePlus = LucidePlus;
  readonly LucideTrash2 = LucideTrash2;
  readonly LucideSave = LucideSave;
  readonly LucideEye = LucideEye;
  readonly LucideBold = LucideBold;
  readonly LucideItalic = LucideItalic;
  readonly LucideList = LucideList;
  readonly LucideSparkles = LucideSparkles;

  // Minimal variable chips
  readonly quickVariables = [
    { tag: 'nom_candidat', label: 'Nom candidat' },
    { tag: 'titre_offre', label: 'Titre offre' },
    { tag: 'nom_recruteur', label: 'Nom recruteur' },
    { tag: 'email_candidat', label: 'Email candidat' },
  ];

  // State Signals
  templates = signal<EmailTemplateResponse[]>([]);
  selectedTemplate = signal<EmailTemplateResponse | null>(null);
  isCreatingNew = signal<boolean>(false);
  isLoading = signal<boolean>(false);
  isSaving = signal<boolean>(false);
  isDeleting = signal<boolean>(false);
  showDeleteConfirm = signal<boolean>(false);

  // Form Signals
  formName = signal<string>('');
  formSubject = signal<string>('');
  formBodyHtml = signal<string>('');

  // Toast
  toastMessage = signal<string | null>(null);
  toastType = signal<'success' | 'error'>('success');

  // Sample data for simulated preview
  sampleContext = computed(() => {
    const user = this.authService.currentUser();
    const recruiterName = user
      ? user.fullName ||
        `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
        user.preferredUsername
      : 'Sophie Martin';

    return {
      nom_candidat: 'Jean Dupont',
      titre_offre: 'Développeur Full-Stack Senior',
      nom_recruteur: recruiterName,
      email_candidat: 'jean.dupont@example.com',
    };
  });

  // Dynamic preview
  previewSubject = computed(() => {
    return this.interpolate(this.formSubject(), this.sampleContext());
  });

  previewBodyHtml = computed(() => {
    return this.interpolate(this.formBodyHtml(), this.sampleContext());
  });

  ngOnInit(): void {
    this.loadTemplates();
  }

  loadTemplates(selectId?: string): void {
    this.isLoading.set(true);
    this.workflowService.getTemplates().subscribe({
      next: (data) => {
        this.templates.set(data);
        this.isLoading.set(false);

        if (selectId) {
          const target = data.find((t) => t.id === selectId);
          if (target) {
            this.selectTemplate(target);
            return;
          }
        }

        if (data.length > 0 && !this.selectedTemplate() && !this.isCreatingNew()) {
          this.selectTemplate(data[0]);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.showToast('Erreur lors du chargement des modèles.', 'error');
        console.error('Failed to load email templates', err);
      },
    });
  }

  onDropdownChange(template: EmailTemplateResponse | null): void {
    if (template) {
      this.selectTemplate(template);
    }
  }

  selectTemplate(template: EmailTemplateResponse): void {
    this.isCreatingNew.set(false);
    this.selectedTemplate.set(template);
    this.formName.set(template.name);
    this.formSubject.set(template.subject);
    this.formBodyHtml.set(template.bodyHtml);
  }

  startCreateNew(): void {
    this.isCreatingNew.set(true);
    this.selectedTemplate.set(null);
    this.formName.set('Nouveau modèle personnalisé');
    this.formSubject.set('Objet de votre email — {{titre_offre}}');
    this.formBodyHtml.set(
      '<p>Bonjour {{nom_candidat}},</p>\n<p>Nous vous contactons au sujet de votre candidature pour le poste de <strong>{{titre_offre}}</strong>.</p>\n<p>Cordialement,<br>{{nom_recruteur}}<br>SmartRecruit</p>',
    );
  }

  cancelEditing(): void {
    if (this.isCreatingNew()) {
      const all = this.templates();
      if (all.length > 0) {
        this.selectTemplate(all[0]);
      }
    } else if (this.selectedTemplate()) {
      this.selectTemplate(this.selectedTemplate()!);
    }
  }

  /**
   * Inserts dynamic variable placeholder at current cursor position
   */
  insertTag(tag: string, field: 'subject' | 'body' = 'body'): void {
    const placeholder = `{{${tag}}}`;
    const el =
      field === 'subject'
        ? this.subjectInputRef?.nativeElement
        : this.bodyTextareaRef?.nativeElement;

    if (el) {
      const start = el.selectionStart ?? el.value.length;
      const end = el.selectionEnd ?? el.value.length;
      const val = el.value;
      const updated = val.substring(0, start) + placeholder + val.substring(end);

      if (field === 'subject') {
        this.formSubject.set(updated);
      } else {
        this.formBodyHtml.set(updated);
      }

      setTimeout(() => {
        el.focus();
        const nextPos = start + placeholder.length;
        el.setSelectionRange(nextPos, nextPos);
      }, 0);
    } else {
      if (field === 'subject') {
        this.formSubject.update((s) => s + ' ' + placeholder);
      } else {
        this.formBodyHtml.update((b) => b + ' ' + placeholder);
      }
    }
  }

  /**
   * Inserts HTML formatting tags around selected text or at cursor position
   */
  insertHtmlTag(openTag: string, closeTag: string): void {
    const el = this.bodyTextareaRef?.nativeElement;
    if (el) {
      const start = el.selectionStart ?? el.value.length;
      const end = el.selectionEnd ?? el.value.length;
      const val = el.value;
      const selected = val.substring(start, end);
      const inserted = openTag + selected + closeTag;
      const updated = val.substring(0, start) + inserted + val.substring(end);

      this.formBodyHtml.set(updated);

      setTimeout(() => {
        el.focus();
        if (selected.length > 0) {
          el.setSelectionRange(start, start + inserted.length);
        } else {
          const cursorInside = start + openTag.length;
          el.setSelectionRange(cursorInside, cursorInside);
        }
      }, 0);
    } else {
      this.formBodyHtml.update((b) => `${b}${openTag}texte${closeTag}`);
    }
  }

  saveTemplate(): void {
    const name = this.formName().trim();
    const subject = this.formSubject().trim();
    const bodyHtml = this.formBodyHtml().trim();

    if (!name || !subject || !bodyHtml) {
      this.showToast('Veuillez remplir tous les champs obligatoires.', 'error');
      return;
    }

    this.isSaving.set(true);

    if (this.isCreatingNew()) {
      const request: CreateEmailTemplateRequest = { name, subject, bodyHtml };
      this.workflowService.createTemplate(request).subscribe({
        next: (created) => {
          this.isSaving.set(false);
          this.showToast('Modèle créé avec succès !', 'success');
          this.loadTemplates(created.id);
        },
        error: (err) => {
          this.isSaving.set(false);
          this.showToast('Erreur lors de la création du modèle.', 'error');
          console.error('Failed to create template', err);
        },
      });
    } else if (this.selectedTemplate()) {
      const id = this.selectedTemplate()!.id;
      const request: UpdateEmailTemplateRequest = { name, subject, bodyHtml };
      this.workflowService.updateTemplate(id, request).subscribe({
        next: (updated) => {
          this.isSaving.set(false);
          this.showToast('Modèle enregistré avec succès !', 'success');
          this.loadTemplates(updated.id);
        },
        error: (err) => {
          this.isSaving.set(false);
          this.showToast("Erreur lors de l'enregistrement du modèle.", 'error');
          console.error('Failed to update template', err);
        },
      });
    }
  }

  openDeleteModal(): void {
    this.showDeleteConfirm.set(true);
  }

  closeDeleteModal(): void {
    this.showDeleteConfirm.set(false);
  }

  executeDelete(): void {
    const template = this.selectedTemplate();
    if (!template || template.templateKey !== 'OTHER') return;

    this.isDeleting.set(true);
    this.workflowService.deleteTemplate(template.id).subscribe({
      next: () => {
        this.isDeleting.set(false);
        this.closeDeleteModal();
        this.showToast(`Modèle "${template.name}" supprimé.`, 'success');
        this.selectedTemplate.set(null);
        this.loadTemplates();
      },
      error: (err) => {
        this.isDeleting.set(false);
        const msg = err.error?.message || 'Erreur lors de la suppression.';
        this.showToast(msg, 'error');
        console.error('Failed to delete template', err);
      },
    });
  }

  private interpolate(text: string, variables: Record<string, string>): string {
    if (!text) return '';
    let res = text;
    for (const [key, value] of Object.entries(variables)) {
      res = res.replaceAll(`{{${key}}}`, value || '');
    }
    return res;
  }

  private showToast(msg: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage.set(msg);
    this.toastType.set(type);
    setTimeout(() => {
      this.toastMessage.set(null);
    }, 4000);
  }
}
