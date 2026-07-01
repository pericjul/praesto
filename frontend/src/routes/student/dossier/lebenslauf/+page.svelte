<script>
    import { enhance } from "$app/forms";
    import { t } from "$lib/i18n";
    import RepeaterField from "$lib/components/RepeaterField.svelte";
    import PhotoCropper from "$lib/components/PhotoCropper.svelte";

    let { data, form } = $props();

    let generating = $state(false);
    let croppedPhoto = $state(null); // zugeschnittenes Foto (Blob) aus dem Cropper
    let prefillFirst = $derived(data.user?.firstName ?? "");
    let prefillLast = $derived(data.user?.lastName ?? "");

    function handle({ formData }) {
        generating = true;
        // Zugeschnittenes Foto (3:4) statt der Roh-Datei mitschicken (vor dem Absenden)
        formData.delete("photo");
        if (croppedPhoto) formData.set("photo", croppedPhoto, "foto.jpg");
        return async ({ update }) => {
            await update();
            generating = false;
        };
    }
</script>

<svelte:head>
    <title>{$t("cvform.title")} – Praesto</title>
</svelte:head>

<div class="page">
    <a href="/student/dossier" class="back">{$t("cvform.back")}</a>
    <h1>📄 {$t("cvform.title")}</h1>
    <p class="intro">{$t("cvform.intro")}</p>
    <p class="disclaimer">⚠️ {$t("dossier.disclaimer")}</p>

    {#if form?.error}<div class="err">{$t("cvform.error")}</div>{/if}
    {#if generating}<div class="generating">{$t("cvform.generating")}</div>{/if}

    <form method="POST" enctype="multipart/form-data" use:enhance={handle} class="survey" class:busy={generating}>
        <h2>{$t("cvform.sec.personal")}</h2>
        <div class="grid">
            <label><span>{$t("cvform.firstName")} *</span><input name="firstName" value={prefillFirst} required /></label>
            <label><span>{$t("cvform.lastName")} *</span><input name="lastName" value={prefillLast} required /></label>
            <label><span>{$t("cvform.street")} *</span><input name="address" required /></label>
            <label><span>{$t("cvform.zipCity")} *</span><input name="zipCity" placeholder={$t("cvform.ph.zipCity")} required /></label>
            <label><span>{$t("cvform.phone")} *</span><input name="phone" placeholder="079 123 45 67" required /></label>
            <label><span>{$t("cvform.email")} *</span><input name="email" type="email" value={data.user?.email ?? ""} required /></label>
            <label><span>{$t("cvform.birthDate")} <em>({$t("cvform.optional")})</em></span><input name="birthDate" placeholder={$t("cvform.ph.birthDate")} /></label>
            <label><span>{$t("cvform.hometown")} <em>({$t("cvform.optional")})</em></span><input name="hometown" /></label>
            <label><span>{$t("cvform.nationality")} <em>({$t("cvform.optional")})</em></span><input name="nationality" /></label>
        </div>
        <div class="photo-field">
            <span class="photo-label">{$t("cvform.photo")} <em>({$t("cvform.photoHint")})</em></span>
            <PhotoCropper onchange={(blob) => (croppedPhoto = blob)} />
        </div>

        <h2>{$t("cvform.sec.family")} <em class="opt">({$t("cvform.optional")})</em></h2>
        <RepeaterField name="parents" label={$t("cvform.parents")} hint={"(" + $t("cvform.perPerson") + ")"} placeholder={$t("cvform.ph.parents")} addLabel={$t("cvform.parentsAdd")} />
        <RepeaterField name="siblings" label={$t("cvform.siblings")} hint={"(" + $t("cvform.perPerson") + ")"} placeholder={$t("cvform.ph.siblings")} addLabel={$t("cvform.siblingsAdd")} />

        <h2>{$t("cvform.sec.job")}</h2>
        <label><span>{$t("cvform.targetJob")} *</span><input name="targetJob" placeholder={$t("cvform.ph.targetJob")} required /></label>
        <label><span>{$t("cvform.aboutMe")} <em>({$t("cvform.aboutMeHint")})</em></span><textarea name="aboutMe" rows="2" placeholder={$t("cvform.ph.aboutMe")}></textarea></label>

        <h2>{$t("cvform.sec.school")}</h2>
        <RepeaterField name="education" label={$t("cvform.schools")} hint={"(" + $t("cvform.perSchool") + ")"} placeholder={$t("cvform.ph.schools")} addLabel={$t("cvform.schoolsAdd")} />

        <h2>{$t("cvform.sec.exp")}</h2>
        <RepeaterField name="experience" label={$t("cvform.experiences")} hint={"(" + $t("cvform.perEntry") + ")"} placeholder={$t("cvform.ph.experiences")} addLabel={$t("cvform.experiencesAdd")} />

        <h2>{$t("cvform.sec.lang")}</h2>
        <RepeaterField name="languages" label={$t("cvform.langLevel")} hint={"(" + $t("cvform.perLang") + ")"} placeholder={$t("cvform.ph.languages")} addLabel={$t("cvform.langAdd")} />

        <h2>{$t("cvform.sec.more")}</h2>
        <label><span>{$t("cvform.skills")} <em>({$t("cvform.skillsHint")})</em></span><textarea name="skills" rows="2" placeholder={$t("cvform.ph.skills")}></textarea></label>
        <label><span>{$t("cvform.hobbies")}</span><textarea name="hobbies" rows="2" placeholder={$t("cvform.ph.hobbies")}></textarea></label>
        <RepeaterField name="references" label={$t("cvform.references")} hint={"(" + $t("cvform.referencesHint") + ")"} placeholder={$t("cvform.ph.references")} addLabel={$t("cvform.referencesAdd")} />

        <button type="submit" class="btn-primary" disabled={generating}>
            {generating ? $t("cvform.submitting") : $t("cvform.submit")}
        </button>
    </form>
</div>

<style>
    .page { max-width: 1000px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    .back { color: #6b647a; text-decoration: none; font-size: 0.9rem; }
    h1 { margin: 0.5rem 0 0.4rem; color: #2F124D; font-size: 1.5rem; }
    h2 { margin: 1.1rem 0 0.1rem; color: #2F124D; font-size: 1rem; border-bottom: 2px solid #ece3f5; padding-bottom: 0.3rem; }
    .intro { margin: 0 0 0.6rem; color: #6b647a; }
    .disclaimer { background: #fff7ed; border: 1px solid #fed7aa; color: #9a3412; border-radius: 0.6rem; padding: 0.7rem 0.9rem; font-size: 0.85rem; line-height: 1.45; margin: 0 0 1.25rem; }

    .survey { display: grid; gap: 0.85rem; }
    .survey.busy { opacity: 0.6; pointer-events: none; }
    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.85rem; }
    label { display: grid; gap: 0.3rem; }
    label span { font-size: 0.85rem; font-weight: 600; color: #2d2141; }
    .photo-field { display: grid; gap: 0.4rem; }
    .photo-label { font-size: 0.85rem; font-weight: 600; color: #2d2141; }
    .photo-label em { color: #9a8baf; font-weight: 400; font-style: normal; }
    label em { color: #9a8baf; font-weight: 400; font-style: normal; }
    input, textarea { border: 1px solid #e8e0f0; border-radius: 0.6rem; padding: 0.6rem 0.75rem; font: inherit; width: 100%; box-sizing: border-box; }
    input:focus, textarea:focus { outline: 2px solid #2F124D; outline-offset: 1px; }

    .btn-primary { justify-self: start; margin-top: 0.5rem; background: #2F124D; color: #fff; border: none; border-radius: 999px; padding: 0.8rem 1.8rem; font-weight: 700; font-size: 1rem; cursor: pointer; text-decoration: none; }
    .btn-primary:hover { background: #41205f; }
    .btn-primary:disabled { opacity: 0.6; cursor: default; }

    .generating { background: #f5f8ff; border: 1px solid #dbe4ff; color: #1e40af; border-radius: 0.6rem; padding: 0.85rem 1rem; margin-bottom: 0.9rem; font-weight: 600; }
    .err { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; border-radius: 0.6rem; padding: 0.7rem 1rem; margin-bottom: 0.9rem; }

    @media (max-width: 640px) { .grid { grid-template-columns: 1fr; } }
</style>
