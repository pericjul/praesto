<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    let showForm = $state(false);
    let lastInvite = $state(null);
    let copied = $state(false);

    let search = $state("");
    let onlyInactive = $state(false);

    const INACTIVE_DAYS = 21; // Schule gilt als "still", wenn seit 3 Wochen niemand eingeloggt

    let stats = $derived(data.stats ?? null);
    let totalUsers = $derived(stats?.totalUsers ?? (data.schools ?? []).reduce((s, x) => s + (x.userCount ?? 0), 0));
    let activeSchools = $derived(stats?.activeSchools ?? (data.schools ?? []).filter((s) => s.active).length);

    function daysSince(ts) {
        if (!ts) return null;
        return Math.floor((Date.now() - new Date(ts).getTime()) / 86400000);
    }
    // Nur aktive Schulen MIT Nutzern, in denen seit >21 Tagen (oder noch nie) niemand eingeloggt hat.
    function isQuiet(s) {
        if (!s.active || (s.userCount ?? 0) === 0) return false;
        if (!s.lastActivityAt) return true;
        return Date.now() - new Date(s.lastActivityAt).getTime() > INACTIVE_DAYS * 86400000;
    }
    function lastActivityText(s) {
        if (!s.lastActivityAt) return $t("sadmin.neverActive");
        const d = daysSince(s.lastActivityAt);
        if (d <= 0) return $t("sadmin.activeToday");
        return $t("sadmin.activeDaysAgo").replace("%N", d);
    }

    let quietCount = $derived((data.schools ?? []).filter(isQuiet).length);
    let topSchools = $derived([...(data.schools ?? [])].filter((s) => (s.sessionCount ?? 0) > 0).slice(0, 3));

    let sortedSchools = $derived(
        [...(data.schools ?? [])]
            .filter((s) => {
                if (onlyInactive && !isQuiet(s)) return false;
                const q = search.trim().toLowerCase();
                if (!q) return true;
                return `${s.name ?? ""} ${s.city ?? ""} ${s.canton ?? ""}`.toLowerCase().includes(q);
            })
            .sort((a, b) => (b.userCount ?? 0) - (a.userCount ?? 0))
    );

    function inviteUrl(token) {
        return `${data.origin}/join/${token}`;
    }

    async function copy(token) {
        try {
            await navigator.clipboard.writeText(inviteUrl(token));
            copied = true;
            setTimeout(() => (copied = false), 1500);
        } catch {
            copied = false;
        }
    }

    function handleCreateSchool() {
        return async ({ result, update }) => {
            if (result.type === "success") {
                showForm = false;
                await invalidateAll();
            }
            await update({ reset: true });
        };
    }

    function handleInvite() {
        return async ({ result, update }) => {
            if (result.type === "success" && result.data?.invite) {
                lastInvite = result.data.invite;
            }
            await update({ reset: false });
        };
    }
</script>

<svelte:head>
    <title>{$t('sadmin.headTitle')}</title>
</svelte:head>

