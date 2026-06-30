import { fail } from "@sveltejs/kit";
import { API_BASE } from "$lib/server/api.js";

export async function load({ locals }) {
	// Prefill, falls eingeloggt (Seite ist auch öffentlich erreichbar).
	return { user: locals.user ?? null };
}

export const actions = {
	default: async ({ request, fetch }) => {
		const d = await request.formData();
		const payload = {
			name: d.get("name"),
			email: d.get("email"),
			message: d.get("message"),
			interest: "Hilfe-Frage"
		};
		if (!payload.name || !payload.email || !payload.message) {
			return fail(400, { error: true });
		}
		try {
			const res = await fetch(`${API_BASE}/contact`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify(payload)
			});
			if (!res.ok) {
				return fail(400, { error: true });
			}
		} catch (e) {
			console.error("Hilfe-Frage senden fehlgeschlagen:", e);
			return fail(500, { error: true });
		}
		return { success: true };
	}
};
