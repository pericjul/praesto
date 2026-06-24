<script>
    import { enhance } from "$app/forms";
    import logo from "$lib/assets/praesto-logo.png";
    import { t } from "$lib/i18n";

    let { form } = $props();

    let sections = $derived([
        {
            id: "intro", icon: "📘", title: $t("hb.intro.title"),
            blocks: [
                { type: "p", text: $t("hb.intro.p1") },
                { type: "p", text: $t("hb.intro.p2") }
            ]
        },
        {
            id: "start", icon: "🚀", title: $t("hb.start.title"),
            blocks: [
                { type: "p", text: $t("hb.start.intro") },
                { type: "steps", items: [
                    { t: $t("hb.start.s1t"), d: $t("hb.start.s1d") },
                    { t: $t("hb.start.s2t"), d: $t("hb.start.s2d") },
                    { t: $t("hb.start.s3t"), d: $t("hb.start.s3d") },
                    { t: $t("hb.start.s4t"), d: $t("hb.start.s4d") }
                ] }
            ]
        },
        {
            id: "admin", icon: "🏫", title: $t("hb.admin.title"),
            blocks: [
                { type: "p", text: $t("hb.admin.intro") },
                { type: "list", items: [
                    { t: $t("hb.admin.l1t"), d: $t("hb.admin.l1d") },
                    { t: $t("hb.admin.l2t"), d: $t("hb.admin.l2d") },
                    { t: $t("hb.admin.l3t"), d: $t("hb.admin.l3d") },
                    { t: $t("hb.admin.l4t"), d: $t("hb.admin.l4d") }
                ] },
                { type: "tip", text: $t("hb.admin.tip") }
            ]
        },
        {
            id: "teacher", icon: "👩‍🏫", title: $t("hb.teacher.title"),
            blocks: [
                { type: "p", text: $t("hb.teacher.intro") },
                { type: "h", text: $t("hb.teacher.classesH") },
                { type: "p", text: $t("hb.teacher.classesP") },
                { type: "h", text: $t("hb.teacher.studentsH") },
                { type: "p", text: $t("hb.teacher.studentsP") },
                { type: "h", text: $t("hb.teacher.asgH") },
                { type: "p", text: $t("hb.teacher.asgP") },
                { type: "list", items: [
                    { t: $t("hb.teacher.typeInterviewT"), d: $t("hb.teacher.typeInterviewD") },
                    { t: $t("hb.teacher.typeDocT"), d: $t("hb.teacher.typeDocD") },
                    { t: $t("hb.teacher.typeReflectionT"), d: $t("hb.teacher.typeReflectionD") },
                    { t: $t("hb.teacher.typeVideoT"), d: $t("hb.teacher.typeVideoD") },
                    { t: $t("hb.teacher.typeResearchT"), d: $t("hb.teacher.typeResearchD") }
                ] },
                { type: "h", text: $t("hb.teacher.fbH") },
                { type: "p", text: $t("hb.teacher.fbP") },
                { type: "h", text: $t("hb.teacher.exportH") },
                { type: "p", text: $t("hb.teacher.exportP") }
            ]
        },
        {
            id: "student", icon: "🎓", title: $t("hb.student.title"),
            blocks: [
                { type: "p", text: $t("hb.student.intro") },
                { type: "h", text: $t("hb.student.trainH") },
                { type: "p", text: $t("hb.student.trainP") },
                { type: "tip", text: $t("hb.student.trainTip") },
                { type: "h", text: $t("hb.student.taskH") },
                { type: "p", text: $t("hb.student.taskP") },
                { type: "h", text: $t("hb.student.noteH") },
                { type: "p", text: $t("hb.student.noteP") },
                { type: "h", text: $t("hb.student.appH") },
                { type: "p", text: $t("hb.student.appP") },
                { type: "h", text: $t("hb.student.badgeH") },
                { type: "p", text: $t("hb.student.badgeP") }
            ]
        },
        {
            id: "roles", icon: "👥", title: $t("hb.roles.title"),
            blocks: [
                { type: "p", text: $t("hb.roles.intro") },
                { type: "list", items: [
                    { t: $t("hb.roles.superT"), d: $t("hb.roles.superD") },
                    { t: $t("hb.roles.adminT"), d: $t("hb.roles.adminD") },
                    { t: $t("hb.roles.teacherT"), d: $t("hb.roles.teacherD") },
                    { t: $t("hb.roles.studentT"), d: $t("hb.roles.studentD") },
                    { t: $t("hb.roles.demoT"), d: $t("hb.roles.demoD") }
                ] }
            ]
        },
        {
            id: "privacy", icon: "🔒", title: $t("hb.privacy.title"),
            blocks: [
                { type: "p", text: $t("hb.privacy.p1") },
                { type: "p", text: $t("hb.privacy.p2") },
                { type: "h", text: $t("hb.privacy.secH") },
                { type: "p", text: $t("hb.privacy.secP") }
            ]
        },
        {
            id: "faq", icon: "❓", title: $t("hb.faq.title"),
            blocks: [
                { type: "faq", items: [
                    { q: $t("hb.faq.q1"), a: $t("hb.faq.a1") },
                    { q: $t("hb.faq.q2"), a: $t("hb.faq.a2") },
                    { q: $t("hb.faq.q3"), a: $t("hb.faq.a3") },
                    { q: $t("hb.faq.q4"), a: $t("hb.faq.a4") },
                    { q: $t("hb.faq.q5"), a: $t("hb.faq.a5") },
                    { q: $t("hb.faq.q6"), a: $t("hb.faq.a6") }
                ] }
            ]
        },
        {
            id: "support", icon: "✉️", title: $t("hb.support.title"),
            blocks: [
                { type: "p", text: $t("hb.support.p1") }
            ]
        }
    ]);
