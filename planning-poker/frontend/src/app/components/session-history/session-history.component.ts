import { DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal } from '@angular/core';
import { CompletedRoundResponse, PlanningPokerId } from '../../core/api/planning-poker-api.models';
import { SessionHistoryService } from './session-history.service';

@Component({
  selector: 'app-session-history',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './session-history.component.html',
  styleUrl: './session-history.component.css',
})
export class SessionHistoryComponent {
  readonly sessionId = input.required<PlanningPokerId>();
  readonly refreshVersion = input(0);
  readonly rounds = signal<CompletedRoundResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  private readonly history = inject(SessionHistoryService);

  private readonly loadHistory = effect((onCleanup) => {
    const sessionId = this.sessionId();
    this.refreshVersion();
    if (!sessionId) {
      this.rounds.set([]);
      return;
    }

    this.loading.set(true);
    const subscription = this.history.getCompletedRounds(sessionId).subscribe({
      next: (rounds) => {
        this.rounds.set(rounds);
        this.error.set('');
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Das Sitzungsprotokoll konnte nicht geladen werden.');
        this.loading.set(false);
      },
    });
    onCleanup(() => subscription.unsubscribe());
  });
}
