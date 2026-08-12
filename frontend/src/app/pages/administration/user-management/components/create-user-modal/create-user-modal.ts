import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LucideX } from '@lucide/angular';
import { CreateUserRequest } from '../../../../../core/models/user.model';

@Component({
  selector: 'app-create-user-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideX],
  template: `
    <div class="modal-backdrop">
      <div class="modal modal-content" style="padding: 24px;">
        <button class="modal-x" type="button" (click)="onClose()">
          <svg lucideX></svg>
        </button>

        <h2 class="h2" style="margin-bottom: 24px;">Ajouter un utilisateur</h2>

        <form [formGroup]="userForm" (ngSubmit)="onSubmit()">
          <div style="display: grid; gap: 16px;">
            <div class="form-group">
              <label>Nom d'utilisateur <span class="text-red">*</span></label>
              <input type="text" formControlName="username" class="input" placeholder="ex: jdoe" />
            </div>

            <div class="form-group">
              <label>Adresse Email <span class="text-red">*</span></label>
              <input
                type="email"
                formControlName="email"
                class="input"
                placeholder="jean.dupont@norsys.fr"
              />
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
              <div class="form-group">
                <label>Prénom</label>
                <input type="text" formControlName="firstName" class="input" placeholder="Jean" />
              </div>
              <div class="form-group">
                <label>Nom</label>
                <input type="text" formControlName="lastName" class="input" placeholder="Dupont" />
              </div>
            </div>

            <div class="form-group">
              <label>Rôle <span class="text-red">*</span></label>
              <select formControlName="role" class="input">
                <option value="VIEWER">Consultation</option>
                <option value="RECRUITER">Recruteur</option>
                <option value="HR_ADMIN">Admin RH</option>
              </select>
            </div>
          </div>

          <div class="modal-actions" style="margin-top: 32px;">
            <button type="button" class="btn btn-secondary" (click)="onClose()">Annuler</button>
            <button type="submit" class="btn btn-primary" [disabled]="userForm.invalid">
              Créer l'utilisateur
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class CreateUserModalComponent {
  @Output() closeModal = new EventEmitter<void>();
  @Output() save = new EventEmitter<CreateUserRequest>();

  private fb = inject(FormBuilder);

  userForm: FormGroup = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    firstName: [''],
    lastName: [''],
    role: ['VIEWER', Validators.required],
  });

  onClose() {
    this.closeModal.emit();
  }

  onSubmit() {
    if (this.userForm.valid) {
      this.save.emit(this.userForm.value as CreateUserRequest);
    }
  }
}
