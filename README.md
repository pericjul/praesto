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

Praesto hilft Jugendlichen, die eine Lehrstelle suchen, sich gezielt und spielerisch auf den Bewerbungsprozess vorzubereiten. Die Plattform kombiniert künstliche Intelligenz mit schulischem Lernen: Schüler:innen trainieren Bewerbungsgespräche über einen Chat, Lehrkräfte verwalten Aufgaben und sehen die Antworten ein. Durch Badges und Fortschrittssysteme werden Motivation und Lernbereitschaft gefördert.

---

## Explore-Board

### Trends & Technologie
- Zunehmender Einsatz von künstlicher Intelligenz im Bildungsbereich
- Digitalisierung des Unterrichts und Online-Lernplattformen
- Bedeutung von Soft Skills und persönlicher Präsentation im Bewerbungsprozess
- Realitätsnahe Simulationen und spielerische Lernmethoden (Gamification)

### Potentielle Partner & Wettbewerb

**Partnerschaften:**
- Schulen, Berufsinformationszentren, Bildungsdirektionen, Lehrbetriebe

**Wettbewerb:**
- Online-Ratgeber und Bewerbungsvorlagen (statisch)
- YouTube-Tutorials und allgemeine Karriereportale
- Kein interaktives, schulintegriertes KI-Tool für Bewerbungstraining

### Fakten
- Viele Schüler:innen fühlen sich unvorbereitet auf Bewerbungsgespräche
- Bewerbungstrainings werden in Schulen kaum noch priorisiert
- Eigene Beobachtung: Jugendliche sind oft unsicher im Umgang mit typischen Interviewfragen

### User
- Schüler:innen zwischen 14 und 17 Jahren auf Lehrstellensuche
- Lehrkräfte im Berufswahlunterricht

### Potenzialfelder
- Interaktive Gesprächssimulation mit KI
- Aufgabenverwaltung durch Lehrkräfte
- Gamification für Motivation und Lernfreude

### Erkenntnisse
- Schüler:innen haben Angst vor Bewerbungsgesprächen
- Lehrkräfte wünschen sich digitale Unterstützung im Berufswahlunterricht
- KI kann individuelle Lernbedürfnisse besser berücksichtigen

### Bedürfnisse
- Realistische Vorbereitung auf Bewerbungsgespräche
- Einfaches, direktes Feedback
- Struktur und Motivation im Lernprozess

### Touchpoints
- PC oder Laptop in der Schule
- Smartphone oder Tablet zuhause

### Wie können wir?
Wie können wir Schüler:innen, die eine Lehrstelle suchen, digital und individuell auf Bewerbungsgespräche vorbereiten und Lehrkräften ein einfaches Tool zur Verwaltung dieser Trainings bieten?

---

## Create-Board

### Ideen-Beschreibung
Praesto ist eine Webplattform, auf der Schüler:innen mit einer KI Bewerbungsgespräche üben. Lehrkräfte erstellen Aufgaben, sehen die Antworten ihrer Schüler:innen und erhalten Auswertungen über Fortschritte und Leistungen.

### Adressierte Nutzer
- Schüler:innen auf Lehrstellensuche
- Lehrkräfte, die Berufswahlunterricht erteilen

### Adressierte Bedürfnisse
- Schüler:innen: Sicherheit und Übung durch Feedback
- Lehrkräfte: Strukturierte Kontrolle und Zeitersparnis

### Probleme
- Unsicherheit und Nervosität vor Bewerbungsgesprächen
- Fehlendes Training im Schulalltag
- Fehlende Motivation, sich mit Bewerbungsthemen zu beschäftigen

### Ideenpotenzial

| Kriterium | Bewertung |
|-----------|-----------|
| Mehrwert | 🔵🔵🔵🔵🔵🔵🔵🔵⚪⚪ (8/10) |
| Übertragbarkeit | 🔵🔵🔵🔵🔵🔵⚪⚪⚪⚪ (6/10) |
| Machbarkeit | 🔵🔵🔵🔵🔵🔵🔵⚪⚪⚪ (7/10) |

### Das WOW
Die KI reagiert individuell auf jede Antwort der Schüler:innen, stellt Rückfragen und gibt sofort Feedback – wie in einem echten Bewerbungsgespräch.

### High-Level-Konzept
**Praesto ist wie Duolingo, aber für Bewerbungsgespräche.**

### Wertversprechen
Praesto stärkt das Selbstvertrauen der Schüler:innen und unterstützt Lehrkräfte bei der Durchführung moderner, digitaler Bewerbungstrainings.

---

## Evaluate-Board

### Kanäle
- Schulen und Berufsinformationszentren
- Präsentationen an Bildungsmessen
- Social Media (LinkedIn, Instagram, TikTok)
- Empfehlungen durch Lehrkräfte und Schulnetzwerke

### Unfairer Vorteil
- Kombination aus KI-Interviewtraining und Lehrer-Feedback-System
- Echtzeit-Auswertung der Schüler:innen-Antworten
- Motivation durch Badges und Fortschrittssysteme

### KPI
- Anzahl durchgeführter Bewerbungstrainings
- Zufriedenheit der Schüler:innen (Selbsteinschätzung)
- Zeitersparnis für Lehrkräfte
- Aktive Nutzer:innen pro Schule

### Einnahmequellen
- Lizenzmodell für Schulen (jährliche Nutzung)
- Freemium-Version für einzelne Schüler:innen
- Potenzielle Förderung durch Bildungsstiftungen oder öffentliche Initiativen

---

## Diskussion Feedback Pitch

Beim Pitch wurden verschiedene Fragen und Anregungen aus dem Publikum gesammelt. Die wichtigsten Themen und deren Einordnung:

### Erweiterung der Zielgruppe
**Feedback:** Mehrere Personen fragten, ob Praesto auch für Studierende, Berufseinsteiger:innen, Arbeitsuchende oder Arbeitslosenkassen angeboten werden könnte.

**Einordnung:** Die Erweiterung auf weitere Zielgruppen ist grundsätzlich möglich, da die KI-Gesprächssimulation universell einsetzbar ist. Für den aktuellen Projektumfang bleibt der Fokus jedoch bewusst auf Schüler:innen und Lehrkräften, um eine klare Nutzerführung zu gewährleisten. Eine Erweiterung wäre ein sinnvoller nächster Schritt nach dem MVP.

### Voice-Chat und Video-Upload
**Feedback:** Es wurde gefragt, ob ein Voice-Chat integriert werden könnte oder ob Videos hochgeladen werden können, um Feedback zum Auftreten zu erhalten.

**Einordnung:** Diese Features würden den Realitätsgrad des Trainings erhöhen. Im aktuellen Scope wurde jedoch bewusst auf Text-Chat gesetzt, da dies technisch einfacher umzusetzen ist und Schüler:innen auch schriftliche Kommunikation üben (z.B. für E-Mail-Bewerbungen). Voice/Video sind als optionale Erweiterungen im Backlog notiert.

### Gamification / Belohnungssystem
**Feedback:** Eine Teilnehmerin schlug vor, ein Belohnungssystem mit Abzeichen einzubauen – ähnlich wie bei Duolingo.

**Einordnung:** Dieses Feedback wurde direkt umgesetzt! Praesto enthält ein Badge-System mit 37 Badges in 7 Kategorien (KI-Training, Notizen, Bewerbungen, Meilensteine, Abgaben, Feedback, Bewertungen). Schüler:innen werden so motiviert, regelmässig zu üben.

### Barrierefreiheit
**Feedback:** Wie können Schüler:innen mit Leseschwäche die App effizient nutzen?

**Einordnung:** Aktuell ist die App textbasiert. Für zukünftige Versionen wäre eine Text-to-Speech-Funktion oder vereinfachte Sprache denkbar. Dies wurde als Feature-Idee aufgenommen.

### Finanzierung und Marketing
**Feedback:** Fragen zu Monetarisierung und wie Schulen mit begrenztem Budget erreicht werden können.

**Einordnung:** Das Evaluate-Board enthält bereits ein Lizenzmodell. Zusätzlich wäre eine Förderung durch Bildungsstiftungen oder kantonale Bildungsdirektionen denkbar, um die Hürde für Schulen zu senken.

### Firmenspezifische Vorbereitung
**Feedback:** Könnte Praesto darauf hinweisen, sich über das Unternehmen zu informieren?

**Einordnung:** Die KI fragt bereits nach der Firma und dem Beruf, für den geübt wird, und passt die Fragen entsprechend an. Eine tiefere Integration mit Firmendatenbanken wäre eine mögliche Erweiterung.

---

# Anforderungen

## Use-Case Diagramm

![Use-Case Diagramm](doc/uc-diagram.drawio.svg)

### Akteure

