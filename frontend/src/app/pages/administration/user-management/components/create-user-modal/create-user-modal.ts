import { Component, EventEmitter, Output, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LucideX } from '@lucide/angular';
import { CreateUserRequest } from '../../../../../core/models/user.model';

@Component({
  selector: 'app-create-user-modal',
  standalone: true,
  imports: [ReactiveFormsModule, LucideX],
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
              @if (userForm.get('username')?.invalid && userForm.get('username')?.touched) {
                <div style="color: #ef4444; font-size: 0.875rem; margin-top: 4px;">
                  @if (userForm.get('username')?.errors?.['required']) {
                    <span>Ce champ est requis.</span>
                  }
                  @if (userForm.get('username')?.errors?.['minlength']) {
                    <span>Doit contenir au moins 3 caractères.</span>
                  }
                  @if (userForm.get('username')?.errors?.['pattern']) {
                    <span>Ne doit pas contenir d'espaces ni de caractères spéciaux non autorisés.</span>
                  }
                </div>
              }
            </div>

            <div class="form-group">
              <label>Adresse Email <span class="text-red">*</span></label>
              <input
                type="email"
                formControlName="email"
                class="input"
                placeholder="yassine.elidrissi@norsys.fr"
              />
              @if (userForm.get('email')?.invalid && userForm.get('email')?.touched) {
                <div style="color: #ef4444; font-size: 0.875rem; margin-top: 4px;">
                  @if (userForm.get('email')?.errors?.['required']) {
                    <span>Ce champ est requis.</span>
                  }
                  @if (userForm.get('email')?.errors?.['email']) {
                    <span>Veuillez entrer une adresse email valide.</span>
                  }
                </div>
              }
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
                <option value="" disabled selected>Sélectionner le rôle</option>
                <option value="VIEWER">Consultation</option>
                <option value="RECRUITER">Recruteur</option>
                <option value="HR_ADMIN">Admin RH</option>
              </select>
              @if (userForm.get('role')?.invalid && userForm.get('role')?.touched) {
                <div style="color: #ef4444; font-size: 0.875rem; margin-top: 4px;">
                  <span>Veuillez sélectionner un rôle.</span>
                </div>
              }
            </div>
          </div>

          <div class="modal-actions" style="margin-top: 32px;">
            <button type="button" class="btn btn-secondary" (click)="onClose()">Annuler</button>
            <button type="submit" class="btn btn-primary">Créer l'utilisateur</button>
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
    username: [
      '',
      [
        Validators.required,
        Validators.minLength(3),
        Validators.pattern(/^[a-zA-Z0-9._@+-]+$/),
      ],
    ],
    email: ['', [Validators.required, Validators.email]],
    firstName: [''],
    lastName: [''],
    role: ['', Validators.required],
  });

  onClose() {
    this.closeModal.emit();
  }

  onSubmit() {
    if (this.userForm.valid) {
      this.save.emit(this.userForm.value as CreateUserRequest);
    } else {
      this.userForm.markAllAsTouched();
    }
  }
}
