<script>
    import { t, locale } from "$lib/i18n";
    import { get } from "svelte/store";

    let { data } = $props();

    let classes = $derived(data?.classes ?? []);
    let assignments = $derived(data?.assignments ?? []);
    let recentSubmissions = $derived(data?.recentSubmissions ?? []);
    let stats = $derived(data?.stats ?? {});
    let user = $derived(data?.user ?? {});

    // Nur Submissions die noch Feedback brauchen
    let pendingSubmissions = $derived(
        recentSubmissions.filter(s => !s.teacherFeedback)
    );

    // Anstehende Deadlines der eigenen Aufgaben
    let upcomingDeadlines = $derived(
        assignments
            .filter(a => a.dueDate && new Date(a.dueDate) >= new Date())
            .sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate))
            .slice(0, 4)
    );

    function formatDeadline(date) {
        if (!date) return "";
        const localeMap = { de: "de-CH", en: "en-GB", fr: "fr-CH", it: "it-CH" };
        const days = Math.ceil((new Date(date) - new Date()) / 86400000);
        const tr = get(t);
        if (days <= 0) return tr("tdash.dueToday");
        if (days === 1) return tr("tdash.dueTomorrow");
        if (days <= 7) return `${tr("tdash.dueInPre")} ${days} ${tr("tdash.dueInPost")}`;
        return new Date(date).toLocaleDateString(localeMap[get(locale)] ?? "de-CH", {
            day: "2-digit", month: "2-digit"
        });
    }

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

    function formatRelativeTime(date) {
        if (!date) return "";
        const tr = get(t);
        const localeMap = { de: "de-CH", en: "en-GB", fr: "fr-CH", it: "it-CH" };
        const now = new Date();
        const then = new Date(date);
        const diffMs = now - then;
        const diffMin = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        const join = (pre, n, post) => [pre, n, post].filter(Boolean).join(" ");

        if (diffMin < 1) return tr("tdash.timeNow");
        if (diffMin < 60) return join(tr("tdash.timeMinPre"), diffMin, tr("tdash.timeMinPost"));
        if (diffHours < 24) return join(tr("tdash.timeHoursPre"), diffHours, tr("tdash.timeHoursPost"));
        if (diffDays < 7) return join(tr("tdash.timeDaysPre"), diffDays, tr("tdash.timeDaysPost"));
        return new Date(date).toLocaleDateString(localeMap[get(locale)] ?? "de-CH");
    }

    function shortenEmail(email) {
        return email?.split("@")[0] ?? "";
    }

    function getInitial(email) {
        return shortenEmail(email).charAt(0).toUpperCase();
    }
</script>

<svelte:head>
    <title>{$t('tdash.pageTitle')}</title>
</svelte:head>

