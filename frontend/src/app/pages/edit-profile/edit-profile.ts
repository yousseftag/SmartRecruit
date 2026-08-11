import { Component, OnInit, inject, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/auth/auth.service';
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
  private cdr = inject(ChangeDetectorRef);

  @Output() closeModal = new EventEmitter<boolean>();

  profileForm: FormGroup;
  isLoading = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';

  username = '';
  role = '';

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
    this.isLoading = true;
    this.userService
      .getMyProfile()
      .pipe(
        catchError((err) => {
          this.errorMessage = 'Erreur lors du chargement du profil.';
          return EMPTY;
        }),
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe((data) => {
        this.username = data.username;

        switch (data.role) {
          case 'HR_ADMIN':
            this.role = 'Admin RH';
            break;
          case 'RECRUITER':
            this.role = 'Recruteur';
            break;
          default:
            this.role = 'Consultation';
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
    this.errorMessage = '';
    this.successMessage = '';

    if (this.profileForm.invalid) {
      this.errorMessage = 'Veuillez remplir tous les champs correctement.';
      return;
    }

    this.isSaving = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.userService
      .updateMyProfile(this.profileForm.value)
      .pipe(
        catchError((err) => {
          this.errorMessage =
            err.status === 409
              ? 'Cet email est déjà utilisé par un autre compte.'
              : 'Une erreur est survenue lors de la mise à jour.';

          return EMPTY;
        }),
        finalize(() => {
          this.isSaving = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe(() => {
        // Refresh token then close modal
        this.authService.forceTokenRefresh().subscribe(() => {
          this.closeModal.emit(true);
        });
      });
  }

  onCancel() {
    this.closeModal.emit(false);
  }
}
