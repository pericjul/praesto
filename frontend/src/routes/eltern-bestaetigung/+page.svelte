<script>
	import logo from "$lib/assets/praesto-logo.png";
	import { t } from "$lib/i18n";
	let { data, form } = $props();
	let accepted = $state(false);
</script>

<svelte:head><title>{$t('ec.title')} – Praesto</title></svelte:head>

<div class="c-page">
	<div class="c-card">
		<img src={logo} alt="Praesto" class="logo" />

		{#if form?.done}
			<div class="ok">{$t('ec.done').replace('%NAME', form.name || '')}</div>
			<a href="/login" class="btn">{$t('ec.toLogin')}</a>
		{:else if form?.invalid || !data.token}
			<div class="alert">{$t('ec.invalid')}</div>
		{:else}
			<h1>{$t('ec.title')}</h1>
			<p class="intro">{$t('ec.intro')}</p>
			<form method="POST" class="form">
				<input type="hidden" name="token" value={data.token} />
				<label class="declare">
					<input type="checkbox" bind:checked={accepted} />
					<span>{$t('ec.declare')}</span>
				</label>
				<button type="submit" class="btn" disabled={!accepted}>{$t('ec.confirm')}</button>
			</form>
		{/if}
	</div>
</div>

<style>
	.c-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 2rem 1rem; background: #F8F3EB; }
	.c-card { width: 100%; max-width: 480px; background: #fff; border: 1px solid #ece7f0; border-radius: 1rem; padding: 2rem; box-shadow: 0 10px 40px rgba(47,18,77,0.08); text-align: center; }
	.logo { width: 44px; height: 44px; object-fit: contain; }
	h1 { font-size: 1.4rem; color: #2d2141; margin: 0.5rem 0 0.6rem; }
	.intro { color: #4b4560; line-height: 1.55; margin: 0 0 1.25rem; text-align: left; }
	.form { display: flex; flex-direction: column; gap: 1rem; }
	.declare { display: flex; align-items: flex-start; gap: 0.6rem; text-align: left; font-size: 0.9rem; color: #3e384f; line-height: 1.45; cursor: pointer; }
	.declare input { margin-top: 0.2rem; flex-shrink: 0; }
	.btn { display: inline-block; background: #2F124D; color: #fff; border: none; border-radius: 0.6rem; padding: 0.75rem 1.4rem; font: inherit; font-weight: 600; cursor: pointer; text-decoration: none; }
	.btn:hover { background: #4A1C74; }
	.btn:disabled { opacity: 0.5; cursor: default; }
	.ok { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; border-radius: 0.6rem; padding: 0.9rem 1rem; font-size: 0.95rem; line-height: 1.5; margin-bottom: 1.25rem; }
	.alert { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; border-radius: 0.6rem; padding: 0.9rem 1rem; font-size: 0.92rem; }
</style>
