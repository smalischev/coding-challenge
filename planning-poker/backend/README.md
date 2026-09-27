# Code Challenge – Planning Poker (Backend)

## Ziel

Erstelle ein Java-Backend mit Quarkus und Jakarta EE, das eine kollaborative Planning-Poker-Sitzung für Scrum-Teams ermöglicht. Das Backend verwaltet Sitzungen und Schätzrunden, ruft zu schätzende Issues über die GitLab-API ab und schreibt das abgestimmte Ergebnis nach Abschluss einer Runde zurück an das jeweilige GitLab-Issue.

## Aufgabe

### 1. Sitzung erstellen und beitreten

- Der Scrum Master legt eine neue Planning-Poker-Sitzung an und gibt dabei die GitLab-Projekt-ID sowie das zu schätzende Issue an.
- Weitere Teilnehmer können der Sitzung beitreten und geben dabei einen Namen an, der während der Sitzung sichtbar bleibt.
- Das Backend unterscheidet zwischen den Rollen Scrum Master und Entwickler und stellt sicher, dass Steuerungsaktionen ausschließlich dem Scrum Master vorbehalten sind.

### 2. Issue-Auswahl durch den Scrum Master

- Der Scrum Master wählt das Issue aus, das als nächstes geschätzt werden soll, und gibt es für die aktive Schätzrunde frei.
- Erst nach der Freigabe können Teilnehmer Schätzungen für dieses Issue abgeben.
- Der Scrum Master kann das aktive Issue wechseln, solange die Runde noch nicht abgeschlossen ist.

### 3. Schätzrunde

- Teilnehmer geben verdeckt eine Schätzung ab (Fibonacci-Folge: 0, 1, 2, 3, 5, 8, 13, 21, 34, ?, ☕).
- Alle Teilnehmer erhalten in Echtzeit den Abstimmungsfortschritt: wer bereits geschätzt hat und wer noch aussteht – ohne den konkreten Schätzwert zu verraten.
- Sobald alle Teilnehmer geschätzt haben, wird der Scrum Master darüber informiert.

### 4. Auflösung durch den Scrum Master

- Der Scrum Master kann die Runde jederzeit auflösen, unabhängig davon ob alle Teilnehmer abgestimmt haben.
- Nach der Auflösung erhalten alle Teilnehmer:
  - Den Schätzwert je Teilnehmer.
  - Eine Gruppenauswertung: wie viele Teilnehmer jeweils denselben Wert gewählt haben.
  - Durchschnitt und häufigsten Wert als Orientierung.

### 5. Weitere Schätzrunde oder Ergebnisübernahme

Nach der Auflösung stehen dem Scrum Master zwei Optionen zur Verfügung:

#### Option A – Neue Schätzrunde für dasselbe Issue
- Der Scrum Master startet eine neue Runde für dasselbe Issue.
- Alle bisherigen Karten werden zurückgesetzt.
#### Option B – Ergebnis in GitLab übernehmen
- Der Scrum Master wählt den vereinbarten Wert aus dem definierten Kartensatz (0, 1, 2, 3, 5, 8, 13, 21, 34, ?, ☕) und bestätigt die Runde. Errechnete Werte wie der Durchschnitt sind nicht direkt übernehmbar.
- Der Wert wird am GitLab-Issue hinterlegt. Wie und in welcher Form ist Teil der Aufgabe – bitte dokumentiere deine Entscheidung im `README.md`.

## Kommunikation

Wie das Frontend mit dem Backend kommuniziert – Protokoll, Datenformat, Schnittstellen und Echtzeit-Mechanismus – ist vollständig dir überlassen. Bitte dokumentiere deine Entscheidungen und die Schnittstellendefinition im `README.md`.

## GitLab-Integration

- Issues eines Projekts werden über die GitLab-API abgerufen. Authentifizierung erfolgt über einen konfigurierbaren Personal Access Token.
- Nach Bestätigung eines Ergebnisses wird dieses über die GitLab-API am Issue hinterlegt.

## Abgabe

Bitte stelle das Projekt als Git-Repository zur Verfügung (bevorzugt auf GitHub/GitLab). Gib eine Anleitung zur Einrichtung und zum Starten der Anwendung im `README.md` an. Dokumentiere außerdem die Schnittstellendefinition so, dass das Frontend ohne Rückfragen integriert werden kann.

