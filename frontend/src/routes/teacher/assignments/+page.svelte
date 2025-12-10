<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";

    // Svelte 5
    let { data, form } = $props();

    // State
    let showModal = $state(false);
    let editingAssignment = $state(null);
    let deleteConfirmId = $state(null);
    let selectedType = $state("");

    // Aufgabentypen
    const assignmentTypes = [
        { value: "AI_INTERVIEW", label: "🤖 KI-Bewerbungsgespräch", desc: "Übe ein Bewerbungsgespräch mit der KI", hasDuration: true },
        { value: "DOCUMENT_UPLOAD", label: "📄 Dokument einreichen", desc: "Lade ein Dokument hoch (PDF, Word)", hasDuration: false },
        { value: "SELF_REFLECTION", label: "✍️ Selbstreflexion", desc: "Schreibe eine Reflexion zu einem Thema", hasDuration: false },
        { value: "VIDEO_PITCH", label: "🎥 Video-Bewerbung", desc: "Nimm ein kurzes Bewerbungsvideo auf", hasDuration: true },
        { value: "RESEARCH", label: "🔍 Recherche", desc: "Recherchiere zu einem Thema", hasDuration: false }
    ];

    // Derived
    let assignments = $derived(data?.assignments ?? []);
    let classes = $derived(data?.classes ?? []);

    // Prüfen ob aktueller Typ Dauer braucht
    function typeHasDuration(type) {
        const t = assignmentTypes.find(at => at.value === type);
        return t?.hasDuration ?? false;
    }

    // Modal öffnen für neue Aufgabe
    function openNewModal() {
        editingAssignment = null;
        selectedType = "";
        showModal = true;
    }

    // Modal öffnen zum Bearbeiten
    function openEditModal(assignment) {
        editingAssignment = assignment;
        selectedType = assignment.type || "";
        showModal = true;
    }

    // Modal schliessen
    function closeModal() {
        showModal = false;
        editingAssignment = null;
        selectedType = "";
    }

    // Löschen bestätigen
    function confirmDelete(id) {
        deleteConfirmId = id;
    }

    function cancelDelete() {
        deleteConfirmId = null;
    }

    // Klassenname holen
    function getClassName(classId) {
        const cls = classes.find(c => c.id === classId);
        return cls?.name ?? "Unbekannt";
    }

    // Typ-Label holen
    function getTypeLabel(type) {
        const t = assignmentTypes.find(at => at.value === type);
        return t?.label ?? type;
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

    // Datum für datetime-local Input formatieren
    function formatDateTimeLocal(date) {
        if (!date) return getDefaultDeadline();
        return new Date(date).toISOString().slice(0, 16);
    }

    // Default Deadline (in 7 Tagen)
    function getDefaultDeadline() {
        const d = new Date();
        d.setDate(d.getDate() + 7);
        return d.toISOString().slice(0, 16);
    }
</script>

<svelte:head>
    <title>Aufgabenverwaltung – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">📚 Aufgabenverwaltung</h1>
            <p class="subtitle">
                Erstelle und verwalte Aufgaben für deine Klassen.
            </p>
        </div>
        <button type="button" class="btn btn-primary" onclick={openNewModal}>
            ➕ Neue Aufgabe
        </button>
    </header>

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if form?.success}
        <div class="alert alert-success">
            {#if form.action === "created"}
                Aufgabe wurde erstellt.
            {:else if form.action === "updated"}
                Aufgabe wurde aktualisiert.
            {:else if form.action === "deleted"}
                Aufgabe wurde gelöscht.
            {/if}
        </div>
    {/if}

    <!-- Stats -->
    <div class="stats-bar">
        <div class="stat-item">
            <span class="stat-value">{assignments.length}</span>
            <span class="stat-label">Aufgaben</span>
        </div>
        <div class="stat-item">
            <span class="stat-value">{classes.length}</span>
            <span class="stat-label">Klassen</span>
        </div>
    </div>

    {#if classes.length === 0}
        <div class="empty-state">
            <div class="empty-icon">🎓</div>
            <h2>Keine Klassen vorhanden</h2>
            <p>Erstelle zuerst eine Klasse, bevor du Aufgaben hinzufügen kannst.</p>
            <a href="/teacher/classes" class="btn btn-primary">Klasse erstellen</a>
        </div>
    {:else if assignments.length === 0}
        <div class="empty-state">
            <div class="empty-icon">📚</div>
            <h2>Noch keine Aufgaben</h2>
            <p>Erstelle deine erste Aufgabe für deine Schüler.</p>
            <button type="button" class="btn btn-primary" onclick={openNewModal}>
                Erste Aufgabe erstellen
            </button>
        </div>
    {:else}
        <div class="classes-list">
            {#each assignments as assignment}
                <div class="class-card">
                    <div class="class-header">
                        <div class="class-info">
                            <a href="/teacher/assignments/{assignment.id}" class="class-name-link">
                                <h3 class="class-name">{assignment.title}</h3>
                            </a>
                            <span class="class-meta">
                                <span class="type-badge">{getTypeLabel(assignment.type)}</span>
                                · {getClassName(assignment.classId)} · Deadline: {formatDate(assignment.dueDate)}
                                {#if assignment.durationMin}
                                    · {assignment.durationMin} Min
                                {/if}
                            </span>
                        </div>
                        <div class="class-actions">
                            <a 
                                href="/teacher/assignments/{assignment.id}" 
                                class="btn-icon"
                                title="Abgaben anzeigen"
                            >
                                📥
                            </a>
                            <button 
                                type="button" 
                                class="btn-icon" 
                                onclick={() => openEditModal(assignment)}
                                title="Bearbeiten"
                            >
                                ✏️
                            </button>
                            <button 
                                type="button" 
                                class="btn-icon btn-danger" 
                                onclick={() => confirmDelete(assignment.id)}
                                title="Löschen"
                            >
                                🗑️
                            </button>
                        </div>
                    </div>

                    <!-- Delete Confirmation -->
                    {#if deleteConfirmId === assignment.id}
                        <div class="delete-confirm">
                            <span>Aufgabe <strong>{assignment.title}</strong> wirklich löschen?</span>
                            <div class="delete-actions">
                                <form method="POST" action="?/delete" use:enhance={() => {
                                    return async ({ result }) => {
                                        deleteConfirmId = null;
                                        if (result.type === 'success') {
                                            await invalidateAll();
                                        }
                                    };
                                }}>
                                    <input type="hidden" name="assignmentId" value={assignment.id} />
                                    <button type="submit" class="btn btn-danger-solid">Ja, löschen</button>
                                </form>
                                <button type="button" class="btn btn-secondary" onclick={cancelDelete}>Abbrechen</button>
                            </div>
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
            <h2>{editingAssignment ? "Aufgabe bearbeiten" : "Neue Aufgabe"}</h2>
            <button type="button" class="btn-close-modal" onclick={closeModal} aria-label="Schliessen">✕</button>
        </div>
        <form 
            method="POST" 
            action={editingAssignment ? "?/update" : "?/create"}
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

            <div class="form-group">
                <label for="title">Titel *</label>
                <input 
                    type="text" 
                    id="title" 
                    name="title" 
                    placeholder="z.B. Bewerbungsgespräch üben"
                    value={editingAssignment?.title ?? ""}
                    required
                />
            </div>

            <div class="form-group">
                <label for="type">Aufgabentyp *</label>
                <select id="type" name="type" required bind:value={selectedType}>
                    <option value="">-- Typ wählen --</option>
                    {#each assignmentTypes as t}
                        <option value={t.value}>{t.label}</option>
                    {/each}
                </select>
                <small class="form-hint">Der Typ bestimmt, wie Schüler die Aufgabe bearbeiten.</small>
            </div>

            <div class="form-group">
                <label for="description">Beschreibung</label>
                <textarea 
                    id="description" 
                    name="description" 
                    rows="2"
                    placeholder="Optionale Beschreibung..."
                >{editingAssignment?.description ?? ""}</textarea>
            </div>

            <div class="form-group">
                <label for="classId">Klasse *</label>
                <select id="classId" name="classId" required>
                    <option value="">-- Klasse wählen --</option>
                    {#each classes as cls}
                        <option value={cls.id} selected={editingAssignment?.classId === cls.id}>{cls.name}</option>
                    {/each}
                </select>
            </div>

            {#if typeHasDuration(selectedType)}
                <div class="form-group">
                    <label for="durationMin">Dauer in Minuten</label>
                    <input 
                        type="number" 
                        id="durationMin" 
                        name="durationMin" 
                        placeholder="z.B. 30"
                        value={editingAssignment?.durationMin ?? ""}
                        min="1"
                    />
                </div>
            {/if}

            <div class="form-group">
                <label for="dueDate">Deadline *</label>
                <input 
                    type="datetime-local" 
                    id="dueDate" 
                    name="dueDate"
                    value={formatDateTimeLocal(editingAssignment?.dueDate)}
                    required
                />
            </div>

            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>
                    Abbrechen
                </button>
                <button type="submit" class="btn btn-primary">
                    {editingAssignment ? "Speichern" : "Erstellen"}
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

    .class-name-link {
        text-decoration: none;
        color: inherit;
    }

    .class-name-link:hover .class-name {
        color: #3b134f;
        text-decoration: underline;
    }

    .class-meta {
        font-size: 0.85rem;
        color: #7c6b80;
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 0.25rem;
    }

    .type-badge {
        display: inline-block;
        padding: 0.2rem 0.5rem;
        background: #e8e0f0;
        color: #3b134f;
        border-radius: 0.4rem;
        font-size: 0.8rem;
        font-weight: 500;
    }

    .class-actions {
        display: flex;
        gap: 0.25rem;
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
        content: "📚";
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
        overflow-y: auto;
        max-height: calc(90vh - 70px);
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

    .form-group input,
    .form-group select,
    .form-group textarea {
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

    .form-group input::placeholder,
    .form-group textarea::placeholder {
        color: #a89bb5;
    }

    .form-group input:focus,
    .form-group select:focus,
    .form-group textarea:focus {
        outline: none;
        border-color: #3b134f;
        background: #fff;
        box-shadow: 0 0 0 4px rgba(59, 19, 79, 0.1);
    }

    .form-group textarea {
        resize: vertical;
        min-height: 60px;
    }

    .form-hint {
        display: block;
        margin-top: 0.4rem;
        font-size: 0.8rem;
        color: #7c6b80;
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
</style>