<script>
    let { data } = $props();

    let sessions = $derived(data?.sessions ?? []);
    let assignments = $derived(data?.assignments ?? []);

    // Filter State
    let filterAssignment = $state("all");
    let filterStatus = $state("all");

    // Gefilterte Sessions
    let filteredSessions = $derived(() => {
        let result = sessions;

        if (filterAssignment !== "all") {
            result = result.filter(s => s.assignmentId === filterAssignment);
        }

        if (filterStatus === "completed") {
            result = result.filter(s => s.submittedAsAssignment);
        } else if (filterStatus === "in_progress") {
            result = result.filter(s => !s.submittedAsAssignment);
        } else if (filterStatus === "feedback_pending") {
            result = result.filter(s => s.submittedAsAssignment && !s.hasFeedback);
        }

        return result;
    });

    function formatDate(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        });
    }

    function formatDuration(seconds) {
        if (!seconds) return "-";
        const min = Math.floor(seconds / 60);
        const sec = seconds % 60;
        return `${min}:${sec.toString().padStart(2, "0")}`;
    }

    function shortenEmail(email) {
        return email?.split("@")[0] ?? "";
    }

    function getStatusInfo(session) {
        if (!session.submittedAsAssignment) {
            return { label: "In Bearbeitung", color: "#f59e0b", bg: "#fef3c7" };
        }
        if (!session.hasFeedback) {
            return { label: "Feedback ausstehend", color: "#3b82f6", bg: "#dbeafe" };
        }
        return { label: "Bewertet", color: "#10b981", bg: "#d1fae5" };
    }
</script>

