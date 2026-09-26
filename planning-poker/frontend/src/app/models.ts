export type CardValue = '0' | '1' | '2' | '3' | '5' | '8' | '13' | '21' | '34' | '?' | '☕';

export interface Participant {
  name: string;
  role: 'Scrum Master' | 'Entwickler';
  estimated: boolean;
}

export interface Issue {
  id: number;
  title: string;
  description: string;
}
