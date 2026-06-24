<script>
    import { enhance } from "$app/forms";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    // Reaktive sessions Liste (kann lokal geändert werden)
    let sessions = $state(data.sessions ?? []);

    // Loading States
    let closingSessionId = $state(null);
    let deleteConfirmId = $state(null);

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
        <form method="POST" action="?/start" use:enhance>
            <button type="submit" class="btn btn-primary">{$t('ssess.newSession')}</button>
        </form>
    </header>

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
</style>
