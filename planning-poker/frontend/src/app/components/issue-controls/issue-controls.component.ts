import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Issue } from '../../models';

@Component({
  selector: 'app-issue-controls',
  standalone: true,
  templateUrl: './issue-controls.component.html',
  styleUrl: './issue-controls.component.css'
})
export class IssueControlsComponent {
  @Input({ required: true }) released = false;
  @Output() issueReleased = new EventEmitter<Issue>();
  readonly issues: Issue[] = [
    { id: 42, title: 'Login überarbeiten', description: 'Die Anmeldung soll verständlicher werden und Fehlermeldungen klar darstellen. Die Umsetzung wird gemeinsam geschätzt.' },
    { id: 57, title: 'Benachrichtigungen bündeln', description: 'Mehrere gleichartige Benachrichtigungen sollen für eine bessere Übersicht zusammengefasst werden.' }
  ];
  selectedIssue = this.issues[0];

  selectIssue(issueId: number): void {
    const issue = this.issues.find((candidate) => candidate.id === issueId);
    if (issue) {
      this.selectedIssue = issue;
    }
  }
}
