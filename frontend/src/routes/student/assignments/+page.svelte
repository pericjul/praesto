<script>
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    // Derived
    let assignments = $derived(data?.assignments ?? []);
    let submissions = $derived(data?.submissions ?? []);
    let myClass = $derived(data?.myClass ?? null);
    let error = $derived(data?.error ?? null);

    // Sortierte Assignments: Offene zuerst (nach Deadline), dann erledigte
    let sortedAssignments = $derived(() => {
        const open = [];
        const done = [];
        
        for (const a of assignments) {
            if (submissions.some(s => s.assignmentId === a.id)) {
                done.push(a);
            } else {
                open.push(a);
            }
        }
        
        // Offene nach Deadline sortieren (dringendste zuerst)
        open.sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate));
        // Erledigte nach Abgabedatum sortieren (neueste zuerst)
        done.sort((a, b) => {
            const subA = submissions.find(s => s.assignmentId === a.id);
            const subB = submissions.find(s => s.assignmentId === b.id);
            return new Date(subB?.submittedAt) - new Date(subA?.submittedAt);
        });
        
        return [...open, ...done];
    });

    let assignmentTypes = $derived({
        AI_INTERVIEW: { label: $t("stasks.typeInterview"), action: $t("stasks.actionInterview"), color: "var(--color-primary)" },
        DOCUMENT_UPLOAD: { label: $t("stasks.typeDocument"), action: $t("stasks.actionDocument"), color: "var(--color-info)" },
        SELF_REFLECTION: { label: $t("stasks.typeReflection"), action: $t("stasks.actionReflection"), color: "var(--color-success)" },
        VIDEO_PITCH: { label: $t("stasks.typeVideo"), action: $t("stasks.actionVideo"), color: "var(--color-warning)" },
        RESEARCH: { label: $t("stasks.typeResearch"), action: $t("stasks.actionResearch"), color: "#6366f1" }
    });

    function getTypeInfo(type) {
        return assignmentTypes[type] ?? { label: type, action: $t("stasks.actionDefault"), color: "var(--color-text-muted)" };
    }

    function getSubmissionForAssignment(assignmentId) {
        return submissions.find(s => s.assignmentId === assignmentId);
    }

    function hasSubmitted(assignmentId) {
        return submissions.some(s => s.assignmentId === assignmentId);
    }

    function formatDate(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", year: "numeric"
        });
    }

    function formatDateTime(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", year: "numeric",
            hour: "2-digit", minute: "2-digit"
        });
    }

    function getDeadlineStatus(dueDate) {
        if (!dueDate) return "ok";
        const now = new Date();
        const due = new Date(dueDate);
        const diffDays = (due - now) / (1000 * 60 * 60 * 24);
        if (diffDays < 0) return "overdue";
        if (diffDays <= 3) return "soon";
        return "ok";
    }

    function formatStatus(status) {
        switch (status) {
            case "ASSIGNED": return $t("stasks.statusAssigned");
            case "IN_PROGRESS": return $t("stasks.statusInProgress");
            case "COMPLETED": return $t("stasks.statusCompleted");
            default: return status;
        }
    }

    function formatSubmissionStatus(status) {
        switch (status) {
            case "SUBMITTED": return $t("stasks.subSubmitted");
            case "IN_REVIEW": return $t("stasks.subInReview");
            case "REVIEWED": return $t("stasks.subReviewed");
            case "RETURNED": return $t("stasks.subReturned");
            default: return status;
        }
    }
</script>

<svelte:head>
    <title>{$t("stasks.headTitle")}</title>
</svelte:head>

