import {
  Component,
  inject,
  OnInit,
  signal,
  computed,
  HostListener,
  ElementRef,
  DestroyRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FileDropzone } from '../../../shared/components/file-dropzone/file-dropzone';
import { OfferService } from '../../../core/services/offer.service';
import { ApplicationService } from '../../../core/services/application.service';
import { OfferTitleResponse, TaskStatus, UploadTask } from '../../../core/models';
import { environment } from '../../../../environments/environment';

interface StoredImportSession {
  selectedOfferId?: string;
  tasks: Array<Omit<UploadTask, 'file'>>;
}

@Component({
  selector: 'app-candidate-import',
  standalone: true,
  imports: [CommonModule, RouterModule, FileDropzone],
  templateUrl: './candidate-import.html',
})
export class CandidateImport implements OnInit {
  private offerService = inject(OfferService);
  private applicationService = inject(ApplicationService);
  private elementRef = inject(ElementRef);
  private destroyRef = inject(DestroyRef);

  private readonly SESSION_STORAGE_KEY = 'smartrecruit_active_import_tasks';
  private activePollers = new Map<string, ReturnType<typeof setInterval>>();

  readonly offers = signal<OfferTitleResponse[]>([]);
  readonly selectedOfferId = signal<string>('');
  readonly isOfferDropdownOpen = signal(false);

  readonly uploadTasks = signal<UploadTask[]>([]);
  readonly isUploading = signal(false);

  // --- Computed States for the State Machine ---

  // 1. Files ready to be processed (Pending or Failed with a valid File object)
  readonly tasksToProcess = computed(() => {
    return this.uploadTasks().filter(
      (t) => (t.status === 'PENDING' || t.status === 'FAILED') && !!t.file,
    );
  });

  // 2. Lock state: active if currently uploading or parsing
  readonly isProcessing = computed(() => {
    return (
      this.isUploading() ||
      this.uploadTasks().some((t) => t.status === 'UPLOADING' || t.status === 'PARSING')
    );
  });

  // 3. True if entire queue is completed successfully
  readonly hasOnlySuccess = computed(() => {
    const tasks = this.uploadTasks();
    return tasks.length > 0 && tasks.every((t) => t.status === 'SUCCESS');
  });

  // Counts
  readonly pendingCount = computed(
    () => this.uploadTasks().filter((t) => t.status === 'PENDING').length,
  );
  readonly failedCount = computed(
    () => this.uploadTasks().filter((t) => t.status === 'FAILED').length,
  );
  readonly duplicateCount = computed(
    () => this.uploadTasks().filter((t) => t.status === 'DUPLICATE').length,
  );
  readonly successCount = computed(
    () => this.uploadTasks().filter((t) => t.status === 'SUCCESS').length,
  );

