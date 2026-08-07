<script>
    import { t } from "$lib/i18n";

    let { data } = $props();

    let classes = $derived(data.classes ?? []);
    let overview = $derived(data.overview);
    let selectedId = $derived(data.selectedId);

    let search = $state("");
    $effect(() => { selectedId; search = ""; });

    let students = $derived(overview?.students ?? []);
    let filtered = $derived(
        students.filter((s) => !search.trim() || (s.name ?? "").toLowerCase().includes(search.trim().toLowerCase()))
    );
    let sharedCount = $derived(students.filter((s) => s.shared).length);

    const STATUS = {
        TRIAL_PLANNED: { label: () => $t("sapp.statusTrialPlanned"), emoji: "🔍", color: "#14b8a6" },
        TRIAL_DONE: { label: () => $t("sapp.statusTrialDone"), emoji: "🤝", color: "#0d9488" },
        PLANNED: { label: () => $t("sapp.statusPlanned"), emoji: "📝", color: "#6b7280" },
        APPLIED: { label: () => $t("sapp.statusApplied"), emoji: "📤", color: "#3b82f6" },
        INVITED: { label: () => $t("sapp.statusInvited"), emoji: "📅", color: "#f59e0b" },
        INTERVIEW_DONE: { label: () => $t("sapp.statusInterviewDone"), emoji: "✅", color: "#8b5cf6" },
        ACCEPTED: { label: () => $t("sapp.statusAccepted"), emoji: "🎉", color: "#10b981" },
        REJECTED: { label: () => $t("sapp.statusRejected"), emoji: "❌", color: "#ef4444" },
        WITHDRAWN: { label: () => $t("sapp.statusWithdrawn"), emoji: "🔙", color: "#9ca3af" }
    };
    function statusOf(code) {
        return STATUS[code] ?? { label: () => code, emoji: "•", color: "#9ca3af" };
    }
    function fmtDate(d) {
        if (!d) return "";
        try { return new Date(d).toLocaleDateString("de-CH", { day: "2-digit", month: "2-digit", year: "numeric" }); }
        catch { return ""; }
    }

    // Wichtiges zuerst: laufende Gespräche/Einladungen vor Absagen.
    const ORDER = { INVITED: 0, INTERVIEW_DONE: 1, ACCEPTED: 2, APPLIED: 3, TRIAL_DONE: 4, PLANNED: 5, TRIAL_PLANNED: 6, REJECTED: 7, WITHDRAWN: 8 };
    function sortedApps(apps) {
        return [...(apps ?? [])].sort((a, b) => (ORDER[a.status] ?? 9) - (ORDER[b.status] ?? 9));
    }
</script>

<svelte:head><title>{$t('tapp.title')} – Praesto</title></svelte:head>

