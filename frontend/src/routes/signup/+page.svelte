<script>
    import logo from "$lib/assets/praesto-logo.png";
    import { t } from "$lib/i18n";
</script>

<svelte:head>
    <title>{$t('signup.headTitle')}</title>
</svelte:head>

<div class="signup-page">
    <div class="info-card">
        <img src={logo} alt="Praesto Logo" class="brand-logo" />
        <h1>{$t('signup.title')}</h1>
        <p>
            {$t('signup.intro')}
        </p>
        <ul class="info-list">
            <li>👩‍🏫 <strong>{$t('signup.teachersLabel')}</strong> {$t('signup.teachersLine')}</li>
            <li>🎓 <strong>{$t('signup.studentsLabel')}</strong> {$t('signup.studentsLine')}</li>
            <li>🏫 <strong>{$t('signup.adminsLabel')}</strong> {$t('signup.adminsLine')}</li>
        </ul>
        <p class="hint">
            {$t('signup.hint')}
        </p>
        <div class="actions">
            <a href="/login" class="btn-primary">{$t('signup.toLogin')}</a>
            <a href="/demo" class="btn-ghost">{$t('signup.viewDemo')}</a>
        </div>
    </div>
</div>

<style>
    .signup-page {
        position: fixed;
        inset: 0;
        background: #F8F3EB;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 1rem;
        z-index: 1000;
    }

    .info-card {
        max-width: 460px;
        width: 100%;
        background: #fff;
        border-radius: 1.5rem;
        box-shadow: 0 20px 60px rgba(47, 18, 77, 0.15);
        padding: 2.5rem;
        text-align: center;
    }

    .brand-logo {
        width: 80px;
        height: auto;
        margin-bottom: 1rem;
    }

    h1 {
        margin: 0 0 0.75rem;
        font-size: 1.4rem;
        color: #2F124D;
    }

    p {
        color: #6b647a;
        margin: 0 0 1rem;
    }

    .info-list {
        list-style: none;
        padding: 0;
        margin: 0 0 1.25rem;
        text-align: left;
        display: flex;
        flex-direction: column;
        gap: 0.6rem;
    }

    .info-list li {
        background: #faf8fc;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        padding: 0.6rem 0.85rem;
        font-size: 0.92rem;
        color: #2d2141;
    }

    .hint {
        font-size: 0.88rem;
    }

    .actions {
        display: flex;
        gap: 0.75rem;
        justify-content: center;
        margin-top: 0.5rem;
    }

    .btn-primary,
    .btn-ghost {
        padding: 0.7rem 1.4rem;
        border-radius: 0.5rem;
        font-weight: 600;
        text-decoration: none;
        font-size: 0.95rem;
    }

    .btn-primary {
        background: #2F124D;
        color: #fff;
    }

    .btn-ghost {
        background: transparent;
        border: 1px solid #2F124D;
        color: #2F124D;
    }
</style>
