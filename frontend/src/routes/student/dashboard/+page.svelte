<script>
  export let data;

  const dashboard = data.dashboard ?? {};

  const {
    studentName = "Student",
    openAssignmentsCount = 0,
    nextAssignmentTitle,
    nextAssignmentDueDate,
    lastSessionStartedAt,
    totalSessionsCount = 0,
    badgesCount = 0,
    notesCount = 0
  } = dashboard;

  function formatDate(date) {
    if (!date) return "-";
    return new Date(date).toLocaleDateString("de-CH");
  }

  function formatDateTime(date) {
    if (!date) return "-";
    return new Date(date).toLocaleString("de-CH", {
      dateStyle: "short",
      timeStyle: "short"
    });
  }
</script>

<svelte:head>
  <title>Praesto – Dashboard</title>
</svelte:head>

<div class="dashboard-page">
  <!-- HERO -->
  <section class="hero-card">
    <div class="hero-left">
      <p class="hero-eyebrow">Willkommen zurück 👋</p>
      <h1 class="hero-title">Hi {studentName}</h1>
      <p class="hero-subtitle">
        Hier findest du alles, um dich optimal auf Bewerbungen vorzubereiten.
        Aufgaben, KI-Trainings und persönliche Notizen – übersichtlich an einem Ort.
      </p>


      <!-- NEU: klare Actions -->
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
      <div class="hero-stat">
        <span class="hero-stat-label">Badges</span>
        <span class="hero-stat-value">{badgesCount}</span>
      </div>
    </div>
  </section>

  <!-- GRID -->
  <section class="grid">
    <!-- Aufgaben -->
    <article class="card">
      <header class="card-header">
        <div class="card-title-row">
          <span class="card-icon">📚</span>
          <div>
            <h2 class="card-title">Aufgaben</h2>
            <p class="card-subtitle">Deine offenen Aufgaben & nächste Steps.</p>
          </div>
        </div>
        <span class="card-tag">
          {openAssignmentsCount} offen
        </span>
      </header>

      <div class="card-body">
        {#if openAssignmentsCount === 0}
          <p class="card-empty">
            Du hast aktuell keine offenen Aufgaben. Starte ein KI-Training oder frage deine Lehrperson nach neuen Aufgaben.
          </p>
        {:else}
          <div class="card-highlight">
            <p class="highlight-label">Nächste Aufgabe</p>
            <p class="highlight-title">
              {nextAssignmentTitle ?? "Aufgabe ohne Titel"}
            </p>
            <p class="highlight-meta">
              ⏰ fällig am {formatDate(nextAssignmentDueDate)}
            </p>
          </div>
        {/if}
      </div>

      <footer class="card-footer">
        <a href="/student/assignments" class="btn btn-inline">Aufgaben öffnen</a>
      </footer>
    </article>

    <!-- KI-Trainings -->
    <article class="card">
      <header class="card-header">
        <div class="card-title-row">
          <span class="card-icon">🤖</span>
          <div>
            <h2 class="card-title">KI-Trainings</h2>
            <p class="card-subtitle">Interview üben – mit Feedback.</p>
          </div>
        </div>
        <span class="card-tag">
          {totalSessionsCount} Sessions
        </span>
      </header>

      <div class="card-body">
        {#if totalSessionsCount === 0}
          <p class="card-empty">
            Du hast noch keine KI-Trainings gestartet.
            Wähle eine Aufgabe und starte ein simuliertes Bewerbungsgespräch mit der KI.
          </p>
        {:else}
          <div class="card-highlight">
            <p class="highlight-label">Letztes Training</p>
            <p class="highlight-title">
              Session vom {formatDateTime(lastSessionStartedAt)}
            </p>
            <p class="highlight-meta">
              💬 Du kannst jederzeit eine neue Session starten.
            </p>
          </div>
        {/if}
      </div>

      <footer class="card-footer">
        <a href="/student/sessions" class="btn btn-inline">Training starten</a>
      </footer>
    </article>

    <!-- Badges -->
    <article class="card">
      <header class="card-header">
        <div class="card-title-row">
          <span class="card-icon">🏅</span>
          <div>
            <h2 class="card-title">Badges</h2>
            <p class="card-subtitle">Deine kleinen Erfolge.</p>
          </div>
        </div>
        <span class="card-tag">
          {badgesCount} Badges
        </span>
      </header>

      <div class="card-body">
        {#if badgesCount === 0}
          <p class="card-empty">
            Sammle Badges, indem du Aufgaben erledigst und Interviews übst.
            So siehst du deinen Fortschritt auf einen Blick.
          </p>
        {:else}
          <p class="card-empty">
            Du hast bereits {badgesCount} Badges gesammelt. 🎉
          </p>
        {/if}
      </div>

      <footer class="card-footer card-footer--disabled">
        <span class="btn btn-inline disabled">Badges bald verfügbar</span>
      </footer>
    </article>

    <!-- Notizen -->
    <article class="card">
      <header class="card-header">
        <div class="card-title-row">
          <span class="card-icon">📝</span>
          <div>
            <h2 class="card-title">Notizen</h2>
            <p class="card-subtitle">Gedanken zu Firmen, Gesprächen & Bewerbungen.</p>
          </div>
        </div>
        <span class="card-tag">
          {notesCount} Notizen
        </span>
      </header>

      <div class="card-body">
        {#if notesCount === 0}
          <p class="card-empty">
            Halte hier fest, wie Gespräche gelaufen sind, was dir an Firmen gefallen hat
            und welche Fragen du beim nächsten Mal besser beantworten möchtest.
          </p>
        {:else}
          <p class="card-empty">
            Du hast {notesCount} Notizen gespeichert. Perfekt, um den Überblick zu behalten.
          </p>
        {/if}
      </div>

      <footer class="card-footer">
        <a href="/student/notes" class="btn btn-inline">Notizen öffnen</a>
      </footer>
    </article>
  </section>
</div>

<style>
  :global(body) {
    background-color: #fdf7ef;
  }

  .dashboard-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 1.5rem 1rem 3rem;
  }

  /* HERO */
  .hero-card {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    padding: 1.6rem 1.8rem;
    border-radius: 1.25rem;
    background: linear-gradient(120deg, #3b134f, #f29f3f);
    color: #fff;
    margin-bottom: 1.8rem;
    box-shadow: 0 14px 35px rgba(0, 0, 0, 0.18);
  }

  @media (min-width: 768px) {
    .hero-card {
      flex-direction: row;
      justify-content: space-between;
      align-items: center;
    }
  }

  .hero-left {
    max-width: 60ch;
  }

  .hero-eyebrow {
    margin: 0 0 0.3rem;
    font-size: 0.85rem;
    opacity: 0.9;
  }

  .hero-title {
    margin: 0;
    font-size: 1.9rem;
    font-weight: 700;
  }

  .hero-subtitle {
    margin: 0.5rem 0 1rem;
    font-size: 0.98rem;
    opacity: 0.9;
  }

  .hero-actions {
    margin-top: 1.1rem;
    display: flex;
    flex-wrap: wrap;
    gap: 0.6rem;
  }

  .hero-right {
    display: flex;
    gap: 0.75rem;
  }

  @media (max-width: 767px) {
    .hero-right {
      width: 100%;
      justify-content: space-between;
    }
  }

  .hero-stat {
    background: rgba(255, 255, 255, 0.12);
    border-radius: 0.9rem;
    padding: 0.6rem 0.9rem;
    min-width: 90px;
    text-align: left;
  }

  .hero-stat-label {
    display: block;
    font-size: 0.75rem;
    opacity: 0.9;
  }

  .hero-stat-value {
    display: block;
    font-weight: 700;
    font-size: 1.1rem;
    margin-top: 0.1rem;
  }

  /* Buttons */
  .btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 999px;
    font-size: 0.86rem;
    font-weight: 500;
    text-decoration: none;
    cursor: pointer;
    border: none;
    outline: none;
    transition: transform 0.08s ease, box-shadow 0.08s ease, background 0.08s ease,
      color 0.08s ease;
    white-space: nowrap;
  }

  .btn-primary {
    padding: 0.45rem 1.1rem;
    background: #fbe4b2;
    color: #3b134f;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  }

  .btn-primary:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 14px rgba(0, 0, 0, 0.25);
    background: #ffe2a2;
  }

  .btn-ghost {
    padding: 0.42rem 1.05rem;
    background: transparent;
    border: 1px solid rgba(255, 255, 255, 0.6);
    color: #fff;
  }

  .btn-ghost:hover {
    background: rgba(255, 255, 255, 0.12);
  }

  .btn-inline {
    padding: 0.3rem 0.9rem;
    background: #fdf4e4;
    color: #7a4b20;
    border: 1px solid #f1d7aa;
  }

  .btn-inline:hover {
    background: #ffe9cc;
  }

  .btn-inline.disabled {
    opacity: 0.6;
    cursor: default;
  }

  .btn:focus-visible {
    outline: 2px solid #3b134f;
    outline-offset: 2px;
  }

  /* GRID */
  .grid {
    display: grid;
    gap: 1.2rem;
  }

  @media (min-width: 900px) {
    .grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  .card {
    background: #fffdf9;
    border-radius: 1rem;
    border: 1px solid #f0e1c7;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
    display: flex;
    flex-direction: column;
  }

  .card-header {
    padding: 0.9rem 1.1rem 0.7rem;
    border-bottom: 1px solid #f4ead6;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 0.75rem;
  }

  .card-title-row {
    display: flex;
    align-items: center;
    gap: 0.6rem;
  }

  .card-icon {
    font-size: 1.3rem;
  }

  .card-title {
    margin: 0;
    font-size: 1.05rem;
    font-weight: 600;
    color: #3b134f;
  }

  .card-subtitle {
    margin: 0;
    font-size: 0.82rem;
    color: #7c6b80;
  }

  .card-tag {
    font-size: 0.75rem;
    padding: 0.25rem 0.6rem;
    border-radius: 999px;
    background: #fff3da;
    color: #a35b10;
    white-space: nowrap;
  }

  .card-body {
    padding: 0.9rem 1.1rem 0.6rem;
    font-size: 0.9rem;
    color: #4c3a3a;
  }

  .card-footer {
    padding: 0.4rem 1.1rem 0.9rem;
    border-top: 1px solid #f4ead6;
    display: flex;
    justify-content: flex-end;
  }

  .card-footer--disabled {
    justify-content: flex-start;
  }

  .card-empty {
    margin: 0;
    color: #746666;
    line-height: 1.4;
  }

  .card-highlight {
    padding: 0.8rem 0.85rem;
    border-radius: 0.85rem;
    background: #fff7e6;
    border: 1px solid #f1d7aa;
  }

  .highlight-label {
    margin: 0 0 0.2rem;
    font-size: 0.78rem;
    text-transform: uppercase;
    letter-spacing: 0.06em;
    color: #a36a18;
  }

  .highlight-title {
    margin: 0;
    font-weight: 600;
    font-size: 0.96rem;
  }

  .highlight-meta {
    margin: 0.25rem 0 0;
    font-size: 0.82rem;
    color: #7a5d37;
  }
</style>