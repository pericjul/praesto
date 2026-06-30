<script>
    // Offerten-Generator (nur Super-Admin). Felder ausfüllen → druckbare Offerte,
    // die als PDF gespeichert/gesendet werden kann. Preise pro Anfrage frei eintragbar.
    let school = $state("");
    let contact = $state("");
    let address = $state("");
    let offerNr = $state("");
    let date = $state("");
    let validUntil = $state("");
    let items = $state([
        { desc: "Praesto – Lizenz pro Schuljahr (inkl. KI-Bewerbungstraining, Dossier, Lehrer-Cockpit)", price: "" }
    ]);
    let notes = $state("Hosting & Datenhaltung in der Schweiz. Support per E-Mail inbegriffen. Preise in CHF, exkl. allfälliger MwSt.");

    let total = $derived(items.reduce((s, i) => s + (parseFloat(i.price) || 0), 0));

    function addItem() {
        items = [...items, { desc: "", price: "" }];
    }
    function removeItem(idx) {
        items = items.filter((_, i) => i !== idx);
    }
    function fmt(n) {
        return (Number(n) || 0).toLocaleString("de-CH", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
    function print() {
        window.print();
    }
</script>

<svelte:head>
    <title>Offerte erstellen – Praesto</title>
</svelte:head>

<div class="wrap">
    <!-- Eingabe -->
    <section class="form no-print">
        <div class="form-head">
            <h1>📄 Offerte erstellen</h1>
            <button class="btn" onclick={print}>Als PDF speichern / drucken</button>
        </div>
        <p class="hint">Felder ausfüllen – die Vorschau rechts/unten wird zur fertigen Offerte. Preise frei eintragbar (je nach Anfrage).</p>

        <div class="grid">
            <label><span>Schule</span><input bind:value={school} placeholder="Sekundarschule Muster" /></label>
            <label><span>Ansprechperson</span><input bind:value={contact} placeholder="Frau/Herr …" /></label>
            <label><span>Adresse der Schule</span><input bind:value={address} placeholder="Strasse, PLZ Ort" /></label>
            <label><span>Offert-Nr.</span><input bind:value={offerNr} placeholder="2026-001" /></label>
            <label><span>Datum</span><input type="date" bind:value={date} /></label>
            <label><span>Gültig bis</span><input type="date" bind:value={validUntil} /></label>
        </div>

        <h3>Positionen</h3>
        {#each items as item, idx}
            <div class="item-row">
                <input class="desc" bind:value={item.desc} placeholder="Beschreibung" />
                <input class="price" type="number" step="0.05" bind:value={item.price} placeholder="CHF" />
                <button class="rm" type="button" onclick={() => removeItem(idx)} aria-label="Entfernen">✕</button>
            </div>
        {/each}
        <button class="btn-ghost" type="button" onclick={addItem}>+ Position</button>

        <label class="notes-label"><span>Bemerkungen / Konditionen</span><textarea bind:value={notes} rows="3"></textarea></label>
    </section>

    <!-- Druckbare Offerte -->
    <section class="sheet">
        <header class="doc-head">
            <div>
                <div class="logo">Praesto</div>
                <div class="sub">KI-Bewerbungscoach für die Lehrstellensuche</div>
            </div>
            <div class="op">
                Praesto – Julia Perić<br />
                Sandäckerstrasse 1a, 8957 Spreitenbach<br />
                info@praesto.ch · praesto.ch
            </div>
        </header>

        <div class="meta">
            <div class="recipient">
                {#if school}<strong>{school}</strong><br />{/if}
                {#if contact}{contact}<br />{/if}
                {#if address}{address}{/if}
            </div>
            <div class="meta-right">
                {#if offerNr}<div>Offerte Nr. {offerNr}</div>{/if}
                {#if date}<div>Datum: {date}</div>{/if}
                {#if validUntil}<div>Gültig bis: {validUntil}</div>{/if}
            </div>
        </div>

        <h1 class="doc-title">Offerte</h1>

        <table class="positions">
            <thead>
                <tr><th>Beschreibung</th><th class="amount">Betrag (CHF)</th></tr>
            </thead>
            <tbody>
                {#each items as item}
                    <tr><td>{item.desc || "—"}</td><td class="amount">{item.price ? fmt(item.price) : "—"}</td></tr>
                {/each}
            </tbody>
            <tfoot>
                <tr><td class="total-label">Total</td><td class="amount total">CHF {fmt(total)}</td></tr>
            </tfoot>
        </table>

        {#if notes}<p class="notes">{notes}</p>{/if}

        <p class="thanks">Wir freuen uns auf eine Zusammenarbeit. Bei Fragen: info@praesto.ch.</p>
        <p class="foot">Praesto – Julia Perić · Sandäckerstrasse 1a, 8957 Spreitenbach · info@praesto.ch</p>
    </section>
</div>

<style>
    .wrap { max-width: 900px; margin: 0 auto; padding: 1.5rem 1rem 3rem; color: #1f1830; }

    /* Eingabe */
    .form { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1.25rem; margin-bottom: 1.75rem; }
    .form-head { display: flex; justify-content: space-between; align-items: center; gap: 1rem; flex-wrap: wrap; }
    .form-head h1 { margin: 0; font-size: 1.4rem; color: #2F124D; }
    .hint { color: #6b647a; font-size: 0.88rem; margin: 0.4rem 0 1rem; }
    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }
    label { display: grid; gap: 0.25rem; }
    label span { font-size: 0.8rem; font-weight: 600; color: #2d2141; }
    input, textarea { border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.55rem 0.7rem; font: inherit; width: 100%; box-sizing: border-box; }
    h3 { margin: 1.1rem 0 0.5rem; color: #2d2141; font-size: 1rem; }
    .item-row { display: flex; gap: 0.5rem; margin-bottom: 0.5rem; }
    .item-row .desc { flex: 1; }
    .item-row .price { width: 120px; flex-shrink: 0; }
    .rm { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; border-radius: 0.5rem; width: 38px; flex-shrink: 0; cursor: pointer; }
    .btn { background: #2F124D; color: #fff; border: none; border-radius: 999px; padding: 0.6rem 1.2rem; font-weight: 700; cursor: pointer; }
    .btn-ghost { background: #faf8fc; border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.45rem 0.9rem; cursor: pointer; margin-top: 0.25rem; }
    .notes-label { margin-top: 1rem; }
    @media (max-width: 640px) { .grid { grid-template-columns: 1fr; } }

    /* Druckbare Offerte */
    .sheet { background: #fff; border: 1px solid #ece3f5; border-radius: 1rem; padding: 2rem; }
    .doc-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; border-bottom: 3px solid #2F124D; padding-bottom: 0.75rem; margin-bottom: 1.25rem; flex-wrap: wrap; }
    .logo { font-size: 1.6rem; font-weight: 800; color: #2F124D; }
    .sub { font-size: 0.85rem; color: #6b647a; }
    .op { font-size: 0.8rem; color: #4b4060; text-align: right; line-height: 1.5; }
    .meta { display: flex; justify-content: space-between; gap: 1rem; margin-bottom: 1.25rem; flex-wrap: wrap; }
    .recipient { line-height: 1.55; }
    .meta-right { font-size: 0.85rem; color: #4b4060; text-align: right; line-height: 1.6; }
    .doc-title { font-size: 1.5rem; color: #2F124D; margin: 0 0 1rem; }
    .positions { width: 100%; border-collapse: collapse; margin-bottom: 1rem; }
    .positions th, .positions td { text-align: left; padding: 0.6rem 0.5rem; border-bottom: 1px solid #ece3f5; vertical-align: top; }
    .positions .amount { text-align: right; white-space: nowrap; }
    .positions tfoot td { border-top: 2px solid #2F124D; border-bottom: none; font-weight: 700; padding-top: 0.7rem; }
    .total { color: #2F124D; font-size: 1.1rem; }
    .notes { background: #faf7fc; border: 1px solid #ece3f5; border-radius: 0.6rem; padding: 0.8rem 1rem; color: #2d2141; line-height: 1.55; }
    .thanks { color: #2d2141; margin-top: 1rem; }
    .foot { margin-top: 1.5rem; font-size: 0.75rem; color: #9a8b9d; text-align: center; }

    @media print {
        :global(.app-header), :global(.app-footer), :global(.demo-bar), :global(.demo-toast) { display: none !important; }
        :global(.app-main) { padding: 0 !important; }
        .no-print { display: none !important; }
        .wrap { padding: 0; max-width: 100%; }
        .sheet { border: none; border-radius: 0; padding: 0; }
    }
</style>
