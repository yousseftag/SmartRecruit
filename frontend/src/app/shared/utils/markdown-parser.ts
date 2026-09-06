/**
 * Safe, robust lightweight Markdown parser for job descriptions and recruiter notes.
 * Converts standard Markdown into semantic, beautifully styled Tailwind HTML.
 * Escapes any raw HTML before processing to prevent XSS vulnerabilities.
 */

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function parseInline(text: string): string {
  return (
    text
      // Bold + Italic (***text*** or ___text___)
      .replace(
        /\*\*\*(.*?)\*\*\*/g,
        '<strong class="font-bold text-ink"><em class="italic">$1</em></strong>',
      )
      .replace(
        /___(.*?)___/g,
        '<strong class="font-bold text-ink"><em class="italic">$1</em></strong>',
      )
      // Bold (**text** or __text__)
      .replace(/\*\*(.*?)\*\*/g, '<strong class="font-bold text-ink">$1</strong>')
      .replace(/__(.*?)__/g, '<strong class="font-bold text-ink">$1</strong>')
      // Italic (*text* or _text_)
      .replace(/\*(.*?)\*/g, '<em class="italic text-slate-800">$1</em>')
      .replace(/_(.*?)_/g, '<em class="italic text-slate-800">$1</em>')
      // Strikethrough (~~text~~)
      .replace(/~~(.*?)~~/g, '<del class="line-through text-slate-400">$1</del>')
      // Links [text](url) - restricted to safe web schemes
      .replace(
        /\[(.*?)\]\(((?:https?:\/\/|mailto:|\/)[^\s\)]+)\)/g,
        '<a href="$2" target="_blank" rel="noopener noreferrer" class="text-blue hover:text-blue-700 underline font-medium inline-flex items-center gap-0.5">$1</a>',
      )
  );
}

/**
 * Parses a markdown string into styled HTML markup.
 *
 * @param markdown The raw markdown content
 * @returns Clean, styled, sanitized HTML string
 */
