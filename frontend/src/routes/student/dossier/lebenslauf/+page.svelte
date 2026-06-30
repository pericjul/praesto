<script>
    import { enhance } from "$app/forms";
    import RepeaterField from "$lib/components/RepeaterField.svelte";

    let { data, form } = $props();

    let generating = $state(false);
    let prefillFirst = $derived(data.user?.firstName ?? "");
    let prefillLast = $derived(data.user?.lastName ?? "");

    function handle() {
        generating = true;
        return async ({ update }) => {
            await update();
            generating = false;
        };
    }
</script>

<svelte:head>
    <title>Lebenslauf erstellen – Praesto</title>
</svelte:head>

<div class="page">
    <a href="/student/dossier" class="back">← Zurück zum Dossier</a>
    <h1>📄 Lebenslauf erstellen</h1>
    <p class="intro">Fülle die Felder aus – dein Lebenslauf wird automatisch sauber formatiert (ohne KI). Leere Felder werden weggelassen. Bei mehrzeiligen Feldern: <strong>eine Zeile pro Eintrag</strong>.</p>

    {#if form?.error}<div class="err">Konnte den Lebenslauf nicht erstellen. Bitte nochmals versuchen.</div>{/if}
    {#if generating}<div class="generating">Lebenslauf wird erstellt …</div>{/if}

    <form method="POST" enctype="multipart/form-data" use:enhance={handle} class="survey" class:busy={generating}>
        <h2>Personalien</h2>
        <div class="grid">
            <label><span>Vorname *</span><input name="firstName" value={prefillFirst} required /></label>
            <label><span>Name *</span><input name="lastName" value={prefillLast} required /></label>
            <label><span>Strasse / Nr. *</span><input name="address" required /></label>
            <label><span>PLZ / Ort *</span><input name="zipCity" placeholder="8000 Zürich" required /></label>
            <label><span>Telefon *</span><input name="phone" placeholder="079 123 45 67" required /></label>
            <label><span>E-Mail *</span><input name="email" type="email" value={data.user?.email ?? ""} required /></label>
            <label><span>Geburtsdatum <em>(freiwillig)</em></span><input name="birthDate" placeholder="01.05.2009" /></label>
            <label><span>Heimat-/Geburtsort <em>(freiwillig)</em></span><input name="hometown" /></label>
            <label><span>Nationalität <em>(freiwillig)</em></span><input name="nationality" /></label>
        </div>
        <label class="photo-field"><span>Bewerbungsfoto <em>(freiwillig, Hochformat – JPG/PNG)</em></span><input name="photo" type="file" accept="image/png,image/jpeg" /></label>

        <h2>Familie <em class="opt">(freiwillig)</em></h2>
        <RepeaterField name="parents" label="Eltern" hint="(ein Eintrag pro Person)" placeholder="Mutter · Anna Muster · Pflegefachfrau" addLabel="Elternteil hinzufügen" />
        <RepeaterField name="siblings" label="Geschwister" hint="(ein Eintrag pro Person)" placeholder="Lena Muster · Lehre als Logistikerin" addLabel="Geschwister hinzufügen" />

        <h2>Angestrebte Lehrstelle</h2>
        <label><span>Beruf / Lehrstelle *</span><input name="targetJob" placeholder="Kauffrau/Kaufmann EFZ" required /></label>
        <label><span>Über mich <em>(kurzer Satz, optional)</em></span><textarea name="aboutMe" rows="2" placeholder="z.B. Ich arbeite gerne organisiert und mit Menschen."></textarea></label>

        <h2>Schulbildung</h2>
        <RepeaterField name="education" label="Schulen" hint="(ein Eintrag pro Schule)" placeholder="2021–2025 · Sekundarschule B, Zürich" addLabel="Schule hinzufügen" />

        <h2>Praktische Erfahrung / Schnupperlehren</h2>
        <RepeaterField name="experience" label="Erfahrungen" hint="(ein Eintrag pro Erfahrung)" placeholder="Mai 2024 · Schnupperlehre Kauffrau, Muster AG — Büroarbeit, Kundenkontakt" addLabel="Erfahrung hinzufügen" />

        <h2>Sprachen</h2>
        <RepeaterField name="languages" label="Sprachen mit Niveau" hint="(ein Eintrag pro Sprache)" placeholder="Deutsch · Muttersprache" addLabel="Sprache hinzufügen" />

        <h2>Weiteres</h2>
        <label><span>Kenntnisse & Fähigkeiten <em>(Komma- oder zeilenweise)</em></span><textarea name="skills" rows="2" placeholder="Word, Excel, Teamarbeit, Zuverlässigkeit"></textarea></label>
        <label><span>Hobbys & Interessen</span><textarea name="hobbies" rows="2" placeholder="Fussball, Klavier, Pfadi"></textarea></label>
        <RepeaterField name="references" label="Referenzen" hint="(ein Eintrag pro Person – nicht aus der Familie, vorher fragen)" placeholder="Frau Muster · Klassenlehrerin · 079 123 45 67" addLabel="Referenz hinzufügen" />

        <button type="submit" class="btn-primary" disabled={generating}>
            {generating ? "Wird erstellt …" : "Lebenslauf erstellen"}
        </button>
    </form>
</div>

<style>
    .page { max-width: 1000px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    .back { color: #6b647a; text-decoration: none; font-size: 0.9rem; }
    h1 { margin: 0.5rem 0 0.4rem; color: #2F124D; font-size: 1.5rem; }
    h2 { margin: 1.1rem 0 0.1rem; color: #2F124D; font-size: 1rem; border-bottom: 2px solid #ece3f5; padding-bottom: 0.3rem; }
    .intro { margin: 0 0 1.25rem; color: #6b647a; }

    .survey { display: grid; gap: 0.85rem; }
    .survey.busy { opacity: 0.6; pointer-events: none; }
    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.85rem; }
    label { display: grid; gap: 0.3rem; }
    label span { font-size: 0.85rem; font-weight: 600; color: #2d2141; }
    label em { color: #9a8baf; font-weight: 400; font-style: normal; }
    input, textarea { border: 1px solid #e8e0f0; border-radius: 0.6rem; padding: 0.6rem 0.75rem; font: inherit; width: 100%; box-sizing: border-box; }
    input:focus, textarea:focus { outline: 2px solid #2F124D; outline-offset: 1px; }

    .btn-primary { justify-self: start; margin-top: 0.5rem; background: #2F124D; color: #fff; border: none; border-radius: 999px; padding: 0.8rem 1.8rem; font-weight: 700; font-size: 1rem; cursor: pointer; text-decoration: none; }
    .btn-primary:hover { background: #41205f; }
    .btn-primary:disabled { opacity: 0.6; cursor: default; }

    .generating { background: #f5f8ff; border: 1px solid #dbe4ff; color: #1e40af; border-radius: 0.6rem; padding: 0.85rem 1rem; margin-bottom: 0.9rem; font-weight: 600; }
    .err { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; border-radius: 0.6rem; padding: 0.7rem 1rem; margin-bottom: 0.9rem; }

    @media (max-width: 640px) { .grid { grid-template-columns: 1fr; } }
</style>
