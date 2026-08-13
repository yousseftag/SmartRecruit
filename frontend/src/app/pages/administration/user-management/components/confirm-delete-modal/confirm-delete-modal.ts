import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideX, LucideAlertTriangle } from '@lucide/angular';
import { UserResponse } from '../../../../../core/models/user.model';

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

        <h2 class="h2" style="margin-bottom: 12px;">Supprimer l'utilisateur</h2>
        <p style="margin-bottom: 24px; line-height: 1.5;">
          Êtes-vous sûr de vouloir supprimer définitivement l'utilisateur
          <strong>{{ user.username }}</strong> ? Cette action est irréversible.
        </p>

        <div class="modal-actions" style="justify-content: center;">
          <button type="button" class="btn btn-secondary" (click)="onClose()">Annuler</button>
          <button
            type="button"
            class="btn btn-primary"
            style="background: var(--red); border-color: var(--red);"
            (click)="onConfirm()"
          >
            Oui, supprimer
          </button>
        </div>
      </div>
    </div>
  `,
})
export class ConfirmDeleteModalComponent {
  @Input({ required: true }) user!: UserResponse;
  @Output() closeModal = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<string>(); // emits the user ID

  onClose() {
    this.closeModal.emit();
  }

  onConfirm() {
    this.confirm.emit(this.user.id);
  }
}