| Akteur | Beschreibung |
|--------|--------------|
| **Student** | Schüler:in auf Lehrstellensuche, die das KI-Bewerbungstraining nutzt |
| **Teacher** | Lehrkraft im Berufswahlunterricht, die Klassen und Aufgaben verwaltet |

### Use-Case Übersicht

| ID | Use Case | Akteur(e) | Beschreibung |
|----|----------|-----------|--------------|
| UC01 | Einloggen | Student, Teacher | Authentifizierung via Auth0 |
| UC02 | Dashboard anzeigen | Student | Übersicht mit Fortschritt und offenen Aufgaben |
| UC03 | Mit KI-Coach chatten | Student | Freies Bewerbungstraining mit der KI |
| UC04 | KI-Session für Aufgabe | Student | Aufgabengebundenes KI-Training |
| UC05 | Bewerbungen verwalten | Student | CRUD für Bewerbungen mit Status-Tracking |
| UC06 | Notizen erstellen | Student | Persönliche Notizen zu Firmen/Stellen |
| UC07 | Badges ansehen | Student | Gamification-Fortschritt einsehen |
| UC08 | Aufgaben ansehen | Student | Klassenaufgaben mit Deadlines |
| UC09 | Abgabe einreichen | Student | Verschiedene Abgabetypen |
| UC10 | Dashboard anzeigen | Teacher | Übersicht Klassen und offene Bewertungen |
| UC11 | Klassen verwalten | Teacher | Klassen erstellen und bearbeiten |
| UC12 | Schüler hinzufügen | Teacher | Schüler per Email zur Klasse hinzufügen |
| UC13 | Aufgaben erstellen | Teacher | 5 verschiedene Aufgabentypen |
| UC14 | Abgaben einsehen | Teacher | Schülerabgaben prüfen |
| UC15 | Feedback geben | Teacher | Bewertung und Rückmeldung |

---

## Use-Case Beschreibungen

### UC01: Einloggen

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC01 |
| **Titel** | Einloggen |
| **Akteure** | Student, Teacher |
| **Vorbedingung** | Benutzer hat ein gültiges Konto (via OAuth2/Auth0) |
| **Nachbedingung** | Benutzer ist authentifiziert und wird zum rollenspezifischen Dashboard weitergeleitet |
| **Standardablauf** | 1. Benutzer öffnet die Praesto-Webseite<br>2. System zeigt Login-Seite mit "Sign In"-Button<br>3. Benutzer klickt auf "Sign In"<br>4. System leitet zu Auth0-Login weiter<br>5. Benutzer gibt Credentials ein<br>6. System authentifiziert und ermittelt Rolle (Student/Teacher)<br>7. System leitet zum entsprechenden Dashboard weiter |
| **Ausnahmen** | 5a. Ungültige Credentials → Auth0 zeigt Fehlermeldung |

---

### UC02: Dashboard anzeigen (Student)

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC02 |
| **Titel** | Dashboard anzeigen (Student) |
| **Akteure** | Student |
| **Vorbedingung** | Student ist eingeloggt |
| **Nachbedingung** | Student sieht Übersicht mit Fortschritt und offenen Aufgaben |
| **Standardablauf** | 1. System lädt Student-Dashboard<br>2. System zeigt Begrüssungstext mit Namen<br>3. System zeigt Fortschrittsanzeige (Badges, abgeschlossene Aufgaben)<br>4. System zeigt Liste der offenen Aufgaben mit Deadlines<br>5. System zeigt Quick-Actions (Chat starten, Bewerbungen, Notizen) |

---

### UC03: Mit KI-Coach chatten

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC03 |
| **Titel** | Mit KI-Coach chatten |
| **Akteure** | Student |
| **Vorbedingung** | Student ist eingeloggt |
| **Nachbedingung** | Chat-Session ist gespeichert, ggf. Badges wurden vergeben |
| **Standardablauf** | 1. Student öffnet Chat-Bereich<br>2. System zeigt Chat-Interface mit Eingabefeld<br>3. Student gibt Nachricht ein (z.B. Antwort auf Interviewfrage)<br>4. System sendet Nachricht an Spring AI Backend<br>5. KI generiert kontextbezogene Antwort (Rückfrage oder Feedback)<br>6. System zeigt KI-Antwort im Chat<br>7. Schritte 3-6 wiederholen sich<br>8. Student beendet Session |
| **Ausnahmen** | 4a. KI-Service nicht erreichbar → Fehlermeldung anzeigen |
| **Data Definitions** | Eine **Session** besteht aus mehreren **Messages** (role: user/assistant, content, timestamp). Die KI nutzt zwei Modi: freies Training oder Assignment-Modus. |

---

### UC04: KI-Session für Aufgabe starten

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC04 |
| **Titel** | KI-Session für Aufgabe starten |
| **Akteure** | Student |
| **Vorbedingung** | Student ist eingeloggt, Aufgabe vom Typ AI_INTERVIEW existiert |
| **Nachbedingung** | Session ist mit Assignment verknüpft, Submission wird erstellt |
| **Standardablauf** | 1. Student öffnet Aufgabendetails<br>2. System zeigt Aufgabenbeschreibung und "Training starten"-Button<br>3. Student klickt "Training starten"<br>4. System erstellt neue Session mit Assignment-Referenz<br>5. System lädt Assignment-spezifischen System-Prompt<br>6. KI begrüsst Student mit aufgabenspezifischem Kontext<br>7. Student führt Übung durch (siehe UC03)<br>8. Nach Abschluss erstellt System Submission |
| **Data Definitions** | Ein **Assignment** hat einen Typ (AI_INTERVIEW, DOCUMENT_UPLOAD, etc.), Titel, Beschreibung und Deadline. Eine **Submission** verknüpft Student, Assignment und Session. |

---

### UC05: Bewerbungen verwalten

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC05 |
| **Titel** | Bewerbungen verwalten |
| **Akteure** | Student |
| **Vorbedingung** | Student ist eingeloggt |
| **Nachbedingung** | Bewerbung ist erstellt/aktualisiert/gelöscht |
| **Standardablauf** | 1. Student öffnet Bewerbungs-Bereich<br>2. System zeigt Liste aller Bewerbungen mit Status<br>3. Student wählt "Neue Bewerbung"<br>4. System zeigt Formular (Firma, Beruf, Status, Notizen)<br>5. Student füllt Formular aus und speichert<br>6. System speichert Bewerbung und aktualisiert Liste |
| **Ausnahmen** | 3a. Student wählt bestehende Bewerbung → System zeigt Detailansicht mit Bearbeitungsmöglichkeit |
| **Data Definitions** | Eine **Application** enthält: id, company, jobTitle, status (PLANNED, APPLIED, INTERVIEW, ACCEPTED, REJECTED), notes, appliedDate, userId. |

---

### UC06: Notizen erstellen

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC06 |
| **Titel** | Notizen erstellen |
| **Akteure** | Student |
| **Vorbedingung** | Student ist eingeloggt |
| **Nachbedingung** | Notiz ist gespeichert |
| **Standardablauf** | 1. Student öffnet Notizen-Bereich<br>2. System zeigt Liste bestehender Notizen<br>3. Student klickt "Neue Notiz"<br>4. System zeigt Editor (Titel, Inhalt)<br>5. Student schreibt Notiz und speichert<br>6. System speichert mit Zeitstempel |
| **Data Definitions** | Eine **Note** enthält: id, title, content, createdAt, updatedAt, userId. |

---

### UC07: Badges ansehen

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC07 |
| **Titel** | Badges ansehen |
| **Akteure** | Student |
| **Vorbedingung** | Student ist eingeloggt |
| **Nachbedingung** | - |
| **Standardablauf** | 1. Student öffnet Badge-Übersicht<br>2. System lädt alle verfügbaren Badges (37 Stück)<br>3. System lädt bereits verdiente Badges des Students<br>4. System zeigt Badges gruppiert nach Kategorie<br>5. Verdiente Badges werden farbig angezeigt, nicht verdiente ausgegraut<br>6. Bei Klick auf Badge zeigt System Details (Name, Beschreibung, Kriterium) |
| **Data Definitions** | Ein **Badge** hat: id, name, description, icon, category (KI_TRAINING, NOTES, APPLICATIONS, MILESTONES, SUBMISSIONS, FEEDBACK, RATINGS). Ein **UserBadge** verknüpft Badge mit User und enthält earnedAt-Timestamp. |

---

### UC08: Aufgaben ansehen (Student)

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC08 |
| **Titel** | Aufgaben ansehen |
| **Akteure** | Student |
| **Vorbedingung** | Student ist eingeloggt und einer Klasse zugewiesen |
| **Nachbedingung** | - |
| **Standardablauf** | 1. Student öffnet Aufgaben-Bereich<br>2. System ermittelt Klasse des Students<br>3. System lädt alle Aufgaben der Klasse<br>4. System zeigt Aufgaben mit Titel, Typ, Deadline und Status<br>5. Student kann Aufgabe auswählen für Details |

