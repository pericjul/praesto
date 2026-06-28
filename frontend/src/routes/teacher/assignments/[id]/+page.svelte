<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";
    import { get } from "svelte/store";

    let { data, form } = $props();

    let showModal = $state(false);
    let selectedSub = $state(null);
    let filterStatus = $state("all");
    let feedbackText = $state("");

    let FEEDBACK_SNIPPETS = $derived([
        $t('tgrade.snippet1'),
        $t('tgrade.snippet2'),
        $t('tgrade.snippet3'),
        $t('tgrade.snippet4'),
        $t('tgrade.snippet5'),
        $t('tgrade.snippet6')
    ]);

    function addSnippet(text) {
        feedbackText = feedbackText.trim() ? `${feedbackText.trim()} ${text}` : text;
    }

    function csvEscape(value) {
        const s = value == null ? "" : String(value);
        return `"${s.replace(/"/g, '""')}"`;
    }

    function typeLabel(type) {
        const tr = get(t);
        const map = {
            AI_INTERVIEW: tr('sdash.typeInterview'),
            DOCUMENT_UPLOAD: tr('sdash.typeDocument'),
            SELF_REFLECTION: tr('sdash.typeReflection'),
            VIDEO_PITCH: tr('sdash.typeVideo'),
            RESEARCH: tr('sdash.typeResearch')
        };
        return map[type] ?? type ?? "";
    }

    function exportCsv() {
        const tr = get(t);
        const fmt = (d) => (d ? new Date(d).toLocaleString("de-CH") : "");
        const rows = [[
            tr('tgrade.csvName'), tr('tgrade.csvEmail'), tr('tgrade.csvClass'),
            tr('tgrade.csvAssignment'), tr('tgrade.csvType'), tr('tgrade.csvStatus'),
            tr('tgrade.csvGrade'), tr('tgrade.csvSubmittedAt'), tr('tgrade.csvReviewedAt'),
            tr('tgrade.csvComment'), tr('tgrade.csvFile'), tr('tgrade.csvFeedback')
        ]];
        for (const sub of submissions) {
            rows.push([
                studentLabel(sub),
                sub.studentEmail ?? "",
                schoolClass?.name ?? "",
                assignment?.title ?? "",
                typeLabel(assignment?.type),
                sub.teacherFeedback ? tr('tgrade.reviewed') : tr('tgrade.open'),
                sub.grade ?? "",
                fmt(sub.submittedAt),
                fmt(sub.reviewedAt),
                sub.comment ?? sub.textContent ?? "",
                sub.fileName ?? "",
                sub.teacherFeedback ?? ""
            ]);
        }
        const csv = rows.map((r) => r.map(csvEscape).join(";")).join("\n");
        const blob = new Blob(["﻿" + csv], { type: "text/csv;charset=utf-8;" });
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `abgaben_${(assignment?.title ?? "aufgabe").replace(/[^a-z0-9]+/gi, "_")}.csv`;
        a.click();
        URL.revokeObjectURL(url);
    }

    let assignment = $derived(data?.assignment ?? null);
    let submissions = $derived(data?.submissions ?? []);
    let schoolClass = $derived(data?.schoolClass ?? null);
    let allStudents = $derived(schoolClass?.students ?? []);
    let submittedIds = $derived(submissions.map((s) => s.studentId));
    let pendingStudents = $derived(
        allStudents.filter((student) => !submittedIds.includes(student.id)),
    );

    let needsFeedbackCount = $derived(
        submissions.filter((s) => !s.teacherFeedback).length,
    );
    let reviewedCount = $derived(
        submissions.filter((s) => s.teacherFeedback).length,
    );

    let filteredSubmissions = $derived(() => {
        if (filterStatus === "needsFeedback")
            return submissions.filter((s) => !s.teacherFeedback);
        if (filterStatus === "reviewed")
            return submissions.filter((s) => s.teacherFeedback);
        return submissions;
    });

    let typeLabels = $derived({
        AI_INTERVIEW: $t('tgrade.typeInterview'),
        DOCUMENT_UPLOAD: $t('tgrade.typeDocument'),
        SELF_REFLECTION: $t('tgrade.typeReflection'),
        RESEARCH: $t('tgrade.typeResearch'),
    });

    function formatDate(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
        });
    }

    function formatDateTime(date) {
        if (!date) return "-";
        return new Date(date).toLocaleDateString("de-CH", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        });
    }

    function shortenEmail(email) {
        return email?.split("@")[0] ?? "";
    }

    // Map studentId -> Schüler-Objekt (für echte Namen statt Email-Präfix)
    let studentsById = $derived(
        Object.fromEntries((schoolClass?.students ?? []).map((s) => [s.id, s])),
    );

    function studentLabel(sub) {
        const u = studentsById[sub.studentId];
        if (u) return `${u.firstName} ${u.lastName}`.trim();
        return shortenEmail(sub.studentEmail);
    }

    function openModal(sub) {
        selectedSub = sub;
        feedbackText = sub.teacherFeedback ?? "";
        showModal = true;
    }

    function closeModal() {
        showModal = false;
        selectedSub = null;
    }
