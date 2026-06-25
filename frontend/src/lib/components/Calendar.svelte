<script>
    import { locale, t } from "$lib/i18n";

    let { events = [] } = $props();

    function startOfMonth(d) { return new Date(d.getFullYear(), d.getMonth(), 1); }
    function key(d) {
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
    }

    let view = $state(startOfMonth(new Date()));
    const todayKey = key(new Date());

    let byDay = $derived.by(() => {
        const m = {};
        for (const e of events) {
            const k = key(new Date(e.date));
            (m[k] ??= []).push(e);
        }
        return m;
    });

    let cells = $derived.by(() => {
        const year = view.getFullYear();
        const month = view.getMonth();
        const mondayOffset = (new Date(year, month, 1).getDay() + 6) % 7;
        const start = new Date(year, month, 1 - mondayOffset);
        return Array.from({ length: 42 }, (_, i) =>
            new Date(start.getFullYear(), start.getMonth(), start.getDate() + i));
    });

    let monthLabel = $derived(view.toLocaleDateString($locale, { month: "long", year: "numeric" }));

    let weekdays = $derived.by(() =>
        Array.from({ length: 7 }, (_, i) =>
            new Date(2024, 0, 1 + i).toLocaleDateString($locale, { weekday: "short" })));

    let upcoming = $derived([...events]
        .filter((e) => new Date(e.date) >= new Date(new Date().toDateString()))
        .sort((a, b) => new Date(a.date) - new Date(b.date))
        .slice(0, 6));

    function prev() { view = new Date(view.getFullYear(), view.getMonth() - 1, 1); }
    function next() { view = new Date(view.getFullYear(), view.getMonth() + 1, 1); }
    function inMonth(d) { return d.getMonth() === view.getMonth(); }
    function dayEvents(d) { return byDay[key(d)] ?? []; }
    function shortDate(e) { return new Date(e.date).toLocaleDateString($locale, { day: "2-digit", month: "2-digit" }); }
    function icon(type) { return type === "INTERVIEW" ? "💼" : "📝"; }
</script>

<div class="cal">
    <div class="cal-head">
        <h2>📅 {$t('calendar.title')}</h2>
        <div class="nav">
            <button type="button" onclick={prev} aria-label="prev">‹</button>
            <span class="month">{monthLabel}</span>
            <button type="button" onclick={next} aria-label="next">›</button>
        </div>
    </div>

    <div class="grid weekdays">
        {#each weekdays as wd}<div class="wd">{wd}</div>{/each}
    </div>
    <div class="grid days">
        {#each cells as d (d.toISOString())}
            <div class="cell" class:out={!inMonth(d)} class:today={key(d) === todayKey}>
                <span class="num">{d.getDate()}</span>
                {#each dayEvents(d) as e}
                    <a class="ev ev-{e.type}" href={e.href} title={e.title}>{icon(e.type)} {e.title}</a>
                {/each}
            </div>
        {/each}
    </div>

    <div class="upcoming">
        <h3>{$t('calendar.upcoming')}</h3>
        {#if upcoming.length === 0}
            <p class="none">{$t('calendar.none')}</p>
        {:else}
            <ul>
                {#each upcoming as e (e.href + e.date)}
                    <li>
                        <a href={e.href}>
                            <span class="u-date">{shortDate(e)}</span>
                            <span class="u-ev ev-{e.type}">{icon(e.type)} {e.title}</span>
                        </a>
                    </li>
                {/each}
            </ul>
        {/if}
    </div>
</div>

<style>
    .cal { background: var(--color-bg-card, #fff); border: 1px solid var(--color-border, #e6d9cc); border-radius: 1rem; padding: 1.1rem 1.25rem; }
    .cal-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.85rem; gap: 1rem; flex-wrap: wrap; }
    .cal-head h2 { margin: 0; font-size: 1.2rem; color: var(--color-primary, #2F124D); }
    .nav { display: flex; align-items: center; gap: 0.5rem; }
    .nav button { background: #f3f0f7; border: none; border-radius: 0.5rem; width: 2rem; height: 2rem; cursor: pointer; font-size: 1.1rem; color: #2F124D; }
    .nav button:hover { background: #e7def0; }
    .month { font-weight: 600; color: #2d2141; min-width: 9rem; text-align: center; text-transform: capitalize; }

    .grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; }
    .weekdays { margin-bottom: 4px; }
    .wd { text-align: center; font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.03em; color: #9a8b9d; padding: 0.2rem 0; }

    .cell { min-height: 84px; background: #faf8fc; border-radius: 0.5rem; padding: 0.25rem; display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
    .cell.out { opacity: 0.4; }
    .cell.today { outline: 2px solid #2F124D; }
    .num { font-size: 0.75rem; color: #6b647a; }

    .ev { display: block; font-size: 0.68rem; line-height: 1.25; padding: 2px 4px; border-radius: 4px; text-decoration: none; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .ev-ASSIGNMENT { background: #ede9fe; color: #5b21b6; }
    .ev-INTERVIEW { background: #fef3c7; color: #92400e; }
    .ev:hover { filter: brightness(0.95); }

    .upcoming { margin-top: 1rem; border-top: 1px solid var(--color-border-light, #f0ebf5); padding-top: 0.75rem; }
    .upcoming h3 { margin: 0 0 0.5rem; font-size: 0.95rem; color: #2d2141; }
    .upcoming ul { list-style: none; margin: 0; padding: 0; display: grid; gap: 0.35rem; }
    .upcoming a { display: flex; align-items: center; gap: 0.6rem; text-decoration: none; }
    .u-date { font-size: 0.8rem; font-weight: 700; color: #2F124D; min-width: 3rem; }
    .u-ev { font-size: 0.85rem; padding: 0.15rem 0.5rem; border-radius: 999px; }
    .none { color: #9a8b9d; margin: 0; font-size: 0.9rem; }

    @media (max-width: 560px) {
        .cell { min-height: 60px; }
        .ev { font-size: 0.6rem; }
    }
</style>
