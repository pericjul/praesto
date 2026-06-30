<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";
    import { get } from "svelte/store";

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

    let assignmentTypes = $derived([
        { value: "AI_INTERVIEW", label: $t('tasks.typeInterview'), hasDuration: true },
        { value: "DOCUMENT_UPLOAD", label: $t('tasks.typeDocument'), hasDuration: false },
        { value: "SELF_REFLECTION", label: $t('tasks.typeReflection'), hasDuration: false },
        { value: "VIDEO_PITCH", label: $t('tasks.typeVideo'), hasDuration: true },
        { value: "RESEARCH", label: $t('tasks.typeResearch'), hasDuration: false }
    ]);

    // Erklärt direkt im Modal, was ein Aufgabentyp bedeutet (Lehrpersonen-Hilfe).
    const typeDescKeys = {
        AI_INTERVIEW: "tasks.typeInterviewDesc",
        DOCUMENT_UPLOAD: "tasks.typeDocumentDesc",
        SELF_REFLECTION: "tasks.typeReflectionDesc",
        VIDEO_PITCH: "tasks.typeVideoDesc",
        RESEARCH: "tasks.typeResearchDesc"
    };

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
        if (assignment.isOverdue) return get(t)('tasks.deadlineEnded');
        const days = Math.ceil((new Date(assignment.dueDate) - new Date()) / 86400000);
        if (days === 0) return get(t)('tasks.deadlineToday');
        if (days === 1) return get(t)('tasks.deadlineTomorrow');
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
    <title>{$t('tasks.headTitle')}</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <h1 class="title">📚 {$t('tasks.title')}</h1>
        <button type="button" class="btn btn-primary" onclick={openNewModal}>
            ➕ {$t('tasks.newAssignment')}
        </button>
    </header>

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}
    {#if form?.success}
        <div class="alert alert-success">✓ {$t('tasks.success')}</div>
    {/if}

    <!-- Quick Filter Pills -->
    <div class="quick-filters">
        <button class="pill" class:active={filterStatus === "all"} onclick={() => { filterStatus = "all"; visibleCount = 20; }}>
            {$t('tasks.filterAll')} ({assignments.length})
        </button>
        <button class="pill" class:active={filterStatus === "open"} onclick={() => { filterStatus = "open"; visibleCount = 20; }}>
            🟢 {$t('tasks.filterOpen')} ({openCount})
        </button>
        {#if needsFeedbackCount > 0}
            <button class="pill pill-alert" class:active={filterStatus === "needsFeedback"} onclick={() => { filterStatus = "needsFeedback"; visibleCount = 20; }}>
                🔔 {$t('tasks.filterNeedsFeedback')} ({needsFeedbackCount})
            </button>
        {/if}
        <button class="pill" class:active={filterStatus === "closed"} onclick={() => { filterStatus = "closed"; visibleCount = 20; }}>
            ✓ {$t('tasks.filterClosed')}
        </button>
    </div>

    <!-- Search & Filters -->
    <div class="filter-bar">
        <div class="filter-group search-wrapper">
            <span class="search-icon">🔍</span>
            <input type="text" class="filter-input" placeholder={$t('tasks.searchPlaceholder')} bind:value={searchQuery} />
            {#if searchQuery}
                <button class="clear-btn" onclick={() => searchQuery = ""}>✕</button>
            {/if}
        </div>

        <select bind:value={filterClass} class="filter-select">
            <option value="all">{$t('tasks.allClasses')}</option>
            {#each classes as c}
                <option value={c.id}>{c.name}</option>
            {/each}
        </select>

        <select bind:value={filterType} class="filter-select">
            <option value="all">{$t('tasks.allTypes')}</option>
            {#each assignmentTypes as item}
                <option value={item.value}>{item.label}</option>
            {/each}
        </select>

        <select bind:value={sortBy} class="filter-select">
            <option value="deadline">{$t('tasks.sortDeadline')}</option>
            <option value="title">{$t('tasks.sortTitle')}</option>
            <option value="class">{$t('tasks.sortClass')}</option>
        </select>
    </div>

    {#if searchQuery || filterClass !== "all" || filterType !== "all"}
        <div class="results-info">
            <span>{filteredAssignments().length} {$t('tasks.results')}</span>
            <button class="btn-link" onclick={resetFilters}>{$t('tasks.resetFilters')}</button>
        </div>
    {/if}

    {#if assignments.length === 0}
        <div class="empty-state">
            <div class="empty-icon">📚</div>
            <h2>{$t('tasks.emptyTitle')}</h2>
            <p>{$t('tasks.emptyText')}</p>
            <button type="button" class="btn btn-primary" onclick={openNewModal}>{$t('tasks.createAssignment')}</button>
        </div>
    {:else if filteredAssignments().length === 0}
        <div class="empty-state small">
            <p>{$t('tasks.noResults')}</p>
            <button class="btn-link" onclick={resetFilters}>{$t('tasks.resetFilters')}</button>
        </div>
    {:else}
        <div class="table-wrapper">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>{$t('tasks.colAssignment')}</th>
                        <th>{$t('tasks.colClass')}</th>
                        <th>{$t('tasks.colDeadline')}</th>
                        <th>{$t('tasks.colSubmissions')}</th>
                        <th>{$t('tasks.colStatus')}</th>
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
                                        <span class="duration-tag">⏱️ {assignment.durationMin} {$t('tasks.min')}</span>
                                    {/if}
                                </div>
                            </td>
                            <td>
                                <span class="class-name">{assignment.className}</span>
                                <span class="student-count">{assignment.totalStudents} {$t('tasks.students')}</span>
                            </td>
                            <td>
                                <span class="deadline-text {getDeadlineClass(assignment)}">{getDeadlineText(assignment)}</span>
                                <span class="deadline-date">{formatDate(assignment.dueDate)}</span>
                            </td>
                            <td>
                                <div class="submission-stats">
                                    <span class="submission-count">{assignment.submissionCount} / {assignment.totalStudents}</span>
                                    <span class="submission-label">{$t('tasks.submitted')}</span>
                                </div>
                                <div class="review-stats">
                                    {#if assignment.submissionCount === 0}
                                        <span class="stat-muted">{$t('tasks.noSubmissionsYet')}</span>
                                    {:else if assignment.reviewedCount === assignment.submissionCount}
                                        <span class="stat-success">✓ {$t('tasks.allReviewed')}</span>
                                    {:else if assignment.reviewedCount > 0}
                                        <span class="stat-success">✓ {assignment.reviewedCount} {$t('tasks.reviewedSuffix')}</span>
                                        <span class="stat-warning">· {assignment.pendingFeedback} {$t('tasks.openSuffix')}</span>
                                    {:else}
                                        <span class="stat-warning">{assignment.pendingFeedback} {$t('tasks.waitingForFeedback')}</span>
                                    {/if}
                                </div>
                            </td>
                            <td>
                                {#if assignment.pendingFeedback > 0}
                                    <span class="status-badge status-action">🔔 {$t('tasks.statusNeedsFeedback')}</span>
                                {:else if allSubmitted && allReviewed}
                                    <span class="status-badge status-done">✅ {$t('tasks.statusDone')}</span>
                                {:else if assignment.isOverdue}
                                    <span class="status-badge status-closed">⏹️ {$t('tasks.statusClosed')}</span>
                                {:else if assignment.submissionCount > 0}
                                    <span class="status-badge status-progress">📥 {$t('tasks.statusInProgress')}</span>
                                {:else}
                                    <span class="status-badge status-waiting">⏳ {$t('tasks.statusWaiting')}</span>
                                {/if}
                            </td>
                            <td>
                                <div class="list-item-actions">
                                    <a href="/teacher/assignments/{assignment.id}" class="btn-icon" title={$t('tasks.view')}>👁️</a>
                                    <button type="button" class="btn-icon" onclick={() => openEditModal(assignment)} title={$t('tasks.edit')}>✏️</button>
                                    <button type="button" class="btn-icon btn-danger" onclick={() => confirmDelete(assignment.id)} title={$t('tasks.delete')}>🗑️</button>
                                </div>
                            </td>
                        </tr>
                        {#if deleteConfirmId === assignment.id}
                            <tr>
                                <td colspan="6">
                                    <div class="delete-confirm">
                                        <span class="delete-confirm-text">{$t('tasks.deletePrefix')} <strong>{assignment.title}</strong> {$t('tasks.deleteSuffix')}</span>
                                        <div class="delete-actions">
                                            <form method="POST" action="?/delete" use:enhance={() => {
                                                return async ({ result }) => {
                                                    deleteConfirmId = null;
                                                    if (result.type === 'success') await invalidateAll();
                                                };
                                            }}>
                                                <input type="hidden" name="assignmentId" value={assignment.id} />
                                                <button type="submit" class="btn btn-danger">{$t('tasks.confirmDelete')}</button>
                                            </form>
                                            <button type="button" class="btn btn-secondary" onclick={cancelDelete}>{$t('tasks.cancel')}</button>
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
                <button type="button" class="btn btn-secondary" onclick={loadMore}>{$t('tasks.loadMore')}</button>
            </div>
        {/if}
    {/if}
</div>

<!-- Modal -->
{#if showModal}
    <button type="button" class="modal-backdrop" onclick={closeModal} aria-label={$t('tasks.closeModal')}></button>
    <div class="modal" role="dialog" aria-modal="true">
        <div class="modal-header">
            <h2>{editingAssignment ? $t('tasks.editAssignment') : $t('tasks.newAssignment')}</h2>
            <button type="button" class="btn-close-modal" onclick={closeModal} aria-label={$t('tasks.close')}>✕</button>
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
                    <label for="title">{$t('tasks.fieldTitle')} *</label>
                    <input type="text" id="title" name="title" required
                        placeholder={$t('tasks.titlePlaceholder')} value={editingAssignment?.title ?? ""} />
                </div>

                {#if editingAssignment}
                    <div class="form-row">
                        <div class="form-group">
                            <label for="classId">{$t('tasks.fieldClass')} *</label>
                            <select id="classId" name="classId" required>
                                <option value="">{$t('tasks.choose')}</option>
                                {#each classes as c}
                                    <option value={c.id} selected={editingAssignment?.classId === c.id}>{c.name}</option>
                                {/each}
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="type">{$t('tasks.fieldType')} *</label>
                            <select id="type" name="type" required bind:value={selectedType}>
                                <option value="">{$t('tasks.choose')}</option>
                                {#each assignmentTypes as item}
                                    <option value={item.value} selected={editingAssignment?.type === item.value}>{item.label}</option>
                                {/each}
                            </select>
                        </div>
                    </div>
                {:else}
                    <div class="form-group">
                        <label>{$t('tasks.fieldClasses')} * <span class="hint-inline">{$t('tasks.multiSelectHint')}</span></label>
                        <div class="class-checkboxes">
                            {#each classes as c}
                                <label class="class-checkbox">
                                    <input type="checkbox" name="classIds" value={c.id} />
                                    <span>{c.name}</span>
                                </label>
                            {/each}
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="type">{$t('tasks.fieldType')} *</label>
                        <select id="type" name="type" required bind:value={selectedType}>
                            <option value="">{$t('tasks.choose')}</option>
                            {#each assignmentTypes as item}
                                <option value={item.value}>{item.label}</option>
                            {/each}
                        </select>
                        {#if selectedType && typeDescKeys[selectedType]}
                            <p class="type-desc">ℹ️ {$t(typeDescKeys[selectedType])}</p>
                        {/if}
                    </div>
                {/if}

                <div class="form-row">
                    <div class="form-group">
                        <label for="dueDate">{$t('tasks.fieldDeadline')} *</label>
                        <input type="datetime-local" id="dueDate" name="dueDate" required
                            value={formatDateTimeLocal(editingAssignment?.dueDate)} />
                    </div>
                    {#if typeHasDuration(selectedType)}
                        <div class="form-group">
                            <label for="durationMin">{$t('tasks.fieldDuration')}</label>
                            <input type="number" id="durationMin" name="durationMin" min="1"
                                max={selectedType === 'AI_INTERVIEW' ? 20 : 120}
                                value={Math.min(editingAssignment?.durationMin ?? 15, selectedType === 'AI_INTERVIEW' ? 20 : 120)} />
                            {#if selectedType === 'AI_INTERVIEW'}
                                <span class="hint-inline">{$t('tasks.durationMaxHint')}</span>
                            {/if}
                        </div>
                    {/if}
                </div>

                <div class="form-group">
                    <label for="description">{$t('tasks.fieldDescription')}</label>
                    <textarea id="description" name="description" rows="3"
                        placeholder={$t('tasks.descriptionPlaceholder')}>{editingAssignment?.description ?? ""}</textarea>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>{$t('tasks.cancel')}</button>
                <button type="submit" class="btn btn-primary">{editingAssignment ? $t('tasks.save') : $t('tasks.create')}</button>
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

    .hint-inline {
        font-weight: 400;
        font-size: 0.75rem;
        color: var(--color-text-light, #6b647a);
    }

    .class-checkboxes {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
        max-height: 220px;
        overflow-y: auto;
        padding: 0.15rem;
    }

    .class-checkbox {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        box-sizing: border-box;
        padding: 0.55rem 0.95rem;
        border: 1.5px solid #e8e0f0;
        border-radius: 999px;
        background: #faf8fc;
        font-size: 0.9rem;
        cursor: pointer;
        transition: all 0.15s;
        user-select: none;
        max-width: 100%;
    }

    .class-checkbox span {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .type-desc {
        margin: 0.5rem 0 0;
        padding: 0.6rem 0.8rem;
        background: #f3eefb;
        border: 1px solid #e3d6f3;
        border-radius: 0.6rem;
        color: #4b3a66;
        font-size: 0.85rem;
        line-height: 1.45;
    }

    .class-checkbox:hover {
        border-color: #c9b8e0;
        background: #f3eefb;
    }

    .class-checkbox:has(input:checked) {
        border-color: var(--color-primary, #2F124D);
        background: #f0e7fa;
        color: var(--color-primary, #2F124D);
        font-weight: 600;
    }

    .class-checkbox input {
        margin: 0;
        accent-color: var(--color-primary, #2F124D);
    }
</style>
