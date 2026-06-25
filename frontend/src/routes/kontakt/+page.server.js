import { fail } from "@sveltejs/kit";
import { API_BASE } from "$lib/server/api.js";

export const actions = {
	default: async ({ request, fetch }) => {
		const data = await request.formData();
		const payload = {
			name: data.get("name"),
			email: data.get("email"),
			organisation: data.get("organisation"),
			role: data.get("role"),
			interest: data.get("interest"),
			classes: data.get("classes") ? Number(data.get("classes")) : null,
			students: data.get("students") ? Number(data.get("students")) : null,
			wantsMeeting: data.get("wantsMeeting") === "on",
			message: data.get("message")
		};

		if (!payload.name || !payload.email) {
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
			console.error("Kontakt-Senden fehlgeschlagen:", e);
			return fail(500, { error: true });
		}

		return { success: true };
	}
};
