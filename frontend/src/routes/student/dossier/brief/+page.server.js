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
			senderAddress: d.get("senderAddress"),
			phone: d.get("phone"),
			email: d.get("email"),
			city: d.get("city"),
			companyName: d.get("companyName"),
			companyAddress: d.get("companyAddress"),
			contactPerson: d.get("contactPerson"),
			targetJob: d.get("targetJob"),
			applicationSource: d.get("applicationSource"),
			startDate: d.get("startDate"),
			whyCompany: d.get("whyCompany"),
			strengths: d.get("strengths"),
			schnupperExperience: d.get("schnupperExperience"),
			availability: d.get("availability"),
			extra: d.get("extra")
		};
		const res = await fetch(`${API_BASE}/student/cover-letter`, {
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
