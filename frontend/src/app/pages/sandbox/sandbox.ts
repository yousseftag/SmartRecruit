import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScoreGauge } from '../../shared/components/score-gauge/score-gauge';
import { FileDropzone } from '../../shared/components/file-dropzone/file-dropzone';

@Component({
  selector: 'app-sandbox',
  standalone: true,
  imports: [CommonModule, ScoreGauge, FileDropzone],
  templateUrl: './sandbox.html',
})
export class Sandbox {
  onFilesDropped(files: File[], source: string) {
    console.log(`[${source}] Files dropped:`, files);
    alert(`Succès! Vous avez déposé ${files.length} fichier(s) depuis la dropzone: ${source}.\nRegardez la console pour les détails.`);
  }
}
