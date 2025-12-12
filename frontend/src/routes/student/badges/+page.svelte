<script>
    let { data } = $props();

    let badges = $derived(data?.badges ?? []);
    let earnedCount = $derived(badges.filter(b => b.earned).length);
    let totalCount = $derived(badges.length);

    function formatDate(dateString) {
        if (!dateString) return "";
        return new Date(dateString).toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", year: "numeric"
        });
    }

    // Gruppiere Badges nach Kategorie
    let sessionBadges = $derived(badges.filter(b => b.badge.ruleType === "SESSIONS_COMPLETED"));
    let noteBadges = $derived(badges.filter(b => b.badge.ruleType === "NOTES_CREATED"));
    let applicationCountBadges = $derived(badges.filter(b => b.badge.ruleType === "APPLICATIONS_CREATED"));
    let applicationStatusBadges = $derived(badges.filter(b => b.badge.ruleType === "APPLICATION_STATUS"));
    let submissionBadges = $derived(badges.filter(b => b.badge.ruleType === "SUBMISSIONS_COMPLETED"));
    let feedbackBadges = $derived(badges.filter(b => b.badge.ruleType === "FEEDBACK_RECEIVED"));
    let gradeBadges = $derived(badges.filter(b => b.badge.ruleType === "GRADES_RECEIVED"));
</script>

