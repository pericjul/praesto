# Praesto – Ausführlicher Testplan (Gruppentest)

Stand: 2026-06-29 · Umgebung: produktive Azure-App
`https://praesto-app-gjabfafaauh7cfgj.switzerlandnorth-01.azurewebsites.net`

> Ziel: Vor dem echten Schuleinsatz prüfen wir mit einer Testgruppe **jede Rolle und jede Kernfunktion** durch – Schritt für Schritt, in der richtigen Reihenfolge. Jede:r weiss damit **wer was wann genau macht**.

**Wichtig zur Reihenfolge:** Die Rollen hängen voneinander ab. Eine Schüler:in kann z. B. erst eine Aufgabe lösen, **nachdem** die Lehrperson sie erstellt hat. Darum ist dieser Plan in **Phasen** gegliedert. Bitte die Phasen **der Reihe nach** durchlaufen – nicht vorgreifen.

**Legende für jede Prüfzeile:** `[ ]` = noch offen · `[✓]` = funktioniert · `[✗]` = Fehler (bitte unten in Abschnitt 10 eintragen).

---

## 1. Rollen & Wer macht was

| Rolle | Wer (Name eintragen) | Gerät | Hauptaufgabe im Test |
|-------|----------------------|-------|----------------------|
| **Super-Admin** | ________________ | Laptop | Schule + Schulleitung anlegen, Logs prüfen |
| **Schulleitung (Admin)** | ________________ | Laptop | Lehrperson(en) anlegen |
| **Lehrperson** | ________________ | Laptop | Klasse, Einladungen, Aufgaben, Cockpit |
| **Schüler:in A** | ________________ | **Handy** | Training, Aufgabe, Dossier (Handy-Fokus!) |
| **Schüler:in B** | ________________ | Laptop/Tablet | Training, Aufgabe, Dossier |
| **Demo-Besucher:in** | ________________ | beliebig | Öffentliche Seite + Demo-Modus |

> Mindestens **1 Schüler:in testet auf dem Handy** – der KI-Übungschat muss dort sauber passen.

**Zugänge (vorab eintragen, NICHT in Chats/E-Mails teilen):**
- App-URL: siehe oben.
- Super-Admin-Login: E-Mail `__________`  ·  Passwort liegt sicher bei Julia.
- Schulleitung/Lehrer/Schüler werden **im Test selbst angelegt** (Phasen A–D).

---

## 2. Phase A – Setup (Super-Admin) · *ganz am Anfang, ~10 Min.*

**Wer:** Super-Admin · **Wann:** zuerst, alle anderen warten.

| # | Schritt (genau) | Beispiel-Eingabe | Erwartetes Ergebnis | Status |
|---|-----------------|------------------|---------------------|--------|
| A1 | App öffnen, mit Super-Admin einloggen | – | Landet auf Super-Admin-Dashboard | [ ] |
| A2 | Menü **„Schulen"** → neue Schule anlegen | Name: „Testschule", Ort: „Zürich" | Schule erscheint in Liste | [ ] |
| A3 | Zur neuen Schule eine **Schulleitung (Admin)** anlegen | Vorname/Name, E-Mail `admin-test@…`, Passwort | Admin-Account erstellt, in Liste sichtbar | [ ] |
| A4 | Menü **„📜 Logs"** öffnen | – | Log-Liste lädt, **keine** Zeile „Using generated security password" | [ ] |
| A5 | Menü **„Anfragen"** / **„Demo-Anfragen"** öffnen | – | Seiten laden ohne Fehler (auch wenn leer) | [ ] |

➡️ **Übergabe:** Super-Admin gibt der Schulleitung deren Login (A3) sicher weiter.

---

## 3. Phase B – Schulleitung legt Lehrperson an · *~5 Min.*

**Wer:** Schulleitung (Admin) · **Wann:** nach Phase A.

