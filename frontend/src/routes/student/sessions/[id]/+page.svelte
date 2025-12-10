<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { onMount } from "svelte";

    // Svelte 5: $props()
    let { data, form } = $props();

    // Svelte 5: $state()
    let messageInput = $state("");
    let isSubmitting = $state(false);
    let chatContainer = $state(null);
    let optimisticMessages = $state([]); // Für sofortige Anzeige

    // Derived state
    let session = $derived(data?.session ?? null);
    let serverMessages = $derived(data?.session?.messages ?? []);
    let isOpen = $derived(data?.session?.status === "OPEN");

    // Kombinierte Messages: Server + Optimistic
    let messages = $derived([...serverMessages, ...optimisticMessages]);

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
</script>

<svelte:head>
    <title>KI-Training – Praesto</title>
</svelte:head>

<div class="chat-wrapper">
    <div class="chat-page">
        <!-- Header -->
        <header class="chat-header">
            <a href="/student/sessions" class="back-link">← Zurück</a>
            <div class="header-content">
                <h1>🤖 KI-Bewerbungstraining</h1>
                <p>Übe Vorstellungsgespräche mit deinem persönlichen KI-Coach</p>
            </div>
            {#if isOpen}
                <form method="POST" action="?/close" use:enhance class="close-form">
                    <button type="submit" class="btn-close">
                        Beenden
                    </button>
                </form>
            {:else}
                <span class="status-badge closed">Abgeschlossen</span>
            {/if}
        </header>

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
                        Hallo! 👋 Ich bin dein Bewerbungscoach. Ich helfe dir, dich auf Vorstellungsgespräche vorzubereiten. Sag mir, für welchen Beruf du dich interessierst!
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
        {#if isOpen}
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
                        placeholder="Schreibe deine Antwort..."
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
                <p class="hint">💡 Drücke Enter zum Senden, Shift+Enter für neue Zeile</p>
            </div>
        {:else}
            <div class="closed-notice">
                <p>📋 Diese Session wurde abgeschlossen. Du kannst den Verlauf ansehen.</p>
                <a href="/student/sessions" class="btn-back">← Zurück zur Übersicht</a>
            </div>
        {/if}
    </div>
</div>

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

    /* Glassy "Beenden"-Button, etwas größer */
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
        transition:
            border-color 0.15s ease,
            box-shadow 0.15s ease,
            transform 0.15s ease;

        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
    }

    .btn-close:hover {
        background: rgba(255, 255, 255, 0.22);
        border-color: rgba(255, 255, 255, 0.6);
        box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
        transform: translateY(-1px);
    }

    .btn-close:active {
        transform: translateY(0);
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
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

    /* Chat Container - scrollbar nur hier */
    .chat-container {
        flex: 1;
        overflow-y: auto;
        padding: 1rem;
        background: #ffffff;
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
        min-height: 0; /* Wichtig für Flex-Scroll */
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
</style>