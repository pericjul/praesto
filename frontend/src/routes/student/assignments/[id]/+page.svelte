<script>
    import { enhance } from "$app/forms";
    import { goto } from "$app/navigation";

    let { data, form } = $props();

    let assignment = $derived(data?.assignment);
    let submission = $derived(data?.submission);
    let session = $derived(data?.session);

    let reflectionText = $state("");
    let researchText = $state("");
    let researchLinks = $state("");
    let isStartingSession = $state(false);

    const typeLabels = {
        AI_INTERVIEW: "🤖 KI-Interview",
        DOCUMENT_UPLOAD: "📄 Dokument",
        SELF_REFLECTION: "✍️ Reflexion",
        RESEARCH: "🔍 Recherche"
    };

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

    function isOverdue(dueDate) {
        if (!dueDate) return false;
        return new Date(dueDate) < new Date();
    }

    function getDaysRemaining(dueDate) {
        if (!dueDate) return null;
        const days = Math.ceil((new Date(dueDate) - new Date()) / (1000 * 60 * 60 * 24));
        return days;
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
                alert("Fehler beim Starten");
            }
        } catch {
            alert("Verbindungsfehler");
        } finally {
            isStartingSession = false;
        }
    }
</script>

<svelte:head>
    <title>{assignment?.title ?? "Aufgabe"} – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    {#if !assignment}
        <div class="empty-state">
            <span class="empty-icon">📭</span>
            <h2>Aufgabe nicht gefunden</h2>
            <p>Diese Aufgabe existiert nicht oder du hast keinen Zugriff.</p>
            <a href="/student/assignments" class="btn btn-primary">← Zur Übersicht</a>
        </div>
    {:else}
        <!-- Header -->
        <header class="page-header">
            <a href="/student/assignments" class="back-link">← Zurück zu Aufgaben</a>
            <h1 class="title">{assignment.title}</h1>
            <div class="header-title-row">
                <span class="type-badge">{typeLabels[assignment.type] ?? assignment.type}</span>
                {#if submission}
                    <span class="status-badge success">✓ Abgegeben</span>
                {:else if isOverdue(assignment.dueDate)}
                    <span class="status-badge danger">⚠️ Überfällig</span>
                {:else}
                    {@const days = getDaysRemaining(assignment.dueDate)}
                    {#if days !== null && days <= 3}
                        <span class="status-badge warning">🔥 Noch {days} {days === 1 ? 'Tag' : 'Tage'}</span>
                    {:else}
                        <span class="status-badge">Offen</span>
                    {/if}
                {/if}
                <span class="header-meta">📅 Deadline: {formatDate(assignment.dueDate)}</span>
                {#if assignment.durationMin}
                    <span class="header-meta">⏱️ {assignment.durationMin} Min</span>
                {/if}
            </div>
        </header>

        {#if form?.success}
            <div class="alert alert-success">✓ {form.message}</div>
        {/if}
        {#if form?.error}
            <div class="alert alert-danger">{form.error}</div>
        {/if}

        <div class="content-grid">
            <!-- Hauptbereich -->
            <main class="main-content">
                <!-- Beschreibung -->
                {#if assignment.description}
                    <section class="card">
                        <h2>📋 Beschreibung</h2>
                        <p class="description">{assignment.description}</p>
                    </section>
                {/if}

                <!-- Bereits abgegeben -->
                {#if submission}
                    <section class="card card-success">
                        <h2>✅ Deine Abgabe</h2>
                        <p class="meta-info">Abgegeben am {formatDateTime(submission.submittedAt)}</p>

                        {#if submission.textContent}
                            <div class="content-box">
                                <strong>Dein Text:</strong>
                                <p>{submission.textContent}</p>
                            </div>
                        {/if}

                        {#if submission.chatSessionId}
                            <a href="/student/sessions/{submission.chatSessionId}" class="btn btn-secondary">
                                💬 Chat-Verlauf ansehen
                            </a>
                        {/if}
                    </section>

                    <!-- Feedback -->
                    {#if submission.teacherFeedback}
                        <section class="card card-feedback">
                            <div class="feedback-header">
                                <h2>💬 Feedback</h2>
                                {#if submission.grade != null}
                                    <span class="grade-badge">Note {submission.grade}</span>
                                {/if}
                            </div>
                            <p class="feedback-text">{submission.teacherFeedback}</p>
                        </section>
                    {:else}
                        <section class="card card-waiting">
                            <p>⏳ Deine Lehrperson wird deine Abgabe bald bewerten.</p>
                        </section>
                    {/if}

                {:else}
                    <!-- Noch nicht abgegeben -->
                    <section class="card">
                        <h2>📝 Aufgabe bearbeiten</h2>

                        {#if assignment.type === "AI_INTERVIEW"}
                            <p class="info-text">Starte ein KI-Bewerbungsgespräch und reiche es als Abgabe ein.</p>
                            <button class="btn btn-primary btn-large" onclick={startAIInterview} disabled={isStartingSession}>
                                {isStartingSession ? "Wird gestartet..." : "🤖 Interview starten"}
                            </button>

                        {:else if assignment.type === "SELF_REFLECTION"}
                            <form method="POST" action="?/submitReflection" use:enhance>
                                <p class="info-text">Schreibe eine Selbstreflexion (mind. 50 Zeichen).</p>
                                <div class="form-group">
                                    <textarea name="reflection" bind:value={reflectionText} rows="6" placeholder="Deine Reflexion..."></textarea>
                                    <span class="char-count">{reflectionText.length} / 50 Zeichen</span>
                                </div>
                                <button type="submit" class="btn btn-primary" disabled={reflectionText.length < 50}>Abgeben</button>
                            </form>

                        {:else if assignment.type === "RESEARCH"}
                            <form method="POST" action="?/submitResearch" use:enhance>
                                <p class="info-text">Dokumentiere deine Recherche-Ergebnisse.</p>
                                <div class="form-group">
                                    <textarea name="researchText" bind:value={researchText} rows="5" placeholder="Deine Ergebnisse..."></textarea>
                                    <span class="char-count">{researchText.length} / 50 Zeichen</span>
                                </div>
                                <div class="form-group">
                                    <label>Links (optional, einer pro Zeile)</label>
                                    <textarea name="links" bind:value={researchLinks} rows="2" placeholder="https://..."></textarea>
                                </div>
                                <button type="submit" class="btn btn-primary" disabled={researchText.length < 50}>Abgeben</button>
                            </form>

                        {:else if assignment.type === "DOCUMENT_UPLOAD"}
                            <form method="POST" action="?/submitDocument" enctype="multipart/form-data" use:enhance>
                                <p class="info-text">Lade ein Dokument hoch (PDF, Word, Bild).</p>
                                <div class="form-group">
                                    <input type="file" name="document" accept=".pdf,.doc,.docx,.jpg,.jpeg,.png" />
                                </div>
                                <button type="submit" class="btn btn-primary">Hochladen</button>
                            </form>

                        {:else}
                            <p class="info-text">Dieser Aufgabentyp wird noch nicht unterstützt.</p>
                        {/if}
                    </section>
                {/if}
            </main>

            <!-- Sidebar -->
            <aside class="sidebar">
                <div class="card">
                    <h3>💡 Tipps</h3>
                    {#if assignment.type === "AI_INTERVIEW"}
                        <ul class="tips-list">
                            <li>Antworte in ganzen Sätzen</li>
                            <li>Nimm dir Zeit zum Nachdenken</li>
                            <li>Sei ehrlich und authentisch</li>
                        </ul>
                    {:else if assignment.type === "SELF_REFLECTION"}
                        <ul class="tips-list">
                            <li>Sei ehrlich zu dir selbst</li>
                            <li>Beschreibe konkrete Situationen</li>
                            <li>Überlege, was du lernen kannst</li>
                        </ul>
                    {:else}
                        <ul class="tips-list">
                            <li>Lies die Aufgabe genau durch</li>
                            <li>Plane genug Zeit ein</li>
                            <li>Frag bei Unklarheiten nach</li>
                        </ul>
                    {/if}
                </div>
            </aside>
        </div>
    {/if}
</div>

<style>
    .page-wrapper {
        max-width: 1000px;
        margin: 0 auto;
        padding: 1.5rem 1rem 3rem;
    }

    /* Header */
    .page-header {
        margin-bottom: 1.5rem;
        display: flex;
        flex-direction: column;
        align-items: flex-start;
    }

    .back-link {
        display: inline-block;
        color: #6b647a;
        text-decoration: none;
        font-size: 0.9rem;
        margin-bottom: 0.75rem;
    }

    .back-link:hover {
        color: #2f124d;
    }

    .header-title-row {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        flex-wrap: wrap;
    }

    .type-badge {
        background: #f3e8ff;
        color: #7c3aed;
        padding: 0.25rem 0.6rem;
        border-radius: 0.25rem;
        font-size: 0.8rem;
    }

    .status-badge {
        padding: 0.25rem 0.6rem;
        border-radius: 0.25rem;
        font-size: 0.8rem;
        background: #f3f4f6;
        color: #6b7280;
    }

    .status-badge.success {
        background: #dcfce7;
        color: #166534;
    }

    .status-badge.warning {
        background: #fef3c7;
        color: #92400e;
    }

    .status-badge.danger {
        background: #fee2e2;
        color: #dc2626;
    }

    .header-meta {
        font-size: 0.85rem;
        color: #6b647a;
    }

    .title {
        margin: 0 0 0.5rem;
        font-size: 1.5rem;
        color: #2f124d;
        font-weight: 600;
    }

    /* Alerts */
    .alert {
        padding: 0.75rem 1rem;
        border-radius: 0.75rem;
        margin-bottom: 1rem;
        font-size: 0.9rem;
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

    /* Grid Layout */
    .content-grid {
        display: grid;
        grid-template-columns: 1fr 280px;
        gap: 1.5rem;
        align-items: start;
    }

    /* Cards */
    .card {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 1rem;
        padding: 1.25rem;
        margin-bottom: 1rem;
    }

    .card h2 {
        margin: 0 0 0.75rem;
        font-size: 1rem;
        color: #2d2141;
    }

    .card h3 {
        margin: 0 0 0.75rem;
        font-size: 0.95rem;
        color: #2d2141;
    }

    .card-success {
        background: #f0fdf4;
        border-color: #bbf7d0;
    }

    .card-feedback {
        background: #faf5ff;
        border-color: #e9d5ff;
    }

    .card-waiting {
        background: #fffbeb;
        border-color: #fcd34d;
    }

    .card-waiting p {
        margin: 0;
        color: #92400e;
    }

    .description {
        margin: 0;
        color: #4b5563;
        line-height: 1.6;
        white-space: pre-wrap;
    }

    .meta-info {
        margin: 0 0 1rem;
        color: #166534;
        font-size: 0.9rem;
    }

    .content-box {
        background: #fff;
        border: 1px solid #d1fae5;
        border-radius: 0.5rem;
        padding: 0.75rem;
        margin-bottom: 1rem;
    }

    .content-box strong {
        display: block;
        font-size: 0.8rem;
        color: #166534;
        margin-bottom: 0.25rem;
    }

    .content-box p {
        margin: 0;
        color: #374151;
    }

    /* Feedback */
    .feedback-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 0.75rem;
    }

    .feedback-header h2 {
        margin: 0;
    }

    .grade-badge {
        background: #7c3aed;
        color: white;
        padding: 0.25rem 0.6rem;
        border-radius: 1rem;
        font-size: 0.8rem;
        font-weight: 600;
    }

    .feedback-text {
        margin: 0;
        color: #4c1d95;
        line-height: 1.6;
    }

    /* Form */
    .info-text {
        margin: 0 0 1rem;
        color: #6b647a;
        font-size: 0.9rem;
    }

    .form-group {
        margin-bottom: 1rem;
    }

    .form-group label {
        display: block;
        margin-bottom: 0.4rem;
        font-size: 0.9rem;
        color: #374151;
    }

    textarea, input[type="file"] {
        width: 100%;
        padding: 0.6rem;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        font-family: inherit;
        background: #faf8fc;
    }

    textarea:focus {
        outline: none;
        border-color: #2F124D;
        background: #fff;
    }

    .char-count {
        display: block;
        font-size: 0.75rem;
        color: #9ca3af;
        margin-top: 0.25rem;
    }

    /* Buttons */
    .btn {
        display: inline-block;
        padding: 0.6rem 1.25rem;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        cursor: pointer;
        border: none;
        text-decoration: none;
        font-weight: 500;
    }

    .btn-primary {
        background: #2F124D;
        color: white;
    }

    .btn-primary:hover:not(:disabled) {
        background: #4A1C74;
    }

    .btn-primary:disabled {
        background: #9ca3af;
        cursor: not-allowed;
    }

    .btn-secondary {
        background: #f3f4f6;
        color: #374151;
        border: 1px solid #e5e7eb;
    }

    .btn-large {
        padding: 0.75rem 1.5rem;
        font-size: 1rem;
    }

    /* Sidebar */
    .sidebar .card {
        margin-bottom: 0;
    }

    .tips-list {
        margin: 0;
        padding-left: 1.25rem;
        font-size: 0.85rem;
        color: #4b5563;
        line-height: 1.6;
    }

    .tips-list li {
        margin-bottom: 0.4rem;
    }

    /* Empty State */
    .empty-state {
        text-align: center;
        padding: 4rem 2rem;
        background: #faf8fc;
        border: 2px dashed #e0d6eb;
        border-radius: 1rem;
    }

    .empty-icon {
        font-size: 3rem;
        display: block;
        margin-bottom: 1rem;
    }

    .empty-state h2 {
        margin: 0 0 0.5rem;
        color: #2d2141;
    }

    .empty-state p {
        margin: 0 0 1.5rem;
        color: #6b647a;
    }

    /* Responsive */
    @media (max-width: 768px) {
        .content-grid {
            grid-template-columns: 1fr;
        }

        .sidebar {
            order: -1;
        }
    }
</style>
