<script>
	import logo from "$lib/assets/praesto-logo.png";
	import { t } from "$lib/i18n";
	let { data, form } = $props();
</script>

<svelte:head><title>Neues Passwort – Praesto</title></svelte:head>

<div class="pw-page">
	<div class="pw-card">
		<img src={logo} alt="Praesto" class="logo" />
		<h1>{$t('pwn.title')}</h1>

		{#if form?.done}
			<div class="ok">{$t('pwn.done')}</div>
			<a href="/login" class="btn">{$t('pwn.toLogin')}</a>
		{:else if !data.token}
			<div class="alert">{$t('pwn.invalid')}</div>
			<a href="/passwort-vergessen" class="btn">{$t('pwn.requestNew')}</a>
		{:else}
			<p class="sub">{$t('pwn.sub')}</p>
			{#if form?.error}
				<div class="alert">⚠️ {form.error}</div>
			{/if}
			<form method="POST" class="form">
				<input type="hidden" name="token" value={data.token} />
				<label>{$t('pwn.newPw')}
					<input name="newPassword" type="password" placeholder={$t('reg.pwPlaceholder')} required />
				</label>
				<label>{$t('pwn.confirm')}
					<input name="confirm" type="password" required />
				</label>
				<button type="submit" class="btn">{$t('pwn.save')}</button>
			</form>
		{/if}
	</div>
</div>

<style>
	.pw-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 2rem 1rem; background: #F8F3EB; }
	.pw-card { width: 100%; max-width: 440px; background: #fff; border: 1px solid #ece7f0; border-radius: 1rem; padding: 2rem; box-shadow: 0 10px 40px rgba(47,18,77,0.08); text-align: center; }
	.logo { width: 48px; height: 48px; object-fit: contain; }
	h1 { font-size: 1.5rem; color: #2d2141; margin: 0.5rem 0 0.5rem; }
	.sub { color: #6b647a; font-size: 0.92rem; margin: 0 0 1.25rem; }
	.alert { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; border-radius: 0.6rem; padding: 0.6rem 0.85rem; font-size: 0.85rem; margin-bottom: 1rem; }
	.ok { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; border-radius: 0.6rem; padding: 0.8rem 1rem; font-size: 0.9rem; line-height: 1.5; margin-bottom: 1.25rem; }
	.form { display: flex; flex-direction: column; gap: 0.85rem; text-align: left; }
	label { display: flex; flex-direction: column; gap: 0.3rem; font-size: 0.85rem; color: #4b4560; font-weight: 500; }
	input { border: 1px solid #d8d2e0; border-radius: 0.55rem; padding: 0.6rem 0.75rem; font: inherit; }
	input:focus { outline: none; border-color: #2F124D; box-shadow: 0 0 0 3px rgba(47,18,77,0.1); }
	.btn { display: inline-block; background: #2F124D; color: #fff; border: none; border-radius: 0.6rem; padding: 0.75rem 1.4rem; font: inherit; font-weight: 600; cursor: pointer; text-decoration: none; }
	.btn:hover { background: #4A1C74; }
</style>
