# Offene Aufgaben

- [x] **Details eines ausgewählten GitLab-Issues abrufen** _(README-Pflicht)_

  Beim Erstellen oder Wechseln eines Issues lädt das Backend IID, Titel und Beschreibung über die GitLab-API. Das aktive Issue ist anschließend über einen REST-Endpunkt für die Teilnehmer verfügbar.

<br>

- [ ] **Liste offener GitLab-Issues eines Projekts anbieten**

  Eine Auswahlliste offener Issues wäre eine Komfortfunktion für das Frontend. Die README verlangt sie nicht ausdrücklich, weil der Scrum Master die Issue-IID beim Erstellen der Session angeben kann.

<br>

- [x] **Abstimmungsfortschritt in Echtzeit an alle Teilnehmer senden** _(README-Pflicht)_

  Das SSE-Ereignis `estimation-progress` sendet Änderungen des Abstimmungsfortschritts an alle Abonnenten. Das Ereignis `all-developers-estimated` geht weiterhin ausschließlich an den Scrum Master.

<br>

- [x] **Schnittstellen als OpenAPI dokumentieren** _(README-Pflicht)_

  REST-Endpunkte, SSE-Ereignisse und Bearer-JWT-Authentifizierung sind per MicroProfile-OpenAPI-Annotationen beschrieben und werden beim Build nach `target/openapi/planning-poker-api.yaml` erzeugt.

<br>

- [x] **JWT-Schlüssel in der Docker-Anleitung ergänzen** _(README-Pflicht)_

  Die README beschreibt nun die lokale Konfiguration, die Erzeugung des RSA-Schlüsselpaares, die Docker-Variablen, Start und Stopp sowie den Zugriff auf Swagger UI und OpenAPI.

<br>

- [x] **SSE-Benachrichtigung als Integrationstest prüfen**

  Der REST-Integrationstest öffnet einen SSE-Stream, gibt die letzte Schätzung ab und prüft den Empfang des Ereignisses `all-developers-estimated`.

<br>

- [ ] **Fachliche API-Fehler vereinheitlichen**

  Fachliche Fehler sollen über dokumentierte HTTP-Statuscodes und ein einheitliches JSON-Fehlerformat an das Frontend geliefert werden. Dafür sind spezifische Exceptions und ein zentraler JAX-RS-ExceptionMapper vorzusehen, beispielsweise für unbekannte Sessions (`404 SESSION_NOT_FOUND`), Beitritt nach Freigabe (`409 SESSION_ALREADY_RELEASED`), bereits beigetretene Developer (`409 MEMBER_ALREADY_JOINED`) und nicht erlaubte Rollenaktionen (`403 ROLE_NOT_ALLOWED`).
