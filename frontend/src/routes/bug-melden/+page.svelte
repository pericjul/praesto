<script>
    import { enhance } from "$app/forms";
    import { t } from "$lib/i18n";

    let { form } = $props();
    let sending = $state(false);

    const AREAS = ["CHAT", "ASSIGNMENTS", "DOSSIER", "DASHBOARD", "LOGIN", "OTHER"];
    const SEVERITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

    // ----- Screenshot: einfügen (Ctrl/Cmd+V), reinziehen oder Datei wählen -----
    let screenshot = $state("");   // Data-URL (JPEG), wird als hidden input mitgeschickt
    let imgBusy = $state(false);
    let imgError = $state("");
    let dragOver = $state(false);

    // Bild verkleinern + als JPEG komprimieren, bis es klein genug ist (< ~330 KB).
    async function compress(file) {
        const dataUrl = await new Promise((res, rej) => {
            const r = new FileReader();
            r.onload = () => res(r.result);
            r.onerror = rej;
            r.readAsDataURL(file);
        });
        const img = new Image();
        await new Promise((res, rej) => { img.onload = res; img.onerror = rej; img.src = dataUrl; });

        let maxDim = 1280;
        let scale = Math.min(1, maxDim / Math.max(img.width, img.height));
        let w = Math.max(1, Math.round(img.width * scale));
        let h = Math.max(1, Math.round(img.height * scale));
        const canvas = document.createElement("canvas");
        const ctx = canvas.getContext("2d");
        let quality = 0.85;
        let out = dataUrl;
        for (let i = 0; i < 9; i++) {
            canvas.width = w; canvas.height = h;
            ctx.fillStyle = "#ffffff";
            ctx.fillRect(0, 0, w, h);
            ctx.drawImage(img, 0, 0, w, h);
            out = canvas.toDataURL("image/jpeg", quality);
            if (out.length <= 330000) break;
            if (quality > 0.5) quality -= 0.15;
            else { w = Math.max(1, Math.round(w * 0.8)); h = Math.max(1, Math.round(h * 0.8)); }
        }
        return out;
    }

    async function handleFile(file) {
        imgError = "";
        if (!file) return;
        if (!file.type.startsWith("image/")) { imgError = $t("bug.shotErrType"); return; }
        imgBusy = true;
        try {
            screenshot = await compress(file);
        } catch (e) {
            imgError = $t("bug.shotErrRead");
        } finally {
            imgBusy = false;
        }
    }

    function onPaste(e) {
        const items = e.clipboardData?.items;
        if (!items) return;
        for (const it of items) {
            if (it.type && it.type.startsWith("image/")) {
                const f = it.getAsFile();
                if (f) { e.preventDefault(); handleFile(f); return; }
            }
        }
    }

    function onDrop(e) {
        e.preventDefault();
        dragOver = false;
        const f = e.dataTransfer?.files?.[0];
        if (f) handleFile(f);
    }

    function onPick(e) {
        const f = e.currentTarget.files?.[0];
        if (f) handleFile(f);
    }

    function removeShot() {
        screenshot = "";
        imgError = "";
    }

    function handle() {
        sending = true;
        return async ({ update }) => {
            await update();
            sending = false;
        };
    }
</script>

<svelte:head>
    <title>{$t('bug.title')} – Praesto</title>
</svelte:head>

<svelte:window onpaste={onPaste} />

