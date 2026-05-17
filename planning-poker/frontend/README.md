# Code Challenge – Planning Poker (Frontend)

## Ziel

Erstelle eine Angular-Anwendung, mit der Scrum-Teams gemeinsam Planning Poker durchführen können. Die Anwendung ermöglicht es jedem Teammitglied, synchron eine Schätzung für ein GitLab-Issue abzugeben. Nach jeder Runde wird das abgestimmte Ergebnis direkt am GitLab-Issue hinterlegt.

## Umsetzungsvarianten

Du kannst zwischen zwei Ansätzen wählen – beide sind gleichwertig und werden unterschiedliche Schwerpunkte in der Bewertung setzen:

### Variante A – Mit eigenem Backend

Die Anwendung kommuniziert mit dem Planning-Poker-Backend (siehe `backend/README.md`). Echtzeit-Synchronisation und GitLab-Zugriff laufen über den eigenen Server.

### Variante B – Direkt über die GitLab API (kein eigenes Backend)

Die Anwendung kommuniziert direkt mit der GitLab API. Ein eigenes Backend entfällt. Wie du die Echtzeit-Synchronisation zwischen den Teilnehmern sowie die GitLab-Integration umsetzt, ist Teil der Aufgabe.

Bitte dokumentiere im `README.md` deines Projekts, welche Variante du gewählt hast und warum.

## Rollen

Die Anwendung kennt zwei Rollen:

### Scrum Master (Moderator)
- Erstellt die Sitzung und erhält automatisch die Scrum-Master-Rolle.
- Hat exklusiven Zugriff auf alle Steuerungsfunktionen (siehe unten).
- Die Rolle ist für alle Teilnehmer sichtbar gekennzeichnet.

### Entwickler (Teilnehmer)
- Tritt der Sitzung über einen generierten Link oder eine Session-ID bei.
- Gibt beim Beitreten einen Namen ein, der während der Sitzung sichtbar bleibt.
- Kann ausschließlich Schätzungen abgeben; hat keinen Zugriff auf Steuerungsfunktionen.

## Aufgabe

Du entwickelst ein Frontend mit Angular, das folgende Kernfunktionalitäten umfasst:

### 1. Sitzung erstellen und beitreten

- Der Scrum Master kann eine neue Planning-Poker-Sitzung anlegen und dabei die GitLab-Projekt-ID sowie das zu schätzende Issue angeben.
- Weitere Teammitglieder treten der Sitzung über einen generierten Link oder eine Session-ID bei.
- Jedes Mitglied gibt beim Beitreten einen Namen ein, der während der Sitzung sichtbar bleibt.

### 2. Issue-Auswahl durch den Scrum Master

- **Nur der Scrum Master** wählt das Issue aus, das als nächstes geschätzt werden soll, und gibt es explizit für die Schätzrunde frei.
- Erst nach der Freigabe durch den Scrum Master sehen alle Teilnehmer das aktive Issue und können mit der Schätzung beginnen.
- Der Scrum Master kann das aktive Issue wechseln, solange die Runde noch nicht abgeschlossen ist.

### 3. Schätzrunde

- Alle Teilnehmer sehen den Titel und die Beschreibung des freigegebenen Issues.
- Jeder Teilnehmer wählt verdeckt eine Schätzkarte (Fibonacci-Folge: 0, 1, 2, 3, 5, 8, 13, 21, 34, ?, ☕).
- Der Abstimmungsfortschritt ist für alle Teilnehmer jederzeit sichtbar:
  - Namentliche Anzeige aller Teilnehmer mit klarem Status: **„Hat geschätzt"** vs. **„Ausstehend"**.
  - Der konkrete Schätzwert bleibt bis zur Auflösung verborgen.
- Sobald alle Teilnehmer eine Karte gewählt haben, wird der Scrum Master aktiv darauf hingewiesen.

### 4. Auflösung durch den Scrum Master

- **Nur der Scrum Master** kann die Schätzrunde auflösen und alle Karten aufdecken – unabhängig davon, ob alle Teilnehmer bereits abgestimmt haben.
- Nach der Auflösung werden alle abgegebenen Werte übersichtlich dargestellt:
  - Karte und Name je Teilnehmer.
  - Gruppierte Anzeige: Pro abgegebenem Schätzwert wird angezeigt, wie viele Teilnehmer diesen Wert gewählt haben.
  - Durchschnitt und häufigster Wert als Orientierungshilfe (nicht als wählbares Ergebnis verwendbar, siehe unten).

### 5. Weitere Schätzrunde oder Ergebnisübernahme

Nach der Auflösung stehen dem Scrum Master zwei Optionen zur Verfügung:

#### Option A – Neue Schätzrunde für dasselbe Issue
- Der Scrum Master startet eine neue Runde für das aktuell angezeigte Issue.
- Alle bisherigen Karten werden zurückgesetzt; die Teilnehmer schätzen erneut.
- Abgeschlossene Runden werden im Sitzungsprotokoll festgehalten (siehe Abschnitt 7).

#### Option B – Ergebnis in GitLab übernehmen
- Der Scrum Master wählt den final vereinbarten Wert aus und bestätigt die Runde.
- Als Ergebnis sind **ausschließlich Werte aus dem definierten Kartensatz wählbar** (0, 1, 2, 3, 5, 8, 13, 21, 34, ?, ☕). Errechnete Werte wie der Durchschnitt können nicht direkt übernommen werden.
- Der gewählte Wert wird am GitLab-Issue hinterlegt. Wie und in welcher Form ist Teil der Aufgabe – bitte dokumentiere deine Entscheidung im `README.md`.

### 6. Echtzeit-Synchronisation

Alle Teilnehmer sehen in Echtzeit:

- Welche Teammitglieder der Sitzung beigetreten sind (inklusive Rollenanzeige: Scrum Master / Entwickler).
- Das aktuell zur Schätzung freigegebene Issue.
- Wer bereits abgestimmt hat und wer noch aussteht – namentlich und mit klarem Status, ohne den Schätzwert zu verraten.
- Die aufgedeckten Karten nach der Auflösung durch den Scrum Master, inklusive der Gruppenauswertung pro Schätzwert.


## Abgabe

Bitte stelle das Projekt als Git-Repository zur Verfügung (bevorzugt auf GitHub/GitLab). Gib eine Anleitung zur Einrichtung und zum Starten der Anwendung im `README.md` an.

## Optional

Du hast Zeit und dein Interesse ist geweckt? Dann kannst du gerne über zusätzliche Aspekte nachdenken, wie z. B.:

- Countdown-Timer je Schätzrunde mit automatischer Auflösung nach Ablauf
- Hervorhebung von Ausreißern (z. B. farbliche Markierung bei großer Streuung)
- Automatische Konsens-Erkennung mit visueller Bestätigung (z. B. Konfetti-Animation)