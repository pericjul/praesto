<script>
    let { data } = $props();

    let classes = $derived(data?.classes ?? []);
    let assignments = $derived(data?.assignments ?? []);
    let recentSubmissions = $derived(data?.recentSubmissions ?? []);
    let stats = $derived(data?.stats ?? {});
    let user = $derived(data?.user ?? {});

    const assignmentTypes = {
        AI_INTERVIEW: { label: "🤖 KI-Interview", color: "#8b5cf6" },
        DOCUMENT_UPLOAD: { label: "📄 Dokument", color: "#3b82f6" },
        SELF_REFLECTION: { label: "✍️ Reflexion", color: "#10b981" },
        VIDEO_PITCH: { label: "🎥 Video", color: "#f59e0b" },
        RESEARCH: { label: "🔍 Recherche", color: "#6366f1" }
    };

    function getTypeInfo(type) {
        return assignmentTypes[type] ?? { label: type, color: "#6b7280" };
    }

    function formatDate(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        });
    }

    function formatRelativeTime(date) {
        if (!date) return "";
        const now = new Date();
        const then = new Date(date);
        const diffMs = now - then;
        const diffMin = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        if (diffMin < 1) return "gerade eben";
        if (diffMin < 60) return `vor ${diffMin} Min.`;
        if (diffHours < 24) return `vor ${diffHours} Std.`;
        if (diffDays < 7) return `vor ${diffDays} Tagen`;
        return formatDate(date);
    }

    function shortenEmail(email) {
        return email?.split("@")[0] ?? "";
    }

    function getStatusColor(status) {
        if (status === "SUBMITTED") return "#3b82f6";
        if (status === "REVIEWED") return "#10b981";
        return "#6b7280";
    }

    function formatStatus(status) {
        const map = {
            "SUBMITTED": "Neu",
            "IN_REVIEW": "In Prüfung",
            "REVIEWED": "Bewertet"
        };
        return map[status] ?? status;
    }
</script>

