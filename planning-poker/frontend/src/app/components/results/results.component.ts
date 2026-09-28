import { Component, computed, effect, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { BackendCardValue } from '../../core/api/planning-poker-api.models';
import { PokerSessionStore } from '../../store/poker-session.store';
import { EstimateDistributionChartComponent } from './estimate-distribution-chart/estimate-distribution-chart.component';
import { ResultsService } from './results.service';

@Component({
  selector: 'app-results',
  standalone: true,
  imports: [EstimateDistributionChartComponent],
  templateUrl: './results.component.html',
  styleUrl: './results.component.css',
})
export class ResultsComponent {
  readonly store = inject(PokerSessionStore);
  readonly revealedEstimates = computed(() => Object.entries(this.store.estimates())
    .map(([developerName, value]) => ({ developerName, value }))
  );
  readonly estimateGroups = signal<Partial<Record<BackendCardValue, number>>>({});
  readonly average = signal<number | null>(null);
  readonly mostFrequentValue = signal<number | null>(null);
  readonly error = signal('');
  private readonly results = inject(ResultsService);

  private readonly loadRevealedResults = effect(() => {
    const sessionId = this.store.sessionId();
    if (!sessionId || !this.store.revealed()) return;
    forkJoin({ groups: this.results.getGroups(sessionId), average: this.results.getAverage(sessionId), mostFrequentValue: this.results.getMostFrequentValue(sessionId) }).subscribe({
      next: ({ groups, average, mostFrequentValue }) => {
        this.estimateGroups.set(groups);
        this.average.set(average.value);
        this.mostFrequentValue.set(mostFrequentValue.value);
        this.error.set('');
      },
      error: () => this.error.set('Die aufgedeckten Ergebnisse konnten nicht geladen werden.')
    });
  });
}
