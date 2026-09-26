import { Component, inject } from '@angular/core';
import { HeaderComponent } from './components/header/header.component';
import { IssueComponent } from './components/issue/issue.component';
import { ParticipantsComponent } from './components/participants/participants.component';
import { EstimationCardsComponent } from './components/estimation-cards/estimation-cards.component';
import { ModeratorControlsComponent } from './components/moderator-controls/moderator-controls.component';
import { ResultsComponent } from './components/results/results.component';
import { ScrumMasterNoticeComponent } from './components/scrum-master-notice/scrum-master-notice.component';
import { PokerSessionStore } from './store/poker-session.store';
import { RoleSwitcherComponent } from './components/role-switcher/role-switcher.component';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [HeaderComponent, IssueComponent, ParticipantsComponent, EstimationCardsComponent, ModeratorControlsComponent, ResultsComponent, ScrumMasterNoticeComponent, RoleSwitcherComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  readonly store = inject(PokerSessionStore);
  readonly environment = environment;
}
