<script>
	import { enhance } from "$app/forms";
	import { invalidateAll } from "$app/navigation";
	import { t } from "$lib/i18n";

	let { data, form } = $props();

	let sources = $state(data.sources ?? []);
	$effect(() => { sources = data.sources ?? []; });

	function refresh() {
		return async ({ result, update }) => {
			if (result.type === "success") {
				await invalidateAll();
			}
			await update({ reset: false });
		};
	}

	function typeLabel(type) {
		if (type === "LINK") return $t("skn.typeLink");
		if (type === "DOCUMENT") return $t("skn.typeDocument");
		return $t("skn.typeText");
	}

	function typeIcon(type) {
		if (type === "LINK") return "🔗";
		if (type === "DOCUMENT") return "📄";
		return "📝";
	}

	function fmtDate(d) {
		if (!d) return "";
		try {
			return new Date(d).toLocaleDateString("de-CH", { day: "2-digit", month: "2-digit", year: "numeric" });
		} catch {
			return "";
		}
	}
</script>

<div class="wrap">
	<h1>{$t("skn.title")}</h1>
	<p class="intro">{$t("skn.intro")}</p>
	<p class="note">{$t("skn.note")}</p>

	{#if form?.error}
		<p class="err">{form.error}</p>
	{/if}

	<div class="forms">
		<!-- Link -->
		<section class="card">
			<h2>🔗 {$t("skn.addLinkTitle")}</h2>
			<form method="POST" action="?/addLink" use:enhance={refresh}>
				<input name="url" type="text" placeholder="https://…" required />
				<input name="title" type="text" placeholder={$t("skn.titleOpt")} />
				<button type="submit">{$t("skn.add")}</button>
			</form>
		</section>

		<!-- Dokument -->
		<section class="card">
			<h2>📄 {$t("skn.addDocTitle")}</h2>
			<form method="POST" action="?/addDocument" enctype="multipart/form-data" use:enhance={refresh}>
				<label class="filelabel">{$t("skn.file")}
					<input name="file" type="file" accept=".pdf,.docx,.txt" required />
				</label>
				<input name="title" type="text" placeholder={$t("skn.titleOpt")} />
				<button type="submit">{$t("skn.upload")}</button>
			</form>
		</section>

		<!-- Notiz -->
		<section class="card">
			<h2>📝 {$t("skn.addTextTitle")}</h2>
			<form method="POST" action="?/addText" use:enhance={refresh}>
				<input name="title" type="text" placeholder={$t("skn.titleOpt")} />
				<textarea name="text" rows="4" placeholder={$t("skn.text")} required></textarea>
				<button type="submit">{$t("skn.save")}</button>
			</form>
		</section>
	</div>

	<h2 class="listhead">{$t("skn.sources")} <span class="count">{sources.length}</span></h2>

	{#if sources.length === 0}
		<p class="empty">{$t("skn.empty")}</p>
	{:else}
		<ul class="list">
			{#each sources as s (s.id)}
				<li class="item" class:off={!s.enabled}>
					<div class="meta">
						<span class="tag">{typeIcon(s.type)} {typeLabel(s.type)}</span>
						<span class="itemtitle">{s.title}</span>
						{#if s.url}
							<a class="link" href={s.url} target="_blank" rel="noopener noreferrer">{s.url}</a>
						{/if}
						<span class="sub">
							{$t("skn.chars").replace("%N", (s.content?.length ?? 0).toLocaleString("de-CH"))}
							· {fmtDate(s.createdAt)}
							· {s.enabled ? $t("skn.active") : $t("skn.inactive")}
						</span>
					</div>
					<div class="actions">
						<form method="POST" action="?/setEnabled" use:enhance={refresh}>
							<input type="hidden" name="id" value={s.id} />
							<input type="hidden" name="enabled" value={(!s.enabled).toString()} />
							<button type="submit" class="ghost">
								{s.enabled ? $t("skn.disable") : $t("skn.enable")}
							</button>
						</form>
						<form
							method="POST"
							action="?/delete"
							use:enhance={refresh}
							onsubmit={(e) => { if (!confirm($t("skn.confirmDelete"))) e.preventDefault(); }}
						>
							<input type="hidden" name="id" value={s.id} />
							<button type="submit" class="del">{$t("skn.delete")}</button>
						</form>
					</div>
				</li>
			{/each}
		</ul>
	{/if}
</div>

<style>
	.wrap { max-width: 900px; margin: 0 auto; padding: 1.5rem 1rem 4rem; }
	h1 { font-size: 1.6rem; margin: 0 0 .5rem; }
	.intro { color: #374151; margin: 0 0 .35rem; }
	.note { color: #6b7280; font-size: .85rem; margin: 0 0 1.25rem; }
	.err { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; padding: .6rem .8rem; border-radius: 8px; margin-bottom: 1rem; }

	.forms { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1rem; margin-bottom: 2rem; }
	.card { border: 1px solid #e5e7eb; border-radius: 12px; padding: 1rem; background: #fff; }
	.card h2 { font-size: 1rem; margin: 0 0 .75rem; }
	.card form { display: flex; flex-direction: column; gap: .5rem; }
	input, textarea { border: 1px solid #d1d5db; border-radius: 8px; padding: .5rem .6rem; font: inherit; width: 100%; box-sizing: border-box; }
	textarea { resize: vertical; }
	.filelabel { display: flex; flex-direction: column; gap: .3rem; font-size: .8rem; color: #6b7280; }
	button { border: none; border-radius: 8px; padding: .5rem .9rem; font: inherit; cursor: pointer; background: #2563eb; color: #fff; align-self: flex-start; }
	button:hover { background: #1d4ed8; }

	.listhead { font-size: 1.15rem; margin: 0 0 .75rem; }
	.count { background: #eef2ff; color: #4338ca; border-radius: 999px; padding: .1rem .55rem; font-size: .8rem; margin-left: .35rem; }
	.empty { color: #6b7280; }

	.list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: .6rem; }
	.item { display: flex; justify-content: space-between; gap: 1rem; align-items: flex-start; border: 1px solid #e5e7eb; border-radius: 10px; padding: .8rem 1rem; background: #fff; }
	.item.off { opacity: .55; background: #f9fafb; }
	.meta { display: flex; flex-direction: column; gap: .25rem; min-width: 0; }
	.tag { font-size: .75rem; color: #6b7280; }
	.itemtitle { font-weight: 600; }
	.link { font-size: .8rem; color: #2563eb; word-break: break-all; }
	.sub { font-size: .75rem; color: #9ca3af; }
	.actions { display: flex; gap: .4rem; flex-shrink: 0; }
	.actions button { align-self: auto; }
	.ghost { background: #f3f4f6; color: #374151; }
	.ghost:hover { background: #e5e7eb; }
	.del { background: #fef2f2; color: #b91c1c; }
	.del:hover { background: #fee2e2; }
</style>
