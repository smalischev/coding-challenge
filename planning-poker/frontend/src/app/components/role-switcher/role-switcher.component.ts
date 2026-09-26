import { Component, EventEmitter, Input, Output } from '@angular/core';
import { UserRole } from '../../models';

@Component({
  selector: 'app-role-switcher',
  standalone: true,
  templateUrl: './role-switcher.component.html',
  styleUrl: './role-switcher.component.css'
})
export class RoleSwitcherComponent {
  @Input({ required: true }) role: UserRole = 'Scrum Master';
  @Output() roleChanged = new EventEmitter<UserRole>();
}
