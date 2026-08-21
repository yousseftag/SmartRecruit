import { Component, EventEmitter, Input, Output, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-file-dropzone',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './file-dropzone.html',
})
export class FileDropzone {
  @Input() allowMultiple: boolean = true;
  @Input() maxFiles: number = 50;

  // By default, we accept zip only if multiple is allowed.
  private _acceptedFormats?: string;
  @Input() set acceptedFormats(value: string) {
    this._acceptedFormats = value;
  }
  get acceptedFormats(): string {
    if (this._acceptedFormats) return this._acceptedFormats;
    return this.allowMultiple ? '.pdf,.docx,.zip' : '.pdf,.docx';
  }

  @Output() filesDropped = new EventEmitter<File[]>();

  isDragging = false;
  errorMsg = '';

  @HostListener('dragover', ['$event'])
  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragging = true;
  }

  @HostListener('dragleave', ['$event'])
  onDragLeave(event: DragEvent) {
    event.preventDefault();
    this.isDragging = false;
  }

  @HostListener('drop', ['$event'])
  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragging = false;
    this.errorMsg = '';

    const files = event.dataTransfer?.files;
    if (files) {
      this.handleFiles(Array.from(files));
    }
  }

  onFileSelected(event: any) {
    this.errorMsg = '';
    const files = event.target.files;
    if (files) {
      this.handleFiles(Array.from(files));
    }
    // reset input so same file can be selected again if needed
    event.target.value = null;
  }

  private handleFiles(files: File[]) {
    if (!this.allowMultiple && files.length > 1) {
      this.errorMsg = "Vous ne pouvez sélectionner qu'un seul fichier.";
      return;
    }
    if (files.length > this.maxFiles) {
      this.errorMsg = `Vous ne pouvez pas sélectionner plus de ${this.maxFiles} fichiers.`;
      return;
    }
    this.filesDropped.emit(files);
  }
}
