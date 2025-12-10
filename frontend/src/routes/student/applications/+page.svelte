<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";

    // Svelte 5
    let { data, form } = $props();

    // State
    let showModal = $state(false);
    let editingApplication = $state(null);
    let deleteConfirmId = $state(null);

    // Filter State
    let searchQuery = $state("");
    let filterStatus = $state("all");
    let sortBy = $state("newest");

    // Derived
    let allApplications = $derived(data?.applications ?? []);
    let stats = $derived(data?.stats ?? {});

    // Status-Optionen
    const statusOptions = [
        { value: "PLANNED", label: "Geplant", emoji: "📝", color: "#6b7280" },
        { value: "APPLIED", label: "Beworben", emoji: "📤", color: "#3b82f6" },
        { value: "INVITED", label: "Eingeladen", emoji: "📅", color: "#f59e0b" },
        { value: "INTERVIEW_DONE", label: "Gespräch absolviert", emoji: "✅", color: "#8b5cf6" },
        { value: "ACCEPTED", label: "Zusage", emoji: "🎉", color: "#10b981" },
        { value: "REJECTED", label: "Absage", emoji: "❌", color: "#ef4444" },
        { value: "WITHDRAWN", label: "Zurückgezogen", emoji: "🔙", color: "#9ca3af" }
    ];

    function getStatusInfo(status) {
        return statusOptions.find(s => s.value === status) || statusOptions[0];
    }

    // Gefilterte Bewerbungen
    let applications = $derived(() => {
        let filtered = [...allApplications];

        // Textsuche
        if (searchQuery.trim()) {
            const query = searchQuery.toLowerCase();
            filtered = filtered.filter(app => 
                app.companyName?.toLowerCase().includes(query) ||
                app.position?.toLowerCase().includes(query) ||
                app.notes?.toLowerCase().includes(query)
            );
        }

        // Status-Filter
        if (filterStatus !== "all") {
            filtered = filtered.filter(app => app.status === filterStatus);
        }

        // Sortierung
        if (sortBy === "newest") {
            filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        } else if (sortBy === "oldest") {
            filtered.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
        } else if (sortBy === "company") {
            filtered.sort((a, b) => (a.companyName || "").localeCompare(b.companyName || ""));
        } else if (sortBy === "status") {
            const statusOrder = ["ACCEPTED", "INVITED", "INTERVIEW_DONE", "APPLIED", "PLANNED", "REJECTED", "WITHDRAWN"];
            filtered.sort((a, b) => statusOrder.indexOf(a.status) - statusOrder.indexOf(b.status));
        }

        return filtered;
    });

    // Filter zurücksetzen
    function resetFilters() {
        searchQuery = "";
        filterStatus = "all";
        sortBy = "newest";
    }

    // Modal öffnen für neue Bewerbung
    function openNewModal() {
        editingApplication = null;
        showModal = true;
    }

    // Modal öffnen zum Bearbeiten
    function openEditModal(app) {
        editingApplication = app;
        showModal = true;
    }

    // Modal schliessen
    function closeModal() {
        showModal = false;
        editingApplication = null;
    }

    // Löschen bestätigen
    function confirmDelete(appId) {
        deleteConfirmId = appId;
    }

    function cancelDelete() {
        deleteConfirmId = null;
    }

    // Datum formatieren
    function formatDate(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric"
        });
    }

    // ISO Datum für Input
    function toISODate(date) {
        if (!date) return "";
        try {
            const d = new Date(date);
            return d.toISOString().split("T")[0];
        } catch {
            return "";
        }
    }
</script>

