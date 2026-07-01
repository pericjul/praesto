<script>
    // Ausführliche, druckbare Anleitung (bewusst auf Deutsch, als Nachschlagewerk zum
    // Ausdrucken/als PDF). Alle Rollen, Funktionen und Buttons im Detail.
    function print() {
        window.print();
    }
</script>

<svelte:head>
    <title>Anleitung – Praesto</title>
</svelte:head>

<div class="doc-bg">
    <div class="actions no-print">
        <button class="btn" onclick={print}>📄 Als PDF speichern / drucken</button>
        <a class="back" href="/hilfe">← Zurück zur Hilfe</a>
    </div>

    <article class="sheet">
        <header class="doc-head">
            <div>
                <div class="logo">Praesto</div>
                <div class="sub">Ausführliche Anleitung · Stand laufend aktualisiert</div>
            </div>
            <div class="op">KI-Bewerbungscoach für die Lehrstellensuche<br />info@praesto.ch · praesto.ch</div>
        </header>

        <h1>Praesto – Anleitung &amp; Funktionsübersicht</h1>
        <p class="lead">Dieses Dokument beschreibt alle Rollen, Bereiche, Funktionen und Schaltflächen von Praesto.
        Es dient als Nachschlagewerk für Schüler:innen, Lehrpersonen und Schulleitungen.</p>

        <!-- 1 -->
        <section>
            <h2>1. Die Rollen</h2>
            <ul>
                <li><strong>Schüler:in</strong> – übt Bewerbungsgespräche mit der KI, löst Aufgaben und erstellt Bewerbungsunterlagen.</li>
                <li><strong>Lehrperson</strong> – erstellt Klassen, vergibt und bewertet Aufgaben, gibt Feedback und verwaltet das Einverständnis.</li>
                <li><strong>Schulleitung (Admin)</strong> – verwaltet die ganze Schule: lädt Lehrpersonen ein, sieht alle Nutzer:innen, setzt Passwörter zurück.</li>
                <li><strong>Demo</strong> – read-only Testzugang: alles ansehen, nichts verändern.</li>
            </ul>
        </section>

        <!-- 2 -->
        <section>
            <h2>2. Anmeldung, Sprache &amp; Konto</h2>
            <ul>
                <li><strong>Registrieren:</strong> über den Einladungslink der Schule/Klasse. Beim Erstellen des Kontos muss die Checkbox „Datenschutzerklärung und AGB gelesen und akzeptiert“ angehakt werden.</li>
                <li><strong>Login:</strong> mit E-Mail und Passwort. Passwort vergessen? An die Lehrperson bzw. Schulleitung wenden – sie können es zurücksetzen.</li>
                <li><strong>Sprache (oben rechts):</strong> Deutsch, Englisch, Französisch, Italienisch. Die KI antwortet in der gewählten Sprache.</li>
                <li><strong>Logout (oben rechts):</strong> meldet dich ab.</li>
                <li><strong>Konto:</strong> Profil (Vor-/Nachname) bearbeiten und Passwort ändern.</li>
            </ul>
        </section>

        <!-- 3 -->
        <section>
            <h2>3. Für Schüler:innen</h2>

            <h3>Dashboard</h3>
            <p>Startseite mit Begrüssung, deinem Fortschritt (Übungen, Badges, Streak) und einer täglichen Klassen-Info.</p>

            <h3>Training – KI-Bewerbungsgespräch</h3>
            <ul>
                <li><strong>„Neues Gespräch starten“</strong> – die KI stellt dir Bewerbungsfragen. Antworte in ganzen Sätzen.</li>
                <li><strong>🔥 Roast-Modus</strong> (Schalter) – die KI ist ehrlicher/härter. In Aufgaben ist er gesperrt.</li>
                <li><strong>Nachricht senden</strong> – deine Antwort abschicken.</li>
                <li><strong>Bewertung („Hiring chance“)</strong> – am Ende schätzt die KI deine Einstellungs-Chance in % mit einem Tipp.</li>
                <li><strong>Übungsgespräche:</strong> So viele du willst – jeder Chat dauert 15 Minuten. Gespräche zu Aufgaben zählen separat.</li>
            </ul>

            <h3>Aufgaben</h3>
            <p>Von der Lehrperson vergebene Aufträge mit Deadline. Fünf Typen:</p>
            <ul>
                <li><strong>🤖 KI-Interview</strong> – Gespräch mit der KI führen und mit „Abgeben“ einreichen.</li>
                <li><strong>📄 Dokument</strong> – eine Datei hochladen (z.B. Lebenslauf als PDF).</li>
                <li><strong>✍️ Reflexion</strong> – einen Text direkt in der App schreiben.</li>
                <li><strong>🎥 Video-Pitch</strong> – ein kurzes Bewerbungsvideo hochladen.</li>
                <li><strong>🔍 Recherche</strong> – Ergebnisse mit Text und Links dokumentieren.</li>
            </ul>

            <h3>Notizen</h3>
            <p><strong>„Neue Notiz“</strong> – wichtige Infos zu Firmen und deinem Bewerbungsprozess festhalten.</p>

            <h3>Bewerbungen</h3>
            <p>Zeitstrahl deiner laufenden Bewerbungen mit Status und Deadline-Hinweisen.</p>

            <h3>Dossier – Lebenslauf &amp; Bewerbungsschreiben</h3>
            <ul>
                <li><strong>Lebenslauf erstellen</strong> – Felder ausfüllen (Personalien, Familie, Schulen, Sprachen, Hobbys, Referenzen). Mehrfach-Felder mit „+ hinzufügen“. Foto lässt sich im 3:4-Rahmen passend zuschneiden.</li>
                <li><strong>Bewerbungsschreiben erstellen</strong> – Angaben eingeben (Firma/Adresse optional, Stelle, Pensum, Datum, Stärken …); die KI schreibt den Text, das Layout ist ein sauberer Schweizer Brief.</li>
                <li><strong>Upload</strong> – eigene Dokumente hochladen.</li>
                <li><strong>Herunterladen</strong> / <strong>🗑️ Löschen</strong> – je Dokument.</li>
                <li><strong>Wichtig:</strong> Die erstellten Dokumente sind Vorlagen/Inspiration – vor dem Abgeben selbst überarbeiten.</li>
            </ul>

            <h3>Badges</h3>
            <p>Auszeichnungen für Fortschritt und Fleiss (z.B. Übungs-Serien).</p>

            <h3>Bug melden</h3>
            <p><strong>„Bug absenden“</strong> – Fehler oder komische Anzeigen melden (Titel, Bereich, Beschreibung). In der Testphase mit Wettbewerb.</p>
        </section>

        <!-- 4 -->
        <section>
            <h2>4. Für Lehrpersonen</h2>

            <h3>Klassen</h3>
            <ul>
                <li><strong>„Neue Klasse“</strong> – Klasse anlegen (z.B. „3a“).</li>
                <li><strong>Einladungslink / QR-Code</strong> – im Unterricht zeigen; Schüler:innen treten selbst bei (Link 14 Tage gültig).</li>
                <li><strong>Schüler:in suchen &amp; hinzufügen</strong> – bereits registrierte Schüler:innen der Schule direkt einer Klasse zuordnen.</li>
                <li>Pro Schüler:in: <strong>📝 Einverständnis abhaken</strong> (unterschriebene Erklärung liegt vor), <strong>🔑 Passwort zurücksetzen</strong>, <strong>✕ aus Klasse entfernen</strong> (mit Rückfrage; Konto & Daten bleiben erhalten).</li>
                <li><strong>Einverständnis-Zähler</strong> pro Klasse (z.B. 18/22) – zeigt, wie viele Erklärungen vorliegen.</li>
            </ul>

            <h3>Aufgaben</h3>
            <ul>
                <li><strong>„Neue Aufgabe“</strong> – Titel, Klasse (Dropdown, eine pro Aufgabe), Typ (mit Kurz-Erklärung), Deadline, Beschreibung.</li>
                <li><strong>Bewerten &amp; Feedback</strong> – Abgaben öffnen, Feedback und optional Note geben; Textbausteine für häufiges Feedback.</li>
                <li><strong>CSV-Export</strong> – alle Abgaben und Noten einer Aufgabe als Datei.</li>
            </ul>

            <h3>Klassen-Cockpit</h3>
            <p>Übersicht über die „Bewerbungsreife“ der Klasse auf einen Blick.</p>

            <h3>Klassen-Challenge</h3>
            <p><strong>1-Klick-Challenge</strong> – eine gemeinsame Übungs-Herausforderung für die Klasse starten.</p>

            <h3>Gesprächsleitfaden &amp; Sessions</h3>
            <p>Automatischer Leitfaden für Übungsgespräche; einzelne Schüler-Gespräche einsehen.</p>
        </section>

        <!-- 5 -->
        <section>
            <h2>5. Für die Schulleitung</h2>
            <ul>
                <li><strong>Dashboard/Kennzahlen</strong> – Anzahl Lehrpersonen, Schüler:innen, Klassen und Aufgaben.</li>
                <li><strong>Lehrpersonen einladen</strong> – einen Einladungslink erstellen (30 Tage gültig) und ans Kollegium schicken – alle nutzen denselben Link.</li>
                <li><strong>Benutzer</strong> – alle Lehrpersonen und Schüler:innen der Schule; nach Name/Rolle filtern.</li>
                <li><strong>🔑 Passwort zurücksetzen</strong> und <strong>Konto deaktivieren</strong> – bei Bedarf.</li>
                <li><strong>Demo-Zugang</strong> – Praesto unverbindlich und nur lesend ausprobieren.</li>
            </ul>
        </section>

        <!-- 6 -->
        <section>
            <h2>6. Einverständnis &amp; Datenschutz</h2>
            <ul>
                <li><strong>Ablauf:</strong> Die Lehrperson verteilt die Einverständniserklärung (PDF unter praesto.ch/einverstaendnis), lässt sie von den Eltern unterschreiben und hakt sie pro Schüler:in in der Klassenansicht ab.</li>
                <li><strong>Verantwortung:</strong> Die Lehrperson bzw. die Schule ist dafür verantwortlich, dass für jede:n Schüler:in eine unterschriebene Erklärung vorliegt und aufbewahrt wird.</li>
                <li><strong>Registrierung:</strong> Alle bestätigen beim Erstellen des Kontos, dass sie Datenschutzerklärung und AGB gelesen haben und akzeptieren.</li>
                <li><strong>Hosting:</strong> Anwendung und Datenbank in der Schweiz; KI-Funktionen in der EU. Passwörter verschlüsselt, alles über HTTPS, jede Schule datenmässig getrennt.</li>
                <li><strong>Aufbewahrung:</strong> Konten von Schüler:innen ohne Login seit &gt; 1 Jahr werden inklusive aller Daten gelöscht.</li>
            </ul>
        </section>

        <!-- 7 -->
        <section>
            <h2>7. Schritt-für-Schritt: die wichtigsten Abläufe</h2>

            <h3>A) Lehrperson: Klasse erstellen &amp; Schüler:innen beitreten lassen</h3>
            <ol>
                <li>Menü <strong>„Klassen“</strong> öffnen → <strong>„Neue Klasse“</strong> → Namen eingeben (z.B. „3a“) → speichern.</li>
                <li>Bei der Klasse auf <strong>„Einladungslink“ / QR-Code</strong> klicken. Der QR-Code lässt sich im Unterricht am Beamer zeigen.</li>
                <li>Die Schüler:innen scannen den QR-Code (oder öffnen den Link), erstellen ihr Konto (Name, E-Mail, Passwort) und **haken Datenschutz + AGB an**.</li>
                <li>Sie erscheinen automatisch in der Klasse. Alternativ: bereits registrierte Schüler:innen über das <strong>Suchfeld</strong> finden und hinzufügen.</li>
            </ol>

            <h3>B) Lehrperson: Aufgabe erstellen</h3>
            <ol>
                <li>Menü <strong>„Aufgaben“</strong> → <strong>„Neue Aufgabe“</strong>.</li>
                <li><strong>Titel</strong> eingeben (z.B. „Bewerbungsgespräch üben“).</li>
                <li><strong>Klasse</strong> im Dropdown wählen (eine Klasse pro Aufgabe).</li>
                <li><strong>Typ</strong> wählen – direkt darunter erscheint eine Erklärung, was der Typ bedeutet (KI-Interview, Dokument, Reflexion, Video, Recherche).</li>
                <li><strong>Deadline</strong> setzen (muss in der Zukunft liegen) und optional eine <strong>Beschreibung</strong>.</li>
                <li>Auf <strong>„Erstellen“</strong> → die Aufgabe erscheint bei allen Schüler:innen der Klasse.</li>
            </ol>

            <h3>C) Lehrperson: Einverständnis abhaken</h3>
            <ol>
                <li>PDF unter <strong>praesto.ch/einverstaendnis</strong> herunterladen und an die Eltern verteilen.</li>
                <li>Unterschriebene Erklärungen einsammeln.</li>
                <li>In der Klassenansicht bei der Schüler:in auf das <strong>📝-Symbol</strong> klicken → wird grün (📝✓). Der Zähler oben (z.B. 18/22) zeigt den Stand pro Klasse.</li>
            </ol>

            <h3>D) Schüler:in: KI-Bewerbungsgespräch führen</h3>
            <ol>
                <li>Menü <strong>„Training“</strong> → <strong>„Neues Gespräch starten“</strong> (so oft du willst, je 15 Minuten).</li>
                <li>Die KI begrüsst dich und fragt nach dem Beruf. In ganzen Sätzen antworten und <strong>senden</strong>.</li>
                <li>So lange üben, wie du willst. Optional den <strong>🔥 Roast-Modus</strong> einschalten für härteres Feedback.</li>
                <li>Am Ende zeigt die KI deine <strong>Einstellungs-Chance in %</strong> mit einem konkreten Tipp.</li>
                <li>Ist es eine Aufgabe, mit <strong>„Abgeben“</strong> einreichen – die Lehrperson sieht dann den Gesprächsverlauf.</li>
            </ol>

            <h3>E) Schüler:in: Lebenslauf erstellen</h3>
            <ol>
                <li>Menü <strong>„Dossier“</strong> → <strong>„Lebenslauf erstellen“</strong>.</li>
                <li>Felder ausfüllen. Bei Listen (Schulen, Sprachen, Referenzen …) mit <strong>„+ hinzufügen“</strong> weitere Zeilen einfügen.</li>
                <li>Optional ein <strong>Foto</strong> wählen und im 3:4-Rahmen zurechtziehen/zoomen (kein verzerrtes Bild).</li>
                <li>Auf <strong>„Lebenslauf erstellen“</strong> → das Word-Dokument landet im Dossier zum Herunterladen.</li>
                <li><strong>Wichtig:</strong> vor dem Abgeben selbst überarbeiten – es ist eine Vorlage.</li>
            </ol>
        </section>

        <!-- 8 -->
        <section>
            <h2>8. Häufige Fragen &amp; Problembehebung</h2>
            <ul>
                <li><strong>Ich habe mein Passwort vergessen.</strong> → An die Lehrperson (Schüler:in) bzw. Schulleitung wenden – sie setzen es mit dem 🔑-Symbol zurück.</li>
                <li><strong>Der „Interview starten“-Button reagiert nicht / Fehler.</strong> → Seite neu laden und erneut versuchen. Prüfe deine Internetverbindung (oben erscheint sonst ein Banner).</li>
                <li><strong>Wie oft kann ich üben?</strong> → Übungsgespräche kannst du so oft starten, wie du willst – jeder Chat dauert 15 Minuten. Nur die Dokument-Generatoren (Lebenslauf, Bewerbungsschreiben) haben ein festes Kontingent.</li>
                <li><strong>Die KI antwortet auf Deutsch, obwohl ich Englisch eingestellt habe.</strong> → Neue Gespräche folgen der Sprache oben rechts; bereits gestartete Gespräche bleiben in ihrer Sprache.</li>
                <li><strong>Das erstellte Dokument sieht nicht perfekt aus.</strong> → Lebenslauf/Bewerbungsschreiben sind Vorlagen – öffne die Word-Datei und passe sie an.</li>
                <li><strong>Ein Schüler ist in der falschen Klasse.</strong> → In der Klassenansicht mit ✕ aus der Klasse entfernen (Konto/Daten bleiben) und in der richtigen Klasse über den Link/Suche hinzufügen.</li>
                <li><strong>Etwas funktioniert nicht.</strong> → Über <strong>„Bug melden“</strong> beschreiben (Titel, Bereich, was passiert ist). Das hilft uns, es schnell zu beheben.</li>
                <li><strong>Weitere Fragen?</strong> → info@praesto.ch.</li>
            </ul>
        </section>

        <p class="foot">Praesto – Julia Perić · Sandäckerstrasse 1a, 8957 Spreitenbach · info@praesto.ch · praesto.ch</p>
    </article>
