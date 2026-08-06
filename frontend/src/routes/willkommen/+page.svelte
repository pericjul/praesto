<script>
	import logo from "$lib/assets/praesto-logo.png";
	import { t } from "$lib/i18n";
	let { data } = $props();
	let role = $derived(data?.role ?? "STUDENT");
	let name = $derived(data?.firstName ?? "");

	// Werte (v) bleiben stabil (sprachunabhängig gespeichert), Labels (k) werden übersetzt.
	const interests = [
		{ v: "Technik/Handwerk", k: "wk.i1" }, { v: "Gesundheit/Soziales", k: "wk.i2" },
		{ v: "Büro/KV", k: "wk.i3" }, { v: "Verkauf", k: "wk.i4" },
		{ v: "Gestaltung/Kreativ", k: "wk.i5" }, { v: "Natur/Tiere", k: "wk.i6" },
		{ v: "IT/Computer", k: "wk.i7" }, { v: "Gastronomie", k: "wk.i8" }, { v: "Noch unklar", k: "wk.i9" }
	];
	const teacherGoals = [
		{ v: "Bewerbungstraining", k: "wk.tg1" }, { v: "Aufgaben stellen", k: "wk.tg2" },
		{ v: "Fortschritt verfolgen", k: "wk.tg3" }, { v: "Bewerbungsunterlagen", k: "wk.tg4" },
		{ v: "Berufswahl begleiten", k: "wk.tg5" }
	];
	const adminGoals = [
		{ v: "Berufswahl stärken", k: "wk.ag1" }, { v: "Bewerbungskompetenz", k: "wk.ag2" },
		{ v: "Lehrpersonen entlasten", k: "wk.ag3" }, { v: "Digitalisierung", k: "wk.ag4" },
		{ v: "Chancengleichheit", k: "wk.ag5" }
	];
</script>

<svelte:head><title>Willkommen – Praesto</title></svelte:head>

