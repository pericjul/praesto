<script>
  export let data;

  const API_BASE = "http://localhost:8080/api";

  // Session und Messages aus dem Load
  let session = data.session ?? {};
  let messages = session.messages ?? [];

  let userInput = "";

  function formatDateTime(date) {
    if (!date) return "";
    return new Date(date).toLocaleString("de-CH", {
      dateStyle: "short",
      timeStyle: "short"
    });
  }

  const startedAtText = formatDateTime(session.startedAt);

  // 📨 Nachricht an Backend schicken
  async function sendMessage() {
    const text = userInput.trim();
    if (!text) return;

    // Eingabe leeren
    userInput = "";

    // Optimistisch: eigene Nachricht sofort anzeigen
    messages = [
      ...messages,
      {
        role: "USER",
        content: text,
        createdAt: new Date().toISOString()
      }
    ];

    try {
      const res = await fetch(`${API_BASE}/sessions/${session.id}/messages`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
          // Falls du später Authorization-Header brauchst:
          // "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ message: text })
      });

      if (!res.ok) {
        console.error("Fehler beim Senden der Nachricht", await res.text());
        return;
      }

      // Backend schickt die komplette aktualisierte Session zurück
      const updated = await res.json();
      session = updated;
      messages = updated.messages ?? messages;
    } catch (err) {
      console.error("Netzwerkfehler beim Senden der Nachricht", err);
    }
  }
</script>

<svelte:head>
  <title>Praesto – KI-Training</title>
</svelte:head>

<div class="training-page">
  <header class="training-header">
    <div>
      <h1>KI-Bewerbungsgespraech</h1>
      <p class="subtitle">
        Deine Session wurde gestartet. Antworte Schritt fuer Schritt auf die Fragen,
        so wie in einem echten Gespraech.
      </p>
      <p class="meta">
        Session-ID: <span>{session.id?.slice(-6) ?? "–"}</span> ·
        gestartet am <span>{startedAtText}</span>
      </p>
    </div>
  </header>

  <main class="chat-card">
    <div class="chat-messages">
      {#if messages.length === 0}
        <div class="chat-message assistant">
          <div class="bubble">
            <p>Die KI bereitet gerade das Gespraech vor …</p>
          </div>
        </div>
      {:else}
        {#each messages as msg}
          <div class="chat-message {msg.role === 'USER' ? 'user' : 'assistant'}">
            <div class="bubble">
              <p>{msg.content}</p>
            </div>
          </div>
        {/each}
      {/if}
    </div>

    <div class="chat-hint">
      ⭐ Tipp: Nimm dir Zeit fuer deine Antwort und schreibe so,
      wie du es in einem echten Bewerbungsgespraech sagen wuerdest.
    </div>

    <!-- 🔥 Jetzt wirklich sendbar -->
    <form class="chat-input-row" on:submit|preventDefault={sendMessage}>
      <!-- svelte-ignore element_invalid_self_closing_tag -->
      <textarea
        rows="2"
        bind:value={userInput}
        placeholder="Schreibe hier deine Antwort auf die letzte Frage …"
      />
      <button type="submit">
        Senden
      </button>
    </form>
  </main>
</div>

<style>
  :global(body) {
    background-color: #fdf7ef;
  }

  .training-page {
    max-width: 960px;
    margin: 0 auto;
    padding: 1.5rem 1rem 3rem;
  }

  .training-header h1 {
    margin: 0;
    font-size: 1.6rem;
    font-weight: 650;
    color: #3b134f;
  }

  .training-header .subtitle {
    margin: 0.4rem 0 0.2rem;
    font-size: 0.95rem;
    color: #6b5a70;
  }

  .training-header .meta {
    margin: 0.1rem 0 1rem;
    font-size: 0.8rem;
    color: #9a8b9d;
  }

  .training-header .meta span {
    font-weight: 500;
  }

  .chat-card {
    background: #fffdf9;
    border-radius: 1rem;
    border: 1px solid #f0e1c7;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
    padding: 1rem 1.1rem 0.9rem;
    display: flex;
    flex-direction: column;
    gap: 0.8rem;
    min-height: 320px;
  }

  .chat-messages {
    flex: 1;
    max-height: 460px;
    overflow-y: auto;
    padding-right: 0.4rem;
  }

  .chat-message {
    display: flex;
    margin-bottom: 0.6rem;
  }

  .chat-message.assistant {
    justify-content: flex-start;
  }

  .chat-message.user {
    justify-content: flex-end;
  }

  .bubble {
    max-width: 80%;
    padding: 0.7rem 0.9rem;
    border-radius: 0.9rem;
    font-size: 0.9rem;
    line-height: 1.4;
  }

  .assistant .bubble {
    background: #f4ecff;
    border: 1px solid #dfd0ff;
    color: #3b134f;
  }

  .user .bubble {
    background: #ffe7c2;
    border: 1px solid #f1d1a3;
    color: #5b3713;
  }

  .chat-hint {
    font-size: 0.8rem;
    color: #8a7a7a;
    background: #fff7e6;
    border-radius: 0.6rem;
    padding: 0.4rem 0.6rem;
    border: 1px dashed #f1d7aa;
  }

  .chat-input-row {
    display: flex;
    gap: 0.6rem;
    align-items: flex-end;
    margin-top: 0.3rem;
  }

  .chat-input-row textarea {
    flex: 1;
    resize: vertical;
    border-radius: 0.7rem;
    border: 1px solid #e4d3bb;
    padding: 0.5rem 0.7rem;
    font-size: 0.9rem;
    font-family: inherit;
    background: #fbf4ea;
  }

  .chat-input-row button {
    border-radius: 999px;
    padding: 0.45rem 1.1rem;
    font-size: 0.85rem;
    border: none;
    background: #c7b6dd;
    color: #fff;
    cursor: pointer;
    transition: transform 0.08s ease, box-shadow 0.08s ease, background 0.08s ease;
  }

  .chat-input-row button:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.18);
    background: #b89dd7;
  }
</style>