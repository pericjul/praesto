<script>
	import logo from "$lib/assets/praesto-logo.png";
	let { data } = $props();
	let role = $derived(data?.role ?? "STUDENT");
	let name = $derived(data?.firstName ?? "");
</script>

<svelte:head><title>Willkommen – Praesto</title></svelte:head>

<div class="wk-page">
	<div class="wk-card">
		<img src={logo} alt="Praesto" class="logo" />
		<h1>Willkommen{name ? `, ${name}` : ""}! 👋</h1>
		<p class="lead">Beantworte kurz ein paar Fragen – so können wir Praesto besser auf dich abstimmen. Dauert nur eine Minute.</p>

		<form method="POST" class="wk-form">
			{#if role === "STUDENT"}
				<fieldset>
					<legend>Wo stehst du gerade?</legend>
					<label class="opt"><input type="radio" name="situation" value="weiss-nicht" required /> <span>Ich weiss noch nicht, welcher Beruf zu mir passt</span></label>
					<label class="opt"><input type="radio" name="situation" value="schnuppern" /> <span>Ich suche eine Schnupperlehre</span></label>
					<label class="opt"><input type="radio" name="situation" value="bewerben" /> <span>Ich bewerbe mich gerade</span></label>
					<label class="opt"><input type="radio" name="situation" value="lehrstelle" /> <span>Ich habe schon eine Lehrstelle 🎉</span></label>
				</fieldset>

				<fieldset>
					<legend>Welche Bereiche interessieren dich? <span class="opt-hint">(Mehrfachauswahl, optional)</span></legend>
					<div class="chips">
						{#each ["Technik/Handwerk","Gesundheit/Soziales","Büro/KV","Verkauf","Gestaltung/Kreativ","Natur/Tiere","IT/Computer","Gastronomie","Noch unklar"] as area}
							<label class="chip"><input type="checkbox" name="interessen" value={area} /> <span>{area}</span></label>
						{/each}
					</div>
				</fieldset>

				<fieldset>
					<legend>Hast du schon geschnuppert?</legend>
					<label class="opt"><input type="radio" name="geschnuppert" value="ja" /> <span>Ja</span></label>
					<label class="opt"><input type="radio" name="geschnuppert" value="dabei" /> <span>Bin gerade dabei</span></label>
					<label class="opt"><input type="radio" name="geschnuppert" value="nein" /> <span>Noch nicht</span></label>
				</fieldset>

			{:else if role === "TEACHER"}
				<fieldset>
					<legend>Welche Stufe / Klasse unterrichtest du?</legend>
					<input type="text" name="stufe" placeholder="z.B. Sek B, 3a" />
				</fieldset>
				<fieldset>
					<legend>Was möchtest du mit Praesto vor allem tun? <span class="opt-hint">(Mehrfachauswahl)</span></legend>
					<div class="chips">
						{#each ["Bewerbungstraining","Aufgaben stellen","Fortschritt verfolgen","Bewerbungsunterlagen","Berufswahl begleiten"] as g}
							<label class="chip"><input type="checkbox" name="ziele" value={g} /> <span>{g}</span></label>
						{/each}
					</div>
				</fieldset>
				<fieldset>
					<legend>Wie viele Schüler:innen ungefähr?</legend>
					<select name="anzahl">
						<option value="">Bitte wählen</option>
						<option>1 Klasse (bis ~25)</option>
						<option>2–3 Klassen</option>
						<option>4+ Klassen</option>
					</select>
				</fieldset>

			{:else}
				<fieldset>
					<legend>Was ist euer Ziel mit Praesto? <span class="opt-hint">(Mehrfachauswahl)</span></legend>
					<div class="chips">
						{#each ["Berufswahl stärken","Bewerbungskompetenz","Lehrpersonen entlasten","Digitalisierung","Chancengleichheit"] as g}
							<label class="chip"><input type="checkbox" name="ziele" value={g} /> <span>{g}</span></label>
						{/each}
					</div>
				</fieldset>
				<fieldset>
					<legend>Wie gross ist eure Schule ungefähr?</legend>
					<select name="groesse">
						<option value="">Bitte wählen</option>
						<option>bis 50 Schüler:innen</option>
						<option>50–200</option>
						<option>über 200</option>
					</select>
				</fieldset>
				<fieldset>
					<legend>Gibt es etwas, das wir wissen sollten? <span class="opt-hint">(optional)</span></legend>
					<textarea name="notiz" rows="3" placeholder="Wünsche, Fragen, Kontext …"></textarea>
				</fieldset>
			{/if}

			<div class="actions">
				<button type="submit" name="skipped" value="true" class="skip">Überspringen</button>
				<button type="submit" class="btn-primary">Los geht's</button>
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
