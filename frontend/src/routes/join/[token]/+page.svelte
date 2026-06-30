<script>
    import { enhance } from "$app/forms";
    import logo from "$lib/assets/praesto-logo.png";
    import { t } from "$lib/i18n";

    let { data, form } = $props();

    let invite = $derived(data.invite);

    let contextTitle = $derived.by(() => {
        if (!invite) return "";
        const school = invite.schoolName ?? $t("join.defaultSchool");
        switch (invite.type) {
            case "SCHOOL_ADMIN":
                return `${$t("join.ctxAdminPrefix")} ${school}`;
            case "TEACHER":
                return `${$t("join.ctxTeacherPrefix")} ${school}`;
            case "CLASS":
                return `${$t("join.ctxClassPrefix")} ${invite.className ?? ""} ${$t("join.ctxClassMid")} ${school} ${$t("join.ctxClassSuffix")}`.trim();
            default:
                return `${$t("join.ctxDefaultPrefix")} ${school}`;
        }
    });
</script>

<svelte:head>
    <title>{$t('join.headTitle')}</title>
</svelte:head>

<div class="join-page">
    <div class="join-card">
        <img src={logo} alt="Praesto Logo" class="brand-logo" />

        {#if data.invalid}
            <h1>{$t('join.invalidTitle')}</h1>
            <p class="error-text">
                {$t('join.invalidPrefix')}
                <a href="mailto:info@praesto.ch">info@praesto.ch</a>.
            </p>
            <a href="/login" class="btn-secondary">{$t('join.toLogin')}</a>
        {:else}
            <p class="context">{contextTitle}</p>
            <h1>{$t('join.create')}</h1>

            {#if form?.error}
                <div class="alert alert-danger">⚠️ {form.error}</div>
            {/if}

            <form method="POST" use:enhance class="join-form">
                <div class="form-row">
                    <div class="form-group">
                        <label for="firstName">{$t('join.firstName')}</label>
                        <input id="firstName" name="firstName" type="text" required placeholder="Max" />
                    </div>
                    <div class="form-group">
                        <label for="lastName">{$t('join.lastName')}</label>
                        <input id="lastName" name="lastName" type="text" required placeholder="Muster" />
                    </div>
                </div>

                <div class="form-group">
                    <label for="email">{$t('join.email')}</label>
                    <input id="email" name="email" type="email" required placeholder="max.muster@schule.ch" />
                </div>

                <div class="form-group">
                    <label for="password">{$t('join.password')}</label>
                    <input id="password" name="password" type="password" required placeholder={$t('join.passwordPlaceholder')} />
                </div>

                <div class="form-group">
                    <label for="passwordConfirm">{$t('join.passwordConfirm')}</label>
                    <input id="passwordConfirm" name="passwordConfirm" type="password" required placeholder={$t('join.passwordConfirmPlaceholder')} />
                </div>

                <button type="submit" class="btn-primary">{$t('join.submit')}</button>
            </form>

            <p class="footer-text">
                {$t('join.haveAccount')} <a href="/login">{$t('join.signin')}</a>
            </p>
        {/if}
    </div>
</div>

<style>
    .join-page {
        position: fixed;
        inset: 0;
        background: #F8F3EB;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 1rem;
        overflow: auto;
        z-index: 1000;
    }

    .join-card {
        max-width: 420px;
        width: 100%;
        background: #fff;
        border-radius: 1.5rem;
        box-shadow: 0 20px 60px rgba(47, 18, 77, 0.15);
        padding: 2.5rem;
    }

    .brand-logo {
        width: 70px;
        height: auto;
        display: block;
        margin: 0 auto 1rem;
    }

    .context {
        text-align: center;
        color: #c97d3c;
        font-weight: 600;
        margin: 0 0 0.25rem;
        font-size: 0.95rem;
    }

    h1 {
        text-align: center;
        margin: 0 0 1.25rem;
        font-size: 1.5rem;
        color: #2F124D;
    }

    .alert {
        padding: 0.75rem 1rem;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
        font-size: 0.9rem;
    }

    .alert-danger {
        background: #fef2f2;
        color: #dc2626;
        border: 1px solid #fecaca;
    }

    .error-text {
        color: #6b647a;
        text-align: center;
        margin: 0 0 1.5rem;
    }

    .join-form {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .form-row {
        display: flex;
        gap: 0.75rem;
    }

    .form-row .form-group {
        flex: 1;
    }

    .form-group {
        display: flex;
        flex-direction: column;
        gap: 0.4rem;
    }

    label {
        font-size: 0.9rem;
        font-weight: 500;
        color: #2d2141;
    }

    input {
        padding: 0.75rem 1rem;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        font-size: 1rem;
        background: #faf8fc;
    }

    input:focus {
        outline: none;
        border-color: #2F124D;
        background: #fff;
        box-shadow: 0 0 0 3px rgba(47, 18, 77, 0.1);
    }

    .btn-primary {
        margin-top: 0.5rem;
        padding: 0.85rem 1.5rem;
        background: #2F124D;
        color: #fff;
        border: none;
        border-radius: 0.5rem;
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
    }

    .btn-primary:hover {
        background: #4A1C74;
    }

    .btn-secondary {
        display: inline-block;
        margin-top: 0.5rem;
        padding: 0.7rem 1.4rem;
        border: 1px solid #2F124D;
        color: #2F124D;
        border-radius: 0.5rem;
        text-decoration: none;
        font-weight: 600;
    }

    .footer-text {
        margin-top: 1.5rem;
        text-align: center;
        font-size: 0.9rem;
        color: #6b647a;
    }

    .footer-text a,
    .error-text a {
        color: #2F124D;
        font-weight: 600;
    }
</style>
