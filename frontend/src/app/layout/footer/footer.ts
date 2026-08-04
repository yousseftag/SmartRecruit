import { Component, signal } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './footer.html',
})
export class Footer {
  /**
   * Dynamically computes the current year for the copyright notice
   * to ensure the footer always stays up to date.
   */
  currentYear = signal<number>(new Date().getFullYear());
}
