import { Component, effect, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthenticationService } from '../authentication/authentication.service';
import { EstimationCardsComponent } from '../estimation-cards/estimation-cards.component';
import { HeaderComponent } from '../header/header.component';
import { IssueControlsComponent } from '../issue-controls/issue-controls.component';
import { IssueComponent } from '../issue/issue.component';
import { ModeratorControlsComponent } from '../moderator-controls/moderator-controls.component';
import { ParticipantsComponent } from '../participants/participants.component';
import { ResultsComponent } from '../results/results.component';
import { ScrumMasterNoticeComponent } from '../scrum-master-notice/scrum-master-notice.component';
import { WaitingForReleaseComponent } from '../waiting-for-release/waiting-for-release.component';
import { SessionHistoryComponent } from '../session-history/session-history.component';
import { PokerSessionStore } from '../../store/poker-session.store';
import { SessionSetupComponent, CreateSessionInput } from './session-setup.component';
import { SessionService } from './session.service';
import { SessionEventsService } from './session-events.service';
import { IssueService } from '../issue/issue.service';
import { EstimationRoundRevealedError, EstimationService } from '../estimation-cards/estimation.service';
import { ResultsService } from '../results/results.service';
import { BackendCardValue, EstimationProgressResponse } from '../../core/api/planning-poker-api.models';
import { CardValue, Issue } from '../../models';

@Component({
  selector: 'app-poker-board',
  standalone: true,
  imports: [HeaderComponent, SessionSetupComponent, IssueComponent, ParticipantsComponent, EstimationCardsComponent, ModeratorControlsComponent, ResultsComponent, ScrumMasterNoticeComponent, IssueControlsComponent, WaitingForReleaseComponent, SessionHistoryComponent],
  templateUrl: './poker-board.component.html',
  styleUrl: './poker-board.component.css'
})
export class PokerBoardComponent {
  readonly store = inject(PokerSessionStore);
  readonly error = signal('');
  readonly finalizingResult = signal(false);
  readonly resultMessage = signal('');
  readonly allEstimatedNotification = signal(false);
  readonly estimateSubmitting = signal(false);
  readonly estimateResetToken = signal(0);
  private readonly authentication = inject(AuthenticationService);
  private readonly router = inject(Router);
  private readonly sessions = inject(SessionService);
  private readonly sessionEvents = inject(SessionEventsService);
  private readonly issues = inject(IssueService);
  private readonly estimations = inject(EstimationService);
  private readonly results = inject(ResultsService);

  private readonly synchronizeAuthenticatedUser = effect(() => {
    const user = this.authentication.user();
    if (user) {
      this.store.setAuthenticatedUser(user.username, user.role === 'SCRUM_MASTER' ? 'Scrum Master' : 'Entwickler');
    }
  });

  private readonly synchronizeSessionEvents = effect((onCleanup) => {
    const sessionId = this.store.sessionId();
    if (!sessionId) return;
    const subscription = this.sessionEvents.watch(sessionId).subscribe({
      next: (event) => this.applySessionEvent(event.name, event.data),
      error: () => this.error.set('Die Echtzeit-Synchronisation konnte nicht verbunden werden.')
    });
    onCleanup(() => subscription.unsubscribe());
  });

  logout(): void {
    this.authentication.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }

  createSession(input: CreateSessionInput): void {
    this.error.set('');
    this.sessions.create({ scrumMasterName: this.store.currentUser().name, ...input }).subscribe({
      next: ({ planningPokerId }) => { this.store.setSession(planningPokerId); this.loadSessionState(); },
      error: () => this.error.set('Die Sitzung konnte nicht erstellt werden. Prüfe GitLab-Projekt, Issue und Backend-Konfiguration.')
    });
  }

  joinSession(sessionId: string): void {
    this.error.set('');
    this.sessions.join(sessionId, { developerName: this.store.currentUser().name }).subscribe({
      next: () => { this.store.setSession(sessionId); this.loadSessionState(); },
      error: () => this.error.set('Der Beitritt zur Sitzung ist fehlgeschlagen. Prüfe die Session-ID.')
    });
  }

  releaseIssue(): void {
    const issue = this.store.activeIssue();
    if (!issue) return;
    this.issues.release(this.store.sessionId(), { scrumMasterName: this.store.currentUser().name }).subscribe({
      next: () => { this.allEstimatedNotification.set(false); this.store.releaseIssue(issue); this.refreshProgress(); },
      error: () => this.error.set('Das Issue konnte nicht freigegeben werden.')
    });
  }

  selectIssue(issueIid: number): void {
    this.error.set('');
    this.issues.select(this.store.sessionId(), {
      scrumMasterName: this.store.currentUser().name,
      gitlabIssueIid: issueIid,
    }).subscribe({
      next: () => {
        this.allEstimatedNotification.set(false);
        this.store.startNewRound();
        this.loadSessionState();
      },
      error: () => this.error.set('Das Issue konnte nicht ausgewählt werden.')
    });
  }

