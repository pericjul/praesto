<script>
    let { data, form } = $props();

    // Derived
    let assignments = $derived(data?.assignments ?? []);
    let submissions = $derived(data?.submissions ?? []);
    let myClass = $derived(data?.myClass ?? null);
    let error = $derived(data?.error ?? null);

    const assignmentTypes = {
        AI_INTERVIEW: { label: "🤖 KI-Bewerbungsgespräch", action: "Training starten", color: "#8b5cf6" },
        DOCUMENT_UPLOAD: { label: "📄 Dokument einreichen", action: "Hochladen", color: "#3b82f6" },
        SELF_REFLECTION: { label: "✍️ Selbstreflexion", action: "Schreiben", color: "#10b981" },
        VIDEO_PITCH: { label: "🎥 Video-Bewerbung", action: "Aufnehmen", color: "#f59e0b" },
        RESEARCH: { label: "🔍 Recherche", action: "Bearbeiten", color: "#6366f1" }
    };

    function getTypeInfo(type) {
        return assignmentTypes[type] ?? { label: type, action: "Öffnen", color: "#6b7280" };
    }

    function getSubmissionForAssignment(assignmentId) {
        return submissions.find(s => s.assignmentId === assignmentId);
    }

    function hasSubmitted(assignmentId) {
        return submissions.some(s => s.assignmentId === assignmentId);
    }

    function formatDate(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", year: "numeric"
        });
    }

    function formatDateTime(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", year: "numeric",
            hour: "2-digit", minute: "2-digit"
        });
    }

    function getDeadlineStatus(dueDate) {
        if (!dueDate) return "ok";
        const now = new Date();
        const due = new Date(dueDate);
        const diffDays = (due - now) / (1000 * 60 * 60 * 24);
        if (diffDays < 0) return "overdue";
        if (diffDays <= 3) return "soon";
        return "ok";
    }

    function formatStatus(status) {
        switch (status) {
            case "ASSIGNED": return "Offen";
            case "IN_PROGRESS": return "In Bearbeitung";
            case "COMPLETED": return "Abgeschlossen";
            default: return status;
        }
    }

    function formatSubmissionStatus(status) {
        switch (status) {
            case "SUBMITTED": return "Eingereicht";
            case "IN_REVIEW": return "In Prüfung";
            case "REVIEWED": return "Bewertet";
            case "RETURNED": return "Zurückgegeben";
            default: return status;
        }
    }

    function getStatusColor(status) {
        switch (status) {
            case "ASSIGNED": return "#3b82f6";
            case "IN_PROGRESS": return "#f59e0b";
            case "COMPLETED": return "#10b981";
            default: return "#6b7280";
        }
    }
</script>

