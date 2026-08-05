import { redirect, fail } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	let status = null;
	try {
		const res = await fetch(`${API_BASE}/billing/status`, { headers: apiHeaders(locals.jwt_token) });
		if (res.ok) status = await res.json();
	} catch {
		status = null;
	}
	return { status };
}

async function postJson(fetch, token, path, body) {
	const res = await fetch(`${API_BASE}${path}`, {
		method: "POST",
		headers: apiHeaders(token),
		body: JSON.stringify(body ?? {})
	});
	return res;
}

export const actions = {
	// Stripe-Checkout starten und zur Stripe-Bezahlseite weiterleiten.
	checkout: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const plan = data.get("plan") === "yearly" ? "yearly" : "monthly";
		const res = await postJson(fetch, locals.jwt_token, "/billing/checkout", { plan });
		if (!res.ok) {
			const msg = await res.text().catch(() => "");
			return fail(400, { error: msg && msg.length < 300 ? msg : "Bezahlung konnte nicht gestartet werden." });
		}
		const { url } = await res.json();
		throw redirect(303, url);
	},

	// Stripe-Kundenportal öffnen (Abo verwalten/kündigen).
	portal: async ({ locals, fetch }) => {
		const res = await postJson(fetch, locals.jwt_token, "/billing/portal", {});
		if (!res.ok) {
			const msg = await res.text().catch(() => "");
			return fail(400, { error: msg && msg.length < 300 ? msg : "Kundenportal konnte nicht geöffnet werden." });
		}
		const { url } = await res.json();
		throw redirect(303, url);
	}
};
