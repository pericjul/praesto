<script>
	import { t } from "$lib/i18n";
	import logo from "$lib/assets/praesto-logo.png";

	let faqs = $derived([
		{ q: $t("phelp.q1"), a: $t("phelp.a1") },
		{ q: $t("phelp.q2"), a: $t("phelp.a2") },
		{ q: $t("phelp.q3"), a: $t("phelp.a3") },
		{ q: $t("phelp.q4"), a: $t("phelp.a4") },
		{ q: $t("phelp.q5"), a: $t("phelp.a5") },
		{ q: $t("phelp.q6"), a: $t("phelp.a6") }
	]);
	let dos = $derived([$t("phelp.do1"), $t("phelp.do2"), $t("phelp.do3"), $t("phelp.do4")]);
</script>

<svelte:head>
	<title>Praesto – {$t('phelp.title')}</title>
	<meta name="description" content={$t('phelp.intro')} />
</svelte:head>

<div class="ph">
	<header class="head">
		<img src={logo} alt="Praesto" class="logo" />
		<h1>{$t('phelp.title')}</h1>
		<p class="intro">{$t('phelp.intro')}</p>
	</header>

	<section class="do-card">
		<h2>{$t('phelp.doTitle')}</h2>
		<ul>
			{#each dos as d}<li>{d}</li>{/each}
		</ul>
	</section>

	<section class="faq">
		{#each faqs as f}
			<details>
				<summary>{f.q}</summary>
				<p>{f.a}</p>
			</details>
		{/each}
	</section>

	<section class="contact">
		<h2>{$t('phelp.contactTitle')}</h2>
		<p>{$t('phelp.contactText')}</p>
		<a class="mail" href="mailto:info@praesto.ch">info@praesto.ch</a>
	</section>

	<p class="back"><a href="/privat">← {$t('phelp.back')}</a></p>
</div>

<style>
	.ph { max-width: 720px; margin: 0 auto; padding: 2rem 1rem 4rem; }
	.head { text-align: center; margin-bottom: 2rem; }
	.logo { width: 52px; height: 52px; object-fit: contain; }
	h1 { font-size: 1.8rem; color: #2d2141; margin: 0.6rem 0 0.5rem; }
	.intro { color: #5b5470; line-height: 1.55; max-width: 560px; margin: 0 auto; }

	.do-card { background: #faf8fd; border: 1px solid #ece7f0; border-radius: 1rem; padding: 1.25rem 1.5rem; margin-bottom: 2rem; }
	.do-card h2 { font-size: 1.05rem; color: #2d2141; margin: 0 0 0.6rem; }
	.do-card ul { margin: 0; padding-left: 1.1rem; color: #4b4560; line-height: 1.7; }

	.faq { display: flex; flex-direction: column; gap: 0.6rem; }
	details { background: #fff; border: 1px solid #ece7f0; border-radius: 0.85rem; padding: 0.35rem 0.25rem; }
	summary { cursor: pointer; font-weight: 600; color: #2d2141; padding: 0.7rem 1rem; list-style: none; }
	summary::-webkit-details-marker { display: none; }
	summary::before { content: "＋"; color: #7c3aed; font-weight: 700; margin-right: 0.6rem; }
	details[open] summary::before { content: "－"; }
	details p { margin: 0; padding: 0 1rem 0.9rem 2.1rem; color: #5b5470; line-height: 1.6; }

	.contact { text-align: center; margin: 2.5rem 0 1rem; }
	.contact h2 { font-size: 1.1rem; color: #2d2141; margin: 0 0 0.4rem; }
	.contact p { color: #5b5470; margin: 0 0 0.6rem; }
	.mail { display: inline-block; background: #2F124D; color: #fff; text-decoration: none; border-radius: 0.6rem; padding: 0.6rem 1.4rem; font-weight: 600; }

	.back { text-align: center; margin-top: 2rem; }
	.back a { color: #2F124D; font-weight: 600; text-decoration: none; }
</style>
