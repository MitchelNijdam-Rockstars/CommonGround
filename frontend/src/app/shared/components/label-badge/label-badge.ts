import { Component, computed, input } from '@angular/core';
import { LABEL_TYPE_CLASSES, Label } from '../../../core/models/label.model';

@Component({
  selector: 'app-label-badge',
  imports: [],
  templateUrl: './label-badge.html',
  styleUrl: './label-badge.scss',
})
export class LabelBadge {
  readonly label = input.required<Label>();
  protected readonly typeClasses = computed(() => LABEL_TYPE_CLASSES[this.label().labelType]);
}