## Speicherung des Planning-Poker-Ergebnisses

Das bestätigte Ergebnis wird als scoped GitLab-Label im Format
`planning-poker::<wert>` am Issue gespeichert, zum Beispiel
`planning-poker::8`. Für Sonderkarten werden die textuellen Werte
`planning-poker::question-mark` und `planning-poker::coffee` verwendet.

Diese Lösung wurde gewählt, weil Labels direkt am Issue sichtbar, nach dem
Ergebnis filterbar und für weitere Automatisierungen einfach nutzbar sind.
Durch den gemeinsamen Scope `planning-poker` kann ein Issue nur einen
aktuellen Planning-Poker-Wert besitzen. Änderungen an Labels werden außerdem
in der GitLab-Historie des Issues nachvollziehbar festgehalten.

---

# Einrichtung und Betrieb

## Einrichtung und Start mit Docker

Voraussetzungen:

- aktuelle Docker-Installation mit Docker Compose
- OpenSSL zum Erzeugen eines lokalen JWT-Schlüsselpaares
- ein GitLab Personal Access Token mit Berechtigung, Issues zu lesen und Labels zu ändern

### 1. Lokale Konfiguration anlegen

Zuerst wird die Vorlage für die lokalen Zugangsdaten kopiert:

```bash
cp .env.example .env
```

Danach sind in `.env` die folgenden Werte zu setzen:

```properties
GITLAB_URL=https://gitlab.com
GITLAB_TOKEN=<persönlicher-gitlab-access-token>
JWT_PRIVATE_KEY_FILE=./secrets/jwt-private-key.pem
JWT_PUBLIC_KEY_FILE=./secrets/jwt-public-key.pem
```

Bei einer selbst gehosteten GitLab-Instanz enthält `GITLAB_URL` nur die
Basis-URL der Instanz, beispielsweise `https://gitlab.example.com`. Der
REST-Client ergänzt den API-Pfad selbst.

### 2. JWT-Schlüssel erzeugen

Das Backend signiert Login-Tokens mit dem privaten RSA-Schlüssel und prüft
eingehende Tokens mit dem öffentlichen Schlüssel. Beide Schlüssel werden nur
lokal gespeichert und über Docker in den Container eingebunden:

```bash
mkdir -p secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/jwt-private-key.pem
openssl rsa -pubout -in secrets/jwt-private-key.pem -out secrets/jwt-public-key.pem
chmod 600 secrets/jwt-private-key.pem
```

`secrets/` und `.env` sind in `.gitignore` eingetragen und dürfen nicht in Git
eingecheckt werden.

### 3. Anwendung starten

Die Anwendung wird einschließlich Image-Build gestartet:

```bash
docker compose up --build
```

Das Angular-Frontend ist danach unter `http://localhost:4200` beziehungsweise
`http://<server-ip>:4200` erreichbar. Es läuft in einem eigenen
Nginx-Container.
Nginx leitet `/api/*` einschließlich Server-Sent Events intern an das Backend
weiter. Der Browser kommuniziert somit nur mit einer Origin und benötigt keine
CORS-Konfiguration. Zum Beenden genügt `docker compose down`.

### Schnittstellendokumentation

Swagger UI und die maschinenlesbare OpenAPI-Beschreibung sind innerhalb des
Backend-Containers unter `/q/swagger-ui` beziehungsweise `/q/openapi`
erreichbar.

Bei einem Maven-Build wird zusätzlich
`target/openapi/planning-poker-api.yaml` erzeugt. Sie dokumentiert die
REST-Endpunkte, JWT-Bearer-Authentifizierung und das SSE-Ereignis
`all-developers-estimated`. Dadurch steht die Schnittstellenbeschreibung
nicht erst nach dem Start der Anwendung zur Verfügung. Eine CI/CD-Pipeline
kann sie als Build-Artefakt für Kompatibilitäts- beziehungsweise Contract-Checks
gegen das Backend verwenden. Außerdem lässt sie sich unabhängig von einer
laufenden Instanz bequem an Frontend-Entwickler weitergeben.

### Hinweis zur Speicherung

Benutzer, Sessions und Schätzrunden werden derzeit ausschließlich im Speicher
gehalten. Ein Neustart des Containers löscht diese Daten.

## Offene Aufgaben

Der aktuelle Umsetzungsstand und noch offene Punkte stehen in
[open-tasks.md](open-tasks.md).
