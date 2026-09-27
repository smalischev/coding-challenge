# Offene Tests

- [ ] **Ungültigen Session-Beitritt testen**

  Prüfen, dass ein Developer bei einer ungültigen oder nicht existierenden Session-ID eine verständliche Fehlermeldung sieht.


- [ ] **Beitritt nach Freigabe ablehnen**

  Prüfen, dass ein Developer nach der Freigabe der Schätzrunde nicht mehr beitreten kann.


- [ ] **Developer-Wartezustand und Freigabe testen**

  Prüfen, dass ein Developer vor der Freigabe den Wartehinweis sieht und nach der Freigabe per Polling die Schätzkarten erhält.


- [ ] **Schätzfortschritt für Scrum Master testen**

  Prüfen, dass der Scrum Master über das Fortschritts-Polling erkennt, welcher Developer bereits geschätzt hat und wer noch aussteht.


- [ ] **Frühzeitiges Aufdecken testen**

  Prüfen, dass der Scrum Master die Runde aufdecken kann, obwohl noch Schätzungen ausstehen.


- [ ] **Aufgedeckte Ergebnisse testen**

  Prüfen, dass Einzelkarten, Verteilung, Durchschnitt und häufigster Wert nach dem Aufdecken korrekt dargestellt werden.


- [ ] **Logout und Route Guard testen**

  Prüfen, dass Logout zum Login führt und die Route '/poker' ohne Anmeldung nicht erreichbar ist.


- [ ] **Ungültige Registrierung testen**

  Prüfen, dass ein zu kurzes Passwort oder ein bereits vergebener Benutzername eine verständliche Fehlermeldung erzeugt.


- [ ] **Nicht erlaubte Rollenaktionen testen**

  Prüfen, dass ein Developer keine Session erstellen und kein Ergebnis an GitLab übergeben kann.


- [ ] **Neue Runde testen**

  Prüfen, dass eine neue Runde nach dem Aufdecken Karten und Schätzfortschritt zurücksetzt.
