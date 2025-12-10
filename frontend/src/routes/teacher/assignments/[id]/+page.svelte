<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";

    let { data, form } = $props();

    // State
    let showModal = $state(false);
    let selectedSub = $state(null);

    // Derived
    let assignment = $derived(data?.assignment ?? null);
    let submissions = $derived(data?.submissions ?? []);
    let schoolClass = $derived(data?.schoolClass ?? null);
    let error = $derived(data?.error ?? null);
    let allStudents = $derived(schoolClass?.studentEmails ?? []);
    let submittedEmails = $derived(submissions.map(s => s.studentEmail));
    let pendingStudents = $derived(allStudents.filter(email => !submittedEmails.includes(email)));

    const assignmentTypes = {
        AI_INTERVIEW: { label: "🤖 KI-Bewerbungsgespräch", color: "#8b5cf6" },
        DOCUMENT_UPLOAD: { label: "📄 Dokument einreichen", color: "#3b82f6" },
        SELF_REFLECTION: { label: "✍️ Selbstreflexion", color: "#10b981" },
        VIDEO_PITCH: { label: "🎥 Video-Bewerbung", color: "#f59e0b" },
        RESEARCH: { label: "🔍 Recherche", color: "#6366f1" }
    };

    function getTypeInfo(type) {
        return assignmentTypes[type] ?? { label: type, color: "#6b7280" };
    }

    function formatDate(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", year: "numeric",
            hour: "2-digit", minute: "2-digit"
        });
    }

    function formatStatus(status) {
        const map = {
            "SUBMITTED": "Eingereicht",
            "IN_REVIEW": "In Prüfung", 
            "REVIEWED": "Bewertet",
            "RETURNED": "Zurückgegeben"
        };
        return map[status] ?? status;
    }

    function getStatusColor(status) {
        if (status === "SUBMITTED") return "#3b82f6";
        if (status === "REVIEWED") return "#10b981";
        return "#6b7280";
    }

    function shortenEmail(email) {
        return email.split("@")[0];
    }

    function openModal(sub) {
        selectedSub = sub;
        showModal = true;
    }

    function closeModal() {
        showModal = false;
        selectedSub = null;
    }
</script>