---

### UC09: Abgabe einreichen

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC09 |
| **Titel** | Abgabe einreichen |
| **Akteure** | Student |
| **Vorbedingung** | Student ist eingeloggt, Aufgabe existiert |
| **Nachbedingung** | Submission ist erstellt und für Lehrkraft sichtbar |
| **Standardablauf** | 1. Student öffnet Aufgabendetails<br>2. System zeigt Aufgabe mit Typ-spezifischem Eingabebereich<br>3a. Bei AI_INTERVIEW: Student führt KI-Session durch (UC04)<br>3b. Bei DOCUMENT_UPLOAD: Student lädt Datei hoch<br>3c. Bei SELF_REFLECTION: Student schreibt Text<br>4. Student klickt "Abgeben"<br>5. System erstellt Submission mit Zeitstempel<br>6. System prüft Badge-Kriterien und vergibt ggf. Badges |

---

### UC10: Dashboard anzeigen (Teacher)

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC10 |
| **Titel** | Dashboard anzeigen (Teacher) |
| **Akteure** | Teacher |
| **Vorbedingung** | Teacher ist eingeloggt |
| **Nachbedingung** | - |
| **Standardablauf** | 1. System lädt Teacher-Dashboard<br>2. System zeigt Übersicht der verwalteten Klassen<br>3. System zeigt Anzahl offener Abgaben zur Bewertung<br>4. System zeigt Quick-Actions (Klassen, Aufgaben, Abgaben) |

---

### UC11: Klassen verwalten

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC11 |
| **Titel** | Klassen verwalten |
| **Akteure** | Teacher |
| **Vorbedingung** | Teacher ist eingeloggt |
| **Nachbedingung** | Klasse ist erstellt/aktualisiert |
| **Standardablauf** | 1. Teacher öffnet Klassen-Bereich<br>2. System zeigt Liste aller Klassen des Teachers<br>3. Teacher wählt "Neue Klasse erstellen"<br>4. System zeigt Formular (Name, Beschreibung)<br>5. Teacher füllt aus und speichert<br>6. System erstellt Klasse und zeigt in Liste |
| **Data Definitions** | Eine **Class** enthält: id, name, description, teacherId, studentIds[], createdAt. |

---

### UC12: Schüler zur Klasse hinzufügen

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC12 |
| **Titel** | Schüler zur Klasse hinzufügen |
| **Akteure** | Teacher |
| **Vorbedingung** | Teacher ist eingeloggt, Klasse existiert |
| **Nachbedingung** | Student ist der Klasse zugewiesen |
| **Standardablauf** | 1. Teacher öffnet Klassendetails<br>2. System zeigt aktuelle Schülerliste<br>3. Teacher klickt "Schüler hinzufügen"<br>4. System zeigt verfügbare Studenten<br>5. Teacher wählt Student(en) aus<br>6. System fügt Student(en) zur Klasse hinzu |

---

### UC13: Aufgaben erstellen

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC13 |
| **Titel** | Aufgaben erstellen |
| **Akteure** | Teacher |
| **Vorbedingung** | Teacher ist eingeloggt, mindestens eine Klasse existiert |
| **Nachbedingung** | Aufgabe ist erstellt und für Klasse sichtbar |
| **Standardablauf** | 1. Teacher öffnet Aufgaben-Bereich<br>2. Teacher klickt "Neue Aufgabe"<br>3. System zeigt Formular<br>4. Teacher wählt Typ (AI_INTERVIEW, DOCUMENT_UPLOAD, SELF_REFLECTION, VIDEO_PITCH, RESEARCH)<br>5. Teacher gibt Titel, Beschreibung, Deadline ein<br>6. Teacher wählt Zielklasse(n)<br>7. Teacher speichert<br>8. System erstellt Assignment und benachrichtigt Klasse |
| **Data Definitions** | Ein **Assignment** enthält: id, title, description, type, dueDate, classId, teacherId, createdAt. |

---

### UC14: Abgaben einsehen

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC14 |
| **Titel** | Abgaben einsehen |
| **Akteure** | Teacher |
| **Vorbedingung** | Teacher ist eingeloggt, Submissions existieren |
| **Nachbedingung** | - |
| **Standardablauf** | 1. Teacher öffnet Abgaben-Bereich<br>2. System zeigt Liste aller Submissions für Teacher's Klassen<br>3. Teacher filtert nach Klasse/Aufgabe/Status<br>4. Teacher wählt Submission<br>5. System zeigt Details (bei AI_INTERVIEW: Chat-Verlauf, bei DOCUMENT_UPLOAD: Datei) |

---

### UC15: Feedback geben

| Attribut | Beschreibung |
|----------|--------------|
| **ID** | UC15 |
| **Titel** | Feedback geben |
| **Akteure** | Teacher |
| **Vorbedingung** | Teacher ist eingeloggt, Submission existiert |
| **Nachbedingung** | Feedback ist gespeichert, Student kann es einsehen |
| **Standardablauf** | 1. Teacher öffnet Submission-Details (UC14)<br>2. Teacher liest/prüft Abgabe<br>3. Teacher gibt schriftliches Feedback ein<br>4. Teacher vergibt optional Bewertung/Punkte<br>5. Teacher speichert<br>6. System aktualisiert Submission-Status<br>7. System prüft Badge-Kriterien für Student |

---

## Fachliches Datenmodell

![ER-Diagramm](doc/ER-diagram.drawio.svg)

### Entitäten und Attribute

#### SchoolClass (Schulklasse)
Repräsentiert eine Schulklasse, die von einer Lehrkraft verwaltet wird.

| Attribut | Typ | Beschreibung |
|----------|-----|--------------|
| id | String | Eindeutige ID (MongoDB ObjectId) |
| name | String | Klassenbezeichnung (z.B. "INF2024a") |
| teacherId | String | Auth0-ID der Lehrkraft |
| studentEmails | List&lt;String&gt; | Email-Adressen der Schüler:innen |
| createdAt | Instant | Erstellungszeitpunkt |
| updatedAt | Instant | Letzte Änderung |

---

#### Assignment (Aufgabe)
Eine von der Lehrkraft erstellte Übungsaufgabe für eine Klasse.

| Attribut | Typ | Beschreibung |
|----------|-----|--------------|
| id | String | Eindeutige ID |
| title | String | Aufgabentitel |
| description | String | Detaillierte Beschreibung |
| durationMin | Integer | Vorgesehene Dauer in Minuten |
| dueDate | Instant | Abgabefrist |
| type | AssignmentType | Art der Aufgabe (siehe Enum) |
| status | AssignmentStatus | Aktueller Status (siehe Enum) |
| classId | String | Referenz zur Klasse |
| createdByTeacherId | String | Ersteller (Lehrkraft) |
| createdAt | Instant | Erstellungszeitpunkt |

**AssignmentType (Enum):**

| Wert | Anzeigename | Beschreibung |
|------|-------------|--------------|
| AI_INTERVIEW | KI-Bewerbungsgespräch | Übe ein Bewerbungsgespräch mit der KI |
| DOCUMENT_UPLOAD | Dokument einreichen | Lade ein Dokument hoch (PDF, Word) |
| SELF_REFLECTION | Selbstreflexion | Schreibe eine Reflexion zu einem Thema |
| VIDEO_PITCH | Video-Bewerbung | Nimm ein kurzes Bewerbungsvideo auf |
| RESEARCH | Recherche | Recherchiere zu einem Thema und fasse zusammen |

**AssignmentStatus (Enum):**

| Wert | Beschreibung |
|------|--------------|
| ASSIGNED | Aufgabe zugewiesen, noch nicht begonnen |
| IN_PROGRESS | Schüler:in arbeitet daran |
| SUBMITTED | Abgegeben |
| REVIEWED | Vom Lehrer bewertet |
| CLOSED | Geschlossen (keine Abgaben mehr möglich) |

---

#### Submission (Abgabe)
Eine eingereichte Lösung eines Schülers zu einer Aufgabe.

| Attribut | Typ | Beschreibung |
|----------|-----|--------------|
| id | String | Eindeutige ID |
| assignmentId | String | Referenz zur Aufgabe |
| studentEmail | String | Email der/des Schüler:in |
| type | AssignmentType | Typ (übernommen von Assignment) |
| textContent | String | Textinhalt (für SELF_REFLECTION, RESEARCH) |
| links | List&lt;String&gt; | Links (für RESEARCH) |
| fileUrl | String | Datei-URL (für DOCUMENT_UPLOAD, VIDEO_PITCH) |
| fileName | String | Original-Dateiname |
| comment | String | Optionaler Kommentar |
| chatSessionId | String | Referenz zur KI-Session (für AI_INTERVIEW) |
| status | SubmissionStatus | Bearbeitungsstatus |
| submittedAt | Instant | Abgabezeitpunkt |
| teacherFeedback | String | Rückmeldung der Lehrkraft |
| grade | Integer | Note/Punkte (optional) |
| reviewedAt | Instant | Bewertungszeitpunkt |
| reviewedByTeacherId | String | Bewertende Lehrkraft |

