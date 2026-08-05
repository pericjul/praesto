<script>
	import logo from "$lib/assets/praesto-logo.png";
	let { form } = $props();
</script>

<svelte:head>
	<title>Konto erstellen – Praesto</title>
</svelte:head>

<div class="reg-page">
	<div class="reg-card">
		<img src={logo} alt="Praesto" class="reg-logo" />
		<h1>Konto erstellen</h1>
		<p class="sub">Übe Bewerbungsgespräche mit deinem persönlichen KI-Coach – ganz ohne Schule.</p>
		<div class="trial-badge">✨ 7 Tage gratis testen</div>

		{#if form?.error}
			<div class="alert">⚠️ {form.error}</div>
		{/if}

		<form method="POST" class="reg-form">
			<div class="row">
				<div class="field">
					<label for="firstName">Vorname</label>
					<input id="firstName" name="firstName" type="text" value={form?.firstName ?? ""} required />
				</div>
				<div class="field">
					<label for="lastName">Nachname</label>
					<input id="lastName" name="lastName" type="text" value={form?.lastName ?? ""} required />
				</div>
			</div>

			<div class="field">
				<label for="email">E-Mail</label>
				<input id="email" name="email" type="email" placeholder="deine@email.ch" value={form?.email ?? ""} required />
			</div>

			<div class="row">
				<div class="field">
					<label for="password">Passwort</label>
					<input id="password" name="password" type="password" placeholder="mind. 8 Zeichen" required />
				</div>
				<div class="field">
					<label for="passwordConfirm">Passwort wiederholen</label>
					<input id="passwordConfirm" name="passwordConfirm" type="password" required />
				</div>
			</div>

			<label class="check">
				<input type="checkbox" name="parentConsent" />
				<span>Ich bin über 18 – oder ein Elternteil ist mit meiner Anmeldung einverstanden.</span>
			</label>

			<label class="check">
				<input type="checkbox" name="acceptTerms" />
				<span>Ich akzeptiere die <a href="/datenschutz" target="_blank" rel="noopener">Datenschutzerklärung</a> und die <a href="/agb" target="_blank" rel="noopener">AGB</a>.</span>
			</label>

			<button type="submit" class="btn-primary">Konto erstellen</button>
		</form>

		<p class="foot">Schon ein Konto? <a href="/login">Anmelden</a></p>
	</div>
</div>

<style>
	.reg-page {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 2rem 1rem;
		background: #faf8f3;
	}
	.reg-card {
		width: 100%;
		max-width: 460px;
		background: #fff;
		border: 1px solid #ece7f0;
		border-radius: 1rem;
		padding: 2rem;
		box-shadow: 0 10px 40px rgba(47, 18, 77, 0.08);
	}
	.reg-logo { width: 48px; height: 48px; object-fit: contain; }
	h1 { font-size: 1.5rem; margin: 0.5rem 0 0.25rem; color: #2d2141; }
	.sub { color: #6b647a; font-size: 0.9rem; margin: 0 0 0.75rem; }
	.trial-badge {
		display: inline-block;
		background: #f0fdfa;
		color: #0d9488;
		border: 1px solid #99f6e4;
		border-radius: 999px;
		padding: 0.25rem 0.7rem;
		font-size: 0.8rem;
		font-weight: 600;
		margin-bottom: 1.25rem;
	}
	.alert {
		background: #fef2f2;
		color: #b91c1c;
		border: 1px solid #fecaca;
		border-radius: 0.6rem;
		padding: 0.6rem 0.85rem;
		font-size: 0.85rem;
		margin-bottom: 1rem;
	}
	.reg-form { display: flex; flex-direction: column; gap: 0.85rem; }
	.row { display: flex; gap: 0.75rem; }
	.row .field { flex: 1; }
	.field { display: flex; flex-direction: column; gap: 0.3rem; }
	label { font-size: 0.85rem; color: #4b4560; font-weight: 500; }
	input[type="text"], input[type="email"], input[type="password"] {
		border: 1px solid #d8d2e0;
		border-radius: 0.55rem;
		padding: 0.6rem 0.75rem;
		font: inherit;
		width: 100%;
		box-sizing: border-box;
	}
	input:focus { outline: none; border-color: #2F124D; box-shadow: 0 0 0 3px rgba(47, 18, 77, 0.1); }
	.check {
		display: flex;
		align-items: flex-start;
		gap: 0.5rem;
		font-size: 0.82rem;
		color: #6b647a;
		font-weight: 400;
		line-height: 1.4;
		cursor: pointer;
	}
	.check input { margin-top: 0.15rem; flex-shrink: 0; }
	.check a { color: #2F124D; }
	.btn-primary {
		margin-top: 0.4rem;
		background: #2F124D;
		color: #fff;
		border: none;
		border-radius: 0.6rem;
		padding: 0.75rem;
		font: inherit;
		font-weight: 600;
		cursor: pointer;
	}
	.btn-primary:hover { background: #3f1a66; }
	.foot { text-align: center; margin: 1.25rem 0 0; font-size: 0.88rem; color: #6b647a; }
	.foot a { color: #2F124D; font-weight: 600; }
</style>
