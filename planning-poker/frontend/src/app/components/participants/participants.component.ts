import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Participant } from '../../models';

@Component({
  selector: 'app-participants',
  standalone: true,
  templateUrl: './participants.component.html',
  styleUrl: './participants.component.css'
})
export class ParticipantsComponent {
  @Input({ required: true }) participants: Participant[] = [];
  @Input({ required: true }) currentUserName = '';
  @Input() testUserSelectionEnabled = false;
  @Output() userSelected = new EventEmitter<string>();
  get developers(): Participant[] {
    return this.participants.filter((person) => person.role === 'Entwickler');
  }

  get estimatedCount(): number {
    return this.developers.filter((person) => person.estimated).length;
  }

  get allEstimated(): boolean {
    return this.developers.every((person) => person.estimated);
  }
}
