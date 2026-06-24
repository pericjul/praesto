<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";

    let { data } = $props();

    let inquiries = $derived(data.inquiries ?? []);

    function formatDate(d) {
        if (!d) return "";
        return new Date(d).toLocaleDateString("de-CH", {
            day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit"
        });
    }

    function handleToggle() {
        return async ({ result, update }) => {
            if (result.type === "success") {
                await invalidateAll();
            }
            await update({ reset: false });
        };
    }
</script>

<svelte:head>
    <title>{$t('nav.inquiries')} – Praesto</title>
</svelte:head>

<div class="page">
    <h1>{$t('nav.inquiries')}</h1>

    {#if inquiries.length === 0}
        <p class="empty">{$t('inq.empty')}</p>
    {:else}
        <div class="inquiry-list">
            {#each inquiries as i (i.id)}
                <article class="inquiry" class:handled={i.handled}>
                    <div class="inq-head">
                        <div>
                            <h3>{i.organisation || i.name}</h3>
                            <span class="inq-date">{formatDate(i.createdAt)}</span>
                        </div>
                        <span class="status" class:done={i.handled}>
                            {i.handled ? $t('inq.statusDone') : $t('inq.statusNew')}
                        </span>
                    </div>

                    <div class="inq-grid">
                        <div><span class="k">{$t('contact.name')}</span><span class="v">{i.name}</span></div>
                        <div><span class="k">{$t('contact.email')}</span><span class="v"><a href={`mailto:${i.email}`}>{i.email}</a></span></div>
                        {#if i.role}<div><span class="k">{$t('contact.role')}</span><span class="v">{i.role}</span></div>{/if}
                        {#if i.interest}<div><span class="k">{$t('contact.interest')}</span><span class="v">{i.interest}</span></div>{/if}
                        {#if i.classes != null}<div><span class="k">{$t('contact.classes')}</span><span class="v">{i.classes}</span></div>{/if}
                        {#if i.students != null}<div><span class="k">{$t('contact.students')}</span><span class="v">{i.students}</span></div>{/if}
                    </div>

                    {#if i.wantsMeeting}
                        <p class="meeting">{$t('inq.meetingYes')}</p>
                    {/if}

                    {#if i.message}
                        <p class="message">{i.message}</p>
                    {/if}

                    <form method="POST" action="?/toggleHandled" use:enhance={handleToggle}>
                        <input type="hidden" name="id" value={i.id} />
                        <input type="hidden" name="handled" value={(!i.handled).toString()} />
                        <button type="submit" class="btn-toggle">
                            {i.handled ? $t('inq.markOpen') : $t('inq.markDone')}
                        </button>
                    </form>
                </article>
            {/each}
        </div>
    {/if}
</div>

<style>
    .page { max-width: 900px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    h1 { margin: 0 0 1.25rem; color: #2F124D; font-size: 1.6rem; }
    .empty { color: #9a8b9d; }

    .inquiry-list { display: flex; flex-direction: column; gap: 1rem; }

    .inquiry {
        background: #fff;
        border: 1px solid #e8e0f0;
        border-radius: 1rem;
        padding: 1.25rem;
    }

    .inquiry.handled { opacity: 0.7; }

    .inq-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; margin-bottom: 0.85rem; }
    .inq-head h3 { margin: 0; color: #2F124D; font-size: 1.1rem; }
    .inq-date { font-size: 0.8rem; color: #9a8b9d; }

    .status { font-size: 0.75rem; font-weight: 600; padding: 0.2rem 0.6rem; border-radius: 999px; background: #fef3c7; color: #92400e; white-space: nowrap; }
    .status.done { background: #f0fdf4; color: #16a34a; }

    .inq-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 0.5rem 1rem; margin-bottom: 0.75rem; }
    .inq-grid .k { display: block; font-size: 0.72rem; color: #9a8b9d; text-transform: uppercase; letter-spacing: 0.03em; }
    .inq-grid .v { font-size: 0.92rem; color: #2d2141; }
    .inq-grid a { color: #2F124D; }

    .meeting { margin: 0 0 0.5rem; font-size: 0.9rem; color: #c97d3c; font-weight: 600; }
    .message { margin: 0 0 0.85rem; padding: 0.75rem; background: #faf8fc; border-radius: 0.5rem; color: #374151; white-space: pre-wrap; font-size: 0.92rem; }

    .btn-toggle {
        background: #faf8fc;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        padding: 0.45rem 0.9rem;
        cursor: pointer;
        font-size: 0.85rem;
        color: #2d2141;
    }

    .btn-toggle:hover { background: #f0ebf5; }
</style>
