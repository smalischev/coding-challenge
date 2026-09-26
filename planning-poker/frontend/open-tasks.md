# Offene Aufgaben

- [ ] **Scrum-Master-Rolle beim Erstellen einer Sitzung automatisch vergeben**

  Das Backend soll dem Benutzer, der eine Planning-Poker-Sitzung erstellt, automatisch die Rolle `SCRUM_MASTER` zuweisen und ihn als Owner dieser Sitzung speichern. Damit entspricht das technische Verhalten der Anforderung aus der Frontend-README: Der Ersteller einer Sitzung ist automatisch Scrum Master und erhält exklusiven Zugriff auf die Steuerungsfunktionen.

  Derzeit muss ein Benutzer vor dem Erstellen bereits mit der Rolle `SCRUM_MASTER` registriert sein; der übermittelte `scrumMasterName` wird beim Erstellen der Sitzung nicht verwendet.


- [x] **Authentifizierung umsetzen**

  Ansichten für Registrierung, Login und Logout sind mit dem vorhandenen API-Service verbunden. Das JWT wird für die Browser-Session gehalten; Rolle und angemeldeter Benutzer werden danach in den Signal-Store übernommen.


- [x] **CORS im Backend für das Frontend freigeben**

  Das Quarkus-Backend erlaubt Browser-Anfragen des Angular-Frontends von `http://localhost:4200` und `http://69.62.113.205:4200`; die jeweiligen Preflights wurden erfolgreich geprüft.


- [x] **Development-Login ohne Backend ergänzen**

  Im Development-Build steht eine nicht persistierte lokale Demo-Session für Anna als Scrum Master bereit. Im Production-Build ist diese Option ausgeblendet und sendet keine Backend-Anfrage.


- [x] **Backend lokal starten und Frontend-Verbindung prüfen**

  Backend gemäß dessen README mit JWT-Schlüsseln gestartet. OpenAPI, CORS-Preflight sowie Registrierung und Login wurden erfolgreich gegen die laufende Instanz geprüft.


- [x] **Vertikalen Backend-Ablauf integrieren und testen**

  Die Frontend-Schritte für Login, Sitzung erstellen oder beitreten, aktives Issue laden und freigeben, schätzen, Fortschritt abrufen sowie Runde aufdecken sind angebunden. Der Ablauf wurde gegen das Backend im Quarkus-Profil `dev` vollständig geprüft; dort wird ein lokales GitLab-Mock-Gateway statt des externen GitLab-Gateways injiziert.


- [x] **Sitzung erstellen und beitreten ermöglichen**

  Ein Formular für GitLab-Projekt-ID und Issue-IID für Scrum Master sowie eine Beitrittsansicht für Entwickler über Session-ID ergänzen. Die Endpunkte zum Erstellen und Beitreten übernehmen die Session-ID und den aktuellen Teilnehmer in den Signal-Store.


- [x] **Issue-Auswahl und Freigabe durch den Scrum Master umsetzen**

  Der Scrum Master sieht das beim Anlegen geladene aktive Issue und gibt es explizit zur Schätzrunde frei. Vor der Freigabe sehen Entwickler einen Wartehinweis. Das Laden und Freigeben sind mit den Backend-Endpunkten verbunden.


- [x] **Abstimmungsfortschritt dynamisch anzeigen**

  Der Status „hat geschätzt“ beziehungsweise „ausstehend“ sowie der Hinweis, dass alle Entwickler geschätzt haben, aktualisieren sich aus dem Signal-Store. Nach Beitritt, Freigabe und abgegebener Schätzung werden die Fortschrittsdaten vom Backend geladen; Echtzeit-Updates bleiben eine eigene Aufgabe.


- [x] **Aufgedeckte Ergebnisse dynamisch darstellen**

  Einzelwerte, Gruppierung, Durchschnitt und häufigsten Wert werden nach dem Aufdecken über die Ergebnis-Endpunkte geladen und dynamisch dargestellt. Die Balkendiagrammwerte leiten sich aus der Backend-Verteilung ab; Scrum Master erscheinen nicht als Schätzer.


- [x] **Finales Ergebnis nach GitLab übernehmen**

  Nach dem Aufdecken kann der Scrum Master ausschließlich eine Karte aus dem definierten Kartensatz wählen und bestätigen. Die Auswahl ruft den Ergebnis-Endpunkt auf; der Ablauf wurde im Backend-Profil `dev` gegen das injizierte GitLab-Mock-Gateway mit `204` geprüft.


- [ ] **Sitzungsprotokoll für abgeschlossene Runden anzeigen**

  Abgeschlossene Schätzrunden mit Issue und Ergebnis im UI festhalten und darstellen.


- [ ] **Echtzeit-Synchronisation anbinden**

  Für den Scrum Master ist der vorhandene SSE-Endpunkt angebunden: Sobald alle Entwickler geschätzt haben, erscheint eine Echtzeit-Benachrichtigung und der Fortschritt wird neu geladen. Teilnehmer, Issue, Fortschritt und aufgedeckte Ergebnisse sind darüber hinaus noch nicht in Echtzeit synchronisiert.

  - [x] Scrum Master per SSE benachrichtigen, sobald alle Developer geschätzt haben
