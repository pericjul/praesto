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

<div class="doc-bg">
    <div class="actions no-print">
        <button class="btn" onclick={print}>📄 Als PDF speichern / drucken</button>
        <a class="back" href="/teacher/dashboard">← Zurück</a>
    </div>

    <article class="sheet">
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

        <h1>Einverständniserklärung der Erziehungsberechtigten</h1>
        <p class="lead">zur Nutzung der Lernplattform Praesto im Rahmen des Berufswahlunterrichts</p>

        <p>Die Schule setzt im Berufswahlunterricht die Lernplattform <strong>Praesto</strong> ein.
        Die Schüler:innen üben damit Bewerbungsgespräche mit einer KI und erstellen
        Bewerbungsunterlagen (Lebenslauf, Bewerbungsschreiben). Die Nutzung ist freiwillig.</p>

        <h2>1. Welche Daten bearbeitet werden</h2>
        <ul>
            <li><strong>Konto-Daten:</strong> Vor- und Nachname, E-Mail, verschlüsseltes Passwort, Klasse/Schule.</li>
            <li><strong>Inhalte:</strong> Übungsgespräche, Aufgaben-Abgaben, Notizen und hochgeladene Dokumente.</li>
        </ul>

        <h2>2. Wo die Daten liegen</h2>
        <p>Anwendung und Datenbank werden in der <strong>Schweiz</strong> betrieben (Microsoft Azure,
        Region Switzerland North). Für die KI-Funktionen werden Eingaben an einen KI-Dienst in der
        <strong>EU</strong> (Azure OpenAI, Region Schweden) übermittelt. Es findet keine Weitergabe
        zu Werbezwecken statt.</p>

        <h2>3. Ihre Rechte</h2>
        <p>Sie können die Einwilligung jederzeit ohne Angabe von Gründen widerrufen und Auskunft,
        Berichtigung oder Löschung der Daten verlangen – über die Schule oder direkt unter
        <strong>info@praesto.ch</strong>. Massgebend sind die
        <a href="https://praesto.ch/datenschutz">Datenschutzerklärung</a> und die
        <a href="https://praesto.ch/agb">AGB</a> von Praesto (auch unter
        praesto.ch/datenschutz bzw. praesto.ch/agb abrufbar).</p>

        <h2>4. Einwilligung</h2>
        <div class="consent">
            <p>Als erziehungsberechtigte Person bestätige ich mit meiner Unterschrift:</p>
            <ul class="checks">
                <li><span class="box"></span><span class="ct">Ich habe die <strong>Datenschutzerklärung</strong> (praesto.ch/datenschutz)
                    und die <strong>AGB</strong> (praesto.ch/agb) gelesen und bin damit einverstanden.</span></li>
                <li><span class="box"></span><span class="ct">Ich willige ein, dass mein Kind Praesto im Rahmen des Unterrichts nutzt
                    und die dafür notwendigen Personendaten gemäss Datenschutzerklärung bearbeitet werden.</span></li>
                <li><span class="box"></span><span class="ct">Mir ist bekannt, dass die Nutzung freiwillig ist und ich diese
                    Einwilligung jederzeit widerrufen und die Löschung der Daten verlangen kann.</span></li>
            </ul>
        </div>

        <div class="fields">
            <div class="row">
                <div class="f"><span>Name der Schülerin / des Schülers</span><div class="line"></div></div>
                <div class="f short"><span>Klasse</span><div class="line"></div></div>
            </div>
            <div class="row">
                <div class="f"><span>Schule</span><div class="line"></div></div>
                <div class="f short"><span>Geburtsdatum Kind</span><div class="line"></div></div>
            </div>
            <div class="row">
                <div class="f"><span>Name Erziehungsberechtigte:r</span><div class="line"></div></div>
            </div>
            <div class="row">
                <div class="f short"><span>Ort, Datum</span><div class="line"></div></div>
                <div class="f"><span>Unterschrift Erziehungsberechtigte:r</span><div class="line"></div></div>
            </div>
        </div>

        <p class="foot">Praesto – Julia Perić · Sandäckerstrasse 1a, 8957 Spreitenbach · info@praesto.ch · praesto.ch</p>
    </article>
</div>