<div class="page">
    <h1>📊 {$t('tapp.title')}</h1>
    <p class="subtitle">{$t('tapp.subtitle')}</p>

    {#if classes.length === 0}
        <p class="empty">{$t('tapp.noClasses')}</p>
    {:else}
        <div class="class-tabs">
            <span class="tabs-label">{$t('tapp.selectClass')}:</span>
            {#each classes as c (c.id)}
                <a href={`?class=${c.id}`} class="tab" class:active={c.id === selectedId}>{c.name}</a>
            {/each}
        </div>

        {#if overview}
            <div class="topbar">
                <input class="search" type="search" placeholder={$t('tapp.searchStudent')} bind:value={search} />
                <span class="sharecount">{sharedCount} / {students.length} {$t('tapp.sharedCount')}</span>
            </div>

            {#if filtered.length === 0}
                <p class="empty">{$t('tapp.noStudents')}</p>
            {:else}
                {@const sharedStudents = filtered.filter((s) => s.shared)}
                {@const notSharedStudents = filtered.filter((s) => !s.shared)}

                {#if sharedStudents.length === 0}
                    <p class="empty">{$t('tapp.noneShared')}</p>
                {:else}
                    <div class="table-wrap">
                        <table class="ov">
                            <thead>
                                <tr>
                                    <th class="th-name">{$t('tapp.thStudent')}</th>
                                    <th>{$t('tapp.thApplications')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                {#each sharedStudents as s (s.studentId)}
                                    <tr>
                                        <td class="name-cell">{s.name}</td>
                                        <td class="apps-cell">
                                            {#if !s.applications || s.applications.length === 0}
                                                <span class="muted">{$t('tapp.noApplications')}</span>
                                            {:else}
                                                {#each sortedApps(s.applications) as a (a.id)}
                                                    {@const st = statusOf(a.status)}
                                                    <span class="chip" style="--c: {st.color}"
                                                        title={a.interviewDate ? `${st.label()} · 🗓️ ${fmtDate(a.interviewDate)}` : st.label()}>
                                                        <span class="chip-em">{st.emoji}</span>
                                                        <span class="chip-co">{a.companyName}</span>
                                                        <span class="chip-st">{st.label()}</span>
                                                    </span>
                                                {/each}
                                            {/if}
                                        </td>
                                    </tr>
                                {/each}
                            </tbody>
                        </table>
                    </div>
                {/if}

                {#if notSharedStudents.length > 0}
                    <div class="ns-panel">
                        <span class="ns-title">🔒 {$t('tapp.notSharedTitle')} <span class="ns-count">{notSharedStudents.length}</span></span>
                        <div class="ns-chips">
                            {#each notSharedStudents as s (s.studentId)}
                                <span class="ns-chip">{s.name}</span>
                            {/each}
                        </div>
                    </div>
                {/if}
            {/if}
        {/if}
    {/if}
</div>

<style>
    .page { max-width: 1000px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    h1 { margin: 0 0 0.3rem; color: #2F124D; font-size: 1.6rem; }
    .subtitle { margin: 0 0 1.25rem; color: #6b647a; }
    .empty { color: #9a8b9d; }

    .class-tabs { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 1.25rem; }
    .tabs-label { font-size: 0.85rem; color: #6b647a; }
    .tab { text-decoration: none; padding: 0.35rem 0.85rem; border-radius: 999px; background: #f3f0f7; color: #2d2141; font-size: 0.9rem; }
    .tab.active { background: #2F124D; color: #fff; }

    .topbar { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 1rem; flex-wrap: wrap; }
    .search { flex: 1; min-width: 180px; padding: 0.55rem 0.75rem; border: 1px solid #e8e0f0; border-radius: 0.6rem; background: #faf8fc; font: inherit; }
    .sharecount { font-size: 0.85rem; color: #6b647a; white-space: nowrap; }

    .table-wrap { overflow-x: auto; border: 1px solid #ece3f5; border-radius: 0.9rem; }
    .ov { width: 100%; border-collapse: collapse; min-width: 520px; background: #fff; }
    .ov th { text-align: left; font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.03em; color: #9a8b9d; font-weight: 600; padding: 0.55rem 0.9rem; border-bottom: 2px solid #ece3f5; background: #faf8fc; }
    .th-name { width: 1%; white-space: nowrap; }
    .ov td { padding: 0.55rem 0.9rem; border-bottom: 1px solid #f3eff8; vertical-align: middle; }
    .ov tbody tr:last-child td { border-bottom: none; }
    .ov tbody tr:hover { background: #faf8fc; }
    .name-cell { font-weight: 600; color: #2d2141; white-space: nowrap; }
    .apps-cell { display: flex; flex-wrap: wrap; gap: 0.4rem; }
    .apps-cell .muted { color: #9a8b9d; font-size: 0.85rem; }

    .chip {
        display: inline-flex; align-items: center; gap: 0.35rem;
        font-size: 0.8rem; padding: 0.2rem 0.6rem; border-radius: 999px;
        background: color-mix(in srgb, var(--c) 12%, #fff);
        border: 1px solid color-mix(in srgb, var(--c) 35%, #fff);
        color: #2d2141; white-space: nowrap;
    }
    .chip-co { font-weight: 600; }
    .chip-st { color: var(--c); font-weight: 600; }

    /* Nicht-freigegebene Schüler:innen kompakt als Chips (statt grosser Leerkarten) */
    .ns-panel { margin-top: 1.25rem; background: #faf8fc; border: 1px solid #ece3f5; border-radius: 0.9rem; padding: 0.9rem 1.1rem; }
    .ns-title { font-size: 0.85rem; font-weight: 600; color: #6b647a; display: inline-flex; align-items: center; gap: 0.4rem; }
    .ns-count { background: #ece3f5; color: #5b2a86; border-radius: 999px; padding: 0.05rem 0.5rem; font-size: 0.75rem; }
    .ns-chips { display: flex; flex-wrap: wrap; gap: 0.4rem; margin-top: 0.6rem; }
    .ns-chip { background: #fff; border: 1px solid #e8e0f0; color: #6b647a; border-radius: 999px; padding: 0.2rem 0.7rem; font-size: 0.82rem; }
</style>
