<script>
    import { enhance } from "$app/forms";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    // Reaktive sessions Liste (kann lokal geändert werden)
    let sessions = $state(data.sessions ?? []);

    // KI-Kontingent freies Üben (Lehrer-Aufgaben zählen nicht dazu)
    let practiceQuota = $derived(data.quota?.PRACTICE_INTERVIEW ?? null);
    let practiceLeft = $derived(practiceQuota ? practiceQuota.remaining : null);

    // Loading States
    let closingSessionId = $state(null);
    let deleteConfirmId = $state(null);
    let starting = $state(false);

    // KI-Session starten: Overlay zeigen, bis die Weiterleitung greift
    function handleStartEnhance() {
        starting = true;
        return async ({ result, update }) => {
            // Erfolg = redirect zur Chat-Seite -> Overlay bleibt bis Navigation.
            if (result.type === "failure" || result.type === "error") {
                starting = false;
            }
            await update();
        };
    }

    function handleDeleteEnhance(sessionId) {
        return () => {
            return async ({ result, update }) => {
                if (result.type === 'success') {
                    sessions = sessions.filter(s => s.id !== sessionId);
                }
                deleteConfirmId = null;
                await update({ reset: false });
            };
        };
    }

    function formatDateTime(date) {
        if (!date) return "-";
        return new Date(date).toLocaleString("de-CH", {
            dateStyle: "medium",
            timeStyle: "short"
        });
    }

    function getStatusColor(status) {
        return status === "OPEN" ? "badge-success" : "badge-muted";
    }

        // Enhance für Session beenden mit sofortigem UI-Update
    function handleCloseEnhance(sessionId) {
        return ({ cancel }) => {
            closingSessionId = sessionId;
            return async ({ result, update }) => {
                if (result.type === 'success') {
                    // Lokales Update der Session
                    sessions = sessions.map(s => 
                        s.id === sessionId 
                            ? { ...s, status: 'CLOSED', closedAt: new Date().toISOString() }
                            : s
                    );
                }
                closingSessionId = null;
                await update();
            };
        };
    }

    let sortedSessions = $derived([...sessions].sort((a, b) => {
        if (a.status === "OPEN" && b.status !== "OPEN") return -1;
        if (a.status !== "OPEN" && b.status === "OPEN") return 1;
        return new Date(b.startedAt) - new Date(a.startedAt);
    }));

    let openCount = $derived(sessions.filter(s => s.status === "OPEN").length);
    let closedCount = $derived(sessions.filter(s => s.status === "CLOSED").length);
</script>

