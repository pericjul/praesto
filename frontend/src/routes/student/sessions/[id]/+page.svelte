<script>
    import { enhance } from "$app/forms";
    import { tick } from "svelte";

    export let data;
    export let form;

    let session = data.session;
    let messages = session?.messages ?? [];
    let messageInput = "";
    let chatContainer;
    let isSubmitting = false;

    // Scroll nach unten wenn neue Nachrichten kommen
    $: if (messages && chatContainer) {
        tick().then(() => {
            chatContainer.scrollTop = chatContainer.scrollHeight;
        });
    }

    // Nach form submit updaten
    $: if (form?.success && data.session) {
        session = data.session;
        messages = session.messages ?? [];
        messageInput = "";
        isSubmitting = false;
    }

    function formatTime(date) {
        if (!date) return "";
        return new Date(date).toLocaleTimeString("de-CH", {
            hour: "2-digit",
            minute: "2-digit"
        });
    }

    function formatDate(date) {
        if (!date) return "";
        return new Date(date).toLocaleDateString("de-CH", {
            weekday: "short",
            day: "numeric",
            month: "short",
            hour: "2-digit",
            minute: "2-digit"
        });
    }

    const isOpen = session?.status === "OPEN";
    const isClosed = session?.status === "CLOSED";
</script>

<svelte:head>
    <title>Training Session – Praesto</title>
</svelte:head>

