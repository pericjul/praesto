import { redirect, fail } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";
import { tr } from "$lib/server/i18n.js";

export async function load({ locals }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "STUDENT") {
		throw redirect(302, "/");
	}
	return { user: locals.user };
}

export const actions = {
	default: async ({ request, locals, fetch }) => {
		const d = await request.formData();
		const beruf = d.get("beruf")?.toString().trim();
		if (!beruf) {
			return fail(400, { error: tr(locals.lang, "schn.needBeruf") });
		}
		const payload = {
			beruf,
			firma: d.get("firma"),
			kontaktperson: d.get("kontaktperson"),
			deinName: d.get("deinName"),
			klasse: d.get("klasse"),
			zeitraum: d.get("zeitraum")
		};
		try {
			const res = await fetch(`${API_BASE}/student/schnupper-request`, {
				method: "POST",
				headers: apiHeaders(locals.jwt_token),
				body: JSON.stringify(payload)
			});
			if (!res.ok) {
				return fail(400, { error: "Konnte nicht erstellt werden. Bitte versuch es nochmal." });
			}
			const reply = await res.json();
			return { reply, beruf };
		} catch {
			return fail(500, { error: "Verbindungsfehler. Bitte versuch es nochmal." });
		}
	}
};
