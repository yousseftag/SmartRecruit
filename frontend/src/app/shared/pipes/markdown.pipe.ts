import { Pipe, PipeTransform, inject } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { parseMarkdown } from '../utils/markdown-parser';

@Pipe({
  name: 'markdown',
  standalone: true,
})
export class MarkdownPipe implements PipeTransform {
  private sanitizer = inject(DomSanitizer);

  transform(value: string | null | undefined): SafeHtml {
    if (!value || !value.trim()) {
      return '';
    }
    const html = parseMarkdown(value);
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }
}
