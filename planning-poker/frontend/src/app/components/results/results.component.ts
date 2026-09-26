import { Component, computed, inject } from '@angular/core';
import { CardValue } from '../../models';
import { PokerSessionStore } from '../../store/poker-session.store';

interface RevealedEstimate {
  developerName: string;
  value: CardValue;
}

interface EstimateGroup {
  value: CardValue;
  count: number;
  percentage: number;
}

@Component({
  selector: 'app-results',
  standalone: true,
  templateUrl: './results.component.html',
  styleUrl: './results.component.css'
})
export class ResultsComponent {
  readonly store = inject(PokerSessionStore);
  readonly revealedEstimates = computed<RevealedEstimate[]>(() =>
    this.store.participants()
      .filter((person) => person.role === 'Entwickler')
      .flatMap((person) => {
        const value = this.store.estimates()[person.name];
        return value ? [{ developerName: person.name, value }] : [];
      })
  );
  readonly estimateGroups = computed<EstimateGroup[]>(() => {
    const counts = new Map<CardValue, number>();
    this.revealedEstimates().forEach(({ value }) => counts.set(value, (counts.get(value) ?? 0) + 1));
    const highestCount = Math.max(...counts.values(), 1);
    return [...counts.entries()].map(([value, count]) => ({ value, count, percentage: (count / highestCount) * 100 }));
  });
  readonly average = computed<number | null>(() => {
    const numericValues = this.revealedEstimates()
      .map(({ value }) => this.numericCardValue(value))
      .filter((value): value is number => value !== null);
    return numericValues.length === 0 ? null : numericValues.reduce((sum, value) => sum + value, 0) / numericValues.length;
  });
  readonly mostFrequentValue = computed<CardValue | null>(() => this.estimateGroups()[0]?.value ?? null);

  private numericCardValue(card: CardValue): number | null {
    return ['?', '☕'].includes(card) ? null : Number(card);
  }
}
