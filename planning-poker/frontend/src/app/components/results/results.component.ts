import { Component, effect, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { BackendCardValue } from '../../core/api/planning-poker-api.models';
import { CardValue } from '../../models';
import { PokerSessionStore } from '../../store/poker-session.store';
import { ResultsService } from './results.service';

interface RevealedEstimate { developerName: string; value: CardValue; }
interface EstimateGroup { value: CardValue; count: number; percentage: number; }

@Component({ selector: 'app-results', standalone: true, templateUrl: './results.component.html', styleUrl: './results.component.css' })
export class ResultsComponent {
  readonly store = inject(PokerSessionStore);
  readonly revealedEstimates = signal<RevealedEstimate[]>([]);
  readonly estimateGroups = signal<EstimateGroup[]>([]);
  readonly average = signal<number | null>(null);
  readonly mostFrequentValue = signal<number | null>(null);
  readonly error = signal('');
  private readonly results = inject(ResultsService);

  private readonly loadRevealedResults = effect(() => {
    const sessionId = this.store.sessionId();
    if (!sessionId || !this.store.revealed()) return;
    forkJoin({ estimates: this.results.getEstimates(sessionId), groups: this.results.getGroups(sessionId), average: this.results.getAverage(sessionId), mostFrequentValue: this.results.getMostFrequentValue(sessionId) }).subscribe({
      next: ({ estimates, groups, average, mostFrequentValue }) => {
        this.revealedEstimates.set(estimates.map((estimate) => ({ developerName: estimate.developerName, value: this.fromBackendCard(estimate.value) })));
        this.estimateGroups.set(this.toGroups(groups));
        this.average.set(average.value);
        this.mostFrequentValue.set(mostFrequentValue.value);
        this.error.set('');
      },
      error: () => this.error.set('Die aufgedeckten Ergebnisse konnten nicht geladen werden.')
    });
  });

  private toGroups(groups: Partial<Record<BackendCardValue, number>>): EstimateGroup[] {
    const entries = Object.entries(groups) as [BackendCardValue, number][];
    const highestCount = Math.max(...entries.map(([, count]) => count), 1);
    return entries.map(([value, count]) => ({ value: this.fromBackendCard(value), count, percentage: (count / highestCount) * 100 }));
  }

  private fromBackendCard(card: BackendCardValue): CardValue {
    return ({ ZERO: '0', ONE: '1', TWO: '2', THREE: '3', FIVE: '5', EIGHT: '8', THIRTEEN: '13', TWENTY_ONE: '21', THIRTY_FOUR: '34', QUESTION_MARK: '?', COFFEE: '☕' } as const)[card];
  }
}
