import { Injectable, Signal, WritableSignal, computed, signal } from '@angular/core';
import { CardValue, Issue, Participant } from '../models';

export interface CurrentUser {
  name: string;
  role: 'Scrum Master' | 'Entwickler';
}

type SessionId = string;

@Injectable({ providedIn: 'root' })
export class PokerSessionStore {
  readonly sessionId: WritableSignal<SessionId> = signal<SessionId>('ABC123');
  readonly currentUser: WritableSignal<CurrentUser> = signal<CurrentUser>({ name: 'Anna', role: 'Scrum Master' });
  readonly activeIssue: WritableSignal<Issue> = signal<Issue>({
    id: 42,
    title: 'Login überarbeiten',
    description: 'Die Anmeldung soll verständlicher werden und Fehlermeldungen klar darstellen. Die Umsetzung wird gemeinsam geschätzt.'
  });
  readonly selectedCard: WritableSignal<CardValue | null> = signal<CardValue | null>(null);
  readonly revealed: WritableSignal<boolean> = signal<boolean>(false);
  readonly participants: WritableSignal<Participant[]> = signal<Participant[]>([
    { name: 'Anna', role: 'Scrum Master', estimated: false },
    { name: 'Ben', role: 'Entwickler', estimated: true },
    { name: 'Lea', role: 'Entwickler', estimated: true },
    { name: 'Lea2', role: 'Entwickler', estimated: true }
  ]);

  readonly allEstimated: Signal<boolean> = computed(() => this.participants().every((person) => person.estimated));

  selectCard(card: CardValue): void {
    this.selectedCard.set(card);
    this.participants.update((participants) =>
      participants.map((person) => person.name === this.currentUser().name ? { ...person, estimated: true } : person)
    );
  }

  revealRound(): void {
    this.revealed.set(true);
  }

  startNewRound(): void {
    this.revealed.set(false);
    this.selectedCard.set(null);
    this.participants.update((participants) => participants.map((person) => ({ ...person, estimated: false })));
  }
}