<div class="wk-page">
	<div class="wk-card">
		<img src={logo} alt="Praesto" class="logo" />
		<h1>{$t('wk.welcome')}{name ? `, ${name}` : ""}! 👋</h1>
		<p class="lead">{$t('wk.lead')}</p>

		<form method="POST" class="wk-form">
			{#if role === "STUDENT"}
				<fieldset>
					<legend>{$t('wk.sWhere')}</legend>
					<label class="opt"><input type="radio" name="situation" value="weiss-nicht" required /> <span>{$t('wk.sWhere1')}</span></label>
					<label class="opt"><input type="radio" name="situation" value="schnuppern" /> <span>{$t('wk.sWhere2')}</span></label>
					<label class="opt"><input type="radio" name="situation" value="bewerben" /> <span>{$t('wk.sWhere3')}</span></label>
					<label class="opt"><input type="radio" name="situation" value="lehrstelle" /> <span>{$t('wk.sWhere4')}</span></label>
				</fieldset>

				<fieldset>
					<legend>{$t('wk.sInterests')} <span class="opt-hint">{$t('wk.multiOptional')}</span></legend>
					<div class="chips">
						{#each interests as o}
							<label class="chip"><input type="checkbox" name="interessen" value={o.v} /> <span>{$t(o.k)}</span></label>
						{/each}
					</div>
				</fieldset>

				<fieldset>
					<legend>{$t('wk.sTried')}</legend>
					<label class="opt"><input type="radio" name="geschnuppert" value="ja" /> <span>{$t('wk.tried1')}</span></label>
					<label class="opt"><input type="radio" name="geschnuppert" value="dabei" /> <span>{$t('wk.tried2')}</span></label>
					<label class="opt"><input type="radio" name="geschnuppert" value="nein" /> <span>{$t('wk.tried3')}</span></label>
				</fieldset>

			{:else if role === "TEACHER"}
				<fieldset>
					<legend>{$t('wk.tLevel')}</legend>
					<input type="text" name="stufe" placeholder={$t('wk.tLevelPlaceholder')} />
				</fieldset>
				<fieldset>
					<legend>{$t('wk.tGoals')} <span class="opt-hint">{$t('wk.multi')}</span></legend>
					<div class="chips">
						{#each teacherGoals as o}
							<label class="chip"><input type="checkbox" name="ziele" value={o.v} /> <span>{$t(o.k)}</span></label>
						{/each}
					</div>
				</fieldset>
				<fieldset>
					<legend>{$t('wk.tCount')}</legend>
					<select name="anzahl">
						<option value="">{$t('wk.choose')}</option>
						<option>{$t('wk.tCount1')}</option>
						<option>{$t('wk.tCount2')}</option>
						<option>{$t('wk.tCount3')}</option>
					</select>
				</fieldset>

			{:else}
				<fieldset>
					<legend>{$t('wk.aGoals')} <span class="opt-hint">{$t('wk.multi')}</span></legend>
					<div class="chips">
						{#each adminGoals as o}
							<label class="chip"><input type="checkbox" name="ziele" value={o.v} /> <span>{$t(o.k)}</span></label>
						{/each}
					</div>
				</fieldset>
				<fieldset>
					<legend>{$t('wk.aSize')}</legend>
					<select name="groesse">
						<option value="">{$t('wk.choose')}</option>
						<option>{$t('wk.aSize1')}</option>
						<option>{$t('wk.aSize2')}</option>
						<option>{$t('wk.aSize3')}</option>
					</select>
				</fieldset>
				<fieldset>
					<legend>{$t('wk.aNote')} <span class="opt-hint">{$t('wk.optional')}</span></legend>
					<textarea name="notiz" rows="3" placeholder={$t('wk.aNotePlaceholder')}></textarea>
				</fieldset>
			{/if}

			<div class="actions">
				<button type="submit" name="skipped" value="true" class="skip">{$t('wk.skip')}</button>
				<button type="submit" class="btn-primary">{$t('wk.start')}</button>
			</div>
		</form>
	</div>
</div>

<style>
	.wk-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 2rem 1rem; background: #F8F3EB; }
	.wk-card { width: 100%; max-width: 560px; background: #fff; border: 1px solid #ece7f0; border-radius: 1.1rem; padding: 2rem; box-shadow: 0 12px 45px rgba(47,18,77,0.08); }
	.logo { width: 48px; height: 48px; object-fit: contain; }
	h1 { font-size: 1.5rem; color: #2d2141; margin: 0.5rem 0 0.35rem; }
	.lead { color: #6b647a; font-size: 0.92rem; margin: 0 0 1.5rem; line-height: 1.55; }
	.wk-form { display: flex; flex-direction: column; gap: 1.4rem; }
	fieldset { border: none; padding: 0; margin: 0; }
	legend { font-weight: 600; color: #2d2141; margin-bottom: 0.6rem; font-size: 0.98rem; }
	.opt-hint { font-weight: 400; color: #9a93a8; font-size: 0.82rem; }
	.opt { display: flex; align-items: flex-start; gap: 0.6rem; padding: 0.5rem 0.7rem; border: 1px solid #ece7f0; border-radius: 0.6rem; margin-bottom: 0.4rem; cursor: pointer; font-size: 0.9rem; color: #3e384f; }
	.opt:hover { background: #faf8fd; border-color: #d6c8ea; }
	.opt input { margin-top: 0.15rem; }
	.chips { display: flex; flex-wrap: wrap; gap: 0.5rem; }
	.chip { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.4rem 0.7rem; border: 1px solid #ece7f0; border-radius: 999px; cursor: pointer; font-size: 0.85rem; color: #3e384f; }
	.chip:hover { background: #faf8fd; border-color: #d6c8ea; }
	input[type="text"], select, textarea { width: 100%; box-sizing: border-box; border: 1px solid #d8d2e0; border-radius: 0.55rem; padding: 0.6rem 0.75rem; font: inherit; }
	input:focus, select:focus, textarea:focus { outline: none; border-color: #2F124D; box-shadow: 0 0 0 3px rgba(47,18,77,0.1); }
	textarea { resize: vertical; }
	.actions { display: flex; justify-content: space-between; align-items: center; margin-top: 0.5rem; }
	.btn-primary { background: #2F124D; color: #fff; border: none; border-radius: 0.6rem; padding: 0.75rem 1.6rem; font: inherit; font-weight: 600; cursor: pointer; }
	.btn-primary:hover { background: #4A1C74; }
	.skip { background: none; border: none; color: #9a93a8; cursor: pointer; font: inherit; font-size: 0.88rem; text-decoration: underline; }
</style>
