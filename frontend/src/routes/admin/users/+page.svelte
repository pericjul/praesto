<script>
    import { enhance } from "$app/forms";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    let users = $state(data.users ?? []);
    let query = $state("");
    let roleFilter = $state("ALL");

    let roleLabels = $derived({
        TEACHER: $t("ausers.roleTeacher"),
        STUDENT: $t("ausers.roleStudent"),
        SCHOOL_ADMIN: $t("ausers.roleAdmin"),
        DEMO_USER: $t("ausers.roleDemo")
    });

    let filtered = $derived(
        users.filter((u) => {
            const matchesRole = roleFilter === "ALL" || u.role === roleFilter;
            const q = query.trim().toLowerCase();
            const matchesQuery =
                !q ||
                `${u.firstName} ${u.lastName}`.toLowerCase().includes(q) ||
                (u.email ?? "").toLowerCase().includes(q);
            return matchesRole && matchesQuery;
        })
    );

    function handleDeactivate(id) {
        return async ({ result, update }) => {
            if (result.type === "success") {
                users = users.map((u) => (u.id === id ? { ...u, active: false } : u));
            }
            await update({ reset: false });
        };
    }

    // ----- Passwort zurücksetzen -----
    let resetUser = $state(null);   // { id, name } oder null
    let resetPw = $state("");
    let resetSavedPw = $state("");  // erfolgreich gesetztes Passwort (zum Weitergeben)
    let resetError = $state("");

    function openReset(u) {
        resetUser = u;
        resetPw = "";
        resetSavedPw = "";
        resetError = "";
    }
    function closeReset() {
        resetUser = null;
    }
    function genPassword() {
        // Gut lesbares Zufallspasswort (keine verwechselbaren Zeichen).
        const chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        let p = "";
        for (let i = 0; i < 10; i++) p += chars[Math.floor(Math.random() * chars.length)];
        resetPw = p;
    }
    function handleReset() {
        return async ({ result, update }) => {
            if (result.type === "success" && result.data?.resetDone) {
                resetSavedPw = resetPw;
                resetError = "";
            } else if (result.data?.error) {
                resetError = result.data.error;
            }
            await update({ reset: false });
        };
    }
</script>

<svelte:head>
    <title>{$t('ausers.headTitle')}</title>
</svelte:head>

