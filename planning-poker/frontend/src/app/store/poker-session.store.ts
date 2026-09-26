import { Injectable, Signal, WritableSignal, computed, signal } from '@angular/core';
import { CardValue, Issue, Participant, UserRole } from '../models';

export interface CurrentUser {
  name: string;
  role: UserRole;
}

type SessionId = string;
type EstimatesByDeveloper = Partial<Record<string, CardValue>>;

@Injectable({ providedIn: 'root' })
export class PokerSessionStore {
  readonly sessionId: WritableSignal<SessionId> = signal<SessionId>('ABC123');
  readonly currentUser: WritableSignal<CurrentUser> = signal<CurrentUser>({ name: 'Anna', role: 'Scrum Master' });
  readonly releasedIssue: WritableSignal<Issue | null> = signal<Issue | null>(null);
  readonly estimates: WritableSignal<EstimatesByDeveloper> = signal<EstimatesByDeveloper>({
    Ben: '5',
    Lea: '8',
    Lea2: '5'
  });
  readonly revealed: WritableSignal<boolean> = signal<boolean>(false);
  readonly participants: WritableSignal<Participant[]> = signal<Participant[]>([
    { name: 'Anna', role: 'Scrum Master', estimated: false },
    { name: 'Ben', role: 'Entwickler', estimated: true },
    { name: 'Lea', role: 'Entwickler', estimated: true },
    { name: 'Lea2', role: 'Entwickler', estimated: true }
  ]);

  readonly allEstimated: Signal<boolean> = computed(() =>
    this.participants().filter((person) => person.role === 'Entwickler').every((person) => person.estimated)
  );

  setAuthenticatedUser(name: string, role: UserRole): void {
    this.currentUser.set({ name, role });
  }
  releaseIssue(issue: Issue): void {
    this.releasedIssue.set(issue);
  }

  setTestCurrentUser(participantName: string): void {
    const participant = this.participants().find((candidate) => candidate.name === participantName);
    if (!participant) {
      return;
    }
    this.currentUser.set({ name: participant.name, role: participant.role });
  }

  selectCard(card: CardValue): void {
    this.estimates.update((estimates) => ({ ...estimates, [this.currentUser().name]: card }));
    this.participants.update((participants) =>
      participants.map((person) => person.name === this.currentUser().name ? { ...person, estimated: true } : person)
    );
  }

  revealRound(): void {
    this.revealed.set(true);
  }

  startNewRound(): void {
    this.revealed.set(false);
    this.estimates.set({});
    this.participants.update((participants) => participants.map((person) => ({ ...person, estimated: false })));
  }

}
