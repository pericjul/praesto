<script>
    // Offerten-Generator (nur Super-Admin). Felder ausfüllen -> Formular wird an den
    // Download-Endpoint geschickt, der die Word-Vorlage befüllt und als .docx liefert.
    let total1 = $state("");
    let total2 = $state("");
    let total3 = $state("");

    function num(s) {
        if (!s) return 0;
        const c = String(s).replace(/[^0-9.,]/g, "").replace(",", ".");
        const v = parseFloat(c);
        return isNaN(v) ? 0 : v;
    }
    let subtotal = $derived(num(total1) + num(total2) + num(total3));
    let mwst = $derived(Math.round(subtotal * 0.081 * 100) / 100);
    let gesamt = $derived(subtotal + mwst);
    function fmt(n) {
        return (Number(n) || 0).toLocaleString("de-CH", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
</script>

<svelte:head>
    <title>Offerte erstellen – Praesto</title>
</svelte:head>

<div class="wrap">
    <div class="head">
        <h1>📄 Offerte erstellen</h1>
        <a class="back" href="/super/dashboard">← Zurück</a>
    </div>
    <p class="hint">Felder ausfüllen und herunterladen – daraus wird eine fertig formatierte Word-Offerte
        (aus der Vorlage). Beträge frei eintragbar; Zwischentotal, MwSt. (8.1 %) und Gesamtbetrag werden berechnet.</p>

    <form method="POST" action="/super/offerte/download" class="form">
        <h2>Empfänger</h2>
        <div class="grid">
            <label><span>Schule</span><input name="schuleName" placeholder="Sekundarschule Muster" /></label>
            <label><span>Ansprechperson</span><input name="ansprechperson" placeholder="Frau/Herr …" /></label>
            <label><span>Strasse</span><input name="strasse" placeholder="Musterstrasse 1" /></label>
            <label><span>PLZ</span><input name="plz" placeholder="8000" /></label>
            <label><span>Ort</span><input name="ort" placeholder="Zürich" /></label>
        </div>

        <h2>Offerte</h2>
        <div class="grid">
            <label><span>Offerten-Nr.</span><input name="offertenNr" placeholder="2026-001" /></label>
            <label><span>Datum</span><input name="datum" placeholder="01.07.2026" /></label>
            <label><span>Gültig bis</span><input name="gueltigBis" placeholder="31.08.2026" /></label>
            <label><span>Schuljahr</span><input name="schuljahr" placeholder="2026/2027" /></label>
            <label><span>Laufzeit</span><input name="laufzeit" placeholder="1 Schuljahr" /></label>
        </div>

        <h2>Positionen</h2>
        <div class="grid3">
            <label><span>Menge</span><input name="menge" value="1" /></label>
            <label><span>Einheit</span><input name="einheit" value="Schuljahr" /></label>
            <label><span>Preis / Einheit (CHF)</span><input name="preis" placeholder="1200" /></label>
        </div>
        <div class="pos">
            <label class="desc"><span>Position 1</span><input name="position1" placeholder="Praesto – Lizenz pro Schuljahr (inkl. KI-Training, Dossier, Lehrer-Cockpit)" /></label>
            <label class="amt"><span>Total (CHF)</span><input name="total1" bind:value={total1} placeholder="1200" /></label>
        </div>
        <div class="pos">
            <label class="desc"><span>Position 2 <em>(optional)</em></span><input name="position2" /></label>
            <label class="amt"><span>Total (CHF)</span><input name="total2" bind:value={total2} /></label>
        </div>
        <div class="pos">
            <label class="desc"><span>Position 3 <em>(optional)</em></span><input name="position3" /></label>
            <label class="amt"><span>Total (CHF)</span><input name="total3" bind:value={total3} /></label>
        </div>

        <div class="totals">
            <div><span>Zwischentotal (exkl. MwSt.)</span><strong>CHF {fmt(subtotal)}</strong></div>
            <div><span>MwSt. 8.1 %</span><strong>CHF {fmt(mwst)}</strong></div>
            <div class="grand"><span>Gesamtbetrag (inkl. MwSt.)</span><strong>CHF {fmt(gesamt)}</strong></div>
        </div>

        <button type="submit" class="btn">📥 Offerte als Word herunterladen</button>
    </form>
</div>

<style>
    .wrap { max-width: 820px; margin: 0 auto; padding: 1.5rem 1rem 3rem; color: #1f1830; }
    .head { display: flex; justify-content: space-between; align-items: center; gap: 1rem; flex-wrap: wrap; }
    .head h1 { margin: 0; font-size: 1.5rem; color: #2F124D; }
    .back { color: #6b647a; text-decoration: none; }
    .hint { color: #6b647a; font-size: 0.9rem; margin: 0.4rem 0 1.5rem; }

    .form { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1.5rem; }
    h2 { margin: 1.25rem 0 0.6rem; color: #2d2141; font-size: 1rem; border-bottom: 2px solid #ece3f5; padding-bottom: 0.3rem; }
    h2:first-of-type { margin-top: 0; }
    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }
    .grid3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0.75rem; margin-bottom: 0.75rem; }
    label { display: grid; gap: 0.25rem; }
    label span { font-size: 0.8rem; font-weight: 600; color: #2d2141; }
    label em { color: #9a8baf; font-weight: 400; font-style: normal; }
    input { border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.55rem 0.7rem; font: inherit; width: 100%; box-sizing: border-box; }
    input:focus { outline: 2px solid #2F124D; outline-offset: 1px; }

    .pos { display: flex; gap: 0.75rem; margin-bottom: 0.6rem; }
    .pos .desc { flex: 1; }
    .pos .amt { flex: 0 0 130px; }

    .totals { margin-top: 1.25rem; border-top: 2px solid #ece3f5; padding-top: 0.85rem; display: flex; flex-direction: column; gap: 0.3rem; }
    .totals div { display: flex; justify-content: space-between; color: #4b4060; }
    .totals .grand { font-size: 1.1rem; color: #2F124D; margin-top: 0.25rem; padding-top: 0.4rem; border-top: 1px solid #ece3f5; }

    .btn { margin-top: 1.5rem; background: #2F124D; color: #fff; border: none; border-radius: 999px; padding: 0.85rem 1.8rem; font-weight: 700; font-size: 1rem; cursor: pointer; }
    .btn:hover { background: #41205f; }

    @media (max-width: 600px) {
        .grid, .grid3 { grid-template-columns: 1fr; }
        .pos { flex-direction: column; }
        .pos .amt { flex: 1; }
    }
</style>