**SubmissionStatus (Enum):**

| Wert | Anzeigename |
|------|-------------|
| SUBMITTED | Eingereicht |
| IN_REVIEW | In Prüfung |
| REVIEWED | Bewertet |
| RETURNED | Zurückgegeben |

---

#### Session (KI-Chat-Session)
Eine Trainingseinheit mit dem KI-Coach.

| Attribut | Typ | Beschreibung |
|----------|-----|--------------|
| id | String | Eindeutige ID |
| studentId | String | Auth0-ID des Schülers |
| studentEmail | String | Email (für Submission-Verknüpfung) |
| assignmentId | String | Optionale Referenz zur Aufgabe |
| assignmentTitle | String | Aufgabentitel (wenn vorhanden) |
| targetDurationMin | Integer | Soll-Dauer von der Aufgabe |
| messages | List&lt;SessionMessage&gt; | Chat-Verlauf (Embedded Document) |
| score | Integer | Auswertung (für spätere Erweiterung) |
| status | SessionStatus | OPEN oder CLOSED |
| startedAt | Instant | Startzeitpunkt |
| closedAt | Instant | Endzeitpunkt |
| submittedAsAssignment | Boolean | Als Aufgabe eingereicht? |

**SessionMessage (Embedded Document):**

| Attribut | Typ | Beschreibung |
|----------|-----|--------------|
| role | String | "user" oder "assistant" |
| content | String | Nachrichtentext |
| createdAt | Instant | Zeitstempel |

---

#### Application (Bewerbung)
Tracking einer echten Bewerbung des Schülers.

| Attribut | Typ | Beschreibung |
|----------|-----|--------------|
| id | String | Eindeutige ID |
| studentId | String | Auth0-ID |
| companyName | String | Firmenname |
| position | String | Stelle/Position |
| status | ApplicationStatus | Aktueller Stand |
| appliedAt | LocalDate | Bewerbungsdatum |
| interviewDate | LocalDate | Gesprächstermin (optional) |
| notes | String | Persönliche Notizen |
| createdAt | Instant | Erstellungszeitpunkt |
| updatedAt | Instant | Letzte Änderung |

**ApplicationStatus (Enum):**

| Wert | Beschreibung |
|------|--------------|
| PLANNED | Geplant, noch nicht beworben |
| APPLIED | Bewerbung abgeschickt |
| INVITED | Zum Gespräch eingeladen |
| INTERVIEW_DONE | Gespräch absolviert |
| ACCEPTED | Zusage erhalten 🎉 |
| REJECTED | Absage erhalten |
| WITHDRAWN | Zurückgezogen |

---

#### Note (Notiz)
Persönliche Notizen zu Firmen oder Stellen.

| Attribut | Typ | Beschreibung |
|----------|-----|--------------|
| id | String | Eindeutige ID |
| studentId | String | Auth0-ID |
| companyName | String | Firmenname (optional) |
| position | String | Stelle (optional) |
| text | String | Notizinhalt |
| createdAt | Instant | Erstellungszeitpunkt |
| lastUpdated | Instant | Letzte Änderung |

---

#### Badge (Abzeichen-Definition)
Definition eines erreichbaren Badges.

| Attribut | Typ | Beschreibung |
|----------|-----|--------------|
| id | String | Eindeutige ID |
| icon | String | Emoji (🎯, 🔥, 🏆, etc.) |
| title | String | Badge-Name (z.B. "Erste Session") |
| description | String | Beschreibung (z.B. "Dein erstes KI-Training!") |
| ruleType | BadgeRuleType | Vergaberegel (siehe Enum) |
| threshold | Integer | Schwellwert für Vergabe |
| sortOrder | Integer | Anzeigereihenfolge |

**BadgeRuleType (Enum):**

| Wert | Beschreibung |
|------|--------------|
| SESSIONS_COMPLETED | Anzahl abgeschlossene KI-Sessions |
| NOTES_CREATED | Anzahl erstellte Notizen |
| APPLICATIONS_CREATED | Anzahl erstellte Bewerbungen |
| APPLICATION_STATUS | Bewerbung mit bestimmtem Status erreicht |
| SUBMISSIONS_COMPLETED | Anzahl abgegebene Aufgaben |
| FEEDBACK_RECEIVED | Anzahl erhaltenes Feedback |
| GRADES_RECEIVED | Anzahl erhaltene Bewertungen |

---

#### UserBadge (Verdientes Badge)
Verknüpfung zwischen Schüler:in und verdientem Badge.

| Attribut | Typ | Beschreibung |
|----------|-----|--------------|
| id | String | Eindeutige ID |
| studentId | String | Auth0-ID |
| badgeId | String | Referenz zum Badge |
| earnedAt | Instant | Zeitpunkt der Vergabe |

*Hinweis: Compound Index auf (studentId, badgeId) verhindert doppelte Vergabe.*

---

### Beziehungen

```
SchoolClass 1 ──────< * Assignment      (Eine Klasse hat viele Aufgaben)
SchoolClass 1 ──────< * Student         (Eine Klasse hat viele Schüler via Email)
Teacher     1 ──────< * SchoolClass     (Ein Lehrer verwaltet viele Klassen)

Assignment  1 ──────< * Submission      (Eine Aufgabe hat viele Abgaben)
Student     1 ──────< * Submission      (Ein Schüler macht viele Abgaben)

Student     1 ──────< * Session         (Ein Schüler hat viele KI-Sessions)
Assignment  1 ──────< * Session         (Eine Aufgabe kann viele Sessions haben)
Session     1 ──────< * SessionMessage  (Eine Session hat viele Nachrichten)

Student     1 ──────< * Application     (Ein Schüler hat viele Bewerbungen)
Student     1 ──────< * Note            (Ein Schüler hat viele Notizen)
Student     1 ──────< * UserBadge       (Ein Schüler hat viele verdiente Badges)
Badge       1 ──────< * UserBadge       (Ein Badge kann von vielen verdient werden)
```

---

### Zustandsübergänge

Die folgenden Entitäten durchlaufen mehrere Zustände während ihres Lebenszyklus:

#### Assignment (Aufgabe)

```
┌──────────┐     Schüler      ┌─────────────┐     Schüler      ┌───────────┐
│ ASSIGNED │ ──── beginnt ───▶│ IN_PROGRESS │ ──── gibt ab ───▶│ SUBMITTED │
└──────────┘                  └─────────────┘                  └───────────┘
                                                                     │
                                                              Lehrer bewertet
                                                                     ▼
┌──────────┐     Lehrer       ┌──────────┐
│  CLOSED  │◀─── schliesst ───│ REVIEWED │
└──────────┘                  └──────────┘
```

| Status | Beschreibung |
|--------|--------------|
| ASSIGNED | Aufgabe wurde der Klasse zugewiesen |
| IN_PROGRESS | Mindestens ein Schüler arbeitet daran |
| SUBMITTED | Abgabe wurde eingereicht |
| REVIEWED | Lehrkraft hat Feedback gegeben |
| CLOSED | Keine weiteren Abgaben möglich |

---

#### Submission (Abgabe)

```
┌───────────┐     Lehrer      ┌───────────┐     Lehrer      ┌──────────┐
│ SUBMITTED │ ──── prüft ────▶│ IN_REVIEW │ ──── bewertet ─▶│ REVIEWED │
└───────────┘                 └───────────┘                 └──────────┘
                                                                  │
                                                           bei Mängeln
                                                                  ▼
                                                            ┌──────────┐
                                                            │ RETURNED │
                                                            └──────────┘
```

| Status | Beschreibung |
|--------|--------------|
| SUBMITTED | Schüler hat Abgabe eingereicht |
| IN_REVIEW | Lehrkraft prüft die Abgabe |
| REVIEWED | Feedback und Note wurden vergeben |
| RETURNED | Zurück an Schüler zur Überarbeitung |

---

#### Application (Bewerbung)