</script>

<svelte:head>
    <title>{assignment?.title ?? $t('tgrade.assignment')} – Praesto</title>
</svelte:head>

<div class="page-wrapper">
    <!-- Header -->
    <header class="page-header">
        <a href="/teacher/assignments" class="back-link">← {$t('tgrade.backToAssignments')}</a>
        {#if assignment}
            <h1 class="title">{assignment.title}</h1>
            <div class="header-title-row">
                <span class="type-badge">{typeLabels[assignment.type] ?? assignment.type}</span>
                <span class="class-badge">{schoolClass?.name ?? ""}</span>
                <span class="header-meta">📅 {$t('tgrade.deadline')}: {formatDate(assignment.dueDate)}</span>
                {#if assignment.durationMin}
                    <span class="header-meta">⏱️ {assignment.durationMin} {$t('tgrade.min')}</span>
                {/if}
            </div>
        {/if}
    </header>

    {#if form?.success}
        <div class="alert alert-success">✓ {form.message}</div>
    {/if}
    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if assignment}
        <!-- Stats -->
        <div class="stats-bar">
            <div class="stat-card">
                <span class="stat-value">{submissions.length}</span>
                <span class="stat-label">{$t('tgrade.statSubmissions')}</span>
            </div>
            <div class="stat-card">
                <span class="stat-value">{pendingStudents.length}</span>
                <span class="stat-label">{$t('tgrade.statPending')}</span>
            </div>
            <div class="stat-card highlight">
                <span class="stat-value">{needsFeedbackCount}</span>
                <span class="stat-label">{$t('tgrade.statFeedbackOpen')}</span>
            </div>
            <div class="stat-card success">
                <span class="stat-value">{reviewedCount}</span>
                <span class="stat-label">{$t('tgrade.statReviewed')}</span>
            </div>
        </div>

        <!-- Filter -->
        <div class="filter-row">
            <button
                type="button"
                class="filter-btn"
                class:active={filterStatus === "all"}
                onclick={() => (filterStatus = "all")}
            >
                {$t('tgrade.filterAll')} ({submissions.length})
            </button>
            <button
                type="button"
                class="filter-btn filter-warning"
                class:active={filterStatus === "needsFeedback"}
                onclick={() => (filterStatus = "needsFeedback")}
            >
                🔔 {$t('tgrade.filterOpen')} ({needsFeedbackCount})
            </button>
            <button
                type="button"
                class="filter-btn filter-success"
                class:active={filterStatus === "reviewed"}
                onclick={() => (filterStatus = "reviewed")}
            >
                ✓ {$t('tgrade.filterReviewed')} ({reviewedCount})
            </button>
            {#if submissions.length > 0}
                <button type="button" class="filter-btn export-btn" onclick={exportCsv}>
                    📥 {$t('tgrade.csvExport')}
                </button>
            {/if}
        </div>

        <!-- Submissions -->
        {#if filteredSubmissions().length === 0}
            <div class="empty-state">
                <span class="empty-icon">📭</span>
                <p>{$t('tgrade.noSubmissionsCategory')}</p>
            </div>
        {:else}
            <div class="submissions-grid">
                {#each filteredSubmissions() as sub}
                    <div class="submission-card" class:reviewed={sub.teacherFeedback}>
                        <div class="sub-header">
                            <div class="sub-student">
                                <span class="student-avatar">{studentLabel(sub).charAt(0).toUpperCase()}</span>
                                <div>
                                    <span class="student-name">{studentLabel(sub)}</span>
                                    <span class="student-email">{sub.studentEmail}</span>
                                </div>
                            </div>
                            <span class="sub-date">{formatDateTime(sub.submittedAt)}</span>
                        </div>

                        <!-- Abgabe-Inhalt -->
                        {#if sub.textContent}
                            <div class="sub-content">
                                <strong>{$t('tgrade.submission')}:</strong>
                                <p>{sub.textContent}</p>
                            </div>
                        {/if}

                        {#if sub.links && sub.links.length > 0}
                            <div class="sub-content">
                                <strong>{$t('tgrade.links')}:</strong>
                                <ul class="sub-links">
                                    {#each sub.links as link}
                                        <li><a href={link} target="_blank" rel="noopener">{link}</a></li>
                                    {/each}
                                </ul>
                            </div>
                        {/if}

                        {#if sub.fileUrl}
                            <a href={`/files/${sub.fileUrl}`} class="chat-link" download>
                                📎 {sub.fileName ?? $t('tgrade.file')} {$t('tgrade.download')} →
                            </a>
                        {/if}

                        {#if assignment.type === "AI_INTERVIEW" && sub.chatSessionId}
                            <a href="/teacher/sessions/{sub.chatSessionId}" class="chat-link">
                                💬 {$t('tgrade.viewChatHistory')} →
                            </a>
                        {/if}

                        <!-- Status & Feedback -->
                        <div class="sub-footer">
                            <div class="sub-status">
                                {#if sub.teacherFeedback}
                                    <span class="badge badge-success">✓ {$t('tgrade.reviewed')}</span>
                                    {#if sub.grade}<span class="grade">{$t('tgrade.grade')} {sub.grade}</span>{/if}
                                {:else}
                                    <span class="badge badge-warning">{$t('tgrade.open')}</span>
                                {/if}
                            </div>
                            <button
                                type="button"
                                class="btn"
                                class:btn-primary={!sub.teacherFeedback}
                                class:btn-secondary={sub.teacherFeedback}
                                onclick={() => openModal(sub)}
                            >
                                {sub.teacherFeedback ? $t('tgrade.edit') : $t('tgrade.giveFeedback')}
                            </button>
                        </div>

                        {#if sub.teacherFeedback}
                            <div class="feedback-preview">
                                <strong>{$t('tgrade.yourFeedback')}:</strong>
                                <p>{sub.teacherFeedback}</p>
                            </div>
                        {/if}
                    </div>
                {/each}
            </div>
        {/if}

        <!-- Ausstehend -->
        {#if pendingStudents.length > 0}
            <div class="pending-section">
                <h3>⏳ {$t('tgrade.notSubmittedYet')} ({pendingStudents.length})</h3>
                <div class="pending-list">
                    {#each pendingStudents as student}
                        <span class="pending-chip">{student.firstName} {student.lastName}</span>
                    {/each}
                </div>
            </div>
        {/if}
    {/if}
</div>

<!-- Modal -->
{#if showModal && selectedSub}
    <button type="button" class="modal-backdrop" onclick={closeModal}></button>
    <div class="modal">
        <div class="modal-header">
            <h2>{$t('tgrade.giveFeedback')}</h2>
            <button type="button" class="btn-close" onclick={closeModal}>✕</button>
        </div>
        <form
            method="POST"
            action="?/giveFeedback"
            use:enhance={() => {
                return async ({ result }) => {
                    if (result.type === "success") {
                        closeModal();
                        await invalidateAll();
                    }
                };
            }}
        >
            <div class="modal-body">
                <input type="hidden" name="submissionId" value={selectedSub.id} />

                <div class="modal-info">
                    <strong>{$t('tgrade.student')}:</strong> {studentLabel(selectedSub)} ({selectedSub.studentEmail})
                </div>

                {#if selectedSub.textContent}
                    <div class="modal-submission">
                        <strong>{$t('tgrade.submission')}:</strong>
                        <p>{selectedSub.textContent}</p>
                    </div>
                {/if}

                {#if selectedSub.fileUrl}
                    <div class="modal-submission">
                        <strong>{$t('tgrade.submissionFile')}:</strong>
                        <a href={`/files/${selectedSub.fileUrl}`} download>
                            📎 {selectedSub.fileName ?? $t('tgrade.file')} {$t('tgrade.download')}
                        </a>
                    </div>
                {/if}

                {#if selectedSub.chatSessionId}
                    <div class="modal-submission">
                        <a href="/teacher/sessions/{selectedSub.chatSessionId}">💬 {$t('tgrade.viewChatHistory')} →</a>
                    </div>
                {/if}

                <div class="form-group">
                    <label for="feedback">{$t('tgrade.yourFeedback')}</label>
                    <div class="snippets">
                        {#each FEEDBACK_SNIPPETS as snippet}
                            <button type="button" class="snippet-chip" onclick={() => addSnippet(snippet)}>
                                + {snippet}
                            </button>
                        {/each}
                    </div>
                    <textarea
                        id="feedback"
                        name="feedback"
                        rows="4"
                        placeholder={$t('tgrade.feedbackPlaceholder')}
                        bind:value={feedbackText}
                    ></textarea>
                </div>

                <div class="form-group">
                    <label for="grade">{$t('tgrade.gradeOptional')}</label>
                    <input
                        type="number"
                        id="grade"
                        name="grade"
                        min="1"
                        max="6"
                        step="0.5"
                        placeholder={$t('tgrade.gradePlaceholder')}
                        value={selectedSub.grade ?? ""}
                    />
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick={closeModal}>{$t('tgrade.cancel')}</button>
                <button type="submit" class="btn btn-primary">{$t('tgrade.save')}</button>
            </div>
        </form>
    </div>
{/if}

<style>
    .page-wrapper {
        max-width: 1000px;
        margin: 0 auto;
        padding: 1.5rem 1rem 3rem;
    }

    /* Header */
    .page-header {
        margin-bottom: 1.5rem;
        display: flex;
        flex-direction: column;
        align-items: flex-start;
    }

    .back-link {
        display: inline-block;
        color: #6b647a;
        text-decoration: none;
        font-size: 0.9rem;
        margin-bottom: 0.75rem;
    }

    .back-link:hover {
        color: #2f124d;
    }

    .header-title-row {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        flex-wrap: wrap;
    }

    .type-badge {
        background: #f0e7fa;
        color: var(--color-primary, #2F124D);
        padding: 0.3rem 0.75rem;
        border-radius: 999px;
        font-size: 0.78rem;
        font-weight: 600;
    }

    .class-badge {
        background: #e0f2fe;
        color: #0369a1;
        padding: 0.3rem 0.75rem;
        border-radius: 999px;
        font-size: 0.78rem;
        font-weight: 600;
    }

    .header-meta {
        font-size: 0.85rem;
        color: #6b647a;
    }

    .title {
        margin: 0 0 0.6rem;
        font-size: clamp(1.6rem, 3vw, 2rem);
        color: var(--color-primary, #2F124D);
        font-weight: 800;
        letter-spacing: -0.01em;
    }

    /* Alerts */
    .alert {
        padding: 0.75rem 1rem;
        border-radius: 0.75rem;
        margin-bottom: 1rem;
        font-size: 0.9rem;
    }

    .alert-success {
        background: #f0fdf4;
        color: #166534;
        border: 1px solid #bbf7d0;
    }

    .alert-danger {
        background: #fef2f2;
        color: #dc2626;
        border: 1px solid #fecaca;
    }

    /* Stats */
    .stats-bar {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 1rem;
        margin-bottom: 1.5rem;
    }

    .stat-card {
        background: #fff;
        border: 1px solid #ece3f5;
        border-radius: 1rem;
        padding: 1.1rem 1rem;
        text-align: center;
        box-shadow: 0 2px 10px rgba(47, 18, 77, 0.05);
        position: relative;
        overflow: hidden;
    }
    .stat-card::before {
        content: "";
        position: absolute; top: 0; left: 0; right: 0; height: 3px;
        background: var(--color-primary, #2F124D);
        opacity: 0.6;
    }
    .stat-card.highlight { background: #fff8f1; border-color: #fcd9b6; }
    .stat-card.highlight::before { background: #e8590c; opacity: 1; }
    .stat-card.success { background: #f1fbf4; border-color: #bbf0cb; }
    .stat-card.success::before { background: #16a34a; opacity: 1; }

    .stat-value {
        display: block;
        font-size: 1.9rem;
        font-weight: 800;
        color: var(--color-primary, #2F124D);
        line-height: 1.1;
    }

    .stat-card.highlight .stat-value {
        color: #c2410c;
    }

    .stat-card.success .stat-value {
        color: #16a34a;
    }

    .stat-label {
        font-size: 0.78rem;
        color: #6b647a;
        margin-top: 0.2rem;
        display: block;
    }

    /* Filter */
    .filter-row {
        display: flex;
        gap: 0.5rem;
        margin-bottom: 1.5rem;
        flex-wrap: wrap;
    }

    .filter-btn {
        padding: 0.5rem 1rem;
        border: 1px solid #e8e0f0;
        background: #fff;
        border-radius: 2rem;
        font-size: 0.85rem;
        cursor: pointer;
        color: #6b647a;
        transition: all 0.15s;
    }

    .filter-btn:hover {
        border-color: #2f124d;
    }

    .filter-btn.active {
        background: #2f124d;
        color: white;
        border-color: #2f124d;
    }

    .filter-btn.filter-warning {
        border-color: #fcd34d;
        color: #92400e;
    }

    .filter-btn.filter-warning.active {
        background: #f59e0b;
        border-color: #f59e0b;
        color: white;
    }

    .filter-btn.filter-success {
        border-color: #bbf7d0;
        color: #166534;
    }

    .filter-btn.filter-success.active {
        background: #16a34a;
        border-color: #16a34a;
        color: white;
    }

    /* Submissions Grid */
    .submissions-grid {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .submission-card {
        background: #fff;
        border: 1px solid #ece3f5;
        border-left: 4px solid #f59e0b;
        border-radius: 1rem;
        padding: 1.25rem;
        box-shadow: 0 2px 10px rgba(47, 18, 77, 0.05);
        transition: transform 0.15s, box-shadow 0.15s;
    }

    .submission-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 24px rgba(47, 18, 77, 0.10);
    }

    .submission-card.reviewed {
        border-left: 4px solid #16a34a;
    }

    .sub-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
    }

    .sub-student {
        display: flex;
        align-items: center;
        gap: 0.75rem;
    }

    .student-avatar {
        width: 36px;
        height: 36px;
        background: linear-gradient(135deg, #2f124d, #5a2d6e);
        color: white;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 0.9rem;
    }

    .student-name {
        display: block;
        font-weight: 600;
        color: #2d2141;
    }

    .student-email {
        display: block;
        font-size: 0.75rem;
        color: #9ca3af;
    }

    .sub-date {
        font-size: 0.8rem;
        color: #9ca3af;
    }

    .sub-content {
        background: #faf8fc;
        border-radius: 0.5rem;
        padding: 0.75rem;
        margin-bottom: 1rem;
    }

    .sub-content strong {
        display: block;
        font-size: 0.8rem;
        color: #6b647a;
        margin-bottom: 0.25rem;
    }

    .sub-content p {
        margin: 0;
        color: #374151;
        line-height: 1.5;
    }

    .chat-link {
        display: inline-block;
        color: #7c3aed;
        font-size: 0.9rem;
        text-decoration: none;
        margin-bottom: 1rem;
    }

    .chat-link:hover {
        text-decoration: underline;
    }

    .sub-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 0.75rem;
        border-top: 1px solid #f3f0f7;
    }

    .sub-status {
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }

    .badge {
        padding: 0.25rem 0.6rem;
        border-radius: 1rem;
        font-size: 0.75rem;
        font-weight: 500;
    }

    .badge-success {
        background: #dcfce7;
        color: #166534;
    }

    .badge-warning {
        background: #fef3c7;
        color: #92400e;
    }

    .grade {
        font-size: 0.85rem;
        font-weight: 600;
        color: #7c3aed;
    }

    .feedback-preview {
        margin-top: 1rem;
        padding: 0.75rem;
        background: #f0fdf4;
        border-radius: 0.5rem;
        border: 1px solid #bbf7d0;
    }

    .feedback-preview strong {
        display: block;
        font-size: 0.75rem;
        color: #166534;
        margin-bottom: 0.25rem;
    }

    .feedback-preview p {
        margin: 0;
        font-size: 0.9rem;
        color: #374151;
    }

    /* Empty State */
    .empty-state {
        text-align: center;
        padding: 3rem 2rem;
        background: #faf8fc;
        border: 2px dashed #e0d6eb;
        border-radius: 1rem;
    }

    .empty-icon {
        font-size: 2.5rem;
        display: block;
        margin-bottom: 0.5rem;
    }

    .empty-state p {
        margin: 0;
        color: #6b647a;
    }

    /* Pending */
    .pending-section {
        margin-top: 2rem;
        padding: 1.25rem;
        background: #fffbeb;
        border: 1px solid #fcd34d;
        border-radius: 1rem;
    }

    .pending-section h3 {
        margin: 0 0 0.75rem;
        font-size: 0.95rem;
        color: #92400e;
    }

    .pending-list {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
    }

    .pending-chip {
        padding: 0.3rem 0.65rem;
        background: #fef3c7;
        color: #92400e;
        border-radius: 1rem;
        font-size: 0.8rem;
    }

    /* Buttons */
    .btn {
        padding: 0.5rem 1rem;
        border-radius: 0.5rem;
        font-size: 0.85rem;
        cursor: pointer;
        border: none;
        font-weight: 500;
    }

    .btn-primary {
        background: #2f124d;
        color: white;
    }

    .btn-primary:hover {
        background: #4a1c74;
    }

    .btn-secondary {
        background: #f3f4f6;
        color: #374151;
        border: 1px solid #e5e7eb;
    }

    .btn-secondary:hover {
        background: #e5e7eb;
    }

    /* Modal */
    .modal-backdrop {
        position: fixed;
        inset: 0;
        background: rgba(47, 18, 77, 0.4);
        backdrop-filter: blur(4px);
        z-index: 998;
        border: none;
        cursor: pointer;
    }

    .modal {
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: #fff;
        border-radius: 1rem;
        width: 90%;
        max-width: 500px;
        max-height: 90vh;
        overflow: hidden;
        z-index: 999;
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
        display: flex;
        flex-direction: column;
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1rem 1.25rem;
        border-bottom: 1px solid #f0e8f8;
    }

    .modal-header h2 {
        margin: 0;
        font-size: 1.1rem;
        color: #2d2141;
    }

    .btn-close {
        background: none;
        border: none;
        font-size: 1.25rem;
        cursor: pointer;
        color: #6b647a;
    }

    .modal-body {
        padding: 1.25rem;
        overflow-y: auto;
    }

    .modal-info {
        padding: 0.75rem;
        background: #faf8fc;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
        font-size: 0.9rem;
    }

    .modal-submission {
        padding: 0.75rem;
        background: #f3f0f7;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
    }

    .modal-submission strong {
        display: block;
        font-size: 0.8rem;
        color: #6b647a;
        margin-bottom: 0.25rem;
    }

    .modal-submission p {
        margin: 0;
        font-size: 0.9rem;
        color: #374151;
        max-height: 120px;
        overflow-y: auto;
    }

    .form-group {
        margin-bottom: 1rem;
    }

    .form-group label {
        display: block;
        font-size: 0.9rem;
        font-weight: 500;
        color: #2d2141;
        margin-bottom: 0.4rem;
    }

    .form-group textarea,
    .form-group input {
        width: 100%;
        padding: 0.6rem;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        font-family: inherit;
        background: #faf8fc;
    }

    .form-group textarea:focus,
    .form-group input:focus {
        outline: none;
        border-color: #2f124d;
        background: #fff;
    }

    .modal-footer {
        display: flex;
        justify-content: flex-end;
        gap: 0.5rem;
        padding: 1rem 1.25rem;
        border-top: 1px solid #f0e8f8;
    }

    /* Responsive */
    @media (max-width: 600px) {
        .stats-bar {
            grid-template-columns: repeat(2, 1fr);
        }

        .sub-header {
            flex-direction: column;
            align-items: flex-start;
            gap: 0.5rem;
        }

        .sub-footer {
            flex-direction: column;
            gap: 0.75rem;
            align-items: stretch;
        }

        .sub-footer .btn {
            width: 100%;
            text-align: center;
        }
    }

    .export-btn {
        margin-left: auto;
    }

    .snippets {
        display: flex;
        flex-wrap: wrap;
        gap: 0.4rem;
        margin-bottom: 0.6rem;
    }

    .snippet-chip {
        background: #f3f0f7;
        border: 1px solid #e8e0f0;
        color: #4c1d95;
        border-radius: 999px;
        padding: 0.25rem 0.6rem;
        font-size: 0.78rem;
        cursor: pointer;
        transition: all 0.15s;
    }

    .snippet-chip:hover {
        background: #ede4f7;
        border-color: #c9b6e6;
    }
</style>
