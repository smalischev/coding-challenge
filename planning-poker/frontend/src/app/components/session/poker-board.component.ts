import { Component, effect, inject } from '@angular/core';
import { Router } from '@angular/router';
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
import { environment } from '../../../environments/environment';
import { PokerSessionStore } from '../../store/poker-session.store';

@Component({
  selector: 'app-poker-board',
  standalone: true,
  imports: [HeaderComponent, IssueComponent, ParticipantsComponent, EstimationCardsComponent, ModeratorControlsComponent, ResultsComponent, ScrumMasterNoticeComponent, IssueControlsComponent, WaitingForReleaseComponent],
  templateUrl: './poker-board.component.html',
  styleUrl: './poker-board.component.css'
})
export class PokerBoardComponent {
  readonly store = inject(PokerSessionStore);
  readonly environment = environment;
  private readonly authentication = inject(AuthenticationService);
  private readonly router = inject(Router);
  private readonly synchronizeAuthenticatedUser = effect(() => {
    const user = this.authentication.user();
    if (user) {
      this.store.setAuthenticatedUser(user.username, user.role === 'SCRUM_MASTER' ? 'Scrum Master' : 'Entwickler');
    }
  });

  logout(): void {
    this.authentication.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }
}
