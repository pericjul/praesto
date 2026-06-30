<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";

    let { data } = $props();

    let requests = $derived(data.requests ?? []);
    let copiedId = $state(null);

    function formatDateTime(d) {
        if (!d) return "";
        return new Date(d).toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit"
        });
    }

    function formatDate(d) {
        if (!d) return "";
        return new Date(d + "T00:00:00").toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", year: "numeric"
        });
    }

    function statusLabel(status) {
        return {
            NEW: $t("dreq.sNew"),
            APPROVED: $t("dreq.sApproved"),
            DONE: $t("dreq.sDone"),
            REJECTED: $t("dreq.sRejected")
        }[status] ?? status;
    }

    function refresh() {
        return async ({ result, update }) => {
            if (result.type === "success") {
                await invalidateAll();
            }
            await update({ reset: false });
        };
    }

    async function copy(url, id) {
        try {
            await navigator.clipboard.writeText(url);
            copiedId = id;
            setTimeout(() => { if (copiedId === id) copiedId = null; }, 2000);
        } catch (e) {
            // ignore
        }
    }
</script>

<svelte:head>
    <title>{$t('dreq.title')} – Praesto</title>
</svelte:head>

<div class="page">
    <h1>{$t('dreq.title')}</h1>
    <p class="subtitle">{$t('dreq.subtitle')}</p>

    {#if requests.length === 0}
        <p class="empty">{$t('dreq.empty')}</p>
    {:else}
        <div class="list">
            {#each requests as r (r.id)}
                <article class="card" class:muted={r.status === 'DONE' || r.status === 'REJECTED'}>
                    <div class="head">
                        <div>
                            <h3>{r.schoolName}</h3>
                            <span class="date">{formatDateTime(r.createdAt)}</span>
                        </div>
                        <span class="status s-{r.status}">{statusLabel(r.status)}</span>
                    </div>

                    <div class="grid">
                        {#if r.contactName}<div><span class="k">{$t('dreq.contact')}</span><span class="v">{r.contactName}</span></div>{/if}
                        <div><span class="k">{$t('dreq.email')}</span><span class="v"><a href={`mailto:${r.email}`}>{r.email}</a></span></div>
                        <div><span class="k">{$t('dreq.preferredDate')}</span><span class="v">{r.preferredDate ? formatDate(r.preferredDate) : $t('dreq.noDate')}</span></div>
                    </div>

                    {#if r.message}<p class="message">{r.message}</p>{/if}

                    {#if r.status === 'APPROVED' && r.inviteUrl}
                        <div class="approved">
                            <p class="approved-for">✅ {$t('dreq.approvedFor')} <strong>{formatDate(r.approvedDate)}</strong></p>
                            <span class="link-label">{$t('dreq.link')}</span>
                            <div class="link-row">
                                <code>{r.inviteUrl}</code>
                                <button type="button" class="btn-copy" onclick={() => copy(r.inviteUrl, r.id)}>
                                    {copiedId === r.id ? $t('dreq.copied') : $t('dreq.copy')}
                                </button>
                            </div>
                        </div>
                    {/if}

                    {#if r.status === 'NEW'}
                        <form method="POST" action="?/approve" use:enhance={refresh} class="approve-form">
                            <input type="hidden" name="id" value={r.id} />
                            <label class="day">
                                <span>{$t('dreq.dateLabel')}</span>
                                <input type="date" name="date" value={r.preferredDate ?? ''} required />
                            </label>
                            <button type="submit" class="btn-approve">{$t('dreq.approve')}</button>
                        </form>
                        <p class="hint">{$t('dreq.approveHint')}</p>
                        <form method="POST" action="?/setStatus" use:enhance={refresh} class="reject-form">
                            <input type="hidden" name="id" value={r.id} />
                            <input type="hidden" name="status" value="REJECTED" />
                            <button type="submit" class="btn-reject">{$t('dreq.reject')}</button>
                        </form>
                    {:else if r.status === 'REJECTED'}
                        <form method="POST" action="?/setStatus" use:enhance={refresh}>
                            <input type="hidden" name="id" value={r.id} />
                            <input type="hidden" name="status" value="NEW" />
                            <button type="submit" class="btn-reopen">{$t('dreq.reopen')}</button>
                        </form>
                    {/if}
                </article>
            {/each}
        </div>
    {/if}
</div>

<style>
    .page { max-width: 900px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    h1 { margin: 0 0 0.4rem; color: #2F124D; font-size: 1.6rem; }
    .subtitle { margin: 0 0 1.25rem; color: #6b647a; line-height: 1.55; }
    .empty { color: #9a8b9d; }

    .list { display: flex; flex-direction: column; gap: 1rem; }

    .card { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1.25rem; }
    .card.muted { opacity: 0.7; }

    .head { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; margin-bottom: 0.85rem; }
    .head h3 { margin: 0; color: #2F124D; font-size: 1.1rem; }
    .date { font-size: 0.8rem; color: #9a8b9d; }

    .status { font-size: 0.75rem; font-weight: 600; padding: 0.2rem 0.6rem; border-radius: 999px; white-space: nowrap; }
    .status.s-NEW { background: #fef3c7; color: #92400e; }
    .status.s-APPROVED { background: #dbeafe; color: #1e40af; }
    .status.s-DONE { background: #f0fdf4; color: #16a34a; }
    .status.s-REJECTED { background: #fee2e2; color: #b91c1c; }

    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 0.5rem 1rem; margin-bottom: 0.75rem; }
    .grid .k { display: block; font-size: 0.72rem; color: #9a8b9d; text-transform: uppercase; letter-spacing: 0.03em; }
    .grid .v { font-size: 0.92rem; color: #2d2141; overflow-wrap: anywhere; }
    .grid a { color: #2F124D; overflow-wrap: anywhere; word-break: break-word; }

    .message { margin: 0 0 0.85rem; padding: 0.75rem; background: #faf8fc; border-radius: 0.5rem; color: #374151; white-space: pre-wrap; font-size: 0.92rem; }

    .approved { background: #f5f8ff; border: 1px solid #dbe4ff; border-radius: 0.6rem; padding: 0.85rem 1rem; }
    .approved-for { margin: 0 0 0.5rem; color: #1e40af; font-size: 0.92rem; }
    .link-label { display: block; font-size: 0.72rem; color: #9a8b9d; text-transform: uppercase; letter-spacing: 0.03em; margin-bottom: 0.3rem; }
    .link-row { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
    .link-row code { flex: 1 1 150px; min-width: 0; background: #fff; border: 1px solid #e8e0f0; border-radius: 0.4rem; padding: 0.45rem 0.6rem; font-size: 0.82rem; color: #2d2141; overflow-wrap: anywhere; }

    .approve-form { display: flex; gap: 0.75rem; align-items: flex-end; flex-wrap: wrap; }
    .day { display: grid; gap: 0.25rem; }
    .day span { font-size: 0.78rem; color: #6b647a; }
    .day input { border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.5rem 0.6rem; font: inherit; }

    .hint { margin: 0.5rem 0 0.75rem; font-size: 0.82rem; color: #9a8b9d; }

    .btn-approve { background: #2F124D; color: #fff; border: none; border-radius: 0.5rem; padding: 0.55rem 1.1rem; cursor: pointer; font-size: 0.9rem; font-weight: 600; }
    .btn-approve:hover { background: #41205f; }

    .btn-copy { background: #2F124D; color: #fff; border: none; border-radius: 0.4rem; padding: 0.45rem 0.9rem; cursor: pointer; font-size: 0.82rem; white-space: nowrap; }
    .btn-copy:hover { background: #41205f; }

    .btn-reject, .btn-reopen { background: #faf8fc; border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.45rem 0.9rem; cursor: pointer; font-size: 0.85rem; color: #2d2141; }
    .btn-reject:hover, .btn-reopen:hover { background: #f0ebf5; }
    .reject-form { display: inline; }
</style>
