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
    let filterStatus = $state("all"); // all, open, closed, needsFeedback
    let sortBy = $state("deadline"); // deadline, title, class
    let showClosed = $state(false);
    let visibleCount = $state(20); // Pagination

    // Aufgabentypen
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

    // Gefilterte & sortierte Aufgaben
    let filteredAssignments = $derived(() => {
        let result = assignments;

        // Suche
        if (searchQuery.trim()) {
            const q = searchQuery.toLowerCase();
            result = result.filter(a => 
                a.title.toLowerCase().includes(q) ||
                a.className.toLowerCase().includes(q)
            );
        }

        // Filter: Klasse
        if (filterClass !== "all") {
            result = result.filter(a => a.classId === filterClass);
        }

        // Filter: Typ
        if (filterType !== "all") {
            result = result.filter(a => a.type === filterType);
        }

        // Filter: Status
        if (filterStatus === "open") {
            result = result.filter(a => !a.isOverdue);
        } else if (filterStatus === "closed") {
            result = result.filter(a => a.isOverdue);
        } else if (filterStatus === "needsFeedback") {
            result = result.filter(a => a.pendingFeedback > 0);
        }

        // Sortierung
        result = [...result].sort((a, b) => {
            if (sortBy === "deadline") {
                return new Date(a.dueDate) - new Date(b.dueDate);
            } else if (sortBy === "title") {
                return a.title.localeCompare(b.title);
            } else if (sortBy === "class") {
                return a.className.localeCompare(b.className);
            }
            return 0;
        });

        return result;
    });

    // Sichtbare Aufgaben (Pagination)
    let visibleAssignments = $derived(filteredAssignments().slice(0, visibleCount));
    let hasMore = $derived(filteredAssignments().length > visibleCount);
    let totalFiltered = $derived(filteredAssignments().length);

    // Quick Stats
    let needsFeedbackCount = $derived(assignments.filter(a => a.pendingFeedback > 0).length);
    let openCount = $derived(assignments.filter(a => !a.isOverdue).length);

    function loadMore() {
        visibleCount += 20;
    }

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

    function confirmDelete(id) {
        deleteConfirmId = id;
    }

    function cancelDelete() {
        deleteConfirmId = null;
    }

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
        if (assignment.isOverdue) return "closed";
        const days = Math.ceil((new Date(assignment.dueDate) - new Date()) / 86400000);
        if (days <= 1) return "urgent";
        if (days <= 3) return "soon";
        return "";
    }
</script>

