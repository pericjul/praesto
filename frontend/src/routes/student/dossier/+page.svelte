<script>
    import { enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    let documents = $derived(data.documents ?? []);
    let cvLeft = $derived(data.quota?.CV?.remaining ?? null);
    let clLeft = $derived(data.quota?.COVER_LETTER?.remaining ?? null);

    let deleteConfirmId = $state(null);
    let uploading = $state(false);
    let uploadError = $state("");

    const MAX_UPLOAD_MB = 20;

    const CATEGORIES = ["LEBENSLAUF", "BEWERBUNGSSCHREIBEN", "ZEUGNIS", "ZERTIFIKAT", "ARBEITSPROBE", "FOTO", "SONSTIGES"];

    function catLabel(c) {
        return $t("dossier.cat." + c);
    }

    function catIcon(c) {
        return { LEBENSLAUF: "📄", BEWERBUNGSSCHREIBEN: "✍️", ZEUGNIS: "🎓", ZERTIFIKAT: "🏅", ARBEITSPROBE: "🎨", FOTO: "🖼️", SONSTIGES: "📎" }[c] ?? "📎";
    }

    let grouped = $derived(CATEGORIES
        .map((c) => ({ category: c, items: documents.filter((d) => d.category === c) }))
        .filter((g) => g.items.length > 0));

    function formatDate(d) {
        if (!d) return "";
        return new Date(d).toLocaleDateString("de-CH", { day: "2-digit", month: "2-digit", year: "numeric" });
    }

    function handleUpload({ formData, cancel }) {
        uploadError = "";
        const f = formData.get("file");
        if (f && typeof f !== "string" && f.size > MAX_UPLOAD_MB * 1024 * 1024) {
            uploadError = $t('dossier.fileTooLarge').replace('%N', MAX_UPLOAD_MB);
            cancel();
            return;
        }
        uploading = true;
        return async ({ result, update }) => {
            if (result.type === "success") {
                await invalidateAll();
            } else if (result.type === "failure" && result.data?.error) {
                uploadError = result.data.error;
            }
            uploading = false;
            await update({ reset: true });
        };
    }

    // Muss die SUBMIT-Funktion sein (bekommt das Submit-Event) und gibt den
    // Nach-Submit-Callback zurück. Vorher wurde der Callback direkt übergeben ->
    // beim Absenden war `result` undefined -> Fehler -> Löschen startete nie.
    function handleDelete() {
        return async ({ result, update }) => {
            deleteConfirmId = null;
            if (result.type === "success") await invalidateAll();
            else await update({ reset: false });
        };
    }
</script>

<svelte:head>
    <title>{$t('dossier.title')} – Praesto</title>
</svelte:head>

<div class="page">
    <header class="page-header">
        <div>
            <h1 class="title">📁 {$t('dossier.title')}</h1>
            <p class="subtitle">{$t('dossier.subtitle')}</p>
        </div>
    </header>

    <!-- Erstellen -->
    <section class="create">
        <a href="/student/dossier/lebenslauf" class="create-btn">
            <span class="ci">📄</span>
            <span class="ct">{$t('dossier.createCv')}</span>
            <span class="cq">{cvLeft != null ? (cvLeft > 0 ? $t('dossier.available') : $t('dossier.usedUp')) : ''}</span>
        </a>
        <a href="/student/dossier/brief" class="create-btn">
            <span class="ci">✍️</span>
            <span class="ct">{$t('dossier.createLetter')}</span>
            <span class="cq">{clLeft != null ? $t('dossier.leftN').replace('%N', clLeft) : ''}</span>
        </a>
    </section>

    <p class="disclaimer">⚠️ {$t('dossier.disclaimer')}</p>

    <!-- Upload -->
    <section class="upload">
        <h2>⬆️ {$t('dossier.uploadTitle')}</h2>
        <p class="upload-hint">{$t('dossier.uploadHint').replace('%N', MAX_UPLOAD_MB)}</p>
        {#if uploadError}<div class="err">{uploadError}</div>{/if}
        {#if form?.error}<div class="err">{typeof form.error === 'string' ? form.error : $t('dossier.uploadError')}</div>{/if}
        <form method="POST" action="?/upload" enctype="multipart/form-data" use:enhance={handleUpload} class="upload-form">
            <input type="file" name="file" required />
            <select name="category">
                {#each CATEGORIES as c}<option value={c}>{catLabel(c)}</option>{/each}
            </select>
            <input type="text" name="title" placeholder={$t('dossier.titlePlaceholder')} />
            <button type="submit" class="btn-upload" disabled={uploading}>
                {uploading ? $t('dossier.uploading') : $t('dossier.uploadBtn')}
            </button>
        </form>
    </section>

    <!-- Dokumente -->
    {#if documents.length === 0}
        <p class="empty">{$t('dossier.empty')}</p>
    {:else}
        {#each grouped as group (group.category)}
            <section class="cat-group">
                <h3>{catIcon(group.category)} {catLabel(group.category)}</h3>
                <ul class="doc-list">
                    {#each group.items as doc (doc.id)}
                        <li class="doc">
                            <div class="doc-info">
                                <span class="doc-title">{doc.title || doc.fileName}</span>
                                <span class="doc-meta">
                                    {formatDate(doc.createdAt)}
                                    {#if doc.generated}<span class="gen-badge">🤖 {$t('dossier.generated')}</span>{/if}
                                </span>
                            </div>
                            <div class="doc-actions">
                                <a class="btn-dl" href={`/files/${doc.fileUrl}`} download>{$t('dossier.download')}</a>
                                {#if deleteConfirmId === doc.id}
                                    <form method="POST" action="?/delete" use:enhance={handleDelete} class="inline">
                                        <input type="hidden" name="id" value={doc.id} />
                                        <button type="submit" class="btn-del-yes">{$t('dossier.confirmDelete')}</button>
                                    </form>
                                    <button type="button" class="btn-cancel" onclick={() => deleteConfirmId = null}>{$t('dossier.cancel')}</button>
                                {:else}
                                    <button type="button" class="btn-del" onclick={() => deleteConfirmId = doc.id} title={$t('dossier.delete')}>🗑️</button>
                                {/if}
                            </div>
                        </li>
                    {/each}
                </ul>
            </section>
        {/each}
    {/if}
</div>

<style>
    .page { max-width: 1000px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    h1 { margin: 0 0 0.3rem; color: #2F124D; font-size: 1.6rem; }
    .subtitle { margin: 0 0 1.5rem; color: #6b647a; }

    .create { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
    .disclaimer { background: #fff7ed; border: 1px solid #fed7aa; color: #9a3412; border-radius: 0.6rem; padding: 0.7rem 0.9rem; font-size: 0.85rem; line-height: 1.45; margin: 0 0 1.5rem; }
    .create-btn {
        display: flex; flex-direction: column; align-items: flex-start; gap: 0.25rem;
        text-decoration: none; padding: 1.1rem 1.25rem; border-radius: 1rem;
        background: linear-gradient(135deg, #2F124D 0%, #5a2d6e 60%, #c97d3c 100%); color: #fff;
    }
    .create-btn .ci { font-size: 1.5rem; }
    .create-btn .ct { font-weight: 700; font-size: 1.05rem; }
    .create-btn .cq { font-size: 0.8rem; opacity: 0.9; }

    .upload { background: #fff; border: 1px solid #e8e0f0; border-radius: 1rem; padding: 1.1rem 1.25rem; margin-bottom: 1.5rem; }
    .upload h2 { margin: 0 0 0.35rem; font-size: 1.1rem; color: #2F124D; }
    .upload-hint { margin: 0 0 0.75rem; font-size: 0.8rem; color: #8a7f9a; }
    .upload-form { display: flex; gap: 0.6rem; flex-wrap: wrap; align-items: center; }
    .upload-form input[type=text], .upload-form select { border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.5rem 0.6rem; font: inherit; }
    .btn-upload { background: #2F124D; color: #fff; border: none; border-radius: 0.5rem; padding: 0.55rem 1.1rem; font-weight: 600; cursor: pointer; }
    .btn-upload:disabled { opacity: 0.6; cursor: default; }

    .empty { color: #9a8b9d; }

    .cat-group { margin-bottom: 1.25rem; }
    .cat-group h3 { margin: 0 0 0.5rem; color: #2d2141; font-size: 1rem; }
    .doc-list { list-style: none; margin: 0; padding: 0; display: grid; gap: 0.5rem; }
    .doc { display: flex; justify-content: space-between; align-items: center; gap: 1rem; background: #fff; border: 1px solid #e8e0f0; border-radius: 0.75rem; padding: 0.7rem 1rem; }
    .doc-title { font-weight: 600; color: #2d2141; overflow-wrap: anywhere; word-break: break-word; min-width: 0; }
    .doc-meta { display: block; font-size: 0.78rem; color: #9a8b9d; overflow-wrap: anywhere; }
    .gen-badge { margin-left: 0.4rem; color: #2F124D; }
    .doc-actions { display: flex; align-items: center; gap: 0.5rem; }
    .btn-dl { background: #faf8fc; border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.4rem 0.85rem; text-decoration: none; color: #2d2141; font-size: 0.85rem; }
    .btn-del { background: transparent; border: none; cursor: pointer; opacity: 0.6; font-size: 1rem; }
    .btn-del:hover { opacity: 1; }
    .btn-del-yes { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; border-radius: 0.5rem; padding: 0.4rem 0.7rem; cursor: pointer; font-size: 0.82rem; }
    .btn-cancel { background: #faf8fc; border: 1px solid #e8e0f0; border-radius: 0.5rem; padding: 0.4rem 0.7rem; cursor: pointer; font-size: 0.82rem; }
    .inline { display: inline; }
    .err { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; border-radius: 0.5rem; padding: 0.6rem 0.9rem; margin-bottom: 0.6rem; }

    @media (max-width: 640px) {
        .create { grid-template-columns: 1fr; }
        .doc { flex-direction: column; align-items: flex-start; }
        .doc-actions { flex-wrap: wrap; width: 100%; }
        .upload-form { flex-direction: column; align-items: stretch; }
        .upload-form input, .upload-form select, .upload-form button { width: 100%; box-sizing: border-box; }
    }
</style>
