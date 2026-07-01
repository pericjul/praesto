# Praesto – Vollständiger Testplan (Pilot)

> Ziel: Jede Funktion jeder Rolle prüfen – inkl. **Nachweis**, dass Aktionen *wirklich* wirken
> (z. B. Passwort-Reset → mit neuem Passwort einloggen). Positive **und** negative Fälle.

## Vorbereitung
1. Neues Image in Azure gepinnt (Tag `734014a…`), `SEED_TEST_DATA=true`, App neu gestartet.
2. Im Azure **Log stream** offen lassen (Azure → Überwachung → Log stream) – dort siehst du jeden Aufruf
   (`API POST /api/... -> 200 (…ms) user=…`). Bei Problemen sofort erkennbar, wo es klemmt.
3. Tipp: In verschiedenen Browsern / privaten Fenstern einloggen, damit du mehrere Rollen parallel offen hast.

## Zugänge (Passwort für alle: `PilotTest2026!`)
| Rolle | E-Mail |
|---|---|
| Schulleitung | admin.test@praesto.ch |
| Lehrperson | lehrer.test@praesto.ch |
| Schüler 1 | schueler1.test@praesto.ch |
| Schüler 2 | schueler2.test@praesto.ch |
| Schüler 3 | schueler3.test@praesto.ch |
| Super-Admin | dein bestehendes Konto |

Legende: **Schritt** → was tun · **Erwartet** → was passieren soll · **Nachweis** → wie du beweist, dass es echt wirkte · **✅/❌** → dein Häkchen.

---

## 1) AUTHENTIFIZIERUNG & KONTO

| # | Schritt | Erwartet | Nachweis | ✅/❌ |
|---|---|---|---|---|
| 1.1 | Login mit richtigem Passwort | Weiterleitung aufs Dashboard | Dashboard mit Namen sichtbar | |
| 1.2 | Login mit **falschem** Passwort | Fehlermeldung „Login fehlgeschlagen" | Kein Zugang | |
| 1.3 | Login mit **falscher E-Mail** | Fehlermeldung | Kein Zugang | |
| 1.4 | 5× hintereinander falsch einloggen | Nach mehreren Versuchen **vorübergehend gesperrt** | Meldung „zu viele Versuche" | |
| 1.5 | Logout (oben rechts) | Zurück zum Login | Geschützte Seite aufrufen → Weiterleitung zu /login | |
| 1.6 | Sprache oben rechts auf **EN** | UI wird englisch | Menü „Dashboard/Tasks/Training…" englisch | |
| 1.7 | Konto → Vorname ändern → speichern | Gespeichert | Seite neu laden → neuer Name bleibt | |
| 1.8 | Konto → Passwort ändern → aus- und mit **neuem** Passwort einloggen | Login klappt nur mit neuem PW | Altes PW → Fehler; neues PW → Zugang | |

---

## 2) SCHÜLER:IN (schueler1.test)

### Training / KI
| # | Schritt | Erwartet | Nachweis | ✅/❌ |
|---|---|---|---|---|
| 2.1 | Training → „Neues Gespräch starten" | KI begrüsst und stellt eine Frage | Antwort erscheint im Chat | |
| 2.2 | Antwort schreiben → senden | KI antwortet inhaltlich passend | Neue KI-Nachricht | |
| 2.3 | Gespräch beenden / Zeit ablaufen lassen | **%-Chance** + Tipp erscheint | Prozentzahl sichtbar in der Gesprächsliste | |
| 2.4 | **Nochmal** „Neues Gespräch" (2. Mal heute) | ❌ „Kontingent aufgebraucht – morgen wieder" | Start blockiert | |
| 2.5 | Roast-Modus 🔥 einschalten → Gespräch | KI ist direkter/härter | Anderer Ton | |
| 2.6 | Sprache EN → neues Gespräch | KI antwortet **englisch** | Englische Begrüssung | |
| 2.7 | Historie: Schüler 1 hat 1 abgeschlossenes Gespräch | In der Liste sichtbar (mit 62 %) | Eintrag vorhanden | |

