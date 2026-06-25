<script>
    import { enhance } from "$app/forms";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    let cvLeft = $derived(data.quota?.CV?.remaining ?? 1);
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
    <title>{$t('cvform.title')} – Praesto</title>
</svelte:head>

<div class="page">
    <a href="/student/dossier" class="back">← {$t('cvform.toDossier')}</a>
    <h1>📄 {$t('cvform.title')}</h1>

    {#if cvLeft === 0}
        <div class="used-up">
            <p>{$t('cvform.usedUp')}</p>
            <a href="/student/dossier" class="btn-primary">{$t('cvform.toDossier')}</a>
        </div>
    {:else}
        <p class="intro">{$t('cvform.intro')}</p>
        {#if form?.error}<div class="err">{$t('cvform.error')}</div>{/if}

        {#if generating}
            <div class="generating">{$t('cvform.generating')}</div>
        {/if}

        <form method="POST" use:enhance={handle} class="survey" class:busy={generating}>
            <div class="grid">
                <label><span>{$t('cvform.fullName')}</span><input name="fullName" value={prefillName} required /></label>
                <label><span>{$t('cvform.birthDate')}</span><input name="birthDate" placeholder="01.05.2009" /></label>
                <label><span>{$t('cvform.address')}</span><input name="address" /></label>
                <label><span>{$t('cvform.phone')}</span><input name="phone" /></label>
                <label><span>{$t('cvform.email')}</span><input name="email" type="email" value={data.user?.email ?? ""} /></label>
                <label><span>{$t('cvform.targetJob')}</span><input name="targetJob" required /></label>
            </div>
            <label><span>{$t('cvform.school')}</span><input name="school" /></label>
            <label><span>{$t('cvform.experience')}</span><textarea name="experience" rows="2"></textarea></label>
            <label><span>{$t('cvform.skills')}</span><textarea name="skills" rows="2"></textarea></label>
            <div class="grid">
                <label><span>{$t('cvform.languages')}</span><input name="languages" placeholder="Deutsch, Englisch …" /></label>
                <label><span>{$t('cvform.hobbies')}</span><input name="hobbies" /></label>
            </div>
            <label><span>{$t('cvform.extra')}</span><textarea name="extra" rows="2"></textarea></label>

            <button type="submit" class="btn-primary" disabled={generating}>
                {generating ? $t('cvform.generating') : $t('cvform.submit')}
            </button>
        </form>
    {/if}
</div>

<style>
    .page { max-width: 760px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    .back { color: #6b647a; text-decoration: none; font-size: 0.9rem; }
    h1 { margin: 0.5rem 0 0.4rem; color: #2F124D; font-size: 1.5rem; }
    .intro { margin: 0 0 1.25rem; color: #6b647a; }

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
