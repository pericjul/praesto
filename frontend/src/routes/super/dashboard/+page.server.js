import { redirect, fail } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

export async function load({ locals, fetch, url }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "SUPER_ADMIN") {
		throw redirect(302, "/");
	}

	const res = await fetch(`${API_BASE}/super/schools`, { headers: apiHeaders(locals.jwt_token) });
	return {
		schools: res.ok ? await res.json() : [],
		origin: url.origin
	};
}

export const actions = {
	createSchool: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const name = data.get("name");
		const canton = data.get("canton");
		const city = data.get("city");

		if (!name) {
			return fail(400, { error: "Schulname ist erforderlich." });
		}

		const res = await fetch(`${API_BASE}/super/schools`, {
			method: "POST",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ name, canton, city })
		});
		if (!res.ok) {
			return fail(400, { error: "Schule konnte nicht erstellt werden." });
		}
		return { created: await res.json() };
	},

	createAdminInvite: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const schoolId = data.get("schoolId");
		const res = await fetch(`${API_BASE}/super/schools/${schoolId}/invite`, {
			method: "POST",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({})
		});
		if (!res.ok) {
			return fail(400, { error: "Einladung konnte nicht erstellt werden." });
		}
		return { invite: await res.json() };
	}
};