| # | Schritt | Beispiel-Eingabe | Erwartetes Ergebnis | Status |
|---|---------|------------------|---------------------|--------|
| B1 | Mit Admin-Login (aus A3) einloggen | – | Admin-Dashboard erscheint | [ ] |
| B2 | **„Benutzer"** → neue **Lehrperson** anlegen | Vorname/Name, E-Mail `lehrer-test@…`, Passwort | Lehrer-Account erstellt | [ ] |
| B3 | Prüfen: Lehrperson erscheint in der Benutzerliste der **eigenen** Schule | – | Sichtbar; **keine** fremden Schulen sichtbar | [ ] |

➡️ **Übergabe:** Admin gibt der Lehrperson deren Login sicher weiter.

---

## 4. Phase C – Lehrperson: Klasse & Einladungen · *~10 Min.*

**Wer:** Lehrperson · **Wann:** nach Phase B. **Schüler:innen stehen bereit (Handy/Laptop).**

| # | Schritt | Beispiel-Eingabe | Erwartetes Ergebnis | Status |
|---|---------|------------------|---------------------|--------|
| C1 | Mit Lehrer-Login einloggen | – | Lehrer-Dashboard erscheint | [ ] |
| C2 | **„Klassen"** → neue Klasse erstellen | „Testklasse 3a" | Klasse erscheint | [ ] |
| C3 | In der Klasse **„Schüler einladen"** öffnen | – | Einladungslink **und QR-Code** werden angezeigt | [ ] |
| C4 | Einladungslink kopieren / QR der Schüler:in A (Handy) zeigen | – | – | [ ] |

➡️ **Übergabe:** Link an Schüler:in B senden, QR der Schüler:in A zum Scannen zeigen.

---

## 5. Phase D – Schüler:innen registrieren sich · *~5 Min.*

**Wer:** Schüler:in A (Handy) + B · **Wann:** nach Phase C.

