<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    let allUsers = $derived(data.users ?? []);
    let confirmId = $state(null);

    // Filter
    let schoolFilter = $state("ALL");
    let roleFilter = $state("ALL");
    let text = $state("");
    let onlyInactive = $state(false);

    const roleIcons = {
        TEACHER: "👩‍🏫",
        STUDENT: "🎓",
        SCHOOL_ADMIN: "🏫",
        DEMO_USER: "🎭",
        SUPER_ADMIN: "👑"
    };

    const YEAR_MS = 365 * 24 * 60 * 60 * 1000;

    // Schulliste für den Filter (aus den Nutzern abgeleitet)
    let schools = $derived(
        [...new Map(allUsers.filter((u) => u.schoolId).map((u) => [u.schoolId, u.schoolName])).entries()]
            .map(([id, name]) => ({ id, name }))
            .sort((a, b) => (a.name || "").localeCompare(b.name || ""))
    );

    function daysSince(ts) {
        if (!ts) return null;
        return Math.floor((Date.now() - new Date(ts).getTime()) / (24 * 60 * 60 * 1000));
    }
    function isInactive(u) {
        return !u.lastLoginAt || Date.now() - new Date(u.lastLoginAt).getTime() > YEAR_MS;
    }
    function fmtLast(u) {
        if (!u.lastLoginAt) return $t("su.never");
        const d = new Date(u.lastLoginAt);
        const days = daysSince(u.lastLoginAt);
        const date = d.toLocaleDateString("de-CH");
        if (days === 0) return `${$t("su.today")} (${date})`;
        return `${date} · ${$t("su.daysAgo").replace("%N", days)}`;
    }

    let filtered = $derived(
        allUsers.filter((u) => {
            if (schoolFilter !== "ALL" && u.schoolId !== schoolFilter) return false;
            if (roleFilter !== "ALL" && u.role !== roleFilter) return false;
            if (onlyInactive && !(u.role === "STUDENT" && isInactive(u))) return false;
            if (text.trim()) {
                const q = text.toLowerCase();
                if (!(`${u.name} ${u.email} ${u.schoolName}`.toLowerCase().includes(q))) return false;
            }
            return true;
        })
    );

    let inactiveStudentCount = $derived(
        allUsers.filter((u) => u.role === "STUDENT" && isInactive(u)).length
    );

    function handleDelete() {
        return async ({ result, update }) => {
            confirmId = null;
            if (result.type === "success") await invalidateAll();
            await update({ reset: false });
        };
    }
</script>

<svelte:head>
    <title>{$t('su.title')} – Praesto</title>
</svelte:head>