```
┌─────────┐     Bewerbung     ┌─────────┐     Einladung     ┌─────────┐
│ PLANNED │ ──── senden ─────▶│ APPLIED │ ──── erhalten ───▶│ INVITED │
└─────────┘                   └─────────┘                   └─────────┘
                                   │                             │
                               Absage                       Gespräch
                                   ▼                             ▼
                              ┌──────────┐               ┌────────────────┐
                              │ REJECTED │               │ INTERVIEW_DONE │
                              └──────────┘               └────────────────┘
                                                               │
                                              ┌────────────────┼────────────────┐
                                              ▼                ▼                ▼
                                        ┌──────────┐    ┌──────────┐    ┌───────────┐
                                        │ ACCEPTED │    │ REJECTED │    │ WITHDRAWN │
                                        └──────────┘    └──────────┘    └───────────┘
```

| Status | Beschreibung |
|--------|--------------|
| PLANNED | Bewerbung geplant, noch nicht gesendet |
| APPLIED | Bewerbung wurde abgeschickt |
| INVITED | Einladung zum Vorstellungsgespräch |
| INTERVIEW_DONE | Gespräch wurde geführt |
| ACCEPTED | Zusage erhalten 🎉 |
| REJECTED | Absage erhalten |
| WITHDRAWN | Bewerbung zurückgezogen |

---

#### Session (KI-Chat)

```
┌──────┐     Schüler beendet     ┌────────┐
│ OPEN │ ────── oder gibt ab ───▶│ CLOSED │
└──────┘                         └────────┘
```

| Status | Beschreibung |
|--------|--------------|
| OPEN | Aktive Chat-Session |
| CLOSED | Session beendet (manuell oder durch Abgabe) |

---

## UI-Mockups

Die folgenden Mockups wurden in der Planungsphase erstellt, um die Kernfunktionen und das Nutzererlebnis zu definieren. Sie dienten als Grundlage für die Implementierung des Frontends.

### Student-Ansichten

#### Student Dashboard
![Student Dashboard Mockup](doc/student/mockup/Student-Dashboard-Mockup.png)

**Konzept:** Das Dashboard ist der zentrale Einstiegspunkt für Schüler:innen. Es zeigt auf einen Blick die wichtigsten Informationen: offene Aufgaben mit Deadlines, den Fortschritt (verdiente Badges), und ermöglicht den schnellen Zugriff auf das KI-Training. Die Gestaltung ist bewusst übersichtlich gehalten, um Jugendliche nicht zu überfordern und zur Nutzung zu motivieren.

---

#### Aufgaben-Übersicht (Student)
![Student Aufgaben Übersicht](doc/student/mockup/Student-Aufgaben-Uebersicht.png)

**Konzept:** Hier sehen Schüler:innen alle Aufgaben ihrer Klasse. Jede Aufgabe zeigt den Typ (KI-Training, Dokument, etc.), die Deadline und den aktuellen Status. Überfällige Aufgaben werden visuell hervorgehoben. Von hier kann direkt eine Aufgabe gestartet oder eine Abgabe eingereicht werden.

---

#### KI-Chat Interface
![Student Chat](doc/student/mockup/Student-Chatting.png)

**Konzept:** Das Chat-Interface ist das Herzstück der Anwendung. Es simuliert ein echtes Bewerbungsgespräch mit einer freundlichen, aber professionellen KI. Die Darstellung als Chat-Verlauf ist Jugendlichen aus Messaging-Apps vertraut. Die KI stellt Fragen, gibt Feedback und passt sich dem Kontext an (freies Training vs. Aufgabe).

---

#### Session-Übersicht
![Student Sessions](doc/student/mockup/Student-Session-Uebersicht.png)

**Konzept:** Schüler:innen können ihre vergangenen KI-Trainings einsehen und nachvollziehen. Dies ermöglicht Reflexion über den eigenen Fortschritt und das Wiederholen von Übungen. Sessions, die zu einer Aufgabe gehören, sind entsprechend markiert.

---

### Teacher-Ansichten

#### Teacher Dashboard
![Teacher Dashboard Mockup](doc/teacher/mockup/Teacher-Dashboard-Mockup.png)

**Konzept:** Das Lehrer-Dashboard bietet einen Überblick über alle verwalteten Klassen und zeigt an, wie viele Abgaben noch bewertet werden müssen. Quick-Actions ermöglichen den schnellen Zugriff auf häufige Aufgaben. Die Ansicht ist effizienzorientiert gestaltet, um Lehrkräften Zeit zu sparen.

---

#### Klassen-Verwaltung
![Teacher Klassen](doc/teacher/mockup/Teacher-Klassen-Mockup.png)

**Konzept:** Lehrkräfte können hier ihre Klassen verwalten, neue Klassen erstellen und Schüler:innen per Email-Adresse hinzufügen. Die Anzahl der Schüler:innen pro Klasse ist auf einen Blick ersichtlich. Das Design erlaubt die einfache Verwaltung mehrerer Klassen parallel.

---

#### Aufgaben-Übersicht (Teacher)
![Teacher Aufgaben](doc/teacher/mockup/Teacher-Aufgaben-Mockup.png)

**Konzept:** Übersicht aller erstellten Aufgaben mit Status-Anzeige: Wie viele Schüler:innen haben bereits abgegeben? Ist die Deadline überschritten? Von hier können neue Aufgaben erstellt oder bestehende bearbeitet werden.

---

#### Aufgabe erstellen
![Teacher Aufgabe erstellen](doc/teacher/mockup/Teacher-Create-Aufgabe-Mockup.png)

**Konzept:** Formular zur Erstellung einer neuen Aufgabe. Lehrkräfte wählen den Aufgabentyp (KI-Interview, Dokument-Upload, Selbstreflexion, etc.), geben Titel und Beschreibung ein, setzen eine Deadline und weisen die Aufgabe einer Klasse zu. Die Dauer-Angabe hilft Schüler:innen bei der Zeitplanung.

---

#### Aufgaben-Detail / Abgaben einsehen
![Teacher Aufgaben Detail](doc/teacher/mockup/Teacher-Aufgaben-Detail-Mockup.png)

**Konzept:** Detailansicht einer Aufgabe mit allen eingereichten Abgaben. Lehrkräfte sehen den Status jeder Abgabe (eingereicht, bewertet), können die Inhalte prüfen (Chat-Verlauf bei KI-Interviews, hochgeladene Dokumente, etc.) und Feedback sowie Bewertungen vergeben. Die Ansicht unterstützt den effizienten Bewertungs-Workflow.

---

# Implementation

## Technologie-Stack

| Bereich | Technologie | Version |
|---------|-------------|---------|
| **Backend** | Spring Boot | 3.x |
| **KI-Integration** | Spring AI + OpenAI | GPT-4 |
| **Datenbank** | MongoDB | Atlas (Cloud) |
| **Authentifizierung** | Auth0 | OAuth2/JWT |
| **Frontend** | SvelteKit | Svelte 5 |
| **Styling** | Tailwind CSS | 3.x |

---

## Frontend

Das Frontend wurde mit SvelteKit und Svelte 5 entwickelt. Es bietet separate Ansichten für Schüler:innen und Lehrkräfte, die rollenbasiert nach dem Login angezeigt werden. Das Design verwendet Tailwind CSS und ist responsive für Desktop und Mobile optimiert.

### Student-Ansichten

#### Dashboard
![Student Dashboard](doc/student/schueler-dashboard.png)

Das Dashboard ist die Startseite für Schüler:innen nach dem Login. Es bietet einen schnellen Überblick über alle relevanten Informationen:

**Angezeigte Elemente:**
- Persönliche Begrüssung mit dem Namen des Schülers
- Anzahl der offenen Aufgaben mit nächster Deadline
- Gesamtzahl der verdienten Badges als Motivationselement
- Liste der aktuell offenen Aufgaben mit Titel, Typ und Fälligkeitsdatum
- Benachrichtigungen über neues Feedback von Lehrkräften

**Mögliche Aktionen:**
- Direkt zu einer Aufgabe navigieren durch Klick auf die Aufgabenkarte
- Neues KI-Training starten über den Quick-Action-Button
- Zu Bewerbungen, Notizen oder Badges wechseln über die Navigation

---

#### Aufgaben-Übersicht
![Student Aufgaben Übersicht](doc/student/schueler-aufgaben-uebersicht.png)

Diese Ansicht zeigt alle Aufgaben, die der Klasse des Schülers zugewiesen wurden.

**Angezeigte Elemente:**
- Liste aller Aufgaben mit Titel und Beschreibung
- Aufgabentyp als Icon (KI-Interview, Dokument, Selbstreflexion, etc.)
- Deadline mit farblicher Hervorhebung (rot = überfällig, orange = bald fällig)
- Aktueller Status der eigenen Abgabe (offen, eingereicht, bewertet)
- Geschätzte Dauer der Aufgabe in Minuten

**Mögliche Aktionen:**
- Aufgabe anklicken um Details zu sehen
- Nach Status filtern (alle, offen, erledigt)
- Nach Deadline sortieren

---

#### Aufgaben-Detail
![Student Aufgabe Detail](doc/student/schueler-aufgabe-detail.png)

