import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CardValue } from '../../models';

@Component({
  selector: 'app-estimation-cards',
  standalone: true,
  templateUrl: './estimation-cards.component.html',
  styleUrl: './estimation-cards.component.css'
})
export class EstimationCardsComponent implements OnChanges {
  @Input() existingCard: CardValue | null = null;
  @Input() submitting = false;
  @Input() resetSelectionToken = 0;
  @Output() cardSelected = new EventEmitter<CardValue>();
  readonly cards: CardValue[] = ['0', '1', '2', '3', '5', '8', '13', '21', '34', '?', '☕'];
  selectedCard: CardValue | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['existingCard'] || changes['resetSelectionToken']) {
      this.selectedCard = this.existingCard;
    }
  }

  select(card: CardValue): void {
    if (this.submitting) return;
    this.selectedCard = card;
    this.cardSelected.emit(card);
  }
}
