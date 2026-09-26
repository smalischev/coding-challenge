# Offene Aufgaben

- [ ] **Scrum-Master-Rolle beim Erstellen einer Sitzung automatisch vergeben**

  Das Backend soll dem Benutzer, der eine Planning-Poker-Sitzung erstellt, automatisch die Rolle `SCRUM_MASTER` zuweisen und ihn als Owner dieser Sitzung speichern. Damit entspricht das technische Verhalten der Anforderung aus der Frontend-README: Der Ersteller einer Sitzung ist automatisch Scrum Master und erhält exklusiven Zugriff auf die Steuerungsfunktionen.

  Derzeit muss ein Benutzer vor dem Erstellen bereits mit der Rolle `SCRUM_MASTER` registriert sein; der übermittelte `scrumMasterName` wird beim Erstellen der Sitzung nicht verwendet.
