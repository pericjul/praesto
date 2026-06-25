<script>
    import { t } from "$lib/i18n";
    import logo from "$lib/assets/praesto-logo.png";

    let roleSections = $derived([
        {
            title: $t("landing.studentsTitle"),
            accent: "#8b5cf6",
            features: [
                { title: $t("landing.students1Title"), text: $t("landing.students1Text") },
                { title: $t("landing.students2Title"), text: $t("landing.students2Text") },
                { title: $t("landing.students3Title"), text: $t("landing.students3Text") }
            ]
        },
        {
            title: $t("landing.teachersTitle"),
            accent: "#3b82f6",
            features: [
                { title: $t("landing.teachers1Title"), text: $t("landing.teachers1Text") },
                { title: $t("landing.teachers2Title"), text: $t("landing.teachers2Text") },
                { title: $t("landing.teachers3Title"), text: $t("landing.teachers3Text") }
            ]
        },
        {
            title: $t("landing.adminsTitle"),
            accent: "#c97d3c",
            features: [
                { title: $t("landing.admins1Title"), text: $t("landing.admins1Text") },
                { title: $t("landing.admins2Title"), text: $t("landing.admins2Text") },
                { title: $t("landing.admins3Title"), text: $t("landing.admins3Text") }
            ]
        }
    ]);
</script>

<svelte:head>
    <title>Praesto – {$t('landing.heroTitle')}</title>
</svelte:head>

<div class="landing">
    <!-- Hero -->
    <section class="hero">
        <img src={logo} alt="Praesto" class="hero-logo" />
        <span class="eyebrow">{$t('landing.heroEyebrow')}</span>
        <h1>{$t('landing.heroTitle')}</h1>
        <p class="lead">{$t('landing.heroSubtitle')}</p>
        <div class="hero-cta">
            <a href="/demo#cta" class="btn-cta-primary">▶ {$t('landing.ctaDemo')}</a>
            <a href="/demo" class="btn-cta-ghost">{$t('landing.ctaGuide')}</a>
            <a href="/login" class="btn-cta-ghost">{$t('landing.ctaLogin')}</a>
        </div>
        <p class="demo-hint">{$t('landing.demoHint')}</p>
    </section>

    <!-- Rollen-Sektionen -->
    {#each roleSections as section}
        <section class="role-section">
            <h2 style="--accent: {section.accent}">{section.title}</h2>
            <div class="feature-grid">
                {#each section.features as f}
                    <article class="feature-card">
                        <h3>{f.title}</h3>
                        <p>{f.text}</p>
                    </article>
                {/each}
            </div>
        </section>
    {/each}

    <!-- Abschluss-CTA -->
    <section class="final-cta">
        <h2>{$t('landing.ctaFooterTitle')}</h2>
        <p>{$t('landing.ctaFooterText')}</p>
        <a href="/demo#cta" class="btn-cta-primary large">▶ {$t('landing.ctaDemo')}</a>
        <a href="/kontakt" class="cta-contact">{$t('contact.ctaOffer')} →</a>
    </section>
</div>

<style>
    .landing {
        max-width: 1000px;
        margin: 0 auto;
        padding: 2rem 1rem 1rem;
    }

    /* Hero */
    .hero {
        text-align: center;
        padding: 2rem 1rem 3rem;
    }

    .hero-logo {
        width: 72px;
        height: auto;
        margin-bottom: 1rem;
    }

    .eyebrow {
        display: inline-block;
        background: var(--color-accent-light, #fbe4b2);
        color: var(--color-primary, #2F124D);
        padding: 0.3rem 0.9rem;
        border-radius: 999px;
        font-size: 0.85rem;
        font-weight: 600;
        margin-bottom: 1rem;
    }

    .hero h1 {
        font-size: clamp(1.8rem, 4vw, 2.6rem);
        color: var(--color-primary, #2F124D);
        margin: 0 auto 1rem;
        max-width: 18ch;
        line-height: 1.15;
    }

    .lead {
        max-width: 60ch;
        margin: 0 auto 1.75rem;
        color: var(--color-text-muted, #5E4C6F);
        font-size: 1.05rem;
        line-height: 1.6;
    }

    .hero-cta {
        display: flex;
        gap: 0.75rem;
        justify-content: center;
        flex-wrap: wrap;
    }

    .btn-cta-primary {
        background: var(--color-primary, #2F124D);
        color: #fff;
        border: none;
        border-radius: 999px;
        padding: 0.8rem 1.8rem;
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
        box-shadow: 0 6px 16px rgba(47, 18, 77, 0.25);
        transition: all 0.15s;
    }

    .btn-cta-primary:hover {
        background: var(--color-primary-hover, #3D1863);
        transform: translateY(-1px);
    }

    .btn-cta-primary.large {
        padding: 0.95rem 2.4rem;
        font-size: 1.1rem;
    }

    .btn-cta-ghost {
        display: inline-flex;
        align-items: center;
        padding: 0.8rem 1.8rem;
        border-radius: 999px;
        border: 1px solid var(--color-primary, #2F124D);
        color: var(--color-primary, #2F124D);
        text-decoration: none;
        font-weight: 600;
    }

    .btn-cta-ghost:hover {
        background: var(--color-bg-hover, #f3f0f7);
    }

    .demo-hint {
        margin-top: 0.9rem;
        font-size: 0.85rem;
        color: var(--color-text-light, #6b647a);
    }

    /* Rollen */
    .role-section {
        margin-bottom: 2.5rem;
    }

    .role-section h2 {
        font-size: 1.4rem;
        color: var(--color-primary, #2F124D);
        margin: 0 0 1rem;
        padding-left: 0.75rem;
        border-left: 4px solid var(--accent, #8b5cf6);
    }

    .feature-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
        gap: 1rem;
    }

    .feature-card {
        background: var(--color-bg-card, #fff);
        border: 1px solid var(--color-border, #e6d9cc);
        border-radius: 1rem;
        padding: 1.25rem;
    }

    .feature-card h3 {
        margin: 0 0 0.5rem;
        font-size: 1.05rem;
        color: var(--color-text-secondary, #2d2141);
    }

    .feature-card p {
        margin: 0;
        color: var(--color-text-muted, #5E4C6F);
        line-height: 1.55;
        font-size: 0.95rem;
    }

    /* Final CTA */
    .final-cta {
        text-align: center;
        background: linear-gradient(135deg, #2F124D 0%, #5a2d6e 55%, #c97d3c 100%);
        color: #fff;
        border-radius: 1.5rem;
        padding: 2.5rem 1.5rem;
        margin: 1rem 0 2rem;
    }

    .final-cta h2 {
        margin: 0 0 0.5rem;
        font-size: 1.5rem;
    }

    .final-cta p {
        margin: 0 0 1.5rem;
        opacity: 0.9;
    }

    .final-cta .btn-cta-primary {
        background: var(--color-accent-light, #fbe4b2);
        color: var(--color-primary, #2F124D);
    }

    .final-cta .btn-cta-primary:hover {
        background: #ffe7b0;
    }

    .cta-contact {
        display: inline-block;
        margin-top: 1rem;
        color: #fff;
        text-decoration: underline;
        font-weight: 600;
        font-size: 0.95rem;
    }
</style>
