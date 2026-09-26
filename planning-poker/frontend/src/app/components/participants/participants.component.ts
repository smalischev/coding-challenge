import { Component, Input } from '@angular/core';
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
  get estimatedCount(): number { return this.participants.filter((person) => person.estimated).length; }
}
