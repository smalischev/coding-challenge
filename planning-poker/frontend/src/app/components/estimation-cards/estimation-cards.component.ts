import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CardValue } from '../../models';

@Component({
  selector: 'app-estimation-cards',
  standalone: true,
  templateUrl: './estimation-cards.component.html',
  styleUrl: './estimation-cards.component.css'
})
export class EstimationCardsComponent {
  @Input() selected: CardValue | null = null;
  @Output() cardSelected = new EventEmitter<CardValue>();
  readonly cards: CardValue[] = ['0', '1', '2', '3', '5', '8', '13', '21', '34', '?', '☕'];
  select(card: CardValue): void { this.cardSelected.emit(card); }
}
