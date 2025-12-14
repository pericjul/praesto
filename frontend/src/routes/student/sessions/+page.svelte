<script>
    import { enhance } from "$app/forms";

    export let data;
    export let form;

    // Reaktive sessions Liste (kann lokal geändert werden)
    let sessions = data.sessions ?? [];
    
    // Loading States
    let closingSessionId = null;

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

    function getStatusText(status) {
        return status === "OPEN" ? "Offen" : "Abgeschlossen";
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

    $: sortedSessions = [...sessions].sort((a, b) => {
        if (a.status === "OPEN" && b.status !== "OPEN") return -1;
        if (a.status !== "OPEN" && b.status === "OPEN") return 1;
        return new Date(b.startedAt) - new Date(a.startedAt);
    });

    $: openCount = sessions.filter(s => s.status === "OPEN").length;
    $: closedCount = sessions.filter(s => s.status === "CLOSED").length;
</script>

<svelte:head>
    <title>Meine Sessions – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">🤖 KI-Training Sessions</h1>
            <p class="subtitle">Hier siehst du alle deine Trainings-Sessions. Setze offene Sessions fort oder starte eine neue.</p>
        </div>
        <form method="POST" action="?/start" use:enhance>
            <button type="submit" class="btn btn-primary">➕ Neue Session starten</button>
        </form>
    </header>

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if form?.success}
        <div class="alert alert-success">Session wurde geschlossen.</div>
    {/if}

    <div class="stats-bar">
        <div class="stat-card">
            <span class="stat-value">{sessions.length}</span>
            <span class="stat-label">Total</span>
        </div>
        <div class="stat-card stat-success">
            <span class="stat-value">{openCount}</span>
            <span class="stat-label">Offen</span>
        </div>
        <div class="stat-card stat-muted">
            <span class="stat-value">{closedCount}</span>
            <span class="stat-label">Abgeschlossen</span>
        </div>
    </div>

    {#if sessions.length === 0}
        <div class="empty-state">
            <div class="empty-icon">🎯</div>
            <h2>Noch keine Sessions</h2>
            <p>Starte dein erstes KI-Training und übe für Bewerbungsgespräche!</p>
            <form method="POST" action="?/start" use:enhance>
                <button type="submit" class="btn btn-primary">Erste Session starten</button>
            </form>
        </div>
    {:else}
        <div class="sessions-grid">
            {#each sortedSessions as session}
                <article class="session-card" class:session-open={session.status === 'OPEN'}>
                    <div class="session-header">
                        <span class="badge {getStatusColor(session.status)}">
                            {getStatusText(session.status)}
                        </span>
                        <span class="session-date">{formatDateTime(session.startedAt)}</span>
                    </div>

                    <div class="session-body">
                        <div class="session-info">
                            <span class="info-label">Nachrichten:</span>
                            <span class="info-value">{session.messages?.length ?? 0}</span>
                        </div>
                        {#if session.assignmentId}
                            <div class="session-info">
                                <span class="info-label">Zu Aufgabe:</span>
                                <span class="info-value">Ja</span>
                            </div>
                        {/if}
                        {#if session.closedAt}
                            <div class="session-info">
                                <span class="info-label">Beendet:</span>
                                <span class="info-value">{formatDateTime(session.closedAt)}</span>
                            </div>
                        {/if}
                    </div>

                    <div class="session-actions">
                        {#if session.status === "OPEN"}
                            <a href="/student/sessions/{session.id}" class="btn btn-primary">▶️ Fortsetzen</a>
                            <form method="POST" action="?/close" use:enhance={handleCloseEnhance(session.id)} class="inline-form">
                                <input type="hidden" name="sessionId" value={session.id} />
                                <button type="submit" class="btn btn-secondary" disabled={closingSessionId === session.id}>
                                    {#if closingSessionId === session.id}
                                        ⏳ Beenden...
                                    {:else}
                                        ⏹️ Beenden
                                    {/if}
                                </button>
                            </form>
                        {:else}
                            <a href="/student/sessions/{session.id}" class="btn btn-secondary">👁️ Ansehen</a>
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
</style>
