<script>
  import "../styles/theme.css";
  import logo from "../lib/assets/praesto-logo.png"; // Pfad/Endung bei dir anpassen

  export let data;
  const user = data?.user ?? {};
  const isAuthenticated = data?.isAuthenticated ?? false;

  const roles = user.user_roles ?? [];
  const isStudent = roles.includes("STUDENT");
  const isTeacher = roles.includes("TEACHER");
</script>

<div class="app-root">
  <header class="app-header">
    <div class="app-header-left">
      <a class="brand" href="/">
        <img src={logo} alt="Praesto Logo" class="brand-logo" />
        <span class="brand-text">Praesto</span>
      </a>

      {#if isAuthenticated}
        <nav class="nav-main">
          {#if isStudent}
            <a href="/dashboard" class="nav-link">Dashboard</a>
            <a href="/assignments" class="nav-link">Aufgaben</a>
            <a href="/interview" class="nav-link">KI Training</a>
            <a href="/notes" class="nav-link">Notizen</a>
            <a href="/badges" class="nav-link">Badges</a>
          {:else if isTeacher}
            <a href="/dashboard" class="nav-link">Dashboard</a>
            <a href="/classes" class="nav-link">Klassen</a>
            <a href="/assignments" class="nav-link">Aufgaben</a>
            <a href="/sessions" class="nav-link">Sessions</a>
          {/if}
        </nav>
      {/if}
    </div>

    <div class="app-header-right">
      {#if isAuthenticated}
        <span class="user-chip">
          {user.name ?? "Eingeloggt"}
        </span>
        <form method="POST" action="/logout">
          <button type="submit" class="btn btn-primary">Logout</button>
        </form>
      {:else}
        <a href="/login" class="btn btn-primary">Login</a>
      {/if}
    </div>
  </header>

  <main class="app-main">
    <slot />
  </main>
</div>

<style>
  .app-header {
    position: sticky;
    top: 0;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0.75rem clamp(1rem, 3vw, 3rem);
    background: rgba(248, 243, 235, 0.92);
    backdrop-filter: blur(12px);
    border-bottom: 1px solid var(--color-card-border);
  }

  .app-header-left {
    display: flex;
    align-items: center;
    gap: 1.8rem;
  }

  .brand {
    display: inline-flex;
    align-items: center;
    gap: 0.6rem;
    text-decoration: none;
  }

  .brand-logo {
    width: 34px;
    height: 34px;
    border-radius: 12px;
  }

  .brand-text {
    font-weight: 800;
    font-size: 1.15rem;
    letter-spacing: 0.03em;
    color: var(--color-primary);
  }

  .nav-main {
    display: flex;
    gap: 0.9rem;
  }

  .nav-link {
    font-size: 0.95rem;
    text-decoration: none;
    color: var(--color-text-muted);
    padding: 0.35rem 0.7rem;
    border-radius: 999px;
  }

  .nav-link:hover {
    background: var(--color-muted-bg);
    color: var(--color-text);
  }

  .app-header-right {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .user-chip {
    font-size: 0.9rem;
    padding: 0.3rem 0.8rem;
    border-radius: 999px;
    background: var(--color-muted-bg);
    color: var(--color-text-muted);
  }

  @media (max-width: 750px) {
    .nav-main {
      display: none; /* für mobile kannst du später ein Burger-Menü machen */
    }
    .brand-text {
      display: none;
    }
  }
</style>