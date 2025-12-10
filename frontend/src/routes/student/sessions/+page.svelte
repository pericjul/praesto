<script>
    import { enhance } from "$app/forms";

    export let data;
    export let form;

    const sessions = data.sessions ?? [];

    function formatDateTime(date) {
        if (!date) return "-";
        return new Date(date).toLocaleString("de-CH", {
            dateStyle: "medium",
            timeStyle: "short"
        });
    }

    function getStatusColor(status) {
        return status === "OPEN" ? "badge-open" : "badge-closed";
    }

    function getStatusText(status) {
        return status === "OPEN" ? "Offen" : "Abgeschlossen";
    }

    // Sortiere: Offene zuerst, dann nach Datum
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
            <h1 class="title">KI-Training Sessions</h1>
            <p class="subtitle">
                Hier siehst du alle deine Trainings-Sessions. Setze offene Sessions fort oder starte eine neue.
            </p>
        </div>
        <form method="POST" action="?/start" use:enhance>
            <button type="submit" class="btn btn-primary">
                ➕ Neue Session starten
            </button>
        </form>
    </header>

    {#if form?.error}
        <div class="alert alert-danger">
            {form.error}
        </div>
    {/if}

    {#if form?.success}
        <div class="alert alert-success">
            Session wurde geschlossen.
        </div>
    {/if}

    <!-- Stats -->
    <div class="stats-row">
        <div class="stat-card">
            <span class="stat-value">{sessions.length}</span>
            <span class="stat-label">Total</span>
        </div>
        <div class="stat-card stat-open">
            <span class="stat-value">{openCount}</span>
            <span class="stat-label">Offen</span>
        </div>
        <div class="stat-card stat-closed">
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
                <button type="submit" class="btn btn-primary">
                    Erste Session starten
                </button>
            </form>
        </div>
    {:else}
        <div class="sessions-grid">
            {#each sortedSessions as session}
                <article class="session-card {session.status === 'OPEN' ? 'session-open' : ''}">
                    <div class="session-header">
                        <span class="session-badge {getStatusColor(session.status)}">
                            {getStatusText(session.status)}
                        </span>
                        <span class="session-date">
                            {formatDateTime(session.startedAt)}
                        </span>
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
                            <a href="/student/sessions/{session.id}" class="btn btn-primary">
                                ▶️ Fortsetzen
                            </a>
                            <form method="POST" action="?/close" use:enhance class="inline-form">
                                <input type="hidden" name="sessionId" value={session.id} />
                                <button type="submit" class="btn btn-secondary">
                                    ⏹️ Beenden
                                </button>
                            </form>
                        {:else}
                            <a href="/student/sessions/{session.id}" class="btn btn-secondary">
                                👁️ Ansehen
                            </a>
                        {/if}
                    </div>
                </article>
            {/each}
        </div>
    {/if}
</div>

<style>
    .page-wrapper {
        max-width: 1000px;
        margin: 0 auto;
        padding: 1.5rem 1rem 3rem;
    }

    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 1rem;
        margin-bottom: 1.5rem;
        flex-wrap: wrap;
    }

    .title {
        font-size: 1.7rem;
        font-weight: 700;
        margin: 0;
        color: #2d2141;
    }

    .subtitle {
        margin: 0.3rem 0 0;
        color: #6b647a;
        font-size: 0.95rem;
    }

    .stats-row {
        display: flex;
        gap: 1rem;
        margin-bottom: 1.5rem;
    }

    .stat-card {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        padding: 0.75rem 1.25rem;
        text-align: center;
        min-width: 100px;
    }

    .stat-value {
        display: block;
        font-size: 1.5rem;
        font-weight: 700;
        color: #3b134f;
    }

    .stat-label {
        font-size: 0.8rem;
        color: #7c6b80;
    }

    .stat-open {
        border-color: #22c55e;
        background: #f0fdf4;
    }

    .stat-open .stat-value {
        color: #16a34a;
    }

    .stat-closed {
        border-color: #d1d5db;
        background: #f9fafb;
    }

    .stat-closed .stat-value {
        color: #6b7280;
    }

    .empty-state {
        text-align: center;
        padding: 3rem 1rem;
        background: #faf6ff;
        border: 2px dashed #e0d4ff;
        border-radius: 1rem;
    }

    .empty-icon {
        font-size: 3rem;
        margin-bottom: 0.5rem;
    }

    .empty-state h2 {
        margin: 0 0 0.5rem;
        color: #3b134f;
    }

    .empty-state p {
        margin: 0 0 1.5rem;
        color: #6b647a;
    }

    .sessions-grid {
        display: grid;
        gap: 1rem;
    }

    @media (min-width: 640px) {
        .sessions-grid {
            grid-template-columns: repeat(2, 1fr);
        }
    }

    .session-card {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 1rem;
        padding: 1rem;
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
    }

    .session-open {
        border-color: #22c55e;
        box-shadow: 0 0 0 1px #22c55e20;
    }

    .session-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .session-badge {
        padding: 0.2rem 0.6rem;
        border-radius: 999px;
        font-size: 0.75rem;
        font-weight: 600;
    }

    .badge-open {
        background: #dcfce7;
        color: #166534;
    }

    .badge-closed {
        background: #f3f4f6;
        color: #6b7280;
    }

    .session-date {
        font-size: 0.8rem;
        color: #7c6b80;
    }

    .session-body {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
    }

    .session-info {
        display: flex;
        justify-content: space-between;
        font-size: 0.85rem;
    }

    .info-label {
        color: #7c6b80;
    }

    .info-value {
        font-weight: 500;
        color: #3b134f;
    }

    .session-actions {
        display: flex;
        gap: 0.5rem;
        margin-top: auto;
        padding-top: 0.5rem;
        border-top: 1px solid #f0e8f8;
    }

    .inline-form {
        display: inline;
    }

    .btn {
        padding: 0.4rem 0.9rem;
        border-radius: 0.5rem;
        font-size: 0.85rem;
        font-weight: 500;
        text-decoration: none;
        border: none;
        cursor: pointer;
        transition: all 0.15s ease;
    }

    .btn-primary {
        background: #7c3aed;
        color: #fff;
    }

    .btn-primary:hover {
        background: #6d28d9;
    }

    .btn-secondary {
        background: #f3f4f6;
        color: #374151;
        border: 1px solid #e5e7eb;
    }

    .btn-secondary:hover {
        background: #e5e7eb;
    }

    .alert {
        padding: 0.75rem 1rem;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
    }

    .alert-danger {
        background: #fef2f2;
        color: #dc2626;
        border: 1px solid #fecaca;
    }

    .alert-success {
        background: #f0fdf4;
        color: #16a34a;
        border: 1px solid #bbf7d0;
    }
</style>
