## Entscheidung: GitLab-Kommunikation über das Backend

Der GitLab-Zugriff erfolgt bewusst nicht direkt aus dem Browser: Ein GitLab Personal Access Token darf nicht im Frontend ausgeliefert oder für Benutzer einsehbar sein. Das Backend verwahrt dieses Geheimnis, erzwingt die Berechtigungen des angemeldeten Planning-Poker-Benutzers und bündelt die GitLab-spezifische Logik – Issue laden sowie das finale Ergebnis als Label speichern – an einer Stelle. Dadurch bleibt das Angular-Frontend auf die Bedienoberfläche und die Planning-Poker-API beschränkt.

## Entscheidung: Speicherung des Planning-Poker-Ergebnisses

Das bestätigte Ergebnis wird als scoped GitLab-Label im Format
`planning-poker::<wert>` am Issue gespeichert, zum Beispiel
`planning-poker::8`. Für Sonderkarten werden die textuellen Werte
`planning-poker::question-mark` und `planning-poker::coffee` verwendet.

Diese Lösung wurde gewählt, weil Labels direkt am Issue sichtbar, nach dem
Ergebnis filterbar und für weitere Automatisierungen einfach nutzbar sind.
Durch den gemeinsamen Scope `planning-poker` kann ein Issue nur einen
aktuellen Planning-Poker-Wert besitzen. Änderungen an Labels werden außerdem
in der GitLab-Historie des Issues nachvollziehbar festgehalten.

## Einrichtung und Start mit Docker

Voraussetzungen:

- aktuelle Docker-Installation mit Docker Compose
- OpenSSL zum Erzeugen eines lokalen JWT-Schlüsselpaares
- ein GitLab Personal Access Token mit Berechtigung, Issues zu lesen und Labels zu ändern

### 1. Lokale Konfiguration anlegen

Zuerst in das Backend-Verzeichnis wechseln und die Vorlage für die lokalen
Zugangsdaten kopieren:

```bash
cd planning-poker/backend
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
lokal gespeichert und über Docker in den Container eingebunden. Die folgenden
Befehle werden im Verzeichnis `planning-poker/backend/` ausgeführt:

```bash
mkdir -p secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/jwt-private-key.pem
openssl rsa -pubout -in secrets/jwt-private-key.pem -out secrets/jwt-public-key.pem
chmod 600 secrets/jwt-private-key.pem
```

`secrets/` und `.env` sind in `.gitignore` eingetragen und dürfen nicht in Git
eingecheckt werden.

### 3. Anwendung starten

Die vollständige Anwendung wird einschließlich Image-Build aus dem Verzeichnis
`coding-challenge/` gestartet:

```bash
docker compose up --build
```

Für die getrennte Entwicklung können Backend und Frontend auch jeweils mit
deren eigener Compose-Datei gestartet werden:

```bash
cd planning-poker/backend
docker compose up --build
```

```bash
cd planning-poker/frontend
docker compose up --build
```

Das Angular-Frontend ist danach unter `http://localhost:4200` beziehungsweise
`http://<server-ip>:4200` erreichbar und läuft in einem eigenen Nginx-Container.
Nginx leitet `/api/*` einschließlich Server-Sent Events intern an das Backend
weiter. Der Browser kommuniziert somit nur mit einer Origin, benötigt keine
CORS-Konfiguration und kennt keine interne Backend-Adresse. Zum Beenden genügt
`docker compose down`.

### Schnittstellendokumentation

Swagger UI und die maschinenlesbare OpenAPI-Beschreibung sind über das
Frontend unter `/api/q/swagger-ui/` beziehungsweise `/api/q/openapi`
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
[frontend/open-tasks.md](planning-poker/frontend/open-tasks.md) und
[backend/open-tasks.md](planning-poker/backend/open-tasks.md).
