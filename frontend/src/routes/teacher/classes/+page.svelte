<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";

    // Svelte 5
    let { data, form } = $props();

    // State
    let showModal = $state(false);
    let editingClass = $state(null);
    let expandedClassId = $state(null);
    let deleteConfirmId = $state(null);
    let newStudentId = $state("");

    // Derived
    let classes = $derived(data?.classes ?? []);

    // Modal öffnen für neue Klasse
    function openNewModal() {
        editingClass = null;
        showModal = true;
    }

    // Modal öffnen zum Bearbeiten
    function openEditModal(cls) {
        editingClass = cls;
        showModal = true;
    }

    // Modal schliessen
    function closeModal() {
        showModal = false;
        editingClass = null;
    }

    // Klasse expandieren/kollabieren (für Schülerliste)
    function toggleExpand(classId) {
        if (expandedClassId === classId) {
            expandedClassId = null;
        } else {
            expandedClassId = classId;
            newStudentId = "";
        }
    }

    // Löschen bestätigen
    function confirmDelete(classId) {
        deleteConfirmId = classId;
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
</script>

<svelte:head>
    <title>Klassenverwaltung – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">🎓 Klassenverwaltung</h1>
            <p class="subtitle">
                Erstelle Klassen und ordne Schüler zu.
            </p>
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
            {#if form.action === "created"}
                Klasse wurde erstellt.
            {:else if form.action === "updated"}
                Klasse wurde aktualisiert.
            {:else if form.action === "deleted"}
                Klasse wurde gelöscht.
            {:else if form.action === "studentAdded"}
                Schüler wurde hinzugefügt.
            {:else if form.action === "studentRemoved"}
                Schüler wurde entfernt.
            {/if}
        </div>
    {/if}

    <!-- Stats -->
    <div class="stats-bar">
        <div class="stat-item">
            <span class="stat-value">{classes.length}</span>
            <span class="stat-label">Klassen</span>
        </div>
        <div class="stat-item">
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
                                {cls.studentEmails?.length || 0} Schüler
                                · Erstellt am {formatDate(cls.createdAt)}
                            </span>
                        </div>
                        <div class="class-actions">
                            <button 
                                type="button" 
                                class="btn-icon" 
                                onclick={() => toggleExpand(cls.id)}
                                title={expandedClassId === cls.id ? "Schliessen" : "Schüler verwalten"}
                            >
                                {expandedClassId === cls.id ? "▲" : "▼"}
                            </button>
                            <button 
                                type="button" 
                                class="btn-icon" 
                                onclick={() => openEditModal(cls)}
                                title="Bearbeiten"
                            >
                                ✏️
                            </button>
                            <button 
                                type="button" 
                                class="btn-icon btn-danger" 
                                onclick={() => confirmDelete(cls.id)}
                                title="Löschen"
                            >
                                🗑️
                            </button>
                        </div>
                    </div>

                    <!-- Delete Confirmation -->
                    {#if deleteConfirmId === cls.id}
                        <div class="delete-confirm">
                            <span>Klasse <strong>{cls.name}</strong> wirklich löschen?</span>
                            <div class="delete-actions">
                                <form method="POST" action="?/delete" use:enhance={() => {
                                    return async ({ result }) => {
                                        deleteConfirmId = null;
                                        if (result.type === 'success') {
                                            await invalidateAll();
                                        }
                                    };
                                }}>
                                    <input type="hidden" name="classId" value={cls.id} />
                                    <button type="submit" class="btn btn-danger-solid">Ja, löschen</button>
                                </form>
                                <button type="button" class="btn btn-secondary" onclick={cancelDelete}>Abbrechen</button>
                            </div>
                        </div>
                    {/if}

                    <!-- Expanded: Schülerliste -->
                    {#if expandedClassId === cls.id}
                        <div class="students-section">
                            <h4>Schüler in dieser Klasse</h4>
                            
                            <!-- Schüler hinzufügen (per Email) -->
                            <form 
                                method="POST" 
                                action="?/addStudent" 
                                class="add-student-form"
                                use:enhance={() => {
                                    return async ({ result }) => {
                                        if (result.type === 'success') {
                                            newStudentId = "";
                                            await invalidateAll();
                                        }
                                    };
                                }}
                            >
                                <input type="hidden" name="classId" value={cls.id} />
                                <input 
                                    type="email" 
                                    name="email" 
                                    placeholder="Schüler-Email eingeben (z.B. max@schule.ch)" 
                                    bind:value={newStudentId}
                                    class="input-student"
                                />
                                <button type="submit" class="btn btn-primary btn-sm" disabled={!newStudentId}>
                                    ➕ Hinzufügen
                                </button>
                            </form>

                            {#if cls.studentEmails && cls.studentEmails.length > 0}
                                <ul class="students-list">
                                    {#each cls.studentEmails as email}
                                        <li class="student-item">
                                            <span class="student-email">{email}</span>
                                            <form 
                                                method="POST" 
                                                action="?/removeStudent"
                                                use:enhance={() => {
                                                    return async ({ result }) => {
                                                        if (result.type === 'success') {
                                                            await invalidateAll();
                                                        }
                                                    };
                                                }}
                                            >
                                                <input type="hidden" name="classId" value={cls.id} />
                                                <input type="hidden" name="email" value={email} />
                                                <button type="submit" class="btn-remove" title="Entfernen">
                                                    ✕
                                                </button>
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
        <form 
            method="POST" 
            action={editingClass ? "?/update" : "?/create"}
            use:enhance={() => {
                return async ({ result }) => {
                    if (result.type === 'success') {
                        closeModal();
                        await invalidateAll();
                    }
                };
            }}
        >
            {#if editingClass}
                <input type="hidden" name="classId" value={editingClass.id} />
            {/if}

            <div class="form-group">
                <label for="name">Klassenname *</label>
                <input 
                    type="text" 
                    id="name" 
                    name="name" 
                    placeholder="z.B. INF2024a, KV2023b"
                    value={editingClass?.name ?? ""}
                    required
                />
            </div>

            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>
                    Abbrechen
                </button>
                <button type="submit" class="btn btn-primary">
                    {editingClass ? "Speichern" : "Erstellen"}
                </button>
            </div>
        </form>
    </div>
{/if}

<style>
    .page-wrapper {
        max-width: 900px;
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

    /* Stats Bar */
    .stats-bar {
        display: flex;
        gap: 1.5rem;
        margin-bottom: 1.5rem;
        padding: 1rem 1.25rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
    }

    .stat-item {
        display: flex;
        flex-direction: column;
    }

    .stat-value {
        font-size: 1.5rem;
        font-weight: 700;
        color: #3b134f;
    }

    .stat-label {
        font-size: 0.8rem;
        color: #7c6b80;
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

    /* Classes List */
    .classes-list {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .class-card {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        overflow: hidden;
        transition: box-shadow 0.2s;
    }

    .class-card:hover {
        box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    }

    .class-card.expanded {
        border-color: #3b134f;
    }

    .class-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1rem 1.25rem;
        gap: 1rem;
    }

    .class-info {
        flex: 1;
    }

    .class-name {
        margin: 0;
        font-size: 1.1rem;
        font-weight: 600;
        color: #2d2141;
    }

    .class-meta {
        font-size: 0.85rem;
        color: #7c6b80;
    }

    .class-actions {
        display: flex;
        gap: 0.25rem;
    }

    /* Students Section */
    .students-section {
        padding: 1rem 1.25rem;
        background: #faf8fc;
        border-top: 1px solid #e8e0f0;
    }

    .students-section h4 {
        margin: 0 0 1rem;
        font-size: 0.95rem;
        color: #3b134f;
    }

    .add-student-form {
        display: flex;
        gap: 0.5rem;
        margin-bottom: 1rem;
    }

    .input-student,
    .select-student {
        flex: 1;
        padding: 0.5rem 0.75rem;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        background: #fff;
        font-family: inherit;
    }

    .input-student:focus,
    .select-student:focus {
        outline: none;
        border-color: #3b134f;
    }

    .no-students-hint {
        margin: 0;
        padding: 0.75rem;
        background: #fef9e7;
        border: 1px solid #f5e6b3;
        border-radius: 0.5rem;
        font-size: 0.85rem;
        color: #7a6420;
    }

    .students-list {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .student-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.6rem 0.75rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        margin-bottom: 0.5rem;
    }

    .student-info {
        display: flex;
        flex-direction: column;
        gap: 0.1rem;
    }

    .student-name {
        font-size: 0.9rem;
        font-weight: 500;
        color: #2d2141;
    }

    .student-email {
        font-size: 0.8rem;
        color: #7c6b80;
    }

    .student-id {
        font-size: 0.85rem;
        color: #4c3a3a;
        font-family: monospace;
        word-break: break-all;
    }

    .btn-remove {
        background: none;
        border: none;
        color: #dc2626;
        cursor: pointer;
        padding: 0.25rem 0.5rem;
        font-size: 0.9rem;
        border-radius: 0.25rem;
    }

    .btn-remove:hover {
        background: #fef2f2;
    }

    .no-students {
        margin: 0;
        color: #7c6b80;
        font-size: 0.9rem;
        font-style: italic;
    }

    /* Delete Confirmation */
    .delete-confirm {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0.75rem 1.25rem;
        background: #fef2f2;
        border-top: 1px solid #fecaca;
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

    .btn-sm {
        padding: 0.4rem 0.75rem;
        font-size: 0.85rem;
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
        background: rgba(45, 33, 65, 0.6);
        backdrop-filter: blur(4px);
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
        background: linear-gradient(180deg, #ffffff 0%, #faf8fc 100%);
        border-radius: 1.25rem;
        width: 90%;
        max-width: 450px;
        max-height: 90vh;
        overflow: hidden;
        z-index: 999;
        box-shadow: 
            0 25px 50px -12px rgba(59, 19, 79, 0.25),
            0 0 0 1px rgba(59, 19, 79, 0.05);
        display: block;
        visibility: visible;
        opacity: 1;
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1.25rem 1.5rem;
        background: linear-gradient(135deg, #3b134f 0%, #5a2d6e 100%);
        color: #fff;
    }

    .modal-header h2 {
        margin: 0;
        font-size: 1.2rem;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }

    .modal-header h2::before {
        content: "🎓";
    }

    .btn-close-modal {
        background: rgba(255,255,255,0.15);
        border: none;
        font-size: 1rem;
        cursor: pointer;
        color: #fff;
        padding: 0.4rem 0.6rem;
        border-radius: 0.5rem;
        transition: background 0.2s;
    }

    .btn-close-modal:hover {
        background: rgba(255,255,255,0.25);
    }

    .modal form {
        padding: 1.5rem;
    }

    .form-group {
        margin-bottom: 1.25rem;
    }

    .form-group label {
        display: block;
        margin-bottom: 0.5rem;
        font-size: 0.9rem;
        font-weight: 600;
        color: #3b134f;
    }

    .form-group input {
        width: 100%;
        padding: 0.85rem 1rem;
        border: 2px solid #e8e0f0;
        border-radius: 0.75rem;
        font-size: 1rem;
        font-family: inherit;
        background: #fff;
        box-sizing: border-box;
        transition: all 0.2s;
    }

    .form-group input::placeholder {
        color: #a89bb5;
    }

    .form-group input:focus {
        outline: none;
        border-color: #3b134f;
        background: #fff;
        box-shadow: 0 0 0 4px rgba(59, 19, 79, 0.1);
    }

    .modal-actions {
        display: flex;
        justify-content: flex-end;
        gap: 0.75rem;
        padding-top: 0.5rem;
        border-top: 1px solid #f0e8f8;
        margin-top: 0.5rem;
        padding-top: 1.25rem;
    }

    .modal-actions .btn {
        padding: 0.7rem 1.5rem;
        font-size: 0.95rem;
        border-radius: 0.75rem;
    }

    .modal-actions .btn-secondary {
        background: #f5f3f7;
        color: #5e4a6d;
        border: 1px solid #e8e0f0;
    }

    .modal-actions .btn-secondary:hover {
        background: #ebe7ef;
    }

    .modal-actions .btn-primary {
        box-shadow: 0 4px 12px rgba(59, 19, 79, 0.3);
    }

    .modal-actions .btn-primary:hover {
        transform: translateY(-1px);
        box-shadow: 0 6px 16px rgba(59, 19, 79, 0.35);
    }

    @media (max-width: 600px) {
        .class-header {
            flex-direction: column;
            align-items: flex-start;
        }
        .class-actions {
            align-self: flex-end;
        }
        .add-student-form {
            flex-direction: column;
        }
    }
</style>