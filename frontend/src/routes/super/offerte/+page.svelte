<script>
    // Offerten-Generator (nur Super-Admin). Dynamische Positionen; beim Download wird
    // die Word-Vorlage serverseitig befüllt und als .docx geliefert.
    let schuleName = $state("");
    let ansprechperson = $state("");
    let strasse = $state("");
    let plz = $state("");
    let ort = $state("");
    let offertenNr = $state("");
    let datum = $state("");
    let gueltigBis = $state("");
    let schuljahr = $state("");
    let laufzeit = $state("1 Schuljahr");

    let uid = 0;
    let positions = $state([
        { id: uid++, bezeichnung: "Praesto – Lizenz pro Schuljahr (inkl. KI-Training, Dossier, Lehrer-Cockpit)", menge: "1", einheit: "Schuljahr", preis: "" }
    ]);
    let downloading = $state(false);
    let error = $state("");

    function addPosition() {
        positions = [...positions, { id: uid++, bezeichnung: "", menge: "1", einheit: "Schuljahr", preis: "" }];
    }
    function removePosition(id) {
        positions = positions.filter((p) => p.id !== id);
        if (positions.length === 0) addPosition();
    }

    function num(s) {
        if (!s) return 0;
        const c = String(s).replace(/[^0-9.,]/g, "").replace(",", ".");
        const v = parseFloat(c);
        return isNaN(v) ? 0 : v;
    }
    function lineTotal(p) {
        return (num(p.menge) || 1) * num(p.preis);
    }
    let subtotal = $derived(positions.reduce((s, p) => s + (p.bezeichnung.trim() ? lineTotal(p) : 0), 0));
    let mwst = $derived(Math.round(subtotal * 0.081 * 100) / 100);
    let gesamt = $derived(subtotal + mwst);
    function fmt(n) {
        return (Number(n) || 0).toLocaleString("de-CH", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    async function download() {
        error = "";
        downloading = true;
        try {
            const payload = {
                schuleName, ansprechperson, strasse, plz, ort,
                offertenNr, datum, gueltigBis, schuljahr, laufzeit,
                positions: positions
                    .filter((p) => p.bezeichnung.trim())
                    .map((p) => ({ bezeichnung: p.bezeichnung, menge: p.menge, einheit: p.einheit, preis: p.preis }))
            };
            const res = await fetch("/super/offerte/download", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            if (!res.ok) {
                error = (await res.text().catch(() => "")) || "Download fehlgeschlagen.";
                return;
            }
            const blob = await res.blob();
            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = `Offerte_Praesto${offertenNr ? "_" + offertenNr.replace(/[^a-zA-Z0-9._-]/g, "_") : ""}.docx`;
            document.body.appendChild(a);
            a.click();
            a.remove();
            URL.revokeObjectURL(url);
        } catch {
            error = "Verbindungsfehler.";
        } finally {
            downloading = false;
        }
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
    <p class="hint">Felder ausfüllen und herunterladen – daraus wird eine fertig formatierte Word-Offerte.
        Zwischentotal, MwSt. (8.1 %) und Gesamtbetrag werden berechnet.</p>

    {#if error}<div class="err">⚠️ {error}</div>{/if}

    <div class="form">
        <h2>Empfänger</h2>
        <div class="grid">
            <label><span>Schule</span><input bind:value={schuleName} placeholder="Sekundarschule Muster" /></label>
            <label><span>Ansprechperson</span><input bind:value={ansprechperson} placeholder="Frau/Herr …" /></label>
            <label><span>Strasse</span><input bind:value={strasse} placeholder="Musterstrasse 1" /></label>
            <label><span>PLZ</span><input bind:value={plz} placeholder="8000" /></label>
            <label><span>Ort</span><input bind:value={ort} placeholder="Zürich" /></label>
        </div>

        <h2>Offerte</h2>
        <div class="grid">
            <label><span>Offerten-Nr.</span><input bind:value={offertenNr} placeholder="2026-001" /></label>
            <label><span>Datum</span><input bind:value={datum} placeholder="01.07.2026" /></label>
            <label><span>Gültig bis</span><input bind:value={gueltigBis} placeholder="31.08.2026" /></label>
            <label><span>Schuljahr</span><input bind:value={schuljahr} placeholder="2026/2027" /></label>
            <label><span>Laufzeit</span><input bind:value={laufzeit} /></label>
        </div>

        <h2>Positionen</h2>
        {#each positions as p (p.id)}
            <div class="pos">
                <label class="desc"><span>Beschreibung</span><input bind:value={p.bezeichnung} placeholder="z.B. Praesto-Lizenz pro Schuljahr" /></label>
                <label class="sm"><span>Menge</span><input bind:value={p.menge} /></label>
                <label class="sm"><span>Einheit</span><input bind:value={p.einheit} /></label>
                <label class="sm"><span>Preis/Einh. (CHF)</span><input bind:value={p.preis} placeholder="1200" /></label>
                <div class="pos-total">
                    <span>Total</span>
                    <strong>CHF {fmt(lineTotal(p))}</strong>
                </div>
                <button type="button" class="rm" onclick={() => removePosition(p.id)} title="Entfernen" aria-label="Entfernen">✕</button>
            </div>
        {/each}
        <button type="button" class="add" onclick={addPosition}>＋ Position hinzufügen</button>

        <div class="totals">
            <div><span>Zwischentotal (exkl. MwSt.)</span><strong>CHF {fmt(subtotal)}</strong></div>
            <div><span>MwSt. 8.1 %</span><strong>CHF {fmt(mwst)}</strong></div>
            <div class="grand"><span>Gesamtbetrag (inkl. MwSt.)</span><strong>CHF {fmt(gesamt)}</strong></div>
        </div>

        <button type="button" class="btn" onclick={download} disabled={downloading}>
            {downloading ? "Wird erstellt …" : "📥 Offerte als Word herunterladen"}
        </button>
    </div>
</div>

<style>
    .wrap { max-width: 860px; margin: 0 auto; padding: 1.5rem 1rem 3rem; color: #1f1830; }
    .head { display: flex; justify-content: space-between; align-items: center; gap: 1rem; flex-wrap: wrap; }
    .head h1 { margin: 0; font-size: 1.5rem; color: #2F124D; }
    .back { color: #6b647a; text-decoration: none; }
    .hint { color: #6b647a; font-size: 0.9rem; margin: 0.4rem 0 1.25rem; }
    .err { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; border-radius: 0.6rem; padding: 0.7rem 1rem; margin-bottom: 1rem; }

    .form { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1.5rem; }
    h2 { margin: 1.25rem 0 0.6rem; color: #2d2141; font-size: 1rem; border-bottom: 2px solid #ece3f5; padding-bottom: 0.3rem; }
    h2:first-of-type { margin-top: 0; }
    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }
    label { display: grid; gap: 0.25rem; }
    label span { font-size: 0.78rem; font-weight: 600; color: #2d2141; }
    input { border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.55rem 0.7rem; font: inherit; width: 100%; box-sizing: border-box; }
    input:focus { outline: 2px solid #2F124D; outline-offset: 1px; }

    .pos { display: grid; grid-template-columns: 1fr 70px 90px 120px auto auto; gap: 0.5rem; align-items: end; margin-bottom: 0.6rem; }
    .pos-total { display: grid; gap: 0.1rem; font-size: 0.78rem; color: #6b647a; padding-bottom: 0.15rem; white-space: nowrap; }
    .pos-total strong { color: #2F124D; }
    .rm { align-self: center; background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; border-radius: 0.5rem; width: 36px; height: 38px; cursor: pointer; }
    .add { margin-top: 0.25rem; background: #f3eefa; color: #2F124D; border: 1px dashed #cdbce4; border-radius: 999px; padding: 0.5rem 1.1rem; font-weight: 600; cursor: pointer; }
    .add:hover { background: #ece1f7; }

    .totals { margin-top: 1.25rem; border-top: 2px solid #ece3f5; padding-top: 0.85rem; display: flex; flex-direction: column; gap: 0.3rem; }
    .totals div { display: flex; justify-content: space-between; color: #4b4060; }
    .totals .grand { font-size: 1.1rem; color: #2F124D; margin-top: 0.25rem; padding-top: 0.4rem; border-top: 1px solid #ece3f5; }

    .btn { margin-top: 1.5rem; background: #2F124D; color: #fff; border: none; border-radius: 999px; padding: 0.85rem 1.8rem; font-weight: 700; font-size: 1rem; cursor: pointer; }
    .btn:hover { background: #41205f; }
    .btn:disabled { opacity: 0.6; cursor: default; }

    @media (max-width: 700px) {
        .grid { grid-template-columns: 1fr; }
        .pos { grid-template-columns: 1fr 1fr; }
        .pos .desc { grid-column: 1 / -1; }
        .pos-total { grid-column: 1 / -1; flex-direction: row; justify-content: space-between; }
        .rm { width: 100%; }
    }
</style>
