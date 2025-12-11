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
      day: "2-digit",
      month: "2-digit",
      year: "numeric"
    });
  }

  function formatDateTime(date) {
    if (!date) return "-";
    return new Date(date).toLocaleString("de-CH", {
      dateStyle: "short",
      timeStyle: "short"
    });
  }

  function isUrgent(dueDate) {
    if (!dueDate) return false;
    const due = new Date(dueDate);
    const now = new Date();
    const diffDays = (due - now) / (1000 * 60 * 60 * 24);
    return diffDays <= 2 && diffDays >= 0;
  }

  function isOverdue(dueDate) {
    if (!dueDate) return false;
    return new Date(dueDate) < new Date();
  }

  function getDaysUntil(dueDate) {
    if (!dueDate) return null;
    const due = new Date(dueDate);
    const now = new Date();
    const diffDays = Math.ceil((due - now) / (1000 * 60 * 60 * 24));
    return diffDays;
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

  <!-- MAIN GRID -->
  <div class="main-grid">
    <!-- LINKE SPALTE: Aufgaben + Bewerbungen -->
    <div class="left-column">
      <!-- Aufgaben -->
      <article class="card card-large">
        <header class="card-header">
          <div class="card-title-row">
            <span class="card-icon">📚</span>
            <div>
              <h2 class="card-title">Deine Aufgaben</h2>
              <p class="card-subtitle">Sortiert nach Deadline</p>
            </div>
          </div>
          <span class="card-tag">{openAssignmentsCount} offen</span>
        </header>

        <div class="card-body assignments-body">
          {#if openAssignments.length === 0}
            <div class="empty-state">
              <span class="empty-icon">🎉</span>
              <p>Keine offenen Aufgaben!</p>
              <p class="empty-hint">Du hast alle Aufgaben erledigt oder es wurden noch keine zugewiesen.</p>
            </div>
          {:else}
            <div class="assignments-list">
              {#each openAssignments as assignment, index}
                <a href="/student/assignments/{assignment.id}" class="assignment-item" class:urgent={isUrgent(assignment.dueDate)} class:overdue={isOverdue(assignment.dueDate)}>
                  <div class="assignment-rank">
                    {#if index === 0}
                      <span class="rank-badge">1</span>
                    {:else}
                      <span class="rank-num">{index + 1}</span>
                    {/if}
                  </div>
                  
                  <div class="assignment-info">
                    <div class="assignment-title">{assignment.title}</div>
                    <div class="assignment-meta">
                      <span class="type-tag" style="background: {getTypeInfo(assignment.type).color}20; color: {getTypeInfo(assignment.type).color}">
                        {getTypeInfo(assignment.type).label}
                      </span>
                      {#if assignment.durationMin}
                        <span class="duration">⏱️ {assignment.durationMin} Min</span>
                      {/if}
                    </div>
                  </div>

                  <div class="assignment-deadline">
                    {#if isOverdue(assignment.dueDate)}
                      <span class="deadline-text overdue">⚠️ Überfällig!</span>
                    {:else if isUrgent(assignment.dueDate)}
                      <span class="deadline-text urgent">🔥 {getDaysUntil(assignment.dueDate) === 0 ? "Heute!" : getDaysUntil(assignment.dueDate) === 1 ? "Morgen!" : "In " + getDaysUntil(assignment.dueDate) + " Tagen"}</span>
                    {:else if assignment.dueDate}
                      <span class="deadline-text">{formatDate(assignment.dueDate)}</span>
                    {:else}
                      <span class="deadline-text muted">Kein Datum</span>
                    {/if}
                  </div>
                </a>
              {/each}
            </div>
          {/if}
        </div>

        <footer class="card-footer">
          <a href="/student/assignments" class="btn btn-inline">Alle Aufgaben ansehen</a>
        </footer>
      </article>

      <!-- Bewerbungen (unter Aufgaben) -->
      <article class="card">
        <header class="card-header">
          <div class="card-title-row">
            <span class="card-icon">💼</span>
            <div>
              <h2 class="card-title">Bewerbungen</h2>
              <p class="card-subtitle">Dein Tracker</p>
            </div>
          </div>
          <span class="card-tag">{applicationsCount} Bewerbungen</span>
        </header>

        <div class="card-body">
          {#if applicationsCount === 0}
            <p class="card-empty">
              Behalte den Überblick über deine Bewerbungen.
            </p>
          {:else}
            <p class="card-empty">
              Du trackst {applicationsCount} Bewerbungen. 💪
            </p>
          {/if}
        </div>

        <footer class="card-footer">
          <a href="/student/applications" class="btn btn-inline">Bewerbungen öffnen</a>
        </footer>
      </article>
    </div>

    <!-- RECHTE SPALTE: Mitteilungen zuerst -->
    <div class="right-column">
      <!-- Mitteilungen (als erstes) -->
      <article class="card">
        <header class="card-header">
          <div class="card-title-row">
            <span class="card-icon">🔔</span>
            <div>
              <h2 class="card-title">Mitteilungen</h2>
              <p class="card-subtitle">Neues Feedback</p>
            </div>
          </div>
          {#if notifications.length > 0}
            <span class="card-tag notification-tag">{notifications.length} neu</span>
          {/if}
        </header>

        <div class="card-body">
          {#if notifications.length === 0}
            <p class="card-empty">
              Keine neuen Mitteilungen. Hier siehst du Feedback von deiner Lehrperson.
            </p>
          {:else}
            <div class="notifications-list">
              {#each notifications.slice(0, 3) as notification}
                <a href="/student/assignments/{notification.assignmentId}" class="notification-item">
                  <span class="notification-icon">{notification.icon}</span>
                  <div class="notification-content">
                    <span class="notification-title">{notification.title}</span>
                    <span class="notification-message">{notification.message}</span>
                    {#if notification.grade != null}
                      <span class="notification-grade">Note: {notification.grade}</span>
                    {/if}
                  </div>
                </a>
              {/each}
            </div>
          {/if}
        </div>

        {#if notifications.length > 3}
          <footer class="card-footer">
            <span class="more-hint">+{notifications.length - 3} weitere</span>
          </footer>
        {/if}
      </article>

      <!-- KI-Trainings -->
      <article class="card">
        <header class="card-header">
          <div class="card-title-row">
            <span class="card-icon">🤖</span>
            <div>
              <h2 class="card-title">KI-Trainings</h2>
              <p class="card-subtitle">Interview üben</p>
            </div>
          </div>
          <span class="card-tag">{totalSessionsCount} Sessions</span>
        </header>

        <div class="card-body">
          {#if totalSessionsCount === 0}
            <p class="card-empty">
              Noch keine Trainings absolviert. Starte jetzt dein erstes KI-Bewerbungsgespräch!
            </p>
          {:else}
            <p class="card-empty">
              Letztes Training: {formatDateTime(lastSessionStartedAt)}
            </p>
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
              <p class="card-subtitle">Deine Erfolge</p>
            </div>
          </div>
          <span class="card-tag">{badgesCount} verdient</span>
        </header>

        <div class="card-body">
          {#if earnedBadgeIcons.length === 0}
            <p class="card-empty">
              Noch keine Badges verdient. Erledige Aufgaben und sammle deine ersten Erfolge!
            </p>
          {:else}
            <div class="badge-icons">
              {#each earnedBadgeIcons as icon}
                <span class="badge-icon">{icon}</span>
              {/each}
            </div>
          {/if}
        </div>

        <footer class="card-footer">
          <a href="/student/badges" class="btn btn-inline">Alle Badges ansehen</a>
        </footer>
      </article>
    </div>
  </div>
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
    text-decoration: none;
    color: inherit;
  }

  .hero-stat-link {
    cursor: pointer;
    transition: background 0.2s, transform 0.2s;
  }

  .hero-stat-link:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
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

  /* MAIN GRID */
  .main-grid {
    display: grid;
    gap: 1.2rem;
    grid-template-columns: 1fr;
  }

  @media (min-width: 900px) {
    .main-grid {
      grid-template-columns: 1.2fr 1fr;
    }
  }

  .left-column {
    display: flex;
    flex-direction: column;
    gap: 1.2rem;
  }

  .right-column {
    display: flex;
    flex-direction: column;
    gap: 1.2rem;
  }

  /* CARDS */
  .card {
    background: #fffdf9;
    border-radius: 1rem;
    border: 1px solid #f0e1c7;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
    display: flex;
    flex-direction: column;
  }

  .card-large {
    min-height: 400px;
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

  .notification-tag {
    background: #fee2e2;
    color: #dc2626;
  }

  .card-body {
    padding: 0.9rem 1.1rem;
    flex: 1;
  }

  .assignments-body {
    padding: 0.5rem;
  }

  .card-footer {
    padding: 0.6rem 1.1rem 0.9rem;
    border-top: 1px solid #f4ead6;
    display: flex;
    justify-content: flex-end;
  }

  .card-empty {
    margin: 0;
    color: #746666;
    line-height: 1.4;
    font-size: 0.9rem;
  }

  /* ASSIGNMENTS LIST */
  .assignments-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .assignment-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem;
    background: #fff;
    border: 1px solid #f0e1c7;
    border-radius: 0.75rem;
    text-decoration: none;
    transition: all 0.15s;
  }

  .assignment-item:hover {
    border-color: #3b134f;
    box-shadow: 0 2px 8px rgba(59, 19, 79, 0.1);
  }

  .assignment-item.urgent {
    border-color: #fcd34d;
    background: #fffbeb;
  }

  .assignment-item.overdue {
    border-color: #fca5a5;
    background: #fef2f2;
  }

  .assignment-rank {
    flex-shrink: 0;
    width: 32px;
    display: flex;
    justify-content: center;
  }

  .rank-badge {
    width: 28px;
    height: 28px;
    background: linear-gradient(135deg, #3b134f, #5a2d6e);
    color: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 0.85rem;
  }

  .rank-num {
    font-size: 0.9rem;
    color: #9ca3af;
    font-weight: 500;
  }

  .assignment-info {
    flex: 1;
    min-width: 0;
  }

  .assignment-title {
    font-weight: 600;
    color: #2d2141;
    font-size: 0.95rem;
    margin-bottom: 0.25rem;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .assignment-meta {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .type-tag {
    font-size: 0.7rem;
    padding: 0.15rem 0.5rem;
    border-radius: 0.3rem;
    font-weight: 500;
  }

  .duration {
    font-size: 0.75rem;
    color: #9ca3af;
  }

  .assignment-deadline {
    flex-shrink: 0;
    text-align: right;
  }

  .deadline-text {
    font-size: 0.8rem;
    color: #6b7280;
  }

  .deadline-text.urgent {
    color: #d97706;
    font-weight: 600;
  }

  .deadline-text.overdue {
    color: #dc2626;
    font-weight: 600;
  }

  .deadline-text.muted {
    color: #9ca3af;
  }

  /* EMPTY STATE */
  .empty-state {
    text-align: center;
    padding: 2rem 1rem;
    color: #6b7280;
  }

  .empty-icon {
    font-size: 2.5rem;
    display: block;
    margin-bottom: 0.5rem;
  }

  .empty-hint {
    font-size: 0.85rem;
    color: #9ca3af;
    margin-top: 0.25rem;
  }

  /* BADGE ICONS */
  .badge-icons {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .badge-icon {
    font-size: 1.8rem;
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #faf5ff, #f3e8ff);
    border-radius: 50%;
    border: 2px solid #c4b5fd;
  }

  /* NOTIFICATIONS */
  .notifications-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .notification-item {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    padding: 0.6rem;
    background: #f0fdf4;
    border: 1px solid #bbf7d0;
    border-radius: 0.5rem;
    text-decoration: none;
    transition: all 0.15s;
  }

  .notification-item:hover {
    border-color: #22c55e;
  }

  .notification-icon {
    font-size: 1.2rem;
    flex-shrink: 0;
  }

  .notification-content {
    flex: 1;
    min-width: 0;
  }

  .notification-title {
    display: block;
    font-weight: 600;
    font-size: 0.85rem;
    color: #166534;
  }

  .notification-message {
    display: block;
    font-size: 0.8rem;
    color: #4b5563;
    margin-top: 0.15rem;
  }

  .notification-grade {
    display: inline-block;
    margin-top: 0.25rem;
    font-size: 0.75rem;
    background: #7c3aed;
    color: #fff;
    padding: 0.1rem 0.4rem;
    border-radius: 0.25rem;
    font-weight: 600;
  }

  .more-hint {
    font-size: 0.8rem;
    color: #9ca3af;
  }

  /* BUTTONS */
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
    transition: all 0.1s ease;
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

  .btn:focus-visible {
    outline: 2px solid #3b134f;
    outline-offset: 2px;
  }

  @media (max-width: 600px) {
    .assignment-item {
      flex-wrap: wrap;
    }
    
    .assignment-deadline {
      width: 100%;
      text-align: left;
      margin-top: 0.25rem;
      padding-left: 40px;
    }
  }
</style>