<div class="chat-page">
    <!-- Header -->
    <header class="chat-header">
        <a href="/student/sessions" class="back-link">← Zurück</a>
        <div class="header-info">
            <h1>KI-Bewerbungsgespräch</h1>
            <div class="header-meta">
                <span class="session-status {isOpen ? 'status-open' : 'status-closed'}">
                    {isOpen ? "🟢 Aktiv" : "⚫ Beendet"}
                </span>
                <span class="session-date">
                    Gestartet: {formatDate(session?.startedAt)}
                </span>
            </div>
        </div>
        {#if isOpen}
            <form method="POST" action="?/close" use:enhance>
                <button type="submit" class="btn btn-danger">
                    ⏹️ Session beenden
                </button>
            </form>
        {/if}
    </header>

    {#if form?.error}
        <div class="alert alert-danger">
            {form.error}
        </div>
    {/if}

    <!-- Chat Container -->
    <div class="chat-container" bind:this={chatContainer}>
        {#if messages.length === 0}
            <div class="empty-chat">
                <p>Die KI bereitet das Gespräch vor...</p>
            </div>
        {:else}
            {#each messages as msg}
                <div class="message {msg.role === 'USER' ? 'message-user' : 'message-assistant'}">
                    <div class="message-bubble">
                        <div class="message-content">
                            {msg.content}
                        </div>
                        <div class="message-time">
                            {formatTime(msg.createdAt)}
                        </div>
                    </div>
                </div>
            {/each}
        {/if}
    </div>

    <!-- Input -->
    {#if isOpen}
        <form 
            method="POST" 
            action="?/send" 
            class="chat-input-form"
            use:enhance={() => {
                isSubmitting = true;
                return async ({ update }) => {
                    await update();
                    isSubmitting = false;
                };
            }}
        >
            <div class="input-wrapper">
                <textarea
                    name="message"
                    bind:value={messageInput}
                    placeholder="Schreibe deine Antwort..."
                    rows="2"
                    disabled={isSubmitting}
                ></textarea>
                <button type="submit" class="btn btn-send" disabled={isSubmitting || !messageInput.trim()}>
                    {#if isSubmitting}
                        ⏳
                    {:else}
                        ➤
                    {/if}
                </button>
            </div>
            <p class="input-hint">
                💡 Antworte so, wie du es in einem echten Bewerbungsgespräch tun würdest.
            </p>
        </form>
    {:else}
        <div class="closed-notice">
            <p>Diese Session wurde beendet. Du kannst den Verlauf ansehen, aber keine neuen Nachrichten senden.</p>
            <a href="/student/sessions" class="btn btn-primary">Zurück zur Übersicht</a>
        </div>
    {/if}
</div>

<style>
    .chat-page {
        display: flex;
        flex-direction: column;
        height: calc(100vh - 60px);
        max-width: 900px;
        margin: 0 auto;
    }

    .chat-header {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1rem;
        background: #fff;
        border-bottom: 1px solid #e8e0f0;
        flex-wrap: wrap;
    }

    .back-link {
        color: #7c3aed;
        text-decoration: none;
        font-size: 0.9rem;
    }

    .back-link:hover {
        text-decoration: underline;
    }

    .header-info {
        flex: 1;
    }

    .header-info h1 {
        margin: 0;
        font-size: 1.2rem;
        color: #3b134f;
    }

    .header-meta {
        display: flex;
        gap: 1rem;
        align-items: center;
        margin-top: 0.25rem;
    }

    .session-status {
        font-size: 0.8rem;
        font-weight: 500;
    }

    .status-open {
        color: #16a34a;
    }

    .status-closed {
        color: #6b7280;
    }

    .session-date {
        font-size: 0.8rem;
        color: #7c6b80;
    }

    .chat-container {
        flex: 1;
        overflow-y: auto;
        padding: 1rem;
        background: #faf8fc;
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
    }

    .empty-chat {
        text-align: center;
        color: #7c6b80;
        padding: 2rem;
    }

    .message {
        display: flex;
    }

    .message-user {
        justify-content: flex-end;
    }

    .message-assistant {
        justify-content: flex-start;
    }

    .message-bubble {
        max-width: 80%;
        padding: 0.75rem 1rem;
        border-radius: 1rem;
    }

    .message-user .message-bubble {
        background: #7c3aed;
        color: #fff;
        border-bottom-right-radius: 0.25rem;
    }

    .message-assistant .message-bubble {
        background: #fff;
        color: #3b134f;
        border: 1px solid #e8e0f0;
        border-bottom-left-radius: 0.25rem;
    }

    .message-content {
        white-space: pre-wrap;
        line-height: 1.5;
    }

    .message-time {
        font-size: 0.7rem;
        opacity: 0.7;
        margin-top: 0.25rem;
        text-align: right;
    }

    .chat-input-form {
        padding: 1rem;
        background: #fff;
        border-top: 1px solid #e8e0f0;
    }

    .input-wrapper {
        display: flex;
        gap: 0.5rem;
        align-items: flex-end;
    }

    .input-wrapper textarea {
        flex: 1;
        padding: 0.75rem;
        border: 1px solid #e8e0f0;
        border-radius: 0.75rem;
        resize: none;
        font-family: inherit;
        font-size: 0.95rem;
    }

    .input-wrapper textarea:focus {
        outline: none;
        border-color: #7c3aed;
    }

    .input-wrapper textarea:disabled {
        background: #f9fafb;
    }

    .btn-send {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background: #7c3aed;
        color: #fff;
        border: none;
        font-size: 1.2rem;
        cursor: pointer;
        transition: all 0.15s ease;
    }

    .btn-send:hover:not(:disabled) {
        background: #6d28d9;
    }

    .btn-send:disabled {
        background: #d1d5db;
        cursor: not-allowed;
    }

    .input-hint {
        margin: 0.5rem 0 0;
        font-size: 0.8rem;
        color: #7c6b80;
    }

    .closed-notice {
        padding: 1.5rem;
        background: #f9fafb;
        border-top: 1px solid #e8e0f0;
        text-align: center;
    }

    .closed-notice p {
        margin: 0 0 1rem;
        color: #6b7280;
    }

    .btn {
        padding: 0.5rem 1rem;
        border-radius: 0.5rem;
        font-size: 0.85rem;
        font-weight: 500;
        text-decoration: none;
        border: none;
        cursor: pointer;
    }

    .btn-primary {
        background: #7c3aed;
        color: #fff;
    }

    .btn-danger {
        background: #fef2f2;
        color: #dc2626;
        border: 1px solid #fecaca;
    }

    .btn-danger:hover {
        background: #fee2e2;
    }

    .alert {
        margin: 0.5rem 1rem;
        padding: 0.75rem;
        border-radius: 0.5rem;
    }

    .alert-danger {
        background: #fef2f2;
        color: #dc2626;
        border: 1px solid #fecaca;
    }
</style>
