import { Component, OnInit, inject, signal, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/auth/auth.service';
import { UserRole } from '../../core/models/user.model';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-profile.html',
})
export class EditProfile implements OnInit {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private authService = inject(AuthService);

  @Output() closeModal = new EventEmitter<boolean>();

  readonly profileForm: FormGroup;
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');

  readonly username = signal('');
  readonly role = signal('');

  constructor() {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
    });
  }

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.isLoading.set(true);
    this.errorMessage.set('');
    this.userService
      .getMyProfile()
      .pipe(
        catchError((err) => {
          console.error('Failed to load user profile from /api/v1/users/me:', err);
          this.errorMessage.set(err.error?.message || 'Erreur lors du chargement du profil.');
          return EMPTY;
        }),
        finalize(() => {
          this.isLoading.set(false);
        }),
      )
      .subscribe((data) => {
        this.username.set(data.username);

        switch (data.role) {
          case UserRole.HR_ADMIN:
            this.role.set('Admin RH');
            break;
          case UserRole.RECRUITER:
            this.role.set('Recruteur');
            break;
          default:
            this.role.set('Consultation');
            break;
        }

        this.profileForm.patchValue({
          firstName: data.firstName,
          lastName: data.lastName,
          email: data.email,
        });
      });
  }

  onSubmit() {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.profileForm.invalid) {
      this.errorMessage.set('Veuillez remplir tous les champs correctement.');
      return;
    }

    this.isSaving.set(true);

    this.userService
      .updateMyProfile(this.profileForm.value)
      .pipe(
        catchError((err) => {
          this.errorMessage.set(
            err.status === 409
              ? 'Cet email est déjà utilisé par un autre compte.'
              : 'Une erreur est survenue lors de la mise à jour.',
          );
          return EMPTY;
        }),
        finalize(() => {
          this.isSaving.set(false);
        }),
      )
      .subscribe(() => {
        // Refresh token then close modal
        this.authService.forceTokenRefresh().subscribe(() => {
          this.authService.syncAuthState();
          this.closeModal.emit(true);
        });
      });
  }

  onCancel() {
    this.closeModal.emit(false);
  }
}
