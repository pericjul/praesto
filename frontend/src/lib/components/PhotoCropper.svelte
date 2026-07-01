<script>
    // Bewerbungsfoto zuschneiden auf das Hochformat (3:4), das der Lebenslauf braucht.
    // Der/die Nutzer:in wählt ein Bild, zoomt/verschiebt es im Rahmen; das Ergebnis
    // wird als sauber zugeschnittenes JPEG (600×800) an den Aufrufer übergeben – so
    // wird im Word-Dokument nichts verzerrt.
    let { onchange } = $props();

    const FRAME_W = 240;
    const FRAME_H = 320; // 3:4
    const OUT_W = 600;
    const OUT_H = 800;

    let src = $state(null);
    let zoom = $state(1);
    let tx = $state(0);
    let ty = $state(0);
    let natW = 0, natH = 0, coverScale = 1;
    let dragging = false, startX = 0, startY = 0, startTx = 0, startTy = 0;
    let imgEl = $state(null);

    function pickFile(e) {
        const f = e.target.files?.[0];
        if (!f) return;
        const reader = new FileReader();
        reader.onload = () => { src = reader.result; zoom = 1; };
        reader.readAsDataURL(f);
    }

    function onLoad() {
        natW = imgEl.naturalWidth;
        natH = imgEl.naturalHeight;
        coverScale = Math.max(FRAME_W / natW, FRAME_H / natH);
        tx = (FRAME_W - natW * coverScale) / 2;
        ty = (FRAME_H - natH * coverScale) / 2;
        emit();
    }

    let dispScale = $derived(coverScale * zoom);
    let dispW = $derived(natW * dispScale);
    let dispH = $derived(natH * dispScale);

    function clamp() {
        // Bild muss den Rahmen immer voll bedecken (keine Ränder)
        const minTx = FRAME_W - dispW, minTy = FRAME_H - dispH;
        if (tx > 0) tx = 0; if (tx < minTx) tx = minTx;
        if (ty > 0) ty = 0; if (ty < minTy) ty = minTy;
    }

    function onZoom() {
        // um die Rahmenmitte zoomen
        const cx = FRAME_W / 2, cy = FRAME_H / 2;
        // aktuelle Bildkoordinate unter der Mitte beibehalten
        clamp();
        emit();
    }

    function down(e) {
        dragging = true;
        const p = e.touches ? e.touches[0] : e;
        startX = p.clientX; startY = p.clientY; startTx = tx; startTy = ty;
    }
    function move(e) {
        if (!dragging) return;
        const p = e.touches ? e.touches[0] : e;
        tx = startTx + (p.clientX - startX);
        ty = startTy + (p.clientY - startY);
        clamp();
    }
    function up() {
        if (!dragging) return;
        dragging = false;
        emit();
    }

    function emit() {
        if (!src || !natW) return;
        clamp();
        const canvas = document.createElement("canvas");
        canvas.width = OUT_W; canvas.height = OUT_H;
        const ctx = canvas.getContext("2d");
        ctx.fillStyle = "#fff";
        ctx.fillRect(0, 0, OUT_W, OUT_H);
        // Quell-Region, die im Rahmen sichtbar ist
        const sx = -tx / dispScale;
        const sy = -ty / dispScale;
        const sw = FRAME_W / dispScale;
        const sh = FRAME_H / dispScale;
        ctx.drawImage(imgEl, sx, sy, sw, sh, 0, 0, OUT_W, OUT_H);
        canvas.toBlob((blob) => { if (blob) onchange?.(blob); }, "image/jpeg", 0.9);
    }

    function clear() {
        src = null;
        onchange?.(null);
    }
</script>

<div class="cropper">
    {#if !src}
        <label class="pick">
            <input type="file" accept="image/png,image/jpeg" onchange={pickFile} />
            <span>📷 Foto auswählen</span>
        </label>
    {:else}
        <div class="stage" style="width:{FRAME_W}px;height:{FRAME_H}px"
            onmousedown={down} onmousemove={move} onmouseup={up} onmouseleave={up}
            ontouchstart={down} ontouchmove={move} ontouchend={up} role="presentation">
            <img bind:this={imgEl} {src} alt="" draggable="false" onload={onLoad}
                style="width:{dispW}px;height:{dispH}px;transform:translate({tx}px,{ty}px)" />
            <div class="frame-hint"></div>
        </div>
        <div class="controls">
            <label class="zoom">🔍 <input type="range" min="1" max="3" step="0.01" bind:value={zoom} oninput={onZoom} /></label>
            <button type="button" class="clear" onclick={clear}>Anderes Foto</button>
        </div>
        <p class="tip">Ziehen zum Verschieben · Regler zum Zoomen. Kopf mittig platzieren.</p>
    {/if}
</div>

<style>
    .cropper { display: grid; gap: 0.5rem; justify-items: start; }
    .pick { display: inline-flex; align-items: center; gap: 0.5rem; background: #faf8fc; border: 1.5px dashed #cdbce4; border-radius: 0.7rem; padding: 0.7rem 1rem; cursor: pointer; color: #2F124D; font-weight: 600; }
    .pick input { display: none; }
    .stage { position: relative; overflow: hidden; border-radius: 0.5rem; border: 1px solid #e8e0f0; background: #f3eefb; cursor: grab; touch-action: none; user-select: none; }
    .stage:active { cursor: grabbing; }
    .stage img { position: absolute; top: 0; left: 0; max-width: none; pointer-events: none; }
    .frame-hint { position: absolute; inset: 0; box-shadow: inset 0 0 0 2px rgba(47,18,77,0.25); pointer-events: none; }
    .controls { display: flex; align-items: center; gap: 1rem; }
    .zoom { display: inline-flex; align-items: center; gap: 0.4rem; }
    .zoom input { width: 160px; }
    .clear { background: none; border: none; color: #6b647a; text-decoration: underline; cursor: pointer; font-size: 0.85rem; }
    .tip { margin: 0; font-size: 0.78rem; color: #9a8baf; }
</style>
