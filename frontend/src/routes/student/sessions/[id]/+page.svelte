<script>
    import { enhance, applyAction } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { onMount, onDestroy } from "svelte";
    import { goto } from "$app/navigation";
    import { get } from "svelte/store";
    import { t } from "$lib/i18n";

    // Svelte 5: $props()
    let { data, form } = $props();

    // Svelte 5: $state()
    let messageInput = $state("");
    let isSubmitting = $state(false);
    let chatContainer = $state(null);
    let optimisticMessages = $state([]); // Für sofortige Anzeige
    let elapsedSeconds = $state(0);
    let timerInterval = $state(null);
    let showTimeUpModal = $state(false);
    let isSubmittingAssignment = $state(false);
    let isClosing = $state(false);

    // Derived state
    let session = $derived(data?.session ?? null);
    let serverMessages = $derived(data?.session?.messages ?? []);
    let isOpen = $derived(data?.session?.status === "OPEN");
    
    // Assignment-Info
    let isAssignment = $derived(!!session?.assignmentId);
    let assignmentTitle = $derived(session?.assignmentTitle ?? "");
    let targetDurationMin = $derived(session?.targetDurationMin ?? 0);
    let targetDurationSec = $derived(targetDurationMin * 60);
    let isSubmittedAsAssignment = $derived(session?.submittedAsAssignment ?? false);

    // Zeit-Berechnung
    let remainingSeconds = $derived(Math.max(0, targetDurationSec - elapsedSeconds));
    let isTimeUp = $derived(targetDurationSec > 0 && elapsedSeconds >= targetDurationSec);
    let progressPercent = $derived(targetDurationSec > 0 ? Math.min(100, (elapsedSeconds / targetDurationSec) * 100) : 0);

    // Kombinierte Messages: Server + Optimistic
    let messages = $derived([...serverMessages, ...optimisticMessages]);

    // Timer formatieren (MM:SS)
    function formatTimer(totalSeconds) {
        const mins = Math.floor(totalSeconds / 60);
        const secs = totalSeconds % 60;
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }

    // Scroll nach unten
    function scrollToBottom() {
        if (chatContainer) {
            setTimeout(() => {
                chatContainer.scrollTop = chatContainer.scrollHeight;
            }, 50);
        }
    }

    onMount(() => {
        scrollToBottom();
        
        // Timer starten wenn Session offen und Assignment
        if (isOpen && isAssignment && !isSubmittedAsAssignment) {
            // Berechne bereits verstrichene Zeit
            if (session?.startedAt) {
                const startTime = new Date(session.startedAt).getTime();
                const now = Date.now();
                elapsedSeconds = Math.floor((now - startTime) / 1000);
            }
            
            timerInterval = setInterval(() => {
                elapsedSeconds += 1;
                
                // Zeit abgelaufen? Modal zeigen (nur einmal)
                if (isTimeUp && !showTimeUpModal) {
                    showTimeUpModal = true;
                }
            }, 1000);
        }
    });

    onDestroy(() => {
        if (timerInterval) {
            clearInterval(timerInterval);
        }
    });

    // Nach Messages-Änderung scrollen
    $effect(() => {
        if (messages.length > 0) {
            scrollToBottom();
        }
    });

    // Enter zum Senden (Shift+Enter für neue Zeile)
    function handleKeydown(event) {
        if (event.key === "Enter" && !event.shiftKey) {
            event.preventDefault();
            const form = event.target.closest("form");
            if (form && messageInput.trim() && !isSubmitting) {
                form.requestSubmit();
            }
        }
    }

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

    // Session als Aufgabe abgeben
    async function submitAsAssignment() {
        if (isSubmittingAssignment) return;
        isSubmittingAssignment = true;
        
        try {
            const response = await fetch(`/api/sessions/${session.id}/submit`, {
                method: "PUT"
            });
            
            if (response.ok) {
                showTimeUpModal = false;
                goto("/student/assignments");
            } else {
                const error = await response.text();
                alert(get(t)("schat.submitError") + error);
            }
        } catch (err) {
            console.error("Fehler:", err);
            alert(get(t)("schat.connectionError"));
        } finally {
            isSubmittingAssignment = false;
        }
    }

    function continueTraining() {
        showTimeUpModal = false;
    }