### Aufgaben
| # | Schritt | Erwartet | Nachweis | ✅/❌ |
|---|---|---|---|---|
| 2.8 | Aufgaben-Liste | 4 Aufgaben sichtbar | „Bewerbungsgespräch üben", „Überfällige Übung", „Selbstreflexion", „Lebenslauf hochladen" | |
| 2.9 | „Überfällige Übung" öffnen | Markierung ⚠️ **Überfällig** | Rotes „Überfällig" | |
| 2.10 | „Bewerbungsgespräch üben (KI)" → **Interview starten** | Chat öffnet **ohne 403-Fehler** | Chat lädt | |
| 2.11 | Im Interview chatten → **Abgeben** | Danach „Abgegeben" | Aufgabe zeigt Status „Abgegeben" | |
| 2.12 | „Selbstreflexion" | zeigt **„Abgegeben"** (schon abgegeben), wartet auf Bewertung | Status sichtbar | |
| 2.13 | „Lebenslauf hochladen" → PDF hochladen → abgeben | Datei abgegeben | „Abgegeben" + Dateiname | |

### Dossier
| # | Schritt | Erwartet | Nachweis | ✅/❌ |
|---|---|---|---|---|
| 2.14 | Dossier → **Lebenslauf erstellen**, Felder ausfüllen, mit „+" Zeilen hinzufügen | Felder funktionieren | Zeilen kommen dazu | |
| 2.15 | Foto wählen → im 3:4-Rahmen **zuschneiden/zoomen** → erstellen | Word öffnet, Foto **nicht verzerrt** | Bild passt sauber in den Rahmen | |
| 2.16 | **Bewerbungsschreiben** ausfüllen (Firma/Adresse optional, Pensum, Datum) → erstellen | Sauberer CH-Brief, **fetter Betreff** | Word öffnet formatiert | |
| 2.17 | Dokument **herunterladen** | Datei öffnet | Öffnet in Word | |
| 2.18 | Dokument **löschen** 🗑️ → bestätigen | Verschwindet | Nach Neuladen weg | |
| 2.19 | Warnhinweis „nur Vorlage" sichtbar? | Oranger Hinweis auf der Seite | Text sichtbar | |

