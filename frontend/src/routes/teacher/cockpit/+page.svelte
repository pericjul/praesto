<script>
    import { t } from "$lib/i18n";
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";

    let { data } = $props();

    let classes = $derived(data.classes ?? []);
    let cockpit = $derived(data.cockpit);
    let challenge = $derived(data.challenge);
    let selectedId = $derived(data.selectedId);

    function refresh() {
        return async ({ result, update }) => {
            if (result.type === "success") await invalidateAll();
            await update({ reset: false });
        };
    }

    function reasonText(code) {
        return {
            NEVER_PRACTICED: $t("cockpit.reason.NEVER_PRACTICED"),
            LOW_SCORE: $t("cockpit.reason.LOW_SCORE"),
            INACTIVE: $t("cockpit.reason.INACTIVE")
        }[code] ?? code;
    }

    function lastActive(d) {
        if (!d) return $t("cockpit.never");
        return new Date(d).toLocaleDateString("de-CH", { day: "2-digit", month: "2-digit", year: "numeric" });
    }

    function scoreClass(v) {
        if (v == null) return "s-none";
        if (v >= 60) return "s-high";
        if (v >= 30) return "s-mid";
        return "s-low";
    }
</script>

<svelte:head>
    <title>{$t('cockpit.title')} – Praesto</title>
</svelte:head>

