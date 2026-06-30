<script>
    import { t } from "$lib/i18n";

    let sections = $derived([
        { t: $t("dp.s1.t"), p: $t("dp.s1.p") },
        { t: $t("dp.s2.t"), p: $t("dp.s2.p") },
        { t: $t("dp.s3.t"), p: $t("dp.s3.p") },
        { t: $t("dp.s4.t"), p: $t("dp.s4.p") },
        { t: $t("dp.s5.t"), p: $t("dp.s5.p") },
        { t: $t("dp.s6.t"), p: $t("dp.s6.p") },
        { t: $t("dp.s7.t"), p: $t("dp.s7.p") },
        { t: $t("dp.s8.t"), p: $t("dp.s8.p") },
        { t: $t("dp.s9.t"), p: $t("dp.s9.p") },
        { t: $t("dp.s10.t"), p: $t("dp.s10.p") },
        { t: $t("dp.s11.t"), p: $t("dp.s11.p") },
        { t: $t("dp.s12.t"), p: $t("dp.s12.p") }
    ]);

    let lastUpdated = "30.06.2026";
</script>

<svelte:head>
    <title>{$t('dp.title')} – Praesto</title>
</svelte:head>

<div class="privacy">
    <h1>{$t('dp.title')}</h1>
    <p class="intro">{$t('dp.intro')}</p>
    <p class="updated">Stand: {lastUpdated}</p>

    {#each sections as section}
        <section class="block">
            <h2>{section.t}</h2>
            <p>{section.p}</p>
        </section>
    {/each}

    <a href="/" class="back">← {$t('dp.backToStart')}</a>
</div>

<style>
    .privacy {
        max-width: 760px;
        margin: 0 auto;
        padding: 2rem 1rem 3rem;
    }

    h1 {
        margin: 0 0 0.5rem;
        color: var(--color-primary, #2F124D);
        font-size: clamp(1.6rem, 3vw, 2.1rem);
    }

    .intro {
        color: var(--color-text-muted, #5E4C6F);
        line-height: 1.6;
        margin: 0 0 1rem;
    }

    .updated {
        color: var(--color-text-muted, #8a7f9a);
        font-size: 0.85rem;
        margin: 0 0 1.75rem;
    }

    .block {
        margin-bottom: 1.5rem;
    }

    .block h2 {
        font-size: 1.1rem;
        color: var(--color-text-secondary, #2d2141);
        margin: 0 0 0.4rem;
    }

    .block p {
        margin: 0;
        color: var(--color-text-muted, #5E4C6F);
        line-height: 1.65;
    }

    .back {
        display: inline-block;
        margin-top: 1rem;
        color: var(--color-primary, #2F124D);
        text-decoration: none;
        font-weight: 600;
    }

    .back:hover {
        text-decoration: underline;
    }
</style>