export function parseMarkdown(markdown: string | null | undefined): string {
  if (!markdown || !markdown.trim()) {
    return '';
  }

  const codeBlocks: string[] = [];
  const inlineCodes: string[] = [];

  // 1. Extract fenced code blocks first
  let processed = markdown.replace(
    /(?:^|\n)```([a-zA-Z0-9_-]*)\r?\n([\s\S]*?)```(?:\r?\n|$)/g,
    (_, lang, code) => {
      const idx = codeBlocks.length;
      const cleanCode = escapeHtml(code.replace(/\r?\n$/, ''));
      const langBadge = lang
        ? `<span class="text-[10px] uppercase font-mono tracking-wider text-slate-400 select-none">${escapeHtml(lang)}</span>`
        : '';
      codeBlocks.push(
        `<div class="my-3 rounded-lg overflow-hidden border border-slate-800 bg-slate-950 shadow-xs">` +
          (langBadge
            ? `<div class="px-3 py-1 bg-slate-900 border-b border-slate-800 flex justify-end">${langBadge}</div>`
            : '') +
          `<pre class="p-3 text-slate-100 text-xs font-mono overflow-x-auto leading-relaxed"><code>${cleanCode}</code></pre></div>`,
      );
      return `\n\n@@CODEBLOCK_${idx}@@\n\n`;
    },
  );

  // 2. Extract inline code
  processed = processed.replace(/`([^`\r\n]+)`/g, (_, code) => {
    const idx = inlineCodes.length;
    inlineCodes.push(
      `<code class="px-1.5 py-0.5 bg-slate-100 text-slate-800 rounded font-mono text-[11px] font-semibold border border-slate-200">${escapeHtml(code)}</code>`,
    );
    return `@@INLINECODE_${idx}@@`;
  });

  // 3. Escape all remaining raw HTML to prevent injection
  processed = escapeHtml(processed);

  // 4. Parse block structures line by line
  const lines = processed.split(/\r?\n/);
  const output: string[] = [];
  let i = 0;

  while (i < lines.length) {
    const rawLine = lines[i];
    const trimmed = rawLine.trim();

    // Blank line
    if (!trimmed) {
      i++;
      continue;
    }

    // Code block placeholder
    if (trimmed.startsWith('@@CODEBLOCK_')) {
      output.push(trimmed);
      i++;
      continue;
    }

    // Horizontal Rule (---, ***, ___)
    if (/^(?:-{3,}|\*{3,}|_{3,})$/.test(trimmed)) {
      output.push('<hr class="my-4 border-t border-slate-200" />');
      i++;
      continue;
    }

    // Headings (#, ##, ###, ####, #####, ######)
    const h1Match = trimmed.match(/^#\s+(.*)$/);
    if (h1Match) {
      output.push(
        `<h1 class="text-xl font-bold text-ink mt-5 mb-2 pb-1.5 border-b border-line">${parseInline(h1Match[1])}</h1>`,
      );
      i++;
      continue;
    }

    const h2Match = trimmed.match(/^##\s+(.*)$/);
    if (h2Match) {
      output.push(
        `<h2 class="text-base font-bold text-ink mt-4 mb-2 pb-1 border-b border-line">${parseInline(h2Match[1])}</h2>`,
      );
      i++;
      continue;
    }

    const h3Match = trimmed.match(/^###\s+(.*)$/);
    if (h3Match) {
      output.push(
        `<h3 class="text-sm font-bold text-ink mt-3 mb-1.5">${parseInline(h3Match[1])}</h3>`,
      );
      i++;
      continue;
    }

    const h4Match = trimmed.match(/^####\s+(.*)$/);
    if (h4Match) {
      output.push(
        `<h4 class="text-xs font-bold text-ink mt-2.5 mb-1">${parseInline(h4Match[1])}</h4>`,
      );
      i++;
      continue;
    }

    const h5Match = trimmed.match(/^#####\s+(.*)$/);
    if (h5Match) {
      output.push(
        `<h5 class="text-xs font-bold text-slate-700 uppercase tracking-wider mt-2 mb-1">${parseInline(h5Match[1])}</h5>`,
      );
      i++;
      continue;
    }

    const h6Match = trimmed.match(/^######\s+(.*)$/);
    if (h6Match) {
      output.push(
        `<h6 class="text-xs font-semibold text-slate-600 mt-2 mb-0.5">${parseInline(h6Match[1])}</h6>`,
      );
      i++;
      continue;
    }

    // Blockquotes (> quote or &gt; quote)
    if (/^(?:>|&gt;)\s?(.*)$/.test(trimmed)) {
      const quoteLines: string[] = [];
      while (i < lines.length && /^(?:>|&gt;)\s?(.*)$/.test(lines[i].trim())) {
        const m = lines[i].trim().match(/^(?:>|&gt;)\s?(.*)$/);
        if (m) quoteLines.push(parseInline(m[1]));
        i++;
      }
      output.push(
        `<blockquote class="border-l-3 border-blue pl-3.5 py-1.5 my-2.5 bg-blue-50/40 rounded-r text-slate-700 italic text-xs leading-relaxed">${quoteLines.join('<br />')}</blockquote>`,
      );
      continue;
    }

    // Unordered List (- item or * item)
    if (/^[-*+]\s+(.*)$/.test(trimmed)) {
      const items: string[] = [];
      while (i < lines.length && /^[-*+]\s+(.*)$/.test(lines[i].trim())) {
        const m = lines[i].trim().match(/^[-*+]\s+(.*)$/);
        if (m) items.push(`<li class="leading-relaxed pl-1">${parseInline(m[1])}</li>`);
        i++;
      }
      output.push(
        `<ul class="list-disc list-outside ml-4 my-2 space-y-1 text-slate-700 text-xs">${items.join('')}</ul>`,
      );
      continue;
    }

    // Ordered List (1. item)
    if (/^\d+\.\s+(.*)$/.test(trimmed)) {
      const items: string[] = [];
      while (i < lines.length && /^\d+\.\s+(.*)$/.test(lines[i].trim())) {
        const m = lines[i].trim().match(/^\d+\.\s+(.*)$/);
        if (m) items.push(`<li class="leading-relaxed pl-1">${parseInline(m[1])}</li>`);
        i++;
      }
      output.push(
        `<ol class="list-decimal list-outside ml-4 my-2 space-y-1 text-slate-700 text-xs">${items.join('')}</ol>`,
      );
      continue;
    }

    // Markdown Table
    if (
      trimmed.includes('|') &&
      i + 1 < lines.length &&
      /^\s*\|?\s*[-:]+[-| :]*\|?\s*$/.test(lines[i + 1].trim())
    ) {
      const headerCells = trimmed
        .split('|')
        .map((c) => c.trim())
        .filter(
          (c, idx, arr) => (idx > 0 && idx < arr.length - 1) || (arr.length === 1 && c.length > 0),
        );

      i += 2; // Skip header row and separator row
      const rows: string[][] = [];

      while (i < lines.length && lines[i].trim().includes('|') && lines[i].trim() !== '') {
        const cells = lines[i]
          .trim()
          .split('|')
          .map((c) => c.trim())
          .filter(
            (c, idx, arr) =>
              (idx > 0 && idx < arr.length - 1) || (arr.length === 1 && c.length > 0),
          );
        rows.push(cells);
        i++;
      }

      let tableHtml =
        '<div class="my-3 overflow-x-auto border border-line rounded-lg"><table class="w-full text-left text-xs border-collapse">';
      tableHtml += '<thead class="bg-slate-50 border-b border-line text-ink font-bold"><tr>';
      headerCells.forEach((h) => {
        tableHtml += `<th class="px-3 py-2 border-r border-line last:border-r-0">${parseInline(h)}</th>`;
      });
      tableHtml += '</tr></thead><tbody>';
      rows.forEach((r, rowIdx) => {
        const bg = rowIdx % 2 === 0 ? 'bg-white' : 'bg-slate-50/50';
        tableHtml += `<tr class="${bg} border-b border-line last:border-b-0 hover:bg-slate-50">`;
        r.forEach((c) => {
          tableHtml += `<td class="px-3 py-2 border-r border-line last:border-r-0 text-slate-700">${parseInline(c)}</td>`;
        });
        tableHtml += '</tr>';
      });
      tableHtml += '</tbody></table></div>';
      output.push(tableHtml);
      continue;
    }

    // Regular paragraph lines
    const pLines: string[] = [];
    while (
      i < lines.length &&
      lines[i].trim() !== '' &&
      !lines[i].trim().startsWith('@@CODEBLOCK_') &&
      !/^(?:-{3,}|\*{3,}|_{3,})$/.test(lines[i].trim()) &&
      !/^#{1,6}\s+/.test(lines[i].trim()) &&
      !/^[-*+]\s+/.test(lines[i].trim()) &&
      !/^\d+\.\s+/.test(lines[i].trim()) &&
      !/^(?:>|&gt;)\s?/.test(lines[i].trim()) &&
      !(
        lines[i].trim().includes('|') &&
        i + 1 < lines.length &&
        /^\s*\|?\s*[-:]+[-| :]*\|?\s*$/.test(lines[i + 1].trim())
      )
    ) {
      pLines.push(parseInline(lines[i].trim()));
      i++;
    }

    if (pLines.length > 0) {
      output.push(
        `<p class="text-xs text-slate-700 leading-relaxed my-1.5">${pLines.join('<br />')}</p>`,
      );
    }
  }

  let result = output.join('\n');

  // Re-inject code blocks
  codeBlocks.forEach((codeBlock, idx) => {
    result = result.replace(new RegExp(`@@CODEBLOCK_${idx}@@`, 'g'), codeBlock);
  });

  // Re-inject inline codes
  inlineCodes.forEach((inlineCode, idx) => {
    result = result.replace(new RegExp(`@@INLINECODE_${idx}@@`, 'g'), inlineCode);
  });

  return `<div class="markdown-body space-y-2 text-xs text-slate-800 leading-relaxed">${result}</div>`;
}