<div class="page">
    <h1>📊 {$t('cockpit.title')}</h1>
    <p class="subtitle">{$t('cockpit.subtitle')}</p>

    {#if classes.length === 0}
        <p class="empty">{$t('cockpit.noClasses')}</p>
    {:else}
        <div class="class-tabs">
            <span class="tabs-label">{$t('cockpit.selectClass')}:</span>
            {#each classes as c (c.id)}
                <a href={`?class=${c.id}`} class="tab" class:active={c.id === selectedId}>{c.name}</a>
            {/each}
        </div>

        {#if cockpit}
            <div class="summary">
                <div class="sum-card"><span class="sv">{cockpit.studentCount}</span><span class="sl">{$t('cockpit.sumStudents')}</span></div>
                <div class="sum-card"><span class="sv">{cockpit.practicedCount}</span><span class="sl">{$t('cockpit.sumPracticed')}</span></div>
                <div class="sum-card"><span class="sv">{cockpit.avgClassScore != null ? cockpit.avgClassScore + '%' : '–'}</span><span class="sl">{$t('cockpit.sumAvg')}</span></div>
                <div class="sum-card attention"><span class="sv">{cockpit.needAttentionCount}</span><span class="sl">{$t('cockpit.sumAttention')}</span></div>
            </div>

            <!-- Klassen-Challenge -->
            <section class="challenge-panel">
                {#if challenge}
                    <div class="ch-head">
                        <h2>🚀 {challenge.title || $t('challenge.defaultTitle')}</h2>
                        <form method="POST" action="?/endChallenge" use:enhance={refresh}>
                            <input type="hidden" name="classId" value={selectedId} />
                            <button type="submit" class="ch-end">{$t('challenge.end')}</button>
                        </form>
                    </div>
                    <div class="ch-bar"><div class="ch-fill" style="width:{challenge.percent}%"></div></div>
                    <p class="ch-progress">
                        <strong>{challenge.current} / {challenge.target}</strong> {$t('challenge.interviews')}
                        {#if challenge.percent >= 100}<span class="ch-done">{$t('challenge.done')}</span>{/if}
                    </p>
                {:else}
                    <div class="ch-start">
                        <div>
                            <h2>🚀 {$t('challenge.title')}</h2>
                            <p>{$t('challenge.startHint')}</p>
                        </div>
                        <form method="POST" action="?/startChallenge" use:enhance={refresh}>
                            <input type="hidden" name="classId" value={selectedId} />
                            <button type="submit" class="ch-start-btn">{$t('challenge.start')}</button>
                        </form>
                    </div>
                {/if}
            </section>

            <!-- Gesprächsleitfaden -->
            <section class="guidance">
                <h2>🗣️ {$t('cockpit.guidanceTitle')}</h2>
                {#if cockpit.guidance.length === 0}
                    <p class="guidance-empty">{$t('cockpit.guidanceEmpty')}</p>
                {:else}
                    <ul>
                        {#each cockpit.guidance as g (g.studentId)}
                            <li><strong>{g.name}</strong> — {reasonText(g.reasonCode)}</li>
                        {/each}
                    </ul>
                {/if}
            </section>

            <!-- Schüler-Tabelle -->
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>{$t('cockpit.colStudent')}</th>
                            <th>{$t('cockpit.colSessions')}</th>
                            <th>{$t('cockpit.colBest')}</th>
                            <th>{$t('cockpit.colAvg')}</th>
                            <th>{$t('cockpit.colLast')}</th>
                            <th>{$t('cockpit.colStatus')}</th>
                        </tr>
                    </thead>
                    <tbody>
                        {#each cockpit.students as s (s.studentId)}
                            <tr class:row-attention={s.needsAttention}>
                                <td class="name">{s.name}</td>
                                <td>{s.sessionCount}</td>
                                <td><span class="score {scoreClass(s.bestScore)}">{s.bestScore != null ? s.bestScore + '%' : '–'}</span></td>
                                <td>{s.avgScore != null ? s.avgScore + '%' : '–'}</td>
                                <td>{lastActive(s.lastActivity)}</td>
                                <td>
                                    {#if s.needsAttention}
                                        <span class="flag">⚠️ {s.reasons.map(reasonText).join(', ')}</span>
                                    {:else}
                                        <span class="ok">✓ {$t('cockpit.ok')}</span>
                                    {/if}
                                </td>
                            </tr>
                        {/each}
                    </tbody>
                </table>
            </div>
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

    .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 0.75rem; margin-bottom: 1.25rem; }
    .sum-card { background: #fff; border: 1px solid #e8e0f0; border-radius: 0.9rem; padding: 0.9rem 1rem; display: flex; flex-direction: column; gap: 0.2rem; }
    .sum-card.attention { border-color: #fcd34d; background: #fffbeb; }
    .sv { font-size: 1.6rem; font-weight: 800; color: #2F124D; }
    .sum-card.attention .sv { color: #b45309; }
    .sl { font-size: 0.78rem; color: #6b647a; }

    .guidance { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1.1rem 1.25rem; margin-bottom: 1.25rem; }
    .guidance h2 { margin: 0 0 0.6rem; font-size: 1.1rem; color: #2F124D; }
    .guidance ul { margin: 0; padding-left: 1.1rem; display: grid; gap: 0.35rem; }
    .guidance li { color: #374151; line-height: 1.5; }
    .guidance-empty { margin: 0; color: #16a34a; }

    .table-wrap { overflow-x: auto; background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; }
    table { width: 100%; border-collapse: collapse; }
    th, td { padding: 0.7rem 0.9rem; text-align: left; font-size: 0.9rem; border-bottom: 1px solid #f0ebf5; }
    th { font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.03em; color: #9a8b9d; }
    tr.row-attention { background: #fffdf5; }
    td.name { font-weight: 600; color: #2d2141; }

    .score { font-weight: 700; padding: 0.15rem 0.5rem; border-radius: 999px; }
    .score.s-high { background: #f0fdf4; color: #16a34a; }
    .score.s-mid { background: #fffbeb; color: #b45309; }
    .score.s-low { background: #fef2f2; color: #b91c1c; }
    .score.s-none { background: #f3f4f6; color: #9ca3af; }

    .flag { color: #b45309; font-size: 0.82rem; }
    .ok { color: #16a34a; font-size: 0.85rem; }

    /* Klassen-Challenge */
    .challenge-panel { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1.1rem 1.25rem; margin-bottom: 1.25rem; }
    .ch-head { display: flex; justify-content: space-between; align-items: center; gap: 1rem; }
    .ch-head h2 { margin: 0; font-size: 1.1rem; color: #2F124D; }
    .ch-end { background: #faf8fc; border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.4rem 0.85rem; cursor: pointer; font-size: 0.82rem; color: #2d2141; }
    .ch-bar { height: 14px; background: #eee; border-radius: 999px; overflow: hidden; margin: 0.75rem 0 0.5rem; }
    .ch-fill { height: 100%; background: linear-gradient(90deg, #2F124D, #c97d3c); border-radius: 999px; transition: width 0.3s; }
    .ch-progress { margin: 0; color: #374151; }
    .ch-done { color: #16a34a; font-weight: 700; margin-left: 0.5rem; }
    .ch-start { display: flex; justify-content: space-between; align-items: center; gap: 1rem; flex-wrap: wrap; }
    .ch-start h2 { margin: 0 0 0.2rem; font-size: 1.1rem; color: #2F124D; }
    .ch-start p { margin: 0; color: #6b647a; font-size: 0.9rem; }
    .ch-start-btn { background: #2F124D; color: #fff; border: none; border-radius: 999px; padding: 0.7rem 1.5rem; font-weight: 700; cursor: pointer; white-space: nowrap; }
    .ch-start-btn:hover { background: #41205f; }
</style>
