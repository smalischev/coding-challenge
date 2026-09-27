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
  @Input({ required: true }) canSelectIssue = false;
  @Output() releaseRequested = new EventEmitter<void>();
  @Output() issueSelected = new EventEmitter<number>();

  selectIssue(issueIid: string): void {
    const parsedIssueIid = Number(issueIid);
    if (Number.isInteger(parsedIssueIid) && parsedIssueIid > 0) {
      this.issueSelected.emit(parsedIssueIid);
    }
  }
}
