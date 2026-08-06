<script>
	import { t } from "$lib/i18n";
	import logo from "$lib/assets/praesto-logo.png";

	let features = $derived([
		{ t: $t("priv.f1t"), x: $t("priv.f1x") },
		{ t: $t("priv.f2t"), x: $t("priv.f2x") },
		{ t: $t("priv.f3t"), x: $t("priv.f3x") },
		{ t: $t("priv.f4t"), x: $t("priv.f4x") }
	]);
	function splitIcon(title) {
		const p = title.trim().split(" ");
		return { icon: p[0], label: p.slice(1).join(" ") };
	}
</script>

<svelte:head>
	<title>Praesto – {$t('priv.title')}</title>
	<meta name="description" content={$t('priv.sub')} />
	<link rel="canonical" href="https://praesto.ch/privat" />
</svelte:head>

<div class="pl">
	<section class="hero">
		<img src={logo} alt="Praesto" class="logo" />
		<span class="eyebrow">{$t('priv.eyebrow')}</span>
		<h1>{$t('priv.title')}</h1>
		<p class="lead">{$t('priv.sub')}</p>
		<div class="trial">{$t('priv.trial')}</div>
		<div class="cta">
			<a href="/register" class="btn-primary">{$t('priv.cta')}</a>
			<a href="/login" class="btn-ghost">{$t('priv.login')}</a>
		</div>
		<p class="parent-note">{$t('priv.parentNote')}</p>
	</section>

	<section class="features">
		{#each features as f}
			{@const fi = splitIcon(f.t)}
			<article class="fcard">
				<span class="f-icon">{fi.icon}</span>
				<h3>{fi.label}</h3>
				<p>{f.x}</p>
			</article>
		{/each}
	</section>

	<section class="pricing">
		<h2>{$t('priv.priceTitle')}</h2>
		<div class="plans">
			<div class="plan">
				<div class="p-name">{$t('abo.monthly')}</div>
				<div class="p-price">CHF 4.90<span>{$t('abo.perMonth')}</span></div>
				<div class="p-note">{$t('abo.monthlyNote')}</div>
			</div>
			<div class="plan featured">
				<div class="badge">{$t('abo.cheaper')}</div>
				<div class="p-name">{$t('abo.yearly')}</div>
				<div class="p-price">CHF 49<span>{$t('abo.perYear')}</span></div>
				<div class="p-note">{$t('abo.yearlyNote')}</div>
			</div>
		</div>
		<a href="/register" class="btn-primary big">{$t('priv.cta')} – {$t('priv.trial')}</a>
	</section>

	<p class="back"><span>{$t('priv.backSchool')}</span> <a href="/">{$t('priv.backSchoolLink')}</a></p>
</div>

<style>
	.pl { max-width: 900px; margin: 0 auto; padding: 2rem 1rem 4rem; }
	.hero { text-align: center; padding: 1.5rem 0 2rem; }
	.logo { width: 56px; height: 56px; object-fit: contain; }
	.eyebrow { display: block; text-transform: uppercase; letter-spacing: 0.05em; font-size: 0.78rem; font-weight: 700; color: #7c3aed; margin: 0.6rem 0 0.3rem; }
	h1 { font-size: 2rem; color: #2d2141; margin: 0 0 0.6rem; }
	.lead { color: #5b5470; font-size: 1.05rem; line-height: 1.55; max-width: 560px; margin: 0 auto 1rem; }
	.trial { display: inline-block; background: #f0fdfa; color: #0d9488; border: 1px solid #99f6e4; border-radius: 999px; padding: 0.3rem 0.9rem; font-weight: 600; font-size: 0.9rem; margin-bottom: 1.25rem; }
	.cta { display: flex; gap: 0.75rem; justify-content: center; flex-wrap: wrap; }
	.btn-primary { background: #2F124D; color: #fff; text-decoration: none; border-radius: 0.7rem; padding: 0.8rem 1.8rem; font-weight: 600; display: inline-block; }
	.btn-primary:hover { background: #4A1C74; }
	.btn-ghost { background: #fff; color: #2F124D; border: 1px solid #d8cfe6; text-decoration: none; border-radius: 0.7rem; padding: 0.8rem 1.6rem; font-weight: 600; }
	.parent-note { font-size: 0.82rem; color: #8b849a; margin: 1rem 0 0; }

	.features { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin: 1rem 0 3rem; }
	.fcard { background: #fff; border: 1px solid #ece7f0; border-radius: 1rem; padding: 1.25rem; }
	.f-icon { font-size: 1.6rem; }
	.fcard h3 { font-size: 1rem; color: #2d2141; margin: 0.5rem 0 0.35rem; }
	.fcard p { color: #6b647a; font-size: 0.9rem; line-height: 1.5; margin: 0; }

	.pricing { text-align: center; background: #faf8fd; border: 1px solid #ece7f0; border-radius: 1.25rem; padding: 2rem 1.5rem; }
	.pricing h2 { color: #2d2141; font-size: 1.4rem; margin: 0 0 1.25rem; }
	.plans { display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap; margin-bottom: 1.5rem; }
	.plan { position: relative; background: #fff; border: 1px solid #e9e3f0; border-radius: 1rem; padding: 1.5rem 1.75rem; min-width: 200px; }
	.plan.featured { border-color: #2F124D; }
	.badge { position: absolute; top: -0.7rem; left: 50%; transform: translateX(-50%); background: #0d9488; color: #fff; font-size: 0.72rem; font-weight: 700; padding: 0.2rem 0.7rem; border-radius: 999px; }
	.p-name { font-weight: 600; color: #2d2141; }
	.p-price { font-size: 2rem; font-weight: 800; color: #2F124D; }
	.p-price span { font-size: 0.85rem; font-weight: 500; color: #8b849a; }
	.p-note { font-size: 0.82rem; color: #8b849a; }
	.big { padding: 0.9rem 2rem; }

	.back { text-align: center; margin: 2rem 0 0; font-size: 0.9rem; color: #6b647a; }
	.back a { color: #2F124D; font-weight: 600; text-decoration: none; }
</style>
