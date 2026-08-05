import { redirect } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";
import { dashboardForRole } from "$lib/server/auth.service.js";

export async function load({ locals }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	return {
		role: locals.user?.role ?? "STUDENT",
		firstName: locals.user?.firstName ?? ""
	};
}

// FormData -> einfaches Objekt (Mehrfachwerte als Array).
function formToObject(data) {
	const out = {};
	for (const key of data.keys()) {
		if (key in out) continue;
		const all = data.getAll(key).filter((v) => v !== "");
		if (all.length === 0) continue;
		out[key] = all.length > 1 ? all : all[0];
	}
	return out;
}

export const actions = {
	default: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const answers = formToObject(data);
		try {
			await fetch(`${API_BASE}/onboarding`, {
				method: "POST",
				headers: apiHeaders(locals.jwt_token),
				body: JSON.stringify(answers)
			});
		} catch {
			// Umfrage ist optional – bei Fehler trotzdem weiterleiten.
		}
		throw redirect(303, dashboardForRole(locals.user?.role));
	}
};
