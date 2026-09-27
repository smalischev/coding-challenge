import { Component, EventEmitter, Input, Output, signal } from '@angular/core';

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
  readonly sessionIdCopied = signal(false);

  async copySessionId(): Promise<void> {
    if (!this.sessionId) {
      return;
    }

    try {
      if (navigator.clipboard) {
        await navigator.clipboard.writeText(this.sessionId);
      } else if (!this.copyWithDocumentCommand()) {
        return;
      }
      this.sessionIdCopied.set(true);
      window.setTimeout(() => this.sessionIdCopied.set(false), 2_000);
    } catch {
      if (!this.copyWithDocumentCommand()) {
        return;
      }
      this.sessionIdCopied.set(true);
      window.setTimeout(() => this.sessionIdCopied.set(false), 2_000);
    }
  }

  private copyWithDocumentCommand(): boolean {
    const temporaryInput = document.createElement('textarea');
    temporaryInput.value = this.sessionId;
    temporaryInput.setAttribute('readonly', '');
    temporaryInput.style.position = 'fixed';
    temporaryInput.style.opacity = '0';
    document.body.appendChild(temporaryInput);
    temporaryInput.select();
    const wasCopied = document.execCommand('copy');
    document.body.removeChild(temporaryInput);
    return wasCopied;
  }
}
