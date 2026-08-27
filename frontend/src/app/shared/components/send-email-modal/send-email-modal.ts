import {
  Component,
  OnInit,
  OnChanges,
  SimpleChanges,
  input,
  output,
  signal,
  computed,
  inject,
  ViewChild,
  ElementRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideDynamicIcon,
  LucideMail,
  LucideSend,
  LucideX,
  LucideAlertCircle,
  LucideBold,
  LucideItalic,
  LucideList,
  LucideUser,
  LucideBriefcase,
  LucideEye,
  LucideSparkles,
} from '@lucide/angular';
import { WorkflowService } from '../../../core/services/workflow.service';
import { AuthService } from '../../../core/auth/auth.service';
import { EmailTemplateResponse } from '../../../core/models/workflow.model';
import { TemplateSelect } from '../template-select/template-select';

@Component({
  selector: 'app-send-email-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideDynamicIcon, TemplateSelect],
  templateUrl: './send-email-modal.html',
})
export class SendEmailModal implements OnInit, OnChanges {
  private workflowService = inject(WorkflowService);
  private authService = inject(AuthService);

  @ViewChild('subjectInput') subjectInputRef?: ElementRef<HTMLInputElement>;
  @ViewChild('bodyTextarea') bodyTextareaRef?: ElementRef<HTMLTextAreaElement>;

  // Icons
  readonly LucideMail = LucideMail;
  readonly LucideSend = LucideSend;
  readonly LucideX = LucideX;
  readonly LucideAlertCircle = LucideAlertCircle;
  readonly LucideBold = LucideBold;
  readonly LucideItalic = LucideItalic;
  readonly LucideList = LucideList;
  readonly LucideUser = LucideUser;
  readonly LucideBriefcase = LucideBriefcase;
  readonly LucideEye = LucideEye;
  readonly LucideSparkles = LucideSparkles;

  // Inputs
  readonly isOpen = input<boolean>(false);
  readonly applicationId = input<string | null>(null);
  readonly candidateName = input<string>('Candidat');
  readonly candidateEmail = input<string>('');
  readonly offerTitle = input<string>('Poste');
  readonly targetStatus = input<string | null>(null);

  // Outputs
  readonly close = output<void>();
  readonly emailSent = output<void>();

  // State Signals
  templates = signal<EmailTemplateResponse[]>([]);
  selectedTemplate = signal<EmailTemplateResponse | null>(null);
  formSubject = signal<string>('');
  formBodyHtml = signal<string>('');
  isLoadingTemplates = signal<boolean>(false);
  isSending = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  // Minimal variable chips
  readonly quickVariables = [
    { tag: 'nom_candidat', label: 'Candidat' },
    { tag: 'titre_offre', label: 'Offre' },
    { tag: 'nom_recruteur', label: 'Recruteur' },
    { tag: 'email_candidat', label: 'Email candidat' },
  ];

  hasGhostEmail = computed(() => {
    const email = this.candidateEmail();
    return !email || !email.includes('@');
  });

  recruiterName = computed(() => {
    const user = this.authService.currentUser();
    return user
      ? user.fullName || `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.preferredUsername
      : 'L\'équipe de recrutement';
  });

  // Dynamic context for live preview
  sampleContext = computed(() => ({
    nom_candidat: this.candidateName() || 'Candidat',
    titre_offre: this.offerTitle() || 'Offre',
    nom_recruteur: this.recruiterName(),
    email_candidat: this.candidateEmail() || '',
  }));

  // Dynamic preview signals
  previewSubject = computed(() => {
    return this.interpolate(this.formSubject(), this.sampleContext());
  });

  previewBodyHtml = computed(() => {
    return this.interpolate(this.formBodyHtml(), this.sampleContext());
  });

  ngOnInit(): void {
    this.loadTemplates();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen'] && this.isOpen()) {
      this.errorMessage.set(null);
      if (this.templates().length > 0) {
        this.autoSelectTemplateForStatus();
      } else {
        this.loadTemplates();
      }
    }
  }

  loadTemplates(): void {
    this.isLoadingTemplates.set(true);
    this.workflowService.getTemplates().subscribe({
      next: (data) => {
        this.templates.set(data);
        this.isLoadingTemplates.set(false);
        if (this.isOpen()) {
          this.autoSelectTemplateForStatus();
        }
      },
      error: (err) => {
        this.isLoadingTemplates.set(false);
        this.errorMessage.set('Erreur lors du chargement des modèles.');
        console.error('Failed to load templates', err);
      },
    });
  }

  autoSelectTemplateForStatus(): void {
    const status = (this.targetStatus() || '').toUpperCase();
    const all = this.templates();
    if (all.length === 0) return;

    let matched: EmailTemplateResponse | undefined;

    if (status === 'INTERVIEWING' || status === 'SHORTLISTED') {
      matched = all.find((t) => t.templateKey === 'INTERVIEW_INVITATION');
    } else if (status === 'FOLLOW_UP') {
      matched = all.find((t) => t.templateKey === 'FOLLOW_UP');
    } else if (status === 'REJECTED') {
      matched = all.find((t) => t.templateKey === 'REJECTION');
    }

    const templateToUse = matched || all[0];
    this.selectTemplate(templateToUse);
  }

  onTemplateChange(template: EmailTemplateResponse | null): void {
    if (template) {
      this.selectTemplate(template);
    }
  }

  selectTemplate(template: EmailTemplateResponse): void {
    this.selectedTemplate.set(template);
    this.formSubject.set(template.subject);
    this.formBodyHtml.set(template.bodyHtml);
  }

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

  onDismiss(): void {
    this.close.emit();
  }

  sendEmail(): void {
    const appId = this.applicationId();
    if (!appId) {
      this.errorMessage.set('Identifiant de candidature manquant.');
      return;
    }

    if (this.hasGhostEmail()) {
      this.errorMessage.set(
        "Impossible d'envoyer l'email : aucune adresse email n'est associée à ce candidat.",
      );
      return;
    }

    const subject = this.formSubject().trim();
    const bodyHtml = this.formBodyHtml().trim();

    if (!subject || !bodyHtml) {
      this.errorMessage.set('Le sujet et le corps du message sont obligatoires.');
      return;
    }

    this.isSending.set(true);
    this.errorMessage.set(null);

    this.workflowService
      .sendEmail(appId, { subject, bodyHtml })
      .subscribe({
        next: () => {
          this.isSending.set(false);
          this.emailSent.emit();
          this.close.emit();
        },
        error: (err) => {
          this.isSending.set(false);
          const msg =
            err.error?.message ||
            "Une erreur est survenue lors de l'envoi de l'email.";
          this.errorMessage.set(msg);
          console.error('Failed to send email', err);
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
}