<div class="page">
    <header class="page-head">
        <h1>{$t('sadmin.title')}</h1>
        <button class="btn-primary" type="button" onclick={() => (showForm = !showForm)}>
            {showForm ? $t('sadmin.cancel') : $t('sadmin.newSchool')}
        </button>
    </header>

    {#if data.schools && data.schools.length > 0}
        <section class="totals">
            <div class="total"><span class="t-num">{data.schools.length}</span><span class="t-lbl">{$t('sadmin.totalSchools')}</span></div>
            <div class="total"><span class="t-num">{activeSchools}</span><span class="t-lbl">{$t('sadmin.activeSchools')}</span></div>
            <div class="total"><span class="t-num">{totalUsers}</span><span class="t-lbl">{$t('sadmin.totalUsers')}</span></div>
            {#if stats}
                <div class="total"><span class="t-num">{stats.totalStudents}</span><span class="t-lbl">{$t('sadmin.totalStudents')}</span></div>
                <div class="total"><span class="t-num">{stats.totalTeachers}</span><span class="t-lbl">{$t('sadmin.totalTeachers')}</span></div>
                <div class="total"><span class="t-num">{stats.totalSessions}</span><span class="t-lbl">{$t('sadmin.totalSessions')}</span></div>
                <div class="total"><span class="t-num">{stats.totalSubmissions}</span><span class="t-lbl">{$t('sadmin.totalSubmissions')}</span></div>
            {/if}
        </section>

        {#if quietCount > 0}
            <button type="button" class="quiet-banner" class:on={onlyInactive} onclick={() => (onlyInactive = !onlyInactive)}>
                ⚠️ {$t('sadmin.quietSchools').replace('%N', quietCount).replace('%D', INACTIVE_DAYS)}
                <span class="quiet-cta">{onlyInactive ? $t('sadmin.showAll') : $t('sadmin.filter')}</span>
            </button>
        {/if}

        {#if topSchools.length > 0 && !onlyInactive}
            <section class="top-schools">
                <h2>🏆 {$t('sadmin.topSchools')}</h2>
                <ol>
                    {#each topSchools as s, i}
                        <li>
                            <span class="ts-rank">{i + 1}.</span>
                            <span class="ts-name">{s.name}</span>
                            <span class="ts-count">{s.sessionCount} {$t('sadmin.sessionsShort')}</span>
                        </li>
                    {/each}
                </ol>
            </section>
        {/if}
    {/if}

    {#if form?.error}
        <div class="alert">{form.error}</div>
    {/if}

    {#if showForm}
        <form method="POST" action="?/createSchool" use:enhance={handleCreateSchool} class="card school-form">
            <h2>{$t('sadmin.newSchoolTitle')}</h2>
            <div class="form-grid">
                <label>{$t('sadmin.fieldName')} <input name="name" required placeholder="Kantonsschule Aarau" /></label>
                <label>{$t('sadmin.fieldCanton')} <input name="canton" placeholder="AG" maxlength="2" /></label>
                <label>{$t('sadmin.fieldCity')} <input name="city" placeholder="Aarau" /></label>
            </div>
            <button class="btn-primary" type="submit">{$t('sadmin.createSchool')}</button>
        </form>
    {/if}

    {#if lastInvite}
        <div class="card invite-banner">
            <div>
                <strong>{$t('sadmin.adminLinkCreated')}</strong>
                <code>{inviteUrl(lastInvite.token)}</code>
            </div>
            <button class="btn-ghost" type="button" onclick={() => copy(lastInvite.token)}>
                {copied ? $t('sadmin.copied') : $t('sadmin.copy')}
            </button>
        </div>
    {/if}

    {#if data.schools && data.schools.length > 0}
        <div class="school-search">
            <input type="search" bind:value={search} placeholder={$t('sadmin.searchPlaceholder')} />
            {#if search.trim()}
                <span class="search-count">{sortedSchools.length} / {data.schools.length}</span>
            {/if}
        </div>
    {/if}

    <div class="schools">
        {#each sortedSchools as school (school.id)}
            <article class="card school" class:quiet={isQuiet(school)}>
                <div class="school-head">
                    <h3>{school.name}</h3>
                    {#if !school.active}<span class="inactive">{$t('sadmin.inactive')}</span>
                    {:else if isQuiet(school)}<span class="quiet-badge">💤 {$t('sadmin.quiet')}</span>{/if}
                </div>
                <p class="school-meta">{school.city ?? "–"}{school.canton ? `, ${school.canton}` : ""}</p>
                <p class="school-count">👥 {school.userCount} {$t('sadmin.userCountSuffix')}{#if school.sessionCount != null} · 💬 {school.sessionCount}{/if}</p>
                {#if school.lastActivityAt != null || school.active}
                    <p class="school-activity" class:stale={isQuiet(school)}>🕓 {lastActivityText(school)}</p>
                {/if}
                <div class="school-actions">
                    <a class="btn-secondary" href={`/super/schools/${school.id}/export`} download>⬇️ {$t('sadmin.exportCsv')}</a>
                    <form method="POST" action="?/createAdminInvite" use:enhance={handleInvite}>
                        <input type="hidden" name="schoolId" value={school.id} />
                        <button class="btn-secondary" type="submit">{$t('sadmin.createAdminLink')}</button>
                    </form>
                    <form method="POST" action="?/setSchoolActive"
                        use:enhance={({ cancel }) => {
                            if (school.active && !confirm(`Schule «${school.name}» sperren? Niemand dieser Schule kann sich dann mehr einloggen. Die Daten bleiben vollständig erhalten und du kannst jederzeit wieder entsperren.`)) {
                                cancel();
                                return;
                            }
                            return async ({ update }) => { await update(); };
                        }}>
                        <input type="hidden" name="schoolId" value={school.id} />
                        <input type="hidden" name="active" value={school.active ? "false" : "true"} />
                        <button class="btn-ghost" class:lock={school.active} class:unlock={!school.active} type="submit">
                            {school.active ? "🔒 Sperren (Zahlung)" : "🔓 Entsperren"}
                        </button>
                    </form>
                </div>
            </article>
        {:else}
            <p class="empty">{search.trim() ? $t('sadmin.noSearchResults') : $t('sadmin.empty')}</p>
        {/each}
    </div>
</div>

<style>
    .page { max-width: 1000px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    .page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 0.5rem; }
    h1 { margin: 0; color: #2F124D; font-size: 1.6rem; }

    .totals { display: grid; grid-template-columns: repeat(auto-fit, minmax(110px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }
    .total { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1.1rem; text-align: center; }
    .t-num { display: block; font-size: 1.8rem; font-weight: 700; color: #2F124D; }
    .t-lbl { font-size: 0.82rem; color: #6b647a; }

    .card { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1.25rem; margin-bottom: 1.25rem; }
    .school-form h2 { margin: 0 0 1rem; font-size: 1.05rem; color: #2d2141; }
    .form-grid { display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 0.75rem; margin-bottom: 1rem; }
    label { display: flex; flex-direction: column; gap: 0.3rem; font-size: 0.85rem; color: #2d2141; }
    input { padding: 0.6rem 0.8rem; border: 1px solid #e8e0f0; border-radius: 0.5rem; background: #faf8fc; }

    .btn-primary { background: #2F124D; color: #fff; border: none; border-radius: 0.5rem; padding: 0.55rem 1.1rem; font-weight: 600; cursor: pointer; }
    .btn-secondary { display: inline-block; background: transparent; border: 1px solid #2F124D; color: #2F124D; border-radius: 0.5rem; padding: 0.45rem 0.9rem; font-weight: 600; cursor: pointer; text-decoration: none; font: inherit; }
    a.btn-secondary:hover { background: #f5f0fb; }

    .school-search { display: flex; align-items: center; gap: 0.6rem; margin-bottom: 1rem; }
    .school-search input { flex: 1; padding: 0.6rem 0.85rem; border: 1px solid #e8e0f0; border-radius: 0.6rem; background: #faf8fc; font: inherit; }
    .school-search input:focus { outline: 2px solid #2F124D; outline-offset: 1px; background: #fff; }
    .search-count { font-size: 0.85rem; color: #6b647a; white-space: nowrap; }
    .btn-ghost { background: #faf8fc; border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.4rem 0.8rem; cursor: pointer; }
    .school-actions { display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; margin-top: 0.25rem; }
    .btn-ghost.lock { color: #b91c1c; border-color: #fecaca; }
    .btn-ghost.unlock { color: #166534; border-color: #bbf7d0; }

    .invite-banner { display: flex; justify-content: space-between; align-items: center; gap: 1rem; background: #f0fdf4; border-color: #bbf7d0; flex-wrap: wrap; }
    .invite-banner code { display: block; font-size: 0.8rem; word-break: break-all; margin-top: 0.25rem; }

    .schools { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 1rem; }
    .school { margin: 0; }
    .school-head { display: flex; justify-content: space-between; align-items: center; }
    .school h3 { margin: 0; color: #2F124D; font-size: 1.05rem; }
    .school-meta { color: #6b647a; font-size: 0.85rem; margin: 0.25rem 0; }
    .school-count { color: #2d2141; font-size: 0.85rem; margin: 0 0 0.75rem; }
    .inactive { background: #fef2f2; color: #dc2626; border-radius: 999px; padding: 0.1rem 0.5rem; font-size: 0.7rem; }
    .quiet-badge { background: #fffbeb; color: #b45309; border: 1px solid #fcd34d; border-radius: 999px; padding: 0.1rem 0.5rem; font-size: 0.68rem; font-weight: 600; white-space: nowrap; }
    .school.quiet { border-color: #fcd34d; }
    .school-activity { color: #6b647a; font-size: 0.8rem; margin: 0 0 0.75rem; }
    .school-activity.stale { color: #b45309; font-weight: 600; }

    .quiet-banner { display: flex; align-items: center; gap: 0.6rem; width: 100%; text-align: left; cursor: pointer;
        background: linear-gradient(135deg, #fffbeb, #fef3c7); border: 1px solid #fcd34d; color: #92400e;
        border-radius: 0.75rem; padding: 0.7rem 1rem; margin: 0 0 1.25rem; font-weight: 600; font-size: 0.9rem; }
    .quiet-banner.on { background: #fef3c7; box-shadow: inset 0 0 0 2px #f59e0b; }
    .quiet-cta { margin-left: auto; text-decoration: underline; font-weight: 700; white-space: nowrap; }

    .top-schools { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1rem 1.25rem; margin: 0 0 1.25rem; }
    .top-schools h2 { margin: 0 0 0.6rem; font-size: 1.02rem; color: #2F124D; }
    .top-schools ol { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 0.35rem; }
    .top-schools li { display: flex; align-items: center; gap: 0.6rem; font-size: 0.92rem; }
    .ts-rank { font-weight: 800; color: #7c3aed; min-width: 1.4rem; }
    .ts-name { font-weight: 600; color: #2d2141; }
    .ts-count { margin-left: auto; font-weight: 700; color: #7c3aed; }
    .empty { color: #9a8b9d; }
    .alert { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; border-radius: 0.5rem; padding: 0.75rem 1rem; margin-bottom: 1rem; }

    @media (max-width: 700px) { .form-grid { grid-template-columns: 1fr; } }
</style>