<style>
    /* Heller, neutraler Hintergrund -> das weisse Blatt wirkt wie ein echtes Dokument */
    .doc-bg { background: #eceaf1; min-height: 100%; padding: 1.5rem 1rem 3rem; }
    .actions { display: flex; align-items: center; gap: 1rem; margin: 0 auto 1.25rem; max-width: 820px; }
    .btn { background: #2F124D; color: #fff; border: none; border-radius: 999px; padding: 0.7rem 1.4rem; font-weight: 700; cursor: pointer; }
    .back { color: #4b4060; text-decoration: none; }

    /* Das eigentliche weisse Dokument */
    .sheet {
        max-width: 820px;
        margin: 0 auto;
        background: #fff;
        color: #1f1830;
        border-radius: 6px;
        box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
        padding: 3rem 3.25rem;
        box-sizing: border-box;
    }

    .doc-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; border-bottom: 3px solid #2F124D; padding-bottom: 0.85rem; margin-bottom: 1.5rem; flex-wrap: wrap; }
    .logo { font-size: 1.6rem; font-weight: 800; color: #2F124D; letter-spacing: -0.01em; }
    .sub { font-size: 0.85rem; color: #6b647a; }
    .op { font-size: 0.8rem; color: #4b4060; text-align: right; line-height: 1.5; }

    h1 { font-size: 1.45rem; color: #2F124D; margin: 0 0 0.25rem; }
    .lead { margin: 0 0 1.25rem; color: #6b647a; font-size: 0.95rem; }
    h2 { font-size: 1.02rem; color: #2d2141; margin: 1.4rem 0 0.4rem; }
    p, li { line-height: 1.6; color: #2d2141; }
    a { color: #2F124D; }
    ul { margin: 0.3rem 0; padding-left: 1.2rem; }

    .consent { background: #faf7fc; border: 1px solid #ece3f5; border-radius: 0.6rem; padding: 0.9rem 1.1rem; margin-top: 0.5rem; }
    .consent p { margin: 0 0 0.5rem; font-weight: 600; }
    .checks { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 0.55rem; }
    .checks li { display: flex; gap: 0.6rem; align-items: flex-start; line-height: 1.5; }
    .checks .ct { flex: 1; min-width: 0; }
    .box { flex: 0 0 auto; width: 1rem; height: 1rem; margin-top: 0.2rem; border: 1.5px solid #2F124D; border-radius: 3px; background: #fff; }

    .fields { margin-top: 1.75rem; display: flex; flex-direction: column; gap: 1.5rem; }
    .row { display: flex; gap: 1.5rem; }
    .f { flex: 1; }
    .f.short { flex: 0 0 28%; }
    .f span { font-size: 0.78rem; color: #6b647a; }
    .line { border-bottom: 1px solid #2d2141; height: 1.7rem; }

    .foot { margin-top: 2rem; font-size: 0.75rem; color: #9a8b9d; text-align: center; }

    @media (max-width: 640px) {
        .sheet { padding: 1.75rem 1.25rem; }
        .row { flex-direction: column; gap: 0.9rem; }
        .f.short { flex: 0 0 auto; }
    }

    @media print {
        @page { size: A4 portrait; margin: 14mm; }
        :global(.app-header), :global(.app-footer), :global(.demo-bar), :global(.demo-toast) { display: none !important; }
        :global(.app-main) { padding: 0 !important; }
        .no-print { display: none !important; }
        /* Reines weisses Blatt, alles auf EINE A4-Seite verdichtet */
        :global(html), :global(body), :global(.app-main) { height: auto !important; min-height: 0 !important; }
        .doc-bg { background: #fff; padding: 0; min-height: 0; }
        .sheet { padding: 0; max-width: 100%; box-shadow: none; border-radius: 0; font-size: 10.5pt; color: #000; }
        .sheet, .sheet * { -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; }
        .doc-head { padding-bottom: 0.5rem; margin-bottom: 0.85rem; }
        .logo { font-size: 1.4rem; }
        h1 { font-size: 1.2rem; }
        .lead { margin: 0 0 0.7rem; }
        h2 { font-size: 0.98rem; margin: 0.7rem 0 0.2rem; }
        p, li { line-height: 1.36; }
        ul { margin: 0.15rem 0; }
        .consent { padding: 0.55rem 0.8rem; }
        .checks { gap: 0.35rem; }
        .fields { margin-top: 1rem; gap: 1rem; }
        .line { height: 1.4rem; }
        .foot { margin-top: 1.1rem; }
        .consent, .fields, .row { break-inside: avoid; page-break-inside: avoid; }
    }
</style>