Die Detailansicht einer einzelnen Aufgabe mit allen Informationen und Aktionsmöglichkeiten.

**Angezeigte Elemente:**
- Vollständiger Titel und Beschreibung der Aufgabe
- Aufgabentyp mit Erklärung was erwartet wird
- Deadline und verbleibende Zeit
- Vorgesehene Bearbeitungsdauer
- Eigener Abgabestatus

**Mögliche Aktionen:**
- Bei KI-Interview: "Training starten" öffnet den Chat
- Bei anderen Typen: Abgabe-Formular ausfüllen und einreichen
- Zurück zur Übersicht navigieren

---

#### Aufgabe mit Feedback
![Student Aufgabe mit Feedback](doc/student/schueler-aufgabe-mit-feedback.png)

Nachdem eine Lehrkraft die Abgabe bewertet hat, erscheint das Feedback in der Aufgabenansicht.

**Angezeigte Elemente:**
- Erhaltene Note/Bewertung prominent angezeigt
- Schriftliches Feedback der Lehrkraft
- Datum der Bewertung
- Eigene ursprüngliche Abgabe zum Vergleich
- Bei KI-Interview: Link zum Chat-Verlauf

**Mögliche Aktionen:**
- Chat-Verlauf der Session nochmals ansehen
- Feedback als gelesen markieren

---

#### KI-Chat Übersicht
![Student Chat Übersicht](doc/student/schueler-chat-übersicht.png)

Übersicht aller bisherigen Trainings-Sessions mit dem KI-Coach.

**Angezeigte Elemente:**
- Liste aller Sessions chronologisch sortiert
- Datum und Uhrzeit jeder Session
- Dauer der Session
- Status: offen (kann fortgesetzt werden) oder abgeschlossen
- Falls vorhanden: Verknüpfte Aufgabe
- Anzahl der Nachrichten in der Session

**Mögliche Aktionen:**
- Offene Session fortsetzen
- Abgeschlossene Session nochmals lesen
- Neue freie Session starten
- Session löschen

---

#### KI-Chat (freies Training)
![Student Chat ohne Abgabe](doc/student/schueler-chat-ohne-abgabe.png)

Das Herzstück der Anwendung: Der Chat mit dem KI-Bewerbungscoach für freies Training.

**Angezeigte Elemente:**
- Chat-Verlauf mit allen Nachrichten (User und KI)
- KI-Nachrichten mit Coach-Avatar
- Eigene Nachrichten rechtsbündig
- Zeitstempel pro Nachricht
- Eingabefeld am unteren Rand
- Sende-Button

**Mögliche Aktionen:**
- Nachricht eingeben und absenden (Enter oder Button)
- Durch den Verlauf scrollen
- Session beenden über Button in der Navigation
- Zur Session-Übersicht zurückkehren

**Besonderheit:** Die KI agiert als HR-Coach und stellt realistische Bewerbungsfragen. Nach jeder Antwort gibt sie konstruktives Feedback mit konkreten Verbesserungsvorschlägen.

---

#### KI-Chat (für Aufgabe)
![Student Chat mit Abgabe](doc/student/schueler-chat-mit-abgabe.png)

Der Chat-Modus wenn eine Session zu einer Aufgabe gehört.

**Angezeigte Elemente:**
- Gleicher Chat wie beim freien Training
- Zusätzlich: Aufgabentitel in der Kopfzeile
- Hinweis auf die vorgesehene Dauer
- "Als Abgabe einreichen"-Button

**Mögliche Aktionen:**
- Chat führen wie beim freien Training
- Session beenden ohne Abgabe
- **Session als Abgabe einreichen** - dies erstellt automatisch eine Submission und schliesst die Session

**Besonderheit:** Nach dem Einreichen kann die Session nicht mehr fortgesetzt werden. Die Lehrkraft kann den gesamten Chat-Verlauf einsehen und bewerten.

---

#### Bewerbungen-Übersicht
![Student Bewerbungen Übersicht](doc/student/schueler-bewerbungen-uebersicht.png)

Der Bewerbungs-Tracker hilft Schüler:innen, den Überblick über ihre echten Bewerbungen zu behalten.

**Angezeigte Elemente:**
- Liste aller erfassten Bewerbungen
- Firmenname und Position pro Eintrag
- Aktueller Status mit farbigem Badge (geplant, beworben, eingeladen, etc.)
- Bewerbungsdatum
- Gesprächstermin falls vorhanden
- Statistik-Übersicht: Wie viele Bewerbungen in welchem Status

**Mögliche Aktionen:**
- Neue Bewerbung erfassen
- Bestehende Bewerbung anklicken zum Bearbeiten
- Status schnell ändern über Dropdown
- Bewerbung löschen
- Nach Status filtern

---

#### Bewerbung erstellen
![Student Bewerbung erstellen](doc/student/schueler-bewerbung-erstellen.png)

Formular zum Erfassen einer neuen Bewerbung.

**Formularfelder:**
- Firmenname (Pflichtfeld)
- Position/Stelle (Pflichtfeld)
- Status (Dropdown: geplant, beworben, etc.)
- Bewerbungsdatum
- Gesprächstermin (optional)
- Notizen (Freitextfeld für persönliche Anmerkungen)

**Mögliche Aktionen:**
- Formular ausfüllen und speichern
- Abbrechen und zurück zur Übersicht

---

#### Bewerbung bearbeiten
![Student Bewerbung bearbeiten](doc/student/schueler-bewerbung-bearbeiten.png)

Bearbeiten einer bestehenden Bewerbung mit vorausgefüllten Feldern.

**Angezeigte Elemente:**
- Gleiches Formular wie beim Erstellen
- Alle Felder mit aktuellen Werten vorausgefüllt
- Erstellungsdatum und letzte Änderung

**Mögliche Aktionen:**
- Felder bearbeiten und Änderungen speichern
- Bewerbung löschen
- Abbrechen ohne Speichern

---

#### Bewerbungsstatus ändern
![Student Bewerbungsstatus ändern](doc/student/schueler-bewerbungsstatus-aendern.png)

Schnelles Ändern des Bewerbungsstatus direkt aus der Übersicht.

**Angezeigte Elemente:**
- Dropdown-Menü mit allen möglichen Status
- Aktueller Status vorausgewählt

**Verfügbare Status:**
- PLANNED (Geplant)
- APPLIED (Beworben)
- INVITED (Eingeladen)
- INTERVIEW_DONE (Gespräch absolviert)
- ACCEPTED (Zusage)
- REJECTED (Absage)
- WITHDRAWN (Zurückgezogen)

---

#### Notizen-Übersicht
![Student Notizen Übersicht](doc/student/schueler-notizen-übersicht.png)

Persönliche Notizen zu Firmen, Stellen oder allgemeine Gedanken zur Berufswahl.

**Angezeigte Elemente:**
- Liste aller Notizen chronologisch sortiert
- Vorschau des Notizinhalts (erste Zeilen)
- Optional: Verknüpfte Firma/Position
- Erstellungsdatum

**Mögliche Aktionen:**
- Neue Notiz erstellen
- Notiz anklicken zum Bearbeiten
- Notiz löschen
- Nach Firma filtern

---

#### Notiz erstellen
![Student Notiz erstellen](doc/student/schueler-notiz-erstellen.png)

Formular zum Erstellen einer neuen Notiz.

**Formularfelder:**
- Firmenname (optional - zur Verknüpfung mit Bewerbung)
- Position (optional)
- Notiztext (Pflichtfeld, mehrzeiliges Textfeld)

**Mögliche Aktionen:**
- Notiz speichern
- Abbrechen

**Anwendungsbeispiele:**
- Infos zu einer Firma nach der Recherche festhalten
- Fragen für ein Vorstellungsgespräch notieren
- Reflexion nach einem Gespräch

---

#### Badges-Übersicht
![Student Badges Übersicht](doc/student/schueler-badges-uebersicht.png)

Das Gamification-Element der Anwendung: 37 Badges in 7 Kategorien motivieren zur regelmässigen Nutzung.

**Angezeigte Elemente:**
- Alle Badges gruppiert nach Kategorie
- Verdiente Badges farbig mit Emoji-Icon
- Noch nicht verdiente Badges ausgegraut
- Badge-Titel und Beschreibung
- Fortschrittsanzeige pro Kategorie

**Badge-Kategorien:**
- KI-Training (Sessions abschliessen)
- Notizen (Notizen erstellen)
- Bewerbungen (Bewerbungen erfassen, Status erreichen)
- Meilensteine (Erste Woche aktiv, etc.)
- Abgaben (Aufgaben einreichen)
- Feedback (Bewertungen erhalten)
- Noten (Gute Bewertungen erhalten)

