![Workflow](https://github.com/pericjul/praesto/actions/workflows/ci-badge.yml/badge.svg)
![Coverage](https://github.com/pericjul/praesto/blob/main/.github/badges/jacoco.svg)
![Branches](https://github.com/pericjul/praesto/blob/main/.github/badges/branches.svg)

# Praesto
Praesto ist eine KI-gestützte Webplattform, die Schüler:innen im Bewerbungsprozess unterstützt. Lehrkräfte können Aufgaben erstellen (z. B. „20 Minuten Bewerbungstraining") und die KI simuliert realistische Bewerbungsgespräche. Die Schüler:innen erhalten dabei individuelles Feedback und sammeln Badges für ihre Fortschritte.

# Inhaltsverzeichnis
- [Einleitung](#einleitung)
    - [Explore-Board](#explore-board)
    - [Create-Board](#create-board)
    - [Evaluate-Board](#evaluate-board)
    - [Diskussion Feedback Pitch](#diskussion-feedback-pitch)
- [Anforderungen](#anforderungen)
    - [Use-Case Diagramm](#use-case-diagramm)
    - [Use-Case Beschreibung](#use-case-beschreibung)
    - [Fachliches Datenmodell](#fachliches-datenmodell)
    - [UI-Mockup](#ui-mockup)
- [Implementation](#implementation)
    - [Frontend](#frontend)
    - [KI-Funktionen](#ki-funktionen)
    - [Drittsysteme](#drittsysteme)
    - [Optionale Anforderungen](#optionale-anforderungen)
- [Fazit](#fazit)
    - [Stand der Implementation](#stand-der-implementation)

---

# Einleitung

Praesto (lat. "ich stehe bereit") hilft Jugendlichen, die eine Lehrstelle suchen, sich gezielt und spielerisch auf den Bewerbungsprozess vorzubereiten. Die Plattform kombiniert künstliche Intelligenz mit schulischem Lernen: Schüler:innen trainieren Bewerbungsgespräche über einen Chat, Lehrkräfte verwalten Aufgaben und sehen die Antworten ein. Durch Badges und Fortschrittssysteme werden Motivation und Lernbereitschaft gefördert.

---

## Explore-Board

### TRENDS & TECHNOLOGIE
- Zunehmender Einsatz von künstlicher Intelligenz im Bildungsbereich
- Digitalisierung des Unterrichts und Online-Lernplattformen
- Bedeutung von Soft Skills und persönlicher Präsentation im Bewerbungsprozess
- Realitätsnahe Simulationen und spielerische Lernmethoden (Gamification)

### POTENTIELLE PARTNER & WETTBEWERB

**Partnerschaften:**
- Schulen, Berufsinformationszentren, Bildungsdirektionen, Lehrbetriebe

**Wettbewerb:**
- Online-Ratgeber und Bewerbungsvorlagen (statisch)
- YouTube-Tutorials und allgemeine Karriereportale
- Kein interaktives, schulintegriertes KI-Tool für Bewerbungstraining

### FAKTEN
- Viele Schüler:innen fühlen sich unvorbereitet auf Bewerbungsgespräche
- Bewerbungstrainings werden in Schulen kaum noch priorisiert
- Eigene Beobachtung: Jugendliche sind oft unsicher im Umgang mit typischen Interviewfragen

### USER
- Schüler:innen zwischen 14 und 17 Jahren auf Lehrstellensuche
- Lehrkräfte im Berufswahlunterricht

### POTENZIALFELDER
- Interaktive Gesprächssimulation mit KI
- Aufgabenverwaltung durch Lehrkräfte
- Gamification für Motivation und Lernfreude

### ERKENNTNISSE
- Schüler:innen haben Angst vor Bewerbungsgesprächen
- Lehrkräfte wünschen sich digitale Unterstützung im Berufswahlunterricht
- KI kann individuelle Lernbedürfnisse besser berücksichtigen

### BEDÜRFNISSE
- Realistische Vorbereitung auf Bewerbungsgespräche
- Einfaches, direktes Feedback
- Struktur und Motivation im Lernprozess

### TOUCHPOINTS
- PC oder Laptop in der Schule
- Smartphone oder Tablet zuhause

### WIE KÖNNEN WIR?
Wie können wir Schüler:innen, die eine Lehrstelle suchen, digital und individuell auf Bewerbungsgespräche vorbereiten und Lehrkräften ein einfaches Tool zur Verwaltung dieser Trainings bieten?

---

## Create-Board

### IDEEN-BESCHREIBUNG
Praesto ist eine Webplattform, auf der Schüler:innen mit einer KI Bewerbungsgespräche üben. Lehrkräfte erstellen Aufgaben, sehen die Antworten ihrer Schüler:innen und erhalten Auswertungen über Fortschritte und Leistungen.

### ADRESSIERTE NUTZER
- Schüler:innen auf Lehrstellensuche
- Lehrkräfte, die Berufswahlunterricht erteilen

### ADRESSIERTE BEDÜRFNISSE
- Schüler:innen: Sicherheit und Übung durch Feedback
- Lehrkräfte: Strukturierte Kontrolle und Zeitersparnis

### PROBLEME
- Unsicherheit und Nervosität vor Bewerbungsgesprächen
- Fehlendes Training im Schulalltag
- Fehlende Motivation, sich mit Bewerbungsthemen zu beschäftigen

### IDEENPOTENZIAL

| Kriterium | Bewertung |
|-----------|-----------|
| **Mehrwert** | 🔵🔵🔵🔵🔵🔵🔵🔵⚪️⚪️ |
| **Übertragbarkeit** | 🔵🔵🔵🔵🔵🔵⚪️⚪️⚪️⚪️ |
| **Machbarkeit** | 🔵🔵🔵🔵🔵🔵🔵⚪️⚪️⚪️ |

### DAS WOW
Die KI reagiert individuell auf jede Antwort der Schüler:innen, stellt Rückfragen und gibt sofort Feedback, wie in einem echten Bewerbungsgespräch.

### HIGH-LEVEL-KONZEPT
> Praesto ist wie Duolingo – aber für Bewerbungsgespräche.

### WERTVERSPRECHEN
Praesto stärkt das Selbstvertrauen der Schüler:innen und unterstützt Lehrkräfte bei der Durchführung moderner, digitaler Bewerbungstrainings.

---

## Evaluate-Board

### KANÄLE
- Schulen und Berufsinformationszentren
- Präsentationen an Bildungsmessen
- Social Media (Instagram, TikTok)
- Empfehlungen durch Lehrkräfte und Schulnetzwerke

### UNFAIRER VORTEIL
- Kombination aus KI-Interviewtraining und Lehrer-Feedback-System
- Echtzeit-Auswertung der Schüler:innen-Antworten
- Motivation durch Badges und Fortschrittssysteme

### KPI
- Anzahl durchgeführter Bewerbungstrainings
- Zufriedenheit der Schüler:innen (Selbsteinschätzung)
- Zeitersparnis für Lehrkräfte
- Aktive Nutzer:innen pro Schule

### EINNAHMEQUELLEN
- Lizenzmodell für Schulen (jährliche Nutzung)
- Potenzielle Förderung durch Bildungsstiftungen oder öffentliche Initiativen

---

## Diskussion Feedback Pitch

Während des Pitches wurde viel positives Feedback gegeben. Die häufigsten Fragen und Anregungen betrafen folgende Themen:

### Erweiterung der Zielgruppe
Mehrfach wurde gefragt, ob Praesto auch für **Studierende, Berufseinsteiger:innen, Arbeitsuchende oder sogar RAV/Arbeitslosenkassen** angeboten werden könnte. Dies ist eine valide Erweiterungsmöglichkeit für zukünftige Versionen. Der aktuelle Fokus liegt bewusst auf Schüler:innen, da diese eine klar definierte Zielgruppe mit spezifischen Bedürfnissen darstellen und der Schulkontext eine einfache Verbreitung über Lehrkräfte ermöglicht. Eine Erweiterung auf weitere Bildungsebenen (Hochschulen) oder Institutionen (RAV) wäre technisch einfach umsetzbar und ist als zukünftige Ausbaustufe denkbar.

### Voice-Chat und Video-Upload
Die Ideen für **Voice-Chat** und **Video-Feedback zum Auftreten** wurden mehrfach genannt. Für den MVP wurde bewusst auf Text-Chat gesetzt, da dies technisch einfacher und für den Schulkontext geeigneter ist (kein Mikrofon nötig, leiser Unterricht möglich). Voice-Chat könnte in einer späteren Version via Speech-to-Text integriert werden. Video-Upload mit Feedback zum Auftreten wäre eine interessante Premium-Funktion.

### Barrierefreiheit (Leseschwäche)
Die Frage nach Schüler:innen mit Leseschwäche ist wichtig. Hier könnte **Text-to-Speech** integriert werden, sodass die KI-Antworten vorgelesen werden. Dies ist für eine zukünftige Version geplant und würde die Inklusion verbessern.

### Monetarisierung und Finanzierung
Mehrere Fragen betrafen das Geschäftsmodell. Das **Lizenzmodell für Schulen** wurde im Evaluate-Board bereits skizziert. Für Schulen mit knappen Budgets könnten **Bildungsstiftungen oder kantonale Förderungen** in Betracht gezogen werden. Die Vermarktung erfolgt primär über direkte Ansprache von Schulen, Präsentationen an Bildungsmessen und Empfehlungen im Schulnetzwerk.

### Gamification (Badges)
Der Vorschlag, ein Belohnungssystem wie bei Duolingo einzubauen, wurde **bereits umgesetzt**! Praesto verfügt über ein Badge-System, das Schüler:innen für verschiedene Aktivitäten belohnt (erste Session, mehrere Bewerbungen erstellt, Feedback erhalten, etc.).

### Firmen- und rollenspezifisches Training
Die Idee, dass Praesto auf **spezifische Unternehmen oder Berufsrollen** (z.B. Business Analyst, Support) vorbereitet, ist spannend. Aktuell ist das KI-Training allgemein gehalten. Durch die flexible Prompt-Gestaltung könnte dies in Zukunft erweitert werden – etwa durch Integration von Firmeninformationen oder branchenspezifischen Fragen.

### Fachwörter-Lexikon
Der Vorschlag, ein **Lexikon mit Fachwörtern** zu generieren, ist interessant für die Erweiterung des Lernmaterials und könnte als zusätzliche Ressource für Schüler:innen dienen.

---

# Anforderungen

## Use-Case Diagramm

![Use-Case Diagramm](doc/uc-diagram.drawio.svg)

---

## Use-Case Beschreibung

### UC-01: Bewerbungsgespräch üben

| Feld | Beschreibung |
|------|--------------|
| **Name** | Bewerbungsgespräch üben |
| **Akteur** | Schüler:in |
| **Auslöser** | Schüler:in möchte ein Bewerbungsgespräch trainieren |
| **Vorbedingung** | Schüler:in ist eingeloggt und einer Klasse zugeordnet |
| **Nachbedingung Erfolg** | Session ist gespeichert und kann eingesehen oder als Aufgabe abgegeben werden |
| **Nachbedingung Fehlschlag** | Fehlermeldung wird angezeigt, keine Session erstellt |
| **Standardablauf** | 1. Schüler:in klickt auf "Neue Session starten"<br>2. System erstellt neue Session und zeigt Chat-Ansicht<br>3. KI begrüsst Schüler:in und stellt erste Frage<br>4. Schüler:in gibt Antwort ein<br>5. KI analysiert Antwort und stellt Rückfrage oder gibt Feedback<br>6. Schritte 4-5 wiederholen sich<br>7. Schüler:in klickt auf "Session beenden"<br>8. System speichert Session mit Status CLOSED |
| **Alternativablauf** | 7a. Schüler:in klickt auf "Als Aufgabe abgeben"<br>7b. System erstellt Submission und verknüpft mit Assignment |

### UC-02: Aufgabe erstellen

| Feld | Beschreibung |
|------|--------------|
| **Name** | Aufgabe erstellen |
| **Akteur** | Lehrperson |
| **Auslöser** | Lehrperson möchte eine neue Übungsaufgabe für die Klasse erstellen |
| **Vorbedingung** | Lehrperson ist eingeloggt und hat mindestens eine Klasse |
| **Nachbedingung Erfolg** | Aufgabe ist erstellt und für Schüler:innen der Klasse sichtbar |
| **Nachbedingung Fehlschlag** | Fehlermeldung wird angezeigt, keine Aufgabe erstellt |
| **Standardablauf** | 1. Lehrperson navigiert zu "Aufgaben"<br>2. Lehrperson klickt auf "Neue Aufgabe"<br>3. System zeigt Formular an<br>4. Lehrperson wählt Klasse aus<br>5. Lehrperson wählt Aufgabentyp (AI-Interview, Selbstreflexion, etc.)<br>6. Lehrperson gibt Titel und Beschreibung ein<br>7. Lehrperson setzt Abgabefrist (optional)<br>8. Lehrperson klickt auf "Speichern"<br>9. System erstellt Aufgabe mit Status ASSIGNED |

### UC-03: Abgabe bewerten

| Feld | Beschreibung |
|------|--------------|
| **Name** | Abgabe bewerten |
| **Akteur** | Lehrperson |
| **Auslöser** | Schüler:in hat eine Aufgabe abgegeben |
| **Vorbedingung** | Abgabe existiert mit Status SUBMITTED |
| **Nachbedingung Erfolg** | Feedback und optional Note sind gespeichert, Status ist REVIEWED |
| **Nachbedingung Fehlschlag** | Fehlermeldung, Abgabe bleibt unverändert |
| **Standardablauf** | 1. Lehrperson öffnet Abgaben-Übersicht<br>2. Lehrperson wählt eine Abgabe aus<br>3. System zeigt Inhalt (z.B. Chat-Verlauf bei AI-Interview)<br>4. Lehrperson liest und bewertet den Inhalt<br>5. Lehrperson gibt schriftliches Feedback ein<br>6. Lehrperson vergibt Note (1-6, optional)<br>7. Lehrperson klickt auf "Feedback speichern"<br>8. System speichert und setzt Status auf REVIEWED |

### UC-04: Bewerbung verwalten

| Feld | Beschreibung |
|------|--------------|
| **Name** | Bewerbung verwalten |
| **Akteur** | Schüler:in |
| **Auslöser** | Schüler:in möchte Bewerbungen tracken |
| **Vorbedingung** | Schüler:in ist eingeloggt |
| **Nachbedingung Erfolg** | Bewerbung ist erstellt oder aktualisiert |
| **Nachbedingung Fehlschlag** | Fehlermeldung wird angezeigt |
| **Standardablauf** | 1. Schüler:in öffnet Bewerbungs-Tracker<br>2. Schüler:in klickt auf "Neue Bewerbung"<br>3. System zeigt Formular an<br>4. Schüler:in gibt Firmenname, Position und Status ein<br>5. Schüler:in klickt auf "Speichern"<br>6. System erstellt Bewerbung |
| **Alternativablauf** | 2a. Schüler:in klickt auf bestehende Bewerbung<br>2b. Schüler:in ändert Status (z.B. APPLIED → INVITED)<br>2c. System aktualisiert Bewerbung |

### UC-05: Klasse verwalten

| Feld | Beschreibung |
|------|--------------|
| **Name** | Klasse verwalten |
| **Akteur** | Lehrperson |
| **Auslöser** | Lehrperson möchte Schüler:innen zu einer Klasse hinzufügen |
| **Vorbedingung** | Lehrperson ist eingeloggt |
| **Nachbedingung Erfolg** | Klasse ist erstellt/aktualisiert mit Schüler:innen |
| **Standardablauf** | 1. Lehrperson navigiert zu "Klassen"<br>2. Lehrperson erstellt neue Klasse oder wählt bestehende<br>3. Lehrperson fügt Schüler:innen via E-Mail hinzu<br>4. System speichert Klassenzuordnung |

### UC-06: Notizen erstellen

| Feld | Beschreibung |
|------|--------------|
| **Name** | Notizen erstellen |
| **Akteur** | Schüler:in |
| **Auslöser** | Schüler:in möchte Informationen zu einer Firma festhalten |
| **Vorbedingung** | Schüler:in ist eingeloggt |
| **Nachbedingung Erfolg** | Notiz ist gespeichert und kann später eingesehen werden |
| **Nachbedingung Fehlschlag** | Fehlermeldung wird angezeigt, keine Notiz erstellt |
| **Standardablauf** | 1. Schüler:in navigiert zu "Notizen"<br>2. Schüler:in klickt auf "Neue Notiz"<br>3. Schüler:in gibt Firmenname, Position und Notiztext ein<br>4. Schüler:in klickt auf "Speichern"<br>5. System erstellt Notiz mit Zeitstempel |

### UC-07: Badges ansehen

| Feld | Beschreibung |
|------|--------------|
| **Name** | Badges ansehen |
| **Akteur** | Schüler:in |
| **Auslöser** | Schüler:in möchte Fortschritt und verdiente Auszeichnungen einsehen |
| **Vorbedingung** | Schüler:in ist eingeloggt |
| **Nachbedingung Erfolg** | Badge-Übersicht wird angezeigt |
| **Standardablauf** | 1. Schüler:in navigiert zu "Badges"<br>2. System zeigt alle verfügbaren Badges<br>3. Bereits verdiente Badges werden hervorgehoben<br>4. Fortschritt zu nächsten Badges wird angezeigt |

### UC-08: Meine Klasse ansehen

| Feld | Beschreibung |
|------|--------------|
| **Name** | Meine Klasse ansehen |
| **Akteur** | Schüler:in |
| **Auslöser** | Schüler:in möchte sehen, welcher Klasse sie/er zugeordnet ist |
| **Vorbedingung** | Schüler:in ist eingeloggt und einer Klasse zugeordnet |
| **Nachbedingung Erfolg** | Klasseninformationen werden angezeigt |
| **Standardablauf** | 1. Schüler:in navigiert zu "Meine Klasse"<br>2. System zeigt Klassenname und zuständige Lehrperson<br>3. Offene Aufgaben der Klasse werden angezeigt |

---

## Fachliches Datenmodell

![ER-Diagramm](doc/ER-diagram.drawio.svg)

### Entitäten und Beziehungen

| Entität | Beschreibung | Beziehungen |
|---------|--------------|-------------|
| **User** | Ein Benutzer (Auth0 extern) | Hat Rolle (Student/Teacher), verwaltet Klassen oder gehört zu Klasse |
| **SchoolClass** | Eine Schulklasse mit Namen | Hat eine Lehrperson (teacherId), enthält mehrere Schüler:innen (studentEmails) |
| **Assignment** | Eine Übungsaufgabe mit Typ und Frist | Gehört zu einer Klasse, hat mehrere Abgaben |
| **Submission** | Die Einreichung einer Aufgabe | Gehört zu einer Aufgabe, stammt von Schüler:in, kann Session referenzieren |
| **Session** | Ein KI-Chat-Gespräch | Gehört zu Schüler:in, kann zu Aufgabe gehören, enthält Nachrichten (embedded als Liste) |
| **Application** | Eine erfasste Stellenbewerbung | Gehört zu Schüler:in |
| **Note** | Eine Notiz zu einer Firma | Gehört zu Schüler:in |
| **Badge** | Eine Auszeichnung (Definition) | Hat Regeln für automatische Vergabe |
| **UserBadge** | Zuordnung Badge zu Schüler:in | Verknüpft Badge mit Schüler:in, speichert Zeitpunkt |

> **Hinweis:** Die Nachrichten (Messages) werden nicht als separate Entität gespeichert, sondern sind als embedded Liste direkt im Session-Dokument enthalten.

### Zustandsdiagramme

#### Session-Status

```
┌────────┐                    ┌────────┐
│  OPEN  │───── beenden ─────>│ CLOSED │
└────────┘                    └────────┘
```

| Status | Beschreibung |
|--------|--------------|
| **OPEN** | Session ist aktiv, Nachrichten können gesendet werden |
| **CLOSED** | Session ist beendet, keine weiteren Nachrichten möglich |

#### Aufgaben-Status

```
┌──────────┐      ┌─────────────┐      ┌───────────┐      ┌──────────┐      ┌────────┐
│ ASSIGNED │─────>│ IN_PROGRESS │─────>│ SUBMITTED │─────>│ REVIEWED │─────>│ CLOSED │
└──────────┘      └─────────────┘      └───────────┘      └──────────┘      └────────┘
   erstellt        Arbeit begonnen      abgegeben         bewertet          beendet
```

| Status | Beschreibung |
|--------|--------------|
| **ASSIGNED** | Aufgabe wurde erstellt und der Klasse zugewiesen |
| **IN_PROGRESS** | Schüler:in hat mit der Bearbeitung begonnen |
| **SUBMITTED** | Schüler:in hat die Aufgabe abgegeben |
| **REVIEWED** | Lehrperson hat Feedback gegeben |
| **CLOSED** | Aufgabe ist abgeschlossen (Frist abgelaufen oder manuell geschlossen) |

#### Abgabe-Status

```
┌───────────┐                    ┌──────────┐
│ SUBMITTED │─── Feedback ──────>│ REVIEWED │
└───────────┘                    └──────────┘
```

| Status | Beschreibung |
|--------|--------------|
| **SUBMITTED** | Abgabe wurde eingereicht, wartet auf Bewertung |
| **REVIEWED** | Lehrperson hat die Abgabe bewertet |

#### Bewerbungs-Status

```
┌─────────┐     ┌─────────┐     ┌─────────┐     ┌────────────────┐
│ PLANNED │────>│ APPLIED │────>│ INVITED │────>│ INTERVIEW_DONE │
└─────────┘     └─────────┘     └─────────┘     └────────────────┘
  geplant       beworben        eingeladen       Gespräch geführt
                                                        │
                          ┌─────────────────────────────┼─────────────────────────────┐
                          ▼                             ▼                             ▼
                   ┌──────────┐                  ┌──────────┐                  ┌───────────┐
                   │ ACCEPTED │                  │ REJECTED │                  │ WITHDRAWN │
                   └──────────┘                  └──────────┘                  └───────────┘
                     Zusage                        Absage                      zurückgezogen
```

| Status | Beschreibung |
|--------|--------------|
| **PLANNED** | Bewerbung ist geplant, noch nicht abgeschickt |
| **APPLIED** | Bewerbung wurde abgeschickt |
| **INVITED** | Einladung zum Vorstellungsgespräch erhalten |
| **INTERVIEW_DONE** | Vorstellungsgespräch wurde geführt |
| **ACCEPTED** | Zusage erhalten |
| **REJECTED** | Absage erhalten |
| **WITHDRAWN** | Bewerbung zurückgezogen |

---

## UI-Mockup

Die folgenden Wireframes wurden in der Konzeptionsphase erstellt, um die grundlegende Struktur und Navigation der Anwendung zu definieren.

### Student Dashboard
![Mockup Student Dashboard](doc/mockup-student-dashboard.png)

### Chat-Ansicht
![Mockup Chat](doc/mockup-chat.png)

### Teacher Dashboard
![Mockup Teacher Dashboard](doc/mockup-teacher-dashboard.png)

---

# Implementation

## Frontend

Das Frontend wurde mit **SvelteKit** und **TailwindCSS** entwickelt. Es bietet eine moderne, responsive Benutzeroberfläche für beide Benutzerrollen.

### Student-Ansichten

#### Dashboard
![Student Dashboard](doc/screenshot-student-dashboard.png)

Das Dashboard zeigt:
- Übersicht offener Aufgaben mit Abgabefristen
- Letzte Session mit Möglichkeit zum Fortsetzen
- Statistiken zu Bewerbungen und verdienten Badges
- Benachrichtigungen über neues Feedback

#### Aufgaben-Übersicht
![Aufgaben Übersicht](doc/screenshot-student-aufgaben.png)

Die Aufgaben-Übersicht zeigt:
- Liste aller zugewiesenen Aufgaben der Klasse
- Filterung nach Status (offen, abgegeben, bewertet)
- Abgabefrist mit visueller Warnung bei bald fälligen Aufgaben
- Direkter Link zur Aufgaben-Detailseite

#### Aufgaben-Detail
![Aufgaben Detail](doc/screenshot-student-aufgabe-detail.png)

Die Aufgaben-Detailseite bietet:
- Vollständige Aufgabenbeschreibung und Anweisungen
- Anzeige des Aufgabentyps (AI-Interview, Selbstreflexion, etc.)
- Button zum Starten einer neuen Session
- Übersicht bereits abgegebener Versuche mit Feedback

#### Sessions-Übersicht
![Sessions Übersicht](doc/screenshot-student-sessions.png)

Die Sessions-Übersicht zeigt:
- Liste aller bisherigen Chat-Sessions
- Status pro Session (aktiv, beendet, abgegeben)
- Datum, Dauer und Anzahl Nachrichten
- Möglichkeit zum Fortsetzen aktiver Sessions

#### Chat-Ansicht
![Chat](doc/screenshot-chat.png)

Die Chat-Ansicht bietet:
- Interaktiver Chat mit der KI
- Anzeige der verknüpften Aufgabe (falls vorhanden)
- Timer für die Gesprächsdauer
- Buttons zum Beenden oder Abgeben der Session

#### Bewerbungs-Tracker
![Bewerbungen](doc/screenshot-bewerbungen.png)

Der Tracker zeigt:
- Kanban-Board mit Bewerbungen nach Status gruppiert
- Statistiken (Anzahl pro Status)
- Formular zum Erstellen neuer Bewerbungen
- Detail-Ansicht mit Notizen

#### Notizen
![Notizen](doc/screenshot-notizen.png)

Die Notizen-Ansicht bietet:
- Liste aller erstellten Notizen zu Firmen
- Suchfunktion nach Firmenname oder Inhalt
- Formular zum Erstellen neuer Notizen
- Bearbeiten und Löschen bestehender Notizen

#### Badge-Übersicht
![Badges](doc/screenshot-badges.png)

Die Badge-Ansicht zeigt:
- Alle verfügbaren Badges mit Beschreibung
- Bereits verdiente Badges hervorgehoben
- Fortschrittsanzeige zu nächsten Badges
- Datum der Freischaltung bei verdienten Badges


### Teacher-Ansichten

#### Klassen-Übersicht
![Klassen](doc/screenshot-klassen.png)

Die Klassenverwaltung bietet:
- Liste aller Klassen der Lehrperson
- Anzahl Schüler:innen pro Klasse
- Möglichkeit zum Hinzufügen/Entfernen von Schüler:innen

#### Aufgaben-Verwaltung
![Aufgaben](doc/screenshot-aufgaben.png)

Die Aufgabenverwaltung zeigt:
- Alle erstellten Aufgaben mit Status
- Abgabe-Quote pro Aufgabe
- Formular zum Erstellen neuer Aufgaben (5 Typen)

#### Abgaben-Bewertung
![Bewertung](doc/screenshot-bewertung.png)

Die Bewertungsansicht bietet:
- Chat-Verlauf der Session (bei AI-Interview)
- Eingabefeld für schriftliches Feedback
- Notenfeld (1-6)
- Übersicht aller Abgaben einer Aufgabe

---

## KI-Funktionen

Praesto verwendet **Spring AI** mit dem **OpenAI GPT-4** Modell für die Simulation von Bewerbungsgesprächen.

### Aufgaben der KI

| Funktion | Beschreibung |
|----------|--------------|
| **Gesprächssimulation** | Die KI übernimmt die Rolle eines Personalverantwortlichen und führt ein realistisches Bewerbungsgespräch |
| **Kontextbewusstsein** | Die KI merkt sich den gesamten Gesprächsverlauf und stellt passende Rückfragen |
| **Individuelles Feedback** | Die KI reagiert auf die Qualität der Antworten und gibt konstruktive Hinweise |
| **Altersgerechte Kommunikation** | Die KI passt Sprache und Komplexität dem Alter der Schüler:innen an |
| **Strukturierte Gesprächsführung** | Die KI führt durch typische Phasen eines Bewerbungsgesprächs |

### System-Prompt

Die KI erhält einen sorgfältig gestalteten System-Prompt mit folgenden Anweisungen:
- Freundlich aber professionell auftreten
- Typische Bewerbungsfragen stellen (Motivation, Stärken/Schwächen, Situationsfragen)
- Auf Antworten eingehen und gezielt nachhaken
- Konstruktives Feedback in die Fragen einbauen
- Das Gespräch nach 10-15 Minuten zu einem natürlichen Ende führen
- Bei zu kurzen oder ausweichenden Antworten nachfragen

### Beispiel-Interaktion

```
KI:   Guten Tag! Ich freue mich, Sie kennenzulernen. Mein Name ist Herr Müller 
      und ich bin der Personalverantwortliche bei der Beispiel AG. 
      Erzählen Sie mir doch ein wenig über sich selbst.

User: Ich heisse Max und bin 15 Jahre alt. Ich besuche die 3. Sek und 
      interessiere mich sehr für Computer.

KI:   Das klingt interessant, Max! Sie sagen, Sie interessieren sich für 
      Computer. Können Sie mir erzählen, was genau Sie an Computern fasziniert? 
      Haben Sie vielleicht schon eigene Projekte umgesetzt oder etwas 
      Bestimmtes ausprobiert?
```

---

## Drittsysteme

Praesto integriert folgende externe Systeme:

| System | Verwendung | Beschreibung |
|--------|------------|--------------|
| **Auth0** | Authentifizierung | OAuth 2.0 / OpenID Connect für sicheres Login, Rollenverwaltung (Student/Teacher) |
| **MongoDB Atlas** | Datenbank | Cloud-Datenbank für alle Entitäten, gehostet auf Azure |
| **OpenAI API** | KI-Modell | GPT-4 für die Gesprächssimulation via Spring AI |
| **Azure App Service** | Hosting | Deployment der Anwendung als Docker Container |
| **GitHub Actions** | CI/CD | Automatisches Testing und Deployment bei jedem Push |

---

## Optionale Anforderungen

Folgende optionale Anforderungen wurden umgesetzt:

| Anforderung | Beschreibung | Status |
|-------------|--------------|--------|
| **Komplexes Datenmodell** | 9 Entitäten mit Beziehungen (statt Minimum 3) | ✅ |
| **Komplexe Benutzerverwaltung** | Zwei Rollen (Student/Teacher) mit unterschiedlichen Berechtigungen via Auth0 | ✅ |
| **Gamification** | Badge-System mit 7 verschiedenen Badge-Typen und automatischer Vergabe | ✅ |
| **Dashboard mit Statistiken** | Übersicht für Schüler:innen mit Fortschritt, offenen Aufgaben, Bewerbungsstatistiken | ✅ |
| **SonarCloud Integration** | Automatische Code-Qualitätsanalyse mit Coverage-Report | ✅ |
| **Docker Multi-Platform Build** | Container-Build für amd64 und arm64 Architekturen | ✅ |
| **Umfangreiche Testabdeckung** | Über 500 Tests mit JUnit, Mockito, parametrisierten Tests | ✅ |

---

# Fazit

## Stand der Implementation

### Umgesetzte Features

✅ **Vollständig implementiert:**
- KI-gestütztes Bewerbungstraining mit Chat-Interface
- Klassenverwaltung für Lehrpersonen
- Aufgabenerstellung mit 5 verschiedenen Typen:
  - AI-Interview (KI-Gespräch)
  - Selbstreflexion (Textabgabe)
  - Recherche (Textabgabe)
  - Dokument-Upload
  - Video-Pitch (URL-Abgabe)
- Abgabe- und Bewertungssystem mit Feedback und Noten
- Bewerbungs-Tracker mit Kanban-Board und Statusverwaltung
- Notizen-System für Firmeninformationen
- Badge-System mit automatischer Vergabe basierend auf Aktivitäten
- Vollständige Auth0-Integration mit Rollentrennung
- Azure Deployment mit Docker und GitHub Actions
- Testabdeckung von über 85%

### Nächste Schritte (Backlog)

Die folgenden Features sind im **Backlog** für zukünftige Versionen:

| Feature | Priorität | Beschreibung |
|---------|-------|-----------|--------------|
| Voice-Chat | Speech-to-Text für mündliche Antworten |
| Text-to-Speech | Vorlesen der KI-Antworten für Barrierefreiheit |
| Export-Funktion | PDF-Export von Bewerbungen und Sessions |
| Mail-Mitteilungen | Erweiterung von Mitteilungen mit Mailfunktion |



### Bekannte Einschränkungen

- Kein Offline-Modus verfügbar (Internetverbindung erforderlich)
- KI-Antworten können bei hoher OpenAI-Last langsamer sein
- Aktuell nur Deutsch als Sprache unterstützt

---

## Technische Dokumentation

| Ressource | Link |
|-----------|------|
| **Postman Collection** | *https://documenter.getpostman.com/view/48903716/2sB3dTs7qG* |
| **SonarCloud Dashboard** | *https://sonarcloud.io/summary/new_code?id=pericjul_praesto-original* |
| **Live-Anwendung** | *https://praesto.azurewebsites.net/login* |

---

*Praesto - Bereite dich auf deine Zukunft vor* 🚀
