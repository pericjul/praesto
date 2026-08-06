import { redirect } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

// Privat-Konten (INDIVIDUAL) ohne aktive Testphase/Abo werden auf die Abo-Seite geleitet.
// Schul-/Bestandskonten sind nie betroffen.
export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}

	if (locals.user?.accountType === "INDIVIDUAL") {
		let hasAccess = true;
		let consentPending = false;
		try {
			const res = await fetch(`${API_BASE}/billing/status`, { headers: apiHeaders(locals.jwt_token) });
			if (res.ok) {
				const s = await res.json();
				hasAccess = s.hasAccess !== false;
				consentPending = s.parentConsentPending === true;
			}
		} catch {
			// Im Zweifel Zugriff lassen (Sperre erzwingt ohnehin das Backend mit 402).
			hasAccess = true;
		}
		if (consentPending) {
			throw redirect(303, "/eltern-warten");
		}
		if (!hasAccess) {
			throw redirect(303, "/abo");
		}
	}

	return {};
}
