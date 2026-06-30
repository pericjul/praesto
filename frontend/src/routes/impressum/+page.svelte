<script>
    import { t } from "$lib/i18n";

    let sections = $derived([
        { t: $t("imp.operatorT"), p: $t("imp.operatorP") },
        { t: $t("imp.contactT"), p: $t("imp.contactP") },
        { t: $t("imp.disclaimerT"), p: $t("imp.disclaimerP") }
    ]);
</script>

<svelte:head>
    <title>{$t('imp.title')} – Praesto</title>
</svelte:head>

<div class="imprint">
    <h1>{$t('imp.title')}</h1>

    {#each sections as section}
        <section class="block">
            <h2>{section.t}</h2>
            <p>{section.p}</p>
        </section>
    {/each}

    <a href="/" class="back">← {$t('imp.backToStart')}</a>
</div>

<style>
    .imprint { max-width: 700px; margin: 0 auto; padding: 2rem 1rem 3rem; }
    h1 { margin: 0 0 1rem; color: var(--color-primary, #2F124D); font-size: clamp(1.6rem, 3vw, 2.1rem); }
    .block { margin-bottom: 1.5rem; }
    .block h2 { font-size: 1.1rem; color: var(--color-text-secondary, #2d2141); margin: 0 0 0.4rem; }
    .block p { margin: 0; color: var(--color-text-muted, #5E4C6F); line-height: 1.65; }
    .back { display: inline-block; margin-top: 1rem; color: var(--color-primary, #2F124D); text-decoration: none; font-weight: 600; }
    .back:hover { text-decoration: underline; }
</style>
