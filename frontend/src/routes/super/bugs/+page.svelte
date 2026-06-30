<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";

    let { data } = $props();

    let reports = $state(data.reports ?? []);
    $effect(() => { reports = data.reports ?? []; });

    const STATUSES = ["NEW", "IN_PROGRESS", "RESOLVED", "WONTFIX"];
    const SEVERITIES = ["CRITICAL", "HIGH", "MEDIUM", "LOW"];
    const AREAS = ["CHAT", "ASSIGNMENTS", "DOSSIER", "DASHBOARD", "LOGIN", "OTHER"];
    const ROLES = ["STUDENT", "TEACHER", "SCHOOL_ADMIN", "SUPER_ADMIN"];

    let fStatus = $state("all");
    let fSeverity = $state("all");
    let fArea = $state("all");
    let fRole = $state("all");
    let search = $state("");

    let filtered = $derived(reports.filter((b) => {
        if (fStatus !== "all" && b.status !== fStatus) return false;
        if (fSeverity !== "all" && b.severity !== fSeverity) return false;
        if (fArea !== "all" && b.area !== fArea) return false;
        if (fRole !== "all" && b.reporterRole !== fRole) return false;
        if (search.trim()) {
            const q = search.toLowerCase();
            const hay = `${b.title ?? ""} ${b.reporterName ?? ""} ${b.reporterEmail ?? ""}`.toLowerCase();
            if (!hay.includes(q)) return false;
        }
        return true;
    }));

    let openCount = $derived(reports.filter((b) => b.status === "NEW" || b.status === "IN_PROGRESS").length);

    // Gewinnspiel-Rangliste: meiste Meldungen pro Person
    let leaderboard = $derived.by(() => {
        const map = new Map();
        for (const b of reports) {
            const key = b.reporterName || b.reporterEmail || "?";
            const e = map.get(key) ?? { name: key, role: b.reporterRole, count: 0 };
            e.count++;
            map.set(key, e);
        }
        return [...map.values()].sort((a, b) => b.count - a.count).slice(0, 10);
    });

    function fmt(d) {
        if (!d) return "";
        try { return new Date(d).toLocaleString("de-CH", { day: "2-digit", month: "2-digit", year: "2-digit", hour: "2-digit", minute: "2-digit" }); }
        catch { return ""; }
    }

    function refresh() {
        return async ({ result }) => {
            if (result.type === "success") await invalidateAll();
        };
    }
</script>

<svelte:head><title>{$t('sbug.title')} – Praesto</title></svelte:head>

