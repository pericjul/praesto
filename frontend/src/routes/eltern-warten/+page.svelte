<script>
	import logo from "$lib/assets/praesto-logo.png";
	import { t } from "$lib/i18n";
	let { data, form } = $props();
	let email = $derived(data?.parentEmail ?? "");
</script>

<svelte:head><title>{$t('ew.title')} – Praesto</title></svelte:head>

<div class="w-page">
	<div class="w-card">
		<img src={logo} alt="Praesto" class="logo" />
		<div class="mail-ico">📨</div>
		<h1>{$t('ew.title')}</h1>
		<p class="lead">{$t('ew.text1').replace('%EMAIL', email)}</p>
		<p class="note">{$t('ew.text2')}</p>

		{#if form?.resent}
			<div class="ok">{$t('ew.resent')}</div>
		{/if}

		<div class="actions">
			<a href="/eltern-warten" class="btn-primary" data-sveltekit-reload>{$t('ew.reload')}</a>
			<form method="POST" action="?/resend">
				<button type="submit" class="btn-ghost">{$t('ew.resend')}</button>
			</form>
		</div>
		<form method="POST" action="/logout" class="foot">
			<button type="submit" class="link-btn">{$t('ew.logout')}</button>
		</form>
	</div>
</div>

<style>
	.w-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 2rem 1rem; background: #F8F3EB; }
	.w-card { width: 100%; max-width: 460px; background: #fff; border: 1px solid #ece7f0; border-radius: 1rem; padding: 2rem; box-shadow: 0 10px 40px rgba(47,18,77,0.08); text-align: center; }
	.logo { width: 44px; height: 44px; object-fit: contain; }
	.mail-ico { font-size: 2.4rem; margin: 0.4rem 0; }
	h1 { font-size: 1.4rem; color: #2d2141; margin: 0 0 0.6rem; }
	.lead { color: #4b4560; line-height: 1.55; margin: 0 0 0.6rem; }
	.note { color: #6b647a; font-size: 0.9rem; margin: 0 0 1.25rem; }
	.ok { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; border-radius: 0.6rem; padding: 0.6rem 0.85rem; font-size: 0.88rem; margin-bottom: 1rem; }
	.actions { display: flex; gap: 0.6rem; justify-content: center; flex-wrap: wrap; }
	.btn-primary { display: inline-block; background: #2F124D; color: #fff; border: none; border-radius: 0.6rem; padding: 0.65rem 1.3rem; font: inherit; font-weight: 600; cursor: pointer; text-decoration: none; }
	.btn-primary:hover { background: #4A1C74; }
	.btn-ghost { background: #f3f0f8; color: #4b4560; border: 1px solid #e0d8ec; border-radius: 0.6rem; padding: 0.65rem 1.2rem; font: inherit; font-weight: 600; cursor: pointer; }
	.foot { margin: 1.25rem 0 0; font-size: 0.85rem; }
	.link-btn { background: none; border: none; color: #9a93a8; text-decoration: underline; cursor: pointer; font: inherit; font-size: 0.85rem; }
</style>
