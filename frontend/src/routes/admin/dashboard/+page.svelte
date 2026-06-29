<script>
    import { enhance } from "$app/forms";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    let invites = $state(data.invites ?? []);
    let copiedToken = $state(null);
    let insights = $derived(data.insights);

    const roleLabels = {
        TEACHER: "👩‍🏫",
        STUDENT: "🎓",
        SCHOOL_ADMIN: "🏫",
        DEMO_USER: "🎭"
    };

    function inviteUrl(token) {
        return `${data.origin}/join/${token}`;
    }

    async function copy(token) {
        try {
            await navigator.clipboard.writeText(inviteUrl(token));
            copiedToken = token;
            setTimeout(() => (copiedToken = null), 1500);
        } catch {
            copiedToken = null;
        }
    }

    function formatDate(d) {
        if (!d) return "–";
        return new Date(d).toLocaleDateString("de-CH", { day: "2-digit", month: "2-digit", year: "numeric" });
    }

    function handleCreate() {
        return async ({ result, update }) => {
            if (result.type === "success" && result.data?.invite) {
                invites = [result.data.invite, ...invites];
            }
            await update({ reset: false });
        };
    }

    function handleDelete(id) {
        return async ({ result, update }) => {
            if (result.type === "success") {
                invites = invites.filter((i) => i.id !== id);
            }
            await update({ reset: false });
        };
    }
</script>

<svelte:head>
    <title>{$t('adash.headTitle')}</title>
</svelte:head>

