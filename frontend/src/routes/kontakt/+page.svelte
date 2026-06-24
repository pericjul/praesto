<script>
    import { enhance } from "$app/forms";
    import { t } from "$lib/i18n";

    let { form } = $props();
</script>

<svelte:head>
    <title>{$t('contact.title')} – Praesto</title>
</svelte:head>

<div class="contact-page">
    <div class="contact-card">
        <h1>{$t('contact.title')}</h1>
        <p class="lead">{$t('contact.subtitle')}</p>

        {#if form?.success}
            <div class="alert success">✓ {$t('contact.success')}</div>
            <a href="/" class="btn-secondary">← Praesto</a>
        {:else}
            {#if form?.error}
                <div class="alert error">⚠️ {$t('contact.error')}</div>
            {/if}

            <form method="POST" use:enhance class="contact-form">
                <div class="row">
                    <label>
                        {$t('contact.name')} *
                        <input name="name" type="text" required />
                    </label>
                    <label>
                        {$t('contact.email')} *
                        <input name="email" type="email" required />
                    </label>
                </div>

                <div class="row">
                    <label>
                        {$t('contact.org')}
                        <input name="organisation" type="text" />
                    </label>
                    <label>
                        {$t('contact.role')}
                        <input name="role" type="text" />
                    </label>
                </div>

                <label>
                    {$t('contact.interest')}
                    <select name="interest">
                        <option value={$t('contact.iGeneral')}>{$t('contact.iGeneral')}</option>
                        <option value={$t('contact.iPilot')}>{$t('contact.iPilot')}</option>
                        <option value={$t('contact.iLicense')}>{$t('contact.iLicense')}</option>
                        <option value={$t('contact.iInfo')}>{$t('contact.iInfo')}</option>
                    </select>
                </label>

                <div class="row">
                    <label>
                        {$t('contact.classes')}
                        <input name="classes" type="number" min="0" />
                    </label>
                    <label>
                        {$t('contact.students')}
                        <input name="students" type="number" min="0" />
                    </label>
                </div>

                <label class="checkbox">
                    <input name="wantsMeeting" type="checkbox" />
                    <span>{$t('contact.meeting')}</span>
                </label>

                <label>
                    {$t('contact.message')}
                    <textarea name="message" rows="4"></textarea>
                </label>

                <button type="submit" class="btn-primary">{$t('contact.submit')}</button>
                <p class="privacy">{$t('contact.privacy')}</p>
            </form>
        {/if}
    </div>
</div>

<style>
    .contact-page {
        max-width: 680px;
        margin: 0 auto;
        padding: 2rem 1rem 3rem;
    }

    .contact-card {
        background: var(--color-bg-card, #fff);
        border: 1px solid var(--color-border, #e6d9cc);
        border-radius: 1.25rem;
        padding: 2rem;
    }

    h1 {
        margin: 0 0 0.5rem;
        color: var(--color-primary, #2F124D);
        font-size: 1.6rem;
    }

    .lead {
        margin: 0 0 1.5rem;
        color: var(--color-text-muted, #5E4C6F);
    }

    .contact-form {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .row {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 1rem;
    }

    label {
        display: flex;
        flex-direction: column;
        gap: 0.35rem;
        font-size: 0.9rem;
        font-weight: 500;
        color: var(--color-text-secondary, #2d2141);
    }

    input,
    select,
    textarea {
        padding: 0.65rem 0.85rem;
        border: 1px solid var(--color-border-input, #e8e0f0);
        border-radius: 0.5rem;
        background: var(--color-bg-input, #faf8fc);
        font-size: 1rem;
        font-family: inherit;
        font-weight: 400;
    }

    input:focus,
    select:focus,
    textarea:focus {
        outline: none;
        border-color: var(--color-primary, #2F124D);
        background: #fff;
    }

    .checkbox {
        flex-direction: row;
        align-items: center;
        gap: 0.6rem;
        font-weight: 400;
    }

    .checkbox input {
        width: auto;
    }

    .btn-primary {
        margin-top: 0.5rem;
        padding: 0.85rem 1.5rem;
        background: var(--color-primary, #2F124D);
        color: #fff;
        border: none;
        border-radius: 0.5rem;
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
    }

    .btn-primary:hover {
        background: var(--color-primary-hover, #4A1C74);
    }

    .btn-secondary {
        display: inline-block;
        margin-top: 1rem;
        color: var(--color-primary, #2F124D);
        font-weight: 600;
        text-decoration: none;
    }

    .privacy {
        margin: 0;
        font-size: 0.8rem;
        color: var(--color-text-light, #6b647a);
        text-align: center;
    }

    .alert {
        padding: 0.75rem 1rem;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
        font-size: 0.95rem;
    }

    .alert.success {
        background: var(--color-success-bg, #f0fdf4);
        color: #166534;
        border: 1px solid var(--color-success-border, #bbf7d0);
    }

    .alert.error {
        background: #fef2f2;
        color: #dc2626;
        border: 1px solid #fecaca;
    }

    @media (max-width: 560px) {
        .row {
            grid-template-columns: 1fr;
        }
    }
</style>
