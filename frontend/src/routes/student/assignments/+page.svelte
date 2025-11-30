<script>
  export let data;

  const { assignments = [], error } = data;

  function formatDate(value) {
    if (!value) return "–";
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return "–";
    return d.toLocaleDateString();
  }

  function formatStatus(status) {
    if (!status) return "Unbekannt";
    // ASSIGNED, IN_PROGRESS, DONE etc.
    return status
      .toString()
      .replace(/_/g, " ")
      .toLowerCase()
      .replace(/^\w/, (c) => c.toUpperCase());
  }

  function statusClass(status) {
    switch (status) {
      case "ASSIGNED":
        return "badge-assigned";
      case "IN_PROGRESS":
        return "badge-progress";
      case "DONE":
        return "badge-done";
      default:
        return "badge-assigned";
    }
  }
</script>

<svelte:head>
  <title>Aufgaben – Praesto</title>
</svelte:head>

<div class="page-wrapper">
  <header class="page-header">
    <div>
      <h1 class="title">Aufgabenübersicht</h1>
      <p class="subtitle">
        Hier siehst du alle Aufgaben deiner Klasse. Nutze sie zur Vorbereitung auf dein Berufsleben.
      </p>
    </div>
  </header>

  {#if error}
    <div class="alert alert-danger mt-2 mb-3">
      {error}
    </div>
  {/if}

  {#if !error && assignments.length === 0}
    <div class="empty-state card shadow-sm">
      <div class="card-body">
        <h2 class="empty-title">Noch keine Aufgaben erfasst</h2>
        <p class="empty-text">
          Deine Lehrperson hat dir aktuell noch keine Aufgaben zugewiesen.
          Schau später wieder vorbei oder frage bei deiner Lehrperson nach.
        </p>
      </div>
    </div>
  {:else if assignments.length > 0}
    <div class="card shadow-sm assignments-card">
      <div class="card-header assignments-header">
        <div>
          <h2 class="section-title">Deine Aufgaben</h2>
          <p class="section-subtitle">
            Insgesamt {assignments.length} Aufgabe{assignments.length === 1 ? "" : "n"}.
          </p>
        </div>
      </div>

      <div class="card-body table-responsive">
        <table class="table align-middle assignments-table">
          <thead>
            <tr>
              <th>Titel</th>
              <th>Beschreibung</th>
              <th>Fällig am</th>
              <th>Status</th>
              <th>Dauer</th>
            </tr>
          </thead>
          <tbody>
            {#each assignments as a}
              <tr>
                <td class="cell-title">
                  <div class="title-main">{a.title}</div>
                  {#if a.classId}
                    <div class="title-sub">Klasse: {a.classId}</div>
                  {/if}
                </td>
                <td class="cell-desc">
                  {#if a.description}
                    {a.description}
                  {:else}
                    <span class="muted">Keine Beschreibung</span>
                  {/if}
                </td>
                <td class="cell-date">
                  {formatDate(a.dueDate)}
                </td>
                <td class="cell-status">
                  <span class={"status-badge " + statusClass(a.status)}>
                    {formatStatus(a.status)}
                  </span>
                </td>
                <td class="cell-duration">
                  {#if a.durationMin}
                    {a.durationMin} min
                  {:else}
                    <span class="muted">–</span>
                  {/if}
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </div>
  {/if}
</div>

<style>
  .page-wrapper {
    max-width: 1100px;
    margin: 0 auto;
    padding: 1.5rem 1rem 3rem;
  }

  .page-header {
    display: flex;
    flex-direction: column;
    gap: 0.4rem;
    margin-bottom: 1.5rem;
  }

  .title {
    font-size: 1.7rem;
    font-weight: 700;
    margin: 0;
    color: #2d2141;
  }

  .subtitle {
    margin: 0;
    color: #6b647a;
    font-size: 0.98rem;
  }

  .empty-state {
    border: 1px dashed #e0d4ff;
    background: #faf6ff;
  }

  .empty-title {
    font-size: 1.2rem;
    margin-bottom: 0.3rem;
  }

  .empty-text {
    margin: 0;
    color: #6f6a7a;
    font-size: 0.95rem;
  }

  .assignments-card {
    border: none;
  }

  .assignments-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #faf6ff;
    border-bottom: 1px solid #eee2ff;
  }

  .section-title {
    margin: 0;
    font-size: 1.1rem;
    font-weight: 600;
  }

  .section-subtitle {
    margin: 0.15rem 0 0;
    font-size: 0.9rem;
    color: #7c6f9c;
  }

  .assignments-table thead tr {
    background: #f9f5ff;
  }

  .assignments-table thead th {
    font-size: 0.8rem;
    text-transform: uppercase;
    letter-spacing: 0.03em;
    color: #7c6f9c;
    border-bottom: 1px solid #eadfff;
    white-space: nowrap;
  }

  .assignments-table tbody tr {
    border-bottom: 1px solid #f1ecff;
  }

  .assignments-table tbody tr:last-child {
    border-bottom: none;
  }

  .cell-title .title-main {
    font-weight: 600;
    color: #34224b;
  }

  .cell-title .title-sub {
    margin-top: 0.1rem;
    font-size: 0.8rem;
    color: #8c839f;
  }

  .cell-desc {
    max-width: 360px;
    font-size: 0.9rem;
    color: #594f6b;
  }

  .cell-date,
  .cell-duration {
    font-size: 0.9rem;
    white-space: nowrap;
  }

  .cell-status {
    text-align: left;
  }

  .muted {
    color: #a0a0b3;
    font-size: 0.85rem;
  }

  .status-badge {
    display: inline-flex;
    align-items: center;
    padding: 0.18rem 0.55rem;
    border-radius: 999px;
    font-size: 0.78rem;
    font-weight: 500;
  }

  .badge-assigned {
    background: #f3e8ff;
    color: #4a1fa6;
  }

  .badge-progress {
    background: #fff4d7;
    color: #a57200;
  }

  .badge-done {
    background: #dff8e9;
    color: #1b7b4a;
  }

  @media (max-width: 768px) {
    .page-wrapper {
      padding-inline: 0.75rem;
    }

    .cell-desc {
      max-width: 220px;
    }

    .assignments-table {
      font-size: 0.88rem;
    }
  }
</style>