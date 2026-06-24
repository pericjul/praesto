<script>
  import "../styles/theme.css";
  import "../styles/components.css";
  import logo from "../lib/assets/praesto-logo.png";
  import { page } from "$app/stores";
  import { t, locale, setLocale, normalizeLang, LANGUAGES } from "$lib/i18n";

  let { data, children } = $props();

  // Sprache aus dem Server (Cookie) übernehmen
  $effect(() => {
    locale.set(normalizeLang(data?.lang));
  });
  let lang = $derived(normalizeLang(data?.lang));

  function changeLang(event) {
    setLocale(event.currentTarget.value);
  }

  let user = $derived(data?.user ?? {});
  let isAuthenticated = $derived(data?.isAuthenticated ?? false);
  let isDemo = $derived(data?.isDemo ?? false);

  let role = $derived(user.role);
  let isStudent = $derived(role === "STUDENT");
  let isTeacher = $derived(role === "TEACHER");
  let isSchoolAdmin = $derived(role === "SCHOOL_ADMIN" || role === "DEMO_USER");
  let isSuperAdmin = $derived(role === "SUPER_ADMIN");

  function isActive(path) {
    return $page.url.pathname.startsWith(path);
  }

  // Navigations-Einträge je Rolle (für Desktop-Nav UND Mobil-Menü)
  let navItems = $derived.by(() => {
    if (isStudent) return [
      { href: "/student/dashboard", label: $t("nav.dashboard") },
      { href: "/student/assignments", label: $t("nav.tasks") },
      { href: "/student/sessions", label: $t("nav.training") },
      { href: "/student/notes", label: $t("nav.notes") },
      { href: "/student/applications", label: $t("nav.applications") },
      { href: "/student/badges", label: "🏅 " + $t("nav.badges") }
    ];
    if (isTeacher) return [
      { href: "/teacher/dashboard", label: $t("nav.dashboard") },
      { href: "/teacher/classes", label: $t("nav.classes") },
      { href: "/teacher/assignments", label: $t("nav.tasks") }
    ];
    if (isSchoolAdmin) return [
      { href: "/admin/dashboard", label: $t("nav.dashboard") },
      { href: "/admin/users", label: $t("nav.users") }
    ];
    if (isSuperAdmin) return [
      { href: "/super/dashboard", label: $t("nav.schools") },
      { href: "/super/inquiries", label: $t("nav.inquiries") },
      { href: "/super/users", label: $t("nav.userData") }
    ];
    return [];
  });

  let mobileMenuOpen = $state(false);
  function closeMenu() {
    mobileMenuOpen = false;
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
          {#each navItems as item}
            <a href={item.href} class="nav-link" class:active={isActive(item.href)}>{item.label}</a>
          {/each}
        </nav>
      {/if}
    </div>

    <div class="app-header-right">
      <select class="lang-select" value={lang} onchange={changeLang} title="Sprache / Language" aria-label="Language">
        {#each LANGUAGES as l}
          <option value={l.code}>{l.flag} {l.code.toUpperCase()}</option>
        {/each}
      </select>
      {#if isAuthenticated}
        <a href="/account" class="user-chip desktop-only">
          {user.firstName ? `${user.firstName} ${user.lastName ?? ""}`.trim() : $t('nav.loggedIn')}
        </a>
        <form method="POST" action="/logout" class="desktop-only">
          <button type="submit" class="btn btn-primary">{$t('nav.logout')}</button>
        </form>
        <button type="button" class="hamburger" onclick={() => (mobileMenuOpen = !mobileMenuOpen)} aria-label="Menu" aria-expanded={mobileMenuOpen}>
          {mobileMenuOpen ? "✕" : "☰"}
        </button>
      {:else}
        <a href="/login" class="btn btn-primary">{$t('nav.login')}</a>
      {/if}
    </div>
  </header>

  {#if isAuthenticated && mobileMenuOpen}
    <nav class="mobile-menu">
      {#each navItems as item}
        <a href={item.href} class="mobile-link" class:active={isActive(item.href)} onclick={closeMenu}>{item.label}</a>
      {/each}
      <a href="/account" class="mobile-link" onclick={closeMenu}>{$t('nav.account')}</a>
      <form method="POST" action="/logout" onsubmit={closeMenu}>
        <button type="submit" class="mobile-logout">{$t('nav.logout')}</button>
      </form>
    </nav>
  {/if}

  <main class="app-main">
    {#if isAuthenticated && isDemo}
      <div class="demo-bar">
        <span class="demo-tag">🎭 {$t('demo.mode')}</span>
        <span class="demo-switch-label">{$t('demo.switchTo')}</span>
        <div class="demo-roles">
          <form method="POST" action="/demo">
            <input type="hidden" name="as" value="student" />
            <button type="submit" class:active={isStudent}>{$t('demo.rStudent')}</button>
          </form>
          <form method="POST" action="/demo">
            <input type="hidden" name="as" value="teacher" />
            <button type="submit" class:active={isTeacher}>{$t('demo.rTeacher')}</button>
          </form>
          <form method="POST" action="/demo">
            <input type="hidden" name="as" value="admin" />
            <button type="submit" class:active={isSchoolAdmin}>{$t('demo.rAdmin')}</button>
          </form>
        </div>
      </div>
    {/if}
    {@render children()}
  </main>

  <footer class="app-footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <img src={logo} alt="" class="footer-logo" />
        <span>Praesto</span>
      </div>
      <p class="footer-tagline">{$t('footer.tagline')}</p>
      <nav class="footer-links">
        <a href="/">{$t('footer.start')}</a>
        <a href="/demo">{$t('footer.demo')}</a>
        <a href="/datenschutz">{$t('footer.privacy')}</a>
        <a href="/impressum">{$t('footer.imprint')}</a>
        <a href="/kontakt">{$t('footer.contact')}</a>
      </nav>
      <span class="footer-copy">{$t('footer.copy')}</span>
    </div>
  </footer>
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

  .app-root {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }

  .app-main {
    padding-top: 54px;
    flex: 1 0 auto;
  }

  /* ===== DEMO-SWITCHER ===== */
  .demo-bar {
    position: sticky;
    top: 54px;
    z-index: 90;
    display: flex;
    align-items: center;
    gap: 0.6rem;
    flex-wrap: wrap;
    background: var(--color-primary, #2F124D);
    color: #fff;
    padding: 0.5rem clamp(1rem, 3vw, 3rem);
  }

  .demo-tag {
    font-weight: 700;
    font-size: 0.85rem;
  }

  .demo-switch-label {
    font-size: 0.85rem;
    opacity: 0.85;
  }

  .demo-roles {
    display: flex;
    gap: 0.4rem;
    flex-wrap: wrap;
  }

  .demo-roles form {
    margin: 0;
  }

  .demo-roles button {
    background: rgba(255, 255, 255, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.3);
    color: #fff;
    border-radius: 999px;
    padding: 0.25rem 0.7rem;
    font-size: 0.8rem;
    cursor: pointer;
    transition: all 0.15s;
  }

  .demo-roles button:hover {
    background: rgba(255, 255, 255, 0.28);
  }

  .demo-roles button.active {
    background: var(--color-accent-light, #fbe4b2);
    color: var(--color-primary, #2F124D);
    border-color: transparent;
    font-weight: 600;
  }

  /* ===== FOOTER ===== */
  .app-footer {
    flex-shrink: 0;
    margin-top: 2rem;
    background: var(--color-primary);
    color: rgba(255, 255, 255, 0.85);
    padding: 1.75rem clamp(1rem, 3vw, 3rem);
  }

  .footer-inner {
    max-width: var(--page-max-width);
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    text-align: center;
  }

  .footer-brand {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    font-weight: 800;
    letter-spacing: 0.03em;
    color: #fff;
  }

  .footer-logo {
    height: 26px;
    width: auto;
  }

  .footer-tagline {
    margin: 0;
    font-size: 0.9rem;
    color: rgba(255, 255, 255, 0.8);
  }

  .footer-links {
    display: flex;
    gap: 1.25rem;
    margin: 0.25rem 0;
  }

  .footer-links a {
    color: var(--color-accent-light);
    text-decoration: none;
    font-size: 0.9rem;
  }

  .footer-links a:hover {
    text-decoration: underline;
  }

  .footer-copy {
    font-size: 0.8rem;
    color: rgba(255, 255, 255, 0.6);
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
    text-decoration: none;
  }

  a.user-chip:hover {
    color: var(--color-text);
  }

  /* Hamburger nur auf Mobile sichtbar */
  .hamburger {
    display: none;
    background: transparent;
    border: 1px solid var(--color-card-border, #e0d6eb);
    color: var(--color-primary);
    border-radius: 0.5rem;
    font-size: 1.2rem;
    line-height: 1;
    padding: 0.25rem 0.55rem;
    cursor: pointer;
  }

  /* Mobil-Menü-Panel */
  .mobile-menu {
    display: none;
    position: fixed;
    top: 54px;
    left: 0;
    right: 0;
    z-index: 95;
    background: rgba(248, 243, 235, 0.99);
    backdrop-filter: blur(12px);
    border-bottom: 1px solid var(--color-card-border, #e0d6eb);
    flex-direction: column;
    padding: 0.5rem clamp(1rem, 3vw, 3rem) 1rem;
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  }

  .mobile-link {
    text-decoration: none;
    color: var(--color-text);
    padding: 0.7rem 0.5rem;
    border-radius: 0.5rem;
    border-bottom: 1px solid var(--color-border-light, #f0e8f8);
  }

  .mobile-link.active {
    color: var(--color-primary);
    font-weight: 600;
  }

  .mobile-logout {
    margin-top: 0.6rem;
    width: 100%;
    background: var(--color-primary);
    color: #fff;
    border: none;
    border-radius: 0.5rem;
    padding: 0.6rem;
    font-weight: 600;
    cursor: pointer;
  }

  .lang-select {
    background: transparent;
    border: 1px solid var(--color-card-border, #e0d6eb);
    color: var(--color-text-muted);
    border-radius: 999px;
    padding: 0.3rem 0.6rem;
    font-size: 0.85rem;
    cursor: pointer;
    transition: all 0.15s;
  }

  .lang-select:hover {
    background: var(--color-muted-bg);
    color: var(--color-text);
  }

  @media (max-width: 750px) {
    .nav-main {
      display: none;
    }
    .desktop-only {
      display: none;
    }
    .hamburger {
      display: inline-flex;
      align-items: center;
    }
    .mobile-menu {
      display: flex;
    }
  }
</style>