  readonly selectedOfferLabel = computed(() => {
    const id = this.selectedOfferId();
    if (!id) return "-- Choisir une offre d'emploi --";
    const found = this.offers().find((o) => o.id === id);
    return found ? found.title : "-- Choisir une offre d'emploi --";
  });

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.stopAllPolling();
    });
  }

  ngOnInit() {
    this.offerService.getOfferTitles().subscribe({
      next: (data) => {
        this.offers.set(data);
        if (data.length > 0 && !this.selectedOfferId()) {
          this.selectedOfferId.set(data[0].id);
        }
      },
      error: (err) => console.error('Failed to load offer titles', err),
    });

    // Restore previous import session from sessionStorage if available
    this.restoreSession();
  }

  // --- Session Storage Management ---
  private saveSession() {
    try {
      const serializableTasks = this.uploadTasks().map((t) => ({
        filename: t.filename,
        isZip: t.isZip,
        size: t.size,
        applicationId: t.applicationId,
        status: t.status,
        progress: t.progress,
        errorCode: t.errorCode,
        errorMessage: t.errorMessage,
        summaryMessage: t.summaryMessage,
        subErrors: t.subErrors,
      }));

      const sessionData: StoredImportSession = {
        selectedOfferId: this.selectedOfferId(),
        tasks: serializableTasks,
      };

      sessionStorage.setItem(this.SESSION_STORAGE_KEY, JSON.stringify(sessionData));
    } catch (e) {
      console.warn('Could not save session to sessionStorage', e);
    }
  }

  private restoreSession() {
    try {
      const saved = sessionStorage.getItem(this.SESSION_STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        let tasks: UploadTask[] = [];

        // Handle both legacy array format and object format
        if (Array.isArray(parsed)) {
          tasks = parsed;
        } else if (parsed && Array.isArray(parsed.tasks)) {
          tasks = parsed.tasks;
          if (parsed.selectedOfferId) {
            this.selectedOfferId.set(parsed.selectedOfferId);
          }
        }

        if (tasks.length > 0) {
          // Normalize any interrupted UPLOADING state from an ungraceful reload
          const sanitizedTasks = tasks.map((t) =>
            t.status === 'UPLOADING'
              ? {
                  ...t,
                  status: 'FAILED' as TaskStatus,
                  progress: 0,
                  errorMessage: 'Téléchargement interrompu.',
                }
              : t,
          );

          this.uploadTasks.set(sanitizedTasks);

          // Resume live polling with instant 0ms check for in-flight parsing tasks
          sanitizedTasks.forEach((t) => {
            if (t.status === 'PARSING' && t.applicationId) {
              this.pollStatus(t.applicationId);
            }
          });
        }
      }
    } catch (e) {
      console.warn('Could not restore session from sessionStorage', e);
    }
  }

  private clearSession() {
    try {
      sessionStorage.removeItem(this.SESSION_STORAGE_KEY);
    } catch (e) {
      // Ignore
    }
  }

  // Close dropdown on outside click
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isOfferDropdownOpen.set(false);
    }
  }

  toggleOfferDropdown(event: Event) {
    if (this.isProcessing()) return;
    event.stopPropagation();
    this.isOfferDropdownOpen.update((v) => !v);
  }

  selectOffer(offerId: string) {
    if (this.selectedOfferId() === offerId) {
      this.isOfferDropdownOpen.set(false);
      return;
    }
    this.selectedOfferId.set(offerId);
    this.isOfferDropdownOpen.set(false);

    // Reset all files in memory to PENDING for the newly selected offer
    this.uploadTasks.update((tasks) =>
      tasks
        .filter((t) => !!t.file)
        .map((t) => ({
          ...t,
          status: 'PENDING' as TaskStatus,
          progress: 0,
          errorCode: undefined,
          errorMessage: undefined,
          summaryMessage: undefined,
          subErrors: undefined,
          applicationId: undefined,
        })),
    );
    this.saveSession();
  }

  // --- Dropzone Handler ---
  onFilesDropped(files: File[]) {
    if (this.isProcessing()) return;

    this.uploadTasks.update((current) => {
      const updated = [...current];

      for (const file of files) {
        const existingIdx = updated.findIndex(
          (t) => t.filename === file.name && t.size === file.size,
        );

        const isZip = file.name.endsWith('.zip');

        if (existingIdx >= 0) {
          const existing = updated[existingIdx];
          // Block re-processing if already completed or active
          if (
            existing.status === 'SUCCESS' ||
            existing.status === 'PARSING' ||
            existing.status === 'UPLOADING' ||
            existing.status === 'DUPLICATE'
          ) {
            continue;
          }

          // Otherwise, if it was FAILED or PENDING, reset to fresh PENDING
          updated[existingIdx] = {
            ...existing,
            file,
            status: 'PENDING',
            progress: 0,
            errorCode: undefined,
            errorMessage: undefined,
            summaryMessage: undefined,
            subErrors: undefined,
          };
        } else {
          // Add as new PENDING task
          updated.push({
            file,
            filename: file.name,
            isZip,
            size: file.size,
            status: 'PENDING',
            progress: 0,
          });
        }
      }

      return updated;
    });

    this.saveSession();
  }

  removeTask(index: number) {
    const task = this.uploadTasks()[index];
    if (task && (task.status === 'UPLOADING' || task.status === 'PARSING')) return;
    if (task?.applicationId) {
      this.stopPolling(task.applicationId);
    }
    this.uploadTasks.update((tasks) => tasks.filter((_, i) => i !== index));
    this.saveSession();
  }

  clearAllTasks() {
    if (this.isProcessing()) return;
    this.stopAllPolling();
    this.uploadTasks.set([]);
    this.clearSession();
  }

  clearCompletedTasks() {
    if (this.isProcessing()) return;
    this.uploadTasks.update((tasks) => tasks.filter((t) => t.status !== 'SUCCESS'));
    this.saveSession();
  }

  // --- Main Unified Bulk Ingestion ---
  startUpload() {
    const offerId = this.selectedOfferId();
    const tasksToRun = this.tasksToProcess();

    if (!offerId || tasksToRun.length === 0 || this.isProcessing()) return;

    this.isUploading.set(true);

    const filesToUpload = tasksToRun.map((t) => t.file).filter((f): f is File => !!f);

    const filesSet = new Set(filesToUpload.map((f) => f.name));

    // 1. Mark target tasks as UPLOADING (50% progress)
    this.uploadTasks.update((current) =>
      current.map((t) =>
        filesSet.has(t.filename)
          ? {
              ...t,
              status: 'UPLOADING',
              progress: 50,
              errorMessage: undefined,
              summaryMessage: undefined,
              subErrors: undefined,
            }
          : t,
      ),
    );
    this.saveSession();

    // 2. Submit multipart request to POST /applications/import
    this.applicationService.importCandidates(offerId, filesToUpload).subscribe({
      next: (response) => {
        const fileStatuses = response.fileStatuses || [];

        this.uploadTasks.update((current) =>
          current.map((t) => {
            if (!filesSet.has(t.filename)) return t;

            const statusMatch = fileStatuses.find((s) => s.filename === t.filename);

            if (!statusMatch) {
              return {
                ...t,
                status: 'FAILED',
                progress: 0,
                errorMessage: 'Aucun statut retourné par le serveur.',
              };
            }

            // Case A: Direct CV Success -> Transition to PARSING & start polling
            if (statusMatch.applicationId) {
              return {
                ...t,
                status: 'PARSING',
                progress: 75,
                applicationId: statusMatch.applicationId,
              };
            }

            // Case B: ZIP Archive (Unpacked and executed) -> Complete with summary message & sub-errors
            if (
              t.isZip &&
              statusMatch.errorCode !== 'CORRUPTED' &&
              statusMatch.errorCode !== 'EMPTY_ARCHIVE'
            ) {
              return {
                ...t,
                status: 'SUCCESS',
                progress: 100,
                summaryMessage:
                  statusMatch.message || `${statusMatch.extractedCount} CV(s) extrait(s).`,
                subErrors: statusMatch.subErrors,
              };
            }

            // Case C: Duplicate Candidate Application
            if (statusMatch.errorCode === 'DUPLICATE') {
              return {
                ...t,
                status: 'DUPLICATE',
                progress: 0,
                errorCode: 'DUPLICATE',
                errorMessage:
                  statusMatch.message ||
                  "Ce candidat / CV a déjà postulé à cette offre d'emploi (Doublon).",
                subErrors: statusMatch.subErrors,
              };
            }

            // Case D: Other Ingestion Errors (Invalid format, Corrupted ZIP, etc.)
            return {
              ...t,
              status: 'FAILED',
              progress: 0,
              errorCode: statusMatch.errorCode || undefined,
              errorMessage: statusMatch.message || 'Échec du traitement du fichier.',
              subErrors: statusMatch.subErrors,
            };
          }),
        );

        this.saveSession();

        // 3. Start live extraction status polling for all valid application IDs
        fileStatuses.forEach((s) => {
          if (s.applicationId) {
            this.pollStatus(s.applicationId);
          }
        });

        this.checkAllDone();
      },
      error: (err) => {
        const errorMessage =
          err?.error?.message || err?.message || "Erreur lors de l'envoi des fichiers.";
        this.uploadTasks.update((current) =>
          current.map((t) =>
            filesSet.has(t.filename) ? { ...t, status: 'FAILED', progress: 0, errorMessage } : t,
          ),
        );
        this.isUploading.set(false);
        this.saveSession();
      },
    });
  }

  // --- Resilient Status Polling for Direct CVs with Instant 0ms Check ---
  private pollStatus(applicationId: string) {
    if (this.activePollers.has(applicationId)) return;

    const intervalMs = environment.pollingIntervalMs;
    const maxPolls = environment.pollingMaxAttempts;
    let pollCount = 0;

    const checkStatus = () => {
      this.applicationService.pollExtractionStatus(applicationId).subscribe({
        next: (statusData) => {
          if (statusData.extractionStatus === 'SUCCESS') {
            this.stopPolling(applicationId);
            this.updateTaskStatus(applicationId, 'SUCCESS', 100);
            this.checkAllDone();
          } else if (statusData.extractionStatus === 'FAILED') {
            this.stopPolling(applicationId);
            this.updateTaskStatus(
              applicationId,
              'FAILED',
              0,
              "Échec de l'analyse IA : document illisible ou non reconnu.",
            );
            this.checkAllDone();
          }
        },
        error: (err) => {
          this.stopPolling(applicationId);
          const errorMessage =
            err?.error?.message || err?.message || "Erreur lors de l'extraction IA.";
          this.updateTaskStatus(applicationId, 'FAILED', 0, errorMessage);
          this.checkAllDone();
        },
      });
    };

    // Immediate 0ms check on invocation (vital when returning to the page)
    checkStatus();

    // Recurring polling
    const intervalId = setInterval(() => {
      pollCount++;
      if (pollCount > maxPolls) {
        this.stopPolling(applicationId);
        this.updateTaskStatus(applicationId, 'FAILED', 0, "Délai d'attente d'extraction dépassé.");
        this.checkAllDone();
        return;
      }
      checkStatus();
    }, intervalMs);

    this.activePollers.set(applicationId, intervalId);
  }

  private stopPolling(applicationId: string) {
    const timer = this.activePollers.get(applicationId);
    if (timer) {
      clearInterval(timer);
      this.activePollers.delete(applicationId);
    }
  }

  private stopAllPolling() {
    this.activePollers.forEach((timer) => clearInterval(timer));
    this.activePollers.clear();
  }

  private updateTaskStatus(
    applicationId: string,
    status: 'SUCCESS' | 'FAILED',
    progress: number,
    errorMessage?: string,
  ) {
    this.uploadTasks.update((current) =>
      current.map((t) =>
        t.applicationId === applicationId ? { ...t, status, progress, errorMessage } : t,
      ),
    );
    this.saveSession();
  }

  private checkAllDone() {
    const tasks = this.uploadTasks();
    const stillActive = tasks.some((t) => t.status === 'UPLOADING' || t.status === 'PARSING');
    if (!stillActive) {
      this.isUploading.set(false);
    }
  }

  formatFileSize(bytes: number): string {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }
}
