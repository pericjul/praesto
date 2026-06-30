<script>
    import { t } from "$lib/i18n";

    let docs = $derived([
        { href: "/impressum", title: $t("imp.title"), desc: $t("legal.imprintDesc"), icon: "🏢" },
        { href: "/datenschutz", title: $t("dp.title"), desc: $t("legal.privacyDesc"), icon: "🔒" }
    ]);
</script>

<svelte:head>
    <title>{$t('legal.title')} – Praesto</title>
</svelte:head>

<div class="legal">
    <h1>{$t('legal.title')}</h1>
    <p class="intro">{$t('legal.intro')}</p>

    <div class="cards">
        {#each docs as d}
            <a class="doc-card" href={d.href}>
                <span class="ic">{d.icon}</span>
                <span class="tx">
                    <strong>{d.title}</strong>
                    <span class="ds">{d.desc}</span>
                </span>
                <span class="arr">→</span>
            </a>
        {/each}
    </div>

    <a href="/" class="back">← {$t('legal.back')}</a>
</div>

<style>
    .legal { max-width: 700px; margin: 0 auto; padding: 2rem 1rem 3rem; }
    h1 { margin: 0 0 0.5rem; color: var(--color-primary, #2F124D); font-size: clamp(1.6rem, 3vw, 2.1rem); }
    .intro { color: var(--color-text-muted, #5E4C6F); line-height: 1.6; margin: 0 0 1.75rem; }

    .cards { display: flex; flex-direction: column; gap: 0.85rem; }
    .doc-card {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1.1rem 1.25rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 1rem;
        text-decoration: none;
        color: inherit;
        transition: border-color 0.15s, transform 0.15s, box-shadow 0.15s;
    }
    .doc-card:hover {
        border-color: var(--color-primary, #2F124D);
        transform: translateY(-2px);
        box-shadow: 0 10px 24px rgba(47, 18, 77, 0.08);
    }
    .ic { font-size: 1.6rem; flex-shrink: 0; }
    .tx { display: flex; flex-direction: column; gap: 0.15rem; flex: 1; min-width: 0; }
    .tx strong { color: var(--color-primary, #2F124D); font-size: 1.05rem; }
    .ds { color: var(--color-text-muted, #6b647a); font-size: 0.88rem; }
    .arr { color: var(--color-primary, #2F124D); font-size: 1.2rem; flex-shrink: 0; }

    .back { display: inline-block; margin-top: 1.5rem; color: var(--color-primary, #2F124D); text-decoration: none; font-weight: 600; }
    .back:hover { text-decoration: underline; }
</style>
