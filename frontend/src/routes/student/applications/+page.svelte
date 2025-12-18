<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";

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

    let applications = $derived(() => {
        let filtered = [...allApplications];

        if (searchQuery.trim()) {
            const query = searchQuery.toLowerCase();
            filtered = filtered.filter(app => 
                app.companyName?.toLowerCase().includes(query) ||
                app.position?.toLowerCase().includes(query) ||
                app.notes?.toLowerCase().includes(query)
            );
        }

        if (filterStatus !== "all") {
            filtered = filtered.filter(app => app.status === filterStatus);
        }

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

    function resetFilters() {
        searchQuery = "";
        filterStatus = "all";
        sortBy = "newest";
    }

    function openNewModal() {
        editingApplication = null;
        showModal = true;
    }

    function openEditModal(app) {
        editingApplication = app;
        showModal = true;
    }

    function closeModal() {
        showModal = false;
        editingApplication = null;
    }

    function confirmDelete(appId) {
        deleteConfirmId = appId;
    }

    function cancelDelete() {
        deleteConfirmId = null;
    }

    function formatDate(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", year: "numeric"
        });
    }

    function toISODate(date) {
        if (!date) return "";
        try {
            return new Date(date).toISOString().split("T")[0];
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
            <p class="subtitle">Behalte den Überblick über deine Bewerbungen und deren Status.</p>
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
            {#if form.action === "created"}Bewerbung wurde hinzugefügt.
            {:else if form.action === "updated"}Bewerbung wurde aktualisiert.
            {:else if form.action === "deleted"}Bewerbung wurde gelöscht.
            {:else if form.action === "statusUpdated"}Status wurde geändert.
            {/if}
        </div>
    {/if}

    <!-- Stats Cards -->
    <div class="stats-grid">
        <div class="stat-card"><span class="stat-value">{stats.total ?? 0}</span><span class="stat-label">Total</span></div>
        <div class="stat-card stat-info"><span class="stat-value">{stats.applied ?? 0}</span><span class="stat-label">Beworben</span></div>
        <div class="stat-card stat-warning"><span class="stat-value">{stats.invited ?? 0}</span><span class="stat-label">Eingeladen</span></div>
        <div class="stat-card stat-success"><span class="stat-value">{stats.accepted ?? 0}</span><span class="stat-label">Zusagen</span></div>
        <div class="stat-card stat-danger"><span class="stat-value">{stats.rejected ?? 0}</span><span class="stat-label">Absagen</span></div>
    </div>

    {#if allApplications.length > 0}
        <div class="filter-bar">
            <div class="filter-group">
                <input type="text" placeholder="🔍 Suchen..." bind:value={searchQuery} class="filter-input" />
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
                <button type="button" class="btn-reset" onclick={resetFilters}>✕ Filter zurücksetzen</button>
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
            <button type="button" class="btn btn-secondary" onclick={resetFilters}>Filter zurücksetzen</button>
        </div>
    {:else}
        <div class="table-wrapper">
            <table class="data-table">
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
                        <tr>
                            <td>
                                <div class="td-company">
                                    <span class="company-name">{app.companyName}</span>
                                    {#if app.notes}<span class="has-notes" title={app.notes}>📝</span>{/if}
                                </div>
                            </td>
                            <td class="text-muted">{app.position || "-"}</td>
                            <td>
                                <form method="POST" action="?/updateStatus" use:enhance={() => {
                                    return async ({ result }) => {
                                        if (result.type === 'success') await invalidateAll();
                                    };
                                }}>
                                    <input type="hidden" name="applicationId" value={app.id} />
                                    <select name="status" class="status-select" style="--status-color: {statusInfo.color}"
                                        onchange={(e) => e.target.form.requestSubmit()}>
                                        {#each statusOptions as opt}
                                            <option value={opt.value} selected={app.status === opt.value}>
                                                {opt.emoji} {opt.label}
                                            </option>
                                        {/each}
                                    </select>
                                </form>
                            </td>
                            <td class="text-muted">{formatDate(app.appliedAt)}</td>
                            <td class="text-muted">{formatDate(app.interviewDate)}</td>
                            <td>
                                <div class="list-item-actions">
                                    <button type="button" class="btn-icon" onclick={() => openEditModal(app)} title="Bearbeiten">✏️</button>
                                    <button type="button" class="btn-icon btn-danger" onclick={() => confirmDelete(app.id)} title="Löschen">🗑️</button>
                                </div>
                            </td>
                        </tr>
                        {#if deleteConfirmId === app.id}
                            <tr class="delete-row">
                                <td colspan="6">
                                    <div class="delete-confirm">
                                        <span class="delete-confirm-text">Bewerbung bei <strong>{app.companyName}</strong> löschen?</span>
                                        <div class="delete-actions">
                                            <form method="POST" action="?/delete" use:enhance={() => {
                                                return async ({ result }) => {
                                                    deleteConfirmId = null;
                                                    if (result.type === 'success') await invalidateAll();
                                                };
                                            }}>
                                                <input type="hidden" name="applicationId" value={app.id} />
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
    {/if}
</div>

<!-- Modal -->
{#if showModal}
    <button type="button" class="modal-backdrop" onclick={closeModal} aria-label="Modal schliessen"></button>
    <div class="modal" role="dialog" aria-modal="true">
        <div class="modal-header">
            <h2>{editingApplication ? "Bewerbung bearbeiten" : "Neue Bewerbung"}</h2>
            <button type="button" class="btn-close-modal" onclick={closeModal} aria-label="Schliessen">✕</button>
        </div>

        <form method="POST" action={editingApplication ? "?/update" : "?/create"}
            use:enhance={() => {
                return async ({ result }) => {
                    if (result.type === 'success') {
                        closeModal();
                        await invalidateAll();
                    }
                };
            }}>
            <div class="modal-body">
                {#if editingApplication}
                    <input type="hidden" name="applicationId" value={editingApplication.id} />
                {/if}

                <div class="form-row">
                    <div class="form-group">
                        <label for="companyName">Firma *</label>
                        <input type="text" id="companyName" name="companyName" required
                            placeholder="z.B. Google" value={editingApplication?.companyName ?? ""} />
                    </div>
                    <div class="form-group">
                        <label for="position">Position</label>
                        <input type="text" id="position" name="position"
                            placeholder="z.B. Software Engineer" value={editingApplication?.position ?? ""} />
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
                        <label for="appliedAt">Beworben am</label>
                        <input type="date" id="appliedAt" name="appliedAt" value={toISODate(editingApplication?.appliedAt)} />
                    </div>
                </div>

                <div class="form-group">
                    <label for="interviewDate">Gespräch am</label>
                    <input type="date" id="interviewDate" name="interviewDate" value={toISODate(editingApplication?.interviewDate)} />
                </div>

                <div class="form-group">
                    <label for="notes">Notizen</label>
                    <textarea id="notes" name="notes" placeholder="Zusätzliche Informationen...">{editingApplication?.notes ?? ""}</textarea>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>Abbrechen</button>
                <button type="submit" class="btn btn-primary">{editingApplication ? "Speichern" : "Hinzufügen"}</button>
            </div>
        </form>
    </div>
{/if}

<style>
    .stats-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
        gap: var(--space-md);
        margin-bottom: var(--space-xl);
    }

    .stat-card.stat-info { border-color: var(--color-info-border); background: var(--color-info-bg); }
    .stat-card.stat-info .stat-value { color: var(--color-info); }
    .stat-card.stat-warning { border-color: var(--color-warning-border); background: var(--color-warning-bg); }
    .stat-card.stat-warning .stat-value { color: var(--color-warning); }
    .stat-card.stat-success { border-color: var(--color-success-border); background: var(--color-success-bg); }
    .stat-card.stat-success .stat-value { color: var(--color-success); }
    .stat-card.stat-danger { border-color: var(--color-error-border); background: var(--color-error-bg); }
    .stat-card.stat-danger .stat-value { color: var(--color-error); }

    .td-company {
        display: flex;
        align-items: center;
        gap: var(--space-sm);
    }

    .company-name {
        font-weight: 500;
        color: var(--color-text-secondary);
    }

    .has-notes {
        font-size: var(--font-size-sm);
        cursor: help;
    }

    .status-select {
        padding: var(--space-xs) var(--space-sm);
        border: 1px solid var(--status-color, var(--color-border-input));
        border-radius: var(--radius-md);
        background: var(--color-bg-card);
        font-size: var(--font-size-xs);
        cursor: pointer;
        color: var(--status-color, var(--color-primary));
        font-weight: 500;
    }

    .status-select:focus {
        outline: none;
        box-shadow: 0 0 0 2px rgba(47, 18, 77, 0.2);
    }

    .delete-row td {
        padding: 0 !important;
        background: var(--color-error-bg);
    }

    @media (max-width: 768px) {
        .data-table { font-size: var(--font-size-xs); }
        .data-table th:nth-child(5),
        .data-table td:nth-child(5) { display: none; }
    }
</style>