<div class="page">
    <h1>{$t('ausers.title')}</h1>

    {#if form?.error}
        <div class="alert">{form.error}</div>
    {/if}

    <div class="filters">
        <input class="search" type="search" placeholder={$t('ausers.searchPlaceholder')} bind:value={query} />
        <select bind:value={roleFilter}>
            <option value="ALL">{$t('ausers.allRoles')}</option>
            <option value="TEACHER">{$t('ausers.roleTeachers')}</option>
            <option value="STUDENT">{$t('ausers.roleStudents')}</option>
            <option value="SCHOOL_ADMIN">{$t('ausers.roleAdmins')}</option>
        </select>
    </div>

    <table class="users-table">
        <thead>
            <tr>
                <th>{$t('ausers.thName')}</th>
                <th>{$t('ausers.thEmail')}</th>
                <th>{$t('ausers.thRole')}</th>
                <th>{$t('ausers.thStatus')}</th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            {#each filtered as u (u.id)}
                <tr class:inactive-row={!u.active}>
                    <td>{u.firstName} {u.lastName}</td>
                    <td class="muted">{u.email}</td>
                    <td>{roleLabels[u.role] ?? u.role}</td>
                    <td>
                        {#if u.active}
                            <span class="status active">{$t('ausers.statusActive')}</span>
                        {:else}
                            <span class="status inactive">{$t('ausers.statusInactive')}</span>
                        {/if}
                    </td>
                    <td class="actions-cell">
                        {#if !data.isDemo}
                            <button class="btn-reset" type="button" onclick={() => openReset({ id: u.id, name: `${u.firstName} ${u.lastName}`.trim() })}>
                                🔑 {$t('ausers.resetPw')}
                            </button>
                        {/if}
                        {#if !data.isDemo && u.active && u.role !== "SCHOOL_ADMIN"}
                            <form method="POST" action="?/deactivate" use:enhance={handleDeactivate(u.id)}>
                                <input type="hidden" name="id" value={u.id} />
                                <button class="btn-danger" type="submit">{$t('ausers.deactivate')}</button>
                            </form>
                        {/if}
                    </td>
                </tr>
            {:else}
                <tr><td colspan="5" class="empty">{$t('ausers.empty')}</td></tr>
            {/each}
        </tbody>
    </table>
</div>

{#if resetUser}
    <button type="button" class="modal-backdrop" onclick={closeReset} aria-label="close"></button>
    <div class="modal">
        <h2>🔑 {$t('ausers.resetPwTitle')}</h2>
        <p class="modal-sub">{$t('ausers.resetPwFor')} <strong>{resetUser.name}</strong></p>

        {#if resetSavedPw}
            <div class="reset-success">
                <p>{$t('ausers.resetPwDone')}</p>
                <code class="pw-box">{resetSavedPw}</code>
                <p class="hint">{$t('ausers.resetPwHand')}</p>
            </div>
            <div class="modal-actions">
                <button type="button" class="btn-primary" onclick={closeReset}>{$t('ausers.close')}</button>
            </div>
        {:else}
            {#if resetError}<div class="alert">{resetError}</div>{/if}
            <form method="POST" action="?/resetPassword" use:enhance={handleReset}>
                <input type="hidden" name="id" value={resetUser.id} />
                <label class="pw-label" for="newPassword">{$t('ausers.resetPwNew')}</label>
                <div class="pw-row">
                    <input id="newPassword" name="newPassword" type="text" minlength="8" required
                        bind:value={resetPw} placeholder={$t('ausers.resetPwPlaceholder')} />
                    <button type="button" class="btn-gen" onclick={genPassword}>{$t('ausers.resetPwGen')}</button>
                </div>
                <div class="modal-actions">
                    <button type="button" class="btn-cancel" onclick={closeReset}>{$t('ausers.cancel')}</button>
                    <button type="submit" class="btn-primary">{$t('ausers.resetPwSave')}</button>
                </div>
            </form>
        {/if}
    </div>
{/if}

<style>
    .page { max-width: 1000px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    h1 { margin: 0 0 1.25rem; color: #2F124D; font-size: 1.6rem; }

    .filters { display: flex; gap: 0.75rem; margin-bottom: 1rem; flex-wrap: wrap; }
    .search { flex: 1; min-width: 220px; padding: 0.6rem 0.9rem; border: 1px solid #e8e0f0; border-radius: 0.5rem; background: #faf8fc; }
    select { padding: 0.6rem 0.9rem; border: 1px solid #e8e0f0; border-radius: 0.5rem; background: #faf8fc; }

    .users-table { width: 100%; border-collapse: collapse; background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; overflow: hidden; }
    th, td { text-align: left; padding: 0.75rem 1rem; border-bottom: 1px solid #f0ebf5; font-size: 0.9rem; }
    th { background: #faf8fc; color: #6b647a; font-weight: 600; }
    .muted { color: #9a8b9d; }
    .inactive-row { opacity: 0.55; }
    .status { border-radius: 999px; padding: 0.15rem 0.6rem; font-size: 0.75rem; }
    .status.active { background: #f0fdf4; color: #16a34a; }
    .status.inactive { background: #fef2f2; color: #dc2626; }
    .actions-cell { text-align: right; display: flex; gap: 0.4rem; justify-content: flex-end; align-items: center; flex-wrap: wrap; }
    .btn-danger { background: #fef2f2; border: 1px solid #fecaca; color: #dc2626; border-radius: 0.5rem; padding: 0.35rem 0.7rem; cursor: pointer; }
    .btn-reset { background: #f5f0fb; border: 1px solid #e0d2f0; color: #5b2a86; border-radius: 0.5rem; padding: 0.35rem 0.7rem; cursor: pointer; white-space: nowrap; }
    .btn-reset:hover { background: #ede0fa; }

    /* Reset-Modal */
    .modal-backdrop { position: fixed; inset: 0; background: rgba(47,18,77,0.4); backdrop-filter: blur(3px); border: none; z-index: 998; cursor: pointer; }
    .modal { position: fixed; top: 50%; left: 50%; transform: translate(-50%,-50%); background: #fff; border-radius: 1rem; padding: 1.5rem; width: 92%; max-width: 440px; max-height: 90vh; max-height: 90dvh; overflow-y: auto; z-index: 999; box-shadow: 0 20px 40px rgba(0,0,0,0.2); }
    .modal h2 { margin: 0 0 0.3rem; font-size: 1.2rem; color: #2F124D; }
    .modal-sub { margin: 0 0 1rem; color: #6b647a; font-size: 0.9rem; }
    .pw-label { display: block; font-size: 0.85rem; font-weight: 600; color: #2d2141; margin-bottom: 0.35rem; }
    .pw-row { display: flex; gap: 0.5rem; }
    .pw-row input { flex: 1; padding: 0.6rem 0.7rem; border: 1px solid #e8e0f0; border-radius: 0.5rem; font: inherit; }
    .btn-gen { background: #f3eefb; border: 1px solid #e0d2f0; color: #5b2a86; border-radius: 0.5rem; padding: 0 0.8rem; cursor: pointer; white-space: nowrap; }
    .modal-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1.25rem; }
    .btn-primary { background: #2F124D; color: #fff; border: none; border-radius: 0.5rem; padding: 0.55rem 1.1rem; font-weight: 600; cursor: pointer; }
    .btn-cancel { background: #f3f4f6; border: 1px solid #e5e7eb; color: #374151; border-radius: 0.5rem; padding: 0.55rem 1.1rem; cursor: pointer; }
    .reset-success { background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 0.6rem; padding: 0.9rem; }
    .reset-success p { margin: 0 0 0.5rem; color: #166534; font-size: 0.9rem; }
    .pw-box { display: block; background: #fff; border: 1px dashed #16a34a; border-radius: 0.4rem; padding: 0.6rem; font-size: 1.05rem; font-weight: 700; color: #14532d; text-align: center; letter-spacing: 0.05em; }
    .reset-success .hint { margin: 0.5rem 0 0; color: #4b6b52; font-size: 0.8rem; }
    .empty { color: #9a8b9d; text-align: center; padding: 1.5rem; }
    .alert { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; border-radius: 0.5rem; padding: 0.75rem 1rem; margin-bottom: 1rem; }

    @media (max-width: 640px) {
        .users-table { display: block; overflow-x: auto; white-space: nowrap; }
    }
</style>
