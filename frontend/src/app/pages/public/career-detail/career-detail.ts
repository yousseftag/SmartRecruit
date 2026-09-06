import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FileDropzone } from '../../../shared/components/file-dropzone/file-dropzone';
import { ApplicationService } from '../../../core/services/application.service';
import { OfferService } from '../../../core/services/offer.service';
import { OfferPublicResponse } from '../../../core/models/offer.model';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ExperienceFormatPipe } from '../../../shared/pipes/experience-format.pipe';
import { MarkdownPipe } from '../../../shared/pipes/markdown.pipe';

@Component({
  selector: 'app-career-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FileDropzone,
    ReactiveFormsModule,
    ExperienceFormatPipe,
    MarkdownPipe,
  ],
  templateUrl: './career-detail.html',
})
export class CareerDetail implements OnInit {
  private applicationService = inject(ApplicationService);
  private offerService = inject(OfferService);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);

  readonly isSubmitting = signal(false);
  readonly isSuccess = signal(false);
  readonly isLoading = signal(true);
  readonly error = signal(false);
  readonly errorMessage = signal('');
  readonly submitError = signal<string | null>(null);
  readonly selectedFile = signal<File | null>(null);
  readonly fileError = signal(false);
  readonly offer = signal<OfferPublicResponse | null>(null);

  applyForm = this.fb.group({
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.offerService.getPublicOfferById(id).subscribe({
        next: (data) => {
          this.offer.set(data);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.error.set(true);
          this.errorMessage.set(
            err?.error?.message || "L'offre demandée est introuvable ou n'est plus disponible.",
          );
          this.isLoading.set(false);
        },
      });
    } else {
      this.error.set(true);
      this.errorMessage.set('Identifiant de poste manquant.');
      this.isLoading.set(false);
    }
  }

  onFileDropped(files: File[]) {
    if (files.length > 0) {
      this.selectedFile.set(files[0]);
      this.fileError.set(false);
      this.submitError.set(null);
    }
  }

  removeSelectedFile() {
    this.selectedFile.set(null);
  }

  submitApplication() {
    this.submitError.set(null);
    const file = this.selectedFile();

    if (!file) {
      this.fileError.set(true);
    }

    if (this.applyForm.invalid || !file) {
      this.applyForm.markAllAsTouched();
      return;
    }

    const currentOffer = this.offer();
    if (!currentOffer) return;

    this.isSubmitting.set(true);

    const formData = new FormData();
    formData.append('offerId', currentOffer.id);
    formData.append('firstName', this.applyForm.value.firstName!.trim());
    formData.append('lastName', this.applyForm.value.lastName!.trim());
    formData.append('email', this.applyForm.value.email!.trim());
    if (this.applyForm.value.phone?.trim()) {
      formData.append('phone', this.applyForm.value.phone.trim());
    }
    formData.append('file', file);

    this.applicationService.applyToOffer(formData).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.isSuccess.set(true);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const msg =
          err?.error?.message ||
          "Une erreur s'est produite lors de l'envoi de votre candidature. Veuillez réessayer.";
        this.submitError.set(msg);
      },
    });
  }
}