</script>

<svelte:head>
    <title>{$t('hb.title')} – Praesto</title>
</svelte:head>

<div class="handbook">
    <header class="hb-head">
        <img src={logo} alt="Praesto" class="hb-logo" />
        <h1>{$t('hb.title')}</h1>
        <p class="hb-sub">{$t('hb.subtitle')}</p>
    </header>

    <div class="hb-layout">
        <!-- Inhaltsverzeichnis -->
        <aside class="toc">
            <span class="toc-label">{$t('hb.toc')}</span>
            <nav>
                {#each sections as section}
                    <a href={`#${section.id}`}>{section.icon} {section.title}</a>
                {/each}
            </nav>
        </aside>

        <!-- Inhalt -->
        <main class="hb-content">
            {#each sections as section}
                <section id={section.id} class="hb-section">
                    <h2>{section.icon} {section.title}</h2>
                    {#each section.blocks as block}
                        {#if block.type === "p"}
                            <p>{block.text}</p>
                        {:else if block.type === "h"}
                            <h3>{block.text}</h3>
                        {:else if block.type === "tip"}
                            <div class="tip">💡 {block.text}</div>
                        {:else if block.type === "steps"}
                            <ol class="steps">
                                {#each block.items as item}
                                    <li><strong>{item.t}</strong><span>{item.d}</span></li>
                                {/each}
                            </ol>
                        {:else if block.type === "list"}
                            <ul class="deflist">
                                {#each block.items as item}
                                    <li><strong>{item.t}</strong> — {item.d}</li>
                                {/each}
                            </ul>
                        {:else if block.type === "faq"}
                            <div class="faqs">
                                {#each block.items as item}
                                    <details class="faq">
                                        <summary>{item.q}</summary>
                                        <p>{item.a}</p>
                                    </details>
                                {/each}
                            </div>
                        {/if}
                    {/each}
                </section>
            {/each}

            <!-- CTA: Rolle wählen und wirklich ausprobieren -->
            <section class="cta" id="cta">
                <h2>{$t('guide.ctaTitle')}</h2>
                <p>{$t('demo.chooseRole')}</p>
                {#if form?.error}
                    <div class="alert">{form.error}</div>
                {/if}
                <div class="role-buttons">
                    <form method="POST" use:enhance>
                        <input type="hidden" name="as" value="student" />
                        <button type="submit" class="cta-btn">{$t('demo.tryStudent')}</button>
                    </form>
                    <form method="POST" use:enhance>
                        <input type="hidden" name="as" value="teacher" />
                        <button type="submit" class="cta-btn">{$t('demo.tryTeacher')}</button>
                    </form>
                    <form method="POST" use:enhance>
                        <input type="hidden" name="as" value="admin" />
                        <button type="submit" class="cta-btn">{$t('demo.tryAdmin')}</button>
                    </form>
                </div>
                <a href="/kontakt" class="back">{$t('contact.ctaOffer')} →</a>
                <a href="/" class="back">{$t('hb.backToStart')}</a>
            </section>
        </main>
    </div>
</div>

<style>
    .handbook {
        max-width: 1100px;
        margin: 0 auto;
        padding: 2rem 1rem 3rem;
    }

    .hb-head {
        text-align: center;
        margin-bottom: 2rem;
    }

    .hb-logo { width: 60px; height: auto; margin-bottom: 0.5rem; }

    .hb-head h1 {
        margin: 0 0 0.5rem;
        font-size: clamp(1.6rem, 3vw, 2.2rem);
        color: var(--color-primary, #2F124D);
    }

    .hb-sub {
        max-width: 60ch;
        margin: 0 auto;
        color: var(--color-text-muted, #5E4C6F);
    }

    .hb-layout {
        display: grid;
        grid-template-columns: 240px 1fr;
        gap: 2rem;
        align-items: start;
    }

    /* TOC */
    .toc {
        position: sticky;
        top: calc(var(--header-height, 54px) + 1rem);
        background: var(--color-bg-card, #fff);
        border: 1px solid var(--color-border, #e6d9cc);
        border-radius: 1rem;
        padding: 1rem;
    }

    .toc-label {
        display: block;
        font-size: 0.75rem;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: var(--color-text-light, #6b647a);
        margin-bottom: 0.5rem;
    }

    .toc nav {
        display: flex;
        flex-direction: column;
        gap: 0.15rem;
    }

    .toc a {
        text-decoration: none;
        color: var(--color-text-secondary, #2d2141);
        font-size: 0.9rem;
        padding: 0.4rem 0.5rem;
        border-radius: 0.5rem;
    }

    .toc a:hover {
        background: var(--color-bg-hover, #f3f0f7);
        color: var(--color-primary, #2F124D);
    }

    /* Content */
    .hb-section {
        background: var(--color-bg-card, #fff);
        border: 1px solid var(--color-border, #e6d9cc);
        border-radius: 1rem;
        padding: 1.5rem 1.75rem;
        margin-bottom: 1.25rem;
        scroll-margin-top: calc(var(--header-height, 54px) + 1rem);
    }

    .hb-section h2 {
        margin: 0 0 1rem;
        color: var(--color-primary, #2F124D);
        font-size: 1.3rem;
    }

    .hb-section h3 {
        margin: 1.25rem 0 0.4rem;
        color: var(--color-text-secondary, #2d2141);
        font-size: 1.05rem;
    }

    .hb-section p {
        margin: 0 0 0.75rem;
        color: var(--color-text-muted, #5E4C6F);
        line-height: 1.65;
    }

    .tip {
        background: var(--color-warning-bg, #fffbeb);
        border: 1px solid var(--color-warning-border, #fcd34d);
        color: #78600e;
        border-radius: 0.6rem;
        padding: 0.75rem 1rem;
        margin: 0.5rem 0 0.75rem;
        line-height: 1.55;
    }

    .steps {
        margin: 0 0 0.5rem;
        padding: 0;
        list-style: none;
        counter-reset: step;
        display: grid;
        gap: 0.6rem;
    }

    .steps li {
        counter-increment: step;
        position: relative;
        padding-left: 2.4rem;
        line-height: 1.55;
    }

    .steps li::before {
        content: counter(step);
        position: absolute;
        left: 0;
        top: 0;
        width: 1.7rem;
        height: 1.7rem;
        background: var(--color-primary, #2F124D);
        color: #fff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.85rem;
        font-weight: 700;
    }

    .steps li strong {
        display: block;
        color: var(--color-text-secondary, #2d2141);
    }

    .steps li span {
        color: var(--color-text-muted, #5E4C6F);
        font-size: 0.95rem;
    }

    .deflist {
        margin: 0 0 0.75rem;
        padding-left: 1.1rem;
        display: grid;
        gap: 0.4rem;
    }

    .deflist li {
        color: var(--color-text-muted, #5E4C6F);
        line-height: 1.55;
    }

    .deflist li strong {
        color: var(--color-text-secondary, #2d2141);
    }

    .faqs {
        display: grid;
        gap: 0.5rem;
    }

    .faq {
        border: 1px solid var(--color-border, #e6d9cc);
        border-radius: 0.6rem;
        padding: 0.3rem 0.9rem;
    }

    .faq summary {
        cursor: pointer;
        font-weight: 600;
        color: var(--color-text-secondary, #2d2141);
        padding: 0.55rem 0;
    }

    .faq p {
        margin: 0 0 0.6rem;
    }

    /* CTA */
    .cta {
        text-align: center;
        background: linear-gradient(135deg, #2F124D 0%, #5a2d6e 55%, #c97d3c 100%);
        color: #fff;
        border-radius: 1.5rem;
        padding: 2.25rem 1.5rem;
        margin-top: 0.5rem;
    }

    .cta h2 { margin: 0 0 0.5rem; font-size: 1.4rem; }
    .cta p { margin: 0 0 1.1rem; opacity: 0.92; }

    .cta-btn {
        background: var(--color-accent-light, #fbe4b2);
        color: var(--color-primary, #2F124D);
        border: none;
        border-radius: 999px;
        padding: 0.85rem 2.1rem;
        font-size: 1.05rem;
        font-weight: 700;
        cursor: pointer;
    }

    .cta-btn:hover { background: #ffe7b0; }

    .role-buttons {
        display: flex;
        gap: 0.75rem;
        justify-content: center;
        flex-wrap: wrap;
    }

    .alert {
        background: rgba(255,255,255,0.15);
        border-radius: 0.5rem;
        padding: 0.6rem 1rem;
        margin-bottom: 0.75rem;
    }

    .readonly { margin-top: 0.9rem !important; font-size: 0.85rem; opacity: 0.85 !important; }

    .back {
        display: inline-block;
        margin-top: 0.6rem;
        color: #fff;
        text-decoration: underline;
        font-size: 0.9rem;
        opacity: 0.9;
    }

    @media (max-width: 820px) {
        .hb-layout { grid-template-columns: 1fr; }
        .toc { position: static; }
    }
</style>