  submitEstimate(card: CardValue): void {
    if (this.estimateSubmitting() || this.store.revealed()) return;
    this.estimateSubmitting.set(true);
    this.estimations.submit(this.store.sessionId(), { developerName: this.store.currentUser().name, value: this.toBackendCard(card) }).subscribe({
      next: () => {
        this.store.selectCard(card);
        this.estimateSubmitting.set(false);
        this.refreshProgress();
      },
      error: (error: unknown) => {
        this.estimateSubmitting.set(false);
        this.estimateResetToken.update((token) => token + 1);
        if (error instanceof EstimationRoundRevealedError) {
          this.store.markRevealed();
          this.loadRevealedEstimates();
          this.error.set('Die Runde wurde inzwischen aufgedeckt. Die Kartenauswahl ist gesperrt.');
          return;
        }
        this.error.set('Die Schätzung konnte nicht gespeichert werden.');
      }
    });
  }

  revealRound(): void {
    this.estimations.reveal(this.store.sessionId(), { scrumMasterName: this.store.currentUser().name }).subscribe({
      next: () => this.loadRevealedEstimates(),
      error: () => this.error.set('Die Runde konnte nicht aufgedeckt werden.')
    });
  }

  startNewRound(): void { this.estimations.startNewRound(this.store.sessionId(), { scrumMasterName: this.store.currentUser().name }).subscribe({ next: () => { this.allEstimatedNotification.set(false); this.store.startNewRound(); this.store.refreshCompletedRounds(); }, error: () => this.error.set('Die neue Runde konnte nicht gestartet werden.') }); }

  finalizeResult(card: CardValue): void {
    this.finalizingResult.set(true);
    this.resultMessage.set('');
    this.results.finalize(this.store.sessionId(), { scrumMasterName: this.store.currentUser().name, value: this.toBackendCard(card) }).subscribe({
      next: () => { this.finalizingResult.set(false); this.resultMessage.set(`Ergebnis ${card} wurde an GitLab übergeben.`); },
      error: () => { this.finalizingResult.set(false); this.error.set('Das Ergebnis konnte nicht an GitLab übergeben werden.'); }
    });
  }

  private loadSessionState(): void {
    const sessionId = this.store.sessionId();
    forkJoin({
      issue: this.issues.getActive(sessionId),
      progress: this.estimations.getProgress(sessionId),
    }).subscribe({
      next: ({ issue, progress }) => {
        const activeIssue = this.toIssue(issue);
        this.store.setActiveIssue(activeIssue);
        this.store.setProgress(progress.developers, progress.scrumMasterName);
        if (progress.released) {
          this.store.releaseIssue(activeIssue);
        }
        if (progress.revealed) {
          this.store.markRevealed();
          this.loadRevealedEstimates();
        }
      },
      error: () => this.error.set('Der Zustand der Sitzung konnte nicht geladen werden.'),
    });
  }
  
  private refreshProgress(): void {
    this.estimations.getProgress(this.store.sessionId()).subscribe({ next: (progress) => this.store.setProgress(progress.developers, progress.scrumMasterName) });
  }

  private applySessionEvent(name: string, data: unknown): void {
    if (name === 'estimation-progress') {
      const progress = data as Pick<EstimationProgressResponse, 'developers' | 'scrumMasterName'>;
      this.store.setProgress(progress.developers, progress.scrumMasterName);
    }
    if (name === 'issue-released') this.store.releaseIssue(this.toIssue(data as { gitlabIssueIid: number; title: string; description: string }));
    if (name === 'all-developers-estimated') {
      this.allEstimatedNotification.set(true);
      this.store.markAllDevelopersEstimated();
    }
    if (name === 'round-revealed') this.store.setRevealedEstimates(Object.fromEntries((data as { developerName: string; value: BackendCardValue }[])
                                                                         .map((estimate) => [estimate.developerName, this.fromBackendCard(estimate.value)])
        )
    );  
    if (name === 'round-started') { 
      this.allEstimatedNotification.set(false);
      this.store.startNewRound();
      this.store.refreshCompletedRounds();
      this.loadSessionState();
    }
  }

  private loadRevealedEstimates(): void {
    this.results.getEstimates(this.store.sessionId()).subscribe((estimates) => this.store.setRevealedEstimates(
      Object.fromEntries(estimates.map((estimate) => [estimate.developerName, this.fromBackendCard(estimate.value)]))
    ));
  }

  private toIssue(issue: { gitlabIssueIid: number; title: string; description: string }): Issue { 
    return { 
      id: issue.gitlabIssueIid,
      title: issue.title,
      description: issue.description
    };
  }

  private toBackendCard(card: CardValue): BackendCardValue { 
    return ({ 
      '0': 'ZERO',
      '1': 'ONE',
      '2': 'TWO',
      '3': 'THREE',
      '5': 'FIVE',
      '8': 'EIGHT',
      '13': 'THIRTEEN',
      '21': 'TWENTY_ONE',
      '34': 'THIRTY_FOUR',
      '?': 'QUESTION_MARK',
      '☕': 'COFFEE'
    } as const)[card];
  }

  private fromBackendCard(card: BackendCardValue): CardValue { 
    return ({ 
      ZERO: '0',
      ONE: '1',
      TWO: '2',
      THREE: '3',
      FIVE: '5',
      EIGHT: '8',
      THIRTEEN: '13',
      TWENTY_ONE: '21',
      THIRTY_FOUR: '34',
      QUESTION_MARK: '?',
      COFFEE: '☕'
    } as const)[card];
  }
}