<div class="page">
    <h1>{$t('su.title')}</h1>
    <p class="lead">{$t('su.subtitle')}</p>

    {#if data.loadError}<div class="alert error">⚠️ {data.loadError}</div>{/if}
    {#if form?.deleted}<div class="alert success">✓ {$t('su.deleted')}</div>{/if}
    {#if form?.error}<div class="alert error">⚠️ {$t('contact.error')}</div>{/if}

    <div class="stats">
        <span class="stat">{allUsers.length} {$t('su.usersTotal')}</span>
        <span class="stat">{schools.length} {$t('su.schoolsCount')}</span>
        {#if inactiveStudentCount > 0}
            <button type="button" class="stat warnstat" onclick={() => (onlyInactive = !onlyInactive)}>
                ⚠️ {inactiveStudentCount} {$t('su.inactiveOver1y')}
            </button>
        {/if}
    </div>

    <div class="filters">
        <select bind:value={schoolFilter}>
            <option value="ALL">{$t('su.allSchools')}</option>
            {#each schools as s}<option value={s.id}>{s.name}</option>{/each}
        </select>
        <select bind:value={roleFilter}>
            <option value="ALL">{$t('su.allRoles')}</option>
            <option value="STUDENT">🎓 {$t('su.roleStudents')}</option>
            <option value="TEACHER">👩‍🏫 {$t('su.roleTeachers')}</option>
            <option value="SCHOOL_ADMIN">🏫 {$t('su.roleAdmins')}</option>
        </select>
        <input type="search" bind:value={text} placeholder={$t('su.searchPlaceholder')} />
        <label class="chk"><input type="checkbox" bind:checked={onlyInactive} /> {$t('su.onlyInactive')}</label>
    </div>

    {#if filtered.length === 0}
        <p class="empty">{$t('su.noResults')}</p>
    {:else}
        <div class="table-scroll">
            <table class="utable">
                <thead>
                    <tr>
                        <th>{$t('su.thName')}</th>
                        <th>{$t('su.thSchool')}</th>
                        <th>{$t('su.thLastActive')}</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    {#each filtered as u (u.id)}
                        <tr class:inactive={u.role === "STUDENT" && isInactive(u)}>
                            <td>
                                <span class="uname">{roleIcons[u.role] ?? "•"} {u.name}</span>
                                <span class="uemail">{u.email}</span>
                            </td>
                            <td class="uschool">{u.schoolName || "—"}</td>
                            <td class="ulast">
                                {fmtLast(u)}
                                {#if u.role === "STUDENT" && isInactive(u)}<span class="flag">⚠️ &gt;1 J.</span>{/if}
                            </td>
                            <td class="uact">
                                {#if confirmId === u.id}
                                    <form method="POST" action="?/delete" use:enhance={handleDelete} class="inline">
                                        <input type="hidden" name="id" value={u.id} />
                                        <button type="submit" class="btn-danger">{$t('su.confirmYes')}</button>
                                    </form>
                                    <button type="button" class="btn-ghost" onclick={() => (confirmId = null)}>{$t('su.cancel')}</button>
                                {:else}
                                    <a href={`/super/users/${u.id}/export`} class="btn-ghost" download>{$t('su.export')}</a>
                                    {#if u.role !== "SUPER_ADMIN"}
                                        <button type="button" class="btn-danger" onclick={() => (confirmId = u.id)}>{$t('su.delete')}</button>
                                    {/if}
                                {/if}
                            </td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    {/if}

    <p class="warn">{$t('su.retentionNote')}</p>
</div>

<style>
    .page { max-width: 1000px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    h1 { margin: 0 0 0.4rem; color: #2F124D; font-size: 1.6rem; }
    .lead { margin: 0 0 1.25rem; color: #6b647a; }

    .stats { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-bottom: 1rem; }
    .stat { background: #faf8fc; border: 1px solid #e8e0f0; border-radius: 999px; padding: 0.35rem 0.85rem; font-size: 0.85rem; color: #4b4060; }
    .warnstat { cursor: pointer; background: #fffbeb; border-color: #fcd34d; color: #92400e; }

    .filters { display: flex; flex-wrap: wrap; gap: 0.6rem; margin-bottom: 1.25rem; align-items: center; }
    .filters select, .filters input { padding: 0.55rem 0.75rem; border: 1px solid #e8e0f0; border-radius: 0.5rem; background: #faf8fc; font: inherit; }
    .filters input[type="search"] { flex: 1; min-width: 140px; }
    .chk { display: inline-flex; align-items: center; gap: 0.4rem; font-size: 0.88rem; color: #4b4060; white-space: nowrap; }

    .table-scroll { overflow-x: auto; }
    .utable { width: 100%; border-collapse: collapse; }
    .utable th { text-align: left; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.03em; color: #9a8b9d; padding: 0.4rem 0.6rem; border-bottom: 2px solid #ece3f5; }
    .utable td { padding: 0.6rem 0.6rem; border-bottom: 1px solid #f0e8f8; vertical-align: top; }
    tr.inactive { background: #fffbeb; }
    .uname { display: block; font-weight: 600; color: #2d2141; }
    .uemail { display: block; font-size: 0.82rem; color: #9a8b9d; overflow-wrap: anywhere; }
    .uschool { color: #2d2141; }
    .ulast { font-size: 0.85rem; color: #4b4060; white-space: nowrap; }
    .flag { display: inline-block; margin-left: 0.3rem; color: #92400e; font-weight: 600; }
    .uact { text-align: right; white-space: nowrap; }
    .inline { display: inline; }

    .btn-ghost { display: inline-block; background: #faf8fc; border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.4rem 0.7rem; cursor: pointer; text-decoration: none; color: #2d2141; font-size: 0.85rem; }
    .btn-danger { background: #fef2f2; border: 1px solid #fecaca; color: #dc2626; border-radius: 0.5rem; padding: 0.4rem 0.7rem; cursor: pointer; font-size: 0.85rem; }

    .alert { padding: 0.75rem 1rem; border-radius: 0.5rem; margin-bottom: 1rem; }
    .alert.success { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }
    .alert.error { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; overflow-wrap: anywhere; }

    .empty { color: #9a8b9d; }
    .warn { margin-top: 1.5rem; font-size: 0.85rem; color: #92400e; background: #fffbeb; border: 1px solid #fcd34d; border-radius: 0.5rem; padding: 0.6rem 0.85rem; }

    @media (max-width: 640px) {
        .filters input[type="search"] { flex: 1 1 100%; }
    }
</style>
