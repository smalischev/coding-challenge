import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Issue } from '../../models';

@Component({
  selector: 'app-issue-controls',
  standalone: true,
  templateUrl: './issue-controls.component.html',
  styleUrl: './issue-controls.component.css'
})
export class IssueControlsComponent {
  @Input({ required: true }) issue: Issue | null = null;
  @Input({ required: true }) released = false;
  @Output() releaseRequested = new EventEmitter<void>();
}
