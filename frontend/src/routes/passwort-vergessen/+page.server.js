import { fail } from "@sveltejs/kit";
import { API_BASE } from "$lib/server/api.js";
import { tr } from "$lib/server/i18n.js";

export const actions = {
	default: async ({ request, fetch, locals }) => {
		const data = await request.formData();
		const email = data.get("email")?.toString().trim();
		if (!email || !email.includes("@")) {
			return fail(400, { error: tr(locals.lang, "verr.invalidEmail") });
		}
		try {
			await fetch(`${API_BASE}/auth/forgot-password`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({ email })
			});
		} catch {
			// Auch bei Fehler neutral bleiben.
		}
		// Immer neutral (verrät nicht, ob die E-Mail existiert).
		return { sent: true };
	}
};
