<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";

    let { data, form } = $props();

    // State
    let showModal = $state(false);
    let editingAssignment = $state(null);
    let deleteConfirmId = $state(null);
    let selectedType = $state("");

    // Filter & Search State
    let searchQuery = $state("");
    let filterClass = $state("all");
    let filterType = $state("all");
    let filterStatus = $state("all");
    let sortBy = $state("deadline");
    let visibleCount = $state(20);

    const assignmentTypes = [
        { value: "AI_INTERVIEW", label: "🤖 KI-Interview", hasDuration: true },
        { value: "DOCUMENT_UPLOAD", label: "📄 Dokument", hasDuration: false },
        { value: "SELF_REFLECTION", label: "✍️ Reflexion", hasDuration: false },
        { value: "VIDEO_PITCH", label: "🎥 Video", hasDuration: true },
        { value: "RESEARCH", label: "🔍 Recherche", hasDuration: false }
    ];

    // Derived
    let assignments = $derived(data?.assignments ?? []);
    let classes = $derived(data?.classes ?? []);

    let filteredAssignments = $derived(() => {
        let result = assignments;

        if (searchQuery.trim()) {
            const q = searchQuery.toLowerCase();
            result = result.filter(a => 
                a.title.toLowerCase().includes(q) ||
                a.className.toLowerCase().includes(q)
            );
        }

        if (filterClass !== "all") result = result.filter(a => a.classId === filterClass);
        if (filterType !== "all") result = result.filter(a => a.type === filterType);

        if (filterStatus === "open") result = result.filter(a => !a.isOverdue);
        else if (filterStatus === "closed") result = result.filter(a => a.isOverdue);
        else if (filterStatus === "needsFeedback") result = result.filter(a => a.pendingFeedback > 0);

        result = [...result].sort((a, b) => {
            if (sortBy === "deadline") return new Date(a.dueDate) - new Date(b.dueDate);
            if (sortBy === "title") return a.title.localeCompare(b.title);
            if (sortBy === "class") return a.className.localeCompare(b.className);
            return 0;
        });

        return result;
    });

    let visibleAssignments = $derived(filteredAssignments().slice(0, visibleCount));
    let hasMore = $derived(filteredAssignments().length > visibleCount);
    let needsFeedbackCount = $derived(assignments.filter(a => a.pendingFeedback > 0).length);
    let openCount = $derived(assignments.filter(a => !a.isOverdue).length);

    function loadMore() { visibleCount += 20; }
    function resetFilters() {
        searchQuery = "";
        filterClass = "all";
        filterType = "all";
        filterStatus = "all";
        visibleCount = 20;
    }

    function typeHasDuration(type) {
        return assignmentTypes.find(at => at.value === type)?.hasDuration ?? false;
    }

    function openNewModal() {
        editingAssignment = null;
        selectedType = "";
        showModal = true;
    }

    function openEditModal(assignment) {
        editingAssignment = assignment;
        selectedType = assignment.type || "";
        showModal = true;
    }

    function closeModal() {
        showModal = false;
        editingAssignment = null;
        selectedType = "";
    }

    function confirmDelete(id) { deleteConfirmId = id; }
    function cancelDelete() { deleteConfirmId = null; }

    function getTypeLabel(type) {
        return assignmentTypes.find(at => at.value === type)?.label ?? type;
    }

    function formatDate(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", year: "numeric"
        });
    }

    function formatDateTimeLocal(date) {
        if (!date) {
            const d = new Date();
            d.setDate(d.getDate() + 7);
            return d.toISOString().slice(0, 16);
        }
        return new Date(date).toISOString().slice(0, 16);
    }

    function getDeadlineText(assignment) {
        if (assignment.isOverdue) return "Beendet";
        const days = Math.ceil((new Date(assignment.dueDate) - new Date()) / 86400000);
        if (days === 0) return "Heute";
        if (days === 1) return "Morgen";
        if (days <= 7) return `${days}d`;
        return formatDate(assignment.dueDate);
    }

    function getDeadlineClass(assignment) {
        if (assignment.isOverdue) return "text-muted";
        const days = Math.ceil((new Date(assignment.dueDate) - new Date()) / 86400000);
        if (days <= 1) return "text-danger";
        if (days <= 3) return "text-warning";
        return "";
    }
</script>

