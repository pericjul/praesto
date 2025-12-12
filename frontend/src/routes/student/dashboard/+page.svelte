<script>
    export let data;

    const dashboard = data.dashboard ?? {};

    const {
        studentName = "Student",
        openAssignmentsCount = 0,
        openAssignments = [],
        lastSessionStartedAt,
        totalSessionsCount = 0,
        badgesCount = 0,
        earnedBadgeIcons = [],
        applicationsCount = 0,
        notifications = []
    } = dashboard;

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
            day: "2-digit", month: "2-digit", year: "numeric"
        });
    }

    function getDaysUntil(dueDate) {
        if (!dueDate) return null;
        return Math.ceil((new Date(dueDate) - new Date()) / (1000 * 60 * 60 * 24));
    }

    function isUrgent(dueDate) {
        const days = getDaysUntil(dueDate);
        return days !== null && days >= 0 && days <= 2;
    }

    function isOverdue(dueDate) {
        const days = getDaysUntil(dueDate);
        return days !== null && days < 0;
    }
</script>

<svelte:head>
    <title>Dashboard – Praesto</title>
</svelte:head>

<div class="dashboard">
    <!-- ORIGINAL HERO -->
    <section class="hero">
        <div class="hero-left">
            <p class="hero-eyebrow">Willkommen zurück 👋</p>
            <h1 class="hero-title">Hi {studentName}</h1>
            <p class="hero-subtitle">
                Hier findest du alles, um dich optimal auf Bewerbungen vorzubereiten.
            </p>
            <div class="hero-actions">
                <a href="/student/assignments" class="btn btn-primary">Zu den Aufgaben</a>
                <a href="/student/sessions" class="btn btn-ghost">KI-Training starten</a>
            </div>
        </div>

        <div class="hero-right">
            <div class="hero-stat">
                <span class="hero-stat-label">Offene Aufgaben</span>
                <span class="hero-stat-value">{openAssignmentsCount}</span>
            </div>
            <div class="hero-stat">
                <span class="hero-stat-label">KI-Trainings</span>
                <span class="hero-stat-value">{totalSessionsCount}</span>
            </div>
            <a href="/student/badges" class="hero-stat hero-stat-link">
                <span class="hero-stat-label">🏅 Badges</span>
                <span class="hero-stat-value">{badgesCount}</span>
            </a>
        </div>
    </section>

    <!-- NEUER HAUPTBEREICH: 2 klare Spalten -->
    <div class="main-grid">
        
        <!-- LINKE SPALTE: Aufgaben-Fokus -->
        <section class="tasks-section">
            <div class="section-header">
                <h2>📚 Nächste Aufgaben</h2>
                <a href="/student/assignments" class="link-all">Alle ansehen →</a>
            </div>

            {#if openAssignments.length === 0}
                <div class="empty-state">
                    <span class="empty-icon">🎉</span>
                    <p>Keine offenen Aufgaben!</p>
                    <span class="empty-hint">Du hast alles erledigt. Zeit für ein Training?</span>
                    <a href="/student/sessions" class="btn btn-accent">KI-Training starten</a>
                </div>
            {:else}
                <div class="task-list">
                    {#each openAssignments.slice(0, 4) as assignment, i}
                        <a href="/student/assignments/{assignment.id}" 
                           class="task-card" 
                           class:urgent={isUrgent(assignment.dueDate)} 
                           class:overdue={isOverdue(assignment.dueDate)}>
                            
                            <div class="task-rank">{i + 1}</div>
                            
                            <div class="task-info">
                                <span class="task-title">{assignment.title}</span>
                                <div class="task-meta">
                                    <span class="task-type" style="--type-color: {getTypeInfo(assignment.type).color}">
                                        {getTypeInfo(assignment.type).label}
                                    </span>
                                    {#if assignment.durationMin}
                                        <span class="task-duration">⏱️ {assignment.durationMin} Min</span>
                                    {/if}
                                </div>
                            </div>

                            <div class="task-deadline">
                                {#if isOverdue(assignment.dueDate)}
                                    <span class="deadline overdue">⚠️ Überfällig</span>
                                {:else if isUrgent(assignment.dueDate)}
                                    <span class="deadline urgent">🔥 {getDaysUntil(assignment.dueDate) === 0 ? "Heute!" : "Morgen!"}</span>
                                {:else if assignment.dueDate}
                                    <span class="deadline">{formatDate(assignment.dueDate)}</span>
                                {:else}
                                    <span class="deadline muted">Kein Datum</span>
                                {/if}
                            </div>
                        </a>
                    {/each}
                </div>

                {#if openAssignments.length > 4}
                    <a href="/student/assignments" class="btn btn-outline show-more">
                        +{openAssignments.length - 4} weitere Aufgaben
                    </a>
                {/if}
            {/if}
        </section>

        <!-- RECHTE SPALTE: Feedback + Schnellzugriff -->
        <aside class="sidebar">
            
            <!-- Feedback (nur wenn vorhanden) -->
            {#if notifications.length > 0}
                <section class="card feedback-card">
                    <h3>🔔 Neues Feedback</h3>
                    <div class="feedback-list">
                        {#each notifications.slice(0, 3) as notification}
                            <a href="/student/assignments/{notification.assignmentId}" class="feedback-item">
                                <span class="feedback-icon">{notification.icon ?? "💬"}</span>
                                <div class="feedback-content">
                                    <span class="feedback-title">{notification.title}</span>
                                    {#if notification.grade != null}
                                        <span class="feedback-grade">Note: {notification.grade}</span>
                                    {/if}
                                </div>
                            </a>
                        {/each}
                    </div>
                    {#if notifications.length > 3}
                        <span class="more-count">+{notifications.length - 3} weitere</span>
                    {/if}
                </section>
            {/if}

            <!-- Schnellzugriff -->
            <section class="card">
                <h3>⚡ Schnellzugriff</h3>
                <nav class="quick-links">
                    <a href="/student/sessions" class="quick-link">
                        <span class="quick-icon">🤖</span>
                        <span>KI-Training</span>
                        <span class="quick-arrow">→</span>
                    </a>
                    <a href="/student/applications" class="quick-link">
                        <span class="quick-icon">💼</span>
                        <span>Bewerbungen</span>
                        <span class="quick-count">{applicationsCount}</span>
                    </a>
                    <a href="/student/notes" class="quick-link">
                        <span class="quick-icon">📝</span>
                        <span>Notizen</span>
                        <span class="quick-arrow">→</span>
                    </a>
                    <a href="/student/badges" class="quick-link">
                        <span class="quick-icon">🏅</span>
                        <span>Badges</span>
                        <span class="quick-count">{badgesCount}</span>
                    </a>
                </nav>
            </section>

            <!-- Tipp des Tages (optional, nice-to-have) -->
            <section class="card tip-card">
                <h3>💡 Tipp</h3>
                <p>Übe regelmässig mit dem KI-Training – so wirst du sicherer im echten Bewerbungsgespräch!</p>
            </section>

        </aside>
    </div>
</div>

<style>
    .dashboard {
        max-width: 1100px;
        margin: 0 auto;
        padding: 1.5rem 1rem 3rem;
    }

    /* ===== ORIGINAL HERO ===== */
    .hero {
        background: linear-gradient(135deg, #2F124D 0%, #5a2d6e 50%, #c97d3c 100%);
        border-radius: 1.5rem;
        padding: 2rem 2.5rem;
        margin-bottom: 2rem;
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 2rem;
        flex-wrap: wrap;
    }

    .hero-left { flex: 1; min-width: 280px; }

    .hero-eyebrow {
        margin: 0 0 0.25rem;
        color: rgba(255, 255, 255, 0.75);
        font-size: 0.9rem;
    }

    .hero-title {
        margin: 0 0 0.5rem;
        color: #fff;
        font-size: 1.8rem;
        font-weight: 700;
    }

    .hero-subtitle {
        margin: 0 0 1.25rem;
        color: rgba(255, 255, 255, 0.85);
        font-size: 0.95rem;
    }

    .hero-actions {
        display: flex;
        gap: 0.75rem;
        flex-wrap: wrap;
    }

    .hero-right {
        display: flex;
        gap: 0.75rem;
    }

    .hero-stat {
        background: rgba(255, 255, 255, 0.15);
        backdrop-filter: blur(10px);
        border-radius: 0.75rem;
        padding: 0.75rem 1.25rem;
        text-align: center;
        min-width: 90px;
    }

    .hero-stat-link {
        text-decoration: none;
        transition: all 0.2s;
    }

    .hero-stat-link:hover {
        background: rgba(255, 255, 255, 0.25);
        transform: translateY(-2px);
    }

    .hero-stat-label {
        display: block;
        font-size: 0.75rem;
        color: rgba(255, 255, 255, 0.8);
        margin-bottom: 0.25rem;
    }

    .hero-stat-value {
        display: block;
        font-size: 1.5rem;
        font-weight: 700;
        color: #fff;
    }

    /* Hero Buttons */
    .btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 0.5rem 1.25rem;
        border-radius: 999px;
        font-size: 0.9rem;
        font-weight: 500;
        text-decoration: none;
        cursor: pointer;
        border: none;
        transition: all 0.15s;
    }

    .btn-primary {
        background: #fbe4b2;
        color: #2F124D;
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
    }

    .btn-primary:hover {
        background: #ffe2a2;
        transform: translateY(-1px);
    }

    .btn-ghost {
        background: transparent;
        border: 1px solid rgba(255, 255, 255, 0.5);
        color: #fff;
    }

    .btn-ghost:hover {
        background: rgba(255, 255, 255, 0.1);
    }

    .btn-accent {
        background: #2F124D;
        color: #fff;
        padding: 0.6rem 1.5rem;
        margin-top: 1rem;
    }

    .btn-accent:hover {
        background: #3d1a63;
    }

    .btn-outline {
        background: transparent;
        border: 1px solid #e0d6eb;
        color: #2F124D;
        width: 100%;
        margin-top: 1rem;
    }

    .btn-outline:hover {
        background: #f8f5fc;
    }

    /* ===== MAIN GRID ===== */
    .main-grid {
        display: grid;
        grid-template-columns: 1fr 320px;
        gap: 2rem;
    }

    /* ===== TASKS SECTION ===== */
    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
    }

    .section-header h2 {
        margin: 0;
        font-size: 1.1rem;
        color: #2d2141;
    }

    .link-all {
        font-size: 0.85rem;
        color: #2F124D;
        text-decoration: none;
    }

    .link-all:hover {
        text-decoration: underline;
    }

    .task-list {
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
    }

    .task-card {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1rem 1.25rem;
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        text-decoration: none;
        transition: all 0.15s;
    }

    .task-card:hover {
        border-color: #2F124D;
        box-shadow: 0 4px 12px rgba(47, 18, 77, 0.1);
    }

    .task-card.urgent {
        border-color: #fcd34d;
        background: #fffbeb;
    }

    .task-card.overdue {
        border-color: #fca5a5;
        background: #fef2f2;
    }

    .task-rank {
        width: 28px;
        height: 28px;
        background: #2F124D;
        color: #fff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 0.8rem;
        flex-shrink: 0;
    }

    .task-info {
        flex: 1;
        min-width: 0;
    }

    .task-title {
        display: block;
        font-weight: 600;
        color: #2d2141;
        margin-bottom: 0.25rem;
    }

    .task-meta {
        display: flex;
        gap: 0.75rem;
        font-size: 0.8rem;
        color: #7c6b80;
    }

    .task-type {
        padding: 0.1rem 0.5rem;
        background: color-mix(in srgb, var(--type-color) 15%, transparent);
        color: var(--type-color);
        border-radius: 0.25rem;
        font-size: 0.75rem;
    }

    .task-deadline {
        flex-shrink: 0;
        font-size: 0.85rem;
        color: #6b7280;
    }

    .deadline.urgent { color: #d97706; font-weight: 600; }
    .deadline.overdue { color: #dc2626; font-weight: 600; }
    .deadline.muted { color: #9ca3af; }

    .empty-state {
        text-align: center;
        padding: 3rem 2rem;
        background: #faf8fc;
        border: 2px dashed #e0d6eb;
        border-radius: 1rem;
    }

    .empty-icon { font-size: 2.5rem; margin-bottom: 0.5rem; display: block; }
    .empty-state p { margin: 0; color: #2d2141; font-weight: 500; }
    .empty-hint { font-size: 0.85rem; color: #7c6b80; display: block; margin-top: 0.25rem; }

    /* ===== SIDEBAR ===== */
    .sidebar {
        display: flex;
        flex-direction: column;
        gap: 1.25rem;
    }

    .card {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 1rem;
        padding: 1.25rem;
    }

    .card h3 {
        margin: 0 0 1rem;
        font-size: 0.95rem;
        color: #2d2141;
    }

    /* Feedback Card */
    .feedback-card {
        background: #f0fdf4;
        border-color: #bbf7d0;
    }

    .feedback-list {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .feedback-item {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 0.6rem 0.75rem;
        background: #fff;
        border-radius: 0.5rem;
        text-decoration: none;
        transition: all 0.15s;
    }

    .feedback-item:hover {
        background: #f0fdf4;
    }

    .feedback-icon { font-size: 1.1rem; }

    .feedback-content { flex: 1; }

    .feedback-title {
        display: block;
        font-size: 0.85rem;
        color: #2d2141;
        font-weight: 500;
    }

    .feedback-grade {
        font-size: 0.75rem;
        color: #2F124D;
        font-weight: 600;
    }

    .more-count {
        display: block;
        margin-top: 0.5rem;
        font-size: 0.75rem;
        color: #6b7280;
        text-align: center;
    }

    /* Quick Links */
    .quick-links {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .quick-link {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 0.75rem;
        background: #faf8fc;
        border-radius: 0.5rem;
        text-decoration: none;
        transition: all 0.15s;
    }

    .quick-link:hover {
        background: #f0ebf5;
    }

    .quick-icon { font-size: 1.2rem; }

    .quick-link span:nth-child(2) {
        flex: 1;
        font-size: 0.9rem;
        color: #2d2141;
        font-weight: 500;
    }

    .quick-arrow {
        color: #2F124D;
        font-weight: 600;
    }

    .quick-count {
        background: #2F124D;
        color: #fff;
        padding: 0.15rem 0.5rem;
        border-radius: 999px;
        font-size: 0.75rem;
        font-weight: 600;
    }

    /* Tip Card */
    .tip-card {
        background: #fffbeb;
        border-color: #fde68a;
    }

    .tip-card p {
        margin: 0;
        font-size: 0.85rem;
        color: #78600e;
        line-height: 1.5;
    }

    /* ===== RESPONSIVE ===== */
    @media (max-width: 900px) {
        .main-grid {
            grid-template-columns: 1fr;
        }
    }

    @media (max-width: 600px) {
        .hero {
            padding: 1.5rem;
        }

        .hero-right {
            width: 100%;
            justify-content: space-between;
        }

        .hero-stat {
            flex: 1;
            min-width: unset;
            padding: 0.6rem 0.75rem;
        }

        .task-card {
            flex-wrap: wrap;
        }

        .task-deadline {
            width: 100%;
            padding-left: 44px;
            margin-top: 0.25rem;
        }
    }
</style>
