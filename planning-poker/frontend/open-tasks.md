# Offene Aufgaben

- [ ] **Scrum-Master-Rolle beim Erstellen einer Sitzung automatisch vergeben**

  Das Backend soll dem Benutzer, der eine Planning-Poker-Sitzung erstellt, automatisch die Rolle `SCRUM_MASTER` zuweisen und ihn als Owner dieser Sitzung speichern. Damit entspricht das technische Verhalten der Anforderung aus der Frontend-README: Der Ersteller einer Sitzung ist automatisch Scrum Master und erhält exklusiven Zugriff auf die Steuerungsfunktionen.

  Derzeit muss ein Benutzer vor dem Erstellen bereits mit der Rolle `SCRUM_MASTER` registriert sein; der übermittelte `scrumMasterName` wird beim Erstellen der Sitzung nicht verwendet.


- [ ] **Authentifizierung umsetzen**

  Ansichten für Registrierung, Login und Logout erstellen und mit dem vorhandenen API-Service verbinden. Das JWT sicher für die Browser-Session halten; Rolle und angemeldeten Benutzer danach in den Signal-Store übernehmen.


- [ ] **Sitzung erstellen und beitreten ermöglichen**

  Ein Formular für GitLab-Projekt-ID und Issue-IID für Scrum Master sowie eine Beitrittsansicht für Entwickler über Session-ID oder Einladungslink ergänzen. Die Endpunkte zum Erstellen und Beitreten aufrufen und die Session-ID sowie Teilnehmerdaten in den Signal-Store übernehmen.


- [x] **Issue-Auswahl und Freigabe durch den Scrum Master umsetzen**

  Der Scrum Master kann im lokalen UI ein Issue auswählen und es explizit zur Schätzrunde freigeben. Vor der Freigabe sehen Entwickler einen Wartehinweis. Die Auswahl und Freigabe müssen noch mit den vorhandenen Backend-Endpunkten verbunden werden.


- [x] **Abstimmungsfortschritt dynamisch anzeigen**

  Der Status „hat geschätzt“ beziehungsweise „ausstehend“ sowie der Hinweis, dass alle Entwickler geschätzt haben, aktualisieren sich aus dem lokalen Signal-Store. Die Fortschrittsdaten müssen noch aus dem Backend geladen und in Echtzeit aktualisiert werden.


- [ ] **Aufgedeckte Ergebnisse dynamisch darstellen**

  Einzelwerte, Gruppierung, Durchschnitt und häufigsten Wert aus dem Store beziehungsweise Backend darstellen. Die fest verdrahteten Werte entfernen; Scrum Master dürfen nicht als Schätzer erscheinen.


- [ ] **Finales Ergebnis nach GitLab übernehmen**

  Nach dem Aufdecken eine Auswahl ausschließlich aus dem definierten Kartensatz anbieten und die bestätigte Karte in GitLab übernehmen.


- [ ] **Sitzungsprotokoll für abgeschlossene Runden anzeigen**

  Abgeschlossene Schätzrunden mit Issue und Ergebnis im UI festhalten und darstellen.


- [ ] **Echtzeit-Synchronisation anbinden**

  Teilnehmer, Issue, Fortschritt und aufgedeckte Ergebnisse mit dem Backend synchronisieren. Bis ein vollständiger Echtzeitkanal verfügbar ist, kann der Fortschritt per Polling aktualisiert werden.