</div>

<style>
    .doc-bg { background: #eceaf1; min-height: 100%; padding: 1.5rem 1rem 3rem; }
    .actions { display: flex; align-items: center; gap: 1rem; margin: 0 auto 1.25rem; max-width: 820px; }
    .btn { background: #2F124D; color: #fff; border: none; border-radius: 999px; padding: 0.7rem 1.4rem; font-weight: 700; cursor: pointer; }
    .back { color: #4b4060; text-decoration: none; }

    .sheet { max-width: 820px; margin: 0 auto; background: #fff; color: #1f1830; border-radius: 6px; box-shadow: 0 8px 30px rgba(0,0,0,0.12); padding: 3rem 3.25rem; box-sizing: border-box; }
    .doc-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; border-bottom: 3px solid #2F124D; padding-bottom: 0.85rem; margin-bottom: 1.5rem; flex-wrap: wrap; }
    .logo { font-size: 1.6rem; font-weight: 800; color: #2F124D; }
    .sub { font-size: 0.85rem; color: #6b647a; }
    .op { font-size: 0.8rem; color: #4b4060; text-align: right; line-height: 1.5; }

    h1 { font-size: 1.5rem; color: #2F124D; margin: 0 0 0.4rem; }
    .lead { margin: 0 0 1.25rem; color: #4b4060; line-height: 1.55; }
    h2 { font-size: 1.15rem; color: #2F124D; margin: 1.6rem 0 0.5rem; border-bottom: 1px solid #ece3f5; padding-bottom: 0.25rem; }
    h3 { font-size: 1rem; color: #2d2141; margin: 1rem 0 0.3rem; }
    p, li { line-height: 1.55; color: #2d2141; }
    ul, ol { margin: 0.3rem 0 0.7rem; padding-left: 1.3rem; }
    li { margin-bottom: 0.3rem; }
    ol li { padding-left: 0.15rem; }
    section { break-inside: avoid; page-break-inside: avoid; }
    .foot { margin-top: 2rem; font-size: 0.75rem; color: #9a8b9d; text-align: center; }

    @media (max-width: 640px) { .sheet { padding: 1.75rem 1.25rem; } }

    @media print {
        @page { size: A4 portrait; margin: 15mm; }
        :global(html), :global(body), :global(.app-main) { height: auto !important; min-height: 0 !important; margin: 0 !important; padding: 0 !important; }
        :global(.app-header), :global(.app-footer), :global(.demo-bar), :global(.demo-toast) { display: none !important; }
        .no-print { display: none !important; }
        .doc-bg { background: #fff; padding: 0; }
        .sheet { padding: 0; max-width: 100%; box-shadow: none; border-radius: 0; font-size: 10.5pt; color: #000; }
        .sheet, .sheet * { -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; }
        h2 { break-after: avoid; }
        h3 { break-after: avoid; }
    }
</style>