| # | Schritt | Erwartetes Ergebnis | Status |
|---|---------|---------------------|--------|
| D1 | A: QR scannen **/** B: Link öffnen | Selbst-Registrierungs-Seite der „Testklasse 3a" öffnet | [ ] |
| D2 | Eigenen Account anlegen (Vorname, Name, E-Mail, Passwort) | Konto erstellt, automatisch eingeloggt → Schüler-Dashboard | [ ] |
| D3 | Lehrperson prüft in **„Klassen"**: beide Schüler:innen sind in der Klasse | Beide sichtbar | [ ] |

---

## 6. Phase E – Schüler:in: Kernfunktionen · *~20 Min.*

**Wer:** Schüler:in A (**Handy**) + B (Laptop) · **Wann:** nach Phase D. Beide machen dieselben Schritte; A achtet zusätzlich auf **Handy-Darstellung**.

### E1 Dashboard
| # | Schritt | Erwartetes Ergebnis | Status |
|---|---------|---------------------|--------|
| E1.1 | Dashboard ansehen | Kalender + Übersicht sichtbar, **nichts läuft über den Rand** (Handy) | [ ] |

### E2 KI-Übungschat (freies Üben) — **Handy-Schwerpunkt**
| # | Schritt | Erwartetes Ergebnis | Status |
|---|---------|---------------------|--------|
| E2.1 | **„Training"** → freies Üben starten | Chat öffnet, KI begrüsst | [ ] |
| E2.2 | **Auf dem Handy:** Layout prüfen | Header, Eingabefeld, Senden-Button **passen ganz in den Screen**, kein seitliches Scrollen, Text nicht abgeschnitten | [ ] |
| E2.3 | 3–4 Nachrichten schreiben und antworten | KI antwortet sinnvoll auf Deutsch | [ ] |
| E2.4 | „Roast-Modus" umschalten (falls sichtbar) und weiter chatten | Tonfall ändert sich, funktioniert | [ ] |
| E2.5 | Chat beenden → Bewertung ansehen | Prozent-Bewertung + Feedback erscheinen | [ ] |
| E2.6 | Sehr lange chatten / Zeit ablaufen lassen | Nach Limit **stoppt** der Chat sauber mit Hinweis (kein Endlos-Chat) | [ ] |

### E3 Aufgabe (kommt aus Phase F)
> Diese Schritte **erst nach Phase F1** ausführen.

| # | Schritt | Erwartetes Ergebnis | Status |
|---|---------|---------------------|--------|
| E3.1 | **„Aufgaben"** → die von der Lehrperson erstellte Aufgabe öffnen | Aufgabe samt Anweisung sichtbar | [ ] |
| E3.2 | Aufgaben-Interview starten und durchführen | Timer/Ziel sichtbar, Chat läuft | [ ] |
| E3.3 | Aufgabe abgeben | Status wechselt auf „abgegeben" | [ ] |
| E3.4 | Versuchen, **3.** Aufgaben-Interview in derselben Woche zu starten | Wird **blockiert** (max. 2/Woche) mit Hinweis | [ ] |

### E4 Dossier & Dokumente
| # | Schritt | Beispiel-Eingabe | Erwartetes Ergebnis | Status |
|---|---------|------------------|---------------------|--------|
| E4.1 | **„Dossier"** öffnen | – | Ordner/Übersicht lädt | [ ] |
| E4.2 | Datei hochladen (z. B. PDF) | beliebiges PDF | Datei erscheint, lässt sich wieder herunterladen | [ ] |
| E4.3 | **„Lebenslauf erstellen"** – Formular ausfüllen | Name, Adresse, Schulbildung, Sprachen … | Word-Datei wird erzeugt & landet im Dossier; Inhalt stimmt mit Eingaben überein (**ohne KI-Erfindungen**) | [ ] |
| E4.4 | **„Bewerbungsschreiben"** – Formular ausfüllen | Firma, Beruf, Stärken mit Beispiel | KI erzeugt sauberen Brief (CH-Form, „ss" statt „ß", nichts erfunden) | [ ] |
| E4.5 | KI-Kontingent ausreizen | mehrfach Brief erzeugen | Nach Limit klare Meldung, kein Absturz | [ ] |

### E5 Weitere Schüler-Funktionen
| # | Schritt | Erwartetes Ergebnis | Status |
|---|---------|---------------------|--------|
| E5.1 | **„Notizen"** anlegen/bearbeiten | Speichern & Wiederladen funktioniert | [ ] |
| E5.2 | **„Bewerbungen"** Eintrag mit Status/Deadline erfassen | Timeline/Deadline-Hinweis erscheint | [ ] |
| E5.3 | **„Badges"** ansehen | Badges laden **ohne Fehler** (auch nach mehreren Trainings) | [ ] |

---

## 7. Phase F – Lehrperson: Aufgaben & Auswertung · *~20 Min.*

**Wer:** Lehrperson · **Wann:** sobald Schüler:innen registriert sind (nach Phase D). **F1 muss vor E3 passieren.**

| # | Schritt | Beispiel-Eingabe | Erwartetes Ergebnis | Status |
|---|---------|------------------|---------------------|--------|
| F1 | **„Aufgaben"** → neue Aufgabe an „Testklasse 3a" | Titel, Anweisung, Ziel-Dauer | Aufgabe erstellt, bei Schüler:innen sichtbar | [ ] |
| F2 | Aufgabe an **mehrere** Klassen gleichzeitig (falls 2. Klasse vorhanden) | – | Erscheint in beiden | [ ] |
| F3 | **„Cockpit"** öffnen | – | Reife-/Fortschritts-Übersicht der Klasse lädt | [ ] |
| F4 | Abgegebene Aufgabe einer Schüler:in öffnen | – | Verlauf + Bewertung sichtbar | [ ] |
| F5 | **Feedback-Baustein** an Schüler:in geben | – | Feedback wird gespeichert/angezeigt | [ ] |
| F6 | **Gesprächsleitfaden** generieren | – | Leitfaden erscheint | [ ] |
| F7 | **1-Klick-Klassen-Challenge** starten | – | Challenge aktiv, bei Schüler:innen sichtbar | [ ] |
| F8 | **CSV-Export** herunterladen | – | CSV lädt, Spalten korrekt befüllt | [ ] |
| F9 | Prüfen: Lehrperson sieht **nur** die eigene Klasse/Schule | – | Keine fremden Daten | [ ] |

---

## 8. Phase G – Demo & öffentliche Seiten · *jederzeit, ~10 Min.*

**Wer:** Demo-Besucher:in · **Wann:** unabhängig.

| # | Schritt | Erwartetes Ergebnis | Status |
|---|---------|---------------------|--------|
| G1 | Öffentliche Startseite öffnen | Lädt sauber, auch auf dem Handy | [ ] |
| G2 | **Kontaktformular** absenden | Bestätigung; erscheint später beim Super-Admin unter „Anfragen" | [ ] |
| G3 | **Demo anfordern** / Demo-Login | Demo-Modus startet | [ ] |
| G4 | Im Demo etwas **ändern/anlegen** versuchen (z. B. speichern) | Klare Meldung „im Demo-Modus nicht verfügbar" – **nichts** wird verändert | [ ] |
| G5 | Im Demo zwischen Rollen wechseln (Rollen-Umschalter) | Wechsel funktioniert **ohne Fehler** | [ ] |

---

## 9. Querschnitt – am Ende, alle Rollen · *~10 Min.*

| # | Prüfung | Erwartetes Ergebnis | Status |
|---|---------|---------------------|--------|
| Q1 | Sprache umschalten DE/EN/FR/IT | Texte wechseln, keine fehlenden Schlüssel (kein `schat.xyz`) | [ ] |
| Q2 | **Handy quer & hoch** auf 2–3 Geräten | Nichts läuft über den Rand, Buttons erreichbar | [ ] |
| Q3 | Logout → erneut Login | Sauberer Wechsel, richtige Rolle | [ ] |
| Q4 | Direkt fremde URL aufrufen (z. B. Schüler ruft `/teacher/...`) | Wird umgeleitet / kein Zugriff | [ ] |
| Q5 | Seite neu laden (F5) auf Dashboard, Chat, Dossier | Daten bleiben/laden korrekt | [ ] |

---

## 10. Bug-Erfassung (Vorlage – pro Fehler eine Zeile)

| # | Rolle | Schritt-Nr. | Gerät/Browser | Was passiert (Ist) | Was sollte sein (Soll) | Screenshot? |
|---|-------|-------------|---------------|--------------------|------------------------|-------------|
| 1 | | | | | | |
| 2 | | | | | | |
| 3 | | | | | | |

> Bei KI-Fehlern: ungefähre Uhrzeit notieren (hilft beim Nachschauen in „📜 Logs" / Azure-Logs).

---

## 11. Abnahmekriterien (wann ist der Test „bestanden")

Der Test gilt als bestanden, wenn:
- [ ] **Phasen A–F** komplett ohne blockierende Fehler durchlaufen wurden.
- [ ] Der **KI-Übungschat auf dem Handy** sauber in den Screen passt (E2.2).
- [ ] **Lebenslauf** (ohne KI) und **Bewerbungsschreiben** (mit KI) korrekt erzeugt werden.
- [ ] **Limits greifen**: Chat-Zeit-Stop, max. 30 Nachrichten, max. 2 Aufgaben-Interviews/Woche, KI-Kontingent.
- [ ] **Demo ist strikt read-only**, keine fremden Schul-Daten sichtbar (Mandanten-Trennung).
- [ ] In den **Logs keine** „generated security password"-Zeile und keine ungewollten Stacktraces.

---

## 12. Bekannte Punkte / Hinweise
- Erstes KI-Antworten kann kurz dauern (Modell „aufwärmen") – das ist normal.
- Nach einem neuen Deploy ggf. einmal hart neu laden (Cache).
- Support-/Rückfragen: **info@praesto.ch**.
