<script>
  import "../styles/theme.css";
  import "../styles/components.css";
  import logo from "../lib/assets/praesto-logo.png";
  import { page } from "$app/stores";

  export let data;
  const user = data?.user ?? {};
  const isAuthenticated = data?.isAuthenticated ?? false;

  const roles = user.user_roles ?? [];
  const isStudent = roles.includes("STUDENT");
  const isTeacher = roles.includes("TEACHER");

  function isActive(path) {
    return $page.url.pathname.startsWith(path);
  }
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
            <a href="/student/dashboard" class="nav-link" class:active={isActive('/student/dashboard')}>Dashboard</a>
            <a href="/student/assignments" class="nav-link" class:active={isActive('/student/assignments')}>Aufgaben</a>
            <a href="/student/sessions" class="nav-link" class:active={isActive('/student/sessions')}>Training</a>
            <a href="/student/notes" class="nav-link" class:active={isActive('/student/notes')}>Notizen</a>
            <a href="/student/applications" class="nav-link" class:active={isActive('/student/applications')}>Bewerbungen</a>
            <a href="/student/badges" class="nav-link" class:active={isActive('/student/badges')}>🏅 Badges</a>
          {:else if isTeacher}
            <a href="/teacher/dashboard" class="nav-link" class:active={isActive('/teacher/dashboard')}>Dashboard</a>
            <a href="/teacher/classes" class="nav-link" class:active={isActive('/teacher/classes')}>Klassen</a>
            <a href="/teacher/assignments" class="nav-link" class:active={isActive('/teacher/assignments')}>Aufgaben</a>
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
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 100;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0.75rem clamp(1rem, 3vw, 3rem);
    background: rgba(248, 243, 235, 0.97);
    backdrop-filter: blur(12px);
    border-bottom: 1px solid var(--color-card-border);
    height: 54px;
    box-sizing: border-box;
  }

  .app-main {
    padding-top: 54px;
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
    height: 44px;
    width: auto;
    object-fit: contain;
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
    transition: all 0.15s;
  }

  .nav-link:hover {
    background: var(--color-muted-bg);
    color: var(--color-text);
  }

  .nav-link.active {
    background: var(--color-primary);
    color: #fff;
    font-weight: 500;
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
      display: none;
    }
    .brand-text {
      display: none;
    }
  }
</style>