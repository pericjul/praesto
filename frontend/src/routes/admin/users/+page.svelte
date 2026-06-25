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
    .actions-cell { text-align: right; }
    .btn-danger { background: #fef2f2; border: 1px solid #fecaca; color: #dc2626; border-radius: 0.5rem; padding: 0.35rem 0.7rem; cursor: pointer; }
    .empty { color: #9a8b9d; text-align: center; padding: 1.5rem; }
    .alert { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; border-radius: 0.5rem; padding: 0.75rem 1rem; margin-bottom: 1rem; }

    @media (max-width: 640px) {
        .users-table { display: block; overflow-x: auto; white-space: nowrap; }
    }
</style>
