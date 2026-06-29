<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";
    import { get } from "svelte/store";

    let { data, form } = $props();

    // Derived state
    let session = $derived(data?.session ?? null);
    let messages = $derived(data?.session?.messages ?? []);
    let student = $derived(data?.student ?? null);
    
    // Feedback state
    let showFeedbackModal = $state(false);
    let feedbackText = $state(data?.session?.teacherFeedback ?? "");
    let gradeValue = $state(data?.session?.grade ?? "");

    function formatTime(date) {
        if (!date) return "";
        try {
            return new Date(date).toLocaleTimeString("de-CH", {
                hour: "2-digit",
                minute: "2-digit"
            });
        } catch {
            return "";
        }
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

    function formatDuration(seconds) {
        if (!seconds) return "-";
        const min = Math.floor(seconds / 60);
        const sec = seconds % 60;
        return `${min}:${sec.toString().padStart(2, "0")} ${get(t)('tsess.min')}`;
    }

    function openFeedbackModal() {
        feedbackText = session?.teacherFeedback ?? "";
        gradeValue = session?.grade ?? "";
        showFeedbackModal = true;
    }

    function closeFeedbackModal() {
        showFeedbackModal = false;
    }
</script>

<svelte:head>
    <title>{$t('tsess.chatHistory')} – {student?.name ?? $t('tsess.student')} – Praesto</title>
</svelte:head>

<div class="chat-wrapper">
    <div class="chat-page">
        <!-- Header -->
        <header class="chat-header">
            <a href="/teacher/assignments/{session?.assignmentId}" class="back-link">← {$t('tsess.backToAssignment')}</a>
            <div class="header-content">
                <h1>💬 {$t('tsess.chatHistory')}</h1>
                <p>
                    <strong>{student?.name ?? student?.email ?? $t('tsess.student')}</strong>
                    {#if session?.assignmentTitle}
                        · {session.assignmentTitle}
                    {/if}
                </p>
            </div>

            {#if session?.submittedAsAssignment}
                <button type="button" class="btn-feedback" onclick={openFeedbackModal}>
                    {session.teacherFeedback ? `✏️ ${$t('tsess.editFeedback')}` : `💬 ${$t('tsess.giveFeedback')}`}
                </button>
            {:else}
                <span class="status-badge in-progress">⏳ {$t('tsess.inProgress')}</span>
            {/if}
        </header>

        <!-- Session Info Bar -->
        <div class="info-bar">
            <div class="info-item">
                <span class="info-label">{$t('tsess.started')}</span>
                <span class="info-value">{formatDate(session?.startedAt)}</span>
            </div>
            <div class="info-item">
                <span class="info-label">{$t('tsess.duration')}</span>
                <span class="info-value">{formatDuration(session?.durationSeconds)}</span>
            </div>
            <div class="info-item">
                <span class="info-label">{$t('tsess.messages')}</span>
                <span class="info-value">{messages.length}</span>
            </div>
            {#if session?.submittedAsAssignment}
                <div class="info-item">
                    <span class="info-label">{$t('tsess.status')}</span>
                    <span class="info-value status-submitted">✓ {$t('tsess.submitted')}</span>
                </div>
            {/if}
            {#if session?.grade != null}
                <div class="info-item">
                    <span class="info-label">{$t('tsess.grade')}</span>
                    <span class="info-value grade">{session.grade}</span>
                </div>
            {/if}
        </div>

        <!-- Feedback anzeigen (wenn vorhanden) -->
        {#if session?.teacherFeedback}
            <div class="feedback-display">
                <div class="feedback-header">
                    <span class="feedback-icon">💬</span>
                    <span class="feedback-title">{$t('tsess.yourFeedback')}</span>
                </div>
                <p class="feedback-text">{session.teacherFeedback}</p>
            </div>
        {/if}

        {#if form?.error}
            <div class="alert alert-danger">{form.error}</div>
        {/if}

        {#if form?.success}
            <div class="alert alert-success">✓ {form.message ?? $t('tsess.feedbackSaved')}</div>
        {/if}

        <!-- Chat Container -->
        <div class="chat-container">
            {#if messages.length === 0}
                <div class="empty-chat">
                    <span class="empty-icon">💬</span>
                    <p>{$t('tsess.noMessages')}</p>
                </div>
            {:else}
                {#each messages as msg}
                    <div class="message {msg.role === 'USER' ? 'user' : 'bot'}">
                        <div class="message-bubble">
                            <span class="message-text">{msg.content}</span>
                            {#if msg.createdAt}
                                <span class="message-time">{formatTime(msg.createdAt)}</span>
                            {/if}
                        </div>
                    </div>
                {/each}
            {/if}
        </div>

        <!-- Footer -->
        <div class="chat-footer">
            <p>{$t('tsess.footerNote')}</p>
            <a href="/teacher/assignments/{session?.assignmentId}" class="btn-back">← {$t('tsess.toAssignmentOverview')}</a>
        </div>
    </div>
</div>

<!-- Feedback Modal -->
{#if showFeedbackModal}
    <button type="button" class="modal-backdrop" onclick={closeFeedbackModal}></button>
    <div class="modal">
        <div class="modal-header">
            <h2>💬 {$t('tsess.giveFeedback')}</h2>
            <button type="button" class="btn-close-modal" onclick={closeFeedbackModal}>✕</button>
        </div>

        <form method="POST" action="?/saveFeedback"
            use:enhance={() => {
                return async ({ result }) => {
                    if (result.type === 'success') {
                        closeFeedbackModal();
                        await invalidateAll();
                    }
                };
            }}>
            <div class="modal-body">
                <div class="student-info">
                    <span class="student-label">{$t('tsess.student')}:</span>
                    <span class="student-name">{student?.name ?? student?.email}</span>
                </div>

                <div class="form-group">
                    <label for="feedback">{$t('tsess.feedback')}</label>
                    <textarea
                        id="feedback"
                        name="feedback"
                        rows="5"
                        placeholder={$t('tsess.feedbackPlaceholder')}
                        bind:value={feedbackText}
                    ></textarea>
                </div>

                <div class="form-group">
                    <label for="grade">{$t('tsess.gradeOptional')}</label>
                    <input
                        type="number"
                        id="grade"
                        name="grade"
                        min="1"
                        max="6"
                        step="0.5"
                        placeholder={$t('tsess.gradePlaceholder')}
                        bind:value={gradeValue}
                    />
                    <span class="form-hint">{$t('tsess.gradeScaleHint')}</span>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick={closeFeedbackModal}>{$t('tsess.cancel')}</button>
                <button type="submit" class="btn btn-primary">💾 {$t('tsess.save')}</button>
            </div>
        </form>
    </div>
{/if}

<style>
    /* Wrapper */
    .chat-wrapper {
        height: calc(100vh - 54px);
        background: #F8F3EB;
        padding: 1rem;
        overflow: hidden;
    }

    .chat-page {
        max-width: 800px;
        margin: 0 auto;
        background: #fff;
        border-radius: 1rem;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        display: flex;
        flex-direction: column;
        height: 100%;
        overflow: hidden;
    }

    /* Header */
    .chat-header {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1rem 1.5rem;
        background: linear-gradient(135deg, #2F124D 0%, #5a2d6e 100%);
        color: white;
        flex-shrink: 0;
    }

    .back-link {
        color: rgba(255, 255, 255, 0.8);
        text-decoration: none;
        font-size: 0.9rem;
        white-space: nowrap;
    }

    .back-link:hover {
        color: white;
    }

    .header-content {
        flex: 1;
    }

    .header-content h1 {
        margin: 0;
        font-size: 1.2rem;
        font-weight: 600;
    }

    .header-content p {
        margin: 0.25rem 0 0;
        font-size: 0.85rem;
        opacity: 0.9;
    }

    .btn-feedback {
        padding: 0.5rem 1rem;
        background: #F2A43B;
        color: #2F124D;
        border: none;
        border-radius: 0.5rem;
        font-size: 0.85rem;
        font-weight: 600;
        cursor: pointer;
        white-space: nowrap;
        transition: all 0.15s;
    }

    .btn-feedback:hover {
        background: #ffb84d;
    }

    .status-badge {
        padding: 0.4rem 0.75rem;
        border-radius: 1rem;
        font-size: 0.8rem;
        font-weight: 500;
        white-space: nowrap;
    }

    .status-badge.in-progress {
        background: rgba(255, 255, 255, 0.2);
        color: white;
    }

    /* Info Bar */
    .info-bar {
        display: flex;
        gap: 1.5rem;
        padding: 0.75rem 1.5rem;
        background: #faf8fc;
        border-bottom: 1px solid #e8e0f0;
        flex-wrap: wrap;
        flex-shrink: 0;
    }

    .info-item {
        display: flex;
        flex-direction: column;
        gap: 0.1rem;
    }

    .info-label {
        font-size: 0.7rem;
        color: #9ca3af;
        text-transform: uppercase;
        letter-spacing: 0.02em;
    }

    .info-value {
        font-size: 0.9rem;
        color: #2d2141;
        font-weight: 500;
    }

    .info-value.status-submitted {
        color: #16a34a;
    }

    .info-value.grade {
        color: #2F124D;
        font-weight: 700;
    }

    /* Feedback Display */
    .feedback-display {
        margin: 1rem;
        padding: 1rem;
        background: #f0fdf4;
        border: 1px solid #bbf7d0;
        border-radius: 0.75rem;
        flex-shrink: 0;
    }

    .feedback-header {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        margin-bottom: 0.5rem;
    }

    .feedback-icon {
        font-size: 1.1rem;
    }

    .feedback-title {
        font-weight: 600;
        color: #166534;
        font-size: 0.9rem;
    }

    .feedback-text {
        margin: 0;
        color: #166534;
        font-size: 0.9rem;
        line-height: 1.5;
        white-space: pre-wrap;
    }

    /* Alerts */
    .alert {
        margin: 0;
        padding: 0.75rem 1.5rem;
        font-size: 0.9rem;
        flex-shrink: 0;
    }

    .alert-danger {
        background: #fef2f2;
        color: #dc2626;
    }

    .alert-success {
        background: #f0fdf4;
        color: #16a34a;
    }

    /* Chat Container */
    .chat-container {
        flex: 1;
        overflow-y: auto;
        padding: 1.5rem;
        background: #ffffff;
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
        min-height: 0;
    }

    .empty-chat {
        text-align: center;
        padding: 3rem;
        color: #6b647a;
    }

    .empty-icon {
        font-size: 2.5rem;
        display: block;
        margin-bottom: 0.5rem;
    }

    .empty-chat p {
        margin: 0;
    }

    /* Messages */
    .message {
        display: flex;
    }

    .message.user {
        justify-content: flex-end;
    }

    .message.bot {
        justify-content: flex-start;
    }

    .message-bubble {
        max-width: 80%;
        padding: 0.75rem 1rem;
        border-radius: 1rem;
        line-height: 1.5;
    }

    .message.user .message-bubble {
        background: linear-gradient(135deg, #2F124D, #4A1C74);
        color: white;
        border-bottom-right-radius: 0.25rem;
    }

    .message.bot .message-bubble {
        background: white;
        color: #2F124D;
        border: 1px solid #E6D9CC;
        border-bottom-left-radius: 0.25rem;
        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    }

    .message-text {
        white-space: pre-wrap;
        display: block;
    }

    .message-time {
        display: block;
        font-size: 0.7rem;
        opacity: 0.6;
        margin-top: 0.3rem;
        text-align: right;
    }

    /* Footer */
    .chat-footer {
        padding: 1.5rem;
        background: #faf8fc;
        border-top: 1px solid #e8e0f0;
        text-align: center;
        flex-shrink: 0;
    }

    .chat-footer p {
        margin: 0 0 1rem;
        color: #6b647a;
        font-size: 0.9rem;
    }

    .btn-back {
        display: inline-block;
        padding: 0.6rem 1.2rem;
        background: #2F124D;
        color: white;
        text-decoration: none;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        transition: all 0.15s;
    }

    .btn-back:hover {
        background: #4A1C74;
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
        background: #ffffff;
        border-radius: 1.25rem;
        width: 90%;
        max-width: 500px;
        max-height: 90vh;
        max-height: 90dvh;
        overflow: hidden;
        z-index: 999;
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
        display: flex;
        flex-direction: column;
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1rem 1.5rem;
        border-bottom: 1px solid #f0e8f8;
    }

    .modal-header h2 {
        margin: 0;
        font-size: 1.25rem;
        color: #2d2141;
    }

    .btn-close-modal {
        background: none;
        border: none;
        font-size: 1.25rem;
        cursor: pointer;
        color: #6b647a;
        padding: 0.25rem;
    }

    .modal-body {
        padding: 1.5rem;
        overflow-y: auto;
    }

    .student-info {
        display: flex;
        gap: 0.5rem;
        padding: 0.75rem;
        background: #faf8fc;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
    }

    .student-label {
        color: #6b647a;
        font-size: 0.9rem;
    }

    .student-name {
        font-weight: 600;
        color: #2d2141;
        font-size: 0.9rem;
    }

    .form-group {
        margin-bottom: 1rem;
    }

    .form-group label {
        display: block;
        margin-bottom: 0.5rem;
        font-weight: 500;
        color: #2d2141;
        font-size: 0.9rem;
    }

    .form-group textarea,
    .form-group input {
        width: 100%;
        padding: 0.75rem;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        font-family: inherit;
        font-size: 0.95rem;
        background: #faf8fc;
    }

    .form-group textarea:focus,
    .form-group input:focus {
        outline: none;
        border-color: #2F124D;
        background: #fff;
    }

    .form-hint {
        display: block;
        margin-top: 0.25rem;
        font-size: 0.75rem;
        color: #9ca3af;
    }

    .modal-footer {
        display: flex;
        justify-content: flex-end;
        gap: 0.75rem;
        padding: 1rem 1.5rem;
        border-top: 1px solid #f0e8f8;
        background: #ffffff;
    }

    .btn {
        padding: 0.6rem 1.25rem;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        font-weight: 500;
        cursor: pointer;
        border: none;
        transition: all 0.15s;
    }

    .btn-primary {
        background: #2F124D;
        color: white;
    }

    .btn-primary:hover {
        background: #4A1C74;
    }

    .btn-secondary {
        background: #f3f4f6;
        color: #374151;
    }

    .btn-secondary:hover {
        background: #e5e7eb;
    }

    @media (max-width: 600px) {
        .chat-header {
            flex-wrap: wrap;
        }

        .info-bar {
            gap: 1rem;
        }
    }
</style>
