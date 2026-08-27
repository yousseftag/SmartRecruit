import {
  Component,
  input,
  output,
  signal,
  ElementRef,
  HostListener,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  LucideDynamicIcon,
  LucideMail,
  LucideChevronDown,
  LucideCheck,
  LucidePlus,
} from '@lucide/angular';
import { EmailTemplateResponse } from '../../../core/models/workflow.model';

@Component({
  selector: 'app-template-select',
  standalone: true,
  imports: [CommonModule, LucideDynamicIcon],
  templateUrl: './template-select.html',
})
export class TemplateSelect {
  private elementRef = inject(ElementRef);

  readonly LucideMail = LucideMail;
  readonly LucideChevronDown = LucideChevronDown;
  readonly LucideCheck = LucideCheck;
  readonly LucidePlus = LucidePlus;

  readonly templates = input<EmailTemplateResponse[]>([]);
  readonly selectedTemplateId = input<string | null>(null);
  readonly showCreateOption = input<boolean>(false);
  readonly placeholder = input<string>('Sélectionner un modèle...');
  readonly disabled = input<boolean>(false);

  readonly templateChange = output<EmailTemplateResponse | null>();
  readonly createClicked = output<void>();

  isOpen = signal<boolean>(false);

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen.set(false);
    }
  }

  toggleDropdown(): void {
    if (this.disabled()) return;
    this.isOpen.update((v) => !v);
  }

  selectTemplate(template: EmailTemplateResponse): void {
    this.templateChange.emit(template);
    this.isOpen.set(false);
  }

  triggerCreate(): void {
    this.createClicked.emit();
    this.isOpen.set(false);
  }

  getSelectedTemplate(): EmailTemplateResponse | undefined {
    const id = this.selectedTemplateId();
    if (!id) return undefined;
    return this.templates().find((t) => t.id === id);
  }

  getBadgeLabel(key: string): string {
    switch (key) {
      case 'INTERVIEW_INVITATION':
        return 'Convocation';
      case 'FOLLOW_UP':
        return 'Relance';
      case 'REJECTION':
        return 'Refus';
      default:
        return 'Personnalisé';
    }
  }

  getBadgeClass(key: string): string {
    switch (key) {
      case 'INTERVIEW_INVITATION':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'FOLLOW_UP':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'REJECTION':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      default:
        return 'bg-purple-50 text-purple-700 border-purple-200';
    }
  }
}
