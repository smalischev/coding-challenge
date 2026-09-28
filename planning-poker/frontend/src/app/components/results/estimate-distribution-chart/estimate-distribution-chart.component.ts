import { Component, computed, input } from '@angular/core';
import { ChartData, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { BackendCardValue } from '../../../core/api/planning-poker-api.models';
import { CardValue } from '../../../models';

const CARD_VALUES: readonly CardValue[] = ['0', '1', '2', '3', '5', '8', '13', '21', '34', '?', '☕'];
const CARD_COLORS = [
  '#64748b', '#0ea5e9', '#14b8a6', '#22c55e', '#84cc16', '#eab308',
  '#f97316', '#ef4444', '#ec4899', '#8b5cf6', '#a16207',
];
const DEMO_COUNTS: Readonly<Partial<Record<CardValue, number>>> = {
  '2': 1,
  '3': 3,
  '5': 7,
  '8': 5,
  '13': 3,
  '21': 1,
};

@Component({
  selector: 'app-estimate-distribution-chart',
  standalone: true,
  imports: [BaseChartDirective],
  templateUrl: './estimate-distribution-chart.component.html',
  styleUrl: './estimate-distribution-chart.component.css',
})
export class EstimateDistributionChartComponent {
  readonly groups = input<Partial<Record<BackendCardValue, number>>>({});
  readonly includeDemoEstimates = input(false);
  readonly chartData = computed<ChartData<'bar'>>(() => {
    const countsByCard = new Map(
      (Object.entries(this.groups()) as [BackendCardValue, number][])
        .map(([value, count]) => [this.fromBackendCard(value), count]),
    );

    return {
      labels: [...CARD_VALUES],
      datasets: [{
        label: 'Schätzungen',
        data: CARD_VALUES.map((value) =>
          (countsByCard.get(value) ?? 0) + (this.includeDemoEstimates() ? (DEMO_COUNTS[value] ?? 0) : 0),
        ),
        backgroundColor: CARD_COLORS,
        borderColor: CARD_COLORS,
        borderWidth: 1,
        borderRadius: 4,
        maxBarThickness: 38,
      }],
    };
  });
  readonly chartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    layout: { padding: { top: 6, right: 8 } },
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (context) => `${context.parsed.y} ${context.parsed.y === 1 ? 'Schätzung' : 'Schätzungen'}`,
        },
      },
    },
    scales: {
      x: {
        title: {
          display: true,
          text: 'Kartenwert',
          color: '#81889a',
          font: { size: 11, weight: 500 },
          padding: { top: 10 },
        },
        ticks: { color: '#4b5563', font: { size: 11, weight: 600 } },
        grid: { display: false },
        border: { color: '#dfe3ec' },
      },
      y: {
        beginAtZero: true,
        ticks: { color: '#81889a', font: { size: 10 }, padding: 8, precision: 0 },
        title: {
          display: true,
          text: 'Anzahl',
          color: '#81889a',
          font: { size: 11, weight: 500 },
          padding: { bottom: 8 },
        },
        grid: { color: '#edf0f5', drawTicks: false },
        border: { display: false },
      },
    },
  };

  private fromBackendCard(card: BackendCardValue): CardValue {
    return ({
      ZERO: '0',
      ONE: '1',
      TWO: '2',
      THREE: '3',
      FIVE: '5',
      EIGHT: '8',
      THIRTEEN: '13',
      TWENTY_ONE: '21',
      THIRTY_FOUR: '34',
      QUESTION_MARK: '?',
      COFFEE: '☕',
    } as const)[card];
  }
}
