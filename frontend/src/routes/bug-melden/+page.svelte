<script>
    import { enhance } from "$app/forms";
    import { t } from "$lib/i18n";

    let { form } = $props();
    let sending = $state(false);

    const AREAS = ["CHAT", "ASSIGNMENTS", "DOSSIER", "DASHBOARD", "LOGIN", "OTHER"];
    const SEVERITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

    function handle() {
        sending = true;
        return async ({ update }) => {
            await update();
            sending = false;
        };
    }
</script>

<svelte:head>
    <title>{$t('bug.title')} – Praesto</title>
</svelte:head>

<div class="page">
    <h1>🐞 {$t('bug.title')}</h1>
    <p class="contest">{$t('bug.contest')}</p>

    {#if form?.success}
        <div class="done">
            <p>✅ {$t('bug.success')}</p>
            <a href="/bug-melden" class="btn-primary" data-sveltekit-reload>{$t('bug.again')}</a>
        </div>
    {:else}
        <p class="intro">{$t('bug.intro')}</p>
        {#if form?.error}<div class="err">{$t('bug.error')}</div>{/if}

        <form method="POST" use:enhance={handle} class="form" class:busy={sending}>
            <label>
                <span>{$t('bug.fTitle')}</span>
                <input name="title" required placeholder={$t('bug.fTitlePh')} maxlength="200" />
            </label>

            <div class="grid">
                <label>
                    <span>{$t('bug.fArea')}</span>
                    <select name="area">
                        {#each AREAS as a}<option value={a}>{$t('bug.area.' + a)}</option>{/each}
                    </select>
                </label>
                <label>
                    <span>{$t('bug.fSeverity')}</span>
                    <select name="severity">
                        {#each SEVERITIES as s}<option value={s} selected={s === 'MEDIUM'}>{$t('bug.sev.' + s)}</option>{/each}
                    </select>
                </label>
            </div>

            <label>
                <span>{$t('bug.fDesc')}</span>
                <textarea name="description" rows="4" required placeholder={$t('bug.fDescPh')}></textarea>
            </label>

            <label>
                <span>{$t('bug.fSteps')}</span>
                <textarea name="steps" rows="3" placeholder={$t('bug.fStepsPh')}></textarea>
            </label>

            <label>
                <span>{$t('bug.fDevice')}</span>
                <input name="device" placeholder={$t('bug.fDevicePh')} maxlength="200" />
            </label>

            <button type="submit" class="btn-primary" disabled={sending}>
                {sending ? $t('bug.sending') : $t('bug.submit')}
            </button>
        </form>
    {/if}
</div>

<style>
    .page { max-width: 1000px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    h1 { margin: 0 0 0.6rem; color: #2F124D; font-size: 1.5rem; }
    .contest {
        background: linear-gradient(135deg, #fff4e6, #ffe9d6);
        border: 1px solid #f7c98f; color: #8a4b12;
        border-radius: 0.75rem; padding: 0.7rem 1rem; margin: 0 0 1.25rem; font-weight: 600; font-size: 0.92rem;
    }
    .intro { color: #6b647a; margin: 0 0 1.25rem; }

    .form { display: grid; gap: 0.9rem; }
    .form.busy { opacity: 0.6; pointer-events: none; }
    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.9rem; }
    label { display: grid; gap: 0.3rem; }
    label span { font-size: 0.85rem; font-weight: 600; color: #2d2141; }
    input, textarea, select {
        width: 100%; box-sizing: border-box; border: 1px solid #e8e0f0; border-radius: 0.6rem;
        padding: 0.6rem 0.75rem; font: inherit; background: #faf8fc;
    }
    input:focus, textarea:focus, select:focus { outline: 2px solid #2F124D; outline-offset: 1px; background: #fff; }

    .btn-primary {
        justify-self: start; margin-top: 0.3rem; background: #2F124D; color: #fff; border: none;
        border-radius: 999px; padding: 0.8rem 1.8rem; font-weight: 700; font-size: 1rem; cursor: pointer; text-decoration: none;
    }
    .btn-primary:hover { background: #41205f; }
    .btn-primary:disabled { opacity: 0.6; cursor: default; }

    .done { background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 1rem; padding: 1.5rem; text-align: center; }
    .done p { margin: 0 0 1rem; color: #166534; font-weight: 600; }
    .err { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; border-radius: 0.6rem; padding: 0.7rem 1rem; }

    @media (max-width: 560px) { .grid { grid-template-columns: 1fr; } }
</style>
