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

    let showDelete = $state(false);
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
            <h2>{$t('acc2.aboTitle')}</h2>

            {#if form?.portalError}
                <div class="alert error">⚠️ {form.portalError}</div>
            {/if}

            {#if !isIndividual}
                <p class="abo-lead">{$t('acc2.schoolPays')}</p>
                <p class="abo-note">{$t('acc2.schoolPaysText')}</p>
            {:else if trialActive}
                <p class="abo-lead">{$t('acc2.trialLead').replace('%DATE', fmtDate(billing.trialEndsAt))}</p>
                <p class="abo-note">{$t('acc2.trialText')}</p>
                <p class="abo-tip">{$t('acc2.schoolTip')}</p>
                <a href="/abo" class="btn-primary">{$t('acc2.subscribe')}</a>
            {:else if hasSub}
                <p class="abo-lead">{$t('acc2.activeLead').replace('%DATE', fmtDate(billing.subscriptionEndsAt))}</p>
                <p class="abo-note">{$t('acc2.activeText')}</p>
                <form method="POST" action="?/portal">
                    <button type="submit" class="btn-primary">{$t('acc2.manage')}</button>
                </form>
            {:else}
                <p class="abo-lead">{$t('acc2.noAccessLead')}</p>
                <p class="abo-note">{$t('acc2.noAccessText')}</p>
                <a href="/abo" class="btn-primary">{$t('acc2.toAbos')}</a>
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
            <h2>{$t('acc2.emailTitle')}</h2>
            <p class="abo-note">{$t('acc2.emailNote')}</p>
            {#if form?.emailSuccess}
                <div class="alert success">{$t('acc2.emailSuccess')}</div>
            {/if}
            {#if form?.emailError}
                <div class="alert error">⚠️ {form.emailError}</div>
            {/if}
            <form method="POST" action="?/email" use:enhance={handleProfile} class="form">
                <label>
                    {$t('acc2.emailNew')}
                    <input name="newEmail" type="email" value={user.email ?? ""} required />
                </label>
                <label>
                    {$t('acc2.emailPassword')}
                    <input name="emailPassword" type="password" placeholder={$t('acc2.emailPwPlaceholder')} required />
                </label>
                <button type="submit" class="btn-primary">{$t('acc2.emailBtn')}</button>
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

        <!-- Konto löschen (Gefahrenzone) -->
        <section class="card danger">
            <h2>{$t('acc2.delTitle')}</h2>
            <p class="abo-note">{$t('acc2.delText')}</p>
            {#if isIndividual && hasSub}
                <p class="abo-note">{$t('acc2.delSubHint')}</p>
            {/if}
            {#if form?.deleteError}
                <div class="alert error">⚠️ {form.deleteError}</div>
            {/if}
            {#if !showDelete}
                <button type="button" class="btn-danger" onclick={() => (showDelete = true)}>{$t('acc2.delBtn')}</button>
            {:else}
                <form method="POST" action="?/deleteAccount" use:enhance class="form">
                    <label>
                        {$t('acc2.delConfirmLabel')}
                        <input type="password" name="deletePassword" placeholder={$t('acc2.delPwPlaceholder')} required />
                    </label>
                    <div class="del-actions">
                        <button type="button" class="btn-ghost" onclick={() => (showDelete = false)}>{$t('acc2.cancel')}</button>
                        <button type="submit" class="btn-danger">{$t('acc2.delFinal')}</button>
                    </div>
                </form>
            {/if}
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

    .card.danger { border-color: #f3c9c9; }
    .card.danger h2 { color: #b91c1c; }
    .btn-danger { align-self: flex-start; background: #dc2626; color: #fff; border: none; border-radius: 0.5rem; padding: 0.6rem 1.2rem; font-weight: 600; cursor: pointer; }
    .btn-danger:hover { background: #b91c1c; }
    .btn-ghost { background: #f3f0f8; color: #4b4560; border: 1px solid #e0d8ec; border-radius: 0.5rem; padding: 0.6rem 1.1rem; font-weight: 600; cursor: pointer; }
    .del-actions { display: flex; gap: 0.6rem; }

    .abo-card h2 { margin-top: 0; }
    .abo-lead { font-size: 1rem; margin: 0 0 0.35rem; color: #2d2141; }
    .abo-note { font-size: 0.88rem; color: #6b647a; margin: 0 0 0.6rem; line-height: 1.5; }
    .abo-tip { font-size: 0.85rem; color: #0d9488; background: #f0fdfa; border: 1px solid #99f6e4; border-radius: 0.5rem; padding: 0.5rem 0.7rem; margin: 0 0 0.85rem; }
    a.btn-primary { display: inline-block; text-decoration: none; }
</style>
