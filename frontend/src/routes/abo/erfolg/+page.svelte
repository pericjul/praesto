<script>
	import logo from "$lib/assets/praesto-logo.png";
</script>

<svelte:head><title>Danke – Praesto</title></svelte:head>

<div class="ok-page">
	<div class="ok-card">
		<img src={logo} alt="Praesto" class="logo" />
		<div class="check">✅</div>
		<h1>Vielen Dank!</h1>
		<p>Deine Zahlung war erfolgreich. Dein Zugang wird gerade aktiviert – das dauert nur einen Moment.</p>
		<a href="/student/dashboard" class="btn">Zum Dashboard</a>
		<p class="hint">Falls dein Zugang noch nicht frei ist, lade die Seite in ein paar Sekunden neu.</p>
	</div>
</div>

<style>
	.ok-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 2rem 1rem; background: #faf8f3; }
	.ok-card { background: #fff; border: 1px solid #ece7f0; border-radius: 1rem; padding: 2.5rem 2rem; max-width: 440px; text-align: center; box-shadow: 0 10px 40px rgba(47,18,77,0.08); }
	.logo { width: 48px; height: 48px; object-fit: contain; }
	.check { font-size: 2.5rem; margin: 0.5rem 0; }
	h1 { font-size: 1.5rem; color: #2d2141; margin: 0 0 0.5rem; }
	p { color: #5b5470; margin: 0 0 1.25rem; }
	.btn { display: inline-block; background: #2F124D; color: #fff; border-radius: 0.6rem; padding: 0.7rem 1.4rem; font-weight: 600; text-decoration: none; }
	.btn:hover { background: #3f1a66; }
	.hint { font-size: 0.8rem; color: #9a93a8; margin: 1rem 0 0; }
</style>