<svelte:head>
    <title>Bewerbungs-Tracker – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">📊 Bewerbungs-Tracker</h1>
            <p class="subtitle">
                Behalte den Überblick über deine Bewerbungen und deren Status.
            </p>
        </div>
        <button type="button" class="btn btn-primary" onclick={openNewModal}>
            ➕ Neue Bewerbung
        </button>
    </header>

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if form?.success}
        <div class="alert alert-success">
            {#if form.action === "created"}
                Bewerbung wurde hinzugefügt.
            {:else if form.action === "updated"}
                Bewerbung wurde aktualisiert.
            {:else if form.action === "deleted"}
                Bewerbung wurde gelöscht.
            {:else if form.action === "statusUpdated"}
                Status wurde geändert.
            {/if}
        </div>
    {/if}

    <!-- Stats Cards -->
    <div class="stats-grid">
        <div class="stat-card stat-total">
            <span class="stat-value">{stats.total ?? 0}</span>
            <span class="stat-label">Total</span>
        </div>
        <div class="stat-card stat-applied">
            <span class="stat-value">{stats.applied ?? 0}</span>
            <span class="stat-label">Beworben</span>
        </div>
        <div class="stat-card stat-invited">
            <span class="stat-value">{stats.invited ?? 0}</span>
            <span class="stat-label">Eingeladen</span>
        </div>
        <div class="stat-card stat-accepted">
            <span class="stat-value">{stats.accepted ?? 0}</span>
            <span class="stat-label">Zusagen</span>
        </div>
        <div class="stat-card stat-rejected">
            <span class="stat-value">{stats.rejected ?? 0}</span>
            <span class="stat-label">Absagen</span>
        </div>
    </div>

    <!-- Filter -->
    {#if allApplications.length > 0}
        <div class="filter-bar">
            <div class="filter-group">
                <input 
                    type="text" 
                    placeholder="🔍 Suchen..." 
                    bind:value={searchQuery}
                    class="filter-input"
                />
            </div>

            <div class="filter-group">
                <select bind:value={filterStatus} class="filter-select">
                    <option value="all">Alle Status</option>
                    {#each statusOptions as opt}
                        <option value={opt.value}>{opt.emoji} {opt.label}</option>
                    {/each}
                </select>
            </div>

            <div class="filter-group">
                <select bind:value={sortBy} class="filter-select">
                    <option value="newest">Neueste zuerst</option>
                    <option value="oldest">Älteste zuerst</option>
                    <option value="company">Nach Firma</option>
                    <option value="status">Nach Status</option>
                </select>
            </div>

            {#if searchQuery || filterStatus !== "all" || sortBy !== "newest"}
                <button type="button" class="btn-reset" onclick={resetFilters}>
                    ✕ Filter zurücksetzen
                </button>
            {/if}
        </div>
    {/if}

    {#if allApplications.length === 0}
        <div class="empty-state">
            <div class="empty-icon">📋</div>
            <h2>Noch keine Bewerbungen</h2>
            <p>Starte deinen Bewerbungs-Tracker und behalte den Überblick!</p>
            <button type="button" class="btn btn-primary" onclick={openNewModal}>
                Erste Bewerbung hinzufügen
            </button>
        </div>
    {:else if applications().length === 0}
        <div class="empty-state">
            <div class="empty-icon">🔍</div>
            <h2>Keine Bewerbungen gefunden</h2>
            <p>Passe deine Filter an.</p>
            <button type="button" class="btn btn-secondary" onclick={resetFilters}>
                Filter zurücksetzen
            </button>
        </div>
    {:else}
        <!-- Applications Table -->
        <div class="table-wrapper">
            <table class="applications-table">
                <thead>
                    <tr>
                        <th>Firma</th>
                        <th>Position</th>
                        <th>Status</th>
                        <th>Beworben am</th>
                        <th>Gespräch</th>
                        <th>Aktionen</th>
                    </tr>
                </thead>
                <tbody>
                    {#each applications() as app}
                        {@const statusInfo = getStatusInfo(app.status)}
                        <tr class="app-row">
                            <td>
                                <div class="td-company">
                                    <span class="company-name">{app.companyName}</span>
                                    {#if app.notes}
                                        <span class="has-notes" title={app.notes}>📝</span>
                                    {/if}
                                </div>
                            </td>
                            <td class="td-position">{app.position || "-"}</td>
                            <td class="td-status">
                                <form method="POST" action="?/updateStatus" use:enhance={() => {
                                    return async ({ result }) => {
                                        if (result.type === 'success') {
                                            await invalidateAll();
                                        }
                                    };
                                }}>
                                    <input type="hidden" name="applicationId" value={app.id} />
                                    <select 
                                        name="status" 
                                        class="status-select"
                                        style="--status-color: {statusInfo.color}"
                                        onchange={(e) => e.target.form.requestSubmit()}
                                    >
                                        {#each statusOptions as opt}
                                            <option value={opt.value} selected={app.status === opt.value}>
                                                {opt.emoji} {opt.label}
                                            </option>
                                        {/each}
                                    </select>
                                </form>
                            </td>
                            <td class="td-date">{formatDate(app.appliedAt)}</td>
                            <td class="td-date">{formatDate(app.interviewDate)}</td>
                            <td class="td-actions">
                                <button type="button" class="btn-icon" onclick={() => openEditModal(app)} title="Bearbeiten">
                                    ✏️
                                </button>
                                <button type="button" class="btn-icon btn-danger" onclick={() => confirmDelete(app.id)} title="Löschen">
                                    🗑️
                                </button>
                            </td>
                        </tr>

                        <!-- Delete Confirmation -->
                        {#if deleteConfirmId === app.id}
                            <tr class="delete-row">
                                <td colspan="6">
                                    <div class="delete-confirm">
                                        <span>Bewerbung bei <strong>{app.companyName}</strong> wirklich löschen?</span>
                                        <div class="delete-actions">
                                            <form method="POST" action="?/delete" use:enhance={() => {
                                                return async ({ result }) => {
                                                    deleteConfirmId = null;
                                                    if (result.type === 'success') {
                                                        await invalidateAll();
                                                    }
                                                };
                                            }}>
                                                <input type="hidden" name="applicationId" value={app.id} />
                                                <button type="submit" class="btn btn-danger-solid">Ja, löschen</button>
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
    {/if}
</div>

<!-- Modal -->
{#if showModal}
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <button type="button" class="modal-backdrop" onclick={closeModal} aria-label="Modal schliessen"></button>
    <div class="modal" role="dialog" aria-modal="true">
        <div class="modal-header">
            <h2>{editingApplication ? "Bewerbung bearbeiten" : "Neue Bewerbung"}</h2>
            <button type="button" class="btn-close-modal" onclick={closeModal} aria-label="Schliessen">✕</button>
        </div>
        <form 
            method="POST" 
            action={editingApplication ? "?/update" : "?/create"}
            use:enhance={() => {
                return async ({ result }) => {
                    if (result.type === 'success') {
                        closeModal();
                        await invalidateAll();
                    }
                };
            }}
        >
            {#if editingApplication}
                <input type="hidden" name="applicationId" value={editingApplication.id} />
            {/if}

            <div class="form-row">
                <div class="form-group">
                    <label for="companyName">Firma *</label>
                    <input 
                        type="text" 
                        id="companyName" 
                        name="companyName" 
                        placeholder="z.B. Swisscom, ABB, ..."
                        value={editingApplication?.companyName ?? ""}
                        required
                    />
                </div>

                <div class="form-group">
                    <label for="position">Position</label>
                    <input 
                        type="text" 
                        id="position" 
                        name="position" 
                        placeholder="z.B. Informatiker EFZ"
                        value={editingApplication?.position ?? ""}
                    />
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="status">Status</label>
                    <select id="status" name="status">
                        {#each statusOptions as opt}
                            <option value={opt.value} selected={editingApplication?.status === opt.value}>
                                {opt.emoji} {opt.label}
                            </option>
                        {/each}
                    </select>
                </div>

                <div class="form-group">
                    <label for="appliedAt">Bewerbungsdatum</label>
                    <input 
                        type="date" 
                        id="appliedAt" 
                        name="appliedAt"
                        value={toISODate(editingApplication?.appliedAt)}
                    />
                </div>
            </div>

            <div class="form-group">
                <label for="interviewDate">Gesprächstermin (optional)</label>
                <input 
                    type="date" 
                    id="interviewDate" 
                    name="interviewDate"
                    value={toISODate(editingApplication?.interviewDate)}
                />
            </div>

            <div class="form-group">
                <label for="notes">Notizen</label>
                <textarea 
                    id="notes" 
                    name="notes" 
                    rows="3"
                    placeholder="Ansprechpartner, Besonderheiten, ..."
                >{editingApplication?.notes ?? ""}</textarea>
            </div>

            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>
                    Abbrechen
                </button>
                <button type="submit" class="btn btn-primary">
                    {editingApplication ? "Speichern" : "Hinzufügen"}
                </button>
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

    /* Stats Grid */
    .stats-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
        gap: 0.75rem;
        margin-bottom: 1.5rem;
    }

    .stat-card {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        padding: 0.75rem;
        text-align: center;
    }

    .stat-value {
        display: block;
        font-size: 1.5rem;
        font-weight: 700;
    }

    .stat-label {
        font-size: 0.75rem;
        color: #7c6b80;
    }

    .stat-total .stat-value { color: #3b134f; }
    .stat-applied .stat-value { color: #3b82f6; }
    .stat-invited .stat-value { color: #f59e0b; }
    .stat-accepted .stat-value { color: #16a34a; }
    .stat-rejected .stat-value { color: #dc2626; }

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
        border-color: #3b134f;
        background: #fff;
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

    /* Empty State */
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

    /* Table */
    .table-wrapper {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        overflow: hidden;
        overflow-x: auto;
    }

    .applications-table {
        width: 100%;
        border-collapse: collapse;
        font-size: 0.9rem;
    }

    .applications-table th {
        background: #f8f5fc;
        padding: 0.75rem 1rem;
        text-align: left;
        font-weight: 600;
        color: #3b134f;
        border-bottom: 1px solid #e8e0f0;
        white-space: nowrap;
    }

    .applications-table td {
        padding: 0.75rem 1rem;
        border-bottom: 1px solid #f0e8f8;
        vertical-align: middle;
    }

    .app-row:hover {
        background: #faf8fc;
    }

    .td-company {
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }

    .company-name {
        font-weight: 500;
        color: #2d2141;
    }

    .has-notes {
        font-size: 0.85rem;
        cursor: help;
    }

    .td-position {
        color: #5e4a6d;
    }

    .td-date {
        color: #7c6b80;
        white-space: nowrap;
    }

    .td-actions {
        display: flex;
        gap: 0.25rem;
    }

    /* Status Select in Table */
    .status-select {
        padding: 0.35rem 0.5rem;
        border: 1px solid var(--status-color, #e8e0f0);
        border-radius: 0.5rem;
        background: #fff;
        font-size: 0.8rem;
        cursor: pointer;
        color: var(--status-color, #3b134f);
        font-weight: 500;
    }

    .status-select:focus {
        outline: none;
        box-shadow: 0 0 0 2px rgba(59, 19, 79, 0.2);
    }

    /* Delete Row */
    .delete-row td {
        background: #fef2f2;
        padding: 0;
    }

    .delete-confirm {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0.75rem 1rem;
        gap: 1rem;
        flex-wrap: wrap;
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
        background: #3b134f;
        color: #fff;
    }

    .btn-primary:hover {
        background: #4a1a63;
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
        max-width: 550px;
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

    .form-row {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 1rem;
    }

    @media (max-width: 500px) {
        .form-row {
            grid-template-columns: 1fr;
        }
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
    .form-group select,
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
    .form-group select:focus,
    .form-group textarea:focus {
        outline: none;
        border-color: #3b134f;
        background: #fff;
    }

    .form-group textarea {
        resize: vertical;
        min-height: 80px;
    }

    .modal-actions {
        display: flex;
        justify-content: flex-end;
        gap: 0.75rem;
        margin-top: 1.5rem;
    }

    @media (max-width: 600px) {
        .filter-bar {
            flex-direction: column;
        }
        .filter-group {
            width: 100%;
        }
        .applications-table {
            font-size: 0.8rem;
        }
        .applications-table th,
        .applications-table td {
            padding: 0.5rem;
        }
    }
</style>