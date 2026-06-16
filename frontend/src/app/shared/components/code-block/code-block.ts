import { Component, computed, inject, input } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import hljs from 'highlight.js/lib/common';

@Component({
  selector: 'app-code-block',
  imports: [],
  templateUrl: './code-block.html',
  styleUrl: './code-block.scss',
})
export class CodeBlock {
  private readonly sanitizer = inject(DomSanitizer);

  readonly code = input.required<string>();
  readonly language = input<string | null>();

  protected readonly highlighted = computed<SafeHtml>(() => {
    const language = this.language()?.toLowerCase();
    const result =
      language && hljs.getLanguage(language)
        ? hljs.highlight(this.code(), { language })
        : hljs.highlightAuto(this.code());
    // hljs output is span/class markup generated from escaped code — safe to trust.
    return this.sanitizer.bypassSecurityTrustHtml(result.value);
  });
}
