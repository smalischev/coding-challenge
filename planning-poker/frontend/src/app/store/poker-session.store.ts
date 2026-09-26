import { Injectable, Signal, WritableSignal, computed, signal } from '@angular/core';
import { CardValue, Issue, Participant, UserRole } from '../models';

export interface CurrentUser { name: string; role: UserRole; }
type EstimatesByDeveloper = Partial<Record<string, CardValue>>;

@Injectable({ providedIn: 'root' })
export class PokerSessionStore {
  readonly sessionId: WritableSignal<string> = signal('');
  readonly currentUser: WritableSignal<CurrentUser> = signal({ name: '', role: 'Entwickler' });
  readonly activeIssue: WritableSignal<Issue | null> = signal(null);
  readonly releasedIssue: WritableSignal<Issue | null> = signal(null);
  readonly estimates: WritableSignal<EstimatesByDeveloper> = signal({});
  readonly revealed: WritableSignal<boolean> = signal(false);
  readonly participants: WritableSignal<Participant[]> = signal([]);
  readonly allEstimated: Signal<boolean> = computed(() => {
    const developers = this.participants().filter((person) => person.role === 'Entwickler');
    return developers.length > 0 && developers.every((person) => person.estimated);
  });

  setAuthenticatedUser(name: string, role: UserRole): void { this.currentUser.set({ name, role }); this.upsertParticipant({ name, role, estimated: false }); }
  setSession(id: string): void { this.sessionId.set(id); }
  setTestCurrentUser(participantName: string): void { const participant = this.participants().find((candidate) => candidate.name === participantName); if (participant) this.currentUser.set({ name: participant.name, role: participant.role }); }
  setActiveIssue(issue: Issue): void { this.activeIssue.set(issue); }
  releaseIssue(issue: Issue): void { this.activeIssue.set(issue); this.releasedIssue.set(issue); }

  setProgress(estimatedDevelopers: string[], pendingDevelopers: string[]): void {
    const developerNames = new Set([...estimatedDevelopers, ...pendingDevelopers]);
    this.participants.update((participants) => {
      const scrumMasters = participants.filter((person) => person.role === 'Scrum Master');
      const developers = [...developerNames].map((name) => ({ name, role: 'Entwickler' as const, estimated: estimatedDevelopers.includes(name) }));
      return [...scrumMasters, ...developers];
    });
  }

  selectCard(card: CardValue): void { this.estimates.update((estimates) => ({ ...estimates, [this.currentUser().name]: card })); this.participants.update((participants) => participants.map((person) => person.name === this.currentUser().name ? { ...person, estimated: true } : person)); }
  setRevealedEstimates(estimates: EstimatesByDeveloper): void { this.estimates.set(estimates); this.revealed.set(true); }
  startNewRound(): void { this.releasedIssue.set(null); this.revealed.set(false); this.estimates.set({}); this.participants.update((participants) => participants.map((person) => ({ ...person, estimated: false }))); }

  private upsertParticipant(participant: Participant): void {
    this.participants.update((participants) => participants.some((person) => person.name === participant.name) ? participants.map((person) => person.name === participant.name ? { ...person, ...participant } : person) : [...participants, participant]);
  }
}
