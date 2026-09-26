# Offene Aufgaben

- [ ] **Scrum-Master-Rolle beim Erstellen einer Sitzung automatisch vergeben**

  Das Backend soll dem Benutzer, der eine Planning-Poker-Sitzung erstellt, automatisch die Rolle `SCRUM_MASTER` zuweisen und ihn als Owner dieser Sitzung speichern. Damit entspricht das technische Verhalten der Anforderung aus der Frontend-README: Der Ersteller einer Sitzung ist automatisch Scrum Master und erhält exklusiven Zugriff auf die Steuerungsfunktionen.

  Derzeit muss ein Benutzer vor dem Erstellen bereits mit der Rolle `SCRUM_MASTER` registriert sein; der übermittelte `scrumMasterName` wird beim Erstellen der Sitzung nicht verwendet.


- [ ] **Authentifizierung umsetzen**

  Ansichten für Registrierung, Login und Logout erstellen. Die Rolle und der angemeldete Benutzer werden danach aus dem Backend-JWT übernommen.


- [ ] **Sitzung erstellen und beitreten ermöglichen**

  Ein Formular für GitLab-Projekt-ID und Issue-IID für Scrum Master sowie eine Beitrittsansicht für Entwickler über Session-ID oder Einladungslink ergänzen.


- [ ] **Issue-Auswahl und Freigabe durch den Scrum Master umsetzen**

  Der Scrum Master muss die Issue-IID ändern und das ausgewählte Issue explizit zur Schätzrunde freigeben können. Vor der Freigabe darf das Issue für Entwickler nicht sichtbar sein.


- [ ] **Abstimmungsfortschritt dynamisch anzeigen**

  Den Status „hat geschätzt“ beziehungsweise „ausstehend“ sowie den Hinweis, dass alle Entwickler geschätzt haben, aus dem Store beziehungsweise Backend aktualisieren.


- [ ] **Aufgedeckte Ergebnisse dynamisch darstellen**

  Einzelwerte, Gruppierung, Durchschnitt und häufigsten Wert aus dem Store beziehungsweise Backend darstellen. Die fest verdrahteten Werte entfernen; Scrum Master dürfen nicht als Schätzer erscheinen.


- [ ] **Finales Ergebnis nach GitLab übernehmen**

  Nach dem Aufdecken eine Auswahl ausschließlich aus dem definierten Kartensatz anbieten und die bestätigte Karte in GitLab übernehmen.


- [ ] **Sitzungsprotokoll für abgeschlossene Runden anzeigen**

  Abgeschlossene Schätzrunden mit Issue und Ergebnis im UI festhalten und darstellen.


- [ ] **Echtzeit-Synchronisation anbinden**

  Teilnehmer, Issue, Fortschritt und aufgedeckte Ergebnisse mit dem Backend synchronisieren. Bis ein vollständiger Echtzeitkanal verfügbar ist, kann der Fortschritt per Polling aktualisiert werden.
