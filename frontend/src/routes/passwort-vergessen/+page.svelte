<script>
	import logo from "$lib/assets/praesto-logo.png";
	let { form } = $props();
</script>

<svelte:head><title>Passwort vergessen – Praesto</title></svelte:head>

<div class="pw-page">
	<div class="pw-card">
		<img src={logo} alt="Praesto" class="logo" />
		<h1>Passwort vergessen?</h1>

		{#if form?.sent}
			<div class="ok">✓ Falls ein Konto mit dieser E-Mail existiert, haben wir dir einen Link zum Zurücksetzen geschickt. Schau in dein Postfach (auch im Spam-Ordner).</div>
			<a href="/login" class="btn">Zurück zum Login</a>
		{:else}
			<p class="sub">Gib deine E-Mail-Adresse ein. Wir schicken dir einen Link, mit dem du ein neues Passwort setzen kannst.</p>
			{#if form?.error}
				<div class="alert">⚠️ {form.error}</div>
			{/if}
			<form method="POST" class="form">
				<label>E-Mail
					<input name="email" type="email" placeholder="deine@email.ch" required />
				</label>
				<button type="submit" class="btn">Link senden</button>
			</form>
			<p class="foot"><a href="/login">Zurück zum Login</a></p>
		{/if}
	</div>
</div>

<style>
	.pw-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 2rem 1rem; background: #F8F3EB; }
	.pw-card { width: 100%; max-width: 440px; background: #fff; border: 1px solid #ece7f0; border-radius: 1rem; padding: 2rem; box-shadow: 0 10px 40px rgba(47,18,77,0.08); text-align: center; }
	.logo { width: 48px; height: 48px; object-fit: contain; }
	h1 { font-size: 1.5rem; color: #2d2141; margin: 0.5rem 0 0.5rem; }
	.sub { color: #6b647a; font-size: 0.92rem; margin: 0 0 1.25rem; line-height: 1.55; }
	.alert { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; border-radius: 0.6rem; padding: 0.6rem 0.85rem; font-size: 0.85rem; margin-bottom: 1rem; }
	.ok { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; border-radius: 0.6rem; padding: 0.8rem 1rem; font-size: 0.9rem; line-height: 1.5; margin-bottom: 1.25rem; }
	.form { display: flex; flex-direction: column; gap: 0.85rem; text-align: left; }
	label { display: flex; flex-direction: column; gap: 0.3rem; font-size: 0.85rem; color: #4b4560; font-weight: 500; }
	input { border: 1px solid #d8d2e0; border-radius: 0.55rem; padding: 0.6rem 0.75rem; font: inherit; }
	input:focus { outline: none; border-color: #2F124D; box-shadow: 0 0 0 3px rgba(47,18,77,0.1); }
	.btn { display: inline-block; background: #2F124D; color: #fff; border: none; border-radius: 0.6rem; padding: 0.75rem 1.4rem; font: inherit; font-weight: 600; cursor: pointer; text-decoration: none; }
	.btn:hover { background: #4A1C74; }
	.foot { margin: 1.25rem 0 0; font-size: 0.88rem; }
	.foot a { color: #6b647a; }
</style>