<svelte:head>
    <title>{$t('ssess.headTitle')}</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">{$t('ssess.title')}</h1>
            <p class="subtitle">{$t('ssess.subtitle')}</p>
        </div>
        <form method="POST" action="?/start" use:enhance={handleStartEnhance} class="start-form">
            <label class="roast-toggle" title={$t('ssess.roastHint')}>
                <input type="checkbox" name="roast" />
                <span>🔥 {$t('ssess.roastMode')}</span>
            </label>
            <button type="submit" class="btn btn-primary" disabled={practiceLeft === 0 || starting}>
                {#if starting}<span class="btn-spinner" aria-hidden="true"></span> {$t('ssess.starting')}{:else}{$t('ssess.newSession')}{/if}
            </button>
        </form>
    </header>

    {#if starting}
        <div class="start-overlay" role="status" aria-live="polite">
            <div class="start-overlay-card">
                <div class="big-spinner" aria-hidden="true"></div>
                <p class="start-overlay-title">{$t('ssess.starting')}</p>
                <p class="start-overlay-sub">{$t('ssess.startingHint')}</p>
            </div>
        </div>
    {/if}

    {#if practiceQuota}
        <p class="quota-note" class:quota-empty={practiceLeft === 0}>
            {#if practiceLeft === 0}
                🔒 {$t('ssess.quotaEmpty')}
            {:else}
                🎯 {$t('ssess.quotaLeft').replace('%N%', practiceLeft).replace('%T%', practiceQuota.limit)}
            {/if}
        </p>
    {/if}

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if form?.success}
        <div class="alert alert-success">{$t('ssess.closedAlert')}</div>
    {/if}

    <div class="stats-bar">
        <div class="stat-card">
            <span class="stat-value">{sessions.length}</span>
            <span class="stat-label">{$t('ssess.total')}</span>
        </div>
        <div class="stat-card stat-success">
            <span class="stat-value">{openCount}</span>
            <span class="stat-label">{$t('ssess.open')}</span>
        </div>
        <div class="stat-card stat-muted">
            <span class="stat-value">{closedCount}</span>
            <span class="stat-label">{$t('ssess.closed')}</span>
        </div>
    </div>

    {#if sessions.length === 0}
        <div class="empty-state">
            <div class="empty-icon">🎯</div>
            <h2>{$t('ssess.emptyTitle')}</h2>
            <p>{$t('ssess.emptyText')}</p>
            <form method="POST" action="?/start" use:enhance>
                <button type="submit" class="btn btn-primary">{$t('ssess.firstSession')}</button>
            </form>
        </div>
    {:else}
        <div class="sessions-grid">
            {#each sortedSessions as session}
                <article class="session-card" class:session-open={session.status === 'OPEN'}>
                    <div class="session-header">
                        <span class="badge {getStatusColor(session.status)}">
                            {session.status === 'OPEN' ? $t('ssess.open') : $t('ssess.closed')}
                        </span>
                        <span class="session-date">{formatDateTime(session.startedAt)}</span>
                    </div>

                    <div class="session-body">
                        <div class="session-info">
                            <span class="info-label">{$t('ssess.messages')}</span>
                            <span class="info-value">{session.messages?.length ?? 0}</span>
                        </div>
                        {#if session.roast}
                            <div class="session-info">
                                <span class="info-label">{$t('ssess.mode')}</span>
                                <span class="info-value">🔥 {$t('ssess.roastMode')}</span>
                            </div>
                        {/if}
                        {#if session.status === 'CLOSED' && session.score != null}
                            <div class="score-box">
                                <div class="score-head">
                                    <span class="score-label">{$t('ssess.chance')}</span>
                                    <span class="score-pct">{session.score}%</span>
                                </div>
                                <div class="score-bar"><div class="score-fill" style="width:{session.score}%"></div></div>
                                {#if session.scoreReason}<p class="score-reason">{session.scoreReason}</p>{/if}
                            </div>
                        {/if}
                        {#if session.assignmentId}
                            <div class="session-info">
                                <span class="info-label">{$t('ssess.toAssignment')}</span>
                                <span class="info-value">{$t('ssess.yes')}</span>
                            </div>
                        {/if}
                        {#if session.closedAt}
                            <div class="session-info">
                                <span class="info-label">{$t('ssess.endedAt')}</span>
                                <span class="info-value">{formatDateTime(session.closedAt)}</span>
                            </div>
                        {/if}
                    </div>

                    <div class="session-actions">
                        {#if deleteConfirmId === session.id}
                            <span class="delete-q">{$t('ssess.confirmDelete')}</span>
                            <form method="POST" action="?/delete" use:enhance={handleDeleteEnhance(session.id)} class="inline-form">
                                <input type="hidden" name="sessionId" value={session.id} />
                                <button type="submit" class="btn btn-danger">{$t('ssess.yesDelete')}</button>
                            </form>
                            <button type="button" class="btn btn-secondary" onclick={() => deleteConfirmId = null}>{$t('ssess.cancel')}</button>
                        {:else}
                            {#if session.status === "OPEN"}
                                <a href="/student/sessions/{session.id}" class="btn btn-primary">{$t('ssess.continue')}</a>
                                <form method="POST" action="?/close" use:enhance={handleCloseEnhance(session.id)} class="inline-form">
                                    <input type="hidden" name="sessionId" value={session.id} />
                                    <button type="submit" class="btn btn-secondary" disabled={closingSessionId === session.id}>
                                        {#if closingSessionId === session.id}
                                            {$t('ssess.ending')}
                                        {:else}
                                            {$t('ssess.end')}
                                        {/if}
                                    </button>
                                </form>
                            {:else}
                                <a href="/student/sessions/{session.id}" class="btn btn-secondary">{$t('ssess.view')}</a>
                            {/if}
                            <button type="button" class="btn-delete" title={$t('ssess.deleteTitle')} onclick={() => deleteConfirmId = session.id}>🗑️</button>
                        {/if}
                    </div>
                </article>
            {/each}
        </div>
    {/if}
</div>

<style>
    /* ===== PAGE SPECIFIC STYLES ===== */
    .sessions-grid {
        display: grid;
        gap: var(--space-lg);
    }

    @media (min-width: 640px) {
        .sessions-grid {
            grid-template-columns: repeat(2, 1fr);
        }
    }

    .session-card {
        background: var(--color-bg-card);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-xl);
        padding: var(--space-lg);
        display: flex;
        flex-direction: column;
        gap: var(--space-md);
    }

    .session-open {
        border-color: var(--color-success-border);
        box-shadow: 0 0 0 1px var(--color-success-border);
    }

    .session-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .session-date {
        font-size: var(--font-size-xs);
        color: var(--color-text-muted);
    }

    .session-body {
        display: flex;
        flex-direction: column;
        gap: var(--space-xs);
    }

    .session-info {
        display: flex;
        justify-content: space-between;
        font-size: var(--font-size-sm);
    }

    .info-label {
        color: var(--color-text-muted);
    }

    .info-value {
        font-weight: 500;
        color: var(--color-primary);
    }

    .session-actions {
        display: flex;
        gap: var(--space-sm);
        margin-top: auto;
        padding-top: var(--space-sm);
        border-top: 1px solid var(--color-border-light);
    }

    .inline-form {
        display: inline;
    }

    .btn-danger {
        background: var(--color-error-bg);
        color: var(--color-error);
        border: 1px solid var(--color-error-border);
    }

    .btn-delete {
        margin-left: auto;
        background: transparent;
        border: none;
        cursor: pointer;
        font-size: 1rem;
        opacity: 0.55;
        border-radius: var(--radius-md);
        padding: 0.3rem 0.45rem;
        transition: all var(--transition-base);
    }

    .btn-delete:hover {
        opacity: 1;
        background: var(--color-error-bg);
    }

    .delete-q {
        font-size: var(--font-size-sm);
        color: var(--color-error);
        align-self: center;
    }

    /* Roast-Toggle im Start-Formular */
    .start-form {
        display: flex;
        align-items: center;
        gap: var(--space-md);
    }
    .roast-toggle {
        display: inline-flex;
        align-items: center;
        gap: 0.4rem;
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
        cursor: pointer;
        white-space: nowrap;
    }
    .roast-toggle input { accent-color: #e8590c; cursor: pointer; }
    @media (max-width: 600px) {
        .start-form { flex-direction: column; align-items: stretch; }
        .roast-toggle { justify-content: flex-start; }
    }

    .quota-note { margin: 0 0 1rem; font-size: var(--font-size-sm); color: var(--color-text-muted); }
    .quota-note.quota-empty { color: #b45309; font-weight: 600; }

    /* Einstellungs-Chance (Score) */
    .score-box {
        margin-top: var(--space-xs);
        padding: var(--space-sm);
        background: var(--color-bg-subtle, #faf8fc);
        border: 1px solid var(--color-border-light, #eee);
        border-radius: var(--radius-md);
    }
    .score-head { display: flex; justify-content: space-between; align-items: baseline; }
    .score-label { font-size: var(--font-size-xs); color: var(--color-text-muted); text-transform: uppercase; letter-spacing: 0.03em; }
    .score-pct { font-size: 1.25rem; font-weight: 800; color: var(--color-primary, #2F124D); }
    .score-bar { height: 8px; background: #eee; border-radius: 999px; overflow: hidden; margin: 0.4rem 0; }
    .score-fill { height: 100%; background: linear-gradient(90deg, #e8590c, #ffd43b); border-radius: 999px; }
    .score-reason { margin: 0.2rem 0 0; font-size: var(--font-size-sm); color: var(--color-text-secondary); line-height: 1.4; }

    /* Deaktivierter Button: dezenter „gesperrt"-Look statt blass-blau */
    .btn-primary:disabled,
    .btn:disabled {
        background: #ece7f3 !important;
        color: #9a8baf !important;
        box-shadow: none !important;
        cursor: not-allowed;
        opacity: 1 !important;
        border: 1px solid #e0d6eb !important;
    }

    /* ===== KI-Session Start: Lade-Animation ===== */
    .btn-spinner {
        display: inline-block;
        width: 0.9em; height: 0.9em;
        border: 2px solid rgba(255, 255, 255, 0.5);
        border-top-color: #fff;
        border-radius: 50%;
        vertical-align: -0.1em;
        animation: praesto-spin 0.7s linear infinite;
    }
    .start-overlay {
        position: fixed;
        inset: 0;
        z-index: 9999;
        background: rgba(47, 18, 77, 0.55);
        backdrop-filter: blur(4px);
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .start-overlay-card {
        background: #fff;
        border-radius: 1.25rem;
        padding: 2.25rem 2.5rem;
        text-align: center;
        box-shadow: 0 24px 60px rgba(0, 0, 0, 0.3);
        max-width: 90vw;
    }
    .big-spinner {
        width: 3rem; height: 3rem;
        margin: 0 auto 1rem;
        border: 4px solid #ece3f5;
        border-top-color: var(--color-primary, #2F124D);
        border-radius: 50%;
        animation: praesto-spin 0.8s linear infinite;
    }
    .start-overlay-title { margin: 0 0 0.35rem; font-weight: 700; color: var(--color-primary, #2F124D); font-size: 1.1rem; }
    .start-overlay-sub { margin: 0; color: var(--color-text-muted, #5E4C6F); font-size: 0.9rem; }
    @keyframes praesto-spin { to { transform: rotate(360deg); } }
</style>
