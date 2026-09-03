import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideX, LucideAlertTriangle } from '@lucide/angular';

@Component({
  selector: 'app-confirm-delete-modal',
  standalone: true,
  imports: [CommonModule, LucideX, LucideAlertTriangle],
  template: `
    <div class="modal-backdrop">
      <div class="modal modal-content" style="padding: 24px; max-width: 400px; text-align: center;">
        <button class="modal-x" type="button" (click)="onClose()">
          <svg lucideX></svg>
        </button>

        <div class="modal-ic" style="background: var(--red-bg); color: var(--red);">
          <svg lucideAlertTriangle></svg>
        </div>

        <h2 class="h2" style="margin-bottom: 12px;">{{ title }}</h2>
        <p style="margin-bottom: 24px; line-height: 1.5;">
          @if (message) {
            {{ message }}
          } @else {
            Êtes-vous sûr de vouloir supprimer définitivement
            <strong>{{ itemName || 'cet élément' }}</strong> ? {{ warningText }}
          }
        </p>

        <div class="modal-actions" style="justify-content: center;">
          <button type="button" class="btn btn-secondary" (click)="onClose()">
            {{ cancelText }}
          </button>
          <button
            type="button"
            class="btn btn-primary"
            [style.background]="isDanger ? 'var(--red)' : ''"
            [style.border-color]="isDanger ? 'var(--red)' : ''"
            (click)="onConfirm()"
          >
            {{ confirmText }}
          </button>
        </div>
      </div>
    </div>
  `,
})
export class ConfirmDeleteModalComponent {
  @Input() title: string = "Supprimer l'utilisateur";
  @Input() itemName?: string;
  @Input() message?: string;
  @Input() warningText: string = 'Cette action est irréversible.';
  @Input() confirmText: string = 'Oui, supprimer';
  @Input() cancelText: string = 'Annuler';
  @Input() isDanger: boolean = true;

  @Output() closeModal = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();

  onClose() {
    this.closeModal.emit();
  }

  onConfirm() {
    this.confirm.emit();
  }
}
