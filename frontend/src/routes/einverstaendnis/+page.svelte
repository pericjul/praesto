<script>
    // Fertige Einverständniserklärung als PDF (von der Betreiberin gestaltet).
    // Liegt unter /static und wird hier direkt zum Download angeboten.
    const PDF = "/einverstaendnis-praesto.pdf";
</script>

<svelte:head>
    <title>Einverständniserklärung – Praesto</title>
</svelte:head>

<div class="wrap">
    <a class="back" href="/teacher/dashboard">← Zurück</a>

    <div class="card">
        <div class="logo">Praesto</div>
        <div class="sub">KI-Bewerbungscoach für die Lehrstellensuche</div>

        <h1>Einverständniserklärung der Eltern</h1>
        <p class="lead">
            Zum Ausdrucken und Unterschreiben. Bitte an die Eltern verteilen, unterschrieben
            zurückgeben lassen und im Klassen-Bereich abhaken, wer die Erklärung abgegeben hat.
        </p>

        <a class="btn" href={PDF} download="Einverstaendniserklaerung_Praesto.pdf">📄 Als PDF herunterladen</a>
        <a class="btn-ghost" href={PDF} target="_blank" rel="noopener">In neuem Tab ansehen</a>

        <p class="note">Enthält die Einwilligung zur Nutzung sowie die Bestätigung von Datenschutzerklärung und AGB.</p>
    </div>
</div>

<style>
    .wrap { max-width: 640px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    .back { color: #6b647a; text-decoration: none; font-size: 0.9rem; }
    .card {
        margin-top: 1rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 1rem;
        box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
        padding: 2.25rem 2rem;
        text-align: center;
    }
    .logo { font-size: 1.6rem; font-weight: 800; color: #2F124D; letter-spacing: -0.01em; }
    .sub { font-size: 0.85rem; color: #6b647a; margin-bottom: 1.5rem; }
    h1 { font-size: 1.35rem; color: #2F124D; margin: 0 0 0.6rem; }
    .lead { color: #4b4060; line-height: 1.55; margin: 0 auto 1.5rem; max-width: 46ch; }
    .btn {
        display: inline-block;
        background: #2F124D;
        color: #fff;
        border: none;
        border-radius: 999px;
        padding: 0.85rem 1.8rem;
        font-weight: 700;
        font-size: 1rem;
        text-decoration: none;
        cursor: pointer;
    }
    .btn:hover { background: #41205f; }
    .btn-ghost {
        display: inline-block;
        margin-left: 0.6rem;
        color: #2F124D;
        text-decoration: none;
        font-weight: 600;
        font-size: 0.92rem;
        padding: 0.85rem 0.5rem;
    }
    .note { margin-top: 1.5rem; font-size: 0.82rem; color: #9a8b9d; }
    @media (max-width: 480px) {
        .btn-ghost { display: block; margin: 0.75rem 0 0; }
    }
</style>
