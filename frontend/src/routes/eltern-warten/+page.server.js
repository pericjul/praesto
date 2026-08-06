import { redirect } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	let parentEmail = "";
	try {
		const res = await fetch(`${API_BASE}/billing/status`, { headers: apiHeaders(locals.jwt_token) });
		if (res.ok) {
			const s = await res.json();
			// Schon bestätigt (oder Schul-/Bestandskonto)? -> weiter zur Willkommens-Umfrage.
			if (!s.parentConsentPending) {
				throw redirect(303, "/willkommen");
			}
			parentEmail = s.parentEmail ?? "";
		}
	} catch (e) {
		if (e?.status === 303) throw e;
	}
	return { parentEmail };
}

export const actions = {
	resend: async ({ locals, fetch }) => {
		await fetch(`${API_BASE}/consent/resend`, {
			method: "POST",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({})
		});
		return { resent: true };
	}
};
