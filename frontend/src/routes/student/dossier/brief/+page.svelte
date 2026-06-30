<script>
    import { enhance } from "$app/forms";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    let clLeft = $derived(data.quota?.COVER_LETTER?.remaining ?? 3);
    let prefillName = $derived(`${data.user?.firstName ?? ""} ${data.user?.lastName ?? ""}`.trim());
    let generating = $state(false);

    function handle() {
        generating = true;
        return async ({ update }) => {
            await update();
            generating = false;
        };
    }
</script>

<svelte:head>
    <title>{$t('clform.title')} – Praesto</title>
</svelte:head>

<div class="page">
    <a href="/student/dossier" class="back">← {$t('cvform.toDossier')}</a>
    <h1>✍️ {$t('clform.title')}</h1>

    {#if clLeft === 0}
        <div class="used-up">
            <p>{$t('clform.usedUp')}</p>
            <a href="/student/dossier" class="btn-primary">{$t('cvform.toDossier')}</a>
        </div>
    {:else}
        <p class="intro">{$t('clform.intro')} · {$t('clform.left').replace('%N%', clLeft)}</p>
        {#if form?.error}<div class="err">{$t('cvform.error')}</div>{/if}
        {#if generating}<div class="generating">{$t('clform.generating')}</div>{/if}

        <form method="POST" use:enhance={handle} class="survey" class:busy={generating}>
            <h2>Du</h2>
            <div class="grid">
                <label><span>Vor- und Name *</span><input name="fullName" value={prefillName} required /></label>
                <label><span>Deine Adresse</span><input name="senderAddress" placeholder="Strasse, PLZ Ort" /></label>
                <label><span>Telefon</span><input name="phone" placeholder="079 123 45 67" /></label>
                <label><span>E-Mail</span><input name="email" type="email" value={data.user?.email ?? ""} /></label>
                <label><span>Ort (für Datum)</span><input name="city" placeholder="Zürich" /></label>
                <label><span>Beruf / Lehrstelle *</span><input name="targetJob" placeholder="Kauffrau/Kaufmann EFZ" required /></label>
            </div>

            <h2>Firma</h2>
            <div class="grid">
                <label><span>Firma *</span><input name="companyName" required /></label>
                <label><span>Ansprechperson <em>(falls bekannt)</em></span><input name="contactPerson" placeholder="Frau Muster" /></label>
                <label><span>Firmen-Adresse</span><input name="companyAddress" /></label>
                <label><span>Wo gefunden <em>(Quelle)</em></span><input name="applicationSource" placeholder="yousty.ch / Inserat / Website" /></label>
                <label><span>Möglicher Lehrbeginn</span><input name="startDate" placeholder="August 2026" /></label>
            </div>

            <h2>Inhalt (Stichworte – die KI macht schöne Sätze daraus)</h2>
            <label><span>Warum diese Firma?</span><textarea name="whyCompany" rows="2"></textarea></label>
            <label><span>Deine Stärken <em>(mit kurzem Beispiel)</em></span><textarea name="strengths" rows="2" placeholder="z.B. zuverlässig – ich erledige Aufgaben in der Schule immer pünktlich"></textarea></label>
            <label><span>Schnuppererfahrung <em>(was hat dir gefallen?)</em></span><textarea name="schnupperExperience" rows="2"></textarea></label>
            <label><span>Verfügbarkeit <em>(z.B. für ein Gespräch / Schnuppertage)</em></span><input name="availability" placeholder="z.B. mittwochs oder in den Ferien" /></label>
            <label><span>Sonstiges</span><textarea name="extra" rows="2"></textarea></label>

            <button type="submit" class="btn-primary" disabled={generating}>
                {generating ? $t('clform.generating') : $t('clform.submit')}
            </button>
        </form>
    {/if}
</div>

<style>
    .page { max-width: 1000px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    .back { color: #6b647a; text-decoration: none; font-size: 0.9rem; }
    h1 { margin: 0.5rem 0 0.4rem; color: #2F124D; font-size: 1.5rem; }
    h2 { margin: 1.1rem 0 0.1rem; color: #2F124D; font-size: 1rem; border-bottom: 2px solid #ece3f5; padding-bottom: 0.3rem; }
    .intro { margin: 0 0 1.25rem; color: #6b647a; }
    label em { color: #9a8baf; font-weight: 400; font-style: normal; }
    input, textarea { width: 100%; box-sizing: border-box; }

    .survey { display: grid; gap: 0.85rem; }
    .survey.busy { opacity: 0.6; pointer-events: none; }
    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.85rem; }
    label { display: grid; gap: 0.3rem; }
    label span { font-size: 0.85rem; font-weight: 600; color: #2d2141; }
    input, textarea { border: 1px solid #e8e0f0; border-radius: 0.6rem; padding: 0.6rem 0.75rem; font: inherit; }
    input:focus, textarea:focus { outline: 2px solid #2F124D; outline-offset: 1px; }

    .btn-primary { justify-self: start; background: #2F124D; color: #fff; border: none; border-radius: 999px; padding: 0.8rem 1.8rem; font-weight: 700; font-size: 1rem; cursor: pointer; text-decoration: none; }
    .btn-primary:hover { background: #41205f; }
    .btn-primary:disabled { opacity: 0.6; cursor: default; }

    .used-up { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1.5rem; text-align: center; }
    .used-up p { margin: 0 0 1rem; color: #2d2141; }

    .generating { background: #f5f8ff; border: 1px solid #dbe4ff; color: #1e40af; border-radius: 0.6rem; padding: 0.85rem 1rem; margin-bottom: 0.9rem; font-weight: 600; }
    .err { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; border-radius: 0.6rem; padding: 0.7rem 1rem; margin-bottom: 0.9rem; }

    @media (max-width: 640px) { .grid { grid-template-columns: 1fr; } }
</style>
