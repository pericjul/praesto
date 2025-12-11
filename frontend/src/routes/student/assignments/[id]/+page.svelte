<script>
    import { enhance } from "$app/forms";
    import { goto } from "$app/navigation";

    let { data, form } = $props();

    let assignment = $derived(data?.assignment);
    let submission = $derived(data?.submission);
    let session = $derived(data?.session);

    // Form state
    let reflectionText = $state("");
    let researchText = $state("");
    let researchLinks = $state("");
    let isStartingSession = $state(false);

    const assignmentTypes = {
        AI_INTERVIEW: { label: "🤖 KI-Bewerbungsgespräch", icon: "🤖", color: "#8b5cf6" },
        DOCUMENT_UPLOAD: { label: "📄 Dokument einreichen", icon: "📄", color: "#3b82f6" },
        SELF_REFLECTION: { label: "✍️ Selbstreflexion", icon: "✍️", color: "#10b981" },
        VIDEO_PITCH: { label: "🎥 Video-Bewerbung", icon: "🎥", color: "#f59e0b" },
        RESEARCH: { label: "🔍 Recherche", icon: "🔍", color: "#6366f1" }
    };

    function getTypeInfo(type) {
        return assignmentTypes[type] ?? { label: type, icon: "📋", color: "#6b7280" };
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

    async function startAIInterview() {
        isStartingSession = true;
        try {
            const response = await fetch("/api/sessions/start", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ assignmentId: assignment.id })
            });
            
            if (response.ok) {
                const newSession = await response.json();
                goto(`/student/sessions/${newSession.id}`);
            } else {
                const error = await response.text();
                alert("Fehler beim Starten: " + error);
            }
        } catch (err) {
            console.error("Fehler:", err);
            alert("Verbindungsfehler. Bitte versuche es erneut.");
        } finally {
            isStartingSession = false;
        }
    }
</script>

