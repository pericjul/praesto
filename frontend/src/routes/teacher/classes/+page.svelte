<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t, locale } from "$lib/i18n";
    import { get } from "svelte/store";

    let { data, form } = $props();

    // State
    let showModal = $state(false);
    let editingClass = $state(null);
    let expandedClassId = $state(null);
    let deleteConfirmId = $state(null);

    // Schüler-Suche (debounced)
    let searchQuery = $state("");
    let searchResults = $state([]);
    let searching = $state(false);
    let debounceTimer;

    // Derived
    let classes = $derived(data?.classes ?? []);

    function onSearchInput() {
        clearTimeout(debounceTimer);
        const q = searchQuery;
        if (q.trim().length < 2) {
            searchResults = [];
            return;
        }
        debounceTimer = setTimeout(async () => {
            searching = true;
            try {
                const res = await fetch(`/api/students/search?q=${encodeURIComponent(q)}`);
                searchResults = res.ok ? await res.json() : [];
            } catch {
                searchResults = [];
            } finally {
                searching = false;
            }
        }, 300);
    }

    function resetSearch() {
        searchQuery = "";
        searchResults = [];
    }

    function openNewModal() {
        editingClass = null;
        showModal = true;
    }

    function openEditModal(cls) {
        editingClass = cls;
        showModal = true;
    }

    function closeModal() {
        showModal = false;
        editingClass = null;
    }

    function toggleExpand(classId) {
        if (expandedClassId === classId) {
            expandedClassId = null;
        } else {
            expandedClassId = classId;
            resetSearch();
        }
    }

    function confirmDelete(classId) {
        deleteConfirmId = classId;
    }

    function cancelDelete() {
        deleteConfirmId = null;
    }

    function formatDate(date) {
        if (!date) return "-";
        const localeMap = { de: "de-CH", en: "en-GB", fr: "fr-CH", it: "it-CH" };
        return new Date(date).toLocaleDateString(localeMap[get(locale)] ?? "de-CH", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric"
        });
    }
</script>

