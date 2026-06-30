<script>
    // Wiederverwendbares Mehrzeilen-Feld: pro Eintrag eine eigene Zeile mit
    // "+ Zeile hinzufügen" und ✕ zum Entfernen. Statt selber Enter zu drücken.
    // Beim Absenden werden die Zeilen mit \n zusammengefügt (verstecktes Input) –
    // das Backend erwartet weiterhin "eine Zeile = ein Eintrag".
    let {
        name,
        label,
        hint = "",
        placeholder = "",
        addLabel = "Zeile hinzufügen",
        value = ""
    } = $props();

    let uid = 0;
    const init = (value ?? "")
        .split(/\r?\n/)
        .map((s) => s.trim())
        .filter(Boolean);
    let rows = $state((init.length ? init : [""]).map((text) => ({ id: uid++, text })));

    let joined = $derived(
        rows
            .map((r) => r.text.trim())
            .filter(Boolean)
            .join("\n")
    );

    function add() {
        rows = [...rows, { id: uid++, text: "" }];
    }
    function remove(id) {
        rows = rows.filter((r) => r.id !== id);
        if (!rows.length) rows = [{ id: uid++, text: "" }];
    }
</script>

<div class="rep">
    <div class="rep-head">
        <span class="lbl">{label}</span>
        {#if hint}<em>{hint}</em>{/if}
    </div>

    {#each rows as row (row.id)}
        <div class="rep-row">
            <input type="text" bind:value={row.text} {placeholder} />
            <button
                type="button"
                class="rm"
                onclick={() => remove(row.id)}
                title="Eintrag entfernen"
                aria-label="Eintrag entfernen">✕</button
            >
        </div>
    {/each}

    <button type="button" class="add" onclick={add}>＋ {addLabel}</button>
    <input type="hidden" {name} value={joined} />
</div>

<style>
    .rep {
        display: grid;
        gap: 0.4rem;
    }
    .rep-head {
        display: flex;
        align-items: baseline;
        gap: 0.4rem;
    }
    .lbl {
        font-size: 0.85rem;
        font-weight: 600;
        color: #2d2141;
    }
    .rep-head em {
        color: #9a8baf;
        font-weight: 400;
        font-style: normal;
        font-size: 0.85rem;
    }
    .rep-row {
        display: flex;
        gap: 0.4rem;
        align-items: center;
    }
    .rep-row input {
        flex: 1 1 auto;
        min-width: 0;
        border: 1px solid #e8e0f0;
        border-radius: 0.6rem;
        padding: 0.6rem 0.75rem;
        font: inherit;
        box-sizing: border-box;
    }
    .rep-row input:focus {
        outline: 2px solid #2f124d;
        outline-offset: 1px;
    }
    .rm {
        flex: 0 0 auto;
        width: 2.2rem;
        height: 2.2rem;
        border: 1px solid #e8e0f0;
        background: #fff;
        color: #9a8baf;
        border-radius: 0.6rem;
        cursor: pointer;
        font-size: 0.9rem;
        line-height: 1;
    }
    .rm:hover {
        background: #fdecec;
        color: #c0392b;
        border-color: #f3c6c6;
    }
    .add {
        justify-self: start;
        margin-top: 0.1rem;
        background: #f3eefa;
        color: #2f124d;
        border: 1px dashed #cdbce4;
        border-radius: 999px;
        padding: 0.45rem 1rem;
        font-weight: 600;
        font-size: 0.88rem;
        cursor: pointer;
    }
    .add:hover {
        background: #ece1f7;
    }
</style>