<svelte:head>
    <title>Meine Badges – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">🏅 Meine Badges</h1>
            <p class="subtitle">Sammle Badges durch deine Aktivitäten!</p>
        </div>
        <div class="progress-card">
            <span class="progress-number">{earnedCount}/{totalCount}</span>
            <span class="progress-label">Badges verdient</span>
        </div>
    </header>

    {#if data?.error}
        <div class="alert alert-danger">{data.error}</div>
    {/if}

    <!-- Progress Bar -->
    <div class="overall-progress">
        <div class="progress-bar">
            <div class="progress-fill" style="width: {totalCount > 0 ? (earnedCount / totalCount * 100) : 0}%"></div>
        </div>
        <span class="progress-text">{Math.round(earnedCount / totalCount * 100 || 0)}% abgeschlossen</span>
    </div>

    <!-- Badge Sections -->
    {#snippet badgeSection(title, badgeList)}
        {#if badgeList.length > 0}
            <section class="badge-section">
                <h2 class="section-title">{title}</h2>
                <div class="badge-grid">
                    {#each badgeList as item}
                        <div class="badge-card" class:earned={item.earned} class:locked={!item.earned}>
                            <div class="badge-icon">{item.badge.icon}</div>
                            <div class="badge-info">
                                <h3 class="badge-title">{item.badge.title}</h3>
                                <p class="badge-desc">{item.badge.description}</p>
                                {#if item.earned}
                                    <span class="badge-date">✓ Verdient am {formatDate(item.earnedAt)}</span>
                                {:else}
                                    <span class="badge-locked">🔒 Noch nicht freigeschaltet</span>
                                {/if}
                            </div>
                        </div>
                    {/each}
                </div>
            </section>
        {/if}
    {/snippet}

    {@render badgeSection("🤖 KI-Training", sessionBadges)}
    {@render badgeSection("📝 Notizen", noteBadges)}
    {@render badgeSection("💼 Bewerbungen", applicationCountBadges)}
    {@render badgeSection("🎯 Bewerbungs-Meilensteine", applicationStatusBadges)}
    {@render badgeSection("✅ Aufgaben-Abgaben", submissionBadges)}
    {@render badgeSection("💬 Feedback", feedbackBadges)}
    {@render badgeSection("🎓 Bewertungen", gradeBadges)}

    <!-- Legende -->
    <div class="legend">
        <div class="legend-item">
            <span class="legend-badge earned-example">🎯</span>
            <span>Verdient</span>
        </div>
        <div class="legend-item">
            <span class="legend-badge locked-example">🔒</span>
            <span>Noch nicht freigeschaltet</span>
        </div>
    </div>
</div>

<style>
    /* ===== PAGE SPECIFIC STYLES ===== */
    .progress-card {
        background: var(--color-primary);
        color: #fff;
        padding: var(--space-md) var(--space-xl);
        border-radius: var(--radius-lg);
        text-align: center;
    }

    .progress-number {
        display: block;
        font-size: var(--font-size-2xl);
        font-weight: 700;
    }

    .progress-label {
        font-size: var(--font-size-xs);
        opacity: 0.9;
    }

    .overall-progress {
        margin-bottom: var(--space-2xl);
    }

    .progress-bar {
        height: 8px;
        background: var(--color-border-input);
        border-radius: 4px;
        overflow: hidden;
        margin-bottom: var(--space-sm);
    }

    .progress-fill {
        height: 100%;
        background: var(--color-primary);
        border-radius: 4px;
        transition: width 0.5s ease;
    }

    .progress-text {
        font-size: var(--font-size-sm);
        color: var(--color-text-muted);
    }

    .badge-section {
        margin-bottom: var(--space-2xl);
    }

    .section-title {
        font-size: var(--font-size-lg);
        font-weight: 600;
        margin: 0 0 var(--space-lg);
        color: var(--color-text-secondary);
        padding-bottom: var(--space-sm);
        border-bottom: 2px solid var(--color-border-input);
    }

    .badge-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: var(--space-lg);
    }

    .badge-card {
        display: flex;
        gap: var(--space-lg);
        padding: var(--space-lg);
        background: var(--color-bg-card);
        border: 2px solid var(--color-border-input);
        border-radius: var(--radius-lg);
        transition: all var(--transition-base);
    }

    .badge-card.earned {
        background: var(--color-bg-muted);
        border-color: var(--color-primary);
    }

    .badge-card.earned:hover {
        transform: translateY(-2px);
        box-shadow: var(--shadow-primary);
    }

    .badge-card.locked {
        background: #f9fafb;
        border-color: #e5e7eb;
    }

    .badge-card.locked .badge-icon {
        filter: grayscale(100%);
        opacity: 0.5;
    }

    .badge-card.locked .badge-title,
    .badge-card.locked .badge-desc {
        color: #9ca3af;
    }

    .badge-icon {
        font-size: 2.5rem;
        flex-shrink: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 60px;
        height: 60px;
        background: var(--color-bg-card);
        border-radius: 50%;
        box-shadow: var(--shadow-sm);
    }

    .badge-card.earned .badge-icon {
        background: var(--color-primary);
        box-shadow: var(--shadow-primary);
    }

    .badge-info {
        flex: 1;
        min-width: 0;
    }

    .badge-title {
        margin: 0 0 var(--space-xs);
        font-size: var(--font-size-base);
        font-weight: 600;
        color: var(--color-text-secondary);
    }

    .badge-desc {
        margin: 0 0 var(--space-sm);
        font-size: var(--font-size-sm);
        color: var(--color-text-muted);
    }

    .badge-date {
        font-size: var(--font-size-xs);
        color: var(--color-success);
        font-weight: 500;
    }

    .badge-locked {
        font-size: var(--font-size-xs);
        color: #9ca3af;
    }

    .legend {
        display: flex;
        gap: var(--space-2xl);
        justify-content: center;
        padding: var(--space-lg);
        background: #f9fafb;
        border-radius: var(--radius-md);
        margin-top: var(--space-2xl);
    }

    .legend-item {
        display: flex;
        align-items: center;
        gap: var(--space-sm);
        font-size: var(--font-size-sm);
        color: var(--color-text-muted);
    }

    .legend-badge {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        font-size: 1rem;
    }

    .earned-example {
        background: var(--color-primary);
    }

    .locked-example {
        background: #e5e7eb;
        filter: grayscale(100%);
    }

    @media (max-width: 600px) {
        .page-header {
            flex-direction: column;
        }

        .badge-grid {
            grid-template-columns: 1fr;
        }

        .legend {
            flex-direction: column;
            gap: var(--space-md);
            align-items: flex-start;
        }
    }
</style>
