<script>
    import { t } from "$lib/i18n";
    import logo from "$lib/assets/praesto-logo.png";

    // Emoji vom Titel trennen → eigenes Icon-Chip
    function splitIcon(title) {
        const parts = title.trim().split(" ");
        if (parts.length > 1 && /\p{Extended_Pictographic}/u.test(parts[0])) {
            return { icon: parts[0], label: parts.slice(1).join(" ") };
        }
        return { icon: "•", label: title };
    }

    let roleSections = $derived([
        {
            icon: "🎓",
            title: $t("landing.studentsTitle"),
            accent: "#8b5cf6",
            tint: "#f3effc",
            features: [
                { title: $t("landing.students1Title"), text: $t("landing.students1Text") },
                { title: $t("landing.students2Title"), text: $t("landing.students2Text") },
                { title: $t("landing.students3Title"), text: $t("landing.students3Text") }
            ]
        },
        {
            icon: "👩‍🏫",
            title: $t("landing.teachersTitle"),
            accent: "#3b82f6",
            tint: "#eaf1fe",
            features: [
                { title: $t("landing.teachers1Title"), text: $t("landing.teachers1Text") },
                { title: $t("landing.teachers2Title"), text: $t("landing.teachers2Text") },
                { title: $t("landing.teachers3Title"), text: $t("landing.teachers3Text") }
            ]
        },
        {
            icon: "🏫",
            title: $t("landing.adminsTitle"),
            accent: "#c97d3c",
            tint: "#fbf1e6",
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
        <div class="hero-inner">
            <img src={logo} alt="Praesto" class="hero-logo" />
            <span class="eyebrow">{$t('landing.heroEyebrow')}</span>
            <h1>{$t('landing.heroTitle')}</h1>
            <p class="lead">{$t('landing.heroSubtitle')}</p>
            <div class="hero-cta">
                <a href="/demo#cta" class="btn-primary">▶ {$t('landing.ctaDemo')}</a>
                <a href="/demo" class="btn-ghost">{$t('landing.ctaGuide')}</a>
                <a href="/login" class="btn-text">{$t('landing.ctaLogin')}</a>
            </div>
            <p class="demo-hint">🔒 {$t('landing.demoHint')}</p>
        </div>
    </section>

    <!-- Rollen -->
    <div class="roles">
        {#each roleSections as section}
            <section class="role-block">
                <header class="role-head">
                    <span class="role-icon" style="background:{section.tint}">{section.icon}</span>
                    <h2 style="--accent:{section.accent}">{section.title}</h2>
                </header>
                <div class="feature-grid">
                    {#each section.features as f}
                        {@const fi = splitIcon(f.title)}
                        <article class="feature-card" style="--accent:{section.accent}">
                            <span class="f-icon" style="background:{section.tint}">{fi.icon}</span>
                            <h3>{fi.label}</h3>
                            <p>{f.text}</p>
                        </article>
                    {/each}
                </div>
            </section>
        {/each}
    </div>

    <!-- Abschluss-CTA -->
    <section class="final-cta">
        <h2>{$t('landing.ctaFooterTitle')}</h2>
        <p>{$t('landing.ctaFooterText')}</p>
        <div class="final-actions">
            <a href="/demo#cta" class="btn-primary large light">▶ {$t('landing.ctaDemo')}</a>
            <a href="/kontakt" class="cta-contact">{$t('contact.ctaOffer')} →</a>
        </div>
    </section>
</div>

<style>
    .landing {
        max-width: 1080px;
        margin: 0 auto;
        padding: 1.5rem 1rem 2rem;
    }

    /* ===== Hero ===== */
    .hero {
        background:
            radial-gradient(1200px 400px at 50% -10%, rgba(139, 92, 246, 0.12), transparent 70%),
            linear-gradient(180deg, #faf8fc 0%, #ffffff 100%);
        border: 1px solid var(--color-border, #e6d9cc);
        border-radius: 1.75rem;
        padding: 3rem 1.5rem 3.25rem;
        text-align: center;
        margin-bottom: 3rem;
    }
    .hero-inner { max-width: 720px; margin: 0 auto; }
    .hero-logo { width: 64px; height: auto; margin-bottom: 1.25rem; }

    .eyebrow {
        display: inline-block;
        background: var(--color-accent-light, #fbe4b2);
        color: var(--color-primary, #2F124D);
        padding: 0.35rem 0.95rem;
        border-radius: 999px;
        font-size: 0.8rem;
        font-weight: 700;
        letter-spacing: 0.02em;
        text-transform: uppercase;
        margin-bottom: 1.1rem;
    }

    .hero h1 {
        font-size: clamp(2rem, 4.5vw, 3rem);
        color: var(--color-primary, #2F124D);
        margin: 0 auto 1rem;
        max-width: 20ch;
        line-height: 1.12;
        letter-spacing: -0.01em;
    }

    .lead {
        max-width: 60ch;
        margin: 0 auto 2rem;
        color: var(--color-text-muted, #5E4C6F);
        font-size: 1.1rem;
        line-height: 1.6;
    }

    .hero-cta {
        display: flex;
        gap: 0.75rem;
        justify-content: center;
        align-items: center;
        flex-wrap: wrap;
    }

    /* ===== Buttons ===== */
    .btn-primary {
        background: var(--color-primary, #2F124D);
        color: #fff;
        border-radius: 999px;
        padding: 0.85rem 1.9rem;
        font-size: 1rem;
        font-weight: 700;
        text-decoration: none;
        box-shadow: 0 8px 20px rgba(47, 18, 77, 0.22);
        transition: transform 0.15s, box-shadow 0.15s, background 0.15s;
    }
    .btn-primary:hover { background: var(--color-primary-hover, #3D1863); transform: translateY(-2px); box-shadow: 0 12px 26px rgba(47, 18, 77, 0.28); }
    .btn-primary.large { padding: 1rem 2.5rem; font-size: 1.1rem; }
    .btn-primary.light { background: var(--color-accent-light, #fbe4b2); color: var(--color-primary, #2F124D); box-shadow: 0 8px 20px rgba(0,0,0,0.18); }
    .btn-primary.light:hover { background: #ffe7b0; }

    .btn-ghost {
        padding: 0.85rem 1.7rem;
        border-radius: 999px;
        border: 1.5px solid var(--color-primary, #2F124D);
        color: var(--color-primary, #2F124D);
        text-decoration: none;
        font-weight: 600;
        transition: background 0.15s;
    }
    .btn-ghost:hover { background: var(--color-bg-hover, #f3f0f7); }

    .btn-text {
        color: var(--color-text-muted, #5E4C6F);
        text-decoration: none;
        font-weight: 600;
        padding: 0.5rem 0.5rem;
    }
    .btn-text:hover { color: var(--color-primary, #2F124D); text-decoration: underline; }

    .demo-hint {
        margin-top: 1.1rem;
        font-size: 0.85rem;
        color: var(--color-text-light, #6b647a);
    }

    /* ===== Rollen ===== */
    .roles { display: flex; flex-direction: column; gap: 2.5rem; margin-bottom: 3rem; }

    .role-head {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        margin-bottom: 1.1rem;
    }
    .role-icon {
        width: 2.5rem; height: 2.5rem;
        display: grid; place-items: center;
        border-radius: 0.75rem;
        font-size: 1.3rem;
    }
    .role-head h2 {
        font-size: 1.45rem;
        color: var(--color-primary, #2F124D);
        margin: 0;
        position: relative;
        padding-bottom: 0.35rem;
    }
    .role-head h2::after {
        content: "";
        position: absolute; left: 0; bottom: 0;
        width: 2.5rem; height: 3px; border-radius: 2px;
        background: var(--accent, #8b5cf6);
    }

    .feature-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
        gap: 1.1rem;
    }

    .feature-card {
        background: var(--color-bg-card, #fff);
        border: 1px solid var(--color-border, #e6d9cc);
        border-radius: 1.1rem;
        padding: 1.4rem;
        transition: transform 0.15s, box-shadow 0.15s, border-color 0.15s;
    }
    .feature-card:hover {
        transform: translateY(-3px);
        box-shadow: 0 12px 28px rgba(47, 18, 77, 0.10);
        border-color: var(--accent, #8b5cf6);
    }
    .f-icon {
        width: 2.6rem; height: 2.6rem;
        display: grid; place-items: center;
        border-radius: 0.7rem;
        font-size: 1.35rem;
        margin-bottom: 0.85rem;
    }
    .feature-card h3 {
        margin: 0 0 0.45rem;
        font-size: 1.05rem;
        color: var(--color-text-secondary, #2d2141);
    }
    .feature-card p {
        margin: 0;
        color: var(--color-text-muted, #5E4C6F);
        line-height: 1.55;
        font-size: 0.95rem;
    }

    /* ===== Final CTA ===== */
    .final-cta {
        text-align: center;
        background: linear-gradient(135deg, #2F124D 0%, #5a2d6e 55%, #c97d3c 100%);
        color: #fff;
        border-radius: 1.75rem;
        padding: 3rem 1.5rem;
    }
    .final-cta h2 { margin: 0 0 0.5rem; font-size: clamp(1.5rem, 3vw, 2rem); }
    .final-cta p { margin: 0 0 1.75rem; opacity: 0.92; font-size: 1.05rem; }
    .final-actions { display: flex; flex-direction: column; align-items: center; gap: 0.9rem; }
    .cta-contact { color: #fff; text-decoration: underline; font-weight: 600; font-size: 0.95rem; opacity: 0.95; }
    .cta-contact:hover { opacity: 1; }

    @media (max-width: 640px) {
        .hero { padding: 2.25rem 1rem 2.5rem; }
        .btn-text { display: none; }
    }
</style>
