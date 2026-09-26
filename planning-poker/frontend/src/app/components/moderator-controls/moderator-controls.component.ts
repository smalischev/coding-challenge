import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CardValue } from '../../models';

@Component({
  selector: 'app-moderator-controls',
  standalone: true,
  templateUrl: './moderator-controls.component.html',
  styleUrl: './moderator-controls.component.css'
})
export class ModeratorControlsComponent {
  @Input() allEstimated = false;
  @Input() revealed = false;
  @Input() finalizing = false;
  @Output() reveal = new EventEmitter<void>();
  @Output() newRound = new EventEmitter<void>();
  @Output() finalize = new EventEmitter<CardValue>();
  readonly cards: CardValue[] = ['0', '1', '2', '3', '5', '8', '13', '21', '34', '?', '☕'];
  selectedCard: CardValue = '5';
}
