<script>
    import { enhance } from "$app/forms";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    let sending = $state(false);
    let prefillName = $derived(
        data.user?.firstName ? `${data.user.firstName} ${data.user.lastName ?? ""}`.trim() : ""
    );

    let faq = $derived([
        { q: $t("help.q1"), a: $t("help.a1") },
        { q: $t("help.q2"), a: $t("help.a2") },
        { q: $t("help.q3"), a: $t("help.a3") },
        { q: $t("help.q4"), a: $t("help.a4") },
        { q: $t("help.q5"), a: $t("help.a5") },
        { q: $t("help.q6"), a: $t("help.a6") }
    ]);

    function handle() {
        sending = true;
        return async ({ update }) => {
            await update();
            sending = false;
        };
    }
</script>

<svelte:head>
    <title>{$t('help.title')} – Praesto</title>
</svelte:head>

<div class="help">
    <h1>{$t('help.title')}</h1>
    <p class="intro">{$t('help.intro')}</p>

    <h2>{$t('help.faqTitle')}</h2>
    <div class="faq">
        {#each faq as item}
            <details class="faq-item">
                <summary>{item.q}</summary>
                <p>{item.a}</p>
            </details>
        {/each}
    </div>

    <h2>{$t('help.formTitle')}</h2>
    <p class="form-intro">{$t('help.formIntro')}</p>

    {#if form?.success}
        <div class="ok">{$t('help.success')}</div>
    {:else}
        {#if form?.error}<div class="err">{$t('help.error')}</div>{/if}
        <form method="POST" use:enhance={handle} class="ask">
            <label><span>{$t('help.fName')}</span><input name="name" value={prefillName} required /></label>
            <label><span>{$t('help.fEmail')}</span><input name="email" type="email" value={data.user?.email ?? ""} required /></label>
            <label><span>{$t('help.fMessage')}</span><textarea name="message" rows="5" placeholder={$t('help.fMessagePh')} required></textarea></label>
            <button type="submit" class="btn" disabled={sending}>{sending ? $t('help.sending') : $t('help.submit')}</button>
        </form>
    {/if}

    <a href="/" class="back">← {$t('help.back')}</a>
</div>

<style>
    .help { max-width: 1000px; margin: 0 auto; padding: 2rem 1rem 3rem; }
    h1 { margin: 0 0 0.5rem; color: var(--color-primary, #2F124D); font-size: clamp(1.6rem, 3vw, 2.1rem); }
    .intro { color: #5E4C6F; line-height: 1.6; margin: 0 0 1.75rem; }
    h2 { color: #2d2141; font-size: 1.15rem; margin: 1.75rem 0 0.75rem; }
    .form-intro { color: #6b647a; font-size: 0.9rem; margin: 0 0 0.9rem; }

    .faq { display: flex; flex-direction: column; gap: 0.6rem; }
    .faq-item { background: #fff; border: 1px solid #e8e0f0; border-radius: 0.75rem; padding: 0.5rem 1rem; }
    .faq-item summary { cursor: pointer; font-weight: 600; color: #2F124D; padding: 0.4rem 0; list-style: revert; }
    .faq-item[open] summary { margin-bottom: 0.3rem; }
    .faq-item p { margin: 0 0 0.5rem; color: #5E4C6F; line-height: 1.6; }

    .ask { display: grid; gap: 0.85rem; }
    .ask label { display: grid; gap: 0.3rem; }
    .ask label span { font-size: 0.85rem; font-weight: 600; color: #2d2141; }
    .ask input, .ask textarea { width: 100%; box-sizing: border-box; border: 1px solid #e8e0f0; border-radius: 0.6rem; padding: 0.6rem 0.75rem; font: inherit; }
    .ask input:focus, .ask textarea:focus { outline: 2px solid #2F124D; outline-offset: 1px; }
    .btn { justify-self: start; background: #2F124D; color: #fff; border: none; border-radius: 999px; padding: 0.7rem 1.6rem; font-weight: 700; cursor: pointer; }
    .btn:disabled { opacity: 0.6; cursor: default; }

    .ok { background: #f0fdf4; border: 1px solid #bbf7d0; color: #166534; border-radius: 0.6rem; padding: 0.85rem 1rem; }
    .err { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; border-radius: 0.6rem; padding: 0.7rem 1rem; margin-bottom: 0.9rem; }
    .back { display: inline-block; margin-top: 1.5rem; color: #2F124D; text-decoration: none; font-weight: 600; }
    .back:hover { text-decoration: underline; }
</style>
