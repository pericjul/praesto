<script>
    let { data } = $props();

    let session = $derived(data?.session ?? null);
    let error = $derived(data?.error ?? null);
    let messages = $derived(session?.messages ?? []);

    function formatTime(date) {
        if (!date) return "";
        return new Date(date).toLocaleTimeString("de-CH", {
            hour: "2-digit",
            minute: "2-digit"
        });
    }

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

    function calculateDuration() {
        if (!session?.startedAt) return "-";
        const start = new Date(session.startedAt);
        const end = session.closedAt ? new Date(session.closedAt) : new Date();
        const diffMin = Math.round((end - start) / 60000);
        return `${diffMin} Minuten`;
    }
</script>

<svelte:head>
    <title>Chat-Verlauf – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <div class="page-content">
        {#if error}
            <div class="alert alert-danger">{error}</div>
            <a href="/teacher/assignments" class="back-link">← Zurück zu Aufgaben</a>
        {:else if session}
            <!-- Header -->
            <header class="page-header">
                <a href="javascript:history.back()" class="back-link">← Zurück</a>
                <div class="header-content">
                    <h1>🤖 Chat-Verlauf</h1>
                    {#if session.assignmentTitle}
                        <p class="assignment-info">Aufgabe: {session.assignmentTitle}</p>
                    {/if}
                </div>
            </header>

            <!-- Meta Info -->
            <div class="meta-bar">
                <div class="meta-item">
                    <span class="meta-label">Schüler</span>
                    <span class="meta-value">{session.studentEmail ?? session.studentId}</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Gestartet</span>
                    <span class="meta-value">{formatDate(session.startedAt)}</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Dauer</span>
                    <span class="meta-value">{calculateDuration()}</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Status</span>
                    <span class="meta-value status-badge" class:open={session.status === "OPEN"}>
                        {session.status === "OPEN" ? "Offen" : "Abgeschlossen"}
                    </span>
                </div>
                {#if session.submittedAsAssignment}
                    <div class="meta-item">
                        <span class="meta-value submitted-badge">✓ Abgegeben</span>
                    </div>
                {/if}
            </div>

            <!-- Chat Messages - NUR DIESER SCROLLT -->
            <div class="chat-container">
                {#if messages.length === 0}
                    <p class="empty">Keine Nachrichten in dieser Session.</p>
                {:else}
                    {#each messages as msg}
                        <div class="message {msg.role === 'USER' ? 'user' : 'bot'}">
                            <div class="message-header">
                                <span class="message-sender">
                                    {msg.role === 'USER' ? '👤 Schüler' : '🤖 KI-Coach'}
                                </span>
                                {#if msg.createdAt}
                                    <span class="message-time">{formatTime(msg.createdAt)}</span>
                                {/if}
                            </div>
                            <div class="message-bubble">
                                {msg.content}
                            </div>
                        </div>
                    {/each}
                {/if}
            </div>

            <!-- Summary -->
            <div class="summary-box">
                <h3>📊 Zusammenfassung</h3>
                <p><strong>Nachrichten:</strong> {messages.length} ({messages.filter(m => m.role === 'USER').length} vom Schüler)
                {#if session.targetDurationMin}
                    · <strong>Ziel:</strong> {session.targetDurationMin} Min
                {/if}
                </p>
            </div>
        {/if}
    </div>
</div>

<style>
    .page-wrapper {
        position: fixed;
        top: 54px; /* Navbar Höhe */
        left: 0;
        right: 0;
        bottom: 0;
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }

    .page-content {
        max-width: 800px;
        width: 100%;
        margin: 0 auto;
        padding: 1.5rem 1rem;
        display: flex;
        flex-direction: column;
        height: 100%;
        overflow: hidden;
    }

    .back-link {
        display: inline-block;
        margin-bottom: 1rem;
        color: #7c6b80;
        text-decoration: none;
        font-size: 0.9rem;
        flex-shrink: 0;
    }

    .back-link:hover {
        color: #3b134f;
    }

    .page-header {
        margin-bottom: 1rem;
        flex-shrink: 0;
    }

    .header-content h1 {
        margin: 0 0 0.25rem;
        font-size: 1.5rem;
        color: #2d2141;
    }

    .assignment-info {
        margin: 0;
        color: #7c6b80;
        font-size: 0.9rem;
    }

    .alert {
        padding: 1rem;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
        flex-shrink: 0;
    }

    .alert-danger {
        background: #fef2f2;
        color: #dc2626;
        border: 1px solid #fecaca;
    }

    /* Meta Bar */
    .meta-bar {
        display: flex;
        flex-wrap: wrap;
        gap: 1.5rem;
        padding: 1rem 1.25rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        margin-bottom: 1rem;
        flex-shrink: 0;
    }

    .meta-item {
        display: flex;
        flex-direction: column;
        gap: 0.2rem;
    }

    .meta-label {
        font-size: 0.75rem;
        color: #7c6b80;
        text-transform: uppercase;
    }

    .meta-value {
        font-weight: 600;
        color: #2d2141;
    }

    .status-badge {
        padding: 0.2rem 0.6rem;
        border-radius: 1rem;
        font-size: 0.85rem;
        background: #e5e7eb;
    }

    .status-badge.open {
        background: #fef3c7;
        color: #92400e;
    }

    .submitted-badge {
        background: #d1fae5;
        color: #065f46;
        padding: 0.2rem 0.6rem;
        border-radius: 1rem;
        font-size: 0.85rem;
    }

    /* Chat Container - NUR dieser scrollt */
    .chat-container {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        padding: 1.5rem;
        flex: 1;
        min-height: 0; /* Wichtig für Flex-Scroll */
        overflow-y: auto;
    }

    .empty {
        text-align: center;
        color: #7c6b80;
        padding: 2rem;
    }

    .message {
        margin-bottom: 1.25rem;
    }

    .message:last-child {
        margin-bottom: 0;
    }

    .message-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 0.4rem;
        font-size: 0.8rem;
    }

    .message-sender {
        font-weight: 600;
        color: #2d2141;
    }

    .message.bot .message-sender {
        color: #7c3aed;
    }

    .message-time {
        color: #9ca3af;
    }

    .message-bubble {
        padding: 0.75rem 1rem;
        border-radius: 0.75rem;
        line-height: 1.5;
        white-space: pre-wrap;
    }

    .message.user .message-bubble {
        background: #f3e8ff;
        color: #2d2141;
        border-bottom-right-radius: 0.25rem;
    }

    .message.bot .message-bubble {
        background: #f9fafb;
        color: #374151;
        border: 1px solid #e5e7eb;
        border-bottom-left-radius: 0.25rem;
    }

    /* Summary Box */
    .summary-box {
        background: #faf5ff;
        border: 1px solid #e9d5ff;
        border-radius: 0.75rem;
        padding: 1rem 1.25rem;
        margin-top: 1rem;
        flex-shrink: 0;
    }

    .summary-box h3 {
        margin: 0 0 0.5rem;
        font-size: 0.95rem;
        color: #7c3aed;
    }

    .summary-box p {
        margin: 0 0 0.25rem;
        color: #4c1d95;
        font-size: 0.85rem;
    }

    .summary-box p:last-child {
        margin-bottom: 0;
    }
</style>