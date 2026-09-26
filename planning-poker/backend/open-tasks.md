# Offene Aufgaben

- [ ] **GitLab-Issues eines Projekts abrufen** _(README-Pflicht)_

  Der GitLab-Client kann bisher nur ein Label an einem Issue setzen. Es fehlen der GitLab-API-Aufruf zum Abrufen der Issues eines Projekts und ein eigener REST-Endpunkt dafür.

<br>

- [ ] **Abstimmungsfortschritt in Echtzeit an alle Teilnehmer senden** _(README-Pflicht)_

  Der Fortschritt ist per REST abrufbar. Das SSE-Ereignis informiert bisher nur den Scrum Master, sobald alle Entwickler geschätzt haben. Für die README-Anforderung müssen alle Teilnehmer Änderungen des Fortschritts aktiv erhalten.

<br>

- [x] **Schnittstellen als OpenAPI dokumentieren** _(README-Pflicht)_

  REST-Endpunkte, SSE-Ereignisse und Bearer-JWT-Authentifizierung sind per MicroProfile-OpenAPI-Annotationen beschrieben und werden beim Build nach `target/openapi/planning-poker-api.yaml` erzeugt.

<br>

- [x] **JWT-Schlüssel in der Docker-Anleitung ergänzen** _(README-Pflicht)_

  Die README beschreibt nun die lokale Konfiguration, die Erzeugung des RSA-Schlüsselpaares, die Docker-Variablen, Start und Stopp sowie den Zugriff auf Swagger UI und OpenAPI.

<br>

- [x] **SSE-Benachrichtigung als Integrationstest prüfen**

  Der REST-Integrationstest öffnet einen SSE-Stream, gibt die letzte Schätzung ab und prüft den Empfang des Ereignisses `all-developers-estimated`.
