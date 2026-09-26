import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-header',
  standalone: true,
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  @Input({ required: true }) sessionId = '';
  @Input({ required: true }) currentUser = '';
  @Input({ required: true }) currentRole = '';
  @Output() logout = new EventEmitter<void>();
}
