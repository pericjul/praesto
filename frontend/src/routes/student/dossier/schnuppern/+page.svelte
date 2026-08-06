<script>
	import { enhance } from "$app/forms";
	import { t } from "$lib/i18n";
	let { data, form } = $props();

	let prefillName = $derived(
		[data?.user?.firstName, data?.user?.lastName].filter(Boolean).join(" ")
	);
	let loading = $state(false);
	let copied = $state("");

	function submitEnhance() {
		loading = true;
		return async ({ update }) => {
			await update({ reset: false });
			loading = false;
		};
	}

	async function copy(text, which) {
		try {
			await navigator.clipboard.writeText(text);
			copied = which;
			setTimeout(() => (copied = ""), 1500);
		} catch { /* ignore */ }
	}
</script>

<svelte:head><title>Schnupper-Anfrage – Praesto</title></svelte:head>

<div class="wrap">
	<a href="/student/dossier" class="back">{$t('schn.back')}</a>
	<h1>{$t('schn.title')}</h1>
	<p class="intro">{$t('schn.intro')}</p>

	{#if form?.error}
		<div class="alert">⚠️ {form.error}</div>
	{/if}

	<form method="POST" use:enhance={submitEnhance} class="form">
		<div class="row">
			<label>{$t('schn.beruf')}
				<input name="beruf" type="text" placeholder={$t('schn.berufPlaceholder')} value={form?.beruf ?? ""} required />
			</label>
			<label>{$t('schn.firma')}
				<input name="firma" type="text" placeholder={$t('schn.firmaPlaceholder')} />
			</label>
		</div>
		<div class="row">
			<label>{$t('schn.kontakt')}
				<input name="kontaktperson" type="text" placeholder={$t('schn.kontaktPlaceholder')} />
			</label>
			<label>{$t('schn.zeitraum')}
				<input name="zeitraum" type="text" placeholder={$t('schn.zeitraumPlaceholder')} />
			</label>
		</div>
		<div class="row">
			<label>{$t('schn.deinName')}
				<input name="deinName" type="text" value={prefillName} placeholder={$t('schn.deinNamePlaceholder')} />
			</label>
			<label>{$t('schn.klasse')}
				<input name="klasse" type="text" placeholder={$t('schn.klassePlaceholder')} />
			</label>
		</div>
		<button type="submit" class="btn" disabled={loading}>
			{loading ? $t('schn.creating') : $t('schn.create')}
		</button>
	</form>

	{#if form?.reply}
		<div class="results">
			<div class="result-card">
				<div class="result-head">
					<h2>{$t('schn.emailTitle')}</h2>
					<button type="button" class="copy" onclick={() => copy(form.reply.email, "email")}>
						{copied === "email" ? $t('schn.copied') : $t('schn.copy')}
					</button>
				</div>
				<pre>{form.reply.email}</pre>
			</div>
			<div class="result-card">
				<div class="result-head">
					<h2>{$t('schn.phoneTitle')}</h2>
					<button type="button" class="copy" onclick={() => copy(form.reply.phone, "phone")}>
						{copied === "phone" ? $t('schn.copied') : $t('schn.copy')}
					</button>
				</div>
				<pre>{form.reply.phone}</pre>
			</div>
			<p class="tip">{$t('schn.tipPre')}<a href="/student/applications">{$t('schn.tipLink')}</a>{$t('schn.tipPost')}</p>
		</div>
	{/if}
</div>

<style>
	.wrap { max-width: 820px; margin: 0 auto; padding: 1.5rem 1rem 4rem; }
	.back { color: #6b647a; font-size: 0.88rem; text-decoration: none; }
	h1 { font-size: 1.6rem; color: #2d2141; margin: 0.5rem 0 0.4rem; }
	.intro { color: #5b5470; line-height: 1.55; margin: 0 0 1.5rem; }
	.alert { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; border-radius: 0.6rem; padding: 0.6rem 0.85rem; margin-bottom: 1rem; }
	.form { display: flex; flex-direction: column; gap: 0.85rem; background: #fff; border: 1px solid #ece7f0; border-radius: 1rem; padding: 1.25rem; box-shadow: 0 8px 30px rgba(47,18,77,0.05); }
	.row { display: flex; gap: 0.85rem; flex-wrap: wrap; }
	.row label { flex: 1; min-width: 200px; }
	label { display: flex; flex-direction: column; gap: 0.3rem; font-size: 0.85rem; color: #4b4560; font-weight: 500; }
	input { border: 1px solid #d8d2e0; border-radius: 0.55rem; padding: 0.55rem 0.7rem; font: inherit; }
	input:focus { outline: none; border-color: #2F124D; box-shadow: 0 0 0 3px rgba(47,18,77,0.1); }
	.btn { align-self: flex-start; background: #0d9488; color: #fff; border: none; border-radius: 0.6rem; padding: 0.7rem 1.5rem; font-weight: 600; cursor: pointer; }
	.btn:hover { background: #0f766e; }
	.btn:disabled { opacity: 0.6; cursor: default; }
	.results { margin-top: 1.75rem; display: flex; flex-direction: column; gap: 1rem; }
	.result-card { background: #fff; border: 1px solid #ece7f0; border-radius: 1rem; padding: 1rem 1.25rem; }
	.result-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
	.result-head h2 { font-size: 1rem; color: #2d2141; margin: 0; }
	.copy { background: #f3f0f8; border: 1px solid #e0d8ec; color: #4A1C74; border-radius: 0.5rem; padding: 0.35rem 0.8rem; font-size: 0.82rem; font-weight: 600; cursor: pointer; }
	.copy:hover { background: #e8e2f0; }
	pre { white-space: pre-wrap; word-break: break-word; font: inherit; color: #3e384f; margin: 0; line-height: 1.55; }
	.tip { font-size: 0.85rem; color: #0d9488; background: #f0fdfa; border: 1px solid #99f6e4; border-radius: 0.6rem; padding: 0.6rem 0.85rem; }
	.tip a { color: #0f766e; font-weight: 600; }
</style>
