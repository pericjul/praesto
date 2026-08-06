<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";

    let { data, form } = $props();
    let user = $derived(data.user ?? {});
    let isAuthenticated = $derived(data.isAuthenticated ?? false);

    let roleLabels = $derived({
        STUDENT: $t("account.roleStudent"),
        TEACHER: $t("account.roleTeacher"),
        SCHOOL_ADMIN: $t("account.roleSchoolAdmin"),
        SUPER_ADMIN: $t("account.roleSuperAdmin"),
        DEMO_USER: $t("account.roleDemo")
    });

    function pwErrorText(code) {
        if (code === "short") return $t("account.pwShort");
        if (code === "mismatch") return $t("account.pwMismatch");
        if (code === "wrong") return $t("account.pwWrong");
        return "";
    }

    function handleProfile() {
        return async ({ update }) => {
            await invalidateAll();
            await update({ reset: false });
        };
    }

    // Abo/Zugang
    let billing = $derived(data.billing ?? {});
    let isIndividual = $derived(billing?.individual === true || user.accountType === "INDIVIDUAL");
    let hasSub = $derived(!!billing?.subscriptionEndsAt);
    let trialActive = $derived(billing?.trialActive === true);
    function fmtDate(d) {
        if (!d) return "";
        try { return new Date(d).toLocaleDateString("de-CH", { day: "2-digit", month: "2-digit", year: "numeric" }); }
        catch { return ""; }
    }
</script>

<svelte:head>
    <title>{$t('account.headTitle')}</title>
</svelte:head>

