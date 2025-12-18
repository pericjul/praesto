<script>
    import { enhance } from '$app/forms';
    import logo from "$lib/assets/praesto-logo.png";
    
    let { form } = $props();
</script>

<svelte:head>
    <title>Registrieren – Praesto</title>
</svelte:head>

<div class="signup-page">
    <div class="signup-container">
        <div class="signup-branding">
            <div class="branding-content">
                <img src={logo} alt="Praesto Logo" class="brand-logo" />
                <h1>Praesto</h1>
                <p>Starte jetzt mit deinem persönlichen Bewerbungscoach</p>
                
                <div class="features">
                    <div class="feature">
                        <span class="feature-icon">✨</span>
                        <span>Kostenlos starten</span>
                    </div>
                    <div class="feature">
                        <span class="feature-icon">🎯</span>
                        <span>Personalisiertes Training</span>
                    </div>
                    <div class="feature">
                        <span class="feature-icon">📈</span>
                        <span>Verfolge deinen Fortschritt</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="signup-form-section">
            <div class="signup-card">
                <h2>Konto erstellen 🚀</h2>
                <p class="signup-subtitle">Registriere dich und leg los</p>

                {#if form?.error}
                    <div class="alert alert-danger">
                        ⚠️ {form.error}
                    </div>
                {/if}

                {#if form?.success}
                    <div class="alert alert-success">
                        ✓ Konto erstellt! Du kannst dich jetzt anmelden.
                    </div>
                {/if}

                <form method="POST" action="?/signup" class="signup-form" use:enhance>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="firstName">Vorname</label>
                            <input
                                type="text"
                                id="firstName"
                                name="firstName"
                                placeholder="Max"
                            />
                        </div>
                        <div class="form-group">
                            <label for="lastName">Nachname</label>
                            <input
                                type="text"
                                id="lastName"
                                name="lastName"
                                placeholder="Muster"
                            />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="email">E-Mail *</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            placeholder="max.muster@schule.ch"
                            required
                        />
                    </div>

                    <div class="form-group">
                        <label for="password">Passwort *</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            placeholder="Mind. 8 Zeichen"
                            required
                        />
                    </div>

                    <button type="submit" class="btn-signup">
                        Registrieren
                    </button>
                </form>

                <div class="signup-footer">
                    <span>Bereits ein Konto?</span>
                    <a href="/login">Jetzt anmelden</a>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
    .signup-page {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: #F8F3EB;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 1rem;
        overflow: hidden;
        z-index: 1000;
    }

    .signup-container {
        display: flex;
        max-width: 900px;
        width: 100%;
        background: #fff;
        border-radius: 1.5rem;
        box-shadow: 0 20px 60px rgba(47, 18, 77, 0.15);
        overflow: hidden;
        max-height: 90vh;
    }

    .signup-branding {
        flex: 1;
        background: linear-gradient(180deg, #c97d3c 0%, #5a2d6e 50%, #2F124D 100%);
        padding: 3rem;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
    }

    .branding-content {
        text-align: center;
    }

    .brand-logo {
        width: 100px;
        height: auto;
        margin-bottom: 1rem;
        object-fit: contain;
    }

    .branding-content h1 {
        margin: 0 0 0.5rem;
        font-size: 2rem;
        font-weight: 800;
        letter-spacing: 0.02em;
    }

    .branding-content p {
        margin: 0 0 2rem;
        opacity: 0.9;
        font-size: 1rem;
    }

    .features {
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
        text-align: left;
    }

    .feature {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 0.5rem 1rem;
        background: rgba(255, 255, 255, 0.1);
        border-radius: 0.5rem;
        font-size: 0.9rem;
    }

    .feature-icon {
        font-size: 1.2rem;
    }

    .signup-form-section {
        flex: 1;
        padding: 3rem;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .signup-card {
        width: 100%;
        max-width: 340px;
    }

    .signup-card h2 {
        margin: 0 0 0.25rem;
        font-size: 1.5rem;
        color: #2F124D;
        font-weight: 700;
    }

    .signup-subtitle {
        margin: 0 0 1.5rem;
        color: #6b647a;
        font-size: 0.95rem;
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

    .alert-success {
        background: #f0fdf4;
        color: #16a34a;
        border: 1px solid #bbf7d0;
    }

    .signup-form {
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

    .form-group label {
        font-size: 0.9rem;
        font-weight: 500;
        color: #2d2141;
    }

    .form-group input {
        padding: 0.75rem 1rem;
        border: 1px solid #e8e0f0;
        border-radius: 0.5rem;
        font-size: 1rem;
        background: #faf8fc;
        transition: all 0.15s;
    }

    .form-group input:focus {
        outline: none;
        border-color: #2F124D;
        background: #fff;
        box-shadow: 0 0 0 3px rgba(47, 18, 77, 0.1);
    }

    .form-group input::placeholder {
        color: #9a8b9d;
    }

    .btn-signup {
        margin-top: 0.5rem;
        padding: 0.85rem 1.5rem;
        background: #2F124D;
        color: white;
        border: none;
        border-radius: 0.5rem;
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.15s;
    }

    .btn-signup:hover {
        background: #4A1C74;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(47, 18, 77, 0.3);
    }

    .signup-footer {
        margin-top: 1.5rem;
        text-align: center;
        font-size: 0.9rem;
        color: #6b647a;
    }

    .signup-footer a {
        color: #2F124D;
        font-weight: 600;
        text-decoration: none;
        margin-left: 0.25rem;
    }

    .signup-footer a:hover {
        text-decoration: underline;
    }

    @media (max-width: 700px) {
        .signup-container {
            flex-direction: column;
        }

        .signup-branding {
            padding: 2rem;
        }

        .branding-content h1 {
            font-size: 1.5rem;
        }

        .features {
            display: none;
        }

        .signup-form-section {
            padding: 2rem;
        }

        .form-row {
            flex-direction: column;
            gap: 1rem;
        }
    }
</style>
