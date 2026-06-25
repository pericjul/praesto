import { redirect, fail } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "STUDENT") {
		throw redirect(302, "/");
	}
	let quota = null;
	try {
		const q = await fetch(`${API_BASE}/student/ai-quota`, { headers: apiHeaders(locals.jwt_token) });
		if (q.ok) {
			quota = await q.json().catch(() => null);
		}
	} catch {
		quota = null;
	}
	return { quota, user: locals.user };
}

export const actions = {
	default: async ({ request, locals, fetch }) => {
		const d = await request.formData();
		const payload = {
			fullName: d.get("fullName"),
			birthDate: d.get("birthDate"),
			address: d.get("address"),
			phone: d.get("phone"),
			email: d.get("email"),
			targetJob: d.get("targetJob"),
			school: d.get("school"),
			experience: d.get("experience"),
			skills: d.get("skills"),
			languages: d.get("languages"),
			hobbies: d.get("hobbies"),
			extra: d.get("extra")
		};
		const res = await fetch(`${API_BASE}/student/cv`, {
			method: "POST",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify(payload)
		});
		if (!res.ok) {
			const msg = await res.text().catch(() => null);
			return fail(400, { error: msg || "error" });
		}
		throw redirect(303, "/student/dossier");
	}
};
