import { redirect, fail } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

export async function load({ locals }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	return { user: locals.user };
}

export const actions = {
	default: async ({ request, locals, fetch }) => {
		const d = await request.formData();
		const payload = {
			title: d.get("title"),
			description: d.get("description"),
			area: d.get("area") || "OTHER",
			severity: d.get("severity") || "MEDIUM",
			steps: d.get("steps") || "",
			device: d.get("device") || "",
			screenshot: d.get("screenshot") || ""
		};
		if (!payload.title || !payload.description) {
			return fail(400, { error: true });
		}
		try {
			const res = await fetch(`${API_BASE}/bugs`, {
				method: "POST",
				headers: apiHeaders(locals.jwt_token),
				body: JSON.stringify(payload)
			});
			if (!res.ok) {
				return fail(400, { error: true });
			}
		} catch (e) {
			console.error("Bug-Melden fehlgeschlagen:", e);
			return fail(500, { error: true });
		}
		return { success: true };
	}
};