<svelte:head>
    <title>Meine Aufgaben – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">📚 Meine Aufgaben</h1>
            {#if myClass}
                <p class="subtitle">
                    Klasse: <strong>{myClass.name}</strong> · 
                    Hier findest du alle Aufgaben deiner Lehrperson.
                </p>
            {:else}
                <p class="subtitle">Übersicht deiner Aufgaben</p>
            {/if}
        </div>
    </header>

    {#if form?.success}
        <div class="alert alert-success">✓ {form.message || "Erfolgreich gespeichert!"}</div>
    {/if}

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if error}
        <div class="alert alert-warning">
            <span class="alert-icon">⚠️</span>
            <div>
                <strong>Hinweis</strong>
                <p>{error}</p>
            </div>
        </div>
    {:else if assignments.length === 0}
        <div class="empty-state">
            <div class="empty-icon">🎉</div>
            <h2>Keine Aufgaben</h2>
            <p>Du hast aktuell keine offenen Aufgaben. Geniesse die freie Zeit!</p>
        </div>
    {:else}
        <div class="stats-bar">
            <div class="stat-item">
                <span class="stat-value">{assignments.length}</span>
                <span class="stat-label">Aufgaben total</span>
            </div>
            <div class="stat-item">
                <span class="stat-value">{assignments.length - submissions.length}</span>
                <span class="stat-label">Offen</span>
            </div>
            <div class="stat-item">
                <span class="stat-value">{submissions.length}</span>
                <span class="stat-label">Abgegeben</span>
            </div>
        </div>

        <div class="assignments-list">
            {#each assignments as assignment}
                {@const typeInfo = getTypeInfo(assignment.type)}
                {@const deadlineStatus = getDeadlineStatus(assignment.dueDate)}
                {@const submission = getSubmissionForAssignment(assignment.id)}
                {@const submitted = !!submission}
                
                <a href="/student/assignments/{assignment.id}" class="assignment-card" class:overdue={deadlineStatus === "overdue" && !submitted} class:submitted={submitted}>
                    <div class="assignment-content">
                        <div class="assignment-header">
                            <span class="type-badge" style="background: {typeInfo.color}15; color: {typeInfo.color}">
                                {typeInfo.label}
                            </span>
                            {#if submitted}
                                <span class="status-badge submitted-badge">✓ {formatSubmissionStatus(submission.status)}</span>
                            {:else}
                                <span class="status-badge" style="background: {getStatusColor(assignment.status)}15; color: {getStatusColor(assignment.status)}">
                                    {formatStatus(assignment.status)}
                                </span>
                            {/if}
                        </div>
                        
                        <h3 class="assignment-title">{assignment.title}</h3>
                        
                        {#if assignment.description}
                            <p class="assignment-desc">{assignment.description}</p>
                        {/if}
                        
                        <div class="assignment-meta">
                            {#if submitted}
                                <span class="meta-item submitted-info">📤 Abgegeben am {formatDateTime(submission.submittedAt)}</span>
                                {#if submission.teacherFeedback}
                                    <span class="meta-item has-feedback">💬 Feedback erhalten</span>
                                {/if}
                            {:else}
                                <span class="meta-item" class:deadline-warning={deadlineStatus === "soon"} class:deadline-overdue={deadlineStatus === "overdue"}>
                                    📅 Deadline: {formatDate(assignment.dueDate)}
                                    {#if deadlineStatus === "overdue"}
                                        <span class="deadline-label">Überfällig!</span>
                                    {:else if deadlineStatus === "soon"}
                                        <span class="deadline-label">Bald fällig</span>
                                    {/if}
                                </span>
                            {/if}
                            {#if assignment.durationMin}
                                <span class="meta-item">⏱️ ca. {assignment.durationMin} Min</span>
                            {/if}
                        </div>

                        {#if submission?.teacherFeedback}
                            <div class="feedback-box">
                                <strong>Feedback:</strong>
                                <p>{submission.teacherFeedback}</p>
                                {#if submission.grade != null}
                                    <span class="grade-badge">Note: {submission.grade}</span>
                                {/if}
                            </div>
                        {/if}
                    </div>
                    
                    <div class="assignment-action">
                        {#if submitted}
                            <span class="view-hint">Details ansehen →</span>
                        {:else}
                            <span class="btn btn-primary">{typeInfo.action}</span>
                        {/if}
                    </div>
                </a>
            {/each}
        </div>
    {/if}
</div>

<style>
    .page-wrapper {
        max-width: 900px;
        margin: 0 auto;
        padding: 1.5rem 1rem 3rem;
    }

    .page-header { margin-bottom: 1.5rem; }

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

    .alert {
        padding: 1rem;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
    }

    .alert-success {
        background: #f0fdf4;
        color: #166534;
        border: 1px solid #bbf7d0;
    }

    .alert-danger {
        background: #fef2f2;
        color: #dc2626;
        border: 1px solid #fecaca;
    }

    .alert-warning {
        background: #fffbeb;
        color: #92400e;
        border: 1px solid #fde68a;
        display: flex;
        gap: 0.75rem;
        align-items: flex-start;
    }

    .empty-state {
        text-align: center;
        padding: 3rem 2rem;
        background: #faf8fc;
        border: 2px dashed #e8e0f0;
        border-radius: 1rem;
    }

    .empty-icon { font-size: 3rem; margin-bottom: 1rem; }

    .empty-state h2 {
        margin: 0 0 0.5rem;
        color: #2d2141;
    }

    .empty-state p {
        margin: 0;
        color: #6b647a;
    }

    .stats-bar {
        display: flex;
        gap: 2rem;
        margin-bottom: 2rem;
        padding: 1rem 1.5rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
    }

    .stat-item { display: flex; flex-direction: column; }

    .stat-value {
        font-size: 1.5rem;
        font-weight: 700;
        color: #3b134f;
    }

    .stat-label {
        font-size: 0.85rem;
        color: #7c6b80;
    }

    .assignments-list {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .assignment-card {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 1.5rem;
        padding: 1.25rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        transition: all 0.2s ease;
        text-decoration: none;
        cursor: pointer;
    }

    .assignment-card:hover {
        border-color: #d0c8e0;
        box-shadow: 0 4px 12px rgba(59, 19, 79, 0.08);
    }

    .assignment-card.overdue {
        border-color: #fecaca;
        background: #fef2f2;
    }

    .assignment-card.submitted {
        border-color: #bbf7d0;
        background: #f0fdf4;
    }

    .feedback-box {
        margin-top: 0.75rem;
        padding: 0.75rem;
        background: #faf5ff;
        border: 1px solid #e9d5ff;
        border-radius: 0.5rem;
    }

    .feedback-box strong { color: #7c3aed; }
    .feedback-box p { margin: 0.25rem 0 0; color: #4c1d95; font-size: 0.9rem; }

    .grade-badge {
        display: inline-block;
        margin-top: 0.5rem;
        padding: 0.25rem 0.75rem;
        background: #7c3aed;
        color: #fff;
        border-radius: 1rem;
        font-size: 0.85rem;
        font-weight: 600;
    }

    .assignment-content { flex: 1; }

    .assignment-header {
        display: flex;
        gap: 0.5rem;
        margin-bottom: 0.5rem;
        flex-wrap: wrap;
    }

    .type-badge, .status-badge {
        display: inline-block;
        padding: 0.25rem 0.6rem;
        border-radius: 0.4rem;
        font-size: 0.8rem;
        font-weight: 500;
    }

    .submitted-badge {
        background: #d1fae5;
        color: #059669;
    }

    .assignment-title {
        margin: 0 0 0.25rem;
        font-size: 1.1rem;
        font-weight: 600;
        color: #2d2141;
    }

    .assignment-desc {
        margin: 0 0 0.5rem;
        font-size: 0.9rem;
        color: #6b647a;
    }

    .assignment-meta {
        display: flex;
        gap: 1rem;
        flex-wrap: wrap;
        font-size: 0.85rem;
        color: #7c6b80;
    }

    .meta-item {
        display: flex;
        align-items: center;
        gap: 0.25rem;
    }

    .meta-item.deadline-warning { color: #d97706; font-weight: 500; }
    .meta-item.deadline-overdue { color: #dc2626; font-weight: 500; }
    .meta-item.has-feedback { color: #7c3aed; font-weight: 500; }

    .deadline-label {
        margin-left: 0.5rem;
        padding: 0.1rem 0.4rem;
        background: currentColor;
        color: #fff;
        border-radius: 0.25rem;
        font-size: 0.7rem;
    }

    .assignment-action { flex-shrink: 0; }

    .view-hint {
        color: #7c3aed;
        font-size: 0.9rem;
        font-weight: 500;
    }

    .btn {
        padding: 0.6rem 1.25rem;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        font-weight: 500;
        border: none;
        cursor: pointer;
    }

    .btn-primary { background: #3b134f; color: #fff; }
    .btn-primary:hover { filter: brightness(1.1); }

    @media (max-width: 600px) {
        .assignment-card {
            flex-direction: column;
            align-items: stretch;
        }
        .assignment-action { margin-top: 1rem; }
        .btn { width: 100%; text-align: center; }
        .stats-bar { flex-wrap: wrap; }
    }
</style>