<div class="account">
    <h1>{$t('account.title')}</h1>

    {#if isAuthenticated}
        <!-- Übersicht -->
        <section class="card">
            <p><b>{$t('account.email')}</b> {user.email}</p>
            {#if user.role}
                <p><b>{$t('account.role')}</b> {roleLabels[user.role] ?? user.role}</p>
            {/if}
        </section>

        <!-- Abo & Zugang -->
        <section class="card abo-card">
            <h2>⭐ Abo & Zugang</h2>

            {#if form?.portalError}
                <div class="alert error">⚠️ {form.portalError}</div>
            {/if}

            {#if !isIndividual}
                <!-- Schul-/Bestandskonto -->
                <p class="abo-lead">🏫 <b>Deine Schule zahlt für dich.</b></p>
                <p class="abo-note">Du nutzt Praesto kostenlos über deine Schule – alles inklusive, kein eigenes Abo nötig. 🎉</p>
            {:else if trialActive}
                <!-- Privat-Konto in der Testphase -->
                <p class="abo-lead">✨ <b>Gratis-Testphase</b> – noch bis {fmtDate(billing.trialEndsAt)}.</p>
                <p class="abo-note">Damit es danach ohne Unterbruch weitergeht, kannst du jetzt schon ein Abo lösen.</p>
                <p class="abo-tip">💡 Tipp: Sag deiner Schule von Praesto – oft übernimmt die Schule die Kosten für die ganze Klasse!</p>
                <a href="/abo" class="btn-primary">Abo abschliessen</a>
            {:else if hasSub}
                <!-- Privat-Konto mit aktivem Abo -->
                <p class="abo-lead">✅ <b>Abo aktiv</b> – Zugang bis {fmtDate(billing.subscriptionEndsAt)}.</p>
                <p class="abo-note">Du kannst dein Abo jederzeit verwalten oder kündigen. Bei einer Kündigung bleibt der Zugang bis zum Ende der bezahlten Periode bestehen.</p>
                <form method="POST" action="?/portal">
                    <button type="submit" class="btn-primary">Abo verwalten / kündigen</button>
                </form>
            {:else}
                <!-- Privat-Konto ohne Zugang -->
                <p class="abo-lead">⏳ <b>Kein aktiver Zugang.</b></p>
                <p class="abo-note">Schliesse ein Abo ab, um Praesto weiter zu nutzen – oder sag deiner Schule Bescheid, vielleicht übernimmt sie die Kosten.</p>
                <a href="/abo" class="btn-primary">Zu den Abos</a>
            {/if}
        </section>

        <!-- Profil bearbeiten -->
        <section class="card">
            <h2>{$t('account.editProfile')}</h2>
            {#if form?.profileSuccess}
                <div class="alert success">✓ {$t('account.profileSaved')}</div>
            {/if}
            {#if form?.profileError}
                <div class="alert error">⚠️ {$t('account.profileError')}</div>
            {/if}
            <form method="POST" action="?/profile" use:enhance={handleProfile} class="form">
                <div class="row">
                    <label>
                        {$t('account.firstName')}
                        <input name="firstName" value={user.firstName ?? ""} required />
                    </label>
                    <label>
                        {$t('account.lastName')}
                        <input name="lastName" value={user.lastName ?? ""} required />
                    </label>
                </div>
                <button type="submit" class="btn-primary">{$t('account.save')}</button>
            </form>
        </section>

        <!-- E-Mail (Login) ändern -->
        <section class="card">
            <h2>📧 E-Mail-Adresse ändern</h2>
            <p class="abo-note">Mit dieser E-Mail meldest du dich an. Zur Sicherheit bestätige dein aktuelles Passwort.</p>
            {#if form?.emailSuccess}
                <div class="alert success">✓ E-Mail-Adresse geändert. Beim nächsten Login gilt die neue Adresse.</div>
            {/if}
            {#if form?.emailError}
                <div class="alert error">⚠️ {form.emailError}</div>
            {/if}
            <form method="POST" action="?/email" use:enhance={handleProfile} class="form">
                <label>
                    Neue E-Mail-Adresse
                    <input name="newEmail" type="email" value={user.email ?? ""} required />
                </label>
                <label>
                    Aktuelles Passwort
                    <input name="emailPassword" type="password" placeholder="zur Bestätigung" required />
                </label>
                <button type="submit" class="btn-primary">E-Mail ändern</button>
            </form>
        </section>

        <!-- Passwort ändern -->
        <section class="card">
            <h2>{$t('account.changePassword')}</h2>
            {#if form?.pwSuccess}
                <div class="alert success">✓ {$t('account.pwSaved')}</div>
            {/if}
            {#if form?.pwError}
                <div class="alert error">⚠️ {pwErrorText(form.pwError)}</div>
            {/if}
            <form method="POST" action="?/password" use:enhance class="form">
                <label>
                    {$t('account.currentPassword')}
                    <input type="password" name="currentPassword" required />
                </label>
                <label>
                    {$t('account.newPassword')}
                    <input type="password" name="newPassword" required minlength="8" />
                </label>
                <label>
                    {$t('account.confirmPassword')}
                    <input type="password" name="confirm" required minlength="8" />
                </label>
                <button type="submit" class="btn-primary">{$t('account.save')}</button>
            </form>
        </section>
    {:else}
        <p>{$t('account.notLoggedIn')}</p>
    {/if}
</div>

<style>
    .account { max-width: 620px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    h1 { margin: 0 0 1.25rem; color: var(--color-primary, #2F124D); font-size: 1.6rem; }

    .card {
        background: var(--color-bg-card, #fff);
        border: 1px solid var(--color-border, #e6d9cc);
        border-radius: 1rem;
        padding: 1.25rem;
        margin-bottom: 1.25rem;
    }

    .card h2 { margin: 0 0 1rem; font-size: 1.05rem; color: var(--color-text-secondary, #2d2141); }
    .card p { margin: 0 0 0.5rem; color: var(--color-text-secondary, #2d2141); }

    .form { display: flex; flex-direction: column; gap: 0.85rem; }
    .row { display: flex; gap: 0.85rem; flex-wrap: wrap; }
    .row label { flex: 1; min-width: 140px; }
    @media (max-width: 500px) { .row label { min-width: 0; flex-basis: 100%; } }

    label { display: flex; flex-direction: column; gap: 0.35rem; font-size: 0.9rem; color: var(--color-text-secondary, #2d2141); }

    input {
        padding: 0.6rem 0.85rem;
        border: 1px solid var(--color-border-input, #e8e0f0);
        border-radius: 0.5rem;
        background: var(--color-bg-input, #faf8fc);
        font-size: 1rem;
    }

    input:focus { outline: none; border-color: var(--color-primary, #2F124D); background: #fff; }

    .btn-primary {
        align-self: flex-start;
        background: var(--color-primary, #2F124D);
        color: #fff;
        border: none;
        border-radius: 0.5rem;
        padding: 0.6rem 1.3rem;
        font-weight: 600;
        cursor: pointer;
    }

    .btn-primary:hover { background: var(--color-primary-hover, #4A1C74); }

    .alert { padding: 0.7rem 1rem; border-radius: 0.5rem; margin-bottom: 0.85rem; font-size: 0.9rem; }
    .alert.success { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }
    .alert.error { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }

    .abo-card h2 { margin-top: 0; }
    .abo-lead { font-size: 1rem; margin: 0 0 0.35rem; color: #2d2141; }
    .abo-note { font-size: 0.88rem; color: #6b647a; margin: 0 0 0.6rem; line-height: 1.5; }
    .abo-tip { font-size: 0.85rem; color: #0d9488; background: #f0fdfa; border: 1px solid #99f6e4; border-radius: 0.5rem; padding: 0.5rem 0.7rem; margin: 0 0 0.85rem; }
    a.btn-primary { display: inline-block; text-decoration: none; }
</style>