<svelte:head>
    <title>Chat-Sessions – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">💬 KI-Interview Sessions</h1>
            <p class="subtitle">Übersicht aller Bewerbungsgespräch-Trainings deiner Schüler</p>
        </div>
    </header>

    <!-- Stats -->
    <div class="stats-bar">
        <div class="stat-item">
            <span class="stat-value">{sessions.length}</span>
            <span class="stat-label">Sessions total</span>
        </div>
        <div class="stat-item">
            <span class="stat-value">{sessions.filter(s => s.submittedAsAssignment).length}</span>
            <span class="stat-label">Abgeschlossen</span>
        </div>
        <div class="stat-item highlight">
            <span class="stat-value">{sessions.filter(s => s.submittedAsAssignment && !s.hasFeedback).length}</span>
            <span class="stat-label">Feedback nötig</span>
        </div>
    </div>

    <!-- Filter -->
    <div class="filter-bar">
        <div class="filter-group">
            <label for="filterAssignment">Aufgabe:</label>
            <select id="filterAssignment" bind:value={filterAssignment}>
                <option value="all">Alle Aufgaben</option>
                {#each assignments as assignment}
                    <option value={assignment.id}>{assignment.title}</option>
                {/each}
            </select>
        </div>

        <div class="filter-group">
            <label for="filterStatus">Status:</label>
            <select id="filterStatus" bind:value={filterStatus}>
                <option value="all">Alle</option>
                <option value="completed">Abgeschlossen</option>
                <option value="in_progress">In Bearbeitung</option>
                <option value="feedback_pending">Feedback ausstehend</option>
            </select>
        </div>
    </div>

    <!-- Sessions Liste -->
    {#if sessions.length === 0}
        <div class="empty-state">
            <div class="empty-icon">💬</div>
            <h2>Keine Sessions vorhanden</h2>
            <p>Sobald Schüler KI-Interview Aufgaben bearbeiten, erscheinen ihre Sessions hier.</p>
        </div>
    {:else if filteredSessions().length === 0}
        <div class="empty-state">
            <p>Keine Sessions entsprechen den Filterkriterien.</p>
        </div>
    {:else}
        <div class="sessions-list">
            {#each filteredSessions() as session}
                {@const statusInfo = getStatusInfo(session)}
                <a href="/teacher/sessions/{session.id}" class="session-card">
                    <div class="session-main">
                        <div class="session-student">
                            <span class="student-name">{shortenEmail(session.studentEmail)}</span>
                            <span class="student-email">{session.studentEmail}</span>
                        </div>
                        <div class="session-assignment">{session.assignmentTitle}</div>
                    </div>

                    <div class="session-meta">
                        <div class="meta-item">
                            <span class="meta-label">Gestartet</span>
                            <span class="meta-value">{formatDate(session.startedAt)}</span>
                        </div>
                        <div class="meta-item">
                            <span class="meta-label">Dauer</span>
                            <span class="meta-value">{formatDuration(session.durationSeconds)}</span>
                        </div>
                        <div class="meta-item">
                            <span class="meta-label">Nachrichten</span>
                            <span class="meta-value">{session.messages?.length ?? 0}</span>
                        </div>
                    </div>

                    <div class="session-status">
                        <span class="status-badge" style="background: {statusInfo.bg}; color: {statusInfo.color}">
                            {statusInfo.label}
                        </span>
                        {#if session.grade != null}
                            <span class="grade-badge">Note: {session.grade}</span>
                        {/if}
                    </div>

                    <div class="session-action">
                        <span class="view-link">Chat ansehen →</span>
                    </div>
                </a>
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
        margin-bottom: 1.5rem;
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

    .stats-bar {
        display: flex;
        gap: 2rem;
        margin-bottom: 1.5rem;
        padding: 1rem 1.5rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
    }

    .stat-item {
        display: flex;
        flex-direction: column;
    }

    .stat-item.highlight .stat-value {
        color: #dc2626;
    }

    .stat-value {
        font-size: 1.5rem;
        font-weight: 700;
        color: #3b134f;
    }

    .stat-label {
        font-size: 0.85rem;
        color: #7c6b80;
    }

    .filter-bar {
        display: flex;
        gap: 1.5rem;
        margin-bottom: 1.5rem;
        padding: 1rem;
        background: #faf8fc;
        border-radius: 0.5rem;
    }

    .filter-group {
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }

    .filter-group label {
        font-size: 0.9rem;
        color: #6b647a;
    }

    .filter-group select {
        padding: 0.5rem 0.75rem;
        border: 1px solid #e8e0f0;
        border-radius: 0.4rem;
        font-size: 0.9rem;
        background: #fff;
    }

    .empty-state {
        text-align: center;
        padding: 3rem 2rem;
        background: #faf8fc;
        border: 2px dashed #e8e0f0;
        border-radius: 1rem;
    }

    .empty-icon {
        font-size: 3rem;
        margin-bottom: 1rem;
    }

    .empty-state h2 {
        margin: 0 0 0.5rem;
        color: #2d2141;
    }

    .empty-state p {
        margin: 0;
        color: #6b647a;
    }

    .sessions-list {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .session-card {
        display: grid;
        grid-template-columns: 1fr auto auto auto;
        gap: 1.5rem;
        align-items: center;
        padding: 1.25rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        text-decoration: none;
        transition: all 0.2s ease;
    }

    .session-card:hover {
        border-color: #3b134f;
        box-shadow: 0 4px 12px rgba(59, 19, 79, 0.08);
    }

    .session-main {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
    }

    .session-student {
        display: flex;
        flex-direction: column;
    }

    .student-name {
        font-weight: 600;
        color: #2d2141;
    }

    .student-email {
        font-size: 0.8rem;
        color: #9ca3af;
    }

    .session-assignment {
        font-size: 0.9rem;
        color: #6b647a;
    }

    .session-meta {
        display: flex;
        gap: 1.5rem;
    }

    .meta-item {
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    .meta-label {
        font-size: 0.7rem;
        color: #9ca3af;
        text-transform: uppercase;
    }

    .meta-value {
        font-size: 0.9rem;
        color: #2d2141;
        font-weight: 500;
    }

    .session-status {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 0.5rem;
    }

    .status-badge {
        padding: 0.3rem 0.75rem;
        border-radius: 1rem;
        font-size: 0.8rem;
        font-weight: 500;
    }

    .grade-badge {
        padding: 0.2rem 0.5rem;
        background: #7c3aed;
        color: #fff;
        border-radius: 0.4rem;
        font-size: 0.75rem;
        font-weight: 600;
    }

    .session-action {
        color: #7c3aed;
    }

    .view-link {
        font-size: 0.9rem;
        font-weight: 500;
    }

    @media (max-width: 800px) {
        .session-card {
            grid-template-columns: 1fr;
            gap: 1rem;
        }

        .session-meta {
            justify-content: flex-start;
        }

        .session-status {
            flex-direction: row;
            justify-content: flex-start;
        }

        .filter-bar {
            flex-direction: column;
            gap: 0.75rem;
        }
    }
</style>