<div class="page">
    <header class="page-head">
        <div>
            <h1>{$t('adash.title')}</h1>
            <p class="sub">{$t('adash.sub')}</p>
        </div>
        {#if data.isDemo}
            <span class="demo-badge">{$t('adash.demoBadge')}</span>
        {/if}
    </header>

    <section class="stats">
        <div class="stat"><span class="num">{data.stats.teachers}</span><span class="lbl">{$t('adash.statTeachers')}</span></div>
        <div class="stat"><span class="num">{data.stats.students}</span><span class="lbl">{$t('adash.statStudents')}</span></div>
        <div class="stat"><span class="num">{data.stats.classes}</span><span class="lbl">{$t('adash.statClasses')}</span></div>
        <div class="stat"><span class="num">{data.stats.assignments}</span><span class="lbl">{$t('adash.statAssignments')}</span></div>
    </section>

    {#if form?.error}
        <div class="alert">{form.error}</div>
    {/if}

    {#if insights}
        <section class="card attention">
            <h2>{$t('adash.attentionTitle')}</h2>
            <div class="attention-grid">
                <div class="att-metric">
                    <span class="att-num">{insights.totalSessions}</span>
                    <span class="att-lbl">{$t('adash.attTrainings')}</span>
                </div>
                <div class="att-metric">
                    <span class="att-num">{insights.totalSubmissions}</span>
                    <span class="att-lbl">{$t('adash.attSubmissions')}</span>
                </div>
                <div class="att-metric" class:warn={insights.classesWithoutAssignments > 0}>
                    <span class="att-num">{insights.classesWithoutAssignments}</span>
                    <span class="att-lbl">{$t('adash.attClassesNoTasks')}</span>
                </div>
                <div class="att-metric" class:warn={insights.inactiveUsers > 0}>
                    <span class="att-num">{insights.inactiveUsers}</span>
                    <span class="att-lbl">{$t('adash.attInactive')}</span>
                </div>
            </div>

            {#if insights.recentUsers && insights.recentUsers.length > 0}
                <h3 class="att-sub">{$t('adash.recentTitle')}</h3>
                <ul class="recent-list">
                    {#each insights.recentUsers as u}
                        <li>
                            <span class="recent-role">{roleLabels[u.role] ?? "•"}</span>
                            <span class="recent-name">{u.firstName} {u.lastName}</span>
                            <span class="recent-email">{u.email}</span>
                        </li>
                    {/each}
                </ul>
            {/if}
        </section>
    {/if}

    <section class="card">
        <div class="card-head">
            <h2>{$t('adash.invitesTitle')}</h2>
            {#if !data.isDemo}
                <form method="POST" action="?/createTeacherInvite" use:enhance={handleCreate}>
                    <button class="btn-primary" type="submit">{$t('adash.inviteTeacher')}</button>
                </form>
            {/if}
        </div>

        {#if invites.length === 0}
            <p class="empty">{$t('adash.invitesEmpty')}</p>
        {:else}
            <ul class="invite-list">
                {#each invites as invite (invite.id)}
                    <li class="invite">
                        <div class="invite-info">
                            <span class="invite-type">{invite.type}</span>
                            <code class="invite-url">{inviteUrl(invite.token)}</code>
                            <span class="invite-exp">{$t('adash.validUntil')} {formatDate(invite.expiresAt)}</span>
                        </div>
                        <div class="invite-actions">
                            <button class="btn-ghost" type="button" onclick={() => copy(invite.token)}>
                                {copiedToken === invite.token ? $t('adash.copied') : $t('adash.copy')}
                            </button>
                            {#if !data.isDemo}
                                <form method="POST" action="?/deleteInvite" use:enhance={handleDelete(invite.id)}>
                                    <input type="hidden" name="id" value={invite.id} />
                                    <button class="btn-danger" type="submit">{$t('adash.deactivate')}</button>
                                </form>
                            {/if}
                        </div>
                    </li>
                {/each}
            </ul>
        {/if}
    </section>

    <div class="two-col">
        <section class="card">
            <h2>{$t('adash.teachers')} ({data.teachers.length})</h2>
            <ul class="people">
                {#each data.teachers as teacher}
                    <li>
                        <span>{teacher.firstName} {teacher.lastName}</span>
                        <span class="muted">{teacher.email}</span>
                        {#if !teacher.active}<span class="inactive">{$t('adash.inactive')}</span>{/if}
                    </li>
                {:else}
                    <li class="empty">{$t('adash.noTeachers')}</li>
                {/each}
            </ul>
        </section>

        <section class="card">
            <h2>{$t('adash.students')} ({data.students.length})</h2>
            <ul class="people">
                {#each data.students as s}
                    <li>
                        <span>{s.firstName} {s.lastName}</span>
                        <span class="muted">{s.email}</span>
                        {#if !s.active}<span class="inactive">{$t('adash.inactive')}</span>{/if}
                    </li>
                {:else}
                    <li class="empty">{$t('adash.noStudents')}</li>
                {/each}
            </ul>
        </section>
    </div>
</div>

<style>
    .page { max-width: 1000px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    .page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    h1 { margin: 0; color: #2F124D; font-size: 1.6rem; }
    .sub { margin: 0.25rem 0 0; color: #6b647a; }
    .demo-badge { background: #fffbeb; border: 1px solid #fde68a; color: #78600e; padding: 0.4rem 0.8rem; border-radius: 999px; font-size: 0.85rem; }

    .stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 1.5rem; }
    .stat { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1.25rem; text-align: center; }
    .num { display: block; font-size: 1.8rem; font-weight: 700; color: #2F124D; }
    .lbl { color: #6b647a; font-size: 0.85rem; }

    /* Attention / Insights */
    .attention h2 { margin: 0 0 1rem; font-size: 1.05rem; color: #2d2141; }
    .attention-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 0.75rem; }
    .att-metric { background: #faf8fc; border: 1px solid #eee5f5; border-radius: 0.75rem; padding: 0.85rem; text-align: center; }
    .att-metric.warn { background: #fffbeb; border-color: #fcd34d; }
    .att-num { display: block; font-size: 1.5rem; font-weight: 700; color: #2F124D; }
    .att-metric.warn .att-num { color: #92400e; }
    .att-lbl { font-size: 0.78rem; color: #6b647a; }
    .att-sub { margin: 1.25rem 0 0.6rem; font-size: 0.95rem; color: #2d2141; }
    .recent-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 0.35rem; }
    .recent-list li { display: flex; align-items: center; gap: 0.5rem; padding: 0.45rem 0.6rem; background: #faf8fc; border-radius: 0.5rem; font-size: 0.88rem; }
    .recent-name { font-weight: 600; color: #2d2141; }
    .recent-email { color: #9a8b9d; margin-left: auto; font-size: 0.8rem; }

    .card { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1.25rem; margin-bottom: 1.5rem; }
    .card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .card h2 { margin: 0 0 0.75rem; font-size: 1.05rem; color: #2d2141; }

    .btn-primary { background: #2F124D; color: #fff; border: none; border-radius: 0.5rem; padding: 0.5rem 1rem; font-weight: 600; cursor: pointer; }
    .btn-ghost { background: #faf8fc; border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.4rem 0.8rem; cursor: pointer; }
    .btn-danger { background: #fef2f2; border: 1px solid #fecaca; color: #dc2626; border-radius: 0.5rem; padding: 0.4rem 0.8rem; cursor: pointer; }

    .invite-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 0.6rem; }
    .invite { display: flex; justify-content: space-between; align-items: center; gap: 1rem; background: #faf8fc; border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.75rem 1rem; flex-wrap: wrap; }
    .invite-info { display: flex; flex-direction: column; gap: 0.2rem; min-width: 0; }
    .invite-type { font-size: 0.7rem; font-weight: 700; color: #c97d3c; }
    .invite-url { font-size: 0.8rem; color: #2d2141; word-break: break-all; }
    .invite-exp { font-size: 0.75rem; color: #6b647a; }
    .invite-actions { display: flex; gap: 0.5rem; }

    .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
    .people { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 0.4rem; max-height: 360px; overflow: auto; }
    .people li { display: flex; gap: 0.5rem; align-items: center; padding: 0.5rem 0.6rem; border-bottom: 1px solid #f0ebf5; font-size: 0.9rem; }
    .muted { color: #9a8b9d; font-size: 0.8rem; margin-left: auto; }
    .inactive { background: #fef2f2; color: #dc2626; border-radius: 999px; padding: 0.1rem 0.5rem; font-size: 0.7rem; }
    .empty { color: #9a8b9d; font-size: 0.9rem; }
    .alert { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; border-radius: 0.5rem; padding: 0.75rem 1rem; margin-bottom: 1rem; }

    @media (max-width: 800px) {
        .stats { grid-template-columns: repeat(2, 1fr); }
        .attention-grid { grid-template-columns: repeat(2, 1fr); }
        .two-col { grid-template-columns: 1fr; }
    }
    @media (max-width: 480px) {
        .attention-grid { grid-template-columns: 1fr; }
    }
</style>
