import { Component, EventEmitter, Input, Output, inject, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LucideX } from '@lucide/angular';
import { UserResponse, UpdateUserRequest } from '../../../../../core/models/user.model';

@Component({
  selector: 'app-edit-user-modal',
  standalone: true,
  imports: [ReactiveFormsModule, LucideX],
  template: `
    <div class="modal-backdrop">
      <div class="modal modal-content" style="padding: 24px;">
        <button class="modal-x" type="button" (click)="onClose()">
          <svg lucideX></svg>
        </button>

        <h2 class="h2" style="margin-bottom: 24px;">Modifier {{ user.username }}</h2>

        <form [formGroup]="userForm" (ngSubmit)="onSubmit()">
          <div style="display: grid; gap: 16px;">
            <div class="form-group">
              <label>Adresse Email <span class="text-red">*</span></label>
              <input type="email" formControlName="email" class="input" />
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
                <input type="text" formControlName="firstName" class="input" />
              </div>
              <div class="form-group">
                <label>Nom</label>
                <input type="text" formControlName="lastName" class="input" />
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
            <button type="submit" class="btn btn-primary">Enregistrer</button>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class EditUserModalComponent implements OnInit {
  @Input({ required: true }) user!: UserResponse;
  @Output() closeModal = new EventEmitter<void>();
  @Output() save = new EventEmitter<{ id: string; data: UpdateUserRequest }>();

  private fb = inject(FormBuilder);

  userForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    firstName: [''],
    lastName: [''],
    role: ['', Validators.required],
  });

  ngOnInit() {
    this.userForm.patchValue({
      email: this.user.email,
      firstName: this.user.firstName,
      lastName: this.user.lastName,
      role: this.user.role,
    });
  }

  onClose() {
    this.closeModal.emit();
  }

  onSubmit() {
    if (this.userForm.valid) {
      this.save.emit({
        id: this.user.id,
        data: this.userForm.value as UpdateUserRequest,
      });
    } else {
      this.userForm.markAllAsTouched();
    }
  }
}