<svelte:head>
    <title>{assignment?.title ?? "Aufgabe"} – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <!-- Breadcrumb -->
    <nav class="breadcrumb">
        <a href="/student/dashboard">Dashboard</a>
        <span class="sep">›</span>
        <a href="/student/assignments">Aufgaben</a>
        <span class="sep">›</span>
        <span class="current">{assignment?.title ?? "Aufgabe"}</span>
    </nav>

    {#if !assignment}
        <div class="error-state">
            <h2>Aufgabe nicht gefunden</h2>
            <p>Diese Aufgabe existiert nicht oder du hast keinen Zugriff darauf.</p>
            <a href="/student/assignments" class="btn btn-primary">Zurück zur Übersicht</a>
        </div>
    {:else}
        {@const typeInfo = getTypeInfo(assignment.type)}
        {@const deadlineStatus = getDeadlineStatus(assignment.dueDate)}
        {@const hasSubmitted = !!submission}

        <!-- Header -->
        <header class="assignment-header" class:submitted={hasSubmitted}>
            <div class="header-top">
                <span class="type-badge" style="background: {typeInfo.color}15; color: {typeInfo.color}">
                    {typeInfo.label}
                </span>
                {#if hasSubmitted}
                    <span class="status-badge submitted">✓ Abgegeben</span>
                {:else if deadlineStatus === "overdue"}
                    <span class="status-badge overdue">⚠️ Überfällig</span>
                {:else if deadlineStatus === "soon"}
                    <span class="status-badge soon">🔥 Bald fällig</span>
                {:else}
                    <span class="status-badge open">Offen</span>
                {/if}
            </div>
            
            <h1 class="assignment-title">{assignment.title}</h1>
            
            <div class="assignment-meta">
                {#if assignment.dueDate}
                    <span class="meta-item" class:overdue={deadlineStatus === "overdue" && !hasSubmitted}>
                        📅 Deadline: {formatDate(assignment.dueDate)}
                    </span>
                {/if}
                {#if assignment.durationMin}
                    <span class="meta-item">⏱️ ca. {assignment.durationMin} Minuten</span>
                {/if}
            </div>
        </header>

        <!-- Alerts -->
        {#if form?.success}
            <div class="alert alert-success">✓ {form.message}</div>
        {/if}
        {#if form?.error}
            <div class="alert alert-danger">{form.error}</div>
        {/if}

        <div class="content-grid">
            <!-- Linke Spalte: Beschreibung & Abgabe -->
            <div class="main-content">
                <!-- Beschreibung -->
                {#if assignment.description}
                    <section class="card">
                        <h2 class="card-title">📋 Aufgabenbeschreibung</h2>
                        <div class="description">
                            {assignment.description}
                        </div>
                    </section>
                {/if}

                <!-- Abgabe-Bereich -->
                {#if hasSubmitted}
                    <!-- Abgabe anzeigen -->
                    <section class="card submitted-card">
                        <h2 class="card-title">✅ Deine Abgabe</h2>
                        <div class="submission-info">
                            <p class="submitted-at">
                                📤 Abgegeben am {formatDateTime(submission.submittedAt)}
                            </p>

                            {#if submission.textContent}
                                <div class="submitted-content">
                                    <strong>Dein Text:</strong>
                                    <p>{submission.textContent}</p>
                                </div>
                            {/if}

                            {#if submission.links && submission.links.length > 0}
                                <div class="submitted-links">
                                    <strong>Deine Links:</strong>
                                    <ul>
                                        {#each submission.links as link}
                                            <li><a href={link} target="_blank" rel="noopener">{link}</a></li>
                                        {/each}
                                    </ul>
                                </div>
                            {/if}

                            {#if submission.fileName}
                                <div class="submitted-file">
                                    <strong>Hochgeladene Datei:</strong>
                                    <span class="file-name">📎 {submission.fileName}</span>
                                </div>
                            {/if}

                            {#if submission.chatSessionId && session}
                                <div class="submitted-session">
                                    <strong>KI-Interview:</strong>
                                    <p>{session.messages?.length ?? 0} Nachrichten ausgetauscht</p>
                                    <a href="/student/sessions/{submission.chatSessionId}" class="btn btn-secondary">
                                        💬 Chat-Verlauf ansehen
                                    </a>
                                </div>
                            {/if}
                        </div>
                    </section>
                {:else}
                    <!-- Abgabe-Formular -->
                    <section class="card">
                        <h2 class="card-title">{typeInfo.icon} Aufgabe bearbeiten</h2>
                        
                        {#if assignment.type === "AI_INTERVIEW"}
                            <p class="form-hint">
                                Starte ein KI-gestütztes Bewerbungsgespräch. Der KI-Coach wird dir Fragen stellen
                                und dir Feedback geben. Am Ende kannst du das Gespräch als Abgabe einreichen.
                            </p>
                            <button 
                                class="btn btn-primary btn-large" 
                                onclick={startAIInterview}
                                disabled={isStartingSession}
                            >
                                {isStartingSession ? "Wird gestartet..." : "🤖 KI-Interview starten"}
                            </button>

                        {:else if assignment.type === "SELF_REFLECTION"}
                            <form method="POST" action="?/submitReflection" use:enhance>
                                <p class="form-hint">
                                    Schreibe eine Selbstreflexion zu diesem Thema. Mindestens 50 Zeichen.
                                </p>
                                <textarea 
                                    name="reflection" 
                                    bind:value={reflectionText}
                                    placeholder="Schreibe hier deine Reflexion..."
                                    rows="8"
                                    class="textarea"
                                ></textarea>
                                <div class="char-count" class:warning={reflectionText.length > 0 && reflectionText.length < 50}>
                                    {reflectionText.length} / min. 50 Zeichen
                                </div>
                                <button type="submit" class="btn btn-primary" disabled={reflectionText.length < 50}>
                                    ✓ Abgeben
                                </button>
                            </form>

                        {:else if assignment.type === "RESEARCH"}
                            <form method="POST" action="?/submitResearch" use:enhance>
                                <p class="form-hint">
                                    Dokumentiere deine Recherche-Ergebnisse. Mindestens 50 Zeichen.
                                </p>
                                <textarea 
                                    name="researchText" 
                                    bind:value={researchText}
                                    placeholder="Deine Recherche-Ergebnisse..."
                                    rows="6"
                                    class="textarea"
                                ></textarea>
                                <div class="char-count" class:warning={researchText.length > 0 && researchText.length < 50}>
                                    {researchText.length} / min. 50 Zeichen
                                </div>
                                <label class="label">Links (optional, einer pro Zeile)</label>
                                <textarea 
                                    name="links" 
                                    bind:value={researchLinks}
                                    placeholder="https://example.com&#10;https://andere-quelle.ch"
                                    rows="3"
                                    class="textarea"
                                ></textarea>
                                <button type="submit" class="btn btn-primary" disabled={researchText.length < 50}>
                                    ✓ Abgeben
                                </button>
                            </form>

                        {:else if assignment.type === "DOCUMENT_UPLOAD"}
                            <form method="POST" action="?/submitDocument" use:enhance enctype="multipart/form-data">
                                <p class="form-hint">
                                    Lade ein Dokument hoch (PDF, Word, etc.)
                                </p>
                                <input type="file" name="document" class="file-input" accept=".pdf,.doc,.docx,.txt" />
                                <label class="label">Kommentar (optional)</label>
                                <textarea name="comment" placeholder="Optionaler Kommentar..." rows="3" class="textarea"></textarea>
                                <button type="submit" class="btn btn-primary">
                                    📤 Hochladen & Abgeben
                                </button>
                            </form>

                        {:else if assignment.type === "VIDEO_PITCH"}
                            <form method="POST" action="?/submitVideo" use:enhance enctype="multipart/form-data">
                                <p class="form-hint">
                                    Lade dein Video hoch (MP4, MOV, WebM)
                                </p>
                                <input type="file" name="video" class="file-input" accept="video/*" />
                                <button type="submit" class="btn btn-primary">
                                    🎥 Video hochladen & Abgeben
                                </button>
                            </form>
                        {/if}
                    </section>
                {/if}
            </div>

            <!-- Rechte Spalte: Feedback -->
            <div class="sidebar">
                {#if hasSubmitted && submission.teacherFeedback}
                    <section class="card feedback-card">
                        <h2 class="card-title">💬 Feedback</h2>
                        <div class="feedback-content">
                            <p>{submission.teacherFeedback}</p>
                            
                            {#if submission.grade != null}
                                <div class="grade-display">
                                    <span class="grade-label">Note</span>
                                    <span class="grade-value">{submission.grade}</span>
                                </div>
                            {/if}

                            {#if submission.reviewedAt}
                                <p class="reviewed-at">
                                    Bewertet am {formatDateTime(submission.reviewedAt)}
                                </p>
                            {/if}
                        </div>
                    </section>
                {:else if hasSubmitted}
                    <section class="card waiting-card">
                        <h2 class="card-title">⏳ Warten auf Feedback</h2>
                        <p>Deine Abgabe wird von der Lehrperson geprüft. Du erhältst eine Benachrichtigung, sobald Feedback vorhanden ist.</p>
                    </section>
                {:else}
                    <section class="card info-card">
                        <h2 class="card-title">ℹ️ Hinweise</h2>
                        <ul class="hints-list">
                            <li>Lies die Aufgabenbeschreibung sorgfältig durch</li>
                            <li>Nach der Abgabe kannst du deine Antwort nicht mehr ändern</li>
                            <li>Die Lehrperson wird deine Abgabe bewerten und Feedback geben</li>
                            {#if assignment.dueDate}
                                <li>Versuche, vor der Deadline abzugeben</li>
                            {/if}
                        </ul>
                    </section>
                {/if}
            </div>
        </div>
    {/if}
</div>

<style>
    .page-wrapper {
        max-width: 1000px;
        margin: 0 auto;
        padding: 1.5rem 1rem 3rem;
    }

    /* Breadcrumb */
    .breadcrumb {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        margin-bottom: 1.5rem;
        font-size: 0.9rem;
    }

    .breadcrumb a {
        color: #7c6b80;
        text-decoration: none;
    }

    .breadcrumb a:hover {
        color: #3b134f;
        text-decoration: underline;
    }

    .breadcrumb .sep {
        color: #d1d5db;
    }

    .breadcrumb .current {
        color: #2d2141;
        font-weight: 500;
    }

    /* Error State */
    .error-state {
        text-align: center;
        padding: 3rem;
        background: #fef2f2;
        border-radius: 1rem;
    }

    .error-state h2 {
        color: #dc2626;
        margin: 0 0 0.5rem;
    }

    .error-state p {
        color: #6b7280;
        margin: 0 0 1.5rem;
    }

    /* Header */
    .assignment-header {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 1rem;
        padding: 1.5rem;
        margin-bottom: 1.5rem;
    }

    .assignment-header.submitted {
        background: #f0fdf4;
        border-color: #bbf7d0;
    }

    .header-top {
        display: flex;
        gap: 0.75rem;
        margin-bottom: 0.75rem;
        flex-wrap: wrap;
    }

    .type-badge, .status-badge {
        padding: 0.3rem 0.75rem;
        border-radius: 1rem;
        font-size: 0.85rem;
        font-weight: 500;
    }

    .status-badge.submitted {
        background: #d1fae5;
        color: #059669;
    }

    .status-badge.overdue {
        background: #fee2e2;
        color: #dc2626;
    }

    .status-badge.soon {
        background: #fef3c7;
        color: #d97706;
    }

    .status-badge.open {
        background: #e0e7ff;
        color: #4f46e5;
    }

    .assignment-title {
        margin: 0 0 0.75rem;
        font-size: 1.5rem;
        font-weight: 700;
        color: #2d2141;
    }

    .assignment-meta {
        display: flex;
        gap: 1.5rem;
        flex-wrap: wrap;
        font-size: 0.9rem;
        color: #7c6b80;
    }

    .meta-item.overdue {
        color: #dc2626;
        font-weight: 500;
    }

    /* Alerts */
    .alert {
        padding: 1rem;
        border-radius: 0.5rem;
        margin-bottom: 1.5rem;
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

    /* Content Grid */
    .content-grid {
        display: grid;
        grid-template-columns: 1fr 320px;
        gap: 1.5rem;
        align-items: start;
    }

    @media (max-width: 850px) {
        .content-grid {
            grid-template-columns: 1fr;
        }
    }

    /* Cards */
    .card {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 1rem;
        padding: 1.5rem;
        margin-bottom: 1.5rem;
    }

    .card-title {
        margin: 0 0 1rem;
        font-size: 1.1rem;
        font-weight: 600;
        color: #3b134f;
    }

    .description {
        color: #4b5563;
        line-height: 1.6;
        white-space: pre-wrap;
    }

    /* Submitted Card */
    .submitted-card {
        background: #f0fdf4;
        border-color: #bbf7d0;
    }

    .submission-info {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .submitted-at {
        color: #059669;
        font-weight: 500;
        margin: 0;
    }

    .submitted-content, .submitted-links, .submitted-file, .submitted-session {
        padding: 1rem;
        background: #fff;
        border-radius: 0.5rem;
        border: 1px solid #d1fae5;
    }

    .submitted-content strong,
    .submitted-links strong,
    .submitted-file strong,
    .submitted-session strong {
        display: block;
        margin-bottom: 0.5rem;
        color: #166534;
    }

    .submitted-content p,
    .submitted-session p {
        margin: 0;
        color: #4b5563;
    }

    .submitted-links ul {
        margin: 0;
        padding-left: 1.25rem;
    }

    .submitted-links a {
        color: #3b82f6;
        word-break: break-all;
    }

    .file-name {
        color: #4b5563;
    }

    /* Feedback Card */
    .feedback-card {
        background: #faf5ff;
        border-color: #e9d5ff;
    }

    .feedback-content p {
        margin: 0 0 1rem;
        color: #4c1d95;
        line-height: 1.6;
    }

    .grade-display {
        display: inline-flex;
        flex-direction: column;
        align-items: center;
        padding: 1rem 1.5rem;
        background: #7c3aed;
        border-radius: 0.75rem;
        margin-bottom: 1rem;
    }

    .grade-label {
        font-size: 0.75rem;
        color: rgba(255, 255, 255, 0.8);
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }

    .grade-value {
        font-size: 2rem;
        font-weight: 700;
        color: #fff;
    }

    .reviewed-at {
        margin: 0;
        font-size: 0.85rem;
        color: #7c3aed;
    }

    /* Waiting & Info Cards */
    .waiting-card {
        background: #fffbeb;
        border-color: #fde68a;
    }

    .waiting-card p {
        margin: 0;
        color: #92400e;
        font-size: 0.9rem;
    }

    .info-card {
        background: #f0f9ff;
        border-color: #bae6fd;
    }

    .hints-list {
        margin: 0;
        padding-left: 1.25rem;
        color: #0369a1;
        font-size: 0.9rem;
    }

    .hints-list li {
        margin-bottom: 0.5rem;
    }

    /* Forms */
    .form-hint {
        margin: 0 0 1rem;
        color: #6b7280;
        font-size: 0.9rem;
    }

    .label {
        display: block;
        margin: 1rem 0 0.5rem;
        font-weight: 500;
        color: #374151;
        font-size: 0.9rem;
    }

    .textarea {
        width: 100%;
        padding: 0.75rem;
        border: 1px solid #d1d5db;
        border-radius: 0.5rem;
        font-size: 0.95rem;
        font-family: inherit;
        resize: vertical;
        box-sizing: border-box;
    }

    .textarea:focus {
        outline: none;
        border-color: #7c3aed;
        box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.1);
    }

    .char-count {
        font-size: 0.8rem;
        color: #9ca3af;
        margin: 0.25rem 0 1rem;
    }

    .char-count.warning {
        color: #d97706;
    }

    .file-input {
        display: block;
        margin-bottom: 1rem;
        padding: 0.5rem;
        border: 2px dashed #d1d5db;
        border-radius: 0.5rem;
        width: 100%;
        box-sizing: border-box;
    }

    /* Buttons */
    .btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        padding: 0.6rem 1.25rem;
        border-radius: 0.5rem;
        font-size: 0.95rem;
        font-weight: 500;
        border: none;
        cursor: pointer;
        text-decoration: none;
        transition: all 0.15s;
    }

    .btn-primary {
        background: #3b134f;
        color: #fff;
    }

    .btn-primary:hover:not(:disabled) {
        background: #4a1d63;
    }

    .btn-primary:disabled {
        background: #9ca3af;
        cursor: not-allowed;
    }

    .btn-secondary {
        background: #f3f4f6;
        color: #374151;
        border: 1px solid #d1d5db;
    }

    .btn-secondary:hover {
        background: #e5e7eb;
    }

    .btn-large {
        padding: 0.85rem 1.75rem;
        font-size: 1rem;
    }
</style>