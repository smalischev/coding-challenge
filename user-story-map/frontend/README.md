# Code Challenge (Frontend)

## Ziel

Erstelle eine Angular-Anwendung zur Verwaltung von User Stories im Kontext einer User Story Map mit lokalem Persistenzspeicher. Die Anwendung soll es ermöglichen, User Journeys und User Steps zu verwalten, GitLab Issues (als Mockdaten) zuzuordnen sowie eine fachliche Releaseplanung vorzunehmen.

## Aufgabe

Du entwickelst ein Frontend mit Angular, das folgende Kernfunktionalitäten umfasst:

### 1. User Story Map erstellen

- Lege User Journeys als oberste Ebene der Map an (z. B. "Onboarding", "Projekt erstellen").
- Jeder User Journey können mehrere User Steps zugeordnet werden (z. B. "Registrieren", "E-Mail bestätigen").
- Visualisiere diese Struktur als horizontale Spalten für die Journeys, mit darunter angeordneten Steps.

### 2. Mock-GitLab-Issues verwalten

- Erstelle eine Liste von mindestens 10 Mock-GitLab Issues (Titel, Beschreibung, ID).
- Die Issues sollen initial in einer eigenen Spalte "Nicht zugeordnet" erscheinen.
- Per Drag & Drop sollen Issues auf einzelne User Steps gezogen und dadurch zugeordnet werden können.
- Die Zuordnung soll persistent in einer IndexedDB gespeichert werden.

### 3. Release-Planung (Erweiterung)

- Ermögliche das Anlegen von fachlichen Releases (z. B. „Release Q3 2025“).
- Für jedes Release sollen Issues aus allen User Steps einem Release zugeordnet werden können.
- Die Zuordnung und Informationen zu Releases müssen ebenfalls in IndexedDB gespeichert werden.
- Eine tabellarische Ansicht aller Releases mit zugeordneten Issues soll verfügbar sein.

## Abgabe

Bitte stelle das Projekt als Git-Repository zur Verfügung (bevorzugt auf GitHub/GitLab). Gebe eine Anleitung zur Einrichtung und zum Starten der Anwendung im `README.md` an.

## Optional

Du hast Zeit und dein Interesse ist geweckt? Dann kannst du gerne über zusätzliche Askepte nachdenken, wie z.B.:

- Undo-Funktion beim Entfernen oder Verschieben von Issues
- Dark-Mode oder Theme-Switcher
- Integration von Unit- oder Component-Tests
- Import/Export der User Story Map als JSON-Datei