<svelte:head>
    <title>{$t('tclass.pageTitle')}</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">{$t('tclass.title')}</h1>
            <p class="subtitle">{$t('tclass.subtitle')}</p>
        </div>
        <button type="button" class="btn btn-primary" onclick={openNewModal}>
            {$t('tclass.newClass')}
        </button>
    </header>

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if form?.success}
        <div class="alert alert-success">
            {#if form.action === "created"}{$t('tclass.createdMsg')}
            {:else if form.action === "updated"}{$t('tclass.updatedMsg')}
            {:else if form.action === "deleted"}{$t('tclass.deletedMsg')}
            {:else if form.action === "studentAdded"}{$t('tclass.studentAddedMsg')}
            {:else if form.action === "studentRemoved"}{$t('tclass.studentRemovedMsg')}
            {/if}
        </div>
    {/if}

    <div class="stats-bar">
        <div class="stat-card">
            <span class="stat-value">{classes.length}</span>
            <span class="stat-label">{$t('tclass.statClasses')}</span>
        </div>
        <div class="stat-card">
            <span class="stat-value">{classes.reduce((sum, c) => sum + (c.students?.length || 0), 0)}</span>
            <span class="stat-label">{$t('tclass.statStudentsTotal')}</span>
        </div>
    </div>

    {#if classes.length === 0}
        <div class="empty-state">
            <div class="empty-icon">🎓</div>
            <h2>{$t('tclass.emptyTitle')}</h2>
            <p>{$t('tclass.emptyText')}</p>
            <button type="button" class="btn btn-primary" onclick={openNewModal}>
                {$t('tclass.emptyAction')}
            </button>
        </div>
    {:else}
        <div class="classes-list">
            {#each classes as cls}
                <div class="class-card" class:expanded={expandedClassId === cls.id}>
                    <div class="class-header">
                        <div class="class-info">
                            <h3 class="class-name">{cls.name}</h3>
                            <span class="class-meta">
                                {cls.students?.length || 0} {$t('tclass.students')} · {$t('tclass.createdOn')} {formatDate(cls.createdAt)}
                            </span>
                        </div>
                        <div class="class-actions">
                            <button type="button" class="btn-icon" onclick={() => toggleExpand(cls.id)}
                                title={expandedClassId === cls.id ? $t('tclass.close') : $t('tclass.manageStudents')}>
                                {expandedClassId === cls.id ? "▲" : "▼"}
                            </button>
                            <button type="button" class="btn-icon" onclick={() => openEditModal(cls)} title={$t('tclass.edit')}>
                                ✏️
                            </button>
                            <button type="button" class="btn-icon btn-danger" onclick={() => confirmDelete(cls.id)} title={$t('tclass.delete')}>
                                🗑️
                            </button>
                        </div>
                    </div>

                    {#if deleteConfirmId === cls.id}
                        <div class="delete-confirm">
                            <span class="delete-confirm-text">{$t('tclass.confirmDeletePre')} <strong>{cls.name}</strong> {$t('tclass.confirmDeletePost')}</span>
                            <div class="delete-actions">
                                <form method="POST" action="?/delete" use:enhance={() => {
                                    return async ({ result }) => {
                                        deleteConfirmId = null;
                                        if (result.type === 'success') await invalidateAll();
                                    };
                                }}>
                                    <input type="hidden" name="classId" value={cls.id} />
                                    <button type="submit" class="btn btn-danger">{$t('tclass.confirmDeleteYes')}</button>
                                </form>
                                <button type="button" class="btn btn-secondary" onclick={cancelDelete}>{$t('tclass.cancel')}</button>
                            </div>
                        </div>
                    {/if}

                    {#if expandedClassId === cls.id}
                        <div class="students-section">
                            <h4>{$t('tclass.studentsInClass')}</h4>

                            <div class="student-search">
                                <input type="search" class="filter-input"
                                    placeholder={$t('tclass.searchPlaceholder')}
                                    bind:value={searchQuery} oninput={onSearchInput} />

                                {#if searching}
                                    <p class="search-hint">{$t('tclass.searching')}</p>
                                {:else if searchQuery.trim().length >= 2 && searchResults.length === 0}
                                    <p class="search-hint">{$t('tclass.noResults')}</p>
                                {:else if searchResults.length > 0}
                                    <ul class="search-results">
                                        {#each searchResults as s (s.id)}
                                            <li>
                                                <form method="POST" action="?/addStudent"
                                                    use:enhance={() => {
                                                        return async ({ result }) => {
                                                            if (result.type === 'success') {
                                                                resetSearch();
                                                                await invalidateAll();
                                                            }
                                                        };
                                                    }}>
                                                    <input type="hidden" name="classId" value={cls.id} />
                                                    <input type="hidden" name="userId" value={s.id} />
                                                    <button type="submit" class="result-btn">
                                                        <span class="result-name">{s.firstName} {s.lastName}</span>
                                                        <span class="result-email">{s.email}</span>
                                                        <span class="result-add">{$t('tclass.add')}</span>
                                                    </button>
                                                </form>
                                            </li>
                                        {/each}
                                    </ul>
                                {/if}
                            </div>

                            {#if cls.students && cls.students.length > 0}
                                <ul class="students-list">
                                    {#each cls.students as student (student.id)}
                                        <li class="student-item">
                                            <span class="student-name">{student.firstName} {student.lastName}</span>
                                            <span class="student-email">{student.email}</span>
                                            <form method="POST" action="?/removeStudent"
                                                use:enhance={() => {
                                                    return async ({ result }) => {
                                                        if (result.type === 'success') await invalidateAll();
                                                    };
                                                }}>
                                                <input type="hidden" name="classId" value={cls.id} />
                                                <input type="hidden" name="userId" value={student.id} />
                                                <button type="submit" class="btn-remove" title={$t('tclass.remove')}>✕</button>
                                            </form>
                                        </li>
                                    {/each}
                                </ul>
                            {:else}
                                <p class="no-students">{$t('tclass.noStudents')}</p>
                            {/if}
                        </div>
                    {/if}
                </div>
            {/each}
        </div>
    {/if}
</div>

<!-- Modal -->
{#if showModal}
    <button type="button" class="modal-backdrop" onclick={closeModal} aria-label={$t('tclass.modalClose')}></button>
    <div class="modal" role="dialog" aria-modal="true">
        <div class="modal-header">
            <h2>{editingClass ? $t('tclass.modalEditTitle') : $t('tclass.modalNewTitle')}</h2>
            <button type="button" class="btn-close-modal" onclick={closeModal} aria-label={$t('tclass.close')}>✕</button>
        </div>

        <form method="POST" action={editingClass ? "?/update" : "?/create"}
            use:enhance={() => {
                return async ({ result }) => {
                    if (result.type === 'success') {
                        closeModal();
                        await invalidateAll();
                    }
                };
            }}>
            <div class="modal-body">
                {#if editingClass}
                    <input type="hidden" name="classId" value={editingClass.id} />
                {/if}
                <div class="form-group">
                    <label for="className">{$t('tclass.classNameLabel')}</label>
                    <input type="text" id="className" name="name" required
                        placeholder={$t('tclass.classNamePlaceholder')} value={editingClass?.name ?? ""} />
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>{$t('tclass.cancel')}</button>
                <button type="submit" class="btn btn-primary">
                    {editingClass ? $t('tclass.save') : $t('tclass.createBtn')}
                </button>
            </div>
        </form>
    </div>
{/if}

<style>
    /* ===== PAGE SPECIFIC STYLES ===== */
    .classes-list {
        display: flex;
        flex-direction: column;
        gap: var(--space-lg);
    }

    .class-card {
        background: var(--color-bg-card);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-xl);
        overflow: hidden;
    }

    .class-card.expanded {
        border-color: var(--color-primary);
    }

    .class-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: var(--space-lg);
    }

    .class-info {
        flex: 1;
        min-width: 0;
    }

    .class-name {
        margin: 0 0 var(--space-xs);
        font-size: var(--font-size-lg);
        font-weight: 600;
        color: var(--color-text-secondary);
    }

    .class-meta {
        font-size: var(--font-size-sm);
        color: var(--color-text-muted);
    }

    .class-actions {
        display: flex;
        gap: var(--space-xs);
    }

    /* Students Section */
    .students-section {
        padding: var(--space-lg);
        background: var(--color-bg-muted);
        border-top: 1px solid var(--color-border-light);
    }

    .students-section h4 {
        margin: 0 0 var(--space-lg);
        font-size: var(--font-size-base);
        color: var(--color-text-secondary);
    }

    .add-student-form {
        display: flex;
        gap: var(--space-sm);
        margin-bottom: var(--space-lg);
    }

    .add-student-form .filter-input {
        flex: 1;
    }

    .students-list {
        list-style: none;
        margin: 0;
        padding: 0;
        display: flex;
        flex-direction: column;
        gap: var(--space-sm);
    }

    .student-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: var(--space-sm) var(--space-md);
        background: var(--color-bg-card);
        border: 1px solid var(--color-border-light);
        border-radius: var(--radius-md);
    }

    .student-email {
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
    }

    .btn-remove {
        background: none;
        border: none;
        color: var(--color-text-muted);
        cursor: pointer;
        padding: var(--space-xs);
        border-radius: var(--radius-sm);
        transition: all var(--transition-base);
    }

    .btn-remove:hover {
        color: var(--color-error);
        background: var(--color-error-bg);
    }

    .no-students {
        margin: 0;
        color: var(--color-text-muted);
        font-size: var(--font-size-sm);
        font-style: italic;
    }

    /* Schüler-Suche */
    .student-search {
        position: relative;
        margin-bottom: var(--space-lg);
    }

    .student-search .filter-input {
        width: 100%;
        box-sizing: border-box;
    }

    .search-hint {
        margin: var(--space-xs) 0 0;
        font-size: var(--font-size-sm);
        color: var(--color-text-muted);
    }

    .search-results {
        list-style: none;
        margin: var(--space-xs) 0 0;
        padding: 0;
        border: 1px solid var(--color-border-light);
        border-radius: var(--radius-md);
        overflow: hidden;
        background: var(--color-bg-card);
    }

    .result-btn {
        display: flex;
        align-items: center;
        gap: var(--space-sm);
        width: 100%;
        padding: var(--space-sm) var(--space-md);
        background: none;
        border: none;
        border-bottom: 1px solid var(--color-border-light);
        cursor: pointer;
        text-align: left;
        font-size: var(--font-size-sm);
    }

    .result-btn:hover {
        background: var(--color-bg-muted);
    }

    .result-name {
        font-weight: 600;
        color: var(--color-text-secondary);
    }

    .result-email {
        color: var(--color-text-muted);
    }

    .result-add {
        margin-left: auto;
        color: var(--color-primary);
        font-weight: 600;
    }

    .student-name {
        font-weight: 600;
        color: var(--color-text-secondary);
        font-size: var(--font-size-sm);
    }

    .student-item .student-email {
        margin-left: var(--space-sm);
        margin-right: auto;
    }

    @media (max-width: 600px) {
        .class-header {
            flex-direction: column;
            align-items: flex-start;
            gap: var(--space-md);
        }

        .class-actions {
            align-self: flex-end;
        }

        .add-student-form {
            flex-direction: column;
        }
    }
</style>
