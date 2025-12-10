<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";

    // Svelte 5
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

    // Unique companies für Dropdown
    let uniqueCompanies = $derived(() => {
        const companies = allNotes
            .map(n => n.companyName)
            .filter(c => c && c.trim() !== "");
        return [...new Set(companies)].sort();
    });

    // Gefilterte Notizen
    let notes = $derived(() => {
        let filtered = [...allNotes];

        // Textsuche
        if (searchQuery.trim()) {
            const query = searchQuery.toLowerCase();
            filtered = filtered.filter(note => 
                note.text?.toLowerCase().includes(query) ||
                note.companyName?.toLowerCase().includes(query) ||
                note.position?.toLowerCase().includes(query)
            );
        }

        // Firmen-Filter
        if (filterCompany === "none") {
            filtered = filtered.filter(note => !note.companyName || note.companyName.trim() === "");
        } else if (filterCompany !== "all") {
            filtered = filtered.filter(note => note.companyName === filterCompany);
        }

        // Sortierung
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

    // Filter zurücksetzen
    function resetFilters() {
        searchQuery = "";
        filterCompany = "all";
        sortBy = "newest";
    }

    // Modal öffnen für neue Notiz
    function openNewModal() {
        editingNote = null;
        showModal = true;
    }

    // Modal öffnen zum Bearbeiten
    function openEditModal(note) {
        editingNote = note;
        showModal = true;
    }

    // Modal schliessen
    function closeModal() {
        showModal = false;
        editingNote = null;
    }

    // Löschen bestätigen
    function confirmDelete(noteId) {
        deleteConfirmId = noteId;
    }

    function cancelDelete() {
        deleteConfirmId = null;
    }

    // Datum formatieren
    function formatDate(date) {
        if (!date) return "";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        });
    }

    // Text kürzen für Vorschau
    function truncateText(text, maxLength = 150) {
        if (!text || text.length <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
</script>

<svelte:head>
    <title>Meine Notizen – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">📝 Meine Notizen</h1>
            <p class="subtitle">
                Halte wichtige Informationen zu Unternehmen und deinem Bewerbungsprozess fest.
            </p>
        </div>
        <button type="button" class="btn btn-primary" onclick={openNewModal}>
            ➕ Neue Notiz
        </button>
    </header>

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if form?.success}
        <div class="alert alert-success">
            {#if form.action === "created"}
                Notiz wurde erstellt.
            {:else if form.action === "updated"}
                Notiz wurde aktualisiert.
            {:else if form.action === "deleted"}
                Notiz wurde gelöscht.
            {/if}
        </div>
    {/if}

    <!-- Filter -->
{#if allNotes.length > 0}
    <div class="filter-bar">
        <!-- Stats links in der Filterzeile -->
        <div class="filter-stats">
            <div class="stat-pill">
                <span class="stat-value">{allNotes.length}</span>
                <span class="stat-label">Total</span>
            </div>
            {#if notes().length !== allNotes.length}
                <div class="stat-pill stat-pill-filtered">
                    <span class="stat-value">{notes().length}</span>
                    <span class="stat-label">Gefiltert</span>
                </div>
            {/if}
        </div>

        <!-- Filter rechts daneben -->
        <div class="filter-group">
            <input 
                type="text" 
                placeholder="🔍 Suchen..." 
                bind:value={searchQuery}
                class="filter-input"
            />
        </div>

        <div class="filter-group">
            <select bind:value={filterCompany} class="filter-select">
                <option value="all">Alle Firmen</option>
                <option value="none">Ohne Firma</option>
                {#each uniqueCompanies() as company}
                    <option value={company}>{company}</option>
                {/each}
            </select>
        </div>

        <div class="filter-group">
            <select bind:value={sortBy} class="filter-select">
                <option value="newest">Neueste zuerst</option>
                <option value="oldest">Älteste zuerst</option>
                <option value="updated">Zuletzt bearbeitet</option>
                <option value="company">Nach Firma</option>
            </select>
        </div>

        {#if searchQuery || filterCompany !== "all" || sortBy !== "newest"}
            <button type="button" class="btn-reset" onclick={resetFilters}>
                ✕ Filter zurücksetzen
            </button>
        {/if}
    </div>
{/if}

    {#if allNotes.length === 0}
        <div class="empty-state">
            <div class="empty-icon">📋</div>
            <h2>Noch keine Notizen</h2>
            <p>Erstelle deine erste Notiz, um wichtige Informationen festzuhalten!</p>
            <button type="button" class="btn btn-primary" onclick={openNewModal}>
                Erste Notiz erstellen
            </button>
        </div>
    {:else if notes().length === 0}
        <div class="empty-state">
            <div class="empty-icon">🔍</div>
            <h2>Keine Notizen gefunden</h2>
            <p>Passe deine Filter an oder erstelle eine neue Notiz.</p>
            <button type="button" class="btn btn-secondary" onclick={resetFilters}>
                Filter zurücksetzen
            </button>
        </div>
    {:else}
        <div class="notes-grid">
            {#each notes() as note}
                <article class="note-card">
                    <div class="note-header">
                        {#if note.companyName}
                            <span class="note-company">🏢 {note.companyName}</span>
                        {/if}
                        {#if note.position}
                            <span class="note-position">💼 {note.position}</span>
                        {/if}
                        {#if !note.companyName && !note.position}
                            <span class="note-general">📌 Allgemeine Notiz</span>
                        {/if}
                    </div>

                    <div class="note-body">
                        <p class="note-text">{truncateText(note.text)}</p>
                    </div>

                    <div class="note-footer">
                        <span class="note-date">
                            {formatDate(note.lastUpdated || note.createdAt)}
                        </span>
                        <div class="note-actions">
                            <button type="button" class="btn-icon" onclick={() => openEditModal(note)} title="Bearbeiten">
                                ✏️
                            </button>
                            <button type="button" class="btn-icon btn-danger" onclick={() => confirmDelete(note.id)} title="Löschen">
                                🗑️
                            </button>
                        </div>
                    </div>

                    <!-- Delete Confirmation -->
                    {#if deleteConfirmId === note.id}
                        <div class="delete-confirm">
                            <p>Wirklich löschen?</p>
                            <div class="delete-actions">
                                <form method="POST" action="?/delete" use:enhance={() => {
                                    return async ({ result }) => {
                                        deleteConfirmId = null;
                                        if (result.type === 'success') {
                                            await invalidateAll();
                                        }
                                    };
                                }}>
                                    <input type="hidden" name="noteId" value={note.id} />
                                    <button type="submit" class="btn btn-danger-solid">Ja, löschen</button>
                                </form>
                                <button type="button" class="btn btn-secondary" onclick={cancelDelete}>Abbrechen</button>
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
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <button type="button" class="modal-backdrop" onclick={closeModal} aria-label="Modal schliessen"></button>
    <div class="modal" role="dialog" aria-modal="true">
        <div class="modal-header">
            <h2>{editingNote ? "Notiz bearbeiten" : "Neue Notiz"}</h2>
            <button type="button" class="btn-close-modal" onclick={closeModal} aria-label="Schliessen">✕</button>
        </div>
        <form 
            method="POST" 
            action={editingNote ? "?/update" : "?/create"}
            use:enhance={() => {
                return async ({ result }) => {
                    if (result.type === 'success') {
                        closeModal();
                        await invalidateAll();
                    }
                };
            }}
        >
            {#if editingNote}
                <input type="hidden" name="noteId" value={editingNote.id} />
            {/if}

            <div class="form-group">
                <label for="companyName">Firma (optional)</label>
                <input 
                    type="text" 
                    id="companyName" 
                    name="companyName" 
                    placeholder="z.B. Swisscom, ABB, ..."
                    value={editingNote?.companyName ?? ""}
                />
            </div>

            <div class="form-group">
                <label for="position">Position (optional)</label>
                <input 
                    type="text" 
                    id="position" 
                    name="position" 
                    placeholder="z.B. Informatiker EFZ, KV, ..."
                    value={editingNote?.position ?? ""}
                />
            </div>

            <div class="form-group">
                <label for="text">Notiz *</label>
                <textarea 
                    id="text" 
                    name="text" 
                    rows="6"
                    placeholder="Deine Notizen..."
                    required
                >{editingNote?.text ?? ""}</textarea>
            </div>

            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>
                    Abbrechen
                </button>
                <button type="submit" class="btn btn-primary">
                    {editingNote ? "Speichern" : "Erstellen"}
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

    .page-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 1rem;
        margin-bottom: 1.5rem;
        flex-wrap: wrap;
    }

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

    .stats-row {
        display: flex;
        gap: 1rem;
        margin-bottom: 1.5rem;
    }

    .stat-card {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        padding: 0.75rem 1.25rem;
        text-align: center;
        min-width: 80px;
    }

    .stat-filtered {
        background: #f0e8ff;
        border-color: #d4c4f0;
    }

    .stat-value {
        display: block;
        font-size: 1.5rem;
        font-weight: 700;
        color: #3b134f;
    }

    .stat-label {
        font-size: 0.8rem;
        color: #7c6b80;
    }

    /* Filter Bar */
    .filter-bar {
        display: flex;
        flex-wrap: wrap;
        gap: 0.75rem;
        align-items: center;
        padding: 1rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        margin-bottom: 1.5rem;
    }

    .filter-group {
        flex: 1;
        min-width: 150px;
    }

    .filter-input,
    .filter-select {
        width: 100%;
        padding: 0.6rem 0.75rem;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        background: #faf8fc;
        font-family: inherit;
    }

    .filter-input:focus,
    .filter-select:focus {
        outline: none;
        border-color: #7c3aed;
        background: #fff;
    }

    .filter-select {
        cursor: pointer;
    }

    .btn-reset {
        background: none;
        border: 1px solid #e8e0f0;
        padding: 0.6rem 1rem;
        border-radius: 0.5rem;
        font-size: 0.85rem;
        color: #6b647a;
        cursor: pointer;
        white-space: nowrap;
    }

    .btn-reset:hover {
        background: #fef2f2;
        border-color: #fecaca;
        color: #dc2626;
    }

    @media (max-width: 600px) {
        .filter-bar {
            flex-direction: column;
        }
        .filter-group {
            width: 100%;
        }
    }

    .empty-state {
        text-align: center;
        padding: 3rem 1rem;
        background: #faf6ff;
        border: 2px dashed #e0d4ff;
        border-radius: 1rem;
    }

    .empty-icon {
        font-size: 3rem;
        margin-bottom: 0.5rem;
    }

    .empty-state h2 {
        margin: 0 0 0.5rem;
        color: #3b134f;
    }

    .empty-state p {
        margin: 0 0 1.5rem;
        color: #6b647a;
    }

    /* Notes Grid */
    .notes-grid {
        display: grid;
        gap: 1rem;
    }

    @media (min-width: 640px) {
        .notes-grid {
            grid-template-columns: repeat(2, 1fr);
        }
    }

    @media (min-width: 900px) {
        .notes-grid {
            grid-template-columns: repeat(3, 1fr);
        }
    }

    .note-card {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 1rem;
        padding: 1rem;
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
        position: relative;
        transition: box-shadow 0.2s;
    }

    .note-card:hover {
        box-shadow: 0 4px 12px rgba(0,0,0,0.08);
    }

    .note-header {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
    }

    .note-company, .note-position, .note-general {
        font-size: 0.8rem;
        padding: 0.25rem 0.5rem;
        border-radius: 0.5rem;
        background: #f3f0f7;
        color: #5e4a6d;
    }

    .note-company {
        background: #e8f4f8;
        color: #1a5f7a;
    }

    .note-position {
        background: #fef3e2;
        color: #8b5a00;
    }

    .note-body {
        flex: 1;
    }

    .note-text {
        margin: 0;
        font-size: 0.9rem;
        color: #3b2d4a;
        line-height: 1.5;
        white-space: pre-wrap;
    }

    .note-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 0.5rem;
        border-top: 1px solid #f0e8f8;
    }

    .note-date {
        font-size: 0.75rem;
        color: #9a8b9d;
    }

    .note-actions {
        display: flex;
        gap: 0.25rem;
    }

    .btn-icon {
        background: none;
        border: none;
        padding: 0.25rem 0.5rem;
        font-size: 1rem;
        cursor: pointer;
        border-radius: 0.5rem;
        transition: background 0.2s;
    }

    .btn-icon:hover {
        background: #f3f0f7;
    }

    .btn-icon.btn-danger:hover {
        background: #fef2f2;
    }

    /* Delete Confirmation */
    .delete-confirm {
        position: absolute;
        inset: 0;
        background: rgba(255,255,255,0.95);
        border-radius: 1rem;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 0.75rem;
        padding: 1rem;
    }

    .delete-confirm p {
        margin: 0;
        font-weight: 600;
        color: #3b134f;
    }

    .delete-actions {
        display: flex;
        gap: 0.5rem;
    }

    /* Buttons */
    .btn {
        padding: 0.5rem 1rem;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        font-weight: 500;
        text-decoration: none;
        border: none;
        cursor: pointer;
        transition: all 0.15s ease;
    }

    .btn-primary {
        background: #2F124D;
        color: #fff;
    }

    .btn-primary:hover {
        background: rgb(56, 16, 121);
    }

    .btn-secondary {
        background: #f3f4f6;
        color: #374151;
        border: 1px solid #e5e7eb;
    }

    .btn-secondary:hover {
        background: #e5e7eb;
    }

    .btn-danger-solid {
        background: #dc2626;
        color: #fff;
    }

    .btn-danger-solid:hover {
        background: #b91c1c;
    }

    /* Alerts */
    .alert {
        padding: 0.75rem 1rem;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
    }

    .alert-danger {
        background: #fef2f2;
        color: #dc2626;
        border: 1px solid #fecaca;
    }

    .alert-success {
        background: #f0fdf4;
        color: #16a34a;
        border: 1px solid #bbf7d0;
    }

    /* Modal */
    .modal-backdrop {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        width: 100%;
        height: 100%;
        background: rgba(0,0,0,0.5);
        z-index: 998;
        border: none;
        cursor: default;
        padding: 0;
        margin: 0;
    }

    .modal {
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background-color: #ffffff !important;
        border-radius: 1rem;
        padding: 1.5rem;
        width: 90%;
        max-width: 500px;
        max-height: 90vh;
        overflow-y: auto;
        z-index: 999;
        box-shadow: 0 20px 40px rgba(0,0,0,0.3);
        display: block;
        visibility: visible;
        opacity: 1;
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1.5rem;
    }

    .modal-header h2 {
        margin: 0;
        font-size: 1.25rem;
        color: #2d2141;
    }

    .btn-close-modal {
        background: none;
        border: none;
        font-size: 1.25rem;
        cursor: pointer;
        color: #6b647a;
        padding: 0.25rem;
    }

    .btn-close-modal:hover {
        color: #2d2141;
    }

    .form-group {
        margin-bottom: 1rem;
    }

    .form-group label {
        display: block;
        margin-bottom: 0.5rem;
        font-size: 0.9rem;
        font-weight: 500;
        color: #3b2d4a;
    }

    .form-group input,
    .form-group textarea {
        width: 100%;
        padding: 0.75rem;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        font-size: 0.95rem;
        font-family: inherit;
        background: #faf8fc;
        box-sizing: border-box;
    }

    .form-group input:focus,
    .form-group textarea:focus {
        outline: none;
        border-color: #7c3aed;
        background: #fff;
    }

    .form-group textarea {
        resize: vertical;
        min-height: 120px;
    }

    .modal-actions {
        display: flex;
        justify-content: flex-end;
        gap: 0.75rem;
        margin-top: 1.5rem;
    }

    .filter-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
    align-items: center;
    padding: 0.9rem;
    background: #ffffff;
    border: 1px solid #e6d9cc;
    border-radius: 0.9rem;
    margin-bottom: 1.5rem;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.03);
}

/* Neuer Bereich links für die Zahlen */
.filter-stats {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex-shrink: 0;
}

/* Kompaktere Pill-Version der bisherigen Stat-Cards */
.stat-pill {
    background: #fbf7f1;
    border: 1px solid #e6d9cc;
    border-radius: 999px;
    padding: 0.35rem 0.8rem;
    display: flex;
    align-items: baseline;
    gap: 0.35rem;
}

.stat-pill-filtered {
    background: #f7f0ff;
    border-color: #ddcff9;
}

.stat-value {
    font-size: 0.95rem;
    font-weight: 600;
    color: #2f124d;
}

.stat-label {
    font-size: 0.75rem;
    color: #8a7a8c;
}
</style>