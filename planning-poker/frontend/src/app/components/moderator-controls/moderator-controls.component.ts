import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-moderator-controls',
  standalone: true,
  templateUrl: './moderator-controls.component.html',
  styleUrl: './moderator-controls.component.css'
})
export class ModeratorControlsComponent {
  @Input() allEstimated = false;
  @Input() revealed = false;
  @Output() reveal = new EventEmitter<void>();
  @Output() newRound = new EventEmitter<void>();
}
