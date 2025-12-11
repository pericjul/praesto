<script>
    let { data } = $props();

    let badges = $derived(data?.badges ?? []);
    let earnedCount = $derived(badges.filter(b => b.earned).length);
    let totalCount = $derived(badges.length);

    function formatDate(dateString) {
        if (!dateString) return "";
        return new Date(dateString).toLocaleDateString("de-CH", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric"
        });
    }

    // Gruppiere Badges nach Kategorie (ruleType)
    let sessionBadges = $derived(badges.filter(b => 
        b.badge.ruleType === "SESSIONS_COMPLETED"
    ));
    let noteBadges = $derived(badges.filter(b => 
        b.badge.ruleType === "NOTES_CREATED"
    ));
    let applicationCountBadges = $derived(badges.filter(b => 
        b.badge.ruleType === "APPLICATIONS_CREATED"
    ));
    let applicationStatusBadges = $derived(badges.filter(b => 
        b.badge.ruleType === "APPLICATION_STATUS"
    ));
    let submissionBadges = $derived(badges.filter(b => 
        b.badge.ruleType === "SUBMISSIONS_COMPLETED"
    ));
    let feedbackBadges = $derived(badges.filter(b => 
        b.badge.ruleType === "FEEDBACK_RECEIVED"
    ));
    let gradeBadges = $derived(badges.filter(b => 
        b.badge.ruleType === "GRADES_RECEIVED"
    ));
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
            <div 
                class="progress-fill" 
                style="width: {totalCount > 0 ? (earnedCount / totalCount * 100) : 0}%"
            ></div>
        </div>
        <span class="progress-text">{Math.round(earnedCount / totalCount * 100 || 0)}% abgeschlossen</span>
    </div>

    <!-- KI-Training Badges -->
    {#if sessionBadges.length > 0}
        <section class="badge-section">
            <h2 class="section-title">🤖 KI-Training</h2>
            <div class="badge-grid">
                {#each sessionBadges as item}
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

    <!-- Notizen Badges -->
    {#if noteBadges.length > 0}
        <section class="badge-section">
            <h2 class="section-title">📝 Notizen</h2>
            <div class="badge-grid">
                {#each noteBadges as item}
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

    <!-- Bewerbungen Anzahl Badges -->
    {#if applicationCountBadges.length > 0}
        <section class="badge-section">
            <h2 class="section-title">💼 Bewerbungen</h2>
            <div class="badge-grid">
                {#each applicationCountBadges as item}
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

    <!-- Bewerbungs-Status Badges -->
    {#if applicationStatusBadges.length > 0}
        <section class="badge-section">
            <h2 class="section-title">🎯 Bewerbungs-Meilensteine</h2>
            <div class="badge-grid">
                {#each applicationStatusBadges as item}
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

    <!-- Aufgaben-Abgabe Badges -->
    {#if submissionBadges.length > 0}
        <section class="badge-section">
            <h2 class="section-title">✅ Aufgaben-Abgaben</h2>
            <div class="badge-grid">
                {#each submissionBadges as item}
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

    <!-- Feedback Badges -->
    {#if feedbackBadges.length > 0}
        <section class="badge-section">
            <h2 class="section-title">💬 Feedback</h2>
            <div class="badge-grid">
                {#each feedbackBadges as item}
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

    <!-- Noten Badges -->
    {#if gradeBadges.length > 0}
        <section class="badge-section">
            <h2 class="section-title">🎓 Bewertungen</h2>
            <div class="badge-grid">
                {#each gradeBadges as item}
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
    .page-wrapper {
        max-width: 900px;
        margin: 0 auto;
        padding: 1.5rem 1rem 3rem;
    }

    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 1.5rem;
        gap: 1rem;
        flex-wrap: wrap;
    }

    .title {
        font-size: 1.6rem;
        font-weight: 700;
        margin: 0;
        color: #2d2141;
    }

    .subtitle {
        margin: 0.25rem 0 0;
        color: #6b647a;
        font-size: 0.9rem;
    }

    .progress-card {
        background: linear-gradient(135deg, #3b134f, #5a2d6e);
        color: #fff;
        padding: 0.75rem 1.25rem;
        border-radius: 0.75rem;
        text-align: center;
    }

    .progress-number {
        display: block;
        font-size: 1.5rem;
        font-weight: 700;
    }

    .progress-label {
        font-size: 0.8rem;
        opacity: 0.9;
    }

    /* Overall Progress */
    .overall-progress {
        margin-bottom: 2rem;
    }

    .progress-bar {
        height: 8px;
        background: #e8e0f0;
        border-radius: 4px;
        overflow: hidden;
        margin-bottom: 0.5rem;
    }

    .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, #7c3aed, #a855f7);
        border-radius: 4px;
        transition: width 0.5s ease;
    }

    .progress-text {
        font-size: 0.85rem;
        color: #6b7280;
    }

    /* Badge Sections */
    .badge-section {
        margin-bottom: 2rem;
    }

    .section-title {
        font-size: 1.1rem;
        font-weight: 600;
        margin: 0 0 1rem;
        color: #2d2141;
        padding-bottom: 0.5rem;
        border-bottom: 2px solid #e8e0f0;
    }

    .badge-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 1rem;
    }

    .badge-card {
        display: flex;
        gap: 1rem;
        padding: 1rem;
        background: #fff;
        border: 2px solid #e8e0f0;
        border-radius: 0.75rem;
        transition: all 0.2s;
    }

    .badge-card.earned {
        background: linear-gradient(135deg, #faf5ff 0%, #f3e8ff 100%);
        border-color: #c4b5fd;
    }

    .badge-card.earned:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(124, 58, 237, 0.15);
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
        background: #fff;
        border-radius: 50%;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }

    .badge-card.earned .badge-icon {
        background: linear-gradient(135deg, #7c3aed, #a855f7);
        box-shadow: 0 4px 12px rgba(124, 58, 237, 0.3);
    }

    .badge-info {
        flex: 1;
        min-width: 0;
    }

    .badge-title {
        margin: 0 0 0.25rem;
        font-size: 1rem;
        font-weight: 600;
        color: #2d2141;
    }

    .badge-desc {
        margin: 0 0 0.5rem;
        font-size: 0.85rem;
        color: #6b7280;
    }

    .badge-date {
        font-size: 0.75rem;
        color: #059669;
        font-weight: 500;
    }

    .badge-locked {
        font-size: 0.75rem;
        color: #9ca3af;
    }

    /* Legend */
    .legend {
        display: flex;
        gap: 2rem;
        justify-content: center;
        padding: 1rem;
        background: #f9fafb;
        border-radius: 0.5rem;
        margin-top: 2rem;
    }

    .legend-item {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 0.85rem;
        color: #6b7280;
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
        background: linear-gradient(135deg, #7c3aed, #a855f7);
    }

    .locked-example {
        background: #e5e7eb;
        filter: grayscale(100%);
    }

    /* Alert */
    .alert {
        padding: 0.75rem 1rem;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
    }

    .alert-danger {
        background: #fef2f2;
        color: #dc2626;
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
            gap: 0.75rem;
            align-items: flex-start;
        }
    }
</style>