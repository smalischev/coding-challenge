# Code Challenge (Backend)

## Ziel

Erstelle ein Java-Backend mit Quarkus und Jakarta EE, das GitLab-Issues über die GitLab-API abruft, diese mit User Journeys und User Steps aus einer relationalen Datenbank anreichert und dem Frontend über eine REST-API zur Verfügung stellt. Zusätzlich soll das Backend die Zuordnung von Issues zu User Steps und Releases persistent verwalten können.

## Aufgabe

Erstellen Sie einen REST-basierten Backend-Service mit folgenden Funktionalitäten:

### 1. Abruf von GitLab-Issues

- Nutze die GitLab-API, um Issues aus einem bestimmten Projekt zu laden.
- Authentifizierung über Personal Access Token (kann über Properties o.ä. konfigurierbar sein).
- Optional: Nutze Pagination und Caching zur Optimierung.
- Die GitLab-Issues sollen mindestens folgende Felder enthalten:
  - Issue ID
  - Title
  - Description
  - Labels (optional)

### 2. Domänenmodell & Datenhaltung

Nutze eine relationale Datenbank, um die folgenden Strukturen zu speichern:

- UserJourney
  - id, title
- UserStep
  - id, title, user_journey_id (FK)
- Release
  - id, name
- IssueAssignment
  - id, gitlab_issue_id, user_step_id (FK), release_id (FK, optional)

### 3. REST-Schnittstellen

#### GET /api/issues

- Gibt eine Liste aller GitLab-Issues zurück, angereichert mit:
  - Zugehörigem User Journey und User Step (sofern zugeordnet)
  - Release-Zuordnung (sofern vorhanden)

#### POST /api/assignments

- Payload: JSON-Objekt mit GitLab-Issue-ID, User Step ID, optional Release ID
- Persistiert oder aktualisiert die Zuordnung in der Datenbank

#### GET /api/user-story-map

- Gibt die komplette hierarchische Struktur zurück:
  - User Journeys > User Steps > zugeordnete Issues (inkl. Release)

## Abgabe

Bitte stelle das Projekt als Git-Repository zur Verfügung (bevorzugt auf GitHub/GitLab). Gebe eine Anleitung zur Einrichtung und zum Starten der Anwendung im `README.md` an.

Berückichtige dabei Postman oder curl-Beispiele für die API-Aufrufe. Alternativ kannst du auch OpenAPI nutzen.

## Optional

- Authentifizierung (z.B. JWT, Basic Auth)
- GitLab-API-Caching (z.B. mit Redis oder im Speicher)
- Unit-/Integrationstests mit JUnit
- Webhook-Verarbeitung von GitLab-Issue-Events