<svelte:head>
    <title>Lehrer Dashboard – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">👋 Willkommen, {user.name ?? "Lehrperson"}!</h1>
            <p class="subtitle">Hier ist deine Übersicht für heute.</p>
        </div>
    </header>

    <!-- Stats Cards -->
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-icon">📚</div>
            <div class="stat-content">
                <span class="stat-value">{stats.totalClasses}</span>
                <span class="stat-label">Klassen</span>
            </div>
            <a href="/teacher/classes" class="stat-link">Verwalten →</a>
        </div>

        <div class="stat-card">
            <div class="stat-icon">👥</div>
            <div class="stat-content">
                <span class="stat-value">{stats.totalStudents}</span>
                <span class="stat-label">Schüler</span>
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-icon">📝</div>
            <div class="stat-content">
                <span class="stat-value">{stats.totalAssignments}</span>
                <span class="stat-label">Aufgaben</span>
            </div>
            <a href="/teacher/assignments" class="stat-link">Alle ansehen →</a>
        </div>

        <div class="stat-card highlight">
            <div class="stat-icon">📥</div>
            <div class="stat-content">
                <span class="stat-value">{stats.pendingSubmissions}</span>
                <span class="stat-label">Ausstehend</span>
            </div>
            {#if stats.pendingSubmissions > 0}
                <span class="stat-badge">Feedback nötig</span>
            {/if}
        </div>
    </div>

    <!-- Quick Actions -->
    <section class="section">
        <h2>⚡ Schnellzugriff</h2>
        <div class="quick-actions">
            <a href="/teacher/assignments?new=1" class="action-card">
                <span class="action-icon">➕</span>
                <span class="action-label">Neue Aufgabe erstellen</span>
            </a>
            <a href="/teacher/classes" class="action-card">
                <span class="action-icon">👥</span>
                <span class="action-label">Klassen verwalten</span>
            </a>
            <a href="/teacher/assignments" class="action-card">
                <span class="action-icon">📝</span>
                <span class="action-label">Alle Aufgaben ansehen</span>
            </a>
        </div>
    </section>

    <!-- Recent Submissions -->
    <section class="section">
        <div class="section-header">
            <h2>📥 Neueste Abgaben</h2>
            <a href="/teacher/assignments" class="section-link">Alle Aufgaben →</a>
        </div>

        {#if recentSubmissions.length === 0}
            <div class="empty-state">
                <p>Noch keine Abgaben eingegangen.</p>
            </div>
        {:else}
            <div class="submissions-list">
                {#each recentSubmissions as sub}
                    <a href="/teacher/assignments/{sub.assignmentId}" class="submission-item">
                        <div class="submission-main">
                            <span class="submission-student">{shortenEmail(sub.studentEmail)}</span>
                            <span class="submission-assignment">{sub.assignmentTitle}</span>
                            <span 
                                class="type-badge" 
                                style="background: {getTypeInfo(sub.assignmentType).color}15; color: {getTypeInfo(sub.assignmentType).color}"
                            >
                                {getTypeInfo(sub.assignmentType).label}
                            </span>
                        </div>
                        <div class="submission-meta">
                            <span class="submission-time">{formatRelativeTime(sub.submittedAt)}</span>
                            <span 
                                class="status-badge"
                                style="background: {getStatusColor(sub.status)}15; color: {getStatusColor(sub.status)}"
                            >
                                {formatStatus(sub.status)}
                            </span>
                            <span class="go-arrow">→</span>
                        </div>
                    </a>
                {/each}
            </div>
        {/if}
    </section>

    <!-- Classes Overview -->
    <section class="section">
        <div class="section-header">
            <h2>📚 Meine Klassen</h2>
            <a href="/teacher/classes" class="section-link">Alle ansehen →</a>
        </div>

        {#if classes.length === 0}
            <div class="empty-state">
                <p>Du hast noch keine Klassen erstellt.</p>
                <a href="/teacher/classes" class="btn btn-primary">Klasse erstellen</a>
            </div>
        {:else}
            <div class="classes-grid">
                {#each classes.slice(0, 4) as schoolClass}
                    <a href="/teacher/classes" class="class-card">
                        <h3 class="class-name">{schoolClass.name}</h3>
                        <div class="class-meta">
                            <span>👥 {schoolClass.studentEmails?.length ?? 0} Schüler</span>
                        </div>
                    </a>
                {/each}
            </div>
        {/if}
    </section>
</div>

<style>
    .page-wrapper {
        max-width: 1100px;
        margin: 0 auto;
        padding: 1.5rem 1rem 3rem;
    }

    .page-header {
        margin-bottom: 2rem;
    }

    .title {
        font-size: 1.8rem;
        font-weight: 700;
        margin: 0;
        color: #2d2141;
    }

    .subtitle {
        margin: 0.3rem 0 0;
        color: #6b647a;
        font-size: 1rem;
    }

    /* Stats Grid */
    .stats-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 1rem;
        margin-bottom: 2rem;
    }

    .stat-card {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 1rem;
        padding: 1.25rem;
        display: flex;
        align-items: center;
        gap: 1rem;
        position: relative;
    }

    .stat-card.highlight {
        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
        border-color: #fcd34d;
    }

    .stat-icon {
        font-size: 2rem;
    }

    .stat-content {
        display: flex;
        flex-direction: column;
    }

    .stat-value {
        font-size: 1.75rem;
        font-weight: 700;
        color: #3b134f;
        line-height: 1;
    }

    .stat-label {
        font-size: 0.85rem;
        color: #7c6b80;
        margin-top: 0.25rem;
    }

    .stat-link {
        position: absolute;
        bottom: 0.75rem;
        right: 1rem;
        font-size: 0.8rem;
        color: #7c3aed;
        text-decoration: none;
    }

    .stat-badge {
        position: absolute;
        top: -0.5rem;
        right: -0.5rem;
        background: #dc2626;
        color: #fff;
        font-size: 0.7rem;
        padding: 0.25rem 0.5rem;
        border-radius: 1rem;
        font-weight: 600;
    }

    /* Sections */
    .section {
        margin-bottom: 2rem;
    }

    .section h2 {
        font-size: 1.15rem;
        margin: 0 0 1rem;
        color: #2d2141;
    }

    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
    }

    .section-header h2 {
        margin: 0;
    }

    .section-link {
        font-size: 0.9rem;
        color: #7c3aed;
        text-decoration: none;
    }

    /* Quick Actions */
    .quick-actions {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
        gap: 1rem;
    }

    .action-card {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 0.5rem;
        padding: 1.5rem 1rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 1rem;
        text-decoration: none;
        transition: all 0.2s ease;
    }

    .action-card:hover {
        border-color: #3b134f;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(59, 19, 79, 0.1);
    }

    .action-icon {
        font-size: 1.5rem;
    }

    .action-label {
        font-size: 0.9rem;
        color: #2d2141;
        text-align: center;
    }

    /* Submissions List */
    .submissions-list {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .submission-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.75rem 1rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        text-decoration: none;
        transition: all 0.15s ease;
    }

    .submission-item:hover {
        border-color: #3b134f;
        background: #faf8fc;
    }

    .submission-main {
        display: flex;
        align-items: center;
        gap: 0.75rem;
    }

    .submission-student {
        font-weight: 600;
        color: #2d2141;
    }

    .submission-assignment {
        color: #6b647a;
        font-size: 0.9rem;
    }

    .submission-meta {
        display: flex;
        align-items: center;
        gap: 0.75rem;
    }

    .submission-time {
        font-size: 0.8rem;
        color: #9ca3af;
    }

    .go-arrow {
        color: #7c3aed;
        font-weight: 600;
    }

    .type-badge, .status-badge {
        padding: 0.2rem 0.5rem;
        border-radius: 0.4rem;
        font-size: 0.75rem;
        font-weight: 500;
    }

    /* Classes Grid */
    .classes-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 1rem;
    }

    .class-card {
        padding: 1.25rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        text-decoration: none;
        transition: all 0.2s ease;
    }

    .class-card:hover {
        border-color: #3b134f;
        box-shadow: 0 4px 12px rgba(59, 19, 79, 0.08);
    }

    .class-name {
        margin: 0 0 0.5rem;
        font-size: 1.1rem;
        color: #2d2141;
    }

    .class-meta {
        font-size: 0.85rem;
        color: #7c6b80;
    }

    /* Empty State */
    .empty-state {
        padding: 2rem;
        text-align: center;
        background: #faf8fc;
        border: 1px dashed #e8e0f0;
        border-radius: 0.75rem;
        color: #7c6b80;
    }

    .empty-state p {
        margin: 0 0 1rem;
    }

    .btn {
        display: inline-block;
        padding: 0.6rem 1.25rem;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        font-weight: 500;
        text-decoration: none;
        border: none;
        cursor: pointer;
    }

    .btn-primary {
        background: #3b134f;
        color: #fff;
    }

    @media (max-width: 600px) {
        .submission-item {
            flex-direction: column;
            align-items: flex-start;
            gap: 0.5rem;
        }

        .submission-main {
            flex-wrap: wrap;
        }

        .submission-meta {
            width: 100%;
            justify-content: space-between;
        }
    }
</style>