<div class="page-wrapper">
    <!-- Header -->
    <header class="page-header">
        <h1>{$t('tdash.greeting')}, {user.firstName ?? $t('tdash.fallbackName')} 👋</h1>
        <p class="subtitle">{$t('tdash.subtitle')}</p>
    </header>

    <!-- Wichtigste Info: Feedback nötig -->
    {#if stats.pendingSubmissions > 0}
        <div class="alert-banner">
            <div class="alert-content">
                <span class="alert-icon">📬</span>
                <div>
                    <strong>{stats.pendingSubmissions} {stats.pendingSubmissions === 1 ? $t('tdash.alertWaitingOne') : $t('tdash.alertWaitingMany')} {$t('tdash.alertOnFeedback')}</strong>
                    <p>{$t('tdash.alertHint')}</p>
                </div>
            </div>
            <a href="/teacher/assignments" class="alert-action">{$t('tdash.alertAction')}</a>
        </div>
    {/if}

    <!-- Stats -->
    <div class="stats-row">
        <div class="stat-card">
            <span class="stat-value">{stats.totalClasses}</span>
            <span class="stat-label">{$t('tdash.statClasses')}</span>
        </div>
        <div class="stat-card">
            <span class="stat-value">{stats.totalStudents}</span>
            <span class="stat-label">{$t('tdash.statStudents')}</span>
        </div>
        <div class="stat-card">
            <span class="stat-value">{stats.totalAssignments}</span>
            <span class="stat-label">{$t('tdash.statAssignments')}</span>
        </div>
        <div class="stat-card" class:highlight={stats.pendingSubmissions > 0}>
            <span class="stat-value">{stats.pendingSubmissions}</span>
            <span class="stat-label">{$t('tdash.statPending')}</span>
        </div>
    </div>

    <!-- Main Grid -->
    <div class="main-grid">
        <!-- Left: Pending Submissions -->
        <section class="section">
            <div class="section-header">
                <h2>{$t('tdash.submissionsTitle')}</h2>
                {#if pendingSubmissions.length > 0}
                    <a href="/teacher/assignments" class="section-link">{$t('tdash.viewAll')}</a>
                {/if}
            </div>

            {#if pendingSubmissions.length === 0}
                <div class="empty-state success">
                    <span class="empty-icon">✅</span>
                    <p>{$t('tdash.allGraded')}</p>
                </div>
            {:else}
                <div class="submissions-list">
                    {#each pendingSubmissions.slice(0, 5) as sub}
                        <a href="/teacher/assignments/{sub.assignmentId}" class="submission-card">
                            <div class="submission-avatar">{getInitial(sub.studentEmail)}</div>
                            <div class="submission-info">
                                <span class="submission-student">{shortenEmail(sub.studentEmail)}</span>
                                <span class="submission-assignment">{sub.assignmentTitle}</span>
                            </div>
                            <div class="submission-meta">
                                <span class="submission-type" style="--type-color: {getTypeInfo(sub.assignmentType).color}">
                                    {getTypeInfo(sub.assignmentType).label}
                                </span>
                                <span class="submission-time">{formatRelativeTime(sub.submittedAt)}</span>
                            </div>
                            <span class="submission-arrow">→</span>
                        </a>
                    {/each}
                </div>
            {/if}
        </section>

        <!-- Right: Quick Actions & Classes -->
        <aside class="sidebar">
            <!-- Quick Actions -->
            <div class="card">
                <h3>{$t('tdash.quickAccess')}</h3>
                <nav class="quick-links">
                    <a href="/teacher/assignments?new=1" class="quick-link">
                        <span class="quick-icon">➕</span>
                        <span>{$t('tdash.quickNewAssignment')}</span>
                    </a>
                    <a href="/teacher/classes" class="quick-link">
                        <span class="quick-icon">👥</span>
                        <span>{$t('tdash.quickManageClasses')}</span>
                    </a>
                    <a href="/teacher/assignments" class="quick-link">
                        <span class="quick-icon">📝</span>
                        <span>{$t('tdash.quickAllAssignments')}</span>
                    </a>
                </nav>
            </div>

            <!-- Anstehende Deadlines -->
            <div class="card">
                <h3>{$t('tdash.deadlinesTitle')}</h3>
                {#if upcomingDeadlines.length === 0}
                    <p class="deadline-empty">{$t('tdash.noDeadlines')}</p>
                {:else}
                    <div class="deadlines-list">
                        {#each upcomingDeadlines as a}
                            <a href="/teacher/assignments/{a.id}" class="deadline-item">
                                <span class="deadline-title">{a.title}</span>
                                <span class="deadline-date">{formatDeadline(a.dueDate)}</span>
                            </a>
                        {/each}
                    </div>
                {/if}
            </div>

            <!-- Classes -->
            <div class="card">
                <div class="card-header">
                    <h3>{$t('tdash.myClasses')}</h3>
                    <a href="/teacher/classes" class="card-link">{$t('tdash.allLink')}</a>
                </div>
                {#if classes.length === 0}
                    <div class="empty-mini">
                        <p>{$t('tdash.noClasses')}</p>
                        <a href="/teacher/classes" class="btn-small">{$t('tdash.create')}</a>
                    </div>
                {:else}
                    <div class="classes-list">
                        {#each classes.slice(0, 4) as schoolClass}
                            <div class="class-item">
                                <span class="class-name">{schoolClass.name}</span>
                                <span class="class-count">{schoolClass.studentIds?.length ?? 0} {$t('tdash.classStudents')}</span>
                            </div>
                        {/each}
                    </div>
                {/if}
            </div>
        </aside>
    </div>
</div>

<style>
    .page-wrapper {
        max-width: 1100px;
        margin: 0 auto;
        padding: 1.5rem 1rem 3rem;
    }

    /* Header */
    .page-header {
        margin-bottom: 1.5rem;
    }

    .page-header h1 {
        font-size: 1.75rem;
        font-weight: 700;
        margin: 0;
        color: #2f124d;
    }

    .subtitle {
        margin: 0.25rem 0 0;
        color: #6b647a;
    }

    /* Alert Banner */
    .alert-banner {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 1rem;
        padding: 1rem 1.25rem;
        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
        border: 1px solid #fcd34d;
        border-radius: 1rem;
        margin-bottom: 1.5rem;
        flex-wrap: wrap;
    }

    .alert-content {
        display: flex;
        align-items: center;
        gap: 0.75rem;
    }

    .alert-icon {
        font-size: 1.5rem;
    }

    .alert-content strong {
        display: block;
        color: #92400e;
    }

    .alert-content p {
        margin: 0.25rem 0 0;
        font-size: 0.85rem;
        color: #a16207;
    }

    .alert-action {
        padding: 0.5rem 1rem;
        background: #2f124d;
        color: white;
        border-radius: 0.5rem;
        text-decoration: none;
        font-size: 0.9rem;
        font-weight: 500;
        white-space: nowrap;
    }

    .alert-action:hover {
        background: #4a1c74;
    }

    /* Stats Row */
    .stats-row {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 1rem;
        margin-bottom: 2rem;
    }

    .stat-card {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        padding: 1rem;
        text-align: center;
    }

    .stat-card.highlight {
        background: #fef3c7;
        border-color: #fcd34d;
    }

    .stat-value {
        display: block;
        font-size: 1.75rem;
        font-weight: 700;
        color: #2f124d;
        line-height: 1;
    }

    .stat-card.highlight .stat-value {
        color: #92400e;
    }

    .stat-label {
        font-size: 0.8rem;
        color: #6b647a;
        margin-top: 0.25rem;
    }

    /* Main Grid */
    .main-grid {
        display: grid;
        grid-template-columns: 1fr 320px;
        gap: 1.5rem;
        align-items: start;
    }

    /* Sections */
    .section {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 1rem;
        padding: 1.25rem;
    }

    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
    }

    .section h2 {
        margin: 0;
        font-size: 1rem;
        color: #2d2141;
    }

    .section-link {
        font-size: 0.85rem;
        color: #7c3aed;
        text-decoration: none;
    }

    /* Submissions List */
    .submissions-list {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .submission-card {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 0.75rem;
        background: #faf8fc;
        border-radius: 0.5rem;
        text-decoration: none;
        transition: all 0.15s;
    }

    .submission-card:hover {
        background: #f3e8ff;
    }

    .submission-avatar {
        width: 36px;
        height: 36px;
        background: linear-gradient(135deg, #2f124d, #5a2d6e);
        color: white;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 0.85rem;
        flex-shrink: 0;
    }

    .submission-info {
        flex: 1;
        min-width: 0;
    }

    .submission-student {
        display: block;
        font-weight: 600;
        color: #2d2141;
        font-size: 0.9rem;
    }

    .submission-assignment {
        display: block;
        font-size: 0.8rem;
        color: #6b647a;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .submission-meta {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 0.25rem;
    }

    .submission-type {
        font-size: 0.7rem;
        padding: 0.15rem 0.4rem;
        border-radius: 0.25rem;
        background: color-mix(in srgb, var(--type-color) 15%, transparent);
        color: var(--type-color);
    }

    .submission-time {
        font-size: 0.75rem;
        color: #9ca3af;
    }

    .submission-arrow {
        color: #7c3aed;
        font-weight: 600;
    }

    /* Empty States */
    .empty-state {
        padding: 2rem;
        text-align: center;
        background: #faf8fc;
        border: 1px dashed #e0d6eb;
        border-radius: 0.75rem;
    }

    .empty-state.success {
        background: #f0fdf4;
        border-color: #bbf7d0;
    }

    .empty-icon {
        font-size: 2rem;
        display: block;
        margin-bottom: 0.5rem;
    }

    .empty-state p {
        margin: 0;
        color: #6b647a;
    }

    .empty-state.success p {
        color: #166534;
    }

    /* Sidebar */
    .sidebar {
        display: flex;
        flex-direction: column;
        gap: 1rem;
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

    .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
    }

    .card-header h3 {
        margin: 0;
    }

    .card-link {
        font-size: 0.8rem;
        color: #7c3aed;
        text-decoration: none;
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

    .quick-icon {
        font-size: 1.1rem;
    }

    .quick-link span:last-child {
        font-size: 0.9rem;
        color: #2d2141;
        font-weight: 500;
    }

    /* Classes List */
    .classes-list {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .class-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.6rem 0.75rem;
        background: #faf8fc;
        border-radius: 0.5rem;
    }

    .class-name {
        font-weight: 600;
        color: #2d2141;
        font-size: 0.9rem;
    }

    .class-count {
        font-size: 0.8rem;
        color: #6b647a;
    }

    .empty-mini {
        text-align: center;
        padding: 1rem;
    }

    .empty-mini p {
        margin: 0 0 0.75rem;
        font-size: 0.85rem;
        color: #6b647a;
    }

    .btn-small {
        display: inline-block;
        padding: 0.4rem 0.75rem;
        background: #2f124d;
        color: white;
        border-radius: 0.4rem;
        text-decoration: none;
        font-size: 0.8rem;
    }

    /* Deadlines */
    .deadlines-list {
        display: flex;
        flex-direction: column;
        gap: 0.4rem;
    }

    .deadline-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 0.5rem;
        padding: 0.55rem 0.7rem;
        background: #faf8fc;
        border-radius: 0.5rem;
        text-decoration: none;
    }

    .deadline-item:hover {
        background: #f0ebf5;
    }

    .deadline-title {
        font-size: 0.85rem;
        color: #2d2141;
        font-weight: 500;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .deadline-date {
        font-size: 0.8rem;
        color: #c97d3c;
        font-weight: 600;
        white-space: nowrap;
        flex-shrink: 0;
    }

    .deadline-empty {
        margin: 0;
        font-size: 0.85rem;
        color: #6b647a;
    }

    /* Responsive */
    @media (max-width: 900px) {
        .main-grid {
            grid-template-columns: 1fr;
        }

        .stats-row {
            grid-template-columns: repeat(2, 1fr);
        }
    }

    @media (max-width: 500px) {
        .alert-banner {
            flex-direction: column;
            align-items: stretch;
            text-align: center;
        }

        .alert-content {
            flex-direction: column;
        }

        .alert-action {
            text-align: center;
        }
    }
</style>
