import { Component, effect, inject } from '@angular/core';
import { HeaderComponent } from './components/header/header.component';
import { IssueComponent } from './components/issue/issue.component';
import { ParticipantsComponent } from './components/participants/participants.component';
import { EstimationCardsComponent } from './components/estimation-cards/estimation-cards.component';
import { ModeratorControlsComponent } from './components/moderator-controls/moderator-controls.component';
import { ResultsComponent } from './components/results/results.component';
import { ScrumMasterNoticeComponent } from './components/scrum-master-notice/scrum-master-notice.component';
import { PokerSessionStore } from './store/poker-session.store';
import { IssueControlsComponent } from './components/issue-controls/issue-controls.component';
import { WaitingForReleaseComponent } from './components/waiting-for-release/waiting-for-release.component';
import { environment } from '../environments/environment';
import { AuthenticationComponent } from './components/authentication/authentication.component';
import { AuthSessionService } from './core/auth/auth-session.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [HeaderComponent, IssueComponent, ParticipantsComponent, EstimationCardsComponent, ModeratorControlsComponent, ResultsComponent, ScrumMasterNoticeComponent, IssueControlsComponent, WaitingForReleaseComponent, AuthenticationComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  readonly store = inject(PokerSessionStore);
  readonly auth = inject(AuthSessionService);
  readonly environment = environment;
  private readonly synchronizeAuthenticatedUser = effect(() => {
    const user = this.auth.user();
    if (user) {
      this.store.setAuthenticatedUser(user.username, user.role === 'SCRUM_MASTER' ? 'Scrum Master' : 'Entwickler');
    }
  });

  logout(): void {
    this.auth.logout().subscribe();
  }
}
