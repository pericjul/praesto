<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";

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

    function daysUntil(date) {
        if (!date) return null;
        return Math.ceil((new Date(date) - new Date()) / (1000 * 60 * 60 * 24));
    }

    function isInterviewSoon(date) {
        const d = daysUntil(date);
        return d !== null && d >= 0 && d <= 7;
    }

    let upcomingInterviews = $derived(
        allApplications
            .filter((a) => isInterviewSoon(a.interviewDate) && !["REJECTED", "WITHDRAWN"].includes(a.status))
            .sort((a, b) => new Date(a.interviewDate) - new Date(b.interviewDate))
    );

    let statusOptions = $derived([
        { value: "PLANNED", label: $t('sapp.statusPlanned'), emoji: "📝", color: "#6b7280" },
        { value: "APPLIED", label: $t('sapp.statusApplied'), emoji: "📤", color: "#3b82f6" },
        { value: "INVITED", label: $t('sapp.statusInvited'), emoji: "📅", color: "#f59e0b" },
        { value: "INTERVIEW_DONE", label: $t('sapp.statusInterviewDone'), emoji: "✅", color: "#8b5cf6" },
        { value: "ACCEPTED", label: $t('sapp.statusAccepted'), emoji: "🎉", color: "#10b981" },
        { value: "REJECTED", label: $t('sapp.statusRejected'), emoji: "❌", color: "#ef4444" },
        { value: "WITHDRAWN", label: $t('sapp.statusWithdrawn'), emoji: "🔙", color: "#9ca3af" }
    ]);

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
    <title>{$t('sapp.headTitle')}</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">📊 {$t('sapp.title')}</h1>
            <p class="subtitle">{$t('sapp.subtitle')}</p>
        </div>
        <button type="button" class="btn btn-primary" onclick={openNewModal}>
            ➕ {$t('sapp.newApplication')}
        </button>
    </header>

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if form?.success}
        <div class="alert alert-success">
            {#if form.action === "created"}{$t('sapp.successCreated')}
            {:else if form.action === "updated"}{$t('sapp.successUpdated')}
            {:else if form.action === "deleted"}{$t('sapp.successDeleted')}
            {:else if form.action === "statusUpdated"}{$t('sapp.successStatusUpdated')}
            {/if}
        </div>
    {/if}

    <!-- Stats Cards -->
    <div class="stats-grid">
        <div class="stat-card"><span class="stat-value">{stats.total ?? 0}</span><span class="stat-label">{$t('sapp.statTotal')}</span></div>
        <div class="stat-card stat-info"><span class="stat-value">{stats.applied ?? 0}</span><span class="stat-label">{$t('sapp.statApplied')}</span></div>
        <div class="stat-card stat-warning"><span class="stat-value">{stats.invited ?? 0}</span><span class="stat-label">{$t('sapp.statInvited')}</span></div>
        <div class="stat-card stat-success"><span class="stat-value">{stats.accepted ?? 0}</span><span class="stat-label">{$t('sapp.statAccepted')}</span></div>
        <div class="stat-card stat-danger"><span class="stat-value">{stats.rejected ?? 0}</span><span class="stat-label">{$t('sapp.statRejected')}</span></div>
    </div>

    {#if allApplications.length > 0}
        <div class="filter-bar">
            <div class="filter-group">
                <input type="text" placeholder={$t('sapp.searchPlaceholder')} bind:value={searchQuery} class="filter-input" />
            </div>
            <div class="filter-group">
                <select bind:value={filterStatus} class="filter-select">
                    <option value="all">{$t('sapp.allStatus')}</option>
                    {#each statusOptions as opt}
                        <option value={opt.value}>{opt.emoji} {opt.label}</option>
                    {/each}
                </select>
            </div>
            <div class="filter-group">
                <select bind:value={sortBy} class="filter-select">
                    <option value="newest">{$t('sapp.sortNewest')}</option>
                    <option value="oldest">{$t('sapp.sortOldest')}</option>
                    <option value="company">{$t('sapp.sortCompany')}</option>
                    <option value="status">{$t('sapp.sortStatus')}</option>
                </select>
            </div>
            {#if searchQuery || filterStatus !== "all" || sortBy !== "newest"}
                <button type="button" class="btn-reset" onclick={resetFilters}>✕ {$t('sapp.resetFilters')}</button>
            {/if}
        </div>
    {/if}

    {#if allApplications.length === 0}
        <div class="empty-state">
            <div class="empty-icon">📋</div>
            <h2>{$t('sapp.emptyTitle')}</h2>
            <p>{$t('sapp.emptyText')}</p>
            <button type="button" class="btn btn-primary" onclick={openNewModal}>
                {$t('sapp.emptyButton')}
            </button>
        </div>
    {:else if applications().length === 0}
        <div class="empty-state">
            <div class="empty-icon">🔍</div>
            <h2>{$t('sapp.noResultsTitle')}</h2>
            <p>{$t('sapp.noResultsText')}</p>
            <button type="button" class="btn btn-secondary" onclick={resetFilters}>{$t('sapp.resetFilters')}</button>
        </div>
    {:else}
        {#if upcomingInterviews.length > 0}
            <div class="upcoming-banner">
                <span class="up-title">📅 {$t('sapp.upcomingTitle')}</span>
                <ul class="up-list">
                    {#each upcomingInterviews as app}
                        {@const d = daysUntil(app.interviewDate)}
                        <li>
                            <span class="up-company">{app.companyName}</span>
                            <span class="up-when">{d === 0 ? $t('sapp.today') : d === 1 ? $t('sapp.tomorrow') : $t('sapp.inDays').replace('{n}', d)} · {formatDate(app.interviewDate)}</span>
                        </li>
                    {/each}
                </ul>
            </div>
        {/if}
        <div class="table-wrapper">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>{$t('sapp.colCompany')}</th>
                        <th>{$t('sapp.colPosition')}</th>
                        <th>{$t('sapp.colStatus')}</th>
                        <th>{$t('sapp.colAppliedAt')}</th>
                        <th>{$t('sapp.colInterview')}</th>
                        <th>{$t('sapp.colActions')}</th>
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
                            <td class="text-muted" class:interview-soon={isInterviewSoon(app.interviewDate) && !["REJECTED","WITHDRAWN"].includes(app.status)}>
                                {formatDate(app.interviewDate)}
                            </td>
                            <td>
                                <div class="list-item-actions">
                                    <button type="button" class="btn-icon" onclick={() => openEditModal(app)} title={$t('sapp.edit')}>✏️</button>
                                    <button type="button" class="btn-icon btn-danger" onclick={() => confirmDelete(app.id)} title={$t('sapp.delete')}>🗑️</button>
                                </div>
                            </td>
                        </tr>
                        {#if deleteConfirmId === app.id}
                            <tr class="delete-row">
                                <td colspan="6">
                                    <div class="delete-confirm">
                                        <span class="delete-confirm-text">{$t('sapp.deleteConfirmPre')} <strong>{app.companyName}</strong> {$t('sapp.deleteConfirmPost')}</span>
                                        <div class="delete-actions">
                                            <form method="POST" action="?/delete" use:enhance={() => {
                                                return async ({ result }) => {
                                                    deleteConfirmId = null;
                                                    if (result.type === 'success') await invalidateAll();
                                                };
                                            }}>
                                                <input type="hidden" name="applicationId" value={app.id} />
                                                <button type="submit" class="btn btn-danger">{$t('sapp.confirmYes')}</button>
                                            </form>
                                            <button type="button" class="btn btn-secondary" onclick={cancelDelete}>{$t('sapp.cancel')}</button>
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
    <button type="button" class="modal-backdrop" onclick={closeModal} aria-label={$t('sapp.closeModal')}></button>
    <div class="modal" role="dialog" aria-modal="true">
        <div class="modal-header">
            <h2>{editingApplication ? $t('sapp.editApplication') : $t('sapp.newApplication')}</h2>
            <button type="button" class="btn-close-modal" onclick={closeModal} aria-label={$t('sapp.close')}>✕</button>
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
                        <label for="companyName">{$t('sapp.labelCompany')}</label>
                        <input type="text" id="companyName" name="companyName" required
                            placeholder={$t('sapp.placeholderCompany')} value={editingApplication?.companyName ?? ""} />
                    </div>
                    <div class="form-group">
                        <label for="position">{$t('sapp.labelPosition')}</label>
                        <input type="text" id="position" name="position"
                            placeholder={$t('sapp.placeholderPosition')} value={editingApplication?.position ?? ""} />
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="status">{$t('sapp.labelStatus')}</label>
                        <select id="status" name="status">
                            {#each statusOptions as opt}
                                <option value={opt.value} selected={editingApplication?.status === opt.value}>
                                    {opt.emoji} {opt.label}
                                </option>
                            {/each}
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="appliedAt">{$t('sapp.labelAppliedAt')}</label>
                        <input type="date" id="appliedAt" name="appliedAt" value={toISODate(editingApplication?.appliedAt)} />
                    </div>
                </div>

                <div class="form-group">
                    <label for="interviewDate">{$t('sapp.labelInterviewDate')}</label>
                    <input type="date" id="interviewDate" name="interviewDate" value={toISODate(editingApplication?.interviewDate)} />
                </div>

                <div class="form-group">
                    <label for="notes">{$t('sapp.labelNotes')}</label>
                    <textarea id="notes" name="notes" placeholder={$t('sapp.placeholderNotes')}>{editingApplication?.notes ?? ""}</textarea>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>{$t('sapp.cancel')}</button>
                <button type="submit" class="btn btn-primary">{editingApplication ? $t('sapp.save') : $t('sapp.add')}</button>
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

    .upcoming-banner {
        background: var(--color-warning-bg, #fffbeb);
        border: 1px solid var(--color-warning-border, #fcd34d);
        border-radius: var(--radius-lg, 0.75rem);
        padding: 0.85rem 1rem;
        margin-bottom: 1rem;
    }

    .up-title {
        font-weight: 700;
        color: #92400e;
        display: block;
        margin-bottom: 0.4rem;
    }

    .up-list {
        margin: 0;
        padding: 0;
        list-style: none;
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
    }

    .up-list li {
        display: flex;
        justify-content: space-between;
        gap: 1rem;
        font-size: 0.9rem;
        color: #78600e;
    }

    .up-company {
        font-weight: 600;
    }

    td.interview-soon {
        color: #b45309 !important;
        font-weight: 700;
    }
</style>