</script>

<svelte:head>
    <title>{isAssignment ? assignmentTitle : $t('schat.headTraining')} – Praesto</title>
</svelte:head>

<div class="chat-wrapper">
    <div class="chat-page">
        <!-- Header -->
        <header class="chat-header">
            <a href={isAssignment ? "/student/assignments" : "/student/sessions"} class="back-link">{$t('schat.back')}</a>
            <div class="header-content">
                <h1>🤖 {isAssignment ? assignmentTitle : $t('schat.titleTraining')}</h1>
                {#if isAssignment}
                    <p>{$t('schat.assignmentFromTeacher')}</p>
                {:else}
                    <p>{$t('schat.trainingSubtitle')}</p>
                {/if}
            </div>
            
            {#if isOpen && !isSubmittedAsAssignment}
                {#if isAssignment}
                    <button 
                        type="button" 
                        class="btn-submit"
                        onclick={submitAsAssignment}
                        disabled={isSubmittingAssignment}
                    >
                        {isSubmittingAssignment ? "..." : $t('schat.submit')}
                    </button>
                {:else}
                    <form
                        method="POST"
                        action="?/close"
                        class="close-form"
                        use:enhance={() => {
                            isClosing = true;
                            return async ({ result }) => {
                                if (result.type === "redirect") {
                                    await goto(result.location);
                                } else {
                                    isClosing = false;
                                    await applyAction(result);
                                }
                            };
                        }}
                    >
                        <button type="submit" class="btn-close" disabled={isClosing}>
                            {isClosing ? $t('schat.ending') : $t('schat.end')}
                        </button>
                    </form>
                {/if}
            {:else if isSubmittedAsAssignment}
                <span class="status-badge submitted">{$t('schat.submitted')}</span>
            {:else}
                <span class="status-badge closed">{$t('schat.closed')}</span>
            {/if}
        </header>

        <!-- Timer Bar (nur bei Assignments) -->
        {#if isAssignment && targetDurationMin > 0 && isOpen && !isSubmittedAsAssignment}
            <div class="timer-bar" class:time-up={isTimeUp}>
                <div class="timer-progress" style="width: {progressPercent}%"></div>
                <div class="timer-content">
                    <span class="timer-label">
                        {#if isTimeUp}
                            {$t('schat.timeUp')}
                        {:else}
                            {$t('schat.remaining')} {formatTimer(remainingSeconds)}
                        {/if}
                    </span>
                    <span class="timer-info">
                        {$t('schat.goalPrefix')} {targetDurationMin} {$t('schat.goalUnit')}
                    </span>
                </div>
            </div>
        {/if}

        {#if form?.error}
            <div class="alert alert-danger">
                {form.error}
            </div>
        {/if}

        <!-- Chat Container -->
        <div class="chat-container" bind:this={chatContainer}>
            {#if messages.length === 0 && !isSubmitting}
                <div class="message bot">
                    <div class="message-bubble">
                        {$t('schat.greeting')}
                    </div>
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

            {#if isSubmitting}
                <div class="message bot">
                    <div class="message-bubble typing">
                        <span></span><span></span><span></span>
                    </div>
                </div>
            {/if}
        </div>

        <!-- Input Area -->
        {#if isOpen && !isSubmittedAsAssignment}
            <div class="chat-input-area">
                <form
                    method="POST"
                    action="?/send"
                    class="input-form"
                    use:enhance={({ formData }) => {
                        const userText = messageInput.trim();
                        if (!userText) return;

                        // Optimistisch: Nachricht sofort anzeigen
                        optimisticMessages = [{
                            role: "USER",
                            content: userText,
                            createdAt: new Date().toISOString()
                        }];

                        isSubmitting = true;
                        messageInput = "";
                        scrollToBottom();

                        return async ({ result }) => {
                            isSubmitting = false;
                            optimisticMessages = []; // Optimistic Messages entfernen
                            
                            if (result.type === 'success') {
                                await invalidateAll();
                                scrollToBottom();
                            }
                        };
                    }}
                >
                    <textarea
                        name="message"
                        bind:value={messageInput}
                        placeholder={$t('schat.placeholder')}
                        rows="2"
                        disabled={isSubmitting}
                        onkeydown={handleKeydown}
                    ></textarea>
                    <button type="submit" class="btn-send" disabled={isSubmitting || !messageInput.trim()}>
                        {#if isSubmitting}
                            ⏳
                        {:else}
                            ➤
                        {/if}
                    </button>
                </form>
                <p class="hint">{$t('schat.hint')}</p>
            </div>
        {:else}
            <div class="closed-notice">
                {#if isSubmittedAsAssignment}
                    <p>{$t('schat.submittedNotice')}</p>
                {:else}
                    <p>{$t('schat.closedNotice')}</p>
                {/if}
                <a href={isAssignment ? "/student/assignments" : "/student/sessions"} class="btn-back">
                    ← {isAssignment ? $t('schat.toAssignments') : $t('schat.toOverview')}
                </a>
            </div>
        {/if}
    </div>
</div>

<!-- Time Up Modal -->
{#if showTimeUpModal}
    <div class="modal-backdrop"></div>
    <div class="time-up-modal">
        <div class="modal-icon">⏰</div>
        <h2>{$t('schat.modalTitle')}</h2>
        <p>{$t('schat.modalText1a')} <strong>{targetDurationMin} {$t('schat.modalText1b')}</strong> {$t('schat.modalText1c')}</p>
        <p>{$t('schat.modalText2')}</p>
        <div class="modal-actions">
            <button type="button" class="btn-secondary" onclick={continueTraining}>
                {$t('schat.continueTraining')}
            </button>
            <button
                type="button"
                class="btn-primary"
                onclick={submitAsAssignment}
                disabled={isSubmittingAssignment}
            >
                {isSubmittingAssignment ? $t('schat.submitting') : $t('schat.submitNow')}
            </button>
        </div>
    </div>
{/if}

<style>
    /* Wrapper verhindert Page-Scroll */
    .chat-wrapper {
        position: fixed;
        top: 54px; /* Höhe der Navbar */
        left: 0;
        right: 0;
        bottom: 0;
        overflow: hidden;
        background: #FBF7F1;
        display: flex;
        justify-content: center;
        z-index: 1;
    }

    .chat-page {
        display: flex;
        flex-direction: column;
        height: 100%;
        max-width: 800px;
        margin: 0 auto;
        padding: 1rem;
    }

    /* Header */
    .chat-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
        padding: 0.875rem 1.25rem;
        background: linear-gradient(135deg, #2F124D, #4A1C74);
        color: white;
        border-radius: 1rem 1rem 0 0;
        flex-shrink: 0;
    }

    .back-link {
        color: rgba(255,255,255,0.85);
        text-decoration: none;
        font-size: 0.9rem;
        flex-shrink: 0;
    }

    .back-link:hover {
        color: white;
    }

    .header-content {
        flex: 1;
        text-align: center;
        min-width: 0; /* Verhindert Overflow */
    }

    .header-content h1 {
        margin: 0;
        font-size: 1.25rem;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .header-content p {
        margin: 0.25rem 0 0;
        opacity: 0.9;
        font-size: 0.8rem;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .close-form {
        margin: 0;
        flex-shrink: 0;
        display: flex;
        align-items: center;
    }

    /* Glassy "Beenden"-Button */
    .btn-close {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 0.4rem 2.5rem;
        border-radius: 999px;
        background: rgba(255, 255, 255, 0.14);
        border: 1px solid rgba(255, 255, 255, 0.35);
        backdrop-filter: blur(10px);
        color: #FDF9FF;
        font-size: 0.85rem;
        font-weight: 500;
        line-height: 1;
        white-space: nowrap;
        cursor: pointer;
        transition: all 0.15s ease;
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
    }

    .btn-close:hover {
        background: rgba(255, 255, 255, 0.22);
        border-color: rgba(255, 255, 255, 0.6);
    }

    /* Abgeben Button */
    .btn-submit {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 0.5rem 1.5rem;
        border-radius: 999px;
        background: #10b981;
        border: none;
        color: white;
        font-size: 0.9rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.15s ease;
    }

    .btn-submit:hover:not(:disabled) {
        background: #059669;
        transform: translateY(-1px);
    }

    .btn-submit:disabled {
        opacity: 0.7;
        cursor: not-allowed;
    }

    .status-badge {
        padding: 0.5rem 1.25rem;
        border-radius: 2rem;
        font-size: 0.85rem;
        font-weight: 600;
        white-space: nowrap;
        flex-shrink: 0;
    }

    .status-badge.closed {
        background: rgba(255,255,255,0.15);
        color: rgba(255,255,255,0.9);
        border: 1px solid rgba(255,255,255,0.3);
    }

    .status-badge.submitted {
        background: #10b981;
        color: white;
    }

    /* Timer Bar */
    .timer-bar {
        position: relative;
        padding: 0.75rem 1rem;
        background: #f0fdf4;
        border-bottom: 1px solid #bbf7d0;
        overflow: hidden;
    }

    .timer-bar.time-up {
        background: #fef2f2;
        border-color: #fecaca;
    }

    .timer-progress {
        position: absolute;
        top: 0;
        left: 0;
        bottom: 0;
        background: #10b981;
        opacity: 0.15;
        transition: width 1s linear;
    }

    .timer-bar.time-up .timer-progress {
        background: #dc2626;
    }

    .timer-content {
        position: relative;
        display: flex;
        justify-content: space-between;
        align-items: center;
        z-index: 1;
    }

    .timer-label {
        font-weight: 600;
        color: #059669;
        font-size: 0.95rem;
    }

    .timer-bar.time-up .timer-label {
        color: #dc2626;
    }

    .timer-info {
        font-size: 0.85rem;
        color: #6b7280;
    }

    /* Alert */
    .alert {
        margin: 0;
        padding: 0.75rem 1rem;
        font-size: 0.9rem;
        flex-shrink: 0;
    }

    .alert-danger {
        background: #fef2f2;
        color: #dc2626;
        border-left: 3px solid #dc2626;
    }

    /* Chat Container */
    .chat-container {
        flex: 1;
        overflow-y: auto;
        padding: 1rem;
        background: #ffffff;
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
        min-height: 0;
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

    /* Typing Animation */
    .typing {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 1rem !important;
    }

    .typing span {
        width: 8px;
        height: 8px;
        background: #2F124D;
        border-radius: 50%;
        animation: bounce 1.4s infinite ease-in-out both;
    }

    .typing span:nth-child(1) { animation-delay: -0.32s; }
    .typing span:nth-child(2) { animation-delay: -0.16s; }

    @keyframes bounce {
        0%, 80%, 100% { transform: scale(0); }
        40% { transform: scale(1); }
    }

    /* Input Area */
    .chat-input-area {
        padding: 1rem;
        background: white;
        border-top: 1px solid #E6D9CC;
        border-radius: 0 0 1rem 1rem;
        flex-shrink: 0;
    }

    .input-form {
        display: flex;
        gap: 0.5rem;
        align-items: flex-end;
    }

    .input-form textarea {
        flex: 1;
        padding: 0.75rem;
        border: 1px solid #E6D9CC;
        border-radius: 0.75rem;
        font-family: inherit;
        font-size: 1rem;
        resize: none;
        background: #FBF7F1;
        min-height: 44px;
        max-height: 120px;
    }

    .input-form textarea:focus {
        outline: none;
        border-color: #2F124D;
        background: white;
    }

    .input-form textarea:disabled {
        opacity: 0.6;
    }

    .btn-send {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background: #2F124D;
        color: white;
        border: none;
        font-size: 1.2rem;
        cursor: pointer;
        flex-shrink: 0;
        transition: all 0.2s;
    }

    .btn-send:hover:not(:disabled) {
        background: #4A1C74;
        transform: scale(1.05);
    }

    .btn-send:disabled {
        background: #E6D9CC;
        cursor: not-allowed;
    }

    .hint {
        margin: 0.5rem 0 0;
        font-size: 0.75rem;
        color: #8E7F9A;
    }

    /* Closed Notice */
    .closed-notice {
        padding: 1.5rem;
        background: #F8F3EB;
        border-top: 1px solid #E6D9CC;
        border-radius: 0 0 1rem 1rem;
        text-align: center;
        flex-shrink: 0;
    }

    .closed-notice p {
        margin: 0 0 1rem;
        color: #5E4C6F;
    }

    .btn-back {
        display: inline-block;
        padding: 0.6rem 1.2rem;
        background: #2F124D;
        color: white;
        text-decoration: none;
        border-radius: 0.5rem;
        font-size: 0.9rem;
    }

    .btn-back:hover {
        background: #4A1C74;
    }

    /* Time Up Modal */
    .modal-backdrop {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.6);
        backdrop-filter: blur(4px);
        z-index: 1000;
    }

    .time-up-modal {
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: white;
        border-radius: 1rem;
        padding: 2rem;
        width: 90%;
        max-width: 400px;
        max-height: 90vh;
        max-height: 90dvh;
        overflow-y: auto;
        text-align: center;
        z-index: 1001;
        box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
    }

    .modal-icon {
        font-size: 3rem;
        margin-bottom: 1rem;
    }

    .time-up-modal h2 {
        margin: 0 0 0.75rem;
        color: #2d2141;
    }

    .time-up-modal p {
        margin: 0 0 0.5rem;
        color: #6b647a;
        font-size: 0.95rem;
    }

    .modal-actions {
        display: flex;
        gap: 0.75rem;
        justify-content: center;
        margin-top: 1.5rem;
    }

    .btn-primary {
        padding: 0.75rem 1.5rem;
        background: #10b981;
        color: white;
        border: none;
        border-radius: 0.5rem;
        font-size: 0.95rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.15s;
    }

    .btn-primary:hover:not(:disabled) {
        background: #059669;
    }

    .btn-primary:disabled {
        opacity: 0.7;
        cursor: not-allowed;
    }

    .btn-secondary {
        padding: 0.75rem 1.5rem;
        background: #f3f4f6;
        color: #374151;
        border: none;
        border-radius: 0.5rem;
        font-size: 0.95rem;
        font-weight: 500;
        cursor: pointer;
    }

    .btn-secondary:hover {
        background: #e5e7eb;
    }

    /* ===== Handy: randlos & kompakt, damit der Chat sauber in den Screen passt ===== */
    @media (max-width: 640px) {
        /* Keine seitlichen Ränder verschwenden – Chat füllt die volle Breite */
        .chat-page {
            padding: 0;
            max-width: 100%;
        }

        /* Ecken weg → randlos statt "schwebende Karte" */
        .chat-header {
            border-radius: 0;
            padding: 0.55rem 0.75rem;
            gap: 0.5rem;
        }
        .header-content h1 { font-size: 1rem; }
        .header-content p { font-size: 0.7rem; }
        .back-link { font-size: 0.8rem; }

        /* Buttons schmaler: das grosse horizontale Padding sprengt sonst den Screen */
        .btn-close { padding: 0.4rem 0.9rem; font-size: 0.8rem; }
        .btn-submit { padding: 0.45rem 0.9rem; font-size: 0.8rem; }
        .status-badge { padding: 0.4rem 0.8rem; font-size: 0.75rem; }

        /* Timer kompakter */
        .timer-bar { padding: 0.5rem 0.8rem; }
        .timer-label { font-size: 0.85rem; }
        .timer-info { font-size: 0.72rem; }

        /* Mehr Platz für die Sprechblasen */
        .chat-container { padding: 0.75rem; gap: 0.6rem; }
        .message-bubble { max-width: 88%; }

        /* Eingabe & Hinweise randlos */
        .chat-input-area { border-radius: 0; padding: 0.6rem 0.75rem; }
        .closed-notice { border-radius: 0; padding: 1.25rem 1rem; }
    }
</style>