<svelte:head>
    <title>{assignment?.title ?? "Aufgabe"} – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    {#if error}
        <div class="alert alert-danger">{error}</div>
    {:else if assignment}
        <header class="page-header">
            <a href="/teacher/assignments" class="back-link">← Zurück zur Übersicht</a>
            <div class="header-content">
                <span class="type-badge" style="background: {getTypeInfo(assignment.type).color}15; color: {getTypeInfo(assignment.type).color}">
                    {getTypeInfo(assignment.type).label}
                </span>
                <h1>{assignment.title}</h1>
                {#if assignment.description}
                    <p class="description">{assignment.description}</p>
                {/if}
                <div class="meta">
                    <span>📅 Deadline: {formatDate(assignment.dueDate)}</span>
                    <span>👥 Klasse: {schoolClass?.name ?? "?"}</span>
                    {#if assignment.durationMin}
                        <span>⏱️ {assignment.durationMin} Min</span>
                    {/if}
                </div>
            </div>
        </header>

        {#if form?.success}
            <div class="alert alert-success">✓ {form.message}</div>
        {/if}
        {#if form?.error}
            <div class="alert alert-danger">{form.error}</div>
        {/if}

        <div class="stats-bar">
            <div class="stat-item">
                <span class="stat-value">{allStudents.length}</span>
                <span class="stat-label">Schüler total</span>
            </div>
            <div class="stat-item good">
                <span class="stat-value">{submissions.length}</span>
                <span class="stat-label">Abgegeben</span>
            </div>
            <div class="stat-item warning">
                <span class="stat-value">{pendingStudents.length}</span>
                <span class="stat-label">Ausstehend</span>
            </div>
            <div class="stat-item">
                <span class="stat-value">{submissions.filter(s => s.status === "REVIEWED").length}</span>
                <span class="stat-label">Bewertet</span>
            </div>
        </div>

        <section class="section">
            <h2>📥 Eingegangene Abgaben ({submissions.length})</h2>
            
            {#if submissions.length === 0}
                <div class="empty-state">
                    <p>Noch keine Abgaben eingegangen.</p>
                </div>
            {:else}
                <div class="submissions-list">
                    {#each submissions as sub}
                        <div class="submission-card" class:reviewed={sub.status === "REVIEWED"}>
                            <div class="submission-header">
                                <span class="student-email">{sub.studentEmail}</span>
                                <span class="status-badge" style="background: {getStatusColor(sub.status)}15; color: {getStatusColor(sub.status)}">
                                    {formatStatus(sub.status)}
                                </span>
                            </div>
                            
                            <div class="submission-meta">
                                📤 Abgegeben: {formatDate(sub.submittedAt)}
                            </div>

                            {#if sub.type === "AI_INTERVIEW" && sub.chatSessionId}
                                <div class="submission-chat">
                                    <strong>🤖 KI-Interview Session</strong>
                                    <a href="/teacher/sessions/{sub.chatSessionId}" class="view-chat-link">
                                        Chat-Verlauf ansehen →
                                    </a>
                                </div>
                            {/if}

                            {#if sub.textContent}
                                <div class="submission-content">
                                    <strong>Inhalt:</strong>
                                    <p>{sub.textContent.substring(0, 300)}{sub.textContent.length > 300 ? "..." : ""}</p>
                                </div>
                            {/if}

                            {#if sub.teacherFeedback}
                                <div class="feedback-display">
                                    <strong>Dein Feedback:</strong>
                                    <p>{sub.teacherFeedback}</p>
                                    {#if sub.grade != null}
                                        <span class="grade">Note: {sub.grade}</span>
                                    {/if}
                                </div>
                            {/if}

                            <div class="submission-actions">
                                <button type="button" class="btn btn-primary" onclick={() => openModal(sub)}>
                                    {sub.teacherFeedback ? "✏️ Feedback bearbeiten" : "💬 Feedback geben"}
                                </button>
                            </div>
                        </div>
                    {/each}
                </div>
            {/if}
        </section>

        {#if pendingStudents.length > 0}
            <section class="section">
                <h2>⏳ Ausstehende Abgaben ({pendingStudents.length})</h2>
                <div class="pending-list">
                    {#each pendingStudents as email}
                        <span class="pending-student">{shortenEmail(email)}</span>
                    {/each}
                </div>
            </section>
        {/if}
    {/if}
</div>

<!-- MODAL MIT INLINE STYLES (wie Debug-Version) -->
{#if showModal && selectedSub}
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <div 
        style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(45, 33, 65, 0.5); backdrop-filter: blur(4px); z-index: 9998; cursor: pointer;"
        onclick={closeModal}
    ></div>
    <div style="position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); background: #fff; border-radius: 1rem; width: 90%; max-width: 550px; max-height: 90vh; overflow-y: auto; z-index: 9999; box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);">
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.5rem; background: linear-gradient(135deg, #3b134f, #5a2d6a); color: #fff; border-radius: 1rem 1rem 0 0;">
            <h2 style="margin: 0; font-size: 1.1rem; font-weight: 600;">💬 Feedback für {selectedSub.studentEmail}</h2>
            <button 
                type="button" 
                style="background: rgba(255, 255, 255, 0.2); border: none; color: #fff; width: 32px; height: 32px; border-radius: 0.5rem; cursor: pointer; font-size: 1rem; display: flex; align-items: center; justify-content: center;"
                onclick={closeModal}
            >✕</button>
        </div>
        <form 
            style="padding: 1.5rem;"
            method="POST" 
            action="?/feedback"
            use:enhance={() => {
                return async ({ result }) => {
                    if (result.type === 'success') {
                        closeModal();
                        await invalidateAll();
                    }
                };
            }}
        >
            <input type="hidden" name="submissionId" value={selectedSub.id} />

            {#if selectedSub.textContent}
                <div style="margin-bottom: 1rem; padding: 0.75rem; background: #f9fafb; border-radius: 0.5rem; font-size: 0.9rem; max-height: 150px; overflow-y: auto;">
                    <strong>Abgabe:</strong>
                    <p style="margin: 0.25rem 0 0; color: #4b5563;">{selectedSub.textContent}</p>
                </div>
            {/if}

            <div style="margin-bottom: 1rem;">
                <label for="feedback" style="display: block; margin-bottom: 0.4rem; font-weight: 600; color: #3b134f; font-size: 0.9rem;">Feedback</label>
                <textarea 
                    id="feedback" 
                    name="feedback" 
                    rows="4"
                    placeholder="Schreibe dein Feedback..."
                    style="width: 100%; padding: 0.75rem; border: 1px solid #e8e0f0; border-radius: 0.5rem; font-family: inherit; font-size: 1rem; box-sizing: border-box;"
                >{selectedSub.teacherFeedback ?? ""}</textarea>
            </div>

            <div style="margin-bottom: 1rem;">
                <label for="grade" style="display: block; margin-bottom: 0.4rem; font-weight: 600; color: #3b134f; font-size: 0.9rem;">Note (optional)</label>
                <input 
                    type="number" 
                    id="grade" 
                    name="grade" 
                    min="1" 
                    max="6" 
                    step="0.5"
                    value={selectedSub.grade ?? ""}
                    placeholder="z.B. 5"
                    style="width: 100%; padding: 0.75rem; border: 1px solid #e8e0f0; border-radius: 0.5rem; font-family: inherit; font-size: 1rem; box-sizing: border-box;"
                />
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid #e8e0f0;">
                <button 
                    type="button" 
                    style="padding: 0.65rem 1.25rem; border-radius: 0.5rem; font-size: 0.9rem; font-weight: 500; border: none; cursor: pointer; background: #f3f4f6; color: #374151;"
                    onclick={closeModal}
                >
                    Abbrechen
                </button>
                <button 
                    type="submit" 
                    style="padding: 0.65rem 1.25rem; border-radius: 0.5rem; font-size: 0.9rem; font-weight: 500; border: none; cursor: pointer; background: #3b134f; color: #fff;"
                >
                    💾 Speichern
                </button>
            </div>
        </form>
    </div>
{/if}

<style>
    .page-wrapper {
        max-width: 1000px;
        margin: 0 auto;
        padding: 1.5rem 1rem 3rem;
    }

    .back-link {
        display: inline-block;
        margin-bottom: 1rem;
        color: #7c6b80;
        text-decoration: none;
        font-size: 0.9rem;
    }

    .page-header {
        margin-bottom: 1.5rem;
    }

    .header-content h1 {
        margin: 0.5rem 0 0.25rem;
        font-size: 1.6rem;
        color: #2d2141;
    }

    .description {
        margin: 0 0 0.75rem;
        color: #6b647a;
    }

    .meta {
        display: flex;
        gap: 1.5rem;
        flex-wrap: wrap;
        font-size: 0.9rem;
        color: #7c6b80;
    }

    .type-badge {
        display: inline-block;
        padding: 0.25rem 0.75rem;
        border-radius: 0.4rem;
        font-size: 0.85rem;
        font-weight: 500;
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

    .stats-bar {
        display: flex;
        gap: 1.5rem;
        margin-bottom: 2rem;
        padding: 1rem 1.25rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
    }

    .stat-item {
        display: flex;
        flex-direction: column;
    }

    .stat-value {
        font-size: 1.5rem;
        font-weight: 700;
        color: #3b134f;
    }

    .stat-item.good .stat-value { color: #059669; }
    .stat-item.warning .stat-value { color: #d97706; }

    .stat-label {
        font-size: 0.8rem;
        color: #7c6b80;
    }

    .section {
        margin-bottom: 2rem;
    }

    .section h2 {
        font-size: 1.1rem;
        margin: 0 0 1rem;
        color: #2d2141;
    }

    .empty-state {
        padding: 2rem;
        text-align: center;
        background: #faf8fc;
        border: 1px dashed #e8e0f0;
        border-radius: 0.75rem;
        color: #7c6b80;
    }

    .submissions-list {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .submission-card {
        padding: 1.25rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
    }

    .submission-card.reviewed {
        border-color: #bbf7d0;
        background: #f0fdf4;
    }

    .submission-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 0.5rem;
    }

    .student-email {
        font-weight: 600;
        color: #2d2141;
    }

    .status-badge {
        padding: 0.2rem 0.6rem;
        border-radius: 0.4rem;
        font-size: 0.8rem;
        font-weight: 500;
    }

    .submission-meta {
        font-size: 0.85rem;
        color: #7c6b80;
        margin-bottom: 0.75rem;
    }

    .submission-content,
    .submission-chat {
        margin-bottom: 0.75rem;
        font-size: 0.9rem;
    }

    .submission-chat {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.75rem;
        background: #f3e8ff;
        border-radius: 0.5rem;
    }

    .view-chat-link {
        color: #7c3aed;
        font-weight: 500;
        text-decoration: none;
    }

    .submission-content p {
        margin: 0.25rem 0 0;
        color: #4b5563;
        background: #f9fafb;
        padding: 0.75rem;
        border-radius: 0.5rem;
    }

    .feedback-display {
        margin-top: 0.75rem;
        padding: 0.75rem;
        background: #faf5ff;
        border: 1px solid #e9d5ff;
        border-radius: 0.5rem;
    }

    .feedback-display strong { color: #7c3aed; }
    .feedback-display p { margin: 0.25rem 0 0; color: #4c1d95; }

    .grade {
        display: inline-block;
        margin-top: 0.5rem;
        padding: 0.2rem 0.6rem;
        background: #7c3aed;
        color: #fff;
        border-radius: 1rem;
        font-size: 0.85rem;
        font-weight: 600;
    }

    .submission-actions {
        margin-top: 1rem;
        padding-top: 0.75rem;
        border-top: 1px solid #e8e0f0;
    }

    .pending-list {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
    }

    .pending-student {
        padding: 0.4rem 0.75rem;
        background: #fef3c7;
        color: #92400e;
        border-radius: 1rem;
        font-size: 0.85rem;
    }

    .btn {
        padding: 0.65rem 1.25rem;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        font-weight: 500;
        border: none;
        cursor: pointer;
    }

    .btn-primary {
        background: #3b134f;
        color: #fff;
    }

    .btn-primary:hover {
        background: #4a1a63;
    }
</style>