**Mögliche Aktionen:**
- Badge anklicken für Details (was muss man tun um es zu verdienen)
- Durch Kategorien navigieren

---

### Teacher-Ansichten

#### Dashboard
![Teacher Dashboard](doc/teacher/teacher-dashboard.png)

Die Startseite für Lehrkräfte mit Fokus auf Klassenmanagement und offene Bewertungen.

**Angezeigte Elemente:**
- Anzahl der verwalteten Klassen
- Gesamtzahl der Schüler:innen
- Anzahl offener Abgaben die bewertet werden müssen (prominent)
- Liste der neuesten Abgaben mit Schülername und Aufgabe
- Quick-Actions für häufige Tätigkeiten

**Mögliche Aktionen:**
- Direkt zu einer Abgabe springen zur Bewertung
- Neue Aufgabe erstellen
- Neue Klasse erstellen
- Zu Klassen- oder Aufgabenverwaltung wechseln

---

#### Klassen-Übersicht
![Teacher Klassen Übersicht](doc/teacher/teacher-klassen-uebersicht.png)

Verwaltung aller Klassen der Lehrkraft.

**Angezeigte Elemente:**
- Liste aller eigenen Klassen
- Klassenname (z.B. "INF2024a")
- Anzahl Schüler:innen pro Klasse
- Anzahl Aufgaben pro Klasse
- Erstellungsdatum

**Mögliche Aktionen:**
- Neue Klasse erstellen
- Klasse anklicken zum Bearbeiten
- Schüler:innen zur Klasse hinzufügen/entfernen
- Klasse löschen (nur wenn keine Aufgaben)

---

#### Klasse erstellen
![Teacher Klasse erstellen](doc/teacher/teacher-klasse-erstellen.png)

Formular zum Erstellen einer neuen Schulklasse.

**Formularfelder:**
- Klassenname (Pflichtfeld, z.B. "KV2024b")
- Schüler-Emails (mehrzeilig, eine Email pro Zeile)

**Mögliche Aktionen:**
- Klasse mit Schüler:innen erstellen
- Weitere Schüler:innen später hinzufügen
- Abbrechen

**Hinweis:** Schüler:innen werden über ihre Email-Adresse identifiziert. Sobald sich ein Schüler mit dieser Email bei Auth0 registriert, wird er automatisch der Klasse zugeordnet.

---

#### Aufgaben-Übersicht
![Teacher Aufgaben Übersicht](doc/teacher/teacher-aufgaben-uebersicht.png)

Alle erstellten Aufgaben mit Abgabe-Status.

**Angezeigte Elemente:**
- Liste aller eigenen Aufgaben
- Aufgabentitel und Typ
- Zugewiesene Klasse
- Deadline
- Abgabe-Fortschritt: "X von Y Schüler:innen haben abgegeben"
- Status (aktiv, geschlossen)

**Mögliche Aktionen:**
- Neue Aufgabe erstellen
- Aufgabe anklicken für Detailansicht
- Aufgabe bearbeiten
- Aufgabe schliessen (keine Abgaben mehr möglich)
- Nach Klasse filtern

---

#### Aufgabe erstellen
![Teacher Aufgabe erstellen](doc/teacher/teacher-aufgabe-erstellen.png)

Formular zum Erstellen einer neuen Aufgabe für eine Klasse.

**Formularfelder:**
- Titel (Pflichtfeld)
- Beschreibung (Pflichtfeld, erklärt was die Schüler:innen tun sollen)
- Aufgabentyp (Dropdown):
  - KI-Bewerbungsgespräch
  - Dokument einreichen
  - Selbstreflexion
  - Video-Bewerbung
  - Recherche
- Klasse (Dropdown mit eigenen Klassen)
- Deadline (Datum und Uhrzeit)
- Geschätzte Dauer in Minuten

**Mögliche Aktionen:**
- Aufgabe erstellen und der Klasse zuweisen
- Abbrechen

**Hinweis:** Nach dem Erstellen sehen alle Schüler:innen der Klasse die Aufgabe in ihrer Aufgabenliste.

---

#### Aufgaben-Detail / Abgaben einsehen
![Teacher Aufgaben Detail](doc/teacher/teacher-aufgaben-detail.png)

Detailansicht einer Aufgabe mit allen eingereichten Abgaben.

**Angezeigte Elemente:**
- Aufgaben-Informationen (Titel, Beschreibung, Typ, Deadline)
- Liste aller Abgaben:
  - Schülername
  - Abgabezeitpunkt
  - Status (eingereicht, bewertet)
  - Note falls bereits bewertet
- Schüler:innen die noch nicht abgegeben haben

**Mögliche Aktionen:**
- Abgabe anklicken zum Bewerten
- Aufgabe bearbeiten
- Aufgabe schliessen
- Alle Schüler:innen ohne Abgabe sehen

---

#### Feedback geben
![Teacher Feedback geben](doc/teacher/teacher-aufgaben-feedback-geben.png)

Die Bewertungs-Ansicht für eine einzelne Schüler-Abgabe.

**Angezeigte Elemente:**
- Schülername und Klasse
- Aufgabentitel und Typ
- **Abgabe-Inhalt je nach Typ:**
  - KI-Interview: Vollständiger Chat-Verlauf
  - Dokument: Download-Link
  - Selbstreflexion: Eingereichter Text
  - Recherche: Text und Links
- Abgabezeitpunkt
- Bisheriges Feedback (falls vorhanden)

**Formularfelder:**
- Note/Punkte (Zahlenfeld)
- Schriftliches Feedback (Textfeld)

**Mögliche Aktionen:**
- Feedback eingeben und speichern
- Note vergeben
- Chat-Verlauf bei KI-Interview vollständig durchlesen
- Dokument herunterladen bei Upload-Aufgaben
- Zurück zur Aufgaben-Detailansicht

---

## KI-Funktionen

### Architektur

Die KI-Integration basiert auf **Spring AI** mit dem OpenAI GPT-4 Modell. Die Kommunikation erfolgt über einen ChatClient mit Memory-Funktion.

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Frontend  │────▶│ Spring Boot  │────▶│  OpenAI API │
│  (Svelte)   │◀────│   Backend    │◀────│   (GPT-4)   │
└─────────────┘     └──────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   MongoDB   │
                    │  (Sessions) │
                    └─────────────┘
```

### System-Prompts

Die KI verwendet zwei verschiedene System-Prompts je nach Kontext:

#### 1. Freies Training (SYSTEM_PROMPT)
Für ungebundenes Üben ohne Aufgabe. Der KI-Coach bietet zwei Modi:

**Modus 1: Interview-Training**
- Fragt nach Beruf und Firma
- Stellt realistische HR-Fragen aus verschiedenen Kategorien:
  - Motivation & Berufswahl
  - Persönlichkeit & Stärken
  - Arbeitsweise & Teamfähigkeit
  - Praktische Erfahrung
- Streut kreative "Culture Fit" Fragen ein (z.B. "Wenn du ein Tier wärst...")
- Gibt nach jeder Antwort konstruktives Feedback

**Modus 2: Bewerbungs-Beratung**
- Hilft bei Fragen zu Lebenslauf, Motivationsschreiben, Outfit, etc.
- Gibt praktische, umsetzbare Tipps

#### 2. Aufgaben-Modus (ASSIGNMENT_SYSTEM_PROMPT)
Für aufgabengebundenes Training mit definiertem Zeitrahmen:

- Strukturierter Ablauf mit 4-6 Fragen
- Zeitrahmen aus der Aufgabe übernommen
- Am Ende: Zusammenfassung mit Schulnote (1-6) und konkreten Tipps
- Fokus auf Effizienz und Bewertbarkeit

### Feedback-Mechanismus

Nach jeder Schüler-Antwort gibt die KI strukturiertes Feedback:

```
✓ Was war gut an der Antwort?
→ Was könnte verbessert werden?
💡 Was wollen HR-Leute bei dieser Frage hören?
```

### Chat-Memory

Die Konversationshistorie wird pro Session in MongoDB gespeichert. Bei jeder neuen Nachricht wird der gesamte Verlauf an die KI gesendet, um kontextbezogene Antworten zu ermöglichen.

```java
List<Message> chatMessages = new ArrayList<>();
chatMessages.add(new SystemMessage(systemPrompt));

for (SessionMessage msg : messages) {
    if ("USER".equals(msg.getRole())) {
        chatMessages.add(new UserMessage(msg.getContent()));
    } else if ("ASSISTANT".equals(msg.getRole())) {
        chatMessages.add(new AssistantMessage(msg.getContent()));
    }
}

