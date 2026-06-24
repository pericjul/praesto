<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    let results = $derived(data.results ?? []);
    let confirmId = $state(null);

    const roleIcons = {
        TEACHER: "👩‍🏫",
        STUDENT: "🎓",
        SCHOOL_ADMIN: "🏫",
        DEMO_USER: "🎭",
        SUPER_ADMIN: "👑"
    };

    function handleDelete() {
        return async ({ result, update }) => {
            confirmId = null;
            if (result.type === "success") {
                await invalidateAll();
            }
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

    {#if form?.deleted}
        <div class="alert success">✓ {$t('su.deleted')}</div>
    {/if}
    {#if form?.error}
        <div class="alert error">⚠️ {$t('contact.error')}</div>
    {/if}

    <form method="GET" class="search-bar">
        <input type="search" name="q" value={data.q} placeholder={$t('su.searchPlaceholder')} />
        <button type="submit" class="btn-primary">{$t('su.search')}</button>
    </form>

    {#if data.searched && results.length === 0}
        <p class="empty">{$t('su.noResults')}</p>
    {:else if results.length > 0}
        <ul class="user-list">
            {#each results as u (u.id)}
                <li class="user-row">
                    <div class="user-info">
                        <span class="user-role">{roleIcons[u.role] ?? "•"}</span>
                        <div>
                            <span class="user-name">{u.firstName} {u.lastName}</span>
                            <span class="user-email">{u.email}</span>
                        </div>
                    </div>

                    {#if confirmId === u.id}
                        <div class="confirm">
                            <span class="confirm-text">{$t('su.confirmDelete')}</span>
                            <form method="POST" action="?/delete" use:enhance={handleDelete}>
                                <input type="hidden" name="id" value={u.id} />
                                <button type="submit" class="btn-danger">{$t('su.confirmYes')}</button>
                            </form>
                            <button type="button" class="btn-ghost" onclick={() => (confirmId = null)}>{$t('su.cancel')}</button>
                        </div>
                    {:else}
                        <div class="user-actions">
                            <a href={`/super/users/${u.id}/export`} class="btn-ghost" download>{$t('su.export')}</a>
                            {#if u.role !== "SUPER_ADMIN"}
                                <button type="button" class="btn-danger" onclick={() => (confirmId = u.id)}>{$t('su.delete')}</button>
                            {/if}
                        </div>
                    {/if}
                </li>
            {/each}
        </ul>
    {/if}

    <p class="warn">{$t('su.warn')}</p>
</div>

<style>
    .page { max-width: 820px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    h1 { margin: 0 0 0.4rem; color: #2F124D; font-size: 1.6rem; }
    .lead { margin: 0 0 1.5rem; color: #6b647a; }

    .search-bar { display: flex; gap: 0.6rem; margin-bottom: 1.5rem; }
    .search-bar input { flex: 1; padding: 0.65rem 0.9rem; border: 1px solid #e8e0f0; border-radius: 0.5rem; background: #faf8fc; }

    .btn-primary { background: #2F124D; color: #fff; border: none; border-radius: 0.5rem; padding: 0.55rem 1.1rem; font-weight: 600; cursor: pointer; }
    .btn-ghost { display: inline-block; background: #faf8fc; border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.45rem 0.85rem; cursor: pointer; text-decoration: none; color: #2d2141; font-size: 0.9rem; }
    .btn-danger { background: #fef2f2; border: 1px solid #fecaca; color: #dc2626; border-radius: 0.5rem; padding: 0.45rem 0.85rem; cursor: pointer; font-size: 0.9rem; }

    .user-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 0.6rem; }
    .user-row { display: flex; justify-content: space-between; align-items: center; gap: 1rem; background: #fff; border: 1px solid #e8e0f0; border-radius: 0.75rem; padding: 0.85rem 1rem; flex-wrap: wrap; }
    .user-info { display: flex; align-items: center; gap: 0.7rem; }
    .user-role { font-size: 1.3rem; }
    .user-name { display: block; font-weight: 600; color: #2d2141; }
    .user-email { display: block; font-size: 0.85rem; color: #9a8b9d; }
    .user-actions { display: flex; gap: 0.5rem; }

    .confirm { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
    .confirm-text { font-size: 0.85rem; color: #dc2626; }

    .alert { padding: 0.75rem 1rem; border-radius: 0.5rem; margin-bottom: 1rem; }
    .alert.success { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }
    .alert.error { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }

    .empty { color: #9a8b9d; }
    .warn { margin-top: 1.5rem; font-size: 0.85rem; color: #92400e; background: #fffbeb; border: 1px solid #fcd34d; border-radius: 0.5rem; padding: 0.6rem 0.85rem; }
</style>