<div class="page">
    <header class="page-header">
        <div>
            <h1 class="title">🐞 {$t('bug.title')}</h1>
            <p class="subtitle">{$t('bug.intro')}</p>
        </div>
    </header>
    <p class="contest">{$t('bug.contest')}</p>

    {#if form?.success}
        <div class="done">
            <p>✅ {$t('bug.success')}</p>
            <a href="/bug-melden" class="btn-primary" data-sveltekit-reload>{$t('bug.again')}</a>
        </div>
    {:else}
        {#if form?.error}<div class="err">{$t('bug.error')}</div>{/if}

        <form method="POST" use:enhance={handle} class="form" class:busy={sending}>
            <label>
                <span>{$t('bug.fTitle')}</span>
                <input name="title" required placeholder={$t('bug.fTitlePh')} maxlength="200" />
            </label>

            <div class="grid">
                <label>
                    <span>{$t('bug.fArea')}</span>
                    <select name="area">
                        {#each AREAS as a}<option value={a}>{$t('bug.area.' + a)}</option>{/each}
                    </select>
                </label>
                <label>
                    <span>{$t('bug.fSeverity')}</span>
                    <select name="severity">
                        {#each SEVERITIES as s}<option value={s} selected={s === 'MEDIUM'}>{$t('bug.sev.' + s)}</option>{/each}
                    </select>
                </label>
            </div>

            <label>
                <span>{$t('bug.fDesc')}</span>
                <textarea name="description" rows="4" required placeholder={$t('bug.fDescPh')}></textarea>
            </label>

            <label>
                <span>{$t('bug.fSteps')}</span>
                <textarea name="steps" rows="3" placeholder={$t('bug.fStepsPh')}></textarea>
            </label>

            <label>
                <span>{$t('bug.fDevice')}</span>
                <input name="device" placeholder={$t('bug.fDevicePh')} maxlength="200" />
            </label>

            <!-- Screenshot: einfügen (Ctrl/Cmd+V), reinziehen oder auswählen -->
            <div class="shot">
                <span class="shot-label">{$t('bug.fScreenshot')}</span>
                <input type="hidden" name="screenshot" value={screenshot} />
                {#if screenshot}
                    <div class="shot-preview">
                        <img src={screenshot} alt="Screenshot" />
                        <button type="button" class="shot-remove" onclick={removeShot}>✕ {$t('bug.shotRemove')}</button>
                    </div>
                {:else}
                    <label
                        class="dropzone"
                        class:over={dragOver}
                        ondragover={(e) => { e.preventDefault(); dragOver = true; }}
                        ondragleave={() => (dragOver = false)}
                        ondrop={onDrop}
                    >
                        <input type="file" accept="image/*" onchange={onPick} hidden />
                        {#if imgBusy}
                            <span class="dz-main">⏳ {$t('bug.shotBusy')}</span>
                        {:else}
                            <span class="dz-main">📎 {$t('bug.shotDrop')}</span>
                            <span class="dz-hint">{$t('bug.shotHint')}</span>
                        {/if}
                    </label>
                {/if}
                {#if imgError}<span class="shot-err">{imgError}</span>{/if}
            </div>

            <button type="submit" class="btn-primary" disabled={sending || imgBusy}>
                {sending ? $t('bug.sending') : $t('bug.submit')}
            </button>
        </form>
    {/if}
</div>

<style>
    .page { max-width: 1000px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    h1 { margin: 0 0 0.6rem; color: #2F124D; font-size: 1.5rem; }
    .contest {
        background: linear-gradient(135deg, #fff4e6, #ffe9d6);
        border: 1px solid #f7c98f; color: #8a4b12;
        border-radius: 0.75rem; padding: 0.7rem 1rem; margin: 0 0 1.25rem; font-weight: 600; font-size: 0.92rem;
    }

    .form { display: grid; gap: 0.9rem; }
    .form.busy { opacity: 0.6; pointer-events: none; }
    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.9rem; }
    label { display: grid; gap: 0.3rem; }
    label span { font-size: 0.85rem; font-weight: 600; color: #2d2141; }
    input, textarea, select {
        width: 100%; box-sizing: border-box; border: 1px solid #e8e0f0; border-radius: 0.6rem;
        padding: 0.6rem 0.75rem; font: inherit; background: #faf8fc;
    }
    input:focus, textarea:focus, select:focus { outline: 2px solid #2F124D; outline-offset: 1px; background: #fff; }

    .btn-primary {
        justify-self: start; margin-top: 0.3rem; background: #2F124D; color: #fff; border: none;
        border-radius: 999px; padding: 0.8rem 1.8rem; font-weight: 700; font-size: 1rem; cursor: pointer; text-decoration: none;
    }
    .btn-primary:hover { background: #41205f; }
    .btn-primary:disabled { opacity: 0.6; cursor: default; }

    /* Screenshot-Zone */
    .shot { display: grid; gap: 0.35rem; }
    .shot-label { font-size: 0.85rem; font-weight: 600; color: #2d2141; }
    .dropzone {
        display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 0.25rem;
        border: 2px dashed #d6c8ea; border-radius: 0.75rem; padding: 1.4rem 1rem; text-align: center;
        background: #faf8fc; cursor: pointer; transition: border-color 0.15s, background 0.15s;
    }
    .dropzone:hover { border-color: #2F124D; background: #f5f0fb; }
    .dropzone.over { border-color: #2F124D; background: #efe7fa; }
    .dz-main { font-weight: 600; color: #2F124D; font-size: 0.95rem; }
    .dz-hint { font-size: 0.8rem; color: #8a7f9a; }
    .shot-preview { position: relative; display: inline-block; max-width: 100%; }
    .shot-preview img {
        max-width: 100%; max-height: 320px; border-radius: 0.6rem; border: 1px solid #e0d6eb; display: block;
    }
    .shot-remove {
        margin-top: 0.5rem; background: #fff; border: 1px solid #e0d6eb; color: #b91c1c;
        border-radius: 999px; padding: 0.35rem 0.9rem; font-size: 0.85rem; font-weight: 600; cursor: pointer;
    }
    .shot-remove:hover { background: #fef2f2; border-color: #fecaca; }
    .shot-err { font-size: 0.82rem; color: #b91c1c; }

    .done { background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 1rem; padding: 1.5rem; text-align: center; }
    .done p { margin: 0 0 1rem; color: #166534; font-weight: 600; }
    .err { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; border-radius: 0.6rem; padding: 0.7rem 1rem; }

    @media (max-width: 560px) { .grid { grid-template-columns: 1fr; } }
</style>
