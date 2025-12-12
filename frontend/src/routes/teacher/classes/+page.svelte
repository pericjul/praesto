<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";

    let { data, form } = $props();

    // State
    let showModal = $state(false);
    let editingClass = $state(null);
    let expandedClassId = $state(null);
    let deleteConfirmId = $state(null);
    let newStudentId = $state("");

    // Derived
    let classes = $derived(data?.classes ?? []);

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
            newStudentId = "";
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
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric"
        });
    }
</script>

<svelte:head>
    <title>Klassenverwaltung – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">🎓 Klassenverwaltung</h1>
            <p class="subtitle">Erstelle Klassen und ordne Schüler zu.</p>
        </div>
        <button type="button" class="btn btn-primary" onclick={openNewModal}>
            ➕ Neue Klasse
        </button>
    </header>

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if form?.success}
        <div class="alert alert-success">
            {#if form.action === "created"}Klasse wurde erstellt.
            {:else if form.action === "updated"}Klasse wurde aktualisiert.
            {:else if form.action === "deleted"}Klasse wurde gelöscht.
            {:else if form.action === "studentAdded"}Schüler wurde hinzugefügt.
            {:else if form.action === "studentRemoved"}Schüler wurde entfernt.
            {/if}
        </div>
    {/if}

    <div class="stats-bar">
        <div class="stat-card">
            <span class="stat-value">{classes.length}</span>
            <span class="stat-label">Klassen</span>
        </div>
        <div class="stat-card">
            <span class="stat-value">{classes.reduce((sum, c) => sum + (c.studentEmails?.length || 0), 0)}</span>
            <span class="stat-label">Schüler total</span>
        </div>
    </div>

    {#if classes.length === 0}
        <div class="empty-state">
            <div class="empty-icon">🎓</div>
            <h2>Noch keine Klassen</h2>
            <p>Erstelle deine erste Klasse und füge Schüler hinzu.</p>
            <button type="button" class="btn btn-primary" onclick={openNewModal}>
                Erste Klasse erstellen
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
                                {cls.studentEmails?.length || 0} Schüler · Erstellt am {formatDate(cls.createdAt)}
                            </span>
                        </div>
                        <div class="class-actions">
                            <button type="button" class="btn-icon" onclick={() => toggleExpand(cls.id)}
                                title={expandedClassId === cls.id ? "Schliessen" : "Schüler verwalten"}>
                                {expandedClassId === cls.id ? "▲" : "▼"}
                            </button>
                            <button type="button" class="btn-icon" onclick={() => openEditModal(cls)} title="Bearbeiten">
                                ✏️
                            </button>
                            <button type="button" class="btn-icon btn-danger" onclick={() => confirmDelete(cls.id)} title="Löschen">
                                🗑️
                            </button>
                        </div>
                    </div>

                    {#if deleteConfirmId === cls.id}
                        <div class="delete-confirm">
                            <span class="delete-confirm-text">Klasse <strong>{cls.name}</strong> wirklich löschen?</span>
                            <div class="delete-actions">
                                <form method="POST" action="?/delete" use:enhance={() => {
                                    return async ({ result }) => {
                                        deleteConfirmId = null;
                                        if (result.type === 'success') await invalidateAll();
                                    };
                                }}>
                                    <input type="hidden" name="classId" value={cls.id} />
                                    <button type="submit" class="btn btn-danger">Ja, löschen</button>
                                </form>
                                <button type="button" class="btn btn-secondary" onclick={cancelDelete}>Abbrechen</button>
                            </div>
                        </div>
                    {/if}

                    {#if expandedClassId === cls.id}
                        <div class="students-section">
                            <h4>Schüler in dieser Klasse</h4>
                            
                            <form method="POST" action="?/addStudent" class="add-student-form"
                                use:enhance={() => {
                                    return async ({ result }) => {
                                        if (result.type === 'success') {
                                            newStudentId = "";
                                            await invalidateAll();
                                        }
                                    };
                                }}>
                                <input type="hidden" name="classId" value={cls.id} />
                                <input type="email" name="email" placeholder="Schüler-Email eingeben"
                                    bind:value={newStudentId} class="filter-input" />
                                <button type="submit" class="btn btn-primary btn-sm" disabled={!newStudentId}>
                                    ➕ Hinzufügen
                                </button>
                            </form>

                            {#if cls.studentEmails && cls.studentEmails.length > 0}
                                <ul class="students-list">
                                    {#each cls.studentEmails as email}
                                        <li class="student-item">
                                            <span class="student-email">{email}</span>
                                            <form method="POST" action="?/removeStudent"
                                                use:enhance={() => {
                                                    return async ({ result }) => {
                                                        if (result.type === 'success') await invalidateAll();
                                                    };
                                                }}>
                                                <input type="hidden" name="classId" value={cls.id} />
                                                <input type="hidden" name="email" value={email} />
                                                <button type="submit" class="btn-remove" title="Entfernen">✕</button>
                                            </form>
                                        </li>
                                    {/each}
                                </ul>
                            {:else}
                                <p class="no-students">Noch keine Schüler in dieser Klasse.</p>
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
    <button type="button" class="modal-backdrop" onclick={closeModal} aria-label="Modal schliessen"></button>
    <div class="modal" role="dialog" aria-modal="true">
        <div class="modal-header">
            <h2>{editingClass ? "Klasse bearbeiten" : "Neue Klasse"}</h2>
            <button type="button" class="btn-close-modal" onclick={closeModal} aria-label="Schliessen">✕</button>
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
                    <label for="className">Klassenname</label>
                    <input type="text" id="className" name="name" required
                        placeholder="z.B. BM2a" value={editingClass?.name ?? ""} />
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>Abbrechen</button>
                <button type="submit" class="btn btn-primary">
                    {editingClass ? "Speichern" : "Erstellen"}
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