<svelte:head>
    <title>Aufgaben – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <!-- Header -->
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
        <button 
            class="pill" 
            class:active={filterStatus === "all"} 
            onclick={() => { filterStatus = "all"; visibleCount = 20; }}
        >
            Alle ({assignments.length})
        </button>
        <button 
            class="pill" 
            class:active={filterStatus === "open"} 
            onclick={() => { filterStatus = "open"; visibleCount = 20; }}
        >
            🟢 Offen ({openCount})
        </button>
        {#if needsFeedbackCount > 0}
            <button 
                class="pill pill-alert" 
                class:active={filterStatus === "needsFeedback"} 
                onclick={() => { filterStatus = "needsFeedback"; visibleCount = 20; }}
            >
                🔔 Feedback nötig ({needsFeedbackCount})
            </button>
        {/if}
        <button 
            class="pill" 
            class:active={filterStatus === "closed"} 
            onclick={() => { filterStatus = "closed"; visibleCount = 20; }}
        >
            ✓ Beendet
        </button>
    </div>

    <!-- Search & Filters -->
    <div class="search-bar">
        <div class="search-input-wrapper">
            <span class="search-icon">🔍</span>
            <input 
                type="text" 
                class="search-input" 
                placeholder="Aufgabe suchen..." 
                bind:value={searchQuery}
            />
            {#if searchQuery}
                <button class="clear-btn" onclick={() => searchQuery = ""}>✕</button>
            {/if}
        </div>

        <div class="filters">
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
    </div>

    <!-- Results Info -->
    {#if searchQuery || filterClass !== "all" || filterType !== "all"}
        <div class="results-info">
            <span>{totalFiltered} Ergebnisse</span>
            <button class="link-btn" onclick={resetFilters}>Filter zurücksetzen</button>
        </div>
    {/if}

    <!-- Assignments List -->
    {#if assignments.length === 0}
        <div class="empty-state">
            <div class="empty-icon">📚</div>
            <h2>Noch keine Aufgaben</h2>
            <p>Erstelle deine erste Aufgabe.</p>
            <button type="button" class="btn btn-primary" onclick={openNewModal}>
                Aufgabe erstellen
            </button>
        </div>
    {:else if filteredAssignments().length === 0}
        <div class="empty-state small">
            <p>Keine Aufgaben gefunden.</p>
            <button class="link-btn" onclick={resetFilters}>Filter zurücksetzen</button>
        </div>
    {:else}
        <div class="assignments-table">
            <div class="table-header">
                <span class="col-title">Aufgabe</span>
                <span class="col-class">Klasse</span>
                <span class="col-deadline">Deadline</span>
                <span class="col-progress">Abgaben</span>
                <span class="col-actions"></span>
            </div>

            {#each visibleAssignments as assignment}
                <div class="table-row" class:closed={assignment.isOverdue}>
                    <div class="col-title">
                        <a href="/teacher/assignments/{assignment.id}" class="assignment-link">
                            {assignment.title}
                        </a>
                        <span class="type-tag">{getTypeLabel(assignment.type)}</span>
                        {#if assignment.pendingFeedback > 0}
                            <span class="badge-alert">{assignment.pendingFeedback} neu</span>
                        {/if}
                    </div>
                    <div class="col-class">{assignment.className}</div>
                    <div class="col-deadline {getDeadlineClass(assignment)}">
                        {getDeadlineText(assignment)}
                    </div>
                    <div class="col-progress">
                        <span class="progress-text">{assignment.submissionCount}/{assignment.totalStudents}</span>
                        {#if assignment.reviewedCount > 0}
                            <span class="reviewed-text">✓{assignment.reviewedCount}</span>
                        {/if}
                    </div>
                    <div class="col-actions">
                        <a href="/teacher/assignments/{assignment.id}" class="action-btn" title="Abgaben ansehen">📥</a>
                        {#if !assignment.isOverdue}
                            <button type="button" class="action-btn" onclick={() => openEditModal(assignment)} title="Bearbeiten">✏️</button>
                            <button type="button" class="action-btn danger" onclick={() => confirmDelete(assignment.id)} title="Löschen">🗑️</button>
                        {/if}
                    </div>

                    {#if deleteConfirmId === assignment.id}
                        <div class="delete-overlay">
                            <span>Löschen?</span>
                            <form method="POST" action="?/delete" use:enhance={() => {
                                return async ({ result }) => {
                                    deleteConfirmId = null;
                                    if (result.type === 'success') await invalidateAll();
                                };
                            }}>
                                <input type="hidden" name="assignmentId" value={assignment.id} />
                                <button type="submit" class="btn btn-danger btn-sm">Ja</button>
                            </form>
                            <button type="button" class="btn btn-secondary btn-sm" onclick={cancelDelete}>Nein</button>
                        </div>
                    {/if}
                </div>
            {/each}
        </div>

        {#if hasMore}
            <div class="load-more">
                <button class="btn btn-secondary" onclick={loadMore}>
                    Mehr laden ({totalFiltered - visibleCount} weitere)
                </button>
            </div>
        {/if}
    {/if}
</div>

<!-- Modal -->
{#if showModal}
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <div class="modal-backdrop" onclick={closeModal}></div>
    <div class="modal">
        <div class="modal-header">
            <h2>{editingAssignment ? "Bearbeiten" : "Neue Aufgabe"}</h2>
            <button type="button" class="close-btn" onclick={closeModal}>✕</button>
        </div>
        <form 
            method="POST" 
            action={editingAssignment ? "?/update" : "?/create"}
            class="modal-form"
            use:enhance={() => {
                return async ({ result }) => {
                    if (result.type === 'success') {
                        closeModal();
                        await invalidateAll();
                    }
                };
            }}
        >
            {#if editingAssignment}
                <input type="hidden" name="assignmentId" value={editingAssignment.id} />
            {/if}

            <label>
                <span>Titel *</span>
                <input type="text" name="title" value={editingAssignment?.title ?? ""} required />
            </label>

            <div class="form-row">
                <label>
                    <span>Typ *</span>
                    <select name="type" required bind:value={selectedType}>
                        <option value="">Wählen...</option>
                        {#each assignmentTypes as t}
                            <option value={t.value}>{t.label}</option>
                        {/each}
                    </select>
                </label>
                <label>
                    <span>Klasse *</span>
                    <select name="classId" required>
                        <option value="">Wählen...</option>
                        {#each classes as c}
                            <option value={c.id} selected={editingAssignment?.classId === c.id}>{c.name}</option>
                        {/each}
                    </select>
                </label>
            </div>

            <div class="form-row">
                <label>
                    <span>Deadline *</span>
                    <input type="datetime-local" name="dueDate" required value={formatDateTimeLocal(editingAssignment?.dueDate)} />
                </label>
                {#if typeHasDuration(selectedType)}
                    <label>
                        <span>Dauer (Min)</span>
                        <input type="number" name="durationMin" min="1" max="120" value={editingAssignment?.durationMin ?? ""} />
                    </label>
                {/if}
            </div>

            <label>
                <span>Beschreibung</span>
                <textarea name="description" rows="2">{editingAssignment?.description ?? ""}</textarea>
            </label>

            <div class="form-actions">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>Abbrechen</button>
                <button type="submit" class="btn btn-primary">{editingAssignment ? "Speichern" : "Erstellen"}</button>
            </div>
        </form>
    </div>
{/if}

<style>
    .page-wrapper {
        max-width: 1100px;
        margin: 0 auto;
        padding: 1.5rem 1rem 3rem;
    }

    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
    }

    .title {
        font-size: 1.5rem;
        font-weight: 700;
        margin: 0;
        color: #2d2141;
    }

    /* Quick Filters */
    .quick-filters {
        display: flex;
        gap: 0.5rem;
        margin-bottom: 1rem;
        flex-wrap: wrap;
    }

    .pill {
        padding: 0.4rem 0.85rem;
        border: 1px solid #e8e0f0;
        border-radius: 2rem;
        background: #fff;
        font-size: 0.8rem;
        cursor: pointer;
        color: #6b7280;
        transition: all 0.15s;
    }

    .pill:hover {
        border-color: #3b134f;
        color: #3b134f;
    }

    .pill.active {
        background: #3b134f;
        color: #fff;
        border-color: #3b134f;
    }

    .pill-alert {
        border-color: #fca5a5;
        color: #dc2626;
        background: #fef2f2;
    }

    .pill-alert.active {
        background: #dc2626;
        color: #fff;
        border-color: #dc2626;
    }

    /* Search Bar */
    .search-bar {
        display: flex;
        gap: 0.75rem;
        margin-bottom: 1rem;
        flex-wrap: wrap;
    }

    .search-input-wrapper {
        flex: 1;
        min-width: 200px;
        position: relative;
    }

    .search-icon {
        position: absolute;
        left: 0.75rem;
        top: 50%;
        transform: translateY(-50%);
        font-size: 0.9rem;
        opacity: 0.5;
    }

    .search-input {
        width: 100%;
        padding: 0.6rem 2rem 0.6rem 2.25rem;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        box-sizing: border-box;
    }

    .search-input:focus {
        outline: none;
        border-color: #3b134f;
    }

    .clear-btn {
        position: absolute;
        right: 0.5rem;
        top: 50%;
        transform: translateY(-50%);
        background: none;
        border: none;
        cursor: pointer;
        color: #9ca3af;
        font-size: 0.8rem;
    }

    .filters {
        display: flex;
        gap: 0.5rem;
        flex-wrap: wrap;
    }

    .filter-select {
        padding: 0.6rem 0.75rem;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        font-size: 0.85rem;
        background: #fff;
        cursor: pointer;
    }

    .results-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.5rem 0;
        font-size: 0.85rem;
        color: #6b7280;
        border-bottom: 1px solid #e8e0f0;
        margin-bottom: 0.5rem;
    }

    .link-btn {
        background: none;
        border: none;
        color: #7c3aed;
        cursor: pointer;
        font-size: 0.85rem;
    }

    /* Table Layout */
    .assignments-table {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        overflow: hidden;
    }

    .table-header {
        display: grid;
        grid-template-columns: 1fr 120px 90px 80px 90px;
        gap: 0.5rem;
        padding: 0.75rem 1rem;
        background: #f9fafb;
        border-bottom: 1px solid #e8e0f0;
        font-size: 0.75rem;
        font-weight: 600;
        color: #6b7280;
        text-transform: uppercase;
    }

    .table-row {
        display: grid;
        grid-template-columns: 1fr 120px 90px 80px 90px;
        gap: 0.5rem;
        padding: 0.75rem 1rem;
        border-bottom: 1px solid #f3f4f6;
        align-items: center;
        position: relative;
        font-size: 0.9rem;
    }

    .table-row:last-child {
        border-bottom: none;
    }

    .table-row:hover {
        background: #faf8fc;
    }

    .table-row.closed {
        background: #f9fafb;
        opacity: 0.7;
    }

    .col-title {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        flex-wrap: wrap;
        min-width: 0;
    }

    .assignment-link {
        color: #2d2141;
        text-decoration: none;
        font-weight: 500;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .assignment-link:hover {
        color: #7c3aed;
    }

    .type-tag {
        font-size: 0.7rem;
        color: #6b7280;
        background: #f3f4f6;
        padding: 0.15rem 0.4rem;
        border-radius: 0.25rem;
        white-space: nowrap;
    }

    .badge-alert {
        font-size: 0.65rem;
        color: #dc2626;
        background: #fee2e2;
        padding: 0.15rem 0.4rem;
        border-radius: 1rem;
        font-weight: 600;
    }

    .col-class {
        color: #6b7280;
        font-size: 0.85rem;
    }

    .col-deadline {
        font-size: 0.85rem;
        color: #6b7280;
    }

    .col-deadline.urgent {
        color: #dc2626;
        font-weight: 600;
    }

    .col-deadline.soon {
        color: #f59e0b;
    }

    .col-deadline.closed {
        color: #9ca3af;
    }

    .col-progress {
        display: flex;
        gap: 0.35rem;
        font-size: 0.85rem;
    }

    .progress-text {
        color: #374151;
    }

    .reviewed-text {
        color: #059669;
        font-size: 0.75rem;
    }

    .col-actions {
        display: flex;
        gap: 0.25rem;
        justify-content: flex-end;
    }

    .action-btn {
        background: none;
        border: none;
        padding: 0.3rem;
        cursor: pointer;
        border-radius: 0.25rem;
        font-size: 0.85rem;
        text-decoration: none;
        opacity: 0.6;
    }

    .action-btn:hover {
        background: #f3f4f6;
        opacity: 1;
    }

    .action-btn.danger:hover {
        background: #fee2e2;
    }

    .delete-overlay {
        position: absolute;
        inset: 0;
        background: rgba(254, 242, 242, 0.95);
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.75rem;
        font-size: 0.85rem;
    }

    /* Load More */
    .load-more {
        text-align: center;
        padding: 1rem;
    }

    /* Empty State */
    .empty-state {
        text-align: center;
        padding: 3rem 2rem;
        background: #faf8fc;
        border: 2px dashed #e8e0f0;
        border-radius: 1rem;
    }

    .empty-state.small {
        padding: 2rem;
    }

    .empty-icon {
        font-size: 2.5rem;
        margin-bottom: 0.5rem;
    }

    .empty-state h2 {
        margin: 0 0 0.25rem;
        font-size: 1.1rem;
        color: #2d2141;
    }

    .empty-state p {
        margin: 0 0 1rem;
        color: #6b7280;
        font-size: 0.9rem;
    }

    /* Buttons */
    .btn {
        padding: 0.5rem 1rem;
        border-radius: 0.5rem;
        font-size: 0.85rem;
        font-weight: 500;
        border: none;
        cursor: pointer;
        text-decoration: none;
    }

    .btn-sm {
        padding: 0.3rem 0.6rem;
        font-size: 0.8rem;
    }

    .btn-primary {
        background: #3b134f;
        color: #fff;
    }

    .btn-secondary {
        background: #f3f4f6;
        color: #374151;
    }

    .btn-danger {
        background: #dc2626;
        color: #fff;
    }

    /* Alerts */
    .alert {
        padding: 0.6rem 1rem;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
        font-size: 0.9rem;
    }

    .alert-danger {
        background: #fef2f2;
        color: #dc2626;
    }

    .alert-success {
        background: #f0fdf4;
        color: #16a34a;
    }

    /* Modal */
    .modal-backdrop {
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.5);
        z-index: 9998;
    }

    .modal {
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: #fff;
        border-radius: 0.75rem;
        width: 90%;
        max-width: 500px;
        max-height: 90vh;
        overflow: hidden;
        z-index: 9999;
        box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1rem 1.25rem;
        background: #3b134f;
        color: #fff;
    }

    .modal-header h2 {
        margin: 0;
        font-size: 1rem;
    }

    .close-btn {
        background: rgba(255,255,255,0.2);
        border: none;
        color: #fff;
        padding: 0.3rem 0.5rem;
        border-radius: 0.25rem;
        cursor: pointer;
    }

    .modal-form {
        padding: 1.25rem;
        overflow-y: auto;
        max-height: calc(90vh - 60px);
    }

    .modal-form label {
        display: block;
        margin-bottom: 0.75rem;
    }

    .modal-form label span {
        display: block;
        font-size: 0.8rem;
        font-weight: 600;
        color: #374151;
        margin-bottom: 0.3rem;
    }

    .modal-form input,
    .modal-form select,
    .modal-form textarea {
        width: 100%;
        padding: 0.6rem;
        border: 1px solid #e5e7eb;
        border-radius: 0.4rem;
        font-size: 0.9rem;
        font-family: inherit;
        box-sizing: border-box;
    }

    .form-row {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 0.75rem;
    }

    .form-actions {
        display: flex;
        justify-content: flex-end;
        gap: 0.5rem;
        margin-top: 1rem;
        padding-top: 1rem;
        border-top: 1px solid #e5e7eb;
    }

    /* Responsive */
    @media (max-width: 768px) {
        .table-header {
            display: none;
        }

        .table-row {
            grid-template-columns: 1fr;
            gap: 0.25rem;
            padding: 0.75rem 1rem;
        }

        .col-title {
            order: 1;
        }

        .col-class, .col-deadline, .col-progress {
            font-size: 0.8rem;
        }

        .col-actions {
            order: 0;
            position: absolute;
            top: 0.5rem;
            right: 0.5rem;
        }

        .form-row {
            grid-template-columns: 1fr;
        }
    }
</style>