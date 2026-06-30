<script>
    // Druck-/PDF-fertige Einverständniserklärung für Eltern. Bewusst auf Deutsch
    // (Dokument zum Ausdrucken/Unterschreiben), nicht app-mehrsprachig.
    function print() {
        window.print();
    }
</script>

<svelte:head>
    <title>Einverständniserklärung – Praesto</title>
</svelte:head>

<div class="sheet">
    <div class="actions no-print">
        <button class="btn" onclick={print}>📄 Als PDF speichern / drucken</button>
        <a class="back" href="/teacher/dashboard">← Zurück</a>
    </div>

    <header class="doc-head">
        <div>
            <div class="logo">Praesto</div>
            <div class="sub">KI-Bewerbungscoach für die Lehrstellensuche</div>
        </div>
        <div class="op">
            Praesto – Julia Perić<br />
            Sandäckerstrasse 1a, 8957 Spreitenbach<br />
            info@praesto.ch · praesto.ch
        </div>
    </header>

    <h1>Einverständniserklärung zur Nutzung von Praesto</h1>

    <p>Die Schule setzt im Rahmen der Berufswahl die Lernplattform <strong>Praesto</strong> ein.
    Die Schüler:innen üben damit Bewerbungsgespräche mit einer KI und erstellen Bewerbungsunterlagen
    (Lebenslauf, Bewerbungsschreiben).</p>

    <h2>Welche Daten bearbeitet werden</h2>
    <ul>
        <li><strong>Konto-Daten:</strong> Vor- und Nachname, E-Mail, verschlüsseltes Passwort, Klasse/Schule.</li>
        <li><strong>Inhalte:</strong> Übungsgespräche, Aufgaben-Abgaben, Notizen und hochgeladene Dokumente.</li>
    </ul>

    <h2>Wo die Daten liegen</h2>
    <p>Anwendung und Datenbank werden in der <strong>Schweiz</strong> betrieben (Microsoft Azure,
    Region Switzerland North). Für die KI-Funktionen werden Eingaben an einen KI-Dienst in der
    <strong>EU</strong> (Azure OpenAI, Region Schweden) übermittelt. Es findet keine Weitergabe zu
    Werbezwecken statt. Details: <strong>praesto.ch/datenschutz</strong>.</p>

    <h2>Einverständnis</h2>
    <p class="consent">Ich bin damit einverstanden, dass mein Kind Praesto im Rahmen des Unterrichts
    nutzt und dass die dafür nötigen Personendaten gemäss der Datenschutzerklärung von Praesto
    bearbeitet werden. Mir ist bekannt, dass ich dieses Einverständnis jederzeit über die Schule
    oder info@praesto.ch widerrufen und die Löschung der Daten verlangen kann.</p>

    <div class="fields">
        <div class="row">
            <div class="f"><span>Name der Schülerin / des Schülers</span><div class="line"></div></div>
            <div class="f short"><span>Klasse</span><div class="line"></div></div>
        </div>
        <div class="row">
            <div class="f"><span>Schule</span><div class="line"></div></div>
        </div>
        <div class="row">
            <div class="f"><span>Name Erziehungsberechtigte:r</span><div class="line"></div></div>
        </div>
        <div class="row">
            <div class="f short"><span>Ort, Datum</span><div class="line"></div></div>
            <div class="f"><span>Unterschrift Erziehungsberechtigte:r</span><div class="line"></div></div>
        </div>
    </div>

    <p class="foot">Praesto – Julia Perić · Sandäckerstrasse 1a, 8957 Spreitenbach · info@praesto.ch</p>
</div>

<style>
    .sheet { max-width: 820px; margin: 0 auto; padding: 1.5rem 1rem 3rem; color: #1f1830; }
    .actions { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; }
    .btn { background: #2F124D; color: #fff; border: none; border-radius: 999px; padding: 0.7rem 1.4rem; font-weight: 700; cursor: pointer; }
    .back { color: #6b647a; text-decoration: none; }

    .doc-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; border-bottom: 3px solid #2F124D; padding-bottom: 0.75rem; margin-bottom: 1.25rem; flex-wrap: wrap; }
    .logo { font-size: 1.6rem; font-weight: 800; color: #2F124D; letter-spacing: -0.01em; }
    .sub { font-size: 0.85rem; color: #6b647a; }
    .op { font-size: 0.8rem; color: #4b4060; text-align: right; line-height: 1.5; }

    h1 { font-size: 1.4rem; color: #2F124D; margin: 0 0 1rem; }
    h2 { font-size: 1.02rem; color: #2d2141; margin: 1.25rem 0 0.4rem; }
    p, li { line-height: 1.6; color: #2d2141; }
    ul { margin: 0.3rem 0; padding-left: 1.2rem; }
    .consent { background: #faf7fc; border: 1px solid #ece3f5; border-radius: 0.6rem; padding: 0.8rem 1rem; }

    .fields { margin-top: 1.5rem; display: flex; flex-direction: column; gap: 1.4rem; }
    .row { display: flex; gap: 1.5rem; }
    .f { flex: 1; }
    .f.short { flex: 0 0 28%; }
    .f span { font-size: 0.78rem; color: #6b647a; }
    .line { border-bottom: 1px solid #2d2141; height: 1.6rem; }

    .foot { margin-top: 2rem; font-size: 0.75rem; color: #9a8b9d; text-align: center; }

    @media print {
        @page { size: A4 portrait; margin: 14mm; }
        :global(.app-header), :global(.app-footer), :global(.demo-bar), :global(.demo-toast) { display: none !important; }
        :global(.app-main) { padding: 0 !important; }
        .no-print { display: none !important; }
        /* Alles auf EINE A4-Seite verdichten */
        .sheet { padding: 0; max-width: 100%; font-size: 10.5pt; color: #000; }
        .doc-head { padding-bottom: 0.5rem; margin-bottom: 0.8rem; }
        .logo { font-size: 1.4rem; }
        h1 { font-size: 1.2rem; margin: 0 0 0.5rem; }
        h2 { font-size: 0.98rem; margin: 0.7rem 0 0.2rem; }
        p, li { line-height: 1.38; }
        ul { margin: 0.2rem 0; }
        .consent { padding: 0.55rem 0.8rem; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
        .fields { margin-top: 1rem; gap: 0.95rem; }
        .line { height: 1.35rem; }
        .foot { margin-top: 1.1rem; }
        /* Unterschriftsblock nicht über den Seitenrand brechen */
        .consent, .fields, .row { break-inside: avoid; page-break-inside: avoid; }
    }
</style>