### Notizen / Bewerbungen / Badges / Bug
| # | Schritt | Erwartet | Nachweis | ✅/❌ |
|---|---|---|---|---|
| 2.20 | Notizen | Schüler 1 hat 1 Notiz (Muster AG) | Sichtbar; neue anlegen geht | |
| 2.21 | Bewerbungen | Schüler 1 hat 1 Bewerbung (Muster AG, „Beworben") | Timeline sichtbar | |
| 2.22 | Badges | Badges-Seite lädt | Übersicht sichtbar | |
| 2.23 | Bug melden → absenden | Bestätigung | Erscheint später beim Super-Admin unter „Bugs" | |

---

## 3) LEHRPERSON (lehrer.test)

| # | Schritt | Erwartet | Nachweis | ✅/❌ |
|---|---|---|---|---|
| 3.1 | Login → Lehrer-Dashboard | Übersicht lädt | | |
| 3.2 | Klassen → „Testklasse 3T" | 3 Schüler sichtbar | | |
| 3.3 | **Einladungslink / QR** anzeigen | Link + QR erscheinen | | |
| 3.4 | Schüler suchen + hinzufügen (falls ein weiterer existiert) | Wird der Klasse hinzugefügt | Erscheint in der Liste | |
| 3.5 | **📝 Einverständnis** bei Schüler 1 abhaken | Wird grün (📝✓), Zähler 1/3 | **Seite neu laden → bleibt grün** (echt gespeichert) | |
| 3.6 | **🔑 Passwort zurücksetzen** bei **Schüler 3** → neues PW z. B. `NeuTest123!` | Bestätigung | **NACHWEIS:** ausloggen → als schueler3.test mit **altem** PW → ❌ Fehler; mit **neuem** PW `NeuTest123!` → ✅ Login. Damit ist bewiesen, dass es wirklich geändert wurde. | |
| 3.7 | **✕ Schüler entfernen** (Testklasse) → bestätigen | Rückfrage, dann entfernt | Nach Neuladen nicht mehr in der Klasse; Konto bleibt (unter „Benutzer" beim Admin noch da) | |
| 3.8 | Aufgaben → 4 Aufgaben sichtbar | Liste vollständig | | |
| 3.9 | **Neue Aufgabe**: Titel, **Klasse-Dropdown**, Typ wählen → **Erklärung** erscheint, Deadline (Zukunft), Beschreibung → Erstellen | Aufgabe erscheint bei den Schülern | Als Schüler einloggen → neue Aufgabe da | |
| 3.10 | Neue Aufgabe mit **Deadline in der Vergangenheit** | ❌ „Deadline muss in der Zukunft liegen" | Nicht erstellt | |
| 3.11 | Neue Aufgabe **ohne Titel/Klasse** | ❌ Fehlermeldung | Nicht erstellt | |
| 3.12 | „Kurze Selbstreflexion" öffnen → Abgabe von Schüler 1 sehen → **Feedback + Note** → speichern | Status „Bewertet" | **NACHWEIS:** als schueler1.test einloggen → Feedback + Note sichtbar | |
| 3.13 | **CSV-Export** einer Aufgabe | Datei lädt herunter | Öffnet in Excel | |
| 3.14 | Cockpit | Reife-Übersicht der Klasse | | |
| 3.15 | Challenge → 1-Klick starten | Challenge aktiv | | |
| 3.16 | **Fremde Klasse?** (nur eine Testklasse vorhanden) | Lehrer sieht nur eigene Klassen | Keine fremden Klassen | |

---

## 4) SCHULLEITUNG (admin.test)

| # | Schritt | Erwartet | Nachweis | ✅/❌ |
|---|---|---|---|---|
| 4.1 | Login → Admin-Dashboard | Kennzahlen: 1 Lehrer, 3 Schüler, 1 Klasse, 4 Aufgaben | | |
| 4.2 | **Lehrer einladen** → Link erstellen | Link erscheint | | |
| 4.3 | **Benutzer** → nach Rolle filtern | Nur passende Rolle sichtbar | | |
| 4.4 | **🔑 Passwort** eines Schülers zurücksetzen (neues PW) | Bestätigung | **NACHWEIS:** mit neuem PW einloggen → klappt; altes → Fehler | |
| 4.5 | **Konto deaktivieren** bei Schüler 2 | Deaktiviert | **NACHWEIS:** als schueler2.test einloggen → ❌ „Account ist deaktiviert" | |
| 4.6 | Schüler 2 wieder **aktivieren** | Aktiv | Login klappt wieder | |
| 4.7 | Fehlerseite: als Admin **„Benutzer"** öffnen | Liste lädt (kein 500) | Alle Nutzer sichtbar | |

---

## 5) SUPER-ADMIN (dein Konto)

| # | Schritt | Erwartet | Nachweis | ✅/❌ |
|---|---|---|---|---|
| 5.1 | Schulen | „Testschule Pilot" sichtbar | | |
| 5.2 | **Nutzerübersicht** → Filter „Testschule Pilot" | Alle 5 Test-Accounts, Spalte „zuletzt aktiv" | | |
| 5.3 | Filter „nur inaktiv > 1 Jahr" | Test-Accounts erscheinen **nicht** (heute aktiv) | Liste leer/kurz | |
| 5.4 | Schule **🔒 sperren** | Login der Schule blockiert | **NACHWEIS:** als lehrer.test → ❌ „Schule gesperrt". Danach **🔓 entsperren** → Login klappt wieder | |
| 5.5 | **Offerte** erstellen: Empfänger + Positionen mit „+" + Preise → herunterladen | Word öffnet, Werte + Totale korrekt | Zwischentotal/MwSt/Total stimmen | |
| 5.6 | Bug-Meldungen | Der zuvor gemeldete Bug (2.23) erscheint | Eintrag sichtbar | |
| 5.7 | Nutzer **Daten exportieren** | Datei lädt | Enthält die Daten der Person | |

---

## 6) DATENTRENNUNG & SICHERHEIT (wichtig)

| # | Schritt | Erwartet | ✅/❌ |
|---|---|---|---|
| 6.1 | Als schueler1.test eingeloggt: URL einer fremden Aufgabe/Session manuell aufrufen (falls du eine ID hast) | Kein Zugriff auf fremde Daten (Fehler/leer) | |
| 6.2 | Lehrer sieht nur **eigene** Klassen/Schüler | Keine fremden Schulen | |
| 6.3 | Nach Logout geschützte Seite aufrufen | Weiterleitung zu /login | |
| 6.4 | Offline testen: WLAN aus → Aktion versuchen | Oben erscheint **Offline-Banner**, kein Absturz | |

---

## 7) NACH DEM TEST
- Alles wieder in den Ausgangszustand: gesperrte Schule **entsperren**, deaktivierte Konten **aktivieren**, geänderte Passwörter notieren.
- `SEED_TEST_DATA` in Azure auf `false` setzen (Test-Daten bleiben trotzdem bestehen).
- Optional: „Testschule Pilot" als Super-Admin komplett löschen, wenn nicht mehr gebraucht.

## Ergebnis
- [ ] Alle ✅ → bereit für die echte Klasse.
- Bei ❌: Schritt-Nr. + Beobachtung + (falls sichtbar) Fehlermeldung notieren → an Entwickler. Im Azure-Log steht die passende `API … -> 4xx/5xx …`-Zeile.
