import { Component, output } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-about-dialog',
  imports: [LucideAngularModule],
  templateUrl: './about-dialog.html',
  styleUrl: './about-dialog.scss',
})
export class AboutDialog {
  readonly closed = output<void>();

  dismiss(): void {
    this.closed.emit();
  }
}
