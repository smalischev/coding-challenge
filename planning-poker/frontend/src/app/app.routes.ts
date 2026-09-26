import { Routes } from '@angular/router';
import { AuthenticationComponent } from './components/authentication/authentication.component';
import { PokerBoardComponent } from './components/session/poker-board.component';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: 'login', component: AuthenticationComponent, data: { mode: 'login' } },
  { path: 'register', component: AuthenticationComponent, data: { mode: 'register' } },
  { path: 'poker', component: PokerBoardComponent, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'poker' },
  { path: '**', redirectTo: 'poker' }
];
