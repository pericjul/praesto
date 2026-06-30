<script>
    import { invalidateAll } from "$app/navigation";

    let { data } = $props();

    let logs = $derived(data?.logs ?? []);
    let filter = $state("");
    let onlyErrors = $state(false);
    let refreshing = $state(false);

    let shown = $derived(
        logs.filter((l) => {
            if (onlyErrors && !/ERROR| WARN/.test(l)) return false;
            if (filter.trim() && !l.toLowerCase().includes(filter.toLowerCase())) return false;
            return true;
        })
    );

    function lineClass(l) {
        if (l.includes("ERROR")) return "log-error";
        if (l.includes("WARN")) return "log-warn";
        return "";
    }

    async function refresh() {
        refreshing = true;
        await invalidateAll();
        refreshing = false;
    }
</script>

<svelte:head><title>Server-Logs · Praesto</title></svelte:head>

<div class="logs-page">
    <header class="logs-header">
        <div>
            <h1>📜 Server-Logs</h1>
            <p class="sub">Die letzten {logs.length} Log-Zeilen (neueste zuerst). Nur für Super-Admin.</p>
        </div>
        <button class="btn-refresh" onclick={refresh} disabled={refreshing}>
            {refreshing ? "…" : "↻ Aktualisieren"}
        </button>
    </header>

    {#if data?.error}
        <div class="err-banner">{data.error}</div>
    {/if}

    <div class="logs-controls">
        <input type="search" placeholder="Filtern (z.B. KI-Fehler, 400, gpt)…" bind:value={filter} />
        <label class="chk"><input type="checkbox" bind:checked={onlyErrors} /> nur Fehler/Warnungen</label>
        <span class="count">{shown.length} angezeigt</span>
    </div>

    <div class="logs-box">
        {#if shown.length === 0}
            <p class="empty">Keine passenden Log-Zeilen.</p>
        {:else}
            {#each shown as line}
                <div class="log-line {lineClass(line)}">{line}</div>
            {/each}
        {/if}
    </div>
</div>

<style>
    .logs-page { max-width: 1100px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    .logs-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; flex-wrap: wrap; margin-bottom: 1rem; }
    .logs-header h1 { margin: 0; font-size: 1.4rem; color: var(--color-primary, #2F124D); }
    .sub { margin: 0.25rem 0 0; color: var(--color-text-muted, #5E4C6F); font-size: 0.9rem; }
    .btn-refresh { background: var(--color-primary, #2F124D); color: #fff; border: none; border-radius: 999px; padding: 0.5rem 1.1rem; font-weight: 600; cursor: pointer; white-space: nowrap; }
    .btn-refresh:disabled { opacity: 0.6; cursor: default; }
    .err-banner { background: #fef2f2; border: 1px solid #fca5a5; color: #b91c1c; border-radius: 0.6rem; padding: 0.6rem 0.9rem; margin-bottom: 1rem; }
    .logs-controls { display: flex; align-items: center; gap: 0.9rem; flex-wrap: wrap; margin-bottom: 0.75rem; }
    .logs-controls input[type="search"] { flex: 1; min-width: 140px; padding: 0.5rem 0.7rem; border: 1px solid #e0d6eb; border-radius: 0.5rem; font-size: 0.9rem; }
    @media (max-width: 480px) { .logs-controls input[type="search"] { min-width: 0; flex: 1 1 100%; } }
    .chk { font-size: 0.85rem; color: #5E4C6F; display: inline-flex; align-items: center; gap: 0.35rem; white-space: nowrap; }
    .count { font-size: 0.8rem; color: #9a8b9d; }
    .logs-box { background: #1e1530; color: #e7e2f0; border-radius: 0.75rem; padding: 0.75rem; max-height: 70vh; overflow: auto; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 0.78rem; line-height: 1.5; }
    .log-line { white-space: pre-wrap; word-break: break-word; padding: 1px 0; border-bottom: 1px solid rgba(255,255,255,0.05); }
    .log-error { color: #ff9a9a; }
    .log-warn { color: #ffd479; }
    .empty { color: #b9aecb; margin: 0; padding: 1rem; }
</style>
