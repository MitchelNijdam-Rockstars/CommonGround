import { Component, inject, output, signal } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { RankingsApi } from '../../../../core/services/rankings-api';

interface AgentOption {
  format: string;
  label: string;
  icon: string;
  filePath: string;
  tip?: string;
}

const AGENTS: AgentOption[] = [
  {
    format: 'CLAUDE',
    label: 'Claude Code',
    icon: 'bot',
    filePath: 'CLAUDE.md',
    tip: "Place in your project root. Claude Code reads this file automatically on every session.",
  },
  {
    format: 'COPILOT',
    label: 'GitHub Copilot',
    icon: 'github',
    filePath: '.github/copilot-instructions.md',
    tip: "Create the .github directory if it doesn't exist. Copilot picks this up automatically in VS Code, JetBrains, and other supported editors.",
  },
  {
    format: 'CURSOR',
    label: 'Cursor',
    icon: 'file-code',
    filePath: '.cursor/rules/coding-standards.mdc',
    tip: "Create the .cursor/rules directory if needed. The file includes frontmatter that applies these rules to all files in your project automatically.",
  },
  {
    format: 'WINDSURF',
    label: 'Windsurf',
    icon: 'wind',
    filePath: '.windsurfrules',
    tip: "Place in your project root. Windsurf merges these with your global rules automatically.",
  },
  {
    format: 'GENERIC',
    label: 'Generic',
    icon: 'file-text',
    filePath: 'common-ground.md',
    tip: "Works with any AI tool that reads Markdown instruction files. Rename it to whatever your tool expects.",
  },
];

@Component({
  selector: 'app-export-dialog',
  imports: [LucideAngularModule],
  templateUrl: './export-dialog.html',
  styleUrl: './export-dialog.scss',
})
export class ExportDialog {
  private readonly api = inject(RankingsApi);

  readonly closed = output<void>();

  protected readonly agents = AGENTS;
  protected readonly selected = signal<AgentOption>(AGENTS[0]);
  protected readonly exporting = signal(false);

  protected selectAgent(agent: AgentOption): void {
    this.selected.set(agent);
  }

  dismiss(): void {
    this.closed.emit();
  }

  protected download(): void {
    if (this.exporting()) return;
    this.exporting.set(true);
    this.api.exportMarkdown(this.selected().format).subscribe({
      next: (response) => {
        this.api.saveExport(response);
        this.exporting.set(false);
        this.dismiss();
      },
      error: () => this.exporting.set(false),
    });
  }
}