Prompt prompt = new Prompt(chatMessages);
return chatClient.prompt(prompt).call().content();
```

---

## Drittsysteme

### Auth0 (Authentifizierung)

**Zweck:** Sichere Benutzerauthentifizierung und Rollenverwaltung

**Integration:**
- OAuth2 Resource Server im Backend (JWT-Validierung)
- SvelteKit Auth-Integration im Frontend
- Rollen werden in Auth0 definiert: `STUDENT`, `TEACHER`

**Konfiguration (application.yml):**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://${AUTH0_DOMAIN}/
```

**Vorteile:**
- Kein eigenes User-Management nötig
- Sichere JWT-basierte Authentifizierung
- Einfache Rollenverwaltung über Auth0 Dashboard

---

### MongoDB Atlas (Datenbank)

**Zweck:** Persistente Datenspeicherung

**Collections:**
| Collection | Inhalt |
|------------|--------|
| `classes` | Schulklassen |
| `assignments` | Aufgaben |
| `submissions` | Abgaben |
| `sessions` | KI-Chat-Sessions |
| `applications` | Bewerbungen |
| `notes` | Notizen |
| `badges` | Badge-Definitionen |
| `user_badges` | Verdiente Badges |

**Konfiguration:**
```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
      database: Praesto
```

**Vorteile:**
- Flexible Schema-Struktur (NoSQL)
- Eingebettete Dokumente (z.B. Messages in Session)
- Cloud-hosted (Atlas) - kein Server-Management

---

### OpenAI API (KI)

**Zweck:** Generierung der KI-Coach-Antworten

**Modell:** GPT-4

**Integration via Spring AI:**
```yaml
spring:
  ai:
    openai:
      api-key: ${OPEN_AI_KEY}
```

**Features genutzt:**
- Chat Completions API
- System Messages für Persona/Verhalten
- Konversationshistorie für Kontext

**Vorteile:**
- State-of-the-art Sprachmodell
- Natürliche, kontextbezogene Antworten
- Einfache Integration via Spring AI

---

## Optionale Anforderungen

Die folgenden optionalen Anforderungen aus der Projektausschreibung wurden umgesetzt:

| Anforderung | Status | Beschreibung |
|-------------|--------|--------------|
| **Codeanalyse mit SonarQube** | ✅ | SonarQube-Integration für statische Code-Analyse |
| **Komplexes Datenmodell** | ✅ | 8 MongoDB-Collections, 6 Enums, eingebettete Dokumente, Beziehungen |
| **Komplexes Frontend** | ✅ | SvelteKit mit Svelte 5, rollenbasierte Views, 20+ Screens |
| **Zugriff auf Drittsysteme** | ✅ | Auth0 (Authentifizierung), OpenAI (KI), MongoDB Atlas (Datenbank) |
| **Backend mit MCP-Server** | ❌ | Nicht implementiert |
| **Komplexe Benutzerverwaltung** | ✅ | Auth0 mit Rollen (Student/Teacher), Klassenzugehörigkeit per Email |
| **Komplexe Abfragen auf der Datenbank** | ✅ | 20+ Repository-Methoden mit Filtern, Sortierung, Status-Abfragen |
| **Detaillierte Dokumentation auf GitHub** | ✅ | Issues, Project Board mit Roadmap |
| **Mehrere Branches sinnvoll verwendet** | 🔶 | Feature-Branches teilweise verwendet |
| **End-to-End Tests** | 🔶 | 26 Unit-/Integrationstests vorhanden, E2E-Tests ausstehend |

### SonarQube

![SonarQube Analyse](doc/sonar.png)

Die Codequalität wird mit SonarQube überwacht.

### Testabdeckung

26 Testklassen decken die wichtigsten Bereiche ab:

| Bereich | Testklassen |
|---------|-------------|
| **Controller** | 10 Tests (alle Endpoints) |
| **Services** | 9 Tests (Geschäftslogik) |
| **Config** | 2 Tests (OpenAI, JWT) |
| **Model/Utils** | 5 Tests (Validation, Enums, Exceptions) |

---

# Fazit

## Stand der Implementation

Die Kernfunktionalität von Praesto ist vollständig implementiert und einsatzbereit. Alle geplanten Features der ersten Iteration wurden umgesetzt.

### Vollständig implementiert ✅

| Bereich | Features |
|---------|----------|
| **Authentifizierung** | Login via Auth0, rollenbasierte Ansichten (Student/Teacher) |
| **KI-Training** | Freies Training, aufgabengebundenes Training, zwei System-Prompts, Feedback nach jeder Antwort |
| **Klassenverwaltung** | Klassen erstellen, Schüler per Email hinzufügen/entfernen |
| **Aufgabensystem** | 5 Aufgabentypen, Deadlines, Status-Tracking |
| **Abgaben** | KI-Interview als Abgabe einreichen, Lehrkraft-Feedback, Bewertung |
| **Bewerbungs-Tracker** | CRUD, 7 Status-Stufen, Statistiken |
| **Notizen** | Persönliche Notizen mit Firmen-/Stellen-Verknüpfung |
| **Gamification** | 37 Badges in 7 Kategorien, automatische Vergabe |
| **Dashboard** | Übersicht für beide Rollen mit Quick-Actions |

### Teilweise implementiert 🔶

| Feature | Status | Details |
|---------|--------|---------|
| **Dokument-Upload** | Backend vorbereitet | Datenmodell vorhanden, UI-Integration ausstehend |
| **Video-Upload** | Backend vorbereitet | Datenmodell vorhanden, UI-Integration ausstehend |

---

## Nächste Schritte

Die folgenden Features sind für zukünftige Iterationen geplant und werden im GitHub Project Backlog geführt.

### Priorität 1: Email-Benachrichtigungen 📧

**Beschreibung:** Integration eines Mail-Services für automatische Benachrichtigungen.

**Geplante Trigger:**
| Ereignis | Empfänger | Inhalt |
|----------|-----------|--------|
| Neue Aufgabe erstellt | Student | "Du hast eine neue Aufgabe: [Titel]" |
| Abgabe eingereicht | Teacher | "[Schüler] hat [Aufgabe] abgegeben" |
| Feedback erhalten | Student | "Du hast Feedback zu [Aufgabe] erhalten" |
| Deadline-Erinnerung | Student | "Aufgabe [Titel] ist in 24h fällig" |

**Technologie:** Spring Boot Mail Starter + SMTP-Provider (z.B. SendGrid, Mailgun)

---

### Priorität 2: Dokument-Upload & -Download 📄

**Beschreibung:** Vollständige Implementation des Datei-Uploads für Aufgabentypen DOCUMENT_UPLOAD und VIDEO_PITCH.

**Geplante Features:**
- Schüler:in kann PDF/Word/Video hochladen
- Lehrkraft kann Dateien herunterladen
- Lehrkraft kann korrigierte Version hochladen
- Vorschau im Browser (PDF)

**Technologie:** 
- File Storage: MongoDB GridFS oder Cloud Storage (S3/Azure Blob)
- Max. Dateigrösse: 50 MB (Dokumente), 200 MB (Videos)

---

### Priorität 3: Erweiterte Features 🚀

| Feature | Beschreibung |
|---------|--------------|
| **Voice-Chat** | Spracheingabe für realistischere Gesprächssimulation |
| **Firmen-Datenbank** | Automatische Infos zu Schweizer Lehrbetrieben |
| **Barrierefreiheit** | Text-to-Speech für Schüler:innen mit Leseschwäche |
| **Analytics** | Detaillierte Fortschritts-Statistiken für Lehrkräfte |

---

## Backlog

Das Projekt wurde in mehreren Iterationen im GitHub Project geplant und umgesetzt. Alle geplanten Issues wurden erfolgreich abgeschlossen. Die oben genannten nächsten Schritte (Email-Service, Dokument-Upload) sind Ideen für zukünftige Weiterentwicklungen nach Projektabschluss.

---

## Reflexion

### Was gut funktioniert hat
- **Spring AI Integration:** Die Anbindung an OpenAI via Spring AI war straightforward und ermöglicht flexible Prompt-Gestaltung
- **MongoDB:** Das flexible Schema passte gut zu den unterschiedlichen Abgabetypen
- **SvelteKit:** Schnelle Entwicklung des Frontends mit guter Developer Experience
- **Auth0:** Sichere Authentifizierung ohne eigene User-Verwaltung

### Lessons Learned
- **System-Prompts:** Iteratives Verfeinern der Prompts war nötig, um realistische HR-Fragen und gutes Feedback zu erhalten
- **Gamification:** Die Badge-Logik sollte von Anfang an gut durchdacht sein - nachträgliche Änderungen der Kriterien können frustrierend für User sein

### Ausblick
Praesto hat das Potenzial, Schüler:innen auf dem Weg zur ersten Lehrstelle wirksam zu unterstützen. Die nächsten Schritte (Email-Service, Datei-Upload) werden die Anwendung abrunden und den Nutzen für Lehrkräfte weiter steigern.
