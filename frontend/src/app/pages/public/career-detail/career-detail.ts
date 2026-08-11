import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FileDropzone } from '../../../shared/components/file-dropzone/file-dropzone';
import { ApplicationService } from '../../../core/services/application.service';
import { OfferService } from '../../../core/services/offer.service';
import { OfferPublicResponse } from '../../../core/models/offer.model';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-career-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FileDropzone, ReactiveFormsModule],
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
  readonly selectedFile = signal<File | null>(null);
  readonly offer = signal<OfferPublicResponse | null>(null);

  applyForm = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: ['']
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.offerService.getPublicOfferById(id).subscribe({
        next: (data) => {
          this.offer.set(data);
          this.isLoading.set(false);
        },
        error: () => {
          this.error.set(true);
          this.isLoading.set(false);
        }
      });
    } else {
      this.error.set(true);
      this.isLoading.set(false);
    }
  }

  onFileDropped(files: File[]) {
    if (files.length > 0) {
      this.selectedFile.set(files[0]);
    }
  }

  submitApplication() {
    const file = this.selectedFile();
    if (this.applyForm.invalid || !file) {
      this.applyForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    
    const formData = new FormData();
    formData.append('offerId', this.offer()!.id); 
    formData.append('firstName', this.applyForm.value.firstName!);
    formData.append('lastName', this.applyForm.value.lastName!);
    formData.append('email', this.applyForm.value.email!);
    if (this.applyForm.value.phone) formData.append('phone', this.applyForm.value.phone);
    formData.append('file', file);

    this.applicationService.applyToOffer(formData).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.isSuccess.set(true);
      },
      error: () => {
        this.isSubmitting.set(false);
        alert("Une erreur s'est produite lors de l'envoi.");
      }
    });
  }
}