<div class="page-wrapper">
    <header class="page-header">
        <div>
            <h1 class="title">{$t("stasks.title")}</h1>
            {#if myClass}
                <p class="subtitle">
                    {$t("stasks.classPrefix")} <strong>{myClass.name}</strong> ·
                    {$t("stasks.classIntro")}
                </p>
            {:else}
                <p class="subtitle">{$t("stasks.subtitle")}</p>
            {/if}
        </div>
    </header>

    {#if form?.success}
        <div class="alert alert-success">✓ {form.message || $t("stasks.saved")}</div>
    {/if}

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if error}
        <div class="alert alert-warning">
            <span class="alert-icon">⚠️</span>
            <div>
                <strong>{$t("stasks.hint")}</strong>
                <p>{error}</p>
            </div>
        </div>
    {:else if assignments.length === 0}
        <div class="empty-state">
            <div class="empty-icon">🎉</div>
            <h2>{$t("stasks.emptyTitle")}</h2>
            <p>{$t("stasks.emptyText")}</p>
        </div>
    {:else}
        <div class="stats-bar">
            <div class="stat-card">
                <span class="stat-value">{assignments.length}</span>
                <span class="stat-label">{$t("stasks.statTotal")}</span>
            </div>
            <div class="stat-card">
                <span class="stat-value">{assignments.filter(a => !submissions.some(s => s.assignmentId === a.id)).length}</span>
                <span class="stat-label">{$t("stasks.statOpen")}</span>
            </div>
            <div class="stat-card stat-success">
                <span class="stat-value">{assignments.filter(a => submissions.some(s => s.assignmentId === a.id)).length}</span>
                <span class="stat-label">{$t("stasks.statSubmitted")}</span>
            </div>
        </div>

        <div class="assignments-list">
            {#each sortedAssignments() as assignment}
                {@const typeInfo = getTypeInfo(assignment.type)}
                {@const deadlineStatus = getDeadlineStatus(assignment.dueDate)}
                {@const submission = getSubmissionForAssignment(assignment.id)}
                {@const submitted = !!submission}
                
                <a href="/student/assignments/{assignment.id}" class="assignment-card" class:overdue={deadlineStatus === "overdue" && !submitted} class:submitted={submitted}>
                    <div class="assignment-content">
                        <div class="assignment-header">
                            <span class="type-badge" style="--type-color: {typeInfo.color}">
                                {typeInfo.label}
                            </span>
                            {#if submitted}
                                <span class="badge badge-success">✓ {formatSubmissionStatus(submission.status)}</span>
                            {:else}
                                <span class="badge badge-primary">{formatStatus(assignment.status)}</span>
                            {/if}
                        </div>
                        
                        <h3 class="assignment-title">{assignment.title}</h3>
                        
                        {#if assignment.description}
                            <p class="assignment-desc">{assignment.description}</p>
                        {/if}
                        
                        <div class="assignment-meta">
                            {#if submitted}
                                <span class="meta-item text-success">{$t("stasks.submittedAt")} {formatDateTime(submission.submittedAt)}</span>
                                {#if submission.teacherFeedback}
                                    <span class="meta-item has-feedback">{$t("stasks.feedbackReceived")}</span>
                                {/if}
                            {:else}
                                <span class="meta-item" class:text-warning={deadlineStatus === "soon"} class:text-danger={deadlineStatus === "overdue"}>
                                    {$t("stasks.deadline")} {formatDate(assignment.dueDate)}
                                    {#if deadlineStatus === "overdue"}
                                        <span class="deadline-label danger">{$t("stasks.overdue")}</span>
                                    {:else if deadlineStatus === "soon"}
                                        <span class="deadline-label warning">{$t("stasks.dueSoon")}</span>
                                    {/if}
                                </span>
                            {/if}
                            {#if assignment.durationMin}
                                <span class="meta-item">⏱️ ca. {assignment.durationMin} {$t("stasks.approxMin")}</span>
                            {/if}
                        </div>

                        {#if submission?.teacherFeedback}
                            <div class="feedback-box">
                                <strong>{$t("stasks.feedbackLabel")}</strong>
                                <p>{submission.teacherFeedback}</p>
                                {#if submission.grade != null}
                                    <span class="grade-badge">{$t("stasks.grade")} {submission.grade}</span>
                                {/if}
                            </div>
                        {/if}
                    </div>
                    
                    <div class="assignment-action">
                        {#if submitted}
                            <span class="view-hint">{$t("stasks.viewDetails")}</span>
                        {:else}
                            <span class="btn btn-primary">{typeInfo.action}</span>
                        {/if}
                    </div>
                </a>
            {/each}
        </div>
    {/if}
</div>

<style>
    /* ===== PAGE SPECIFIC STYLES ===== */
    .alert-warning {
        display: flex;
        gap: var(--space-md);
        align-items: flex-start;
    }

    .alert-warning p {
        margin: var(--space-xs) 0 0;
    }

    .assignments-list {
        display: flex;
        flex-direction: column;
        gap: var(--space-lg);
    }

    .assignment-card {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: var(--space-xl);
        padding: var(--space-xl);
        background: var(--color-bg-card);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-xl);
        transition: all var(--transition-base);
        text-decoration: none;
        cursor: pointer;
    }

    .assignment-card:hover {
        border-color: var(--color-primary);
        box-shadow: var(--shadow-md);
    }

    .assignment-card.overdue {
        border-color: var(--color-error-border);
        background: var(--color-error-bg);
    }

    .assignment-card.submitted {
        border-color: var(--color-success-border);
        background: var(--color-success-bg);
    }

    .assignment-content {
        flex: 1;
        min-width: 0;
    }

    .assignment-header {
        display: flex;
        gap: var(--space-sm);
        margin-bottom: var(--space-sm);
        flex-wrap: wrap;
    }

    .type-badge {
        display: inline-block;
        padding: var(--space-xs) var(--space-sm);
        border-radius: var(--radius-md);
        font-size: var(--font-size-xs);
        font-weight: 500;
        background: color-mix(in srgb, var(--type-color) 15%, transparent);
        color: var(--type-color);
    }

    .assignment-title {
        margin: 0 0 var(--space-xs);
        font-size: var(--font-size-lg);
        font-weight: 600;
        color: var(--color-text-secondary);
    }

    .assignment-desc {
        margin: 0 0 var(--space-sm);
        font-size: var(--font-size-sm);
        color: var(--color-text-light);
    }

    .assignment-meta {
        display: flex;
        gap: var(--space-lg);
        flex-wrap: wrap;
        font-size: var(--font-size-sm);
        color: var(--color-text-muted);
    }

    .meta-item {
        display: flex;
        align-items: center;
        gap: var(--space-xs);
    }

    .meta-item.has-feedback {
        color: var(--color-primary);
        font-weight: 500;
    }

    .deadline-label {
        margin-left: var(--space-sm);
        padding: 2px var(--space-sm);
        border-radius: var(--radius-sm);
        font-size: var(--font-size-xs);
        font-weight: 600;
        color: #fff;
    }

    .deadline-label.warning {
        background: var(--color-warning);
    }

    .deadline-label.danger {
        background: var(--color-error);
    }

    .feedback-box {
        margin-top: var(--space-md);
        padding: var(--space-md);
        background: var(--color-bg-muted);
        border: 1px solid var(--color-border-light);
        border-radius: var(--radius-md);
    }

    .feedback-box strong {
        color: var(--color-primary);
    }

    .feedback-box p {
        margin: var(--space-xs) 0 0;
        color: var(--color-text-secondary);
        font-size: var(--font-size-sm);
    }

    .grade-badge {
        display: inline-block;
        margin-top: var(--space-sm);
        padding: var(--space-xs) var(--space-md);
        background: var(--color-primary);
        color: #fff;
        border-radius: var(--radius-full);
        font-size: var(--font-size-sm);
        font-weight: 600;
    }

    .assignment-action {
        flex-shrink: 0;
    }

    .view-hint {
        color: var(--color-primary);
        font-size: var(--font-size-sm);
        font-weight: 500;
    }

    @media (max-width: 600px) {
        .assignment-card {
            flex-direction: column;
            align-items: stretch;
        }

        .assignment-action {
            margin-top: var(--space-lg);
        }

        .assignment-action .btn {
            width: 100%;
            text-align: center;
        }
    }
</style>
