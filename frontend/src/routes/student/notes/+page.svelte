<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    // State
    let showModal = $state(false);
    let editingNote = $state(null);
    let deleteConfirmId = $state(null);

    // Filter State
    let searchQuery = $state("");
    let filterCompany = $state("all");
    let sortBy = $state("newest");

    // Derived
    let allNotes = $derived(data?.notes ?? []);

    let uniqueCompanies = $derived(() => {
        const companies = allNotes.map(n => n.companyName).filter(c => c && c.trim() !== "");
        return [...new Set(companies)].sort();
    });

    let notes = $derived(() => {
        let filtered = [...allNotes];

        if (searchQuery.trim()) {
            const query = searchQuery.toLowerCase();
            filtered = filtered.filter(note => 
                note.text?.toLowerCase().includes(query) ||
                note.companyName?.toLowerCase().includes(query) ||
                note.position?.toLowerCase().includes(query)
            );
        }

        if (filterCompany === "none") {
            filtered = filtered.filter(note => !note.companyName || note.companyName.trim() === "");
        } else if (filterCompany !== "all") {
            filtered = filtered.filter(note => note.companyName === filterCompany);
        }

        if (sortBy === "newest") {
            filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        } else if (sortBy === "oldest") {
            filtered.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
        } else if (sortBy === "company") {
            filtered.sort((a, b) => (a.companyName || "zzz").localeCompare(b.companyName || "zzz"));
        } else if (sortBy === "updated") {
            filtered.sort((a, b) => new Date(b.lastUpdated || b.createdAt) - new Date(a.lastUpdated || a.createdAt));
        }

        return filtered;
    });

    function resetFilters() {
        searchQuery = "";
        filterCompany = "all";
        sortBy = "newest";
    }

    function openNewModal() {
        editingNote = null;
        showModal = true;
    }

    function openEditModal(note) {
        editingNote = note;
        showModal = true;
    }

    function closeModal() {
        showModal = false;
        editingNote = null;
    }

    function confirmDelete(noteId) {
        deleteConfirmId = noteId;
    }

    function cancelDelete() {
        deleteConfirmId = null;
    }

    function formatDate(date) {
        if (!date) return "";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", year: "numeric",
            hour: "2-digit", minute: "2-digit"
        });
    }

    function truncateText(text, maxLength = 150) {
        if (!text || text.length <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
</script>

<svelte:head>
    <title>{$t('snotes.headTitle')}</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">📝 {$t('snotes.title')}</h1>
            <p class="subtitle">{$t('snotes.subtitle')}</p>
        </div>
        <button type="button" class="btn btn-primary" onclick={openNewModal}>
            ➕ {$t('snotes.newNote')}
        </button>
    </header>

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if form?.success}
        <div class="alert alert-success">
            {#if form.action === "created"}{$t('snotes.successCreated')}
            {:else if form.action === "updated"}{$t('snotes.successUpdated')}
            {:else if form.action === "deleted"}{$t('snotes.successDeleted')}
            {/if}
        </div>
    {/if}

    {#if allNotes.length > 0}
        <div class="filter-bar">
            <div class="stats-inline">
                <span class="badge badge-primary">{allNotes.length} {$t('snotes.total')}</span>
                {#if notes().length !== allNotes.length}
                    <span class="badge badge-muted">{notes().length} {$t('snotes.filtered')}</span>
                {/if}
            </div>

            <div class="filter-group">
                <input type="text" placeholder={$t('snotes.searchPlaceholder')} bind:value={searchQuery} class="filter-input" />
            </div>

            <div class="filter-group">
                <select bind:value={filterCompany} class="filter-select">
                    <option value="all">{$t('snotes.allCompanies')}</option>
                    <option value="none">{$t('snotes.noCompany')}</option>
                    {#each uniqueCompanies() as company}
                        <option value={company}>{company}</option>
                    {/each}
                </select>
            </div>

            <div class="filter-group">
                <select bind:value={sortBy} class="filter-select">
                    <option value="newest">{$t('snotes.sortNewest')}</option>
                    <option value="oldest">{$t('snotes.sortOldest')}</option>
                    <option value="updated">{$t('snotes.sortUpdated')}</option>
                    <option value="company">{$t('snotes.sortCompany')}</option>
                </select>
            </div>

            {#if searchQuery || filterCompany !== "all" || sortBy !== "newest"}
                <button type="button" class="btn-reset" onclick={resetFilters}>✕ {$t('snotes.resetFilters')}</button>
            {/if}
        </div>
    {/if}

    {#if allNotes.length === 0}
        <div class="empty-state">
            <div class="empty-icon">📋</div>
            <h2>{$t('snotes.emptyTitle')}</h2>
            <p>{$t('snotes.emptyText')}</p>
            <button type="button" class="btn btn-primary" onclick={openNewModal}>
                {$t('snotes.emptyButton')}
            </button>
        </div>
    {:else if notes().length === 0}
        <div class="empty-state">
            <div class="empty-icon">🔍</div>
            <h2>{$t('snotes.noResultsTitle')}</h2>
            <p>{$t('snotes.noResultsText')}</p>
            <button type="button" class="btn btn-secondary" onclick={resetFilters}>{$t('snotes.resetFilters')}</button>
        </div>
    {:else}
        <div class="notes-grid">
            {#each notes() as note}
                <article class="note-card">
                    <div class="note-header">
                        {#if note.companyName}
                            <span class="note-tag note-company">🏢 {note.companyName}</span>
                        {/if}
                        {#if note.position}
                            <span class="note-tag note-position">💼 {note.position}</span>
                        {/if}
                        {#if !note.companyName && !note.position}
                            <span class="note-tag note-general">📌 {$t('snotes.generalNote')}</span>
                        {/if}
                    </div>

                    <div class="note-body">
                        <p class="note-text">{truncateText(note.text)}</p>
                    </div>

                    <div class="note-footer">
                        <span class="note-date">{formatDate(note.lastUpdated || note.createdAt)}</span>
                        <div class="note-actions">
                            <button type="button" class="btn-icon" onclick={() => openEditModal(note)} title={$t('snotes.edit')}>✏️</button>
                            <button type="button" class="btn-icon btn-danger" onclick={() => confirmDelete(note.id)} title={$t('snotes.delete')}>🗑️</button>
                        </div>
                    </div>

                    {#if deleteConfirmId === note.id}
                        <div class="delete-overlay">
                            <p>{$t('snotes.confirmDelete')}</p>
                            <div class="delete-actions">
                                <form method="POST" action="?/delete" use:enhance={() => {
                                    return async ({ result }) => {
                                        deleteConfirmId = null;
                                        if (result.type === 'success') await invalidateAll();
                                    };
                                }}>
                                    <input type="hidden" name="noteId" value={note.id} />
                                    <button type="submit" class="btn btn-danger">{$t('snotes.confirmYes')}</button>
                                </form>
                                <button type="button" class="btn btn-secondary" onclick={cancelDelete}>{$t('snotes.cancel')}</button>
                            </div>
                        </div>
                    {/if}
                </article>
            {/each}
        </div>
    {/if}
</div>

<!-- Modal -->
{#if showModal}
    <button type="button" class="modal-backdrop" onclick={closeModal} aria-label={$t('snotes.closeModal')}></button>
    <div class="modal" role="dialog" aria-modal="true">
        <div class="modal-header">
            <h2>{editingNote ? $t('snotes.editNote') : $t('snotes.newNote')}</h2>
            <button type="button" class="btn-close-modal" onclick={closeModal} aria-label={$t('snotes.close')}>✕</button>
        </div>

        <form method="POST" action={editingNote ? "?/update" : "?/create"}
            use:enhance={() => {
                return async ({ result }) => {
                    if (result.type === 'success') {
                        closeModal();
                        await invalidateAll();
                    }
                };
            }}>
            <div class="modal-body">
                {#if editingNote}
                    <input type="hidden" name="noteId" value={editingNote.id} />
                {/if}

                <div class="form-group">
                    <label for="companyName">{$t('snotes.labelCompany')}</label>
                    <input type="text" id="companyName" name="companyName"
                        placeholder={$t('snotes.placeholderCompany')} value={editingNote?.companyName ?? ""} />
                </div>

                <div class="form-group">
                    <label for="position">{$t('snotes.labelPosition')}</label>
                    <input type="text" id="position" name="position"
                        placeholder={$t('snotes.placeholderPosition')} value={editingNote?.position ?? ""} />
                </div>

                <div class="form-group">
                    <label for="text">{$t('snotes.labelText')}</label>
                    <textarea id="text" name="text" required rows="5"
                        placeholder={$t('snotes.placeholderText')}>{editingNote?.text ?? ""}</textarea>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>{$t('snotes.cancel')}</button>
                <button type="submit" class="btn btn-primary">{editingNote ? $t('snotes.save') : $t('snotes.create')}</button>
            </div>
        </form>
    </div>
{/if}

<style>
    /* ===== PAGE SPECIFIC STYLES ===== */
    .stats-inline {
        display: flex;
        gap: var(--space-sm);
        align-items: center;
    }

    .notes-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: var(--space-lg);
    }

    .note-card {
        background: var(--color-bg-card);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-xl);
        padding: var(--space-lg);
        display: flex;
        flex-direction: column;
        gap: var(--space-md);
        position: relative;
    }

    .note-header {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-sm);
    }

    .note-tag {
        font-size: var(--font-size-xs);
        padding: var(--space-xs) var(--space-sm);
        border-radius: var(--radius-md);
        font-weight: 500;
    }

    .note-company {
        background: #eff6ff;
        color: #1e40af;
    }

    .note-position {
        background: #f0fdf4;
        color: #166534;
    }

    .note-general {
        background: var(--color-warning-bg);
        color: #92400e;
    }

    .note-body {
        flex: 1;
    }

    .note-text {
        margin: 0;
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
        line-height: 1.5;
        white-space: pre-wrap;
    }

    .note-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: var(--space-sm);
        border-top: 1px solid var(--color-border-light);
    }

    .note-date {
        font-size: var(--font-size-xs);
        color: var(--color-text-placeholder);
    }

    .note-actions {
        display: flex;
        gap: var(--space-xs);
    }

    .delete-overlay {
        position: absolute;
        inset: 0;
        background: rgba(255, 255, 255, 0.95);
        border-radius: var(--radius-xl);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: var(--space-md);
        padding: var(--space-lg);
    }

    .delete-overlay p {
        margin: 0;
        font-weight: 600;
        color: var(--color-primary);
    }

    @media (max-width: 600px) {
        .notes-grid {
            grid-template-columns: 1fr;
        }
    }
</style>
