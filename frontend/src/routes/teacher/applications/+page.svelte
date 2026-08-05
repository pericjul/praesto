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
                <div class="list">
                    {#each filtered as s (s.studentId)}
                        <article class="student">
                            <div class="student-head">
                                <span class="s-name">{s.name}</span>
                                {#if s.shared}
                                    <span class="s-badge shared">✓ {$t('tapp.shared')}</span>
                                {:else}
                                    <span class="s-badge notshared">🔒 {$t('tapp.notShared')}</span>
                                {/if}
                            </div>

                            {#if s.shared}
                                {#if s.applications.length === 0}
                                    <p class="none">{$t('tapp.noApplications')}</p>
                                {:else}
                                    <div class="apps">
                                        {#each s.applications as a (a.id)}
                                            {@const st = statusOf(a.status)}
                                            <div class="app">
                                                <div class="app-main">
                                                    <span class="company">{a.companyName}</span>
                                                    {#if a.position}<span class="position">· {a.position}</span>{/if}
                                                </div>
                                                <div class="app-meta">
                                                    <span class="status" style="--c: {st.color}">{st.emoji} {st.label()}</span>
                                                    {#if a.appliedAt}<span class="date">{$t('tapp.applied')} {fmtDate(a.appliedAt)}</span>{/if}
                                                    {#if a.interviewDate}<span class="date">🗓️ {fmtDate(a.interviewDate)}</span>{/if}
                                                </div>
                                            </div>
                                        {/each}
                                    </div>
                                {/if}
                            {:else}
                                <p class="none muted">{$t('tapp.notSharedHint')}</p>
                            {/if}
                        </article>
                    {/each}
                </div>
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

    .list { display: flex; flex-direction: column; gap: 0.85rem; }
    .student { background: #fff; border: 1px solid #ece3f5; border-radius: 0.9rem; padding: 0.9rem 1.1rem; }
    .student-head { display: flex; align-items: center; gap: 0.6rem; }
    .s-name { font-weight: 600; color: #2d2141; }
    .s-badge { font-size: 0.72rem; font-weight: 600; border-radius: 999px; padding: 0.1rem 0.55rem; }
    .s-badge.shared { background: #f0e7fa; color: #5b2a86; }
    .s-badge.notshared { background: #f1eff4; color: #6b647a; }

    .none { margin: 0.5rem 0 0; font-size: 0.85rem; color: #6b647a; }
    .none.muted { color: #9a8b9d; }

    .apps { display: flex; flex-direction: column; gap: 0.5rem; margin-top: 0.7rem; }
    .app { border: 1px solid #f0ebf5; border-radius: 0.6rem; padding: 0.55rem 0.75rem; }
    .app-main { font-size: 0.92rem; }
    .company { font-weight: 600; color: #2d2141; }
    .position { color: #6b647a; }
    .app-meta { display: flex; flex-wrap: wrap; gap: 0.6rem; margin-top: 0.25rem; font-size: 0.8rem; }
    .status { font-weight: 600; color: var(--c); }
    .date { color: #8a7f9a; }
</style>
