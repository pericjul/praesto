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
    .row label { flex: 1; min-width: 180px; }

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
</style>
