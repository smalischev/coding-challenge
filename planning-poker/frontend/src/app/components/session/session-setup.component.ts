import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserRole } from '../../models';

export interface CreateSessionInput { gitlabProjectId: number; gitlabIssueIid: number; }

@Component({ selector: 'app-session-setup', standalone: true, imports: [ReactiveFormsModule], templateUrl: './session-setup.component.html', styleUrl: './session-setup.component.css' })
export class SessionSetupComponent {
  @Input({ required: true }) role: UserRole = 'Entwickler';
  @Input() error = '';
  @Output() createRequested = new EventEmitter<CreateSessionInput>();
  @Output() joinRequested = new EventEmitter<string>();
  readonly createForm = new FormGroup({ gitlabProjectId: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]), gitlabIssueIid: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]) });
  readonly joinForm = new FormGroup({ sessionId: new FormControl('', [Validators.required]) });

  create(): void { if (this.createForm.invalid) { this.createForm.markAllAsTouched(); return; } const { gitlabProjectId, gitlabIssueIid } = this.createForm.getRawValue(); this.createRequested.emit({ gitlabProjectId: gitlabProjectId!, gitlabIssueIid: gitlabIssueIid! }); }
  join(): void { if (this.joinForm.invalid) { this.joinForm.markAllAsTouched(); return; } this.joinRequested.emit(this.joinForm.controls.sessionId.value!.trim()); }
}
