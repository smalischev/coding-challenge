import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-issue',
  standalone: true,
  templateUrl: './issue.component.html',
  styleUrl: './issue.component.css'
})
export class IssueComponent {
  @Input({ required: true }) issueId = 0;
  @Input({ required: true }) title = '';
  @Input({ required: true }) description = '';
}
