<script>
	import logo from "$lib/assets/praesto-logo.png";
	let { data, form } = $props();

	let status = $derived(data?.status ?? {});
	let hasSub = $derived(!!status?.subscriptionEndsAt);

	function fmt(d) {
		if (!d) return "";
		try { return new Date(d).toLocaleDateString("de-CH", { day: "2-digit", month: "2-digit", year: "numeric" }); }
		catch { return ""; }
	}
	let trialActive = $derived(status?.trialActive === true);
</script>

<svelte:head><title>Abo – Praesto</title></svelte:head>

<div class="abo-page">
	<div class="abo-inner">
		<img src={logo} alt="Praesto" class="logo" />
		<h1>Praesto weiter nutzen</h1>

		{#if trialActive}
			<p class="lead">Deine Gratis-Testphase läuft noch bis <strong>{fmt(status.trialEndsAt)}</strong>. Sichere dir den vollen Zugang:</p>
		{:else if hasSub}
			<p class="lead">Dein Zugang ist aktiv bis <strong>{fmt(status.subscriptionEndsAt)}</strong>.</p>
		{:else}
			<p class="lead">Deine Testphase ist abgelaufen. Wähle, wie du Praesto weiter nutzen möchtest:</p>
		{/if}

		{#if form?.error}
			<div class="alert">⚠️ {form.error}</div>
		{/if}

		<div class="plans">
			<form method="POST" action="?/checkout" class="plan">
				<input type="hidden" name="plan" value="monthly" />
				<div class="plan-name">Monatlich</div>
				<div class="plan-price">CHF 4.90<span>/Monat</span></div>
				<div class="plan-note">Jederzeit kündbar</div>
				<button type="submit" class="btn">Monatsabo wählen</button>
			</form>

			<form method="POST" action="?/checkout" class="plan featured">
				<input type="hidden" name="plan" value="yearly" />
				<div class="badge">Günstiger</div>
				<div class="plan-name">Jährlich</div>
				<div class="plan-price">CHF 49<span>/Jahr</span></div>
				<div class="plan-note">Spart über 40% gegenüber monatlich</div>
				<button type="submit" class="btn btn-primary">Jahresabo wählen</button>
			</form>
		</div>

		<div class="school-box">
			<strong>🏫 Deine Schule zahlt vielleicht mit!</strong>
			<p>Sag deiner Lehrperson oder Schule von Praesto – oft übernimmt die Schule die Kosten für die ganze Klasse. Die Schule darf sich einfach bei uns melden:</p>
			<a href="mailto:info@praesto.ch?subject=Praesto%20f%C3%BCr%20unsere%20Schule">info@praesto.ch</a>
		</div>

		{#if hasSub}
			<form method="POST" action="?/portal" class="portal">
				<button type="submit" class="link-btn">Abo verwalten / kündigen</button>
			</form>
		{/if}

		<p class="foot"><a href="/login">Zurück zum Login</a></p>
	</div>
</div>

<style>
	.abo-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 2rem 1rem; background: #F8F3EB; }
	.abo-inner { width: 100%; max-width: 640px; text-align: center; }
	.logo { width: 52px; height: 52px; object-fit: contain; }
	h1 { font-size: 1.7rem; margin: 0.5rem 0 0.5rem; color: #2d2141; }
	.lead { color: #5b5470; margin: 0 auto 1.5rem; max-width: 480px; }
	.alert { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; border-radius: 0.6rem; padding: 0.6rem 0.85rem; font-size: 0.9rem; margin-bottom: 1rem; }
	.plans { display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap; }
	.plan { position: relative; flex: 1; min-width: 220px; max-width: 280px; background: #fff; border: 1px solid #e9e3f0; border-radius: 1rem; padding: 1.5rem 1.25rem; display: flex; flex-direction: column; gap: 0.4rem; box-shadow: 0 8px 30px rgba(47,18,77,0.06); }
	.plan.featured { border-color: #2F124D; }
	.badge { position: absolute; top: -0.7rem; left: 50%; transform: translateX(-50%); background: #0d9488; color: #fff; font-size: 0.72rem; font-weight: 700; padding: 0.2rem 0.7rem; border-radius: 999px; }
	.plan-name { font-weight: 600; color: #2d2141; }
	.plan-price { font-size: 1.9rem; font-weight: 800; color: #2F124D; }
	.plan-price span { font-size: 0.85rem; font-weight: 500; color: #8b849a; }
	.plan-note { font-size: 0.8rem; color: #8b849a; min-height: 2.2em; }
	.btn { margin-top: 0.5rem; border: 1px solid #2F124D; background: #fff; color: #2F124D; border-radius: 0.6rem; padding: 0.7rem; font: inherit; font-weight: 600; cursor: pointer; }
	.btn.btn-primary { background: #2F124D; color: #fff; }
	.btn:hover { filter: brightness(1.05); }
	.school-box { background: #faf8fd; border: 1px solid #e6dcf2; border-radius: 1rem; padding: 1.1rem 1.25rem; margin: 1.75rem 0 0; }
	.school-box strong { display: block; color: #2d2141; margin-bottom: 0.35rem; }
	.school-box p { font-size: 0.88rem; color: #6b647a; margin: 0 0 0.5rem; }
	.school-box a { color: #2F124D; font-weight: 600; }
	.portal { margin-top: 1.25rem; }
	.link-btn { background: none; border: none; color: #8b849a; text-decoration: underline; cursor: pointer; font: inherit; font-size: 0.85rem; }
	.foot { margin-top: 1.5rem; font-size: 0.88rem; }
	.foot a { color: #6b647a; }
</style>