<svelte:head>
    <title>Aufgaben – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <h1 class="title">📚 Aufgaben</h1>
        <button type="button" class="btn btn-primary" onclick={openNewModal}>
            ➕ Neue Aufgabe
        </button>
    </header>

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}
    {#if form?.success}
        <div class="alert alert-success">✓ Erfolgreich</div>
    {/if}

    <!-- Quick Filter Pills -->
    <div class="quick-filters">
        <button class="pill" class:active={filterStatus === "all"} onclick={() => { filterStatus = "all"; visibleCount = 20; }}>
            Alle ({assignments.length})
        </button>
        <button class="pill" class:active={filterStatus === "open"} onclick={() => { filterStatus = "open"; visibleCount = 20; }}>
            🟢 Offen ({openCount})
        </button>
        {#if needsFeedbackCount > 0}
            <button class="pill pill-alert" class:active={filterStatus === "needsFeedback"} onclick={() => { filterStatus = "needsFeedback"; visibleCount = 20; }}>
                🔔 Feedback nötig ({needsFeedbackCount})
            </button>
        {/if}
        <button class="pill" class:active={filterStatus === "closed"} onclick={() => { filterStatus = "closed"; visibleCount = 20; }}>
            ✓ Beendet
        </button>
    </div>

    <!-- Search & Filters -->
    <div class="filter-bar">
        <div class="filter-group search-wrapper">
            <span class="search-icon">🔍</span>
            <input type="text" class="filter-input" placeholder="Aufgabe suchen..." bind:value={searchQuery} />
            {#if searchQuery}
                <button class="clear-btn" onclick={() => searchQuery = ""}>✕</button>
            {/if}
        </div>

        <select bind:value={filterClass} class="filter-select">
            <option value="all">Alle Klassen</option>
            {#each classes as c}
                <option value={c.id}>{c.name}</option>
            {/each}
        </select>

        <select bind:value={filterType} class="filter-select">
            <option value="all">Alle Typen</option>
            {#each assignmentTypes as t}
                <option value={t.value}>{t.label}</option>
            {/each}
        </select>

        <select bind:value={sortBy} class="filter-select">
            <option value="deadline">Nach Deadline</option>
            <option value="title">Nach Titel</option>
            <option value="class">Nach Klasse</option>
        </select>
    </div>

    {#if searchQuery || filterClass !== "all" || filterType !== "all"}
        <div class="results-info">
            <span>{filteredAssignments().length} Ergebnisse</span>
            <button class="btn-link" onclick={resetFilters}>Filter zurücksetzen</button>
        </div>
    {/if}

    {#if assignments.length === 0}
        <div class="empty-state">
            <div class="empty-icon">📚</div>
            <h2>Noch keine Aufgaben</h2>
            <p>Erstelle deine erste Aufgabe.</p>
            <button type="button" class="btn btn-primary" onclick={openNewModal}>Aufgabe erstellen</button>
        </div>
    {:else if filteredAssignments().length === 0}
        <div class="empty-state small">
            <p>Keine Aufgaben gefunden.</p>
            <button class="btn-link" onclick={resetFilters}>Filter zurücksetzen</button>
        </div>
    {:else}
        <div class="table-wrapper">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Aufgabe</th>
                        <th>Klasse</th>
                        <th>Deadline</th>
                        <th>Abgaben</th>
                        <th>Status</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    {#each visibleAssignments as assignment}
                        {@const allSubmitted = assignment.submissionCount === assignment.totalStudents && assignment.totalStudents > 0}
                        {@const allReviewed = assignment.reviewedCount === assignment.submissionCount && assignment.submissionCount > 0}
                        <tr class:row-muted={assignment.isOverdue && allReviewed}>
                            <td>
                                <a href="/teacher/assignments/{assignment.id}" class="assignment-link">{assignment.title}</a>
                                <div class="assignment-meta-row">
                                    <span class="type-tag">{getTypeLabel(assignment.type)}</span>
                                    {#if assignment.durationMin}
                                        <span class="duration-tag">⏱️ {assignment.durationMin} Min</span>
                                    {/if}
                                </div>
                            </td>
                            <td>
                                <span class="class-name">{assignment.className}</span>
                                <span class="student-count">{assignment.totalStudents} Schüler</span>
                            </td>
                            <td>
                                <span class="deadline-text {getDeadlineClass(assignment)}">{getDeadlineText(assignment)}</span>
                                <span class="deadline-date">{formatDate(assignment.dueDate)}</span>
                            </td>
                            <td>
                                <div class="submission-stats">
                                    <span class="submission-count">{assignment.submissionCount} / {assignment.totalStudents}</span>
                                    <span class="submission-label">abgegeben</span>
                                </div>
                                <div class="review-stats">
                                    {#if assignment.submissionCount === 0}
                                        <span class="stat-muted">Noch keine Abgaben</span>
                                    {:else if assignment.reviewedCount === assignment.submissionCount}
                                        <span class="stat-success">✓ Alle bewertet</span>
                                    {:else if assignment.reviewedCount > 0}
                                        <span class="stat-success">✓ {assignment.reviewedCount} bewertet</span>
                                        <span class="stat-warning">· {assignment.pendingFeedback} offen</span>
                                    {:else}
                                        <span class="stat-warning">{assignment.pendingFeedback} warten auf Feedback</span>
                                    {/if}
                                </div>
                            </td>
                            <td>
                                {#if assignment.pendingFeedback > 0}
                                    <span class="status-badge status-action">🔔 Feedback nötig</span>
                                {:else if allSubmitted && allReviewed}
                                    <span class="status-badge status-done">✅ Abgeschlossen</span>
                                {:else if assignment.isOverdue}
                                    <span class="status-badge status-closed">⏹️ Beendet</span>
                                {:else if assignment.submissionCount > 0}
                                    <span class="status-badge status-progress">📥 In Bearbeitung</span>
                                {:else}
                                    <span class="status-badge status-waiting">⏳ Wartet</span>
                                {/if}
                            </td>
                            <td>
                                <div class="list-item-actions">
                                    <a href="/teacher/assignments/{assignment.id}" class="btn-icon" title="Ansehen">👁️</a>
                                    <button type="button" class="btn-icon" onclick={() => openEditModal(assignment)} title="Bearbeiten">✏️</button>
                                    <button type="button" class="btn-icon btn-danger" onclick={() => confirmDelete(assignment.id)} title="Löschen">🗑️</button>
                                </div>
                            </td>
                        </tr>
                        {#if deleteConfirmId === assignment.id}
                            <tr>
                                <td colspan="6">
                                    <div class="delete-confirm">
                                        <span class="delete-confirm-text">Aufgabe <strong>{assignment.title}</strong> löschen?</span>
                                        <div class="delete-actions">
                                            <form method="POST" action="?/delete" use:enhance={() => {
                                                return async ({ result }) => {
                                                    deleteConfirmId = null;
                                                    if (result.type === 'success') await invalidateAll();
                                                };
                                            }}>
                                                <input type="hidden" name="assignmentId" value={assignment.id} />
                                                <button type="submit" class="btn btn-danger">Ja, löschen</button>
                                            </form>
                                            <button type="button" class="btn btn-secondary" onclick={cancelDelete}>Abbrechen</button>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        {/if}
                    {/each}
                </tbody>
            </table>
        </div>

        {#if hasMore}
            <div class="load-more">
                <button type="button" class="btn btn-secondary" onclick={loadMore}>Mehr laden</button>
            </div>
        {/if}
    {/if}
</div>

<!-- Modal -->
{#if showModal}
    <button type="button" class="modal-backdrop" onclick={closeModal} aria-label="Modal schliessen"></button>
    <div class="modal" role="dialog" aria-modal="true">
        <div class="modal-header">
            <h2>{editingAssignment ? "Aufgabe bearbeiten" : "Neue Aufgabe"}</h2>
            <button type="button" class="btn-close-modal" onclick={closeModal} aria-label="Schliessen">✕</button>
        </div>

        <form method="POST" action={editingAssignment ? "?/update" : "?/create"}
            use:enhance={() => {
                return async ({ result }) => {
                    if (result.type === 'success') {
                        closeModal();
                        await invalidateAll();
                    }
                };
            }}>
            <div class="modal-body">
                {#if editingAssignment}
                    <input type="hidden" name="assignmentId" value={editingAssignment.id} />
                {/if}

                <div class="form-group">
                    <label for="title">Titel *</label>
                    <input type="text" id="title" name="title" required
                        placeholder="z.B. Bewerbungsgespräch üben" value={editingAssignment?.title ?? ""} />
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="classId">Klasse *</label>
                        <select id="classId" name="classId" required>
                            <option value="">Wählen...</option>
                            {#each classes as c}
                                <option value={c.id} selected={editingAssignment?.classId === c.id}>{c.name}</option>
                            {/each}
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="type">Typ *</label>
                        <select id="type" name="type" required bind:value={selectedType}>
                            <option value="">Wählen...</option>
                            {#each assignmentTypes as t}
                                <option value={t.value} selected={editingAssignment?.type === t.value}>{t.label}</option>
                            {/each}
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="dueDate">Deadline *</label>
                        <input type="datetime-local" id="dueDate" name="dueDate" required
                            value={formatDateTimeLocal(editingAssignment?.dueDate)} />
                    </div>
                    {#if typeHasDuration(selectedType)}
                        <div class="form-group">
                            <label for="durationMin">Dauer (Min)</label>
                            <input type="number" id="durationMin" name="durationMin" min="1" max="120"
                                value={editingAssignment?.durationMin ?? 15} />
                        </div>
                    {/if}
                </div>

                <div class="form-group">
                    <label for="description">Beschreibung</label>
                    <textarea id="description" name="description" rows="3"
                        placeholder="Optional: Weitere Details...">{editingAssignment?.description ?? ""}</textarea>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>Abbrechen</button>
                <button type="submit" class="btn btn-primary">{editingAssignment ? "Speichern" : "Erstellen"}</button>
            </div>
        </form>
    </div>
{/if}

<style>
    /* ===== PAGE SPECIFIC STYLES ===== */
    .quick-filters {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-sm);
        margin-bottom: var(--space-lg);
    }

    .pill {
        padding: var(--space-sm) var(--space-md);
        border-radius: var(--radius-full);
        border: 1px solid var(--color-border);
        background: var(--color-bg-card);
        font-size: var(--font-size-sm);
        cursor: pointer;
        transition: all var(--transition-base);
    }

    .pill:hover {
        border-color: var(--color-primary);
    }

    .pill.active {
        background: var(--color-primary);
        color: #fff;
        border-color: var(--color-primary);
    }

    .pill-alert {
        border-color: var(--color-error-border);
        color: var(--color-error);
    }

    .pill-alert.active {
        background: var(--color-error);
        color: #fff;
    }

    .search-wrapper {
        position: relative;
        flex: 1;
        min-width: 200px;
    }

    .search-icon {
        position: absolute;
        left: var(--space-md);
        top: 50%;
        transform: translateY(-50%);
        pointer-events: none;
    }

    .search-wrapper .filter-input {
        padding-left: 2.5rem;
    }

    .clear-btn {
        position: absolute;
        right: var(--space-sm);
        top: 50%;
        transform: translateY(-50%);
        background: none;
        border: none;
        cursor: pointer;
        color: var(--color-text-muted);
    }

    .results-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: var(--space-lg);
        font-size: var(--font-size-sm);
        color: var(--color-text-muted);
    }

    .assignment-link {
        font-weight: 500;
        color: var(--color-text-secondary);
        text-decoration: none;
    }

    .assignment-link:hover {
        color: var(--color-primary);
        text-decoration: underline;
    }

    .assignment-meta-row {
        display: flex;
        gap: 0.5rem;
        margin-top: 0.25rem;
    }

    .type-tag {
        font-size: var(--font-size-xs);
        color: var(--color-text-muted);
        background: #f3f4f6;
        padding: 2px 6px;
        border-radius: var(--radius-sm);
    }

    .duration-tag {
        font-size: var(--font-size-xs);
        color: #6b7280;
    }

    .class-name {
        display: block;
        font-weight: 500;
        color: var(--color-text-secondary);
    }

    .student-count {
        display: block;
        font-size: var(--font-size-xs);
        color: var(--color-text-muted);
    }

    .deadline-text {
        display: block;
        font-weight: 600;
    }

    .deadline-date {
        display: block;
        font-size: var(--font-size-xs);
        color: var(--color-text-muted);
    }

    /* Submission Stats */
    .submission-stats {
        display: flex;
        align-items: baseline;
        gap: 0.35rem;
    }

    .submission-count {
        font-size: 1.1rem;
        font-weight: 700;
        color: var(--color-text-secondary);
    }

    .submission-label {
        font-size: var(--font-size-xs);
        color: var(--color-text-muted);
    }

    .review-stats {
        margin-top: 0.2rem;
        font-size: var(--font-size-xs);
    }

    .stat-muted {
        color: #9ca3af;
    }

    .stat-success {
        color: #059669;
    }

    .stat-warning {
        color: #d97706;
        font-weight: 500;
    }

    /* Status Badges */
    .status-badge {
        display: inline-block;
        padding: 0.25rem 0.5rem;
        border-radius: var(--radius-md);
        font-size: var(--font-size-xs);
        font-weight: 500;
        white-space: nowrap;
    }

    .status-action {
        background: #fef3c7;
        color: #92400e;
    }

    .status-done {
        background: #d1fae5;
        color: #065f46;
    }

    .status-closed {
        background: #f3f4f6;
        color: #6b7280;
    }

    .status-progress {
        background: #dbeafe;
        color: #1e40af;
    }

    .status-waiting {
        background: #f3e8ff;
        color: #6b21a8;
    }

    .row-muted {
        opacity: 0.6;
    }

    .load-more {
        text-align: center;
        padding: var(--space-lg);
    }

    @media (max-width: 900px) {
        .data-table th:nth-child(5),
        .data-table td:nth-child(5) {
            display: none;
        }
    }

    @media (max-width: 768px) {
        .data-table th:nth-child(4),
        .data-table td:nth-child(4),
        .data-table th:nth-child(5),
        .data-table td:nth-child(5) {
            display: none;
        }

        .assignment-meta-row {
            flex-wrap: wrap;
        }
    }
</style>
