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
        <p class="intro">{$t('clform.intro')} · {$t('clform.left').replace('%N', clLeft)}</p>
        <p class="disclaimer">⚠️ {$t('dossier.disclaimer')}</p>
        {#if form?.error}<div class="err">{$t('cvform.error')}</div>{/if}
        {#if generating}<div class="generating">{$t('clform.generating')}</div>{/if}

        <form method="POST" use:enhance={handle} class="survey" class:busy={generating}>
            <h2>{$t('clform.you')}</h2>
            <div class="grid">
                <label><span>{$t('clform.fullName')} *</span><input name="fullName" value={prefillName} required /></label>
                <label><span>{$t('clform.senderAddress')}</span><input name="senderAddress" placeholder={$t('clform.ph.senderAddress')} /></label>
                <label><span>{$t('clform.phone')}</span><input name="phone" placeholder="079 123 45 67" /></label>
                <label><span>{$t('clform.email')}</span><input name="email" type="email" value={data.user?.email ?? ""} /></label>
                <label><span>{$t('clform.city')}</span><input name="city" placeholder={$t('clform.ph.city')} /></label>
                <label><span>{$t('clform.targetJob')} *</span><input name="targetJob" placeholder={$t('clform.ph.targetJob')} required /></label>
            </div>

            <h2>{$t('clform.companySec')}</h2>
            <div class="grid">
                <label><span>{$t('clform.companyName')} *</span><input name="companyName" required /></label>
                <label><span>{$t('clform.contactPerson')} <em>({$t('clform.ifKnown')})</em></span><input name="contactPerson" placeholder={$t('clform.ph.contactPerson')} /></label>
                <label><span>{$t('clform.companyAddress')}</span><input name="companyAddress" /></label>
                <label><span>{$t('clform.source')} <em>({$t('clform.sourceHint')})</em></span><input name="applicationSource" placeholder={$t('clform.ph.source')} /></label>
                <label><span>{$t('clform.startDate')}</span><input name="startDate" placeholder={$t('clform.ph.startDate')} /></label>
            </div>

            <h2>{$t('clform.contentSec')}</h2>
            <label><span>{$t('clform.whyCompany')}</span><textarea name="whyCompany" rows="2"></textarea></label>
            <label><span>{$t('clform.strengths')} <em>({$t('clform.strengthsHint')})</em></span><textarea name="strengths" rows="2" placeholder={$t('clform.ph.strengths')}></textarea></label>
            <label><span>{$t('clform.schnupper')} <em>({$t('clform.schnupperHint')})</em></span><textarea name="schnupperExperience" rows="2"></textarea></label>
            <label><span>{$t('clform.availability')} <em>({$t('clform.availabilityHint')})</em></span><input name="availability" placeholder={$t('clform.ph.availability')} /></label>
            <label><span>{$t('clform.extra')}</span><textarea name="extra" rows="2"></textarea></label>

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
    .intro { margin: 0 0 0.6rem; color: #6b647a; }
    .disclaimer { background: #fff7ed; border: 1px solid #fed7aa; color: #9a3412; border-radius: 0.6rem; padding: 0.7rem 0.9rem; font-size: 0.85rem; line-height: 1.45; margin: 0 0 1.25rem; }
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
