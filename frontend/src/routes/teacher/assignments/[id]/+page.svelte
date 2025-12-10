<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";

    let { data, form } = $props();

    // State
    let showModal = $state(false);
    let selectedSub = $state(null);
    let filterStatus = $state("all");
    let searchQuery = $state("");

    // Derived
    let assignment = $derived(data?.assignment ?? null);
    let submissions = $derived(data?.submissions ?? []);
    let schoolClass = $derived(data?.schoolClass ?? null);
    let error = $derived(data?.error ?? null);
    let allStudents = $derived(schoolClass?.studentEmails ?? []);
    let submittedEmails = $derived(submissions.map(s => s.studentEmail));
    let pendingStudents = $derived(allStudents.filter(email => !submittedEmails.includes(email)));

    // Counts
    let needsFeedbackCount = $derived(submissions.filter(s => !s.teacherFeedback).length);
    let reviewedCount = $derived(submissions.filter(s => s.teacherFeedback).length);

    // Gefilterte Submissions
    let filteredSubmissions = $derived(() => {
        let result = submissions;

        if (filterStatus === "needsFeedback") {
            result = result.filter(s => !s.teacherFeedback);
        } else if (filterStatus === "reviewed") {
            result = result.filter(s => s.teacherFeedback);
        }

        if (searchQuery.trim()) {
            const q = searchQuery.toLowerCase();
            result = result.filter(s => s.studentEmail.toLowerCase().includes(q));
        }

        return result;
    });

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

    function formatShortDate(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit"
        });
    }

    function shortenEmail(email) {
        return email.split("@")[0];
    }

    function openModal(sub) {
        console.log("Opening modal for:", sub);
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
        <!-- Header -->
        <header class="page-header">
            <a href="/teacher/assignments" class="back-link">← Zurück</a>
            <div class="header-info">
                <span class="type-tag" style="background: {getTypeInfo(assignment.type).color}20; color: {getTypeInfo(assignment.type).color}">
                    {getTypeInfo(assignment.type).label}
                </span>
                <h1>{assignment.title}</h1>
                <div class="meta">
                    <span>👥 {schoolClass?.name ?? "?"}</span>
                    <span>📅 {formatDate(assignment.dueDate)}</span>
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

        <!-- Stats -->
        <div class="stats-row">
            <div class="stat">
                <span class="stat-num">{submissions.length}</span>
                <span class="stat-label">Abgegeben</span>
            </div>
            <div class="stat warning">
                <span class="stat-num">{pendingStudents.length}</span>
                <span class="stat-label">Fehlen noch</span>
            </div>
            <div class="stat alert">
                <span class="stat-num">{needsFeedbackCount}</span>
                <span class="stat-label">Feedback offen</span>
            </div>
            <div class="stat success">
                <span class="stat-num">{reviewedCount}</span>
                <span class="stat-label">Erledigt</span>
            </div>
        </div>

        <!-- Filter Pills -->
        <div class="filter-section">
            <div class="filter-pills">
                <button 
                    type="button"
                    class="pill" 
                    class:active={filterStatus === "all"}
                    onclick={() => filterStatus = "all"}
                >
                    Alle ({submissions.length})
                </button>
                <button 
                    type="button"
                    class="pill pill-alert" 
                    class:active={filterStatus === "needsFeedback"}
                    onclick={() => filterStatus = "needsFeedback"}
                >
                    🔔 Offen ({needsFeedbackCount})
                </button>
                <button 
                    type="button"
                    class="pill pill-success" 
                    class:active={filterStatus === "reviewed"}
                    onclick={() => filterStatus = "reviewed"}
                >
                    ✓ Erledigt ({reviewedCount})
                </button>
            </div>
            
            {#if submissions.length > 5}
                <div class="search-box">
                    <input 
                        type="text" 
                        placeholder="Schüler suchen..." 
                        bind:value={searchQuery}
                    />
                </div>
            {/if}
        </div>

        <!-- Submissions -->
        {#if submissions.length === 0}
            <div class="empty-state">
                <p>Noch keine Abgaben eingegangen.</p>
            </div>
        {:else if filteredSubmissions().length === 0}
            <div class="empty-state small">
                <p>Keine Abgaben in dieser Kategorie.</p>
            </div>
        {:else}
            <div class="submissions-list">
                {#each filteredSubmissions() as sub}
                    <div class="submission-row" class:reviewed={sub.teacherFeedback}>
                        <div class="sub-main">
                            <div class="sub-student">
                                <span class="student-name">{shortenEmail(sub.studentEmail)}</span>
                                <span class="student-email">{sub.studentEmail}</span>
                            </div>
                            <span class="sub-date">{formatShortDate(sub.submittedAt)}</span>
                        </div>

                        {#if assignment.type === "AI_INTERVIEW" && sub.chatSessionId}
                            <a href="/teacher/sessions/{sub.chatSessionId}" class="chat-link">
                                💬 Chat ansehen
                            </a>
                        {/if}

                        {#if sub.textContent}
                            <div class="sub-preview">
                                {sub.textContent.substring(0, 150)}{sub.textContent.length > 150 ? "..." : ""}
                            </div>
                        {/if}

                        <div class="sub-status">
                            {#if sub.teacherFeedback}
                                <div class="feedback-summary">
                                    <span class="feedback-badge">✓ Feedback gegeben</span>
                                    {#if sub.grade != null}
                                        <span class="grade-badge">Note: {sub.grade}</span>
                                    {/if}
                                </div>
                                <p class="feedback-preview">{sub.teacherFeedback.substring(0, 100)}{sub.teacherFeedback.length > 100 ? "..." : ""}</p>
                            {:else}
                                <span class="no-feedback">⏳ Wartet auf Feedback</span>
                            {/if}
                        </div>

                        <div class="sub-action">
                            <button 
                                type="button" 
                                class="btn" 
                                class:btn-primary={!sub.teacherFeedback} 
                                class:btn-secondary={sub.teacherFeedback} 
                                onclick={() => openModal(sub)}
                            >
                                {sub.teacherFeedback ? "✏️ Bearbeiten" : "💬 Feedback"}
                            </button>
                        </div>
                    </div>
                {/each}
            </div>
        {/if}

        <!-- Pending Students -->
        {#if pendingStudents.length > 0}
            <div class="pending-section">
                <h3>⏳ Noch nicht abgegeben ({pendingStudents.length})</h3>
                <div class="pending-chips">
                    {#each pendingStudents as email}
                        <span class="pending-chip">{shortenEmail(email)}</span>
                    {/each}
                </div>
            </div>
        {/if}
    {/if}
</div>

<!-- MODAL MIT INLINE STYLES -->
{#if showModal && selectedSub}
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <div 
        style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); backdrop-filter: blur(4px); z-index: 9998; cursor: pointer;"
        onclick={closeModal}
    ></div>
    <div 
        style="position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); background: #ffffff; border-radius: 0.75rem; width: 90%; max-width: 550px; max-height: 90vh; overflow: hidden; z-index: 9999; box-shadow: 0 25px 50px rgba(0, 0, 0, 0.3);"
    >
        <div style="display: flex; justify-content: space-between; align-items: flex-start; padding: 1rem 1.25rem; background: linear-gradient(135deg, #3b134f, #5a2d6e); color: #fff;">
            <div>
                <h2 style="margin: 0; font-size: 1.1rem; font-weight: 600;">💬 Feedback geben</h2>
                <span style="font-size: 0.8rem; opacity: 0.85;">{selectedSub.studentEmail}</span>
            </div>
            <button 
                type="button" 
                style="background: rgba(255,255,255,0.2); border: none; color: #fff; padding: 0.4rem 0.6rem; border-radius: 0.4rem; cursor: pointer; font-size: 1rem;"
                onclick={closeModal}
            >✕</button>
        </div>
        
        <form 
            style="padding: 1.25rem; overflow-y: auto; max-height: calc(90vh - 80px);"
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
                <div style="margin-bottom: 1rem;">
                    <label style="display: block; font-size: 0.8rem; font-weight: 600; color: #6b7280; margin-bottom: 0.35rem;">Abgabe des Schülers:</label>
                    <div style="background: #f9fafb; padding: 0.75rem; border-radius: 0.5rem; font-size: 0.9rem; max-height: 150px; overflow-y: auto; color: #374151; line-height: 1.5; border: 1px solid #e5e7eb;">
                        {selectedSub.textContent}
                    </div>
                </div>
            {/if}

            {#if assignment.type === "AI_INTERVIEW" && selectedSub.chatSessionId}
                <a 
                    href="/teacher/sessions/{selectedSub.chatSessionId}" 
                    target="_blank"
                    style="display: block; padding: 0.6rem; background: #f3e8ff; color: #7c3aed; border-radius: 0.4rem; text-decoration: none; font-size: 0.85rem; font-weight: 500; text-align: center; margin-bottom: 1rem;"
                >
                    💬 Chat-Verlauf in neuem Tab öffnen →
                </a>
            {/if}

            <div style="margin-bottom: 1rem;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; color: #374151; margin-bottom: 0.35rem;">Dein Feedback</label>
                <textarea 
                    name="feedback" 
                    rows="5"
                    placeholder="Schreibe hier dein Feedback..."
                    style="width: 100%; padding: 0.65rem; border: 1px solid #e5e7eb; border-radius: 0.5rem; font-size: 0.95rem; font-family: inherit; box-sizing: border-box; resize: vertical;"
                >{selectedSub.teacherFeedback ?? ""}</textarea>
            </div>

            <div style="margin-bottom: 1rem;">
                <label style="display: block; font-size: 0.85rem; font-weight: 600; color: #374151; margin-bottom: 0.35rem;">Note (optional)</label>
                <input 
                    type="number" 
                    name="grade" 
                    min="1" 
                    max="6" 
                    step="0.5"
                    value={selectedSub.grade ?? ""}
                    placeholder="1-6"
                    style="width: 100px; padding: 0.65rem; border: 1px solid #e5e7eb; border-radius: 0.5rem; font-size: 0.95rem;"
                />
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #e5e7eb;">
                <button 
                    type="button" 
                    onclick={closeModal}
                    style="padding: 0.6rem 1.25rem; border-radius: 0.5rem; font-size: 0.9rem; font-weight: 500; border: 1px solid #e5e7eb; cursor: pointer; background: #f3f4f6; color: #374151;"
                >Abbrechen</button>
                <button 
                    type="submit"
                    style="padding: 0.6rem 1.25rem; border-radius: 0.5rem; font-size: 0.9rem; font-weight: 500; border: none; cursor: pointer; background: #3b134f; color: #fff;"
                >💾 Speichern</button>
            </div>
        </form>
    </div>
{/if}

<style>
    .page-wrapper {
        max-width: 900px;
        margin: 0 auto;
        padding: 1.5rem 1rem 3rem;
    }

    /* Header */
    .page-header {
        margin-bottom: 1.5rem;
    }

    .back-link {
        display: inline-block;
        margin-bottom: 0.75rem;
        color: #7c6b80;
        text-decoration: none;
        font-size: 0.85rem;
    }

    .back-link:hover {
        color: #3b134f;
    }

    .header-info h1 {
        margin: 0.25rem 0;
        font-size: 1.4rem;
        color: #2d2141;
    }

    .type-tag {
        display: inline-block;
        padding: 0.2rem 0.6rem;
        border-radius: 0.4rem;
        font-size: 0.8rem;
        font-weight: 500;
    }

    .meta {
        display: flex;
        gap: 1rem;
        font-size: 0.85rem;
        color: #6b7280;
    }

    /* Stats - FIXED: alle gleiche Höhe */
    .stats-row {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 0.75rem;
        margin-bottom: 1rem;
    }

    .stat {
        text-align: center;
        padding: 0.75rem 0.5rem;
        background: #f9fafb;
        border-radius: 0.5rem;
        border: 1px solid #e5e7eb;
        min-height: 80px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
    }

    .stat-num {
        display: block;
        font-size: 1.5rem;
        font-weight: 700;
        color: #374151;
        line-height: 1.2;
    }

    .stat-label {
        display: block;
        font-size: 0.7rem;
        color: #6b7280;
        margin-top: 0.2rem;
        white-space: nowrap;
    }

    .stat.warning { background: #fef3c7; border-color: #fcd34d; }
    .stat.warning .stat-num { color: #b45309; }
    
    .stat.alert { background: #fee2e2; border-color: #fca5a5; }
    .stat.alert .stat-num { color: #dc2626; }
    
    .stat.success { background: #d1fae5; border-color: #6ee7b7; }
    .stat.success .stat-num { color: #059669; }

    /* Filter Section */
    .filter-section {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 1rem;
        margin-bottom: 1rem;
        flex-wrap: wrap;
    }

    .filter-pills {
        display: flex;
        gap: 0.5rem;
    }

    .pill {
        padding: 0.4rem 0.85rem;
        border: 1px solid #e5e7eb;
        border-radius: 2rem;
        background: #fff;
        font-size: 0.8rem;
        cursor: pointer;
        color: #6b7280;
    }

    .pill:hover {
        border-color: #3b134f;
    }

    .pill.active {
        background: #3b134f;
        color: #fff;
        border-color: #3b134f;
    }

    .pill-alert {
        border-color: #fca5a5;
        color: #dc2626;
    }

    .pill-alert.active {
        background: #dc2626;
        border-color: #dc2626;
        color: #fff;
    }

    .pill-success {
        border-color: #6ee7b7;
        color: #059669;
    }

    .pill-success.active {
        background: #059669;
        border-color: #059669;
        color: #fff;
    }

    .search-box input {
        padding: 0.5rem 0.75rem;
        border: 1px solid #e5e7eb;
        border-radius: 0.4rem;
        font-size: 0.85rem;
        width: 180px;
    }

    /* Submissions List */
    .submissions-list {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .submission-row {
        background: #fff;
        border: 1px solid #e5e7eb;
        border-radius: 0.5rem;
        padding: 1rem;
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .submission-row.reviewed {
        background: #f0fdf4;
        border-color: #bbf7d0;
    }

    .sub-main {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
    }

    .sub-student {
        display: flex;
        flex-direction: column;
    }

    .student-name {
        font-weight: 600;
        color: #2d2141;
    }

    .student-email {
        font-size: 0.75rem;
        color: #9ca3af;
    }

    .sub-date {
        font-size: 0.8rem;
        color: #9ca3af;
    }

    .chat-link {
        display: inline-block;
        padding: 0.35rem 0.65rem;
        background: #f3e8ff;
        color: #7c3aed;
        border-radius: 0.4rem;
        font-size: 0.8rem;
        text-decoration: none;
        font-weight: 500;
        width: fit-content;
    }

    .sub-preview {
        font-size: 0.85rem;
        color: #6b7280;
        background: #f9fafb;
        padding: 0.5rem 0.75rem;
        border-radius: 0.4rem;
        line-height: 1.4;
    }

    .sub-status {
        padding: 0.5rem 0;
    }

    .feedback-summary {
        display: flex;
        gap: 0.5rem;
        align-items: center;
        margin-bottom: 0.25rem;
    }

    .feedback-badge {
        font-size: 0.75rem;
        color: #059669;
        font-weight: 600;
    }

    .grade-badge {
        font-size: 0.7rem;
        background: #7c3aed;
        color: #fff;
        padding: 0.15rem 0.5rem;
        border-radius: 1rem;
        font-weight: 600;
    }

    .feedback-preview {
        margin: 0;
        font-size: 0.8rem;
        color: #6b7280;
        font-style: italic;
    }

    .no-feedback {
        font-size: 0.85rem;
        color: #f59e0b;
        font-weight: 500;
    }

    .sub-action {
        display: flex;
        justify-content: flex-end;
        padding-top: 0.5rem;
        border-top: 1px solid #f3f4f6;
    }

    /* Pending Section */
    .pending-section {
        margin-top: 2rem;
        padding: 1rem;
        background: #fffbeb;
        border: 1px solid #fcd34d;
        border-radius: 0.5rem;
    }

    .pending-section h3 {
        margin: 0 0 0.75rem;
        font-size: 0.9rem;
        color: #92400e;
    }

    .pending-chips {
        display: flex;
        gap: 0.4rem;
        flex-wrap: wrap;
    }

    .pending-chip {
        padding: 0.25rem 0.6rem;
        background: #fef3c7;
        color: #92400e;
        border-radius: 1rem;
        font-size: 0.8rem;
    }

    /* Empty State */
    .empty-state {
        padding: 2rem;
        text-align: center;
        background: #f9fafb;
        border: 1px dashed #e5e7eb;
        border-radius: 0.5rem;
        color: #6b7280;
    }

    .empty-state.small {
        padding: 1.5rem;
    }

    /* Buttons */
    .btn {
        padding: 0.5rem 1rem;
        border-radius: 0.4rem;
        font-size: 0.85rem;
        font-weight: 500;
        border: none;
        cursor: pointer;
    }

    .btn-primary {
        background: #3b134f;
        color: #fff;
    }

    .btn-secondary {
        background: #f3f4f6;
        color: #374151;
    }

    /* Alerts */
    .alert {
        padding: 0.75rem 1rem;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
    }

    .alert-success { background: #f0fdf4; color: #166534; }
    .alert-danger { background: #fef2f2; color: #dc2626; }

    @media (max-width: 600px) {
        .stats-row {
            grid-template-columns: repeat(2, 1fr);
        }

        .filter-section {
            flex-direction: column;
            align-items: flex-start;
        }

        .search-box input {
            width: 100%;
        }
    }
</style>