<div class="page">
    <div class="head">
        <h1>{$t('sbug.title')}</h1>
        <div class="counts">
            <span class="pill open">{openCount} {$t('sbug.openCount')}</span>
            <span class="pill total">{reports.length} {$t('sbug.totalCount')}</span>
        </div>
    </div>

    {#if leaderboard.length > 0}
        <div class="leaderboard">
            <h2>{$t('sbug.leaderboard')}</h2>
            <ol>
                {#each leaderboard as p, i}
                    <li>
                        <span class="rank">{i + 1}.</span>
                        <span class="lb-name">{p.name}</span>
                        <span class="lb-role">{$t('sbug.role.' + p.role) ?? p.role}</span>
                        <span class="lb-count">{p.count} {$t('sbug.lbReports')}</span>
                    </li>
                {/each}
            </ol>
        </div>
    {/if}

    <!-- Filter -->
    <div class="filters">
        <input class="search" placeholder={$t('sbug.search')} bind:value={search} />
        <select bind:value={fStatus}>
            <option value="all">{$t('sbug.filterStatus')}: {$t('sbug.all')}</option>
            {#each STATUSES as s}<option value={s}>{$t('sbug.status.' + s)}</option>{/each}
        </select>
        <select bind:value={fSeverity}>
            <option value="all">{$t('sbug.filterSeverity')}: {$t('sbug.all')}</option>
            {#each SEVERITIES as s}<option value={s}>{$t('bug.sev.' + s)}</option>{/each}
        </select>
        <select bind:value={fArea}>
            <option value="all">{$t('sbug.filterArea')}: {$t('sbug.all')}</option>
            {#each AREAS as a}<option value={a}>{$t('bug.area.' + a)}</option>{/each}
        </select>
        <select bind:value={fRole}>
            <option value="all">{$t('sbug.filterRole')}: {$t('sbug.all')}</option>
            {#each ROLES as r}<option value={r}>{$t('sbug.role.' + r)}</option>{/each}
        </select>
    </div>

    {#if filtered.length === 0}
        <p class="empty">{$t('sbug.empty')}</p>
    {:else}
        <div class="list">
            {#each filtered as b (b.id)}
                <article class="bug sev-{b.severity}">
                    <div class="bug-top">
                        <span class="sev-badge sev-{b.severity}">{$t('bug.sev.' + b.severity)}</span>
                        <span class="area-badge">{$t('bug.area.' + b.area)}</span>
                        <h3>{b.title}</h3>
                        <span class="date">{fmt(b.createdAt)}</span>
                    </div>

                    <p class="desc">{b.description}</p>
                    {#if b.steps}<p class="meta"><strong>{$t('sbug.steps')}:</strong> {b.steps}</p>{/if}
                    {#if b.device}<p class="meta"><strong>{$t('sbug.device')}:</strong> {b.device}</p>{/if}

                    <div class="bug-foot">
                        <span class="reporter">{$t('sbug.reporter')}: <strong>{b.reporterName}</strong> · {$t('sbug.role.' + b.reporterRole) ?? b.reporterRole}</span>
                        <div class="actions">
                            <form method="POST" action="?/setStatus" use:enhance={refresh}>
                                <input type="hidden" name="id" value={b.id} />
                                <select name="status" value={b.status} onchange={(e) => e.currentTarget.form.requestSubmit()}>
                                    {#each STATUSES as s}<option value={s}>{$t('sbug.status.' + s)}</option>{/each}
                                </select>
                            </form>
                            <form method="POST" action="?/delete" use:enhance={refresh} onsubmit={(e) => { if (!confirm($t('sbug.deleteConfirm'))) e.preventDefault(); }}>
                                <input type="hidden" name="id" value={b.id} />
                                <button type="submit" class="del" title={$t('sbug.delete')}>🗑️</button>
                            </form>
                        </div>
                    </div>
                </article>
            {/each}
        </div>
    {/if}
</div>

<style>
    .page { max-width: 980px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    .head { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.75rem; }
    h1 { margin: 0; color: #2F124D; font-size: 1.5rem; }
    .counts { display: flex; gap: 0.5rem; }
    .pill { padding: 0.3rem 0.8rem; border-radius: 999px; font-size: 0.8rem; font-weight: 700; }
    .pill.open { background: #fff4e6; color: #b45309; border: 1px solid #fcd9b6; }
    .pill.total { background: #f0e7fa; color: #2F124D; }

    .leaderboard { background: linear-gradient(135deg, #fff8ef, #fdeedd); border: 1px solid #f7c98f; border-radius: 1rem; padding: 1rem 1.25rem; margin: 1.25rem 0; }
    .leaderboard h2 { margin: 0 0 0.6rem; font-size: 1.05rem; color: #8a4b12; }
    .leaderboard ol { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 0.35rem; }
    .leaderboard li { display: flex; align-items: center; gap: 0.6rem; font-size: 0.92rem; }
    .rank { font-weight: 800; color: #b45309; min-width: 1.6rem; }
    .lb-name { font-weight: 700; color: #2d2141; }
    .lb-role { color: #8a7f9a; font-size: 0.82rem; }
    .lb-count { margin-left: auto; font-weight: 700; color: #b45309; }

    .filters { display: flex; flex-wrap: wrap; gap: 0.5rem; margin: 1rem 0 1.25rem; }
    .filters select, .filters .search { border: 1px solid #e8e0f0; border-radius: 0.6rem; padding: 0.5rem 0.7rem; font: inherit; background: #fff; }
    .filters .search { flex: 1; min-width: 180px; }

    .empty { color: #8a7f9a; padding: 2rem; text-align: center; background: #faf8fc; border: 2px dashed #e0d6eb; border-radius: 1rem; }

    .list { display: flex; flex-direction: column; gap: 0.85rem; }
    .bug { background: #fff; border: 1px solid #ece3f5; border-left: 4px solid #c9b8e0; border-radius: 1rem; padding: 1rem 1.15rem; }
    .bug.sev-CRITICAL { border-left-color: #dc2626; }
    .bug.sev-HIGH { border-left-color: #ea580c; }
    .bug.sev-MEDIUM { border-left-color: #d97706; }
    .bug.sev-LOW { border-left-color: #16a34a; }
    .bug-top { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
    .bug-top h3 { margin: 0; font-size: 1rem; color: #2d2141; flex: 1; min-width: 0; }
    .date { font-size: 0.78rem; color: #9a8b9d; }
    .sev-badge { font-size: 0.7rem; font-weight: 700; padding: 0.15rem 0.55rem; border-radius: 999px; color: #fff; }
    .sev-badge.sev-CRITICAL { background: #dc2626; }
    .sev-badge.sev-HIGH { background: #ea580c; }
    .sev-badge.sev-MEDIUM { background: #d97706; }
    .sev-badge.sev-LOW { background: #16a34a; }
    .area-badge { font-size: 0.7rem; font-weight: 600; padding: 0.15rem 0.55rem; border-radius: 999px; background: #f0e7fa; color: #2F124D; }
    .desc { margin: 0.6rem 0 0.4rem; color: #374151; white-space: pre-wrap; line-height: 1.5; }
    .meta { margin: 0.2rem 0; font-size: 0.85rem; color: #5E4C6F; white-space: pre-wrap; }
    .meta strong { color: #2d2141; }
    .bug-foot { display: flex; justify-content: space-between; align-items: center; gap: 0.75rem; margin-top: 0.75rem; padding-top: 0.6rem; border-top: 1px solid #f3f0f7; flex-wrap: wrap; }
    .reporter { font-size: 0.82rem; color: #6b647a; }
    .actions { display: flex; gap: 0.5rem; align-items: center; }
    .actions select { border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.35rem 0.5rem; font: inherit; background: #faf8fc; }
    .del { background: none; border: none; cursor: pointer; font-size: 1rem; }

    @media (max-width: 560px) { .bug-foot { flex-direction: column; align-items: flex-start; } }
</style>
