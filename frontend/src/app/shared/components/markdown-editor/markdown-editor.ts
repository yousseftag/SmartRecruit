import { Component, model, signal, computed, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideBold,
  LucideItalic,
  LucideList,
  LucideListOrdered,
  LucideQuote,
  LucideCode,
  LucideEye,
  LucidePencil,
  LucideColumns2,
} from '@lucide/angular';
import { parseMarkdown } from '../../utils/markdown-parser';

@Component({
  selector: 'app-markdown-editor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    LucideBold,
    LucideItalic,
    LucideList,
    LucideListOrdered,
    LucideQuote,
    LucideCode,
    LucideEye,
    LucidePencil,
    LucideColumns2,
  ],
  templateUrl: './markdown-editor.html',
})
export class MarkdownEditor {
  /** Two-way bound Markdown text */
  readonly value = model<string>('');

  /** Active view mode: Edit mode, Live Preview, or Side-by-side Split */
  readonly viewMode = signal<'edit' | 'preview' | 'split'>('edit');

  @ViewChild('textareaRef') textareaRef?: ElementRef<HTMLTextAreaElement>;

  readonly renderedHtml = computed(() => {
    const content = this.value() || '';
    if (!content.trim()) {
      return '<p class="text-slate-400 italic">Aucun contenu à prévisualiser.</p>';
    }
    return parseMarkdown(content);
  });

  setViewMode(mode: 'edit' | 'preview' | 'split') {
    this.viewMode.set(mode);
  }

  // --- Formatting Helpers ---

  applyFormat(type: 'bold' | 'italic' | 'h2' | 'h3' | 'ul' | 'ol' | 'quote' | 'code') {
    switch (type) {
      case 'bold':
        this.wrapSelection('**', '**', 'texte en gras');
        break;
      case 'italic':
        this.wrapSelection('*', '*', 'texte en italique');
        break;
      case 'h2':
        this.insertPrefix('## ', 'Titre de section');
        break;
      case 'h3':
        this.insertPrefix('### ', 'Sous-titre');
        break;
      case 'ul':
        this.insertPrefix('- ', 'Élément de liste');
        break;
      case 'ol':
        this.insertPrefix('1. ', 'Premier élément');
        break;
      case 'quote':
        this.insertPrefix('> ', 'Citation ou note importante');
        break;
      case 'code':
        this.wrapSelection('`', '`', 'code');
        break;
    }
  }

  private wrapSelection(prefix: string, suffix: string, defaultText: string) {
    const textarea = this.textareaRef?.nativeElement;
    if (!textarea) return;

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const current = this.value() || '';
    const selectedText = current.substring(start, end) || defaultText;

    const replacement = `${prefix}${selectedText}${suffix}`;
    const updated = current.substring(0, start) + replacement + current.substring(end);

    this.value.set(updated);

    setTimeout(() => {
      textarea.focus();
      textarea.setSelectionRange(
        start + prefix.length,
        start + prefix.length + selectedText.length,
      );
    }, 0);
  }

  private insertPrefix(prefix: string, defaultText: string) {
    const textarea = this.textareaRef?.nativeElement;
    if (!textarea) return;

    const start = textarea.selectionStart;
    const current = this.value() || '';

    // If starting on a fresh line
    const beforeCursor = current.substring(0, start);
    const afterCursor = current.substring(start);
    const needsNewline = beforeCursor.length > 0 && !beforeCursor.endsWith('\n');
    const actualPrefix = needsNewline ? `\n${prefix}` : prefix;

    const updated = beforeCursor + actualPrefix + defaultText + afterCursor;
    this.value.set(updated);

    setTimeout(() => {
      textarea.focus();
      const newPos = start + actualPrefix.length + defaultText.length;
      textarea.setSelectionRange(newPos, newPos);
    }, 